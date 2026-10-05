/**
 * 消息提示（对应原项目 utils/message.ts）
 * 使用防抖去重，避免同一提示重复弹出
 */

const activeMessages = {}

function dedup(key, fn, cooldown = 1500) {
  const now = Date.now()
  const last = activeMessages[key]
  if (last && now - last < cooldown) return
  activeMessages[key] = now
  fn()
  setTimeout(() => {
    if (activeMessages[key] === now) delete activeMessages[key]
  }, cooldown)
}

function toast(message, icon) {
  wx.showToast({ title: message, icon, duration: 1800, mask: false })
}

function msgSuccess(message) {
  dedup('success:' + message, () => toast(message, 'success'))
}

function msgError(message) {
  dedup('error:' + message, () => toast(message, 'none'))
}

function msgWarning(message) {
  dedup('warning:' + message, () => toast(message, 'none'))
}

function msgInfo(message) {
  dedup('info:' + message, () => toast(message, 'none'))
}

/**
 * 确认对话框，返回 Promise<boolean>
 * 对应原项目 utils/confirm.ts 的 confirm()
 *
 * 实现已迁移到 utils/confirm.js（自定义深色弹窗组件），
 * 这里直接复用，保证所有页面 `require('utils/message').confirm`
 * 的调用方式不变，且弹窗配色跟随主题。
 */
const { confirm } = require('./confirm')

module.exports = {
  msgSuccess,
  msgError,
  msgWarning,
  msgInfo,
  confirm,
}
