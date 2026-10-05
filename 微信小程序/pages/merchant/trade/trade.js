// pages/merchant/trade/trade.js — 商户订单管理（迁移自 src/views/merchant/MerchantTrade.vue）
const { merchantApi } = require('../../../utils/api/merchant')
const { msgSuccess, msgError, confirm } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')
const { buildSearchParams } = require('../../../utils/search')
const { filterNumberInput, showSearchResult } = require('../../../utils/validation')
const { money } = require('../../../utils/format')

const EMPTY_SEARCH = {
  goodsName: '',
  userName: '',
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
    statusOptions: ['处理中', '已完成', '已取消'],
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
      const params = buildSearchParams(this.data.search, this.data.pageNum, this.data.pageSize)
      const res = await merchantApi.getTradeList(params)
      if (res.code === 200 && res.data) {
        // 订单状态由 trade 表的时间字段推导（对应后端 Trade 实体）：
        //   finishTime 有值 → 已完成；cancelTime 有值 → 已取消。
        // 注意：后端是「下单即扣款」模式（余额支付），创建订单时余额已结转，
        // payTime 字段从不写入 —— 之前用 payTime 判断「已支付」导致发货按钮永远不出现。
        // 因此：未完成且未取消的订单即为「待发货」，可直接发货。
        const list = (res.data.list || []).map((row) => {
          const finished = !!row.finishTime
          const canceled = !!row.cancelTime
          return {
            ...row,
            goodsPriceText: money(row.goodsPrice),
            totalPriceText: money(row.totalPrice),
            canDeliver: !finished && !canceled,
            canConfirm: !finished && !canceled,
            // 可删除：已完成或已取消
            canDelete: finished || canceled,
            statusText: finished ? '已完成' : canceled ? '已取消' : '待发货',
            statusClass: finished ? 'tag-success' : canceled ? 'tag-danger' : 'tag-warning',
          }
        })
        this.setData({ tradeList: list, total: res.data.total, pages: res.data.pages })

        const s = this.data.search
        if (s.goodsName || s.userName || s.minQty || s.maxQty || s.minTotal || s.maxTotal || s.status) {
          showSearchResult(res.data.total, s.goodsName || undefined)
        }
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  onSearchInput(e) {
    this.setData({ [`search.${e.currentTarget.dataset.field}`]: e.detail.value })
  },

  onNumberInput(e) {
    const field = e.currentTarget.dataset.field
    const allowDecimal = e.currentTarget.dataset.decimal === 'true'
    this.setData({ [`search.${field}`]: filterNumberInput(e.detail.value, allowDecimal) })
  },

  onStatusChange(e) {
    const idx = Number(e.detail.value)
    const map = ['pending', 'finished', 'canceled']
    this.setData({ statusIndex: idx, 'search.status': map[idx] || '' })
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

  /** 发货（后端 PUT /merchant/trade/deliver） */
  async handleDeliver(e) {
    const row = this.data.tradeList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({
      title: '发货确认',
      message: `确定要为订单「${row.goodsName}」发货吗？`,
      confirmText: '发货',
    })
    if (!ok) return
    try {
      const res = await merchantApi.deliverTrade(row.id)
      if (res.code === 200) {
        msgSuccess('已发货')
        this.fetchTrades()
      } else {
        msgError(res.message || '发货失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  /** 确认（后端 PUT /merchant/trade/confirm） */
  async handleConfirm(e) {
    const row = this.data.tradeList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({
      title: '确认订单',
      message: `确定要确认订单「${row.goodsName}」吗？`,
      confirmText: '确认',
    })
    if (!ok) return
    try {
      const res = await merchantApi.confirmTrade(row.id)
      if (res.code === 200) {
        msgSuccess('已确认')
        this.fetchTrades()
      } else {
        msgError(res.message || '确认失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  /** 删除订单（后端 DELETE /merchant/trade/delete） */
  async handleDelete(e) {
    const row = this.data.tradeList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({
      title: '删除订单',
      message: '删除后不可恢复，确定继续吗？',
      confirmText: '删除',
    })
    if (!ok) return
    try {
      const res = await merchantApi.deleteTrade(row.id)
      if (res.code === 200) {
        msgSuccess('已删除')
        this.fetchTrades()
      } else {
        msgError(res.message || '删除失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  noop() {},
})
