// pages/index/index.js — 首页（迁移自 src/views/HomeView.vue）
const { vipApi } = require('../../utils/api/vip')
const { userApi } = require('../../utils/api/user')
const { merchantApi } = require('../../utils/api/merchant')
const { store } = require('../../utils/store')
const { navigate } = require('../../utils/nav')
const { money } = require('../../utils/format')
const { setCache, getCache } = require('../../utils/cache')
const { DEFAULT_BANNERS, buildDemoGoods, FALLBACK_IMAGE } = require('../../utils/demo-data')

/**
 * 补全商品字段，保证卡片信息完整。
 * 后端缺字段时统一兜底，避免「¥undefined / 库存 undefined」这类异常展示。
 */
function normalizeGoods(g, index) {
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
  }
}

Page({
  data: {
    bannerImgs: [],
    bannerIdx: 0,
    // 商城购物入口已移除（顶部 Banner 的「去购物」按钮承担该功能）
    entries: [
      { icon: '✦', title: '智能推荐', path: '/pages/ai-recommend/ai-recommend', badgeCount: 0 },
      { icon: '📋', title: '未完成订单', path: '/pages/merchant/trade/trade', badgeCount: 0 },
      { icon: '🛍️', title: '购物车', path: '/pages/user/cart/cart', badgeCount: 0 },
      { icon: '🔍', title: '搜索商品', path: '/pages/search/search', badgeCount: 0 },
    ],
    goodsList: [],
    /** 推荐商品轮播当前索引 */
    recIdx: 0,
    /** 商品列表是否来自本地缓存兜底 */
    usingCache: false,
  },

  onLoad() {
    this.fetchAll()
  },

  onShow() {
    this.refreshBadges()
    this.syncTabBar()
  },

  /**
   * 同步自定义 tabBar 状态（首页 = index 0）。
   * 购物车角标只出现在「用户中心」页面内的购物车入口上，
   * 不在 tabBar 上显示，因此这里只同步 selected。
   */
  syncTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
  },

  async fetchAll() {
    const tasks = [
      this.fetchBanners(),
      this.fetchVipGoods(),
      this.refreshBadges(),
    ]
    // 与原项目一致：整体最多等待 2 秒，超时使用兜底数据
    await Promise.race([Promise.all(tasks), new Promise((r) => setTimeout(r, 2000))])
    if (!this.data.bannerImgs.length) {
      this.setData({ bannerImgs: DEFAULT_BANNERS })
    }
    if (!this.data.goodsList.length) {
      // 优先级：本地缓存 > 内置演示数据
      const cached = getCache('goods')
      if (cached && cached.length) {
        this.setData({
          goodsList: cached.map((g, i) => normalizeGoods(g, i)),
          usingCache: true,
        })
      } else {
        this.setData({ goodsList: buildDemoGoods().map((g, i) => normalizeGoods(g, i)) })
      }
    }
  },

  async fetchBanners() {
    try {
      const res = await vipApi.getHomeImages()
      if (res.code === 200 && Array.isArray(res.data)) {
        // 后端 ImageResponse @AllArgsConstructor(imageUrl, imagePath)：
        // /vip/homeImage 把可渲染的 data:image/png;base64 URI 放在 imageUrl，
        // imagePath 只是文件名（如 "1.webp"）。
        // 之前取 img.data（不存在）或误用 imagePath（文件名）→ banner 空白。
        const imgs = res.data
          .map((img) => img.imageUrl || img.imagePath || img.data)
          .filter(Boolean)
        if (imgs.length) this.setData({ bannerImgs: imgs })
      }
    } catch (e) {
      // ignore
    }
  },

  async fetchVipGoods() {
    try {
      const res = await vipApi.getVipGoods()
      if (res.code === 200 && res.data && res.data.length > 0) {
        const list = res.data.map((g, i) => normalizeGoods(g, i))
        this.setData({ goodsList: list, usingCache: false })
        // 与原项目一致：按 userName 匹配商户信息，补上头像
        this.fetchMerchantInfo(list)
        // 首页展示的商品写入缓存，供接口异常时兜底
        setCache('indexGoods', list)
      } else {
        this.applyIndexCache()
      }
    } catch (e) {
      this.applyIndexCache()
    }
  },

  /** 首页商品缓存兜底 */
  applyIndexCache() {
    if (this.data.goodsList.length) return
    const cached = getCache('indexGoods') || getCache('goods')
    if (cached && cached.length) {
      const list = cached.map((g, i) => normalizeGoods(g, i))
      this.setData({ goodsList: list, usingCache: true })
      this.fetchMerchantInfo(list)
    }
  },

  /**
   * 拉取商户信息并按 userName 映射，补全商品卡上的商户头像。
   * 对应原项目 HomeView.vue 的 fetchMerchantInfo + merchantMap。
   */
  async fetchMerchantInfo(list) {
    if (!list || !list.length) return
    try {
      const names = [...new Set(list.map((g) => g.userName).filter(Boolean))]
      if (!names.length) return
      const userList = await store.fetchUserList()
      const map = this.merchantMap || {}
      userList.forEach((u) => {
        if (names.indexOf(u.userName) >= 0) map[u.userName] = u
      })
      this.merchantMap = map
      const goodsList = this.data.goodsList.map((g) => ({
        ...g,
        merchantAvatar: (map[g.userName] && (map[g.userName].avatarPath || map[g.userName].avatarUrl)) || '',
      }))
      this.setData({ goodsList })
    } catch (e) {
      // ignore
    }
  },

  async refreshBadges() {
    let cartCount = 0
    let unfinishedCount = 0
    try {
      const res = await userApi.getCartCount()
      if (res.code === 200) cartCount = res.data || 0
    } catch (e) {
      cartCount = 0
    }
    try {
      // 后端没有 /merchant/trade/unfinished，用订单列表自己统计未完成数，
      // 未登录（403）时接口会失败，静默降级为 0。
      const res = await merchantApi.getTradeList({ page: 1, size: 100 })
      if (res.code === 200 && res.data) {
        unfinishedCount = (res.data.list || []).filter((row) => !row.finishTime && !row.cancelTime).length
      }
    } catch (e) {
      unfinishedCount = 0
    }
    // 按标题匹配角标，避免依赖数组下标（入口顺序调整后仍然正确）
    const entries = this.data.entries.map((item) => {
      if (item.title === '未完成订单') return { ...item, badgeCount: unfinishedCount }
      if (item.title === '购物车') return { ...item, badgeCount: cartCount }
      return { ...item, badgeCount: 0 }
    })
    this.setData({ entries })

    // 注：角标只在本页的入口卡片上显示，tabBar 上不显示购物车角标
    this.syncTabBar()
  },

  // ===== Banner =====
  onBannerSwiperChange(e) {
    this.setData({ bannerIdx: e.detail.current })
  },

  // ===== 推荐商品轮播 =====
  onRecSwiperChange(e) {
    this.setData({ recIdx: e.detail.current })
  },

  /** 跳转搜索页 */
  goSearch() {
    navigate('/pages/search/search')
  },

  // ===== 导航 =====
  goEntry(e) {
    const item = this.data.entries[e.currentTarget.dataset.index]
    if (!item) return
    navigate(item.path)
  },

  /** 点击商品卡进入详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    navigate(`/pages/goods-detail/goods-detail?id=${id}`)
  },

  goUser() {
    navigate('/pages/user/goods/goods')
  },

  goMerchant(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    navigate(`/pages/user/merchant/merchant?userId=${id}`)
  },

  // 说明：本页为纯浏览列表，不再提供加购 / 增减数量操作。
  // 数量调整请进入商品详情页或购物车页面完成。

  // 说明：购买、加购、地址选择等操作已全部收敛到商品详情页，
  // 首页只承担「浏览 + 跳转」，保持轻量。

  onPullDownRefresh() {
    this.fetchAll().then(() => wx.stopPullDownRefresh())
  },
})
