/**
 * 快递相关接口（对应原项目 services/PostmanService.ts）
 */
const { http } = require('../request')

const postmanApi = {
  updateCurrentAddress: (params) => http.post('/postman/updateCurrentAddress', params),
}

module.exports = { postmanApi }
