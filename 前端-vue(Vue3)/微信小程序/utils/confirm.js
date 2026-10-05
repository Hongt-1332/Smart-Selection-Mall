/**
 * 确认弹窗（Promise 风格，替代原生 wx.showModal）
 *
 * 原生 wx.showModal 在暗色界面里会露出系统白底 + 绿色按钮，
 * 无法跟随主题。这里改用自定义组件 components/confirm-dialog。
 *
 * 使用方式与原来完全一致（无需改动业务代码）：
 *   const { confirm } = require('../../utils/message')
 *   const ok = await confirm({ title: '提示', message: '确定吗？' })
 *
 * 原理：
 *   每个页面在 wxml 顶层放一个 <confirm-dialog id="confirmDialog" />，
 *   confirm() 时通过 getCurrentPages() 找到当前页，调用组件实例的 open()。
 */

/** 当前页面上的弹窗组件实例 */
function getDialogInstance() {
  const pages = getCurrentPages()
  if (!pages.length) return null
  const page = pages[pages.length - 1]
  if (!page) return null
  // selectComponent 在基础库 2.4.4+ 可用
  if (typeof page.selectComponent !== 'function') return null
  return page.selectComponent('#confirmDialog')
}

/**
 * 弹出确认框
 * @param {object} options
 * @param {string} [options.title='提示']      标题
 * @param {string} [options.message='']        正文
 * @param {string} [options.confirmText='确定'] 确认按钮文案
 * @param {string} [options.cancelText='取消']  取消按钮文案
 * @param {boolean} [options.showCancel=true]  是否显示取消按钮
 * @param {boolean} [options.danger=false]     确认按钮是否用危险色
 * @returns {Promise<boolean>} 用户是否点了确认
 */
function confirm(options = {}) {
  return new Promise((resolve) => {
    const dialog = getDialogInstance()

    // 兜底：页面没挂组件时退回原生弹窗，保证功能不中断
    if (!dialog) {
      const {
        title = '提示',
        message = '',
        confirmText = '确定',
        cancelText = '取消',
        showCancel = true,
      } = options
      wx.showModal({
        title,
        content: message,
        confirmText,
        cancelText,
        showCancel,
        success(res) {
          resolve(!!res.confirm)
        },
        fail() {
          resolve(false)
        },
      })
      return
    }

    dialog.open({ ...options, resolve })
  })
}

module.exports = { confirm }
