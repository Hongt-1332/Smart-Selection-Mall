// pages/user/bg/bg.js — 我的主页/背景展示（迁移自 src/views/user/UserBg.vue）
const { userApi } = require('../../../utils/api/user')
const { store } = require('../../../utils/store')
const { debug } = require('../../../utils/debug')
const { msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { FALLBACK_IMAGE } = require('../../../utils/demo-data')

/** 调试模式下使用的演示数据（未登录也能看到完整 UI） */
function demoUserInfo() {
  return {
    userName: '演示用户',
    account: 'demo_visitor',
    describe: '这是调试模式下的演示数据，用于预览页面 UI，无需登录即可查看。',
    level: 3,
    levelName: '黄金会员',
    vipRemainingTime: 88,
  }
}

function demoGoods() {
  const names = ['演示商品 A', '演示商品 B', '演示商品 C', '演示商品 D']
  const prices = [128, 256, 512, 1024]
  return names.map((n, i) => ({
    id: -(i + 1),
    goodsName: n,
    describe: '调试模式演示数据',
    goodsPrice: prices[i],
    goodsStock: 50,
    // 本地兜底图，不使用外链
    imagePath: FALLBACK_IMAGE,
    imageUrl: FALLBACK_IMAGE,
    address: '演示地址',
    cartQuantity: 0,
  }))
}

function demoBgImages() {
  return [1, 2, 3, 4, 5, 6].map((i) => ({
    id: i,
    imagePath: FALLBACK_IMAGE,
    // 本地兜底图，不使用外链
    url: FALLBACK_IMAGE,
    sequence: i,
  }))
}

Page({
  data: {
    loading: false,
    userInfo: {
      userName: '',
      account: '',
      describe: '',
      level: 0,
      levelName: '普通用户',
      vipRemainingTime: 0,
    },
    avatarUrl: '',
    goodsList: [],
    imageMap: {},
    bgImages: [],
    /** 背景图轮播当前索引 */
    bgIdx: 0,
    /** 相关状态 */
    isLoggedIn: false,
    debugMode: false,
    /** 是否为演示数据（顶部提示条） */
    isDemo: false,
  },

  onLoad() {
    this.syncState()
    this.loadIfReady()
  },

  onShow() {
    this.syncState()
    this.loadIfReady()
  },

  /** 同步登录态 / 调试模式 */
  syncState() {
    this.setData({
      isLoggedIn: store.isLoggedIn,
      debugMode: debug.isOn(),
    })
  },

  /**
   * 已登录 → 拉真实数据
   * 未登录但开了调试模式 → 用演示数据渲染，保证能看到完整页面
   * （调试模式来自 utils/debug.js 配置，每次 onShow 都会重新读取）
   */
  loadIfReady() {
    if (store.isLoggedIn) {
      if (store.dirty || !this.data.goodsList.length) this.fetchAll()
      return
    }
    if (debug.isOn()) {
      this.applyDemoData()
    } else {
      // 配置已关闭且未登录 → 清掉演示数据
      this.clearDemoData()
    }
  },

  /** 应用演示数据 */
  applyDemoData() {
    if (this.data.isDemo) return
    this.setData({
      isDemo: true,
      userInfo: demoUserInfo(),
      goodsList: demoGoods(),
      bgImages: demoBgImages(),
      bgIdx: 0,
      // 本地兜底头像，不使用外链
      avatarUrl: FALLBACK_IMAGE,
    })
  },

  /** 退出调试模式时清空演示数据 */
  clearDemoData() {
    if (!this.data.isDemo) return
    this.setData({
      isDemo: false,
      userInfo: {
        userName: '',
        account: '',
        describe: '',
        level: 0,
        levelName: '普通用户',
        vipRemainingTime: 0,
      },
      goodsList: [],
      bgImages: [],
      bgIdx: 0,
      avatarUrl: '',
    })
  },

  async fetchAll() {
    this.setData({ loading: true })
    try {
      const [info, bgRes] = await Promise.all([
        store.fetchUserInfoVO(true),
        userApi.getUserBackground(),
      ])

      if (info) {
        this.setData({
          'userInfo.account': info.account || '',
          'userInfo.vipRemainingTime': Number(info.vipRemainingTime) || 0,
          avatarUrl: info.avatarPath || '',
        })
        if (info.avatarPath) {
          try {
            const imgRes = await userApi.getImages([info.avatarPath])
            if (imgRes.code === 200 && imgRes.data) {
              this.setData({ avatarUrl: imgRes.data[info.avatarPath] || info.avatarPath })
            }
          } catch (e) {
            // ignore
          }
        }
      }

      if (bgRes.code === 200 && bgRes.data) {
        const bgData = bgRes.data
        const goods = bgData.goods || []

        const paths = []
        ;(bgData.backgrounds || []).forEach((bg) => {
          if (bg.imagePath) paths.push(bg.imagePath)
        })
        goods.forEach((g) => {
          if (g.imagePath) paths.push(g.imagePath)
        })
        const uniquePaths = [...new Set(paths.filter(Boolean))]

        let imageMap = {}
        if (uniquePaths.length) {
          try {
            const imgRes = await userApi.getImages(uniquePaths)
            if (imgRes.code === 200 && imgRes.data) imageMap = imgRes.data
          } catch (e) {
            // ignore
          }
        }

        const bgImages = (bgData.backgrounds || []).map((bg, index) => ({
          id: index + 1,
          imagePath: bg.imagePath,
          url: imageMap[bg.imagePath] || bg.imagePath,
          sequence: bg.sequence,
        }))

        this.setData({
          userInfo: {
            userName: bgData.userName || '',
            account: this.data.userInfo.account,
            describe: bgData.describe || '',
            level: bgData.level || 0,
            levelName: bgData.levelName || '普通用户',
            vipRemainingTime: this.data.userInfo.vipRemainingTime,
          },
          goodsList: goods.map((g) => ({ ...g, imageUrl: imageMap[g.imagePath] || g.imagePath })),
          imageMap,
          bgImages,
          bgIdx: 0,
        })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 背景图轮播切换 */
  onBgSwiperChange(e) {
    this.setData({ bgIdx: e.detail.current })
  },

  /** 点击在售商品卡进入详情页 */
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${id}` })
  },

  // 说明：本页为浏览态，交易操作请进入商品详情页。

  previewBg(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.previewImage({ urls: this.data.bgImages.map((b) => b.url), current: url })
  },
})
