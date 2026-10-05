/**
 * 错误信息转换（对应原项目 utils/error.ts）
 */
function getErrorMessage(e) {
  if (e instanceof Error) {
    const msg = (e.message || '').toLowerCase()

    if (msg.indexOf('network') >= 0 || msg.indexOf('fetch') >= 0 || msg.indexOf('request:fail') >= 0) {
      return '服务器暂时不可用，请稍后重试'
    }
    if (msg.indexOf('timeout') >= 0 || msg.indexOf('timed out') >= 0 || msg.indexOf('abort') >= 0) {
      return '请求超时，请稍后重试'
    }
    if (msg.indexOf('404') >= 0 || msg.indexOf('not found') >= 0) {
      return '请求的资源不存在（404）'
    }
    if (
      msg.indexOf('token') >= 0 ||
      msg.indexOf('过期') >= 0 ||
      msg.indexOf('登录') >= 0 ||
      msg.indexOf('请先') >= 0 ||
      msg.indexOf('401') >= 0 ||
      msg.indexOf('unauthorized') >= 0
    ) {
      return '请登录'
    }
    if (msg.indexOf('403') >= 0 || msg.indexOf('forbidden') >= 0) {
      return '无权限访问该资源（403）'
    }
    if (msg.indexOf('500') >= 0 || msg.indexOf('internal server error') >= 0) {
      return '服务器内部错误，请联系管理员（500）'
    }
    if (msg.indexOf('502') >= 0 || msg.indexOf('503') >= 0 || msg.indexOf('504') >= 0) {
      return '服务暂时不可用，请稍后重试'
    }
    if (msg.indexOf('cors') >= 0 || msg.indexOf('cross-origin') >= 0) {
      return '跨域请求被拒绝，请检查服务器配置'
    }
    if (msg.indexOf('connection refused') >= 0 || msg.indexOf('econnrefused') >= 0) {
      return '服务器暂时不可用，请稍后重试'
    }
    return e.message
  }

  if (typeof e === 'string' && e.trim()) {
    return e
  }

  return '未知错误，请稍后重试'
}

module.exports = { getErrorMessage }
