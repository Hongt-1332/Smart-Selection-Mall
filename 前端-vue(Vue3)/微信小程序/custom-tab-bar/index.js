// custom-tab-bar/index.js — 自定义 tabBar（用符号绘制图标，不使用图片）
//
// 背景：微信原生 tabBar 的 iconPath 只支持本地图片，无法使用字体图标 / emoji。
// Vue 项目中图标本质是 Unicode 字符，这里改用自定义 tabBar 还原同样的做法。
//
// 用法：各 tab 页 onShow 中调用
//   if (typeof this.getTabBar === 'function' && this.getTabBar()) {
//     this.getTabBar().setData({ selected: <index> })
//   }
Component({
  options: {
    // 允许页面样式影响组件（便于继承主题变量）
    styleIsolation: 'apply-shared',
  },

  data: {
    /** 当前选中项索引 */
    selected: 0,
    /** tab 列表：icon 为符号/emoji，与 Vue 项目的图标用法一致 */
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        icon: '⌂',
      },
      {
        pagePath: '/pages/ai-recommend/ai-recommend',
        text: '智能推荐',
        icon: '✦',
      },
      {
        pagePath: '/pages/user/goods/goods',
        text: '用户中心',
        icon: '☺',
        // 不在 tabBar 上显示购物车角标：
        // 角标只出现在「用户中心」页面内的「购物车」入口上。
      },
      {
        pagePath: '/pages/merchant/goods/goods',
        text: '商户中心',
        icon: '⛁',
      },
      {
        pagePath: '/pages/about/about',
        text: '关于',
        icon: 'ⓘ',
      },
    ],
  },

  methods: {
    /** 点击切换：tabBar 页必须用 switchTab */
    onTap(e) {
      const index = Number(e.currentTarget.dataset.index)
      const item = this.data.list[index]
      if (!item) return
      if (index === this.data.selected) return

      wx.switchTab({
        url: item.pagePath,
        success: () => {
          this.setData({ selected: index })
        },
      })
    },
  },
})
