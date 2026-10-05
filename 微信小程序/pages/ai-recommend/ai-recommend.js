// pages/ai-recommend/ai-recommend.js — 智能推荐（迁移自 src/views/AIRecommend.vue）
const { aiApi } = require('../../utils/api/ai')
const { vipApi } = require('../../utils/api/vip')
const { msgSuccess, msgError } = require('../../utils/message')
const { getErrorMessage } = require('../../utils/error')
const { currentTimeStr, money } = require('../../utils/format')
const { navigate } = require('../../utils/nav')
const { getCache } = require('../../utils/cache')
const { buildDemoGoods, FALLBACK_IMAGE } = require('../../utils/demo-data')
const { ensureLogin } = require('../../utils/auth')

const WELCOME = '你好！我是智能管家 🎯\n告诉我你喜欢什么，我来帮你找到最合适的~'

Page({
  data: {
    input: '',
    loading: false,
    messages: [],
    isThinking: false,
    hasQueried: false,
    quickTags: ['蓝牙耳机推荐', '200元内背包', '送女生礼物', '机械键盘'],
    recItems: [],
    /** 推荐商品轮播当前索引 */
    recIdx: 0,
    /**
     * 未提问时展示的在售商品示例（最多 6 件）。
     * 数据优先级：接口实时数据 > 本地缓存 > 内置演示数据。
     * 注意：不使用任何外链图片，缺图由页面「暂无图片」占位兜底。
     */
    previewGoods: [],
    /** 预览轮播当前索引 */
    previewIdx: 0,
    currentTime: '',
    scrollIntoView: '',
  },

  timeTimer: null,
  sessionTimer: null,
  SESSION_TIMEOUT: 10 * 60 * 1000,

  onLoad() {
    this.resetSession()
    this.updateTime()
    this.fetchPreviewGoods()
    this.timeTimer = setInterval(() => this.updateTime(), 30000)
    // 会话超时自动重置
    this.sessionTimer = setInterval(() => {
      if (Date.now() - this.lastActive > this.SESSION_TIMEOUT) {
        this.resetSession()
        this.lastActive = Date.now()
      }
    }, 60000)
    this.lastActive = Date.now()
  },

  /**
   * 拉取在售商品，作为「为你精选」未提问时的引导展示。
   * 数据优先级：接口实时数据 > 本地缓存 > 内置演示数据，
   * 保证「为你精选」面板始终有示例可看，不会出现空白。
   */
  async fetchPreviewGoods() {
    let list = []
    try {
      const res = await vipApi.getVipGoods()
      if (res.code === 200 && res.data && res.data.length) {
        list = res.data
      }
    } catch (e) {
      // 接口异常，走下方兜底
    }
    if (!list.length) {
      const cached = getCache('indexGoods') || getCache('goods')
      if (cached && cached.length) list = cached
    }
    if (!list.length) list = buildDemoGoods()
    this.setData({ previewGoods: this.normalizePreview(list) })
  },

  /**
   * 补全预览商品字段，保证与首页「推荐商品」卡片信息一致。
   * 字段齐全才不会有「¥undefined / 库存 undefined」这类异常展示。
   */
  normalizePreview(list) {
    return list.slice(0, 6).map((g, i) => {
      const price = Number(g.goodsPrice)
      const stock = Number(g.goodsStock)
      return {
        ...g,
        id: g.id != null ? g.id : `preview_${i}`,
        goodsName: g.goodsName || '未命名商品',
        describe: g.describe || '',
        goodsPrice: isNaN(price) ? 0 : price,
        goodsPriceText: money(isNaN(price) ? 0 : price),
        goodsStock: isNaN(stock) ? 0 : stock,
        // 接口没给图时用本地兜底图，避免整屏「暂无图片」
        imagePath: g.imagePath || FALLBACK_IMAGE,
        userName: g.userName || '',
        merchantAvatar: g.merchantAvatar || '',
        cartQuantity: g.cartQuantity || 0,
      }
    })
  },

  onShow() {
    this.syncTabBar()
  },

  /**
   * 同步自定义 tabBar 状态（智能推荐 = index 1）。
   * tabBar 上不显示购物车角标，因此只同步 selected。
   */
  syncTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
  },

  onUnload() {
    if (this.timeTimer) clearInterval(this.timeTimer)
    if (this.sessionTimer) clearInterval(this.sessionTimer)
    this.abortStream()
  },

  updateTime() {
    this.setData({ currentTime: currentTimeStr() })
  },

  resetSession() {
    this.abortStream()
    this.setData({
      messages: [{ role: 'ai', content: WELCOME, time: currentTimeStr() }],
      recItems: [],
      recIdx: 0,
      previewIdx: 0,
      hasQueried: false,
      isThinking: false,
      loading: false,
      input: '',
    })
    this.scrollBottom()
  },

  /** 中断进行中的流式请求并清理定时器 */
  abortStream() {
    if (this.streamTask) {
      this.streamTask.abort()
      this.streamTask = null
    }
    if (this.flushTimer) {
      clearTimeout(this.flushTimer)
      this.flushTimer = null
    }
    this.pendingText = ''
  },

  /**
   * 开启新对话。
   *
   * 性能要点：这里「先刷新界面，再异步通知服务端」。
   * 之前的写法是 await chatClear() 之后才 resetSession()，
   * 而 chatClear 超时长达 30s，后端未启动 / 响应慢时
   * 用户点击后会明显卡住 —— 但清空会话本身是本地 UI 行为，
   * 服务端清理失败也不影响用户开始新对话，不应该阻塞交互。
   */
  handleNewChat() {
    this.resetSession() // 内部已 abortStream()
    msgSuccess('已开启新对话')
    // 服务端清理放到后台，不阻塞界面
    aiApi.chatClear().catch(() => {
      // 服务端清理失败静默忽略，本地会话已经重置
    })
  },

  onInput(e) {
    this.setData({ input: e.detail.value })
  },

  quickSend(e) {
    const tag = e.currentTarget.dataset.tag
    this.setData({ input: tag })
    this.send()
  },

  scrollBottom() {
    this.setData({ scrollIntoView: 'msg-bottom' })
  },

  /**
   * 发送消息（流式版）
   *
   * 与旧版的区别：不再 await 整个回复，而是先插入一条空的 AI 消息，
   * 后端每吐一个 token 就往这条消息里追加，实现打字机效果。
   * 首字响应从原来的 ~7s 缩短到 ~300ms，体验提升明显。
   */
  send() {
    const text = (this.data.input || '').trim()
    if (!text || this.data.loading) return
    this.lastActive = Date.now()

    const messages = this.data.messages.slice()
    messages.push({ role: 'user', content: text, time: currentTimeStr() })
    // 预留一条 AI 消息占位，后续逐字填充
    const aiIndex = messages.length
    messages.push({ role: 'ai', content: '', time: currentTimeStr() })

    this.setData({
      messages,
      input: '',
      loading: true,
      isThinking: true,
      hasQueried: true,
      recItems: [],
    })
    this.scrollBottom()

    // 中断上一次未完成的流
    if (this.streamTask) {
      this.streamTask.abort()
      this.streamTask = null
    }

    // 节流：流式分片很密集，累积到一定量再 setData，避免频繁重渲染卡顿
    this.pendingText = ''
    this.flushTimer = null

    const flush = () => {
      this.flushTimer = null
      if (!this.pendingText) return
      const chunk = this.pendingText
      this.pendingText = ''
      const list = this.data.messages.slice()
      const target = list[aiIndex]
      if (!target) return
      target.content += chunk
      this.setData({ messages: list })
      this.scrollBottom()
    }

    const scheduleFlush = () => {
      if (this.flushTimer) return
      // 每 80ms 刷新一次，兼顾流畅度与性能
      this.flushTimer = setTimeout(flush, 80)
    }

    this.streamTask = aiApi.chatStream(text, {
      // 商品列表在开头一次性推过来
      onProducts: (products) => {
        this.setData({
          recItems: (products || []).map((p) => {
            const price = Number(p.goodsPrice)
            const stock = Number(p.goodsStock)
            return {
              ...p,
              id: p.id != null ? p.id : p.goodsId,
              goodsId: p.goodsId != null ? p.goodsId : p.id,
              goodsName: p.goodsName || '未命名商品',
              describe: p.describe || '',
              goodsPriceText: money(price),
              goodsStock: isNaN(stock) ? 0 : stock,
              imagePath: p.imagePath || FALLBACK_IMAGE,
              cartQuantity: p.cartQuantity || 0,
            }
          }),
        })
      },

      // 每个 token 到达
      onMessage: (chunk) => {
        // 首个 token 到达时收起「正在思考中」
        if (this.data.isThinking) this.setData({ isThinking: false })
        this.pendingText += chunk
        scheduleFlush()
      },

      onDone: () => {
        flush()
        this.setData({ loading: false, isThinking: false })
        // 若后端没推商品，且回复为空，给个兜底文案
        const list = this.data.messages.slice()
        if (list[aiIndex] && !list[aiIndex].content) {
          list[aiIndex].content = '（无回复）'
          this.setData({ messages: list })
        }
        this.scrollBottom()
      },

      onError: (err) => {
        flush()
        const list = this.data.messages.slice()
        if (list[aiIndex] && !list[aiIndex].content) {
          list[aiIndex].content = getErrorMessage(err)
          this.setData({ messages: list })
        }
        this.setData({ loading: false, isThinking: false })
        this.scrollBottom()
      },
    })
  },

  /** 推荐轮播切换 */
  onRecChange(e) {
    this.setData({ recIdx: e.detail.current })
  },

  /** 预览轮播切换（未提问时的商品示例） */
  onPreviewChange(e) {
    this.setData({ previewIdx: e.detail.current })
  },

  // 说明：推荐结果为纯浏览态，交易操作请进入商品详情页。

  /** 点击推荐商品卡进入详情页 */
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

  /** 微信一键登录（含协议确认） */
  async goLogin() {
    await ensureLogin()
  },
})
