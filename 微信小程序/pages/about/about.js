// pages/about/about.js — 关于（迁移自 src/views/AboutView.vue，纯静态页）

Page({
  data: {
    stats: [
      { num: '5000+', label: '合作品牌' },
      { num: '200万+', label: '注册用户' },
      { num: '50万+', label: '在售商品' },
      { num: '99.6%', label: '好评率' },
    ],
    techs: [
      { icon: '🖥', name: 'Vue 3', ver: 'v3.4+' },
      { icon: '📘', name: 'TypeScript', ver: 'v5.0+' },
      { icon: '🎨', name: 'Element Plus', ver: 'v2.6+' },
      { icon: '🍃', name: 'Spring Boot', ver: 'v3.2+' },
      { icon: '🐬', name: 'MySQL', ver: 'v8.0+' },
      { icon: '📦', name: 'Redis', ver: 'v7.0+' },
    ],
    features: [
      { icon: '🛒', title: '智能购物', desc: 'AI 智能推荐引擎，根据您的浏览和购买记录，精准推荐最适合的商品' },
      { icon: '👤', title: '用户中心', desc: '完整的订单管理、收藏夹、收货地址管理，一站式个人中心' },
      { icon: '🏪', title: '商户中心', desc: '支持商家入驻、商品上架、库存管理、销售数据分析' },
      { icon: '⚙', title: '系统管理', desc: '用户权限管理、订单审核、数据统计、系统配置等多维度后台管理' },
    ],
    contacts: [
      { label: '客服邮箱', value: 'service@youxuan.com' },
      { label: '客服热线', value: '400-888-8888' },
      { label: '工作时间', value: '周一至周日 9:00 - 21:00' },
      { label: '公司地址', value: '北京市朝阳区科技园区优选大厦' },
    ],
  },

  onShow() {
    this.syncTabBar()
  },

  /**
   * 同步自定义 tabBar 状态（关于 = index 4）。
   * tabBar 上不显示购物车角标，因此只同步 selected。
   */
  syncTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 4 })
    }
  },
})
