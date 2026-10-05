// pages/user/merchant/merchant.js — 商家主页（迁移自 src/views/user/MerchantProfile.vue）
const { userApi } = require('../../../utils/api/user')
const { msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { navigate } = require('../../../utils/nav')

Page({
  data: {
    userId: 0,
    loading: false,
    profile: {
      userName: '',
      describe: '',
      level: 0,
      levelName: '',
      backgrounds: [],
      goods: [],
    },
    imageMap: {},
    merchantAvatar: '',
    heroBgUrl: '',
    firstAddress: '',
    bgOffset: 0,
    bgAutoPlay: false,
    bgImages: [],
    bgSlots: [null, null, null, null],
    goodsList: [],
  },

  bgAutoTimer: null,

  onLoad(options) {
    const userId = Number(options.userId) || 0
    this.setData({ userId })
    wx.setNavigationBarTitle({ title: '商家主页' })
    if (userId) this.fetchMerchant()
  },

  onUnload() {
    this.stopAutoPlay()
  },

  async fetchMerchant() {
    this.setData({ loading: true })
    try {
      const res = await userApi.getMerchantBackground(this.data.userId)
      if (res.code === 200 && res.data) {
        const profile = res.data
        const paths = []
        ;(profile.backgrounds || []).forEach((bg) => {
          if (bg.imagePath) paths.push(bg.imagePath)
        })
        ;(profile.goods || []).forEach((g) => {
          if (g.imagePath) paths.push(g.imagePath)
          if (g.merchantAvatar) paths.push(g.merchantAvatar)
        })
        const uniquePaths = [...new Set(paths.filter(Boolean))]

        let imageMap = {}
        if (uniquePaths.length) {
          const imgRes = await userApi.getImages(uniquePaths)
          if (imgRes.code === 200 && imgRes.data) imageMap = imgRes.data
        }

        const backgrounds = (profile.backgrounds || []).slice().sort((a, b) => a.sequence - b.sequence)
        const goods = (profile.goods || []).map((g) => ({
          ...g,
          cartQuantity: g.cartQuantity || 0,
          imageUrl: imageMap[g.imagePath] || g.imagePath,
        }))

        const rawAvatar = (goods[0] && goods[0].merchantAvatar) || ''
        const firstBg = backgrounds[0]

        this.setData({
          profile,
          imageMap,
          goodsList: goods,
          merchantAvatar: imageMap[rawAvatar] || rawAvatar,
          heroBgUrl: firstBg ? imageMap[firstBg.imagePath] || '' : '',
          firstAddress: (goods[0] && goods[0].address) || '',
          bgImages: backgrounds.map((bg) => ({
            ...bg,
            url: imageMap[bg.imagePath] || bg.imagePath,
          })),
          bgOffset: 0,
        })
        this.buildBgSlots()
      } else {
        msgError(res.message || '获取商家信息失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  buildBgSlots() {
    const { bgImages, bgOffset } = this.data
    const slots = [null, null, null, null]
    for (let i = 0; i < 4; i++) {
      const idx = bgOffset + i
      if (idx < bgImages.length) slots[i] = bgImages[idx]
    }
    this.setData({ bgSlots: slots })
  },

  bgPrev() {
    if (this.data.bgOffset > 0) {
      this.setData({ bgOffset: this.data.bgOffset - 4 })
      this.buildBgSlots()
    }
  },

  bgNext() {
    if (this.data.bgOffset + 4 < this.data.bgImages.length) {
      this.setData({ bgOffset: this.data.bgOffset + 4 })
      this.buildBgSlots()
    }
  },

  toggleAutoPlay() {
    if (this.data.bgAutoPlay) {
      this.stopAutoPlay()
      this.setData({ bgAutoPlay: false })
    } else {
      this.setData({ bgAutoPlay: true })
      this.startAutoPlay()
    }
  },

  startAutoPlay() {
    this.stopAutoPlay()
    this.bgAutoTimer = setInterval(() => {
      const { bgOffset, bgImages } = this.data
      const next = bgOffset + 4 >= bgImages.length ? 0 : bgOffset + 4
      this.setData({ bgOffset: next })
      this.buildBgSlots()
    }, 4000)
  },

  stopAutoPlay() {
    if (this.bgAutoTimer) {
      clearInterval(this.bgAutoTimer)
      this.bgAutoTimer = null
    }
  },

  /** 点击商品卡进入详情页（与用户中心 / 首页保持一致） */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    navigate(`/pages/goods-detail/goods-detail?id=${id}`)
  },

  previewBg(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.previewImage({ urls: this.data.bgImages.map((b) => b.url), current: url })
  },

  noop() {},
})
