export function getErrorMessage(e: unknown): string {
  if (e instanceof Error) {
    const msg = e.message.toLowerCase()

    if (msg.includes('network') || msg.includes('fetch') || msg.includes('failed to fetch')) {
      return '服务器暂时不可用，请稍后重试'
    }

    if (msg.includes('timeout') || msg.includes('timed out') || msg.includes('abort')) {
      return '请求超时，请稍后重试'
    }

    if (msg.includes('404') || msg.includes('not found')) {
      return '请求的资源不存在（404）'
    }

    if (msg.includes('token') || msg.includes('过期') || msg.includes('登录') || msg.includes('请先') || msg.includes('401') || msg.includes('unauthorized')) {
      return '请登录'
    }

    if (msg.includes('403') || msg.includes('forbidden')) {
      return '无权限访问该资源（403）'
    }

    if (msg.includes('500') || msg.includes('internal server error')) {
      return '服务器内部错误，请联系管理员（500）'
    }

    if (msg.includes('502') || msg.includes('503') || msg.includes('504')) {
      return '服务暂时不可用，请稍后重试'
    }

    if (msg.includes('cors') || msg.includes('cross-origin')) {
      return '跨域请求被拒绝，请检查服务器配置'
    }

    if (msg.includes('connection refused') || msg.includes('enetunreach') || msg.includes('econnrefused')) {
      return '服务器暂时不可用，请稍后重试'
    }

    return e.message
  }

  if (typeof e === 'string' && e.trim()) {
    return e
  }

  return '未知错误，请稍后重试'
}