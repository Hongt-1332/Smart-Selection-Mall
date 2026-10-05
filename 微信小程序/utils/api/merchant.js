/**
 * 商户相关接口（对应原项目 services/MerchantService.ts）
 */
const { http } = require('../request')

const merchantApi = {
  /**
   * 新增商品：后端签名是
   *   POST /merchant/goods/add  (@Valid AddMerchantGoodsRequest, @RequestParam file)
   *
   * 关键：AddMerchantGoodsRequest 前面没有 @RequestPart / @RequestBody，
   * 走的是 Spring 的 @ModelAttribute 表单绑定 ——
   * 必须把每个字段作为独立的 form-data 字段发送（goodsName=xxx&describe=yyy...），
   * 不能打包成一个 JSON 字符串字段。之前发 { goods: '<json>' } 会导致
   * 绑定结果全空 → @Valid 失败 → 400，且 Controller 方法体不执行（后端无日志）。
   */
  addGoods: (goods, filePath) => http.upload('/merchant/goods/add', filePath, {
    goodsName: goods.goodsName,
    describe: goods.describe,
    goodsPrice: goods.goodsPrice,
    goodsStock: goods.goodsStock,
    addressId: goods.addressId,
  }),

  /**
   * 编辑商品：后端 UpdateMerchantGoodsRequest 支持
   * id / goodsName / describe / goodsPrice / goodsStock / launch 一次提交。
   * 说明：后端没有单独的 updateImage / updateDescribe / updateLaunch 接口，
   * 图片更新也走本接口（只需带上新的表单字段）。
   */
  updateGoods: (params) => http.put('/merchant/goods/update', params),

  /** 上下架：复用 updateGoods，只提交 id + launch */
  updateLaunch: (id, launch) => http.put('/merchant/goods/update', { id, launch: launch === 1 || launch === true }),

  /** 删除商品：后端 DELETE /merchant/goods/delete?id=，仅允许删除自己的商品 */
  deleteGoods: (id) => http.del('/merchant/goods/delete?id=' + id, null),

  /** 商品详情 */
  getGoods: (id) => http.get('/merchant/goods/get?id=' + id),
  /** 商品总数 */
  getGoodsCount: () => http.get('/merchant/goods/count'),

  /**
   * 订单操作：后端为 deliver（发货）/ confirm（确认）/ delete（删除）。
   */
  deliverTrade: (id) => http.put('/merchant/trade/deliver?id=' + id, null),
  confirmTrade: (id) => http.put('/merchant/trade/confirm?id=' + id, null),
  deleteTrade: (id) => http.del('/merchant/trade/delete?id=' + id, null),
  getTrade: (id) => http.get('/merchant/trade/get?id=' + id),

  getGoodsList: (params) => http.post('/page/merchant/goods', params),
  getTradeList: (params) => http.post('/page/merchant/trade', params),

  addAi: (params) => http.post('/merchant/ai/add', params),
  /** 后端是 @PutMapping("/update")，不能用 POST */
  updateAi: (params) => http.put('/merchant/ai/update', params),
  deleteAi: (id) => http.del('/merchant/ai/delete?id=' + id, null),

  getAiList: (params) => http.post('/page/merchant/ai', params),

  getUserList: (params) => http.post('/page/merchant/user', params),
  getBackgroundList: (params) => http.post('/page/merchant/background', params),
}

module.exports = { merchantApi }
