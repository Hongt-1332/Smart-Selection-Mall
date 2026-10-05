/**
 * 调试模式配置（纯配置文件，无界面开关）
 *
 * 用途：
 * - 开启后，未登录也能查看「我的主页」等原本需要登录的页面；
 * - 方便在开发者工具 / 真机上预览完整 UI，不必反复登录。
 *
 * 使用方法：只需修改下方 DEBUG_MODE 常量，重新编译即可生效。
 *
 * ⚠️ 正式发布请使用 'auto' 或 false，避免正式版跳过登录态判断。
 */

/** 基础库环境变量 → 'develop' | 'trial' | 'release' */
function getEnvVersion() {
  try {
    const info = wx.getAccountInfoSync ? wx.getAccountInfoSync() : null
    return (info && info.miniProgram && info.miniProgram.envVersion) || 'develop'
  } catch (e) {
    return 'develop'
  }
}

// ============================================================
// 调试模式总开关（改这里）
//   true   → 始终开启
//   false  → 始终关闭
//   'auto' → 开发版 / 体验版开启，正式版关闭（推荐）
// ============================================================
const DEBUG_MODE = 'auto'

const debug = {
  /** 是否开启调试模式 */
  isOn() {
    if (DEBUG_MODE === true) return true
    if (DEBUG_MODE === false) return false
    // 'auto'：正式版一律关闭，开发版 / 体验版开启
    return getEnvVersion() !== 'release'
  },

  /** 当前配置值（原始值），便于 UI 展示 */
  mode() {
    return DEBUG_MODE
  },

  /** 当前环境：'develop' | 'trial' | 'release' */
  env() {
    return getEnvVersion()
  },
}

module.exports = { debug, DEBUG_MODE }
