// pages/user/cart/cart.js — 购物车（迁移自 src/views/user/UserCart.vue）
//
// ===== 字段约定（对应后端 UserCart DTO，extends Goods）=====
//   item.id      → 商品 id，调 handleCart 时必须传 goodId: item.id
//   item.cartId  → 购物车项 id，调 buyFromCart 时传，且作为列表唯一键
// 历史 bug：多处误把 item.id 当成购物车 id（或反过来），导致
// 删除/改数量删错商品、选中态错乱。现在统一用 cartId 作列表唯一键。
const { userApi } = require('../../../utils/api/user')
const { store } = require('../../../utils/store')
const { msgSuccess, msgError, confirm } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { money } = require('../../../utils/format')
const { navigate } = require('../../../utils/nav')
const { FALLBACK_IMAGE } = require('../../../utils/demo-data')

Page({
  data: {
    balance: '0.00',
    cartList: [],
    loading: false,
    /** 正在提交的购物车项（key 为 cartId） */
    submitting: {},
    /** 已选中的购物车项 id（cartId，不是商品 id） */
    selectedIds: [],
    selectAll: false,
    selectedTotal: '0.00',
    /** 已选条目数（结算按钮显示的数量） */
    selectedCount: 0,
    /** 已选商品总件数（累加 quantity，用于「共 N 件」类提示） */
    selectedQty: 0,
    pageNum: 1,
    pageSize: 12,
    total: 0,
    pages: 0,
  },

  /** 数量防抖定时器（key 为 cartId） */
  pendingTimers: {},

  onLoad() {
    this.fetchCart()
    this.fetchUserInfo()
  },

  onShow() {
    this.fetchCart()
  },

  onUnload() {
    Object.keys(this.pendingTimers).forEach((k) => clearTimeout(this.pendingTimers[k]))
    this.pendingTimers = {}
  },

  async fetchUserInfo() {
    const info = await store.fetchUserInfoVO()
    if (info) this.setData({ balance: money(info.balance) })
  },

  async fetchCart() {
    this.setData({ loading: true })
    try {
      const res = await userApi.getCartList({ page: this.data.pageNum, size: this.data.pageSize })
      if (res.code === 200 && res.data) {
        const list = (res.data.list || []).map((item) => {
          const price = Number(item.goodsPrice)
          const qty = Number(item.quantity) || 0
          const stock = Number(item.goodsStock)
          return {
            ...item,
            // 关键：cartId / id 必须统一成 Number。
            // 后端 JSON 里可能是字符串，而事件回调里读到的是字符串、
            // 模板里 indexOf 比较的却是另一套类型 → 全选/单选会「点了没反应」。
            id: Number(item.id),
            cartId: Number(item.cartId),
            // 选中标记由 JS 预计算（模板内不做 indexOf 运算）
            selected: false,
            // 字段兜底，避免 undefined 展示
            quantity: qty,
            goodsStock: isNaN(stock) ? 0 : stock,
            goodsName: item.goodsName || '未命名商品',
            goodsPriceText: money(price),
            subtotalText: money((isNaN(price) ? 0 : price) * qty),
            imagePath: item.imagePath || FALLBACK_IMAGE,
            // 该商品是否已达到库存上限（UI 用于禁用 + 号）
            reachStockLimit: !isNaN(stock) && qty >= stock,
            // 是否已下架（后端 buyFromCart 会校验）
            offline: item.launch === false || item.launch === 0,
          }
        })
        this.setData({
          cartList: list,
          total: res.data.total,
          pages: res.data.pages,
          // 刷新后清空选中，避免残留已不存在的 id
          selectedIds: [],
          selectAll: false,
          selectedTotal: '0.00',
          selectedCount: 0,
        })
      } else {
        this.setData({ cartList: [], total: 0, pages: 0 })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
      this.setData({ cartList: [], total: 0, pages: 0 })
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 统一刷新「选中状态」相关数据。
   *
   * 为什么要在 JS 里算：小程序 WXML 的 {{}} 对 .indexOf() / >= 这类
   * 复合表达式支持不稳定（尤其在 wx:for 内做 class 拼接时不会可靠重算），
   * 所以把「是否选中」预计算成 item.selected 布尔值，模板只做简单绑定。
   *
   * 同时更新合计金额、总件数、全选态。
   */
  refreshSelection() {
    const selectedIds = this.data.selectedIds
    let sum = 0
    /** 已选商品总件数（累加 quantity，用于提示「共 N 件」） */
    let totalQty = 0
    /** 已选条目数（用于「结算(N)」与全选判断） */
    let itemCount = 0

    const cartList = (this.data.cartList || []).map((item) => {
      const selected = selectedIds.indexOf(Number(item.cartId)) >= 0
      if (selected) {
        sum += Number(item.goodsPrice) * Number(item.quantity)
        totalQty += Number(item.quantity)
        itemCount += 1
      }
      return { ...item, selected }
    })

    this.setData({
      cartList,
      selectedTotal: money(sum),
      // selectedCount 语义 = 条目数（结算按钮显示）
      selectedCount: itemCount,
      // selectedQty 语义 = 总件数（如需展示「共 N 件」可用）
      selectedQty: totalQty,
      selectAll: cartList.length > 0 && itemCount === cartList.length,
    })
  },

  /** 切换单项选中（用 cartId 作为唯一键） */
  toggleSelect(e) {
    const cartId = Number(e.currentTarget.dataset.id)
    let selectedIds = this.data.selectedIds.slice()
    const idx = selectedIds.indexOf(cartId)
    if (idx >= 0) selectedIds.splice(idx, 1)
    else selectedIds.push(cartId)

    this.setData({ selectedIds })
    this.refreshSelection()
  },

  toggleSelectAll() {
    const selectAll = !this.data.selectAll
    // 统一用 Number，避免与 toggleSelect 里的 Number 比较时类型不一致
    const ids = selectAll ? this.data.cartList.map((i) => Number(i.cartId)) : []
    this.setData({ selectedIds: ids })
    this.refreshSelection()
  },

  goMerchant(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    navigate(`/pages/user/merchant/merchant?userId=${id}`)
  },

  /** 点击商品图/名称进详情 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    navigate(`/pages/goods-detail/goods-detail?id=${id}`)
  },

  /** 按 cartId 在列表里定位（所有增减/删除统一走这里） */
  findItem(cartId) {
    return this.data.cartList.find((c) => c.cartId === Number(cartId))
  },

  onIncrease(e) {
    const cartId = Number(e.currentTarget.dataset.id)
    const list = this.data.cartList.slice()
    const idx = list.findIndex((c) => c.cartId === cartId)
    if (idx < 0) return
    const item = list[idx]
    if (item.offline) {
      msgError('商品已下架')
      return
    }
    if (item.quantity >= item.goodsStock) {
      msgError('库存不足')
      return
    }
    item.quantity += 1
    item.subtotalText = money(Number(item.goodsPrice) * item.quantity)
    item.reachStockLimit = item.quantity >= item.goodsStock
    this.setData({ cartList: list })
    this.refreshSelection()
    this.scheduleUpdate(cartId)
  },

  onDecrease(e) {
    const cartId = Number(e.currentTarget.dataset.id)
    const list = this.data.cartList.slice()
    const idx = list.findIndex((c) => c.cartId === cartId)
    if (idx < 0) return
    const item = list[idx]
    if (item.quantity <= 1) {
      // 减到 1 再减 → 询问是否删除
      this.confirmRemove(item)
      return
    }
    item.quantity -= 1
    item.subtotalText = money(Number(item.goodsPrice) * item.quantity)
    item.reachStockLimit = item.quantity >= item.goodsStock
    this.setData({ cartList: list })
    this.refreshSelection()
    this.scheduleUpdate(cartId)
  },

  /** 数量减到 0 时的删除确认；取消则恢复为 1 */
  async confirmRemove(item) {
    const ok = await confirm({
      title: '移除商品',
      message: `确认从购物车中删除「${item.goodsName}」吗？`,
      confirmText: '确认删除',
    })
    if (!ok) {
      const list = this.data.cartList.slice()
      const idx = list.findIndex((c) => c.cartId === item.cartId)
      if (idx >= 0) {
        list[idx].quantity = 1
        list[idx].subtotalText = money(Number(list[idx].goodsPrice))
        list[idx].reachStockLimit = list[idx].goodsStock <= 1
        this.setData({ cartList: list })
        this.refreshSelection()
      }
      return
    }
    await this.deleteCart(item)
  },

  /**
   * 删除购物车项。
   * 注意：后端 CartActionRequest 的 goodId 是「商品 id」，
   * 所以这里必须传 item.id（不是 cartId）。
   */
  async deleteCart(item) {
    try {
      const res = await userApi.handleCart({ goodId: item.id, num: 0 })
      if (res.code === 200) {
        msgSuccess('已删除')
        // 本地移除，避免整页刷新导致滚动位置丢失
        const list = this.data.cartList.filter((c) => c.cartId !== item.cartId)
        const selectedIds = this.data.selectedIds.filter((id) => id !== item.cartId)
        this.setData({
          cartList: list,
          selectedIds,
          total: Math.max(0, this.data.total - 1),
        })
        this.refreshSelection()
        // 删完后当前页可能空了，回到上一页
        if (!list.length && this.data.pageNum > 1) {
          this.setData({ pageNum: this.data.pageNum - 1 })
          this.fetchCart()
        }
      } else {
        msgError(res.message || '删除失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    }
  },

  /** 单项删除按钮 */
  async handleDelete(e) {
    const cartId = Number(e.currentTarget.dataset.id)
    const item = this.findItem(cartId)
    if (!item) return
    const ok = await confirm({ message: `确定要删除「${item.goodsName}」吗？` })
    if (!ok) return
    await this.deleteCart(item)
  },

  /**
   * 批量删除已选商品。
   * 后端没有批量删除接口，只能逐个调 handleCart({ goodId, num: 0 })，
   * 这里串行执行并统计结果，全部成功后再统一刷新列表。
   */
  async handleBatchDelete() {
    const { selectedIds, cartList } = this.data
    if (!selectedIds.length) {
      msgError('请先选择要删除的商品')
      return
    }

    const targets = cartList.filter((i) => selectedIds.indexOf(i.cartId) >= 0)
    const ok = await confirm({
      title: '批量删除',
      message: `确定要删除已选的 ${targets.length} 件商品吗？`,
      confirmText: '删除',
    })
    if (!ok) return

    wx.showLoading({ title: '删除中...', mask: true })
    const successIds = []
    let failed = 0
    for (const item of targets) {
      try {
        // goodId 传「商品 id」
        const res = await userApi.handleCart({ goodId: item.id, num: 0 })
        if (res.code === 200) successIds.push(item.cartId)
        else failed++
      } catch (e) {
        failed++
      }
    }
    wx.hideLoading()

    // 本地移除已删成功的项，避免整页刷新
    const list = this.data.cartList.filter((c) => successIds.indexOf(c.cartId) < 0)
    this.setData({
      cartList: list,
      selectedIds: [],
      total: Math.max(0, this.data.total - successIds.length),
    })
    this.refreshSelection()

    if (failed === 0) {
      msgSuccess(`已删除 ${successIds.length} 件商品`)
    } else {
      msgError(`删除完成：成功 ${successIds.length} 件，失败 ${failed} 件`)
      // 有失败项时以服务端为准重新拉取
      this.fetchCart()
    }

    // 删完后当前页可能空了，回到上一页
    if (!list.length && this.data.pageNum > 1) {
      this.setData({ pageNum: this.data.pageNum - 1 })
      this.fetchCart()
    }
  },

  /** 数量变更防抖提交（key 用 cartId，避免不同商品相互覆盖） */
  scheduleUpdate(cartId) {
    if (this.pendingTimers[cartId]) clearTimeout(this.pendingTimers[cartId])
    this.pendingTimers[cartId] = setTimeout(() => {
      delete this.pendingTimers[cartId]
      const item = this.findItem(cartId)
      if (!item) return
      this.submitQuantityChange(item)
    }, 300)
  },

  async submitQuantityChange(item) {
    const cartId = item.cartId
    const submitting = this.data.submitting
    if (submitting[cartId]) return
    submitting[cartId] = true
    this.setData({ submitting })
    try {
      // 同删除：goodId 传「商品 id」
      const res = await userApi.handleCart({ goodId: item.id, num: item.quantity })
      if (res.code !== 200) {
        msgError(res.message || '操作失败')
        this.fetchCart()
      }
    } catch (e) {
      msgError(getErrorMessage(e))
      this.fetchCart()
    } finally {
      const s = this.data.submitting
      s[cartId] = false
      this.setData({ submitting: s })
    }
  },

  async handleBuy() {
    const { selectedIds, cartList, selectedTotal, selectedCount, selectedQty } = this.data
    if (!selectedIds.length) return

    // 下架商品不允许结算（后端也会拦，这里先给明确提示）
    const offline = cartList.filter((i) => selectedIds.indexOf(i.cartId) >= 0 && i.offline)
    if (offline.length) {
      msgError(`「${offline[0].goodsName}」已下架，请先移除`)
      return
    }

    const ok = await confirm({
      title: '确认结算',
      // 明确区分「种类数」与「总件数」，避免用户误解数量
      message: `已选 ${selectedCount} 种商品，共 ${selectedQty} 件，合计 ¥${selectedTotal}。确认购买吗？`,
      confirmText: '确认购买',
    })
    if (!ok) return
    try {
      // buyFromCart 需要的是「购物车项 id」
      const cartIds = cartList
        .filter((i) => selectedIds.indexOf(i.cartId) >= 0)
        .map((i) => i.cartId)
      const res = await userApi.buyFromCart(cartIds)
      if (res.code === 200) {
        msgSuccess(res.data || '购买成功')
        this.fetchCart()
        store.markDirty()
      } else {
        msgError(res.message || '购买失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    }
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.setData({ pageNum: this.data.pageNum - 1 })
    this.fetchCart()
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.fetchCart()
  },

  goShopping() {
    navigate('/pages/user/goods/goods')
  },

  onPullDownRefresh() {
    this.fetchCart().then(() => wx.stopPullDownRefresh())
  },
})
