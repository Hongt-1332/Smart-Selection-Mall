// pages/search/search.js — 商品搜索页
//
// 为什么独立成页：小程序单屏承载能力有限，
// 把「关键词 + 商家 + 价格区间 + 结果列表」全塞进列表页会非常拥挤，
// 因此拆成独立搜索页，原列表页只保留一个搜索入口。
const { userApi } = require('../../utils/api/user')
const { msgError } = require('../../utils/message')
const { getErrorMessage } = require('../../utils/error')
const { money } = require('../../utils/format')
const { navigate } = require('../../utils/nav')
const { filterNumberInput } = require('../../utils/validation')
const { FALLBACK_IMAGE } = require('../../utils/demo-data')

Page({
  data: {
    keyword: '',
    form: { userName: '', priceMin: '', priceMax: '' },
    filterOpen: false,
    /** 已选条件标签 */
    chips: [],
    goodsList: [],
    total: 0,
    pages: 0,
    pageNum: 1,
    pageSize: 12,
    loading: false,
    /** 是否已执行过搜索（用于区分「未搜索」和「无结果」） */
    searched: false,
    autoFocus: false,
  },

  onLoad(options) {
    // 支持从其它页面带关键词直接进入
    if (options && options.keyword) {
      this.setData({ keyword: decodeURIComponent(options.keyword) })
      this.handleSearch()
    }
  },

  // ===== 输入 =====
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    if (!field) return
    this.setData({ [`form.${field}`]: e.detail.value })
    this.buildChips()
  },

  /** 价格输入只允许数字和小数点 */
  onNumberInput(e) {
    const field = e.currentTarget.dataset.field
    if (!field) return
    const val = filterNumberInput(e.detail.value)
    this.setData({ [`form.${field}`]: val })
    this.buildChips()
  },

  toggleFilter() {
    this.setData({ filterOpen: !this.data.filterOpen })
  },

  // ===== 条件标签 =====
  buildChips() {
    const { form } = this.data
    const chips = []
    if (form.userName) chips.push({ key: 'userName', text: `商家：${form.userName}` })
    if (form.priceMin) chips.push({ key: 'priceMin', text: `最低 ¥${form.priceMin}` })
    if (form.priceMax) chips.push({ key: 'priceMax', text: `最高 ¥${form.priceMax}` })
    this.setData({ chips })
  },

  removeChip(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return
    this.setData({ [`form.${key}`]: '' })
    this.buildChips()
    if (this.data.searched) this.handleSearch()
  },

  resetAll() {
    this.setData({
      keyword: '',
      form: { userName: '', priceMin: '', priceMax: '' },
      chips: [],
      goodsList: [],
      total: 0,
      pages: 0,
      pageNum: 1,
      searched: false,
    })
  },

  // ===== 搜索 =====
  handleSearch() {
    this.setData({ pageNum: 1 })
    this.doSearch(1)
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.doSearch(this.data.pageNum - 1)
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.doSearch(this.data.pageNum + 1)
  },

  async doSearch(page) {
    const { keyword, form, pageSize } = this.data
    this.setData({ loading: true, searched: true })

    const params = { page, size: pageSize }
    const kw = (keyword || '').trim()
    if (kw) params.goodsName = kw
    if (form.userName) params.userName = form.userName
    if (form.priceMin) params.priceMin = Number(form.priceMin)
    if (form.priceMax) params.priceMax = Number(form.priceMax)

    try {
      const res = await userApi.getGoodsList(params)
      if (res && res.code === 200 && res.data) {
        const list = (res.data.list || []).map((g, i) => {
          const price = Number(g.goodsPrice)
          const stock = Number(g.goodsStock)
          return {
            ...g,
            id: g.id != null ? g.id : `local_${i}`,
            goodsName: g.goodsName || '未命名商品',
            describe: g.describe || '',
            goodsPrice: isNaN(price) ? 0 : price,
            goodsPriceText: money(isNaN(price) ? 0 : price),
            goodsStock: isNaN(stock) ? 0 : stock,
            // 接口没给图时用本地兜底图，避免整屏「暂无图片」
            imagePath: g.imagePath || FALLBACK_IMAGE,
            userName: g.userName || '未知商家',
          }
        })
        this.setData({
          goodsList: list,
          total: res.data.total || 0,
          pages: res.data.pages || 0,
          pageNum: page,
        })
      } else {
        this.setData({ goodsList: [], total: 0, pages: 0, pageNum: page })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
      this.setData({ goodsList: [], total: 0, pages: 0 })
    } finally {
      this.setData({ loading: false })
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id === undefined || id === null || id === '') return
    navigate(`/pages/goods-detail/goods-detail?id=${id}`)
  },

  /**
   * 离开搜索页时，把条件写回全局，供列表页 onShow 读取。
   * 这样用户从搜索页返回后，列表页能展示同样的筛选结果。
   */
  onUnload() {
    const app = getApp()
    if (!app) return
    const { keyword, form, searched } = this.data
    app.globalData = app.globalData || {}
    app.globalData.lastSearch = searched
      ? {
          goodsName: (keyword || '').trim(),
          userName: form.userName,
          priceMin: form.priceMin,
          priceMax: form.priceMax,
        }
      : null
  },
})
