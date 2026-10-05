/**
 * 导航工具
 *
 * 小程序限制：tabBar 页面只能用 wx.switchTab，非 tabBar 页面只能用 wx.navigateTo。
 * 这里统一判断，避免出现
 *   "switchTab:fail can not switch to no-tabBar page" 或
 *   "navigateTo:fail can not navigateTo a tabbar page"
 * 这类运行时报错。
 */

/** 与 app.json 中 tabBar.list 保持一致 */
const TAB_BAR_PAGES = [
  'pages/index/index',
  'pages/ai-recommend/ai-recommend',
  'pages/user/goods/goods',
  'pages/merchant/goods/goods',
  'pages/about/about',
]

function normalize(url) {
  return String(url || '').replace(/^\//, '').split('?')[0]
}

function isTabBarPage(url) {
  return TAB_BAR_PAGES.indexOf(normalize(url)) >= 0
}

/**
 * 打开页面（自动区分 switchTab / navigateTo）
 * @param {string} url 绝对路径，如 /pages/user/cart/cart
 */
function navigate(url) {
  if (!url) return
  if (isTabBarPage(url)) {
    wx.switchTab({ url: '/' + normalize(url) })
  } else {
    wx.navigateTo({ url })
  }
}

/** 强制按 tabBar 方式跳转 */
function switchTab(url) {
  wx.switchTab({ url: '/' + normalize(url) })
}

/** 返回上一页；没有上一页时回退到指定页面 */
function back(fallbackUrl) {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    wx.navigateBack()
  } else if (fallbackUrl) {
    navigate(fallbackUrl)
  }
}

module.exports = { navigate, switchTab, back, isTabBarPage, TAB_BAR_PAGES }
