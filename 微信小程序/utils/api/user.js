/**
 * 用户相关接口（对应原项目 services/UserService.ts）
 *
 * 说明：图片字段的 data URI 归一化已在 utils/request.js 的 handleResponse 中统一处理，
 * 此处无需再单独转换。
 */
const { http } = require('../request')

/**
 * 批量获取图片：后端返回 [{ name, data }]，
 * 这里把数组转成 { name: dataUri } 映射，方便按文件名取图。
 */
async function getImages(paths) {
  const res = await http.post('/image/getImages', paths || [])
  if (res && res.code === 200 && Array.isArray(res.data)) {
    const map = {}
    res.data.forEach((item) => {
      if (item && item.name) map[item.name] = item.data
    })
    return { code: res.code, message: res.message, data: map }
  }
  return res
}

const userApi = {
  login: (params) => http.post('/user/login', params),
  /** 微信一键登录：后端 /user/wechatLogin，入参 WechatLoginRequest */
  wechatLogin: (params) => http.post('/user/wechatLogin', params),
  register: (params) => http.post('/user/register', params),
  logout: () => http.post('/user/logout'),

  getUserInfoVO: () => http.post('/page/user/user'),
  getUserInfoById: () => http.get('/user/info'),
  update: (params) => http.put('/user/update', params),
  updateDescribe: (describe) => http.put('/user/updateDescribe?describe=' + encodeURIComponent(describe), null),
  setDefaultAddress: (addressId) => http.put('/user/setDefaultAddress?addressId=' + addressId, null),

  getUserInfo: () => http.post('/page/user/user'),
  /** 收货地址列表（分页接口，兼容原有分页调用） */
  getAddressList: (params) => http.post('/page/user/address', params),
  /** 全部收货地址（后端专用不分页接口，下单弹窗等场景更合适） */
  getAddressShow: () => http.get('/user/address/show'),
  getCartList: (params) => http.post('/page/user/cart', params),
  getGoodsList: (params) => http.post('/page/user/goods', params),
  /**
   * 商品详情：后端未提供单独的详情接口，
   * 详情页统一用「列表接口 + 本地缓存」按 id 查找（见 goods-detail.js）。
   */
  getTradeList: (params) => http.post('/page/user/trade', params),

  addAddress: (params) => http.post('/user/address/add', params),
  updateAddress: (params) => http.put('/user/address/update', params),
  deleteAddress: (id) => http.del('/user/address/delete?id=' + id, null),

  handleCart: (params) => http.post('/user/cart', params),
  getCartCount: () => http.get('/user/cart/count'),
  buyGoods: (params) => http.post('/user/goods/buy', params),
  buyFromCart: (cartIds) => http.post('/user/cart/buy', cartIds),
  cancelTrade: (id) => http.put('/user/trade/cancel?id=' + id, null),

  addBackground: (params) => http.post('/user/background/add', params),
  /** 后端是 @PutMapping("/update") */
  updateBackground: (params) => http.put('/user/background/update', params),
  /** 背景付费（后端 POST /user/background/pay） */
  payBackground: (params) => http.post('/user/background/pay', params),
  getBackgroundList: (params) => http.post('/page/user/background', params),

  getCaptcha: () => http.get('/tool/captcha'),

  getMerchantBackground: (userId) => http.get('/page/merchant/merchantBackground?id=' + userId),
  getUserBackground: () => http.post('/page/user/userBackground'),

  updateImage: (filePath) => http.upload('/user/updateImage', filePath),
  uploadImage: (filePath) => http.upload('/tool/image', filePath),

  getImages,
}

module.exports = { userApi }
