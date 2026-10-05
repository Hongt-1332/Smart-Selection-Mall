// pages/goods-detail/goods-detail.js — 商品详情页
//
// 入口：商品列表（首页 / 商品浏览页）点击商品卡
// 入参：?id=<商品ID>
// 数据兜底：接口不可用时，从本地缓存里按 id 找到该商品渲染
const { userApi } = require('../../utils/api/user')
const { store } = require('../../utils/store')
const { msgSuccess, msgError, msgInfo, confirm } = require('../../utils/message')
const { getErrorMessage } = require('../../utils/error')
const { money } = require('../../utils/format')
const { navigate } = require('../../utils/nav')
const { getCache } = require('../../utils/cache')
const { isDemoGoodsId, findDemoGoods, FALLBACK_IMAGE } = require('../../utils/demo-data')
const { ensureLogin } = require('../../utils/auth')

Page({
  data: {
    goodsId: null,
    goods: null,
    merchantAvatar: '',
    loading: true,
    usingCache: false,
    /** 是否为内置演示商品（id 为负数，不走接口） */
    isDemo: false,
    cartQuantity: 0,
    submitting: false,
    totalPrice: '0.00',
    // 购买弹窗
    buyVisible: false,
    buyQty: 1,
    buyLoading: false,
    // 地址
    addrVisible: false,
    addressList: [],
    addressLoading: false,
    selectedAddressId: null,
    selectedAddressText: '',
    addrFormVisible: false,
    addrSaving: false,
    addrForm: { country: '中国', province: '', city: '', county: '', detail: '' },
  },

  pendingTimer: null,

  onLoad(options) {
    const id = options && options.id
    this.setData({ goodsId: id })
    this.fetchDetail()
  },

  onUnload() {
    if (this.pendingTimer) clearTimeout(this.pendingTimer)
  },

  /** 字段兜底：保证详情页信息完整 */
  normalizeGoods(g) {
    const price = Number(g.goodsPrice)
    const stock = Number(g.goodsStock)
    return {
      ...g,
      id: g.id != null ? g.id : '',
      goodsName: g.goodsName || '未命名商品',
      describe: g.describe || '',
      goodsPrice: isNaN(price) ? 0 : price,
      goodsPriceText: money(isNaN(price) ? 0 : price),
      goodsStock: isNaN(stock) ? 0 : stock,
      // 接口没给图时用本地兜底图，避免详情页大图区空白
      imagePath: g.imagePath || FALLBACK_IMAGE,
      userName: g.userName || '未知商家',
      address: g.address || '',
    }
  },

  async fetchDetail() {
    const { goodsId } = this.data
    this.setData({ loading: true })

    // 0) 内置演示商品（id 为负数）：直接用本地数据渲染，不走接口
    if (isDemoGoodsId(goodsId)) {
      const demo = findDemoGoods(goodsId)
      if (demo) {
        this.setData({ isDemo: true })
        this.applyGoods(demo, false)
        return
      }
    }

    // 1) 先用缓存立即渲染，避免白屏
    const cached = this.findInCache(goodsId)
    if (cached) {
      this.applyGoods(cached, true)
    }

    // 2) 再请求接口拿最新数据
    // 说明：后端没有「商品详情」独立接口，只能用列表接口按 id 查找，
    // 因此这里取 100 条（PageRequest 的 size 上限）以提高命中率。
    try {
      const res = await userApi.getGoodsList({ page: 1, size: 100 })
      let found = null
      if (res && res.code === 200 && res.data && res.data.list) {
        found = res.data.list.find((g) => String(g.id) === String(goodsId))
      }
      if (found) {
        this.applyGoods(found, false)
        this.fetchMerchantAvatar(found.userName)
      } else if (!cached) {
        // 接口和缓存都没有
        this.setData({ goods: null, loading: false })
      } else {
        this.setData({ loading: false })
      }
    } catch (e) {
      if (!cached) {
        this.setData({ goods: null })
        msgError(getErrorMessage(e))
      }
      this.setData({ loading: false })
    }
  },

  /** 从本地缓存查找指定商品 */
  findInCache(id) {
    if (!id) return null
    const list = getCache('goods') || []
    return list.find((g) => String(g.id) === String(id)) || null
  },

  /** 应用商品数据到视图 */
  applyGoods(raw, fromCache) {
    const goods = this.normalizeGoods(raw)
    this.setData({
      goods,
      cartQuantity: raw.cartQuantity || 0,
      usingCache: !!fromCache,
      loading: false,
    })
    this.recalcTotal()
  },

  /** 拉取商家头像（复用 store 的用户列表） */
  async fetchMerchantAvatar(userName) {
    if (!userName) return
    try {
      const users = await store.fetchUserList()
      const u = users.find((x) => x.userName === userName)
      if (u) {
        this.setData({ merchantAvatar: u.avatarPath || u.avatarUrl || '' })
      }
    } catch (e) {
      // ignore
    }
  },

  // ===== 图片预览 =====
  previewImage() {
    const src = this.data.goods && this.data.goods.imagePath
    if (src) wx.previewImage({ urls: [src], current: src })
  },

  // ===== 购物车 =====
  /** 加入购物车前需登录（演示商品除外） */
  async onIncrease() {
    const g = this.data.goods
    if (!g) return
    if (!this.data.isDemo) {
      const ok = await ensureLogin()
      if (!ok) return
    }
    const next = this.data.cartQuantity + 1
    if (next > g.goodsStock) {
      msgError('库存不足')
      return
    }
    this.setData({ cartQuantity: next })
    this.scheduleCartSubmit(next)
  },

  onDecrease() {
    const cur = this.data.cartQuantity
    if (cur <= 0) return
    const next = cur - 1
    this.setData({ cartQuantity: next })
    this.scheduleCartSubmit(next)
  },

  /** 300ms 防抖，避免连点频繁请求（与原项目一致） */
  scheduleCartSubmit(qty) {
    if (this.pendingTimer) clearTimeout(this.pendingTimer)
    this.pendingTimer = setTimeout(() => {
      this.pendingTimer = null
      this.submitCart(qty)
    }, 300)
  },

  async submitCart(qty) {
    if (this.data.submitting) return
    // 演示商品：本地模拟，不请求接口
    if (this.data.isDemo) {
      msgSuccess(qty === 0 ? '已从购物车移除（演示）' : '已加入购物车（演示）')
      return
    }
    this.setData({ submitting: true })
    try {
      const res = await userApi.handleCart({ goodId: this.data.goods.id, num: qty })
      if (res.code === 200) {
        msgSuccess(qty === 0 ? '已从购物车移除' : '购物车已更新')
      } else {
        msgError(res.message || '操作失败')
        this.setData({ cartQuantity: 0 })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ submitting: false })
    }
  },

  // ===== 购买 =====
  async openBuy() {
    // 演示商品可直接体验弹窗；真实商品需先登录
    if (!this.data.isDemo) {
      const ok = await ensureLogin()
      if (!ok) return
    }
    this.setData({ buyVisible: true, buyQty: 1 })
    this.recalcTotal()
    this.fetchAddressList()
  },

  closeBuy() {
    this.setData({ buyVisible: false })
  },

  onBuyQtyMinus() {
    this.setData({ buyQty: Math.max(1, this.data.buyQty - 1) })
    this.recalcTotal()
  },

  onBuyQtyPlus() {
    const g = this.data.goods
    if (!g) return
    this.setData({ buyQty: Math.min(g.goodsStock, this.data.buyQty + 1) })
    this.recalcTotal()
  },

  recalcTotal() {
    const g = this.data.goods
    const total = g ? (g.goodsPrice || 0) * (this.data.buyQty || 1) : 0
    this.setData({ totalPrice: money(total) })
  },

  async fetchAddressList() {
    this.setData({ addressLoading: true })
    try {
      const res = await userApi.getAddressList({ page: 1, size: 50 })
      if (res.code === 200 && res.data) {
        const list = res.data.list || []
        const def = list.find((a) => a.isDefault)
        this.setData({
          addressList: list,
          selectedAddressId: def ? def.id : this.data.selectedAddressId,
          selectedAddressText: def ? this.addrText(def) : this.data.selectedAddressText,
        })
      }
    } catch (e) {
      // ignore
    } finally {
      this.setData({ addressLoading: false })
    }
  },

  addrText(addr) {
    return `${addr.province || ''} ${addr.city || ''} ${addr.county || ''} ${addr.detail || ''}`
  },

  openAddrSheet() {
    this.setData({ addrVisible: true, addrFormVisible: false })
    this.fetchAddressList()
  },

  closeAddrSheet() {
    this.setData({ addrVisible: false, addrFormVisible: false })
  },

  onSelectAddress(e) {
    const addr = this.data.addressList[e.currentTarget.dataset.index]
    if (!addr) return
    this.setData({
      selectedAddressId: addr.id,
      selectedAddressText: this.addrText(addr),
      addrVisible: false,
    })
  },

  // ===== 地址弹层内嵌新增表单 =====
  openAddrForm() {
    this.setData({
      addrFormVisible: true,
      addrForm: { country: '中国', province: '', city: '', county: '', detail: '' },
    })
  },

  cancelAddrForm() {
    this.setData({ addrFormVisible: false })
  },

  onAddrFormInput(e) {
    const field = e.currentTarget.dataset.field
    if (!field) return
    this.setData({ [`addrForm.${field}`]: e.detail.value })
  },

  async saveNewAddress() {
    const f = this.data.addrForm
    if (!f.province || !f.city || !f.detail) {
      msgError('请填写省份、城市和详细地址')
      return
    }
    this.setData({ addrSaving: true })
    try {
      const res = await userApi.addAddress({
        country: f.country || '中国',
        province: f.province,
        city: f.city,
        county: f.county,
        detail: f.detail,
        isDefault: this.data.addressList.length === 0,
      })
      if (res.code === 200) {
        msgSuccess('地址添加成功')
        this.setData({ addrFormVisible: false })
        await this.fetchAddressList()
        const newId = res.data && (res.data.id || res.data)
        if (newId) {
          this.setData({
            selectedAddressId: newId,
            selectedAddressText: `${f.province} ${f.city} ${f.county} ${f.detail}`,
          })
        }
      } else {
        msgError(res.message || '添加失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ addrSaving: false })
    }
  },

  async handleBuyNow() {
    const { goods, selectedAddressId, buyQty } = this.data
    if (!goods) return
    if (!selectedAddressId) {
      msgError('请选择收货地址')
      return
    }
    const ok = await confirm({ title: '提示', message: '确定购买该商品？' })
    if (!ok) return

    // 演示商品：本地模拟购买结果，不请求接口
    if (this.data.isDemo) {
      msgSuccess('演示商品购买成功')
      this.setData({
        buyVisible: false,
        'goods.goodsStock': Math.max(0, goods.goodsStock - buyQty),
      })
      return
    }

    this.setData({ buyLoading: true })
    try {
      const res = await userApi.buyGoods({
        goodsId: goods.id,
        quantity: buyQty,
        shippingAddressId: selectedAddressId,
      })
      if (res.code === 200) {
        msgSuccess('购买成功')
        this.setData({ buyVisible: false })
        // 刷新库存
        const stock = Math.max(0, goods.goodsStock - buyQty)
        this.setData({ 'goods.goodsStock': stock })
      } else {
        msgError(res.message || '购买失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ buyLoading: false })
    }
  },

  goMerchant(e) {
    // 演示商品没有真实商家，避免跳转到空页面
    if (this.data.isDemo) {
      msgInfo('演示数据没有关联的真实店铺')
      return
    }
    const id = e.currentTarget.dataset.id
    if (!id) return
    navigate(`/pages/user/merchant/merchant?userId=${id}`)
  },

  noop() {},
})
