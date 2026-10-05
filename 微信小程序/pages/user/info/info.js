// pages/user/info/info.js — 个人信息（迁移自 src/views/user/UserInfo.vue）
const { userApi } = require('../../../utils/api/user')
const { vipApi } = require('../../../utils/api/vip')
const { store } = require('../../../utils/store')
const { msgSuccess, msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { money } = require('../../../utils/format')
const { navigate } = require('../../../utils/nav')
const { ensureLogin } = require('../../../utils/auth')

/** 默认额度上限，与原项目 utils/quota.ts 的 DEFAULT_QUOTA_LIMITS 一致 */
const DEFAULT_LIMITS = {
  monthlyUpdateBackground: 5,
  monthlyUpdateAvatar: 3,
  monthlyUpdateGoods: 20,
  maxCartQuantity: 50,
}

Page({
  data: {
    isLoggedIn: false,
    loading: false,
    updateLoading: false,
    describeLoading: false,
    uploadLoading: false,
    dataLoaded: false,

    updateForm: {
      account: '',
      userName: '',
      email: '',
      phone: '',
      balanceText: '0.00',
      describe: '',
      avatarPath: '',
    },
    imageUrl: '',
    userLevel: 0,
    userLevelName: '普通用户',
    vipRemainingTime: 0,
    vipLevelText: '普通用户',

    // 背景管理
    backgroundList: [],
    bgSlots: [null, null, null, null],
    uploadingSlot: 0,

    // 额度
    quotaItems: [],
    limits: { ...DEFAULT_LIMITS },
    cartUsed: 0,
    quotaData: { bg: 0, avatar: 0, goods: 0 },

    // VIP
    vipTier: { name: '普通用户', desc: '升级VIP享受更多特权', benefits: [] },
    showVipDialog: false,
    vipPlans: [],
    vipBuyLoading: 0,
    /** 是否已达最高等级（满级时隐藏升级入口） */
    vipMaxed: false,
  },

  onLoad() {
    this.setData({ isLoggedIn: store.isLoggedIn })
    if (store.isLoggedIn) this.loadUserData()
  },

  onShow() {
    const logged = store.isLoggedIn
    this.setData({ isLoggedIn: logged })
    if (logged && store.dirty) this.loadUserData()
  },

  /** 微信一键登录（含协议确认）；成功后直接加载用户数据 */
  async goLogin() {
    const ok = await ensureLogin()
    if (ok) {
      this.setData({ isLoggedIn: true })
      this.loadUserData()
    }
  },

  async loadUserData() {
    await this.fetchUserInfo()
    this.fetchCartCount()
    this.fetchVipLevel()
  },

  async fetchUserInfo() {
    this.setData({ loading: true })
    try {
      const info = await store.fetchUserInfoVO(true)
      if (info) {
        this.setData({
          updateForm: {
            account: info.account || '',
            userName: info.userName || '',
            email: info.email || '',
            phone: info.phone || '',
            balanceText: money(info.balance),
            describe: info.describe || '',
            avatarPath: info.avatarPath || '',
          },
          userLevel: info.level || 0,
          userLevelName: info.vipLevelName || '普通用户',
          vipRemainingTime: info.vipRemainingTime || 0,
          quotaData: {
            bg: info.uploadBackground || 0,
            avatar: info.updateAvatar || 0,
            goods: info.uploadGoods || 0,
          },
          limits: {
            monthlyUpdateBackground: info.monthlyUpdateBackground || DEFAULT_LIMITS.monthlyUpdateBackground,
            monthlyUpdateAvatar: info.monthlyUpdateAvatar || DEFAULT_LIMITS.monthlyUpdateAvatar,
            monthlyUpdateGoods: info.monthlyUpdateGoods || DEFAULT_LIMITS.monthlyUpdateGoods,
            maxCartQuantity: info.maxCartQuantity || DEFAULT_LIMITS.maxCartQuantity,
          },
          backgroundList: (info.backgrounds || []).map((bg) => ({
            id: bg.id,
            imagePath: bg.imagePath,
            sequence: bg.sequence,
          })),
        })
        this.buildVipLevelText()
        this.buildQuotaItems()
        this.buildBgSlots()
        await this.loadAvatar(info.avatarPath)
        await this.loadBackgrounds()
      }
    } catch (e) {
      this.handleError(e)
    } finally {
      this.setData({ loading: false, dataLoaded: true })
    }
  },

  buildVipLevelText() {
    const { userLevel, userLevelName, vipRemainingTime } = this.data
    const text =
      userLevel > 0
        ? `${userLevelName}${vipRemainingTime > 0 ? ` · 剩余${vipRemainingTime}天` : ''}`
        : '普通用户'
    this.setData({ vipLevelText: text })
  },

  buildQuotaItems() {
    const { limits, quotaData, cartUsed } = this.data
    const items = [
      { key: 'bg', icon: '🖼️', label: '背景上传', unit: '次', used: quotaData.bg, max: limits.monthlyUpdateBackground },
      { key: 'goods', icon: '📦', label: '商品上传', unit: '次', used: quotaData.goods, max: limits.monthlyUpdateGoods },
      { key: 'avatar', icon: '👤', label: '头像更新', unit: '次', used: quotaData.avatar, max: limits.monthlyUpdateAvatar },
      { key: 'cart', icon: '🛒', label: '购物车', unit: '件', used: cartUsed, max: limits.maxCartQuantity },
    ].map((it) => {
      const percent = it.max > 0 ? Math.min(100, Math.round((it.used / it.max) * 100)) : 0
      let color = '#5b8def'
      if (percent >= 100) color = '#f56c6c'
      else if (percent >= 80) color = '#e6a23c'
      // 在 JS 中拼好完整 style，避免 WXML 内联模板被解析为 CSS 报错
      const barStyle = `width:${percent}%;background:${color};`
      return { ...it, percent, color, barStyle }
    })
    this.setData({ quotaItems: items })
  },

  async loadAvatar(imagePath) {
    if (!imagePath) {
      this.setData({ imageUrl: '' })
      return
    }
    try {
      const res = await userApi.getImages([imagePath])
      if (res.code === 200 && res.data) {
        this.setData({ imageUrl: res.data[imagePath] || imagePath })
      } else {
        this.setData({ imageUrl: imagePath })
      }
    } catch (e) {
      this.setData({ imageUrl: imagePath })
    }
  },

  async loadBackgrounds() {
    const list = this.data.backgroundList
    const paths = list.map((bg) => bg.imagePath).filter(Boolean)
    if (!paths.length) return
    try {
      const res = await userApi.getImages(paths)
      if (res.code === 200 && res.data) {
        const next = list.map((bg) => ({ ...bg, imagePath: res.data[bg.imagePath] || bg.imagePath }))
        this.setData({ backgroundList: next })
        this.buildBgSlots()
      }
    } catch (e) {
      // ignore
    }
  },

  buildBgSlots() {
    const list = this.data.backgroundList
    const slots = [null, null, null, null]
    for (let i = 1; i <= 4; i++) {
      const found = list.find((b) => b.sequence === i)
      if (found) slots[i - 1] = found
    }
    this.setData({ bgSlots: slots })
  },

  // ===== 编辑资料 =====
  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`updateForm.${field}`]: e.detail.value })
  },

  async handleUpdate() {
    const f = this.data.updateForm
    if (!f.userName) {
      msgError('请输入用户名')
      return
    }
    if (f.userName.length < 2 || f.userName.length > 20) {
      msgError('用户名需2-20个字符')
      return
    }
    if (!f.email) {
      msgError('请输入邮箱')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) {
      msgError('邮箱格式不正确')
      return
    }
    if (!/^1[3-9]\d{9}$/.test(f.phone)) {
      msgError('手机号格式不正确')
      return
    }
    this.setData({ updateLoading: true })
    try {
      const res = await userApi.update({ userName: f.userName, email: f.email, phone: f.phone })
      if (res.code === 200) {
        msgSuccess('更新成功')
        store.markDirty()
        await store.fetchUserInfo()
      } else {
        msgError(res.message || '更新失败')
      }
    } catch (e) {
      this.handleError(e)
    } finally {
      this.setData({ updateLoading: false })
    }
  },

  async handleSaveDescribe() {
    this.setData({ describeLoading: true })
    try {
      const res = await userApi.updateDescribe(this.data.updateForm.describe)
      if (res.code === 200) {
        msgSuccess('简介已保存')
        store.markDirty()
      } else {
        msgError(res.message || '保存失败')
      }
    } catch (e) {
      this.handleError(e)
    } finally {
      this.setData({ describeLoading: false })
    }
  },

  // ===== 头像上传 =====
  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success: (res) => {
        const file = res.tempFiles[0]
        if (file.size / 1024 / 1024 > 5) {
          msgError('图片大小不能超过5MB!')
          return
        }
        this.uploadAvatar(file.tempFilePath)
      },
    })
  },

  async uploadAvatar(filePath) {
    this.setData({ uploadLoading: true })
    // 前台显示本地预览
    this.setData({ imageUrl: filePath })
    try {
      const res = await userApi.updateImage(filePath)
      if (res.code === 200) {
        msgSuccess('头像上传成功')
        store.markDirty()
        await this.fetchUserInfo()
      } else {
        msgError(res.message || '头像上传失败')
      }
    } catch (e) {
      this.handleError(e)
    } finally {
      this.setData({ uploadLoading: false })
    }
  },

  // ===== 背景上传 =====
  chooseSlotBg(e) {
    // 上传进行中时忽略再次点击（按钮已移除，靠槽位区域本身触发）
    if (this.data.uploadingSlot) return
    const slot = Number(e.currentTarget.dataset.slot)
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success: (res) => {
        const file = res.tempFiles[0]
        if (file.size / 1024 / 1024 > 5) {
          msgError('图片大小不能超过5MB!')
          return
        }
        this.uploadSlotBg(file.tempFilePath, slot)
      },
    })
  },

  async uploadSlotBg(filePath, slot) {
    this.setData({ uploadingSlot: slot })
    try {
      const res = await userApi.uploadImage(filePath)
      if (res.code === 200 && res.data) {
        const addRes = await userApi.addBackground({ imagePath: res.data, sequence: slot })
        if (addRes.code === 200) {
          msgSuccess('背景上传成功')
          store.markDirty()
          await this.fetchUserInfo()
        } else {
          msgError(addRes.message || '添加失败')
        }
      } else {
        msgError(res.message || '上传失败')
      }
    } catch (e) {
      this.handleError(e)
    } finally {
      this.setData({ uploadingSlot: 0 })
    }
  },

  // ===== VIP =====
  buildVipBenefits(config) {
    const b = []
    if (config.maxAddressQuantity > 0) b.push(`最多 ${config.maxAddressQuantity} 个收货地址`)
    if (config.monthlyUpdateGoods > 0) b.push(`每月 ${config.monthlyUpdateGoods} 次商品上传`)
    if (config.monthlyUpdateAvatar > 0) b.push(`每月 ${config.monthlyUpdateAvatar} 次头像更新`)
    if (config.monthlyUpdateBackground > 0) b.push(`每月 ${config.monthlyUpdateBackground} 次背景更新`)
    if (config.maxCartQuantity > 0) b.push(`购物车上限 ${config.maxCartQuantity} 件`)
    if (config.maxGoodsQuantity > 0) b.push(`最多上架 ${config.maxGoodsQuantity} 件商品`)
    if (config.maxAiQuantity > 0) b.push(`每月 ${config.maxAiQuantity} 次货物上传AI`)
    b.push('VIP 专属标识')
    return b
  },

  async fetchVipLevel() {
    try {
      const res = await vipApi.getVipConfig()
      if (res.code === 200 && res.data) {
        const configs = Array.isArray(res.data) ? res.data : Object.values(res.data)
        const level = this.data.userLevel
        const current = configs.find((c) => c && c.level === level) || null
        if (current) {
          const name = current.levelName || (level > 0 ? `Lv.${level}` : '普通用户')
          this.setData({
            vipTier: {
              name,
              desc: level > 0 ? `${name} · 尊享权益` : '普通用户权益',
              benefits: this.buildVipBenefits(current),
            },
            limits: {
              monthlyUpdateBackground: current.monthlyUpdateBackground || DEFAULT_LIMITS.monthlyUpdateBackground,
              monthlyUpdateAvatar: current.monthlyUpdateAvatar || DEFAULT_LIMITS.monthlyUpdateAvatar,
              monthlyUpdateGoods: current.monthlyUpdateGoods || DEFAULT_LIMITS.monthlyUpdateGoods,
              maxCartQuantity: current.maxCartQuantity || DEFAULT_LIMITS.maxCartQuantity,
            },
          })
          this.buildQuotaItems()
        }
        if (configs.length > 0) {
          // level 0 是「普通用户」默认档，不是可购买的套餐，过滤掉
          const level = Number(this.data.userLevel) || 0
          const sorted = configs
            .filter((c) => c.level > 0)
            .sort((a, b) => a.level - b.level)
          const maxLevel = sorted.length ? sorted[sorted.length - 1].level : 0
          this.setData({
            vipPlans: sorted.map((c) => ({
              id: c.level,
              level: c.level,
              name: c.levelName || `Lv.${c.level}`,
              icon: '⭐',
              price: c.price,
              featured: c.level === 3,
              // 只有恰好买到的等级算「当前等级」；低等级只是被覆盖，不算已开通
              isExact: level === Number(c.level),
              isLower: level > Number(c.level),
              benefits: this.buildVipBenefits(c).slice(0, 4),
            })),
            // 满级：当前等级 ≥ 可购买的最高档，隐藏升级入口
            vipMaxed: level > 0 && level >= maxLevel,
          })
        }
      }
    } catch (e) {
      // ignore
    }
  },

  openVipDialog() {
    this.setData({ showVipDialog: true })
  },

  closeVipDialog() {
    this.setData({ showVipDialog: false })
  },

  async handleVipBuy(e) {
    const plan = this.data.vipPlans[e.currentTarget.dataset.index]
    if (!plan) return
    // 双保险：已开通的方案直接拦截并告知，不弹支付流程
    if (plan.isExact) {
      wx.showToast({ title: `您已开通${plan.name}，无需重复开通`, icon: 'none', duration: 2200 })
      return
    }
    if (plan.isLower) {
      wx.showToast({ title: '您的等级已覆盖该方案，无需重复开通', icon: 'none', duration: 2200 })
      return
    }
    this.setData({ vipBuyLoading: plan.id })
    try {
      const res = await vipApi.buyVip(plan.level, plan.price)
      if (res.code === 200) {
        msgSuccess(`已成功开通 ${plan.name}！`)
        this.setData({ showVipDialog: false })
        store.markDirty()
        this.loadUserData()
      } else {
        msgError(res.message || '购买失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    } finally {
      this.setData({ vipBuyLoading: 0 })
    }
  },

  async fetchCartCount() {
    try {
      const res = await userApi.getCartList({ page: 1, size: 1 })
      if (res.code === 200 && res.data) {
        this.setData({ cartUsed: res.data.total || 0 })
        this.buildQuotaItems()
      }
    } catch (e) {
      // ignore
    }
  },

  handleError(e) {
    const msg = getErrorMessage(e)
    if (msg.indexOf('token无效') >= 0 || msg.indexOf('已过期') >= 0) {
      msgError('登录已过期，请重新登录')
      store.logout()
      this.setData({ isLoggedIn: false })
    } else if (msg.indexOf('其他设备登录') >= 0) {
      msgError('账号已在其他设备登录，请重新登录')
      store.logout()
      this.setData({ isLoggedIn: false })
    } else {
      msgError(msg)
    }
  },

  noop() {},
})
