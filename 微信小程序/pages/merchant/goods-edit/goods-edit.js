// pages/merchant/goods-edit/goods-edit.js — 商品发布 / 编辑（独立页面版）
//
// 背景：原先的「添加 / 编辑商品」是商户商品管理页里的一个底部弹层，
// 但弹层内的固定提交栏在小程序里始终无法稳定显示（受 flex 高度分配影响），
// 排查多轮未果，因此改为独立页面 —— 内容正常滚动，提交栏用 position: fixed 钉底，
// 不存在"看不见按钮"的可能。
//
// 用法：
//   新增 /pages/merchant/goods-edit/goods-edit
//   编辑 /pages/merchant/goods-edit/goods-edit?id=123
const { merchantApi } = require('../../../utils/api/merchant')
const { userApi } = require('../../../utils/api/user')
const { msgSuccess, msgError } = require('../../../utils/message')
const { getErrorMessage } = require('../../../utils/error')

/**
 * 校验失败的提示：绕开 msgError 的 1.5s 去重。
 *
 * utils/message.js 的 msgError 会按「文案 + 1.5 秒冷却」去重，
 * 用户反复点提交时，相同文案只弹一次，看起来像「点了没反应」。
 * 校验提示必须每次都弹，所以这里直接用 wx.showToast。
 */
function tipInvalid(message) {
  wx.showToast({ title: message, icon: 'none', duration: 2200, mask: false })
}

