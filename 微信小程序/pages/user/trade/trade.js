// pages/user/trade/trade.js — 下单记录（迁移自 src/views/user/UserTrade.vue）
const { userApi } = require('../../../utils/api/user')
const { msgSuccess, msgError, confirm } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { buildSearchParams } = require('../../../utils/search')
const { filterNumberInput, showSearchResult } = require('../../../utils/validation')
const { money, dateTime } = require('../../../utils/format')

const EMPTY_SEARCH = {
  goodsName: '',
  merchantName: '',
  minQty: '',
  maxQty: '',
  minTotal: '',
  maxTotal: '',
  status: '',
}

Page({
  data: {
    tradeList: [],
    search: { ...EMPTY_SEARCH },
    loading: false,
    pageNum: 1,
    pageSize: 10,
    total: 0,
    pages: 0,
    advVisible: false,
    statusOptions: ['待发货', '已完成', '已取消'],
    statusIndex: -1,
  },

  onLoad() {
    this.fetchTrades()
  },

  onShow() {
    this.fetchTrades()
  },

  async fetchTrades() {
    this.setData({ loading: true })
    try {
      const { search, pageNum, pageSize } = this.data
      const params = buildSearchParams(search, pageNum, pageSize)
      const res = await userApi.getTradeList(params)
      if (res.code === 200 && res.data) {
        const list = (res.data.list || []).map((row) => ({
          ...row,
          goodsPriceText: money(row.goodsPrice),
          totalPriceText: money(row.totalPrice),
          createTimeText: dateTime(row.createTime),
          finishTimeText: row.finishTime ? dateTime(row.finishTime) : '-',
          // 后端是「下单即扣款」模式（余额支付，下单时余额已转给商家），
          // payTime 字段从不写入，因此不存在「处理中/待支付」状态：
          // 未完成且未取消的订单就是「待发货」。
          statusText: row.finishTime ? '已完成' : row.cancelTime ? '已取消' : '待发货',
          statusClass: row.finishTime ? 'tag-success' : row.cancelTime ? 'tag-danger' : 'tag-warning',
          canCancel: !row.cancelTime && !row.finishTime,
        }))
        this.setData({ tradeList: list, total: res.data.total, pages: res.data.pages })
        const s = search
        if (s.goodsName || s.merchantName || s.minQty || s.maxQty || s.minTotal || s.maxTotal || s.status) {
          showSearchResult(res.data.total, s.goodsName || s.merchantName || undefined)
        }
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  onSearchInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`search.${field}`]: e.detail.value })
  },

  onNumberInput(e) {
    const field = e.currentTarget.dataset.field
    const allowDecimal = e.currentTarget.dataset.decimal === 'true'
    this.setData({ [`search.${field}`]: filterNumberInput(e.detail.value, allowDecimal) })
  },

  onStatusChange(e) {
    const idx = Number(e.detail.value)
    const map = ['pending', 'finished', 'canceled']
    this.setData({ statusIndex: idx, 'search.status': idx >= 0 ? map[idx] : '' })
  },

  handleSearch() {
    this.setData({ pageNum: 1 })
    this.fetchTrades()
  },

  handleReset() {
    this.setData({ search: { ...EMPTY_SEARCH }, statusIndex: -1, pageNum: 1 })
    this.fetchTrades()
  },

  openAdv() {
    this.setData({ advVisible: true })
  },

  closeAdv() {
    this.setData({ advVisible: false })
  },

  advSearch() {
    this.setData({ advVisible: false, pageNum: 1 })
    this.fetchTrades()
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.setData({ pageNum: this.data.pageNum - 1 })
    this.fetchTrades()
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.fetchTrades()
  },

  async handleCancel(e) {
    const row = this.data.tradeList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({ title: '取消订单', message: '确定要取消该订单吗？' })
    if (!ok) return
    try {
      const res = await userApi.cancelTrade(row.id)
      if (res.code === 200) {
        msgSuccess('已取消')
        this.fetchTrades()
      } else if (res.code === 400 && res.message === '订单已撤销') {
        this.fetchTrades()
      } else {
        msgError(res.message || '取消失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  noop() {},
})
