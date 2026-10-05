// app.js
const { store } = require('./utils/store')
const { initAuth } = require('./utils/auth')

/** 支持 darkmode 的最低基础库版本 */
const DARKMODE_MIN_VERSION = '2.11.0'

/** 比较基础库版本号，返回 true 表示 version >= target */
function gteVersion(version, target) {
  if (!version) return false
  const v1 = String(version).split('.').map((n) => parseInt(n, 10) || 0)
  const v2 = String(target).split('.').map((n) => parseInt(n, 10) || 0)
  const len = Math.max(v1.length, v2.length)
  for (let i = 0; i < len; i++) {
    const a = v1[i] || 0
    const b = v2[i] || 0
    if (a > b) return true
    if (a < b) return false
  }
  return true
}

App({
  globalData: {
    store,
    statusBarHeight: 20,
    navBarHeight: 44,
    /** 当前主题：'dark' | 'light'，默认深色（原项目风格） */
    theme: 'dark',
    /** 基础库是否支持 DarkMode */
    darkmodeSupported: false,
  },

  onLaunch() {
    store.restore()

    try {
      const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
      this.globalData.statusBarHeight = info.statusBarHeight || 20
      this.globalData.darkmodeSupported = gteVersion(info.SDKVersion, DARKMODE_MIN_VERSION)
      // 部分版本可直接读到系统主题
      if (info.theme) {
        this.globalData.theme = info.theme === 'light' ? 'light' : 'dark'
      }
    } catch (e) {
      // ignore
    }

    // 监听系统主题变化（基础库不支持时该 API 不存在，直接跳过）
    this.watchTheme()

    // 启动初始化：首次进入先确认协议，已同意则静默登录
    initAuth()
  },

  /**
   * 监听系统主题变化。
   * 低版本基础库没有 wx.onThemeChange，此时保持默认深色主题，
   * 由 app.wxss 的深色变量兜底，界面依然正常。
   */
  watchTheme() {
    if (typeof wx.onThemeChange !== 'function') return
    try {
      wx.onThemeChange((res) => {
        this.setTheme(res.theme)
      })
    } catch (e) {
      // ignore
    }
  },

  setTheme(theme) {
    const next = theme === 'light' ? 'light' : 'dark'
    if (this.globalData.theme === next) return
    this.globalData.theme = next
    // 通知各页面主题变化
    const pages = getCurrentPages()
    pages.forEach((page) => {
      if (page && typeof page.onThemeChanged === 'function') {
        page.onThemeChanged({ theme: next })
      }
    })
  },
})