Page({
  data: {
    isEdit: false,
    editId: null,
    saveLoading: false,

    // 表单初值留空，避免出现「看起来已经填好但其实没填」的误导，
    // 也避免用户误把默认的 0.01 / 0 当成已设置的值直接提交。
    form: {
      goodsName: '',
      describe: '',
      goodsPrice: '',
      goodsStock: '',
      addressId: null,
    },

    /** 已选图片的本地临时路径 */
    selectedFile: '',

    /** 收货地址（发货地址） */
    addressList: [],
    addressLabels: [],
    addressIndex: -1,
    addrDropdownOpen: false,

    /** 表单完整性：还缺哪些项（用于页面上直接提示） */
    missingItems: [],
    canSubmit: false,
  },

  /**
   * 重新计算「还差哪些必填项」。
   * 每次表单变动 / 选地址 / 选图后都要调一次，
   * 让用户在页面上直接看到差什么，而不是点了提交才知道。
   */
  refreshChecklist() {
    const f = this.data.form
    const missing = []

    if (!String(f.goodsName || '').trim()) missing.push('商品名称')

    const priceRaw = String(f.goodsPrice == null ? '' : f.goodsPrice).trim()
    if (!priceRaw) missing.push('商品价格')
    else if (isNaN(Number(priceRaw)) || Number(priceRaw) <= 0) missing.push('商品价格（需大于 0）')

    const stockRaw = String(f.goodsStock == null ? '' : f.goodsStock).trim()
    if (!stockRaw) missing.push('商品库存')
    else if (isNaN(Number(stockRaw)) || Number(stockRaw) < 0) missing.push('商品库存（不能为负）')

    if (!this.data.isEdit) {
      if (!this.data.addressList.length) missing.push('收货地址（请先到用户中心添加）')
      else if (!f.addressId) missing.push('发货地址')
      if (!this.data.selectedFile) missing.push('商品图片')
    }

    this.setData({ missingItems: missing, canSubmit: missing.length === 0 })
  },

  onLoad(options) {
    const id = options && options.id
    if (id) {
      this.setData({ isEdit: true, editId: id })
      wx.setNavigationBarTitle({ title: '编辑商品' })
      this.fetchGoodsDetail(id)
    } else {
      wx.setNavigationBarTitle({ title: '发布商品' })
      this.fetchAddresses()
    }
    this.refreshChecklist()
  },

  /** 编辑态：拉取商品详情回填表单 */
  async fetchGoodsDetail(id) {
    try {
      const res = await merchantApi.getGoods(id)
      if (res.code === 200 && res.data) {
        const g = res.data
        this.setData({
          form: {
            goodsName: g.goodsName || '',
            describe: g.describe || '',
            goodsPrice: g.goodsPrice != null ? String(g.goodsPrice) : '',
            goodsStock: g.goodsStock != null ? String(g.goodsStock) : '',
            addressId: g.addressId || null,
          },
        })
        this.refreshChecklist()
      } else {
        msgError(res.message || '商品详情加载失败')
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    }
  },

  /**
   * 拉取收货地址作为「发货地址」候选。
   * 刻意不自动选中默认地址 —— 自动选中会让选择框看起来像只读字段，
   * 用户不知道还能改。
   */
  async fetchAddresses() {
    try {
      const res = await userApi.getAddressList({ page: 1, size: 100 })
      if (res.code === 200 && res.data) {
        const list = res.data.list || []
        // 默认地址排最前，作为优先选项
        list.sort((a, b) => (b.isDefault ? 1 : 0) - (a.isDefault ? 1 : 0))

        const addressLabels = list.map((a) => {
          const text = `${a.province || ''}${a.city || ''}${a.county || ''}${a.detail || ''}`
          return a.isDefault ? `${text}（默认）` : text
        })

        this.setData({ addressList: list, addressLabels })
        this.refreshChecklist()

        if (!list.length) {
          msgError('暂无收货地址，请先到「用户中心 - 收货地址」添加')
        }
      }
    } catch (e) {
      msgError(getErrorMessage(e))
    }
  },

  // ===== 表单输入 =====

  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
    this.refreshChecklist()
  },

  onNumberInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
    this.refreshChecklist()
  },

  // ===== 发货地址下拉 =====

  toggleAddrDropdown() {
    if (!this.data.addressList.length) {
      msgError('暂无收货地址，请先到「用户中心 - 收货地址」添加')
      return
    }
    this.setData({ addrDropdownOpen: !this.data.addrDropdownOpen })
  },

  /** 选中地址：写入表单并自动收起下拉 */
  onAddressSelect(e) {
    const idx = Number(e.currentTarget.dataset.index)
    const addr = this.data.addressList[idx]
    if (!addr) return
    this.setData({
      addressIndex: idx,
      'form.addressId': addr.id,
      addrDropdownOpen: false,
    })
    this.refreshChecklist()
  },

  // ===== 图片 =====

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success: (res) => {
        const file = res.tempFiles && res.tempFiles[0]
        if (!file) return
        this.setData({ selectedFile: file.tempFilePath })
        this.refreshChecklist()
      },
    })
  },

  /** 全屏预览已选图片（urls 必须是数组） */
  previewImage(e) {
    const src = (e.currentTarget.dataset && e.currentTarget.dataset.src) || this.data.selectedFile
    if (!src) return
    wx.previewImage({ current: src, urls: [src] })
  },

  clearImage() {
    this.setData({ selectedFile: '' })
    this.refreshChecklist()
  },

  // ===== 提交 =====

  /**
   * 提交前校验。
   *
   * 两个已知坑：
   * 1. Number('') === 0，所以「清空价格 / 库存」以前会静默按 0 提交，
   *    这里必须用「空串判断」而不是 isNaN。
   * 2. 发货地址是新增时的必填项，若地址列表压根没拉回来（未登录 / 无地址），
   *    用户会被永远卡住且提示语指向自己，因此这里把原因说清楚。
   */
  validate() {
    const f = this.data.form

    if (!String(f.goodsName || '').trim()) {
      tipInvalid('请输入商品名称')
      return false
    }

    // 价格：先用空串判断，再判断数值合法性
    const priceRaw = String(f.goodsPrice == null ? '' : f.goodsPrice).trim()
    if (!priceRaw) {
      tipInvalid('请输入商品价格')
      return false
    }
    const price = Number(priceRaw)
    if (isNaN(price) || price <= 0) {
      tipInvalid('商品价格必须大于 0')
      return false
    }

    // 库存：同样用空串判断（库存允许为 0）
    const stockRaw = String(f.goodsStock == null ? '' : f.goodsStock).trim()
    if (!stockRaw) {
      tipInvalid('请输入商品库存')
      return false
    }
    const stock = Number(stockRaw)
    if (isNaN(stock) || stock < 0) {
      tipInvalid('商品库存不能为负数')
      return false
    }

    // 新增才要求发货地址与图片（后端 updateGoods 不接受改这两项）
    if (!this.data.isEdit) {
      if (!this.data.addressList.length) {
        tipInvalid('没有可用的收货地址，请先到「用户中心 - 收货地址」添加')
        return false
      }
      if (!f.addressId) {
        tipInvalid('请选择发货地址')
        return false
      }
      if (!this.data.selectedFile) {
        tipInvalid('请选择商品图片')
        return false
      }
    }

    return true
  },

  async handleSave() {
    if (this.data.saveLoading) {
      console.warn('[goods-edit] 已在提交中，忽略重复点击')
      return
    }
    if (!this.validate()) {
      console.warn('[goods-edit] 校验未通过，表单数据:', JSON.stringify(this.data.form))
      return
    }

    const f = this.data.form
    this.setData({ saveLoading: true })
    // 提交过程可见：loading 转起来，用户知道点了有效
    wx.showLoading({ title: '提交中...', mask: true })
    try {
      let res
      if (this.data.isEdit) {
        // 编辑：后端 UpdateMerchantGoodsRequest 接受
        // id / goodsName / describe / goodsPrice / goodsStock / launch
        res = await merchantApi.updateGoods({
          id: this.data.editId,
          goodsName: f.goodsName,
          describe: f.describe,
          goodsPrice: Number(f.goodsPrice),
          goodsStock: Number(f.goodsStock),
        })
      } else {
        // 新增：后端 @Valid AddMerchantGoodsRequest 是表单绑定（无 @RequestPart），
        // 必须传对象，由 api 层拆成独立的 form-data 字段；
        // 打包成 JSON 字符串会导致绑定全空 → @Valid 400 → 后端方法体不执行。
        res = await merchantApi.addGoods({
          addressId: f.addressId,
          goodsName: f.goodsName,
          describe: f.describe,
          goodsPrice: Number(f.goodsPrice),
          goodsStock: Number(f.goodsStock),
        }, this.data.selectedFile)
      }

      if (res.code === 200) {
        msgSuccess(this.data.isEdit ? '修改成功' : '发布成功')
        // 返回列表页，并让它刷新
        const pages = getCurrentPages()
        const prev = pages[pages.length - 2]
        if (prev && typeof prev.fetchGoods === 'function') {
          prev.fetchGoods()
        }
        setTimeout(() => wx.navigateBack(), 800)
      } else {
        msgError(res.message || `提交失败(code=${res.code})`)
      }
    } catch (e) {
      // 把真实错误展示出来：域名不通 / 401 / 404 / 文件路径无效都在这里现形
      console.error('[goods-edit] 提交异常:', e)
      msgError(getErrorMessage(e))
    } finally {
      wx.hideLoading()
      this.setData({ saveLoading: false })
    }
  },
})