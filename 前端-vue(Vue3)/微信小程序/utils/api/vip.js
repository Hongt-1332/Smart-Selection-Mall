/**
 * VIP 相关接口（对应原项目 services/VipService.ts）
 *
 * 说明：图片字段的 data URI 归一化已在 utils/request.js 的 handleResponse 中统一处理，
 * 这里直接返回后端响应即可。
 */
const { http } = require('../request')

const vipApi = {
  getVipGoods: () => http.get('/vip/goods'),
  getVipImages: () => http.get('/vip/image'),
  getHomeImages: () => http.get('/vip/homeImage'),
  getLoginImages: () => http.get('/vip/loginImage'),
  getVipConfig: () => http.get('/vip/config'),
  getVipLevel: () => http.get('/vip/level'),
  /**
   * 购买 VIP：后端 VipBuyRequest 有三个必填字段 level / money / num，
   * num 缺失会被 @NotNull 拦下（400「数量不能为空」），这里默认传 1。
   */
  buyVip: (level, money, num) => http.post('/vip/buy', { level, money, num: num || 1 }),
}

module.exports = { vipApi }
