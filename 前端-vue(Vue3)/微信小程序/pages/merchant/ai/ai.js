// pages/merchant/ai/ai.js — 商户 AI 上传（迁移自 src/views/merchant/MerchantAi.vue）
const { merchantApi } = require('../../../utils/api/merchant')
const { msgSuccess, msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')

const DEFAULT_FORM = {
  goodsId: null,
  category: '',
  kind: '',
  name: '',
  price: '',
  simpleDescription: '',
  features: '',
}

Page({
  data: {
    aiList: [],
    loading: false,
    pageNum: 1,
    pageSize: 10,
    total: 0,
    pages: 0,

    dialogVisible: false,
    isEdit: false,
    editId: null,
    saveLoading: false,
    form: { ...DEFAULT_FORM },
    goodsList: [],
    goodsIndex: -1,
    goodsLabels: [],
  },

  onLoad() {
    this.fetchAi()
  },

  onShow() {
    this.fetchAi()
  },

  async fetchAi() {
    this.setData({ loading: true })
    try {
      const res = await merchantApi.getAiList({ page: this.data.pageNum, size: this.data.pageSize })
      if (res.code === 200 && res.data) {
        const list = (res.data.list || []).map((row) => ({
          ...row,
          priceText: this.formatPrice(row.price),
          statusClass: row.delete ? 'tag-danger' : 'tag-success',
          statusText: row.delete ? '已删除' : '正常',
        }))
        this.setData({ aiList: list, total: res.data.total, pages: res.data.pages })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  formatPrice(val) {
    const n = Number(val)
    return isNaN(n) ? '0.00' : n.toFixed(2)
  },

  prevPage() {
    if (this.data.pageNum <= 1) return
    this.setData({ pageNum: this.data.pageNum - 1 })
    this.fetchAi()
  },

  nextPage() {
    if (this.data.pageNum >= this.data.pages) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.fetchAi()
  },

  /**
   * 拉取「关联商品」候选列表。
   * 后端没有 /merchant/ai/goodsList，改用商户自己的商品分页接口，
   * 一次取 100 条（PageRequest 的 size 上限）作为下拉候选。
   */
  async fetchGoodsList() {
    try {
      const res = await merchantApi.getGoodsList({ page: 1, size: 100 })
      if (res.code === 200 && res.data) {
        const list = res.data.list || []
        this.setData({
          goodsList: list,
          goodsLabels: list.map((g) => g.goodsName),
        })
      }
    } catch (e) {
      // ignore
    }
  },

  async openAdd() {
    this.setData({
      dialogVisible: true,
      isEdit: false,
      editId: null,
      form: { ...DEFAULT_FORM },
      goodsIndex: -1,
    })
    await this.fetchGoodsList()
  },

  openEdit(e) {
    const row = this.data.aiList[e.currentTarget.dataset.index]
    if (!row) return
    this.setData({
      dialogVisible: true,
      isEdit: true,
      editId: row.id,
      form: {
        goodsId: row.goodsId || null,
        category: row.category || '',
        kind: row.kind || '',
        name: row.name || '',
        price: row.price || '',
        simpleDescription: row.simpleDescription || '',
        features: row.features || '',
      },
    })
  },

  closeDialog() {
    this.setData({ dialogVisible: false })
  },

  onFormInput(e) {
    this.setData({ [`form.${e.currentTarget.dataset.field}`]: e.detail.value })
  },

  onGoodsChange(e) {
    const idx = Number(e.detail.value)
    const goods = this.data.goodsList[idx]
    this.setData({ goodsIndex: idx, 'form.goodsId': goods ? goods.id : null })
  },

  async handleSave() {
    const f = this.data.form
    if (!f.category) {
      msgError('请输入分类')
      return
    }
    if (!f.kind) {
      msgError('请输入种类')
      return
    }
    if (!f.name) {
      msgError('请输入名称')
      return
    }
    if (!f.price) {
      msgError('请输入价格')
      return
    }
    if (!f.simpleDescription) {
      msgError('请输入简述')
      return
    }
    if (!f.features) {
      msgError('请输入特点')
      return
    }

    this.setData({ saveLoading: true })
    try {
      const body = {
        category: f.category,
        kind: f.kind,
        name: f.name,
        price: f.price,
        simpleDescription: f.simpleDescription,
        features: f.features,
      }
      if (!this.data.isEdit && f.goodsId !== null) body.goodsId = f.goodsId

      const res = this.data.isEdit
        ? await merchantApi.updateAi({ id: this.data.editId, ...body })
        : await merchantApi.addAi(body)

      if (res.code === 200) {
        msgSuccess(this.data.isEdit ? '修改成功' : '新增成功')
        this.setData({ dialogVisible: false })
        this.fetchAi()
      } else {
        msgError(res.message || '保存失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ saveLoading: false })
    }
  },

  noop() {},
})
