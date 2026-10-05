/**
 * 微信一键登录 + 启动协议确认
 *
 * 设计背景：
 * 小程序不需要用户主动注册 / 输账号密码，登录应该是「静默」的：
 *   wx.login() 拿 code → 后端 code2Session 换 openid → 返回 token
 *
 * 协议：
 * 首次进入小程序时弹一次确认框，同意后写入本地标记，
 * 之后不再打扰，直接静默登录。
 *
 * 使用方式（app.js onLaunch 调用）：
 *   const { initAuth } = require('./utils/auth')
 *   initAuth()
 */
const { userApi } = require('./api/user')
const { store } = require('./store')
const { setStoredToken } = require('./request')
const { getCache, setCache, clearCache } = require('./cache')
const { msgSuccess, msgError } = require('./message')

/** 用户是否已同意协议（本地持久化） */
const AGREEMENT_KEY = 'agreement_accepted'

/** 协议文本（正式发布前请替换为法务确认的正式条款） */
const AGREEMENT = {
  title: '用户协议与隐私政策',
  content:
    '欢迎使用智选商城。\n\n' +
    '为提供商品浏览、下单与配送服务，我们需要获取你的微信账号标识用于绑定账号，' +
    '并在下单时使用你填写的收货信息。\n\n' +
    '我们不会向第三方出售你的个人信息。\n\n' +
    '点击「同意」即表示你已阅读并同意《用户协议》与《隐私政策》。',
  confirmText: '同意',
  cancelText: '不同意',
}

/** 是否已同意过协议 */
function hasAcceptedAgreement() {
  try {
    return !!getCache(AGREEMENT_KEY)
  } catch (e) {
    return false
  }
}

/** 记录已同意协议 */
function markAgreementAccepted() {
  try {
    setCache(AGREEMENT_KEY, true)
  } catch (e) {
    // ignore
  }
}

/**
 * 弹出协议确认框（必须同意才能继续使用）
 * @returns {Promise<boolean>} 用户是否同意
 */
function confirmAgreement() {
  return new Promise((resolve) => {
    wx.showModal({
      title: AGREEMENT.title,
      content: AGREEMENT.content,
      confirmText: AGREEMENT.confirmText,
      cancelText: AGREEMENT.cancelText,
      confirmColor: '#5b9eff',
      /** 点击遮罩/返回键也视为不同意，不允许绕过 */
      success(res) {
        resolve(!!res.confirm)
      },
      fail() {
        resolve(false)
      },
    })
  })
}

/** 封装 wx.login 为 Promise */
function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) resolve(res.code)
        else reject(new Error('获取微信登录凭证失败'))
      },
      fail(err) {
        reject(new Error(err.errMsg || '微信登录失败'))
      },
    })
  })
}

/**
 * 执行登录（协议已确认的前提下调用）
 * @returns {Promise<{ok: boolean, message?: string, isNewUser?: boolean}>}
 */
async function doLogin() {
  let code
  try {
    code = await wxLogin()
  } catch (e) {
    return { ok: false, message: e.message }
  }

  try {
    const res = await userApi.wechatLogin({ code })
    if (res.code !== 200 || !res.data) {
      return { ok: false, message: res.message || '登录失败' }
    }
    if (res.data.token) setStoredToken(res.data.token)
    store.isLoggedIn = true
    store.markDirty()
    await store.fetchUserInfoVO(true)
    store.persist()
    return { ok: true, isNewUser: !!res.data.isNewUser }
  } catch (e) {
    return { ok: false, message: e.message || '网络异常，请稍后重试' }
  }
}

/**
 * 应用启动初始化（app.js onLaunch 调用）
 *
 * 流程：
 *   首次进入 → 弹协议确认 → 同意后静默登录
 *   已同意过 → 直接静默登录，无任何打扰
 */
async function initAuth() {
  await store.checkSession()

  if (!hasAcceptedAgreement()) {
    const agreed = await confirmAgreement()
    if (!agreed) {
      // 不同意协议 → 直接退出小程序
      exitMiniProgram()
      return
    }
    markAgreementAccepted()
  }

  // 已同意协议 → 静默登录
  await ensureLogin({ silent: true })
}

/**
 * 退出小程序。
 * wx.exitMiniProgram 需要基础库 2.17.3+，低版本降级为
 * 「返回上一个页面 / 提示用户手动关闭」，避免卡在启动页。
 */
function exitMiniProgram() {
  if (typeof wx.exitMiniProgram === 'function') {
    wx.exitMiniProgram({
      fail() {
        fallbackExit()
      },
    })
  } else {
    fallbackExit()
  }
}

function fallbackExit() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    wx.navigateBack({ delta: pages.length })
  } else {
    wx.showToast({ title: '需同意协议后才能使用', icon: 'none', duration: 2000 })
  }
}

/**
 * 确保已登录（业务侧调用入口）
 *
 * @param {object} [options]
 * @param {boolean} [options.silent] 已登录时不弹成功提示
 * @returns {Promise<boolean>} 是否处于已登录状态
 */
async function ensureLogin(options = {}) {
  const { silent = false } = options

  if (store.isLoggedIn) return true

  const result = await doLogin()
  if (result.ok) {
    if (!silent) msgSuccess(result.isNewUser ? '欢迎加入智选商城' : '登录成功')
    return true
  }

  msgError(result.message || '登录失败')
  return false
}

/** 退出登录（保留协议同意状态，下次无需重复确认） */
async function logout() {
  await store.logout()
}

/** 重置协议同意状态（仅用于调试） */
function resetAgreement() {
  try {
    clearCache(AGREEMENT_KEY)
  } catch (e) {
    // ignore
  }
}

module.exports = {
  initAuth,
  ensureLogin,
  logout,
  hasAcceptedAgreement,
  confirmAgreement,
  resetAgreement,
  AGREEMENT,
}
