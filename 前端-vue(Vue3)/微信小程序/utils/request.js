/**
 * 网络请求封装（对应原项目 services/BaseApi.ts）
 * 后端地址通过 config.js 的 BASE_URL 统一配置
 */

const { BASE_URL } = require('./config')
const { normalizeImageFields } = require('./format')

const TOKEN_KEY = 'auth_token'

/**
 * 统一处理响应体：把其中的图片字段（裸 base64）补全为 data URI。
 * 这样所有页面拿到的 imagePath / avatarPath / data 都能被 <image> 直接渲染，
 * 不用每个接口单独处理。
 */
function handleResponse(body) {
  if (!body || typeof body !== 'object') return body
  return normalizeImageFields(body)
}

function getStoredToken() {
  try {
    return wx.getStorageSync(TOKEN_KEY) || null
  } catch (e) {
    return null
  }
}

function setStoredToken(token) {
  try {
    wx.setStorageSync(TOKEN_KEY, token)
  } catch (e) {
    console.warn('[Token] failed to save token')
  }
}

function removeStoredToken() {
  try {
    wx.removeStorageSync(TOKEN_KEY)
  } catch (e) {
    // ignore
  }
}

/**
 * 通用请求
 * @param {string} url 接口路径，如 /user/login
 * @param {object} options { method, data, timeout, header }
 * @returns {Promise<object>} 后端统一响应 { code, message, data }
 */
function request(url, options = {}) {
  const { method = 'GET', data, timeout = 10000, header = {} } = options
  const token = getStoredToken()

  const finalHeader = {
    'Content-Type': 'application/json',
    ...header,
  }
  if (token) {
    finalHeader.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      timeout,
      header: finalHeader,
      success(res) {
        const status = res.statusCode
        if (status === 401 || status === 403) {
          // 与原项目一致：鉴权失败也把响应体交给调用方判断 code
          resolve(handleResponse(res.data || { code: status, message: '未登录', data: null }))
          return
        }
        if (status >= 200 && status < 300) {
          resolve(handleResponse(res.data))
        } else {
          const msg = (res.data && res.data.message) || `请求失败(${status})`
          reject(new Error(msg))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络请求失败'))
      },
    })
  })
}

const http = {
  get(url, params) {
    let query = ''
    if (params && typeof params === 'object') {
      const parts = Object.keys(params)
        .filter((k) => params[k] !== undefined && params[k] !== null && params[k] !== '')
        .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
      if (parts.length) query = '?' + parts.join('&')
    }
    return request(url + query, { method: 'GET' })
  },
  post(url, data) {
    return request(url, { method: 'POST', data })
  },
  put(url, data) {
    return request(url, { method: 'PUT', data })
  },
  del(url, data) {
    return request(url, { method: 'DELETE', data })
  },
  /**
   * 上传文件（单个 file），对应原项目 upload/putUpload
   *
   * 注意：wx.uploadFile 的 success 回调在 4xx / 5xx 时同样会触发
   * （它只代表「HTTP 事务完成」，不代表业务成功），
   * 因此必须显式检查 statusCode，否则后端报 401/404/500 时
   * 错误会被静默吞掉，表现为「什么都没发生」。
   */
  upload(url, filePath, formData = {}, method = 'POST') {
    const token = getStoredToken()
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: BASE_URL + url,
        filePath,
        name: 'file',
        formData,
        header: token ? { Authorization: `Bearer ${token}` } : {},
        success(res) {
          let body = null
          try {
            body = JSON.parse(res.data)
          } catch (e) {
            body = null
          }
          if (res.statusCode >= 200 && res.statusCode < 300 && body) {
            resolve(handleResponse(body))
          } else {
            const msg = (body && body.message) || `上传失败(HTTP ${res.statusCode})`
            reject(new Error(msg))
          }
        },
        fail(err) {
          // 网络层失败：域名不通 / 未勾选「不校验合法域名」/ 文件路径无效等
          console.error('[upload] fail:', err.errMsg)
          reject(new Error(err.errMsg || '上传失败'))
        },
      })
      // 注意：wx.uploadFile 的 method 固定为 POST，
      // PUT 上传通过后端兼容或使用 POST 接口实现
      void method
    })
  },
}

module.exports = {
  http,
  request,
  getStoredToken,
  setStoredToken,
  removeStoredToken,
}