/**
 * 全局配置
 * 后端地址通过环境变量 WX_BASE_URL 管理（由 scripts/apply-env.mjs 从 .env 生成）
 * - 本地开发：.env 中 WX_BASE_URL=http://127.0.0.1:8081（本机后端无 TLS 证书，用 http）
 * - 生产环境：.env 中 WX_BASE_URL=https://你的域名（启用 https + 域名白名单）
 * 开发环境请在微信开发者工具中勾选「不校验合法域名」，
 * 或在小程序后台把 BASE_URL 的域名加入 request/uploadFile 白名单。
 */
let baseURL = 'http://127.0.0.1:8081' // 本地开发默认值
try {
  // eslint-disable-next-line
  const { WX_BASE_URL } = require('./env.generated.js')
  if (WX_BASE_URL) baseURL = WX_BASE_URL
} catch (e) {
  // env.generated.js 尚未生成时，使用上面的本地默认值
}

const BASE_URL = baseURL

module.exports = {
  BASE_URL,
}