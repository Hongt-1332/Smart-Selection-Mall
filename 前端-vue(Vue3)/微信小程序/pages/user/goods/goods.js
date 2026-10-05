// pages/user/goods/goods.js — 商品浏览（迁移自 src/views/user/UserGoods.vue）
const { userApi } = require('../../../utils/api/user')
const { store } = require('../../../utils/store')
const { msgError, msgWarning } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { buildSearchParams } = require('../../../utils/search')
const { showSearchResult } = require('../../../utils/validation')
const { money } = require('../../../utils/format')
const { navigate } = require('../../../utils/nav')
const { setCache, getCache } = require('../../../utils/cache')
const { FALLBACK_IMAGE } = require('../../../utils/demo-data')
const { ensureLogin } = require('../../../utils/auth')

Page({
  data: {
    balance: '0.00',
    goodsList: [],
    merchantMap: {},
    loading: false,
    pageNum: 1,
    pageSize: 12,
    total: 0,
    pages: 0,
    search: {
      goodsName: '',
      userName: '',
      priceMin: '',
      priceMax: '',
    },
    /** 已生效的搜索条件标签 */
    activeChips: [],
    isLoggedIn: false,
    /** 当前列表是否来自本地缓存兜底 */
    usingCache: false,
    /** 购物车角标（显示在「购物车」入口上） */
    cartBadge: 0,
  },

  onLoad() {
    this.fetchGoods()
    this.fetchCartCount()
    this.fetchUserInfo()
  },

  onShow() {
    this.setData({ isLoggedIn: store.isLoggedIn })
    if (store.dirty) this.fetchUserInfo()
    this.syncTabBar()
    // 每次显示都重新拉购物车数量，保证加购后角标即时更新
    // （之前只在 onLoad 拉一次，切换 tab 回来角标是旧的 / 空的）
    this.fetchCartCount()

    // 从搜索页返回时，读取其写回的筛选条件并刷新列表
    const app = getApp()
    const last = app && app.globalData && app.globalData.lastSearch
    if (last !== undefined) {
      app.globalData.lastSearch = undefined
      if (last) {
        this.setData({ search: last, pageNum: 1 })
        this.buildActiveChips()
        this.fetchGoods()
        return
      }
    }
    this.buildActiveChips()
  },

  /**
   * 同步自定义 tabBar 选中态（用户中心 = index 2）。
   * 这里只同步 selected，不传 cartBadge ——
   * tabBar 上的「用户中心」图标不显示购物车角标。
   */
  syncTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
  },

  async fetchUserInfo() {
    const info = await store.fetchUserInfoVO()
    if (info) {
      this.setData({
        balance: money(info.balance),
        isLoggedIn: store.isLoggedIn,
      })
    }
  },

  /**
   * 补全商品字段，保证列表卡片每条都能显示完整信息。
   * 后端可能缺字段（如未返回价格/库存/商家），这里统一兜底，
   * 避免出现「¥undefined」「库存 undefined」这类异常展示。
   */
  normalizeGoods(g, index) {
    const price = Number(g.goodsPrice)
    const stock = Number(g.goodsStock)
    return {
      ...g,
      id: g.id != null ? g.id : `local_${index}`,
      goodsName: g.goodsName || '未命名商品',
      describe: g.describe || '',
      goodsPrice: isNaN(price) ? 0 : price,
      goodsPriceText: money(isNaN(price) ? 0 : price),
      goodsStock: isNaN(stock) ? 0 : stock,
      // 接口没给图时用本地兜底图，避免整屏「暂无图片」
      imagePath: g.imagePath || FALLBACK_IMAGE,
      userName: g.userName || '未知商家',
      address: g.address || '',
      cartQuantity: g.cartQuantity || 0,
      // 标记为缓存数据，UI 可据此提示
      fromCache: !!g.fromCache,
    }
  },

  async fetchGoods() {
    this.setData({ loading: true })
    const { search, pageNum, pageSize } = this.data
    const isSearching = !!(search.goodsName || search.userName || search.priceMin || search.priceMax)
    try {
      const params = buildSearchParams(search, pageNum, pageSize)
      if (search.priceMin) params.priceMin = Number(search.priceMin)
      if (search.priceMax) params.priceMax = Number(search.priceMax)

      const res = await userApi.getGoodsList(params)
      const list = (res && res.data && res.data.list) || []

      if (res && res.code === 200 && list.length) {
        const normalized = list.map((g, i) => this.normalizeGoods(g, i))
        this.setData({
          goodsList: normalized,
          total: res.data.total,
          pages: res.data.pages,
          usingCache: false,
        })
        this.fetchMerchantInfo(normalized)
        // 仅首页（非搜索态）写入缓存，作为下次兜底数据
        if (!isSearching && pageNum === 1) {
          setCache('goods', normalized)
        }
        if (isSearching) {
          showSearchResult(res.data.total, search.goodsName || undefined)
        }
      } else {
        // 接口没数据 → 用缓存兜底，保证界面信息完整
        this.applyCacheFallback(isSearching)
      }
    } catch (e) {
      // 接口异常 → 同样用缓存兜底
      const used = this.applyCacheFallback(isSearching)
      if (!used) msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 用本地缓存渲染列表
   * @returns {boolean} 是否成功应用了缓存
   */
  applyCacheFallback(isSearching) {
    const cached = getCache('goods')
    if (!cached || !cached.length) return false
    const normalized = cached.map((g, i) => this.normalizeGoods({ ...g, fromCache: true }, i))
    this.setData({
      goodsList: normalized,
      total: normalized.length,
      pages: 1,
      pageNum: 1,
      usingCache: true,
    })
    this.fetchMerchantInfo(normalized)
    // 搜索态下缓存不适用，给出提示
    if (isSearching) msgWarning('网络异常，已展示缓存商品')
    return true
  },

  async fetchMerchantInfo(list) {
    const names = [...new Set(list.map((g) => g.userName))]
    const merchantMap = this.data.merchantMap
    const newNames = names.filter((n) => !merchantMap[n])
    if (!newNames.length) {
      // 商户信息已缓存，直接复用，避免重复请求
      this.mergeMerchantAvatar(merchantMap)
      return
    }
    try {
      const users = await store.fetchUserList()
      const next = { ...merchantMap }
      users.forEach((u) => {
        if (newNames.indexOf(u.userName) >= 0) next[u.userName] = u
      })
      this.setData({ merchantMap: next })
      // 头像直接平铺到商品项上：
      // WXML 不支持 merchantMap[item.userName].avatarPath 这种深层取值，
      // 一旦 userName 未命中就会报错，因此在 JS 侧完成映射。
      this.mergeMerchantAvatar(next)
    } catch (e) {
      // ignore
    }
  },

  /** 把商户头像合并进商品列表（按 userName 匹配） */
  mergeMerchantAvatar(map) {
    const goodsList = this.data.goodsList.map((g) => {
      const u = map[g.userName]
      return {
        ...g,
        merchantAvatar: (u && (u.avatarPath || u.avatarUrl)) || g.merchantAvatar || '',
      }
    })
    this.setData({ goodsList })
  },

  /**
   * 拉取购物车件数，用于「购物车」入口上的红点。
   * 说明：角标只出现在本页的「购物车」入口上，
   * 不在 tabBar 的「用户中心」图标上显示。
   */
  async fetchCartCount() {
    try {
      const res = await userApi.getCartCount()
      if (res.code === 200) {
        this.setData({ cartBadge: Number(res.data) || 0 })
      }
    } catch (e) {
      // ignore
    }
  },

  // ===== 搜索 =====

  /** 搜索框输入（列表页只做名称快搜，详细条件在搜索页） */
  onQuickSearchInput(e) {
    this.setData({ 'search.goodsName': e.detail.value })
  },

  /** 执行快搜：直接在当前列表按名称筛选 */
  onQuickSearch() {
    this.setData({ pageNum: 1 })
    this.buildActiveChips()
    this.fetchGoods()
  },

  /** 进入搜索页做高级筛选（带上当前关键词，便于回填） */
  goSearch() {
    const kw = (this.data.search.goodsName || '').trim()
    const url = kw
      ? `/pages/search/search?keyword=${encodeURIComponent(kw)}`
      : '/pages/search/search'
    navigate(url)
  },

  /** 构建已生效的条件标签 */
  buildActiveChips() {
    const { search } = this.data
    const chips = []
    if (search.goodsName) chips.push({ key: 'goodsName', text: search.goodsName })
    if (search.userName) chips.push({ key: 'userName', text: `商家：${search.userName}` })
    if (search.priceMin) chips.push({ key: 'priceMin', text: `最低 ¥${search.priceMin}` })
    if (search.priceMax) chips.push({ key: 'priceMax', text: `最高 ¥${search.priceMax}` })
    this.setData({ activeChips: chips })
  },

  /** 移除单个搜索条件 */
  removeChip(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return
    this.setData({ [`search.${key}`]: '', pageNum: 1 })
    this.buildActiveChips()
    this.fetchGoods()
  },

  handleReset() {
    this.setData({
      search: { goodsName: '', userName: '', priceMin: '', priceMax: '' },
      activeChips: [],
      pageNum: 1,
    })
    this.fetchGoods()
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.setData({ pageNum: this.data.pageNum - 1 })
    this.fetchGoods()
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.fetchGoods()
  },

  /**
   * 个人中心各子页面入口。
   * 需要登录的页面先走一键登录，避免用户进去后看到「请先登录」。
   */
  async goPage(e) {
    const path = e.currentTarget.dataset.path
    if (!path) return
    if (!store.isLoggedIn) {
      const ok = await ensureLogin()
      if (!ok) return
    }
    navigate(path)
  },

  /** 点击商品卡进入详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    navigate(`/pages/goods-detail/goods-detail?id=${id}`)
  },

  goMerchant(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    navigate(`/pages/user/merchant/merchant?userId=${id}`)
  },

  // 说明：本页为纯浏览列表，不再提供加购 / 增减数量 / 购买操作。
  // 所有交易相关操作请进入商品详情页完成。

  goCart() {
    navigate('/pages/user/cart/cart')
  },

  onPullDownRefresh() {
    this.fetchGoods().then(() => wx.stopPullDownRefresh())
  },
})
