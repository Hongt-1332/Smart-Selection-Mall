// components/confirm-dialog/index.js
// 自定义确认弹窗（替代原生 wx.showModal）
//
// 为什么不用原生 wx.showModal：
// 原生弹窗由微信渲染，强制白底 + 绿色按钮，无法跟随 app.wxss 的深色主题，
// 在暗色界面里非常突兀。这里用自定义组件复刻同样的交互，但配色走主题变量。
//
// 用法（命令式，全局只挂一个实例）：
//   const { confirm } = require('../../utils/confirm')
//   const ok = await confirm({ title: '提示', message: '确定吗？' })
Component({
  options: {
    // 允许多个实例叠加时样式独立
    multipleSlots: false,
  },

  data: {
    visible: false,
    title: '',
    message: '',
    confirmText: '确定',
    cancelText: '取消',
    /** 是否显示取消按钮（纯提示型可不显示） */
    showCancel: true,
    /** 确认按钮是否用危险色（删除类操作） */
    danger: false,
  },

  methods: {
    /** 由 utils/confirm.js 调用，打开弹窗 */
    open(options = {}) {
      this.setData({
        visible: true,
        title: options.title || '提示',
        message: options.message || '',
        confirmText: options.confirmText || '确定',
        cancelText: options.cancelText || '取消',
        showCancel: options.showCancel !== false,
        danger: !!options.danger,
      })
      this._resolve = options.resolve
    },

    /** 关闭并回传结果 */
    _close(result) {
      this.setData({ visible: false })
      if (this._resolve) {
        this._resolve(result)
        this._resolve = null
      }
    },

    onConfirm() {
      this._close(true)
    },

    onCancel() {
      this._close(false)
    },

    /** 点击遮罩关闭（等同取消） */
    onMaskTap() {
      this._close(false)
    },

    /** 阻止弹窗内部点击冒泡到遮罩 */
    noop() {},
  },
})
