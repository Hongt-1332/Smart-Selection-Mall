// pages/user/vip/vip.js — VIP 会员（迁移自 src/views/user/UserVip.vue）
const { vipApi } = require('../../../utils/api/vip')
const { store } = require('../../../utils/store')
const { msgSuccess, msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')

Page({
  data: {
    plans: [],
    /** 当前用户 VIP 等级（0 = 普通用户） */
    currentLevel: 0,
    /** 是否已达页面内最高等级 */
    allMaxed: false,
    loading: 0,
    payVisible: false,
    payLoading: false,
    selectedPlan: null,
  },

  onLoad() {
    this.fetchVipConfigs()
  },

  buildBenefits(config) {
    const b = []
    if (config.maxAddressQuantity > 0) b.push(`最多 ${config.maxAddressQuantity} 个收货地址`)
    if (config.monthlyUpdateGoods > 0) b.push(`每月 ${config.monthlyUpdateGoods} 次商品上传`)
    if (config.monthlyUpdateAvatar > 0) b.push(`每月 ${config.monthlyUpdateAvatar} 次头像更新`)
    if (config.monthlyUpdateBackground > 0) b.push(`每月 ${config.monthlyUpdateBackground} 次背景更新`)
    if (config.maxCartQuantity > 0) b.push(`购物车上限 ${config.maxCartQuantity} 件`)
    if (config.maxGoodsQuantity > 0) b.push(`最多上架 ${config.maxGoodsQuantity} 件商品`)
    if (config.maxAiQuantity > 0) b.push(`每月 ${config.maxAiQuantity} 次AI推荐`)
    b.push('VIP 专属标识')
    return b
  },

  async fetchVipConfigs() {
    try {
      // 套餐列表来自 /vip/config（VipConfig 全量列表）；
      // /vip/level 只返回当前用户等级（level/name/price/description），
      // 之前错把 level 当配置列表用，res.data.levelConfigs 永远是 undefined → 页面空白。
      const [cfgRes, lvRes] = await Promise.all([
        vipApi.getVipConfig(),
        vipApi.getVipLevel().catch(() => null),
      ])

      const currentLevel = lvRes && lvRes.code === 200 && lvRes.data
        ? Number(lvRes.data.level) || 0
        : 0

      if (cfgRes.code === 200 && Array.isArray(cfgRes.data)) {
        // level 0 是「普通用户」默认档（¥0 / 0 天），不是可购买的套餐，必须过滤掉，
        // 否则页面会出现一张 ¥0.00 的怪卡片
        const purchasable = cfgRes.data
          .filter((c) => c.level > 0)
          .sort((a, b) => a.level - b.level)
        const plans = purchasable.map((config) => ({
          id: config.level,
          level: config.level,
          name: config.levelName || `Lv.${config.level} 会员`,
          icon: '⭐',
          price: config.price,
          period: `${config.vipDuration || 30} 天`,
          featured: config.level === 3,
          // 只有「恰好买到的那个等级」算已开通（当前等级）；
          // 更低的等级只是被当前等级覆盖，不算开通过，仍正常展示
          isExact: currentLevel === Number(config.level),
          isLower: currentLevel > Number(config.level),
          benefits: this.buildBenefits(config),
        }))
        // 是否已达最高等级（当前等级 ≥ 页面内最高套餐）
        const maxLevel = purchasable.length ? purchasable[purchasable.length - 1].level : 0
        const allMaxed = currentLevel > 0 && currentLevel >= maxLevel
        this.setData({ currentLevel, plans, allMaxed })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    }
  },

  handleBuy(e) {
    const plan = this.data.plans[e.currentTarget.dataset.index]
    if (!plan) return
    // 双保险：即使按钮 disabled 状态因缓存等原因失效，点击时也直接拦截
    if (plan.isExact) {
      wx.showToast({ title: `您已开通${plan.name}，无需重复开通`, icon: 'none', duration: 2200 })
      return
    }
    if (plan.isLower) {
      wx.showToast({ title: '您的等级已覆盖该方案，无需重复开通', icon: 'none', duration: 2200 })
      return
    }
    this.setData({ selectedPlan: plan, payVisible: true })
  },

  closePay() {
    this.setData({ payVisible: false })
  },

  async confirmPay() {
    const plan = this.data.selectedPlan
    if (!plan) return
    this.setData({ payLoading: true })
    try {
      // num 是后端 VipBuyRequest 的必填字段（@NotNull @Min(1)），固定买 1 份
      const res = await vipApi.buyVip(plan.level, plan.price, 1)
      if (res.code === 200) {
        msgSuccess(`已成功开通${plan.name}！`)
        this.setData({ payVisible: false })
        // 购买扣了余额、升了等级：刷新全局用户信息缓存，
        // 让用户中心的余额 / 等级立即同步
        if (store && typeof store.fetchUserInfoVO === 'function') {
          store.fetchUserInfoVO()
        }
        // 重新拉取套餐与当前等级，让「当前等级」标记立即更新
        this.fetchVipConfigs()
      } else {
        // 业务拒绝（余额不足 / 等级已达标 / 配置不存在）——把后端原话弹出来
        msgError(res.message || `购买失败(code=${res.code})`)
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ payLoading: false })
    }
  },

  noop() {},
})