// pages/merchant/goods/goods.js — 商户商品管理（迁移自 src/views/merchant/MerchantGoods.vue）
const { merchantApi } = require('../../../utils/api/merchant')
const { msgSuccess, msgError, confirm } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { money } = require('../../../utils/format')

Page({
  data: {
    goodsList: [],
    loading: false,
    pageNum: 1,
    pageSize: 10,
    total: 0,
    pages: 0,
    launchOptions: ['全部', '已上架', '已下架'],
    launchIndex: 0,
    launchValue: undefined,
    /** 商品名称搜索关键词 */
    goodsName: '',
  },

  onLoad() {
    this.fetchGoods()
  },

  onShow() {
    this.fetchGoods()
    this.syncTabBar()
  },

  /**
   * 同步自定义 tabBar 状态（商户中心 = index 3）。
   * tabBar 上不显示购物车角标，因此只同步 selected。
   */
  syncTabBar() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }
  },

  async fetchGoods() {
    this.setData({ loading: true })
    try {
      const params = { page: this.data.pageNum, size: this.data.pageSize }
      if (this.data.launchValue !== undefined) params.launch = this.data.launchValue
      const kw = (this.data.goodsName || '').trim()
      if (kw) params.goodsName = kw
      const res = await merchantApi.getGoodsList(params)
      if (res.code === 200 && res.data) {
        const list = (res.data.list || []).map((row) => ({
          ...row,
          goodsPriceText: money(row.goodsPrice),
          launchNum: row.launch === 1 || row.launch === true ? 1 : 0,
        }))
        this.setData({ goodsList: list, total: res.data.total, pages: res.data.pages })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 搜索框输入 */
  onKeywordInput(e) {
    this.setData({ goodsName: e.detail.value })
  },

  /** 执行搜索 */
  handleSearch() {
    this.setData({ pageNum: 1 })
    this.fetchGoods()
  },

  onLaunchChange(e) {
    const idx = Number(e.detail.value)
    const map = [undefined, 1, 0]
    this.setData({ launchIndex: idx, launchValue: map[idx], pageNum: 1 })
    this.fetchGoods()
  },

  handleReset() {
    this.setData({ goodsName: '', launchIndex: 0, launchValue: undefined, pageNum: 1 })
    this.fetchGoods()
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.setData({ pageNum: this.data.pageNum - 1 })
    this.fetchGoods()
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.fetchGoods()
  },

  // ===== 新增 / 编辑（跳转独立页面） =====
  //
  // 原先是本页的底部弹层，但弹层内的固定提交栏在小程序里始终无法稳定显示
  // （受 flex 高度分配影响，排查多轮未果），因此改为独立页面承载表单。

  /** 新增商品 */
  openAdd() {
    wx.navigateTo({ url: '/pages/merchant/goods-edit/goods-edit' })
  },

  /** 编辑商品 */
  openEdit(e) {
    const row = this.data.goodsList[e.currentTarget.dataset.index]
    if (!row) return
    wx.navigateTo({
      url: `/pages/merchant/goods-edit/goods-edit?id=${row.id}`,
    })
  },

  async handleToggleLaunch(e) {
    const row = this.data.goodsList[e.currentTarget.dataset.index]
    if (!row) return
    const toLaunch = row.launchNum === 1 ? 0 : 1
    const action = toLaunch === 1 ? '上架' : '下架'
    const ok = await confirm({
      title: `${action}确认`,
      message: `确定要${action}商品「${row.goodsName}」吗？`,
      confirmText: action,
    })
    if (!ok) return
    try {
      const res = await merchantApi.updateLaunch(row.id, toLaunch)
      if (res.code === 200) {
        msgSuccess(`${action}成功`)
        this.fetchGoods()
      } else {
        msgError(`${action}失败`)
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  /** 删除商品（后端 DELETE /merchant/goods/delete?id=，仅能删自己的商品） */
  async handleDelete(e) {
    const row = this.data.goodsList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({
      title: '删除商品',
      message: `删除后不可恢复，确定删除「${row.goodsName}」吗？`,
      confirmText: '删除',
      danger: true,
    })
    if (!ok) return
    try {
      const res = await merchantApi.deleteGoods(row.id)
      if (res.code === 200) {
        msgSuccess('已删除')
        this.fetchGoods()
      } else {
        msgError(res.message || '删除失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

})
