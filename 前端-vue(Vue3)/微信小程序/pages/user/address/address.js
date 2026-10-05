// pages/user/address/address.js — 地址管理（迁移自 src/views/user/UserAddress.vue）
const { userApi } = require('../../../utils/api/user')
const { store } = require('../../../utils/store')
const { msgSuccess, msgError, confirm } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')

const EMPTY_FORM = {
  country: '',
  province: '',
  city: '',
  county: '',
  detail: '',
}

Page({
  data: {
    addressList: [],
    loading: false,
    dialogVisible: false,
    isEdit: false,
    editId: null,
    saveLoading: false,
    form: { ...EMPTY_FORM },
  },

  onLoad() {
    this.fetchAddresses()
  },

  onShow() {
    this.fetchAddresses()
  },

  async fetchAddresses() {
    this.setData({ loading: true })
    try {
      const res = await userApi.getAddressList({ page: 1, size: 50 })
      if (res.code === 200 && res.data) {
        this.setData({ addressList: res.data.list || [] })
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ loading: false })
    }
  },

  openAdd() {
    this.setData({
      dialogVisible: true,
      isEdit: false,
      editId: null,
      form: { ...EMPTY_FORM },
    })
  },

  openEdit(e) {
    const row = this.data.addressList[e.currentTarget.dataset.index]
    if (!row) return
    this.setData({
      dialogVisible: true,
      isEdit: true,
      editId: row.id,
      form: {
        country: row.country || '',
        province: row.province || '',
        city: row.city || '',
        county: row.county || '',
        detail: row.detail || '',
      },
    })
  },

  closeDialog() {
    this.setData({ dialogVisible: false })
  },

  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
  },

  validate() {
    const f = this.data.form
    if (!f.country) return '请输入国家'
    if (!f.province) return '请输入省份'
    if (!f.city) return '请输入城市'
    if (!f.county) return '请输入区县'
    if (!f.detail) return '请输入详细地址'
    return ''
  },

  async handleSave() {
    const err = this.validate()
    if (err) {
      msgError(err)
      return
    }
    this.setData({ saveLoading: true })
    try {
      let res
      if (this.data.isEdit && this.data.editId) {
        res = await userApi.updateAddress({ id: this.data.editId, ...this.data.form })
      } else {
        res = await userApi.addAddress(this.data.form)
      }
      if (res.code === 200) {
        msgSuccess(this.data.isEdit ? '更新成功' : '添加成功')
        this.setData({ dialogVisible: false })
        store.fetchDefaultAddress()
        this.fetchAddresses()
      } else {
        msgError(res.message || '保存失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    } finally {
      this.setData({ saveLoading: false })
    }
  },

  async handleSetDefault(e) {
    const row = this.data.addressList[e.currentTarget.dataset.index]
    if (!row) return
    try {
      const res = await userApi.setDefaultAddress(row.id)
      if (res.code === 200) {
        msgSuccess('已设为默认地址')
        store.fetchDefaultAddress()
        this.fetchAddresses()
      } else {
        msgError(res.message || '操作失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  async handleDelete(e) {
    const row = this.data.addressList[e.currentTarget.dataset.index]
    if (!row) return
    const ok = await confirm({
      title: '确认删除',
      message: '确定要删除该地址吗？删除后无法恢复。',
    })
    if (!ok) return
    try {
      const res = await userApi.deleteAddress(row.id)
      if (res.code === 200) {
        msgSuccess('删除成功')
        this.fetchAddresses()
      } else {
        msgError(res.message || '删除失败')
      }
    } catch (err) {
      msgError(getErrorMessage(err))
    }
  },

  noop() {},
})
