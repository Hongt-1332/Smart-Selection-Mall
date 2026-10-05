/**
 * 通用格式化工具
 */

function pad(n) {
  n = Number(n)
  return n < 10 ? '0' + n : '' + n
}

/** 金额保留两位小数，例如 12 -> "12.00" */
function money(val) {
  const n = Number(val)
  if (isNaN(n)) return '0.00'
  return n.toFixed(2)
}

/** 时间字符串转 "YYYY-MM-DD" */
function dateOnly(str) {
  if (!str) return ''
  return String(str).replace('T', ' ').slice(0, 10)
}

/** 时间字符串转 "YYYY-MM-DD HH:mm" */
function dateTime(str) {
  if (!str) return ''
  return String(str).replace('T', ' ').slice(0, 16)
}

/** HH:mm */
function currentTimeStr() {
  const now = new Date()
  return `${pad(now.getHours())}:${pad(now.getMinutes())}`
}

/**
 * 图片字段名匹配规则。
 * 后端返回的图片可能是「裸 base64」，必须补 data URI 前缀，否则 <image> 不渲染。
 * 这里列出所有承载图片的字段名，用于响应体的递归归一化。
 */
const IMAGE_FIELD_RE = /^(data|url|avatarPath|avatarUrl|imagePath|imageUrl|backgroundPath|backgroundUrl|goodsImage|bannerPath|bgPath|path)$/i

/**
 * 归一化单个图片地址，保证 <image src> 可用。
 *
 * 兼容以下几种后端返回形式：
 *  - "data:image/png;base64,xxx"  已是 data URI → 原样返回
 *  - "http(s)://..."              网络图片     → 原样返回
 *  - "wxfile://..." / "file://"   本地临时文件 → 原样返回
 *  - "/static/a.png"              服务器相对路径 → 原样返回（需后端配域名）
 *  - "iVBORw0KGgo..."             裸 base64    → 补 "data:image/png;base64,"
 */
function normalizeImage(src) {
  if (!src || typeof src !== 'string') return ''
  const str = src.trim()
  if (!str) return ''
  // 已是 data URI / 网络地址 / 本地临时文件 / 绝对或相对路径
  if (/^(data:|https?:|wxfile:|file:|\/)/i.test(str)) return str
  // 带图片扩展名的路径（如 goods1.png），交给 image 组件按相对路径加载
  if (/\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(str)) return str
  // 裸 base64：只含 base64 字符集且长度可观
  if (str.length > 100 && /^[A-Za-z0-9+/=\s]+$/.test(str)) {
    const body = str.replace(/\s/g, '')
    // 按 magic number 推断图片类型，避免统一当 PNG 导致部分图不显示
    if (body.indexOf('iVBORw0KGgo') === 0) return 'data:image/png;base64,' + body
    if (body.indexOf('/9j/') === 0) return 'data:image/jpeg;base64,' + body
    if (body.indexOf('R0lGOD') === 0) return 'data:image/gif;base64,' + body
    if (body.indexOf('UklGR') === 0) return 'data:image/webp;base64,' + body
    return 'data:image/png;base64,' + body
  }
  return str
}

/**
 * 递归归一化响应体中的图片字段。
 * 会识别常见图片字段名（imagePath / avatarPath / data / url 等），
 * 对手动维护的白名单字段统一调用 normalizeImage，
 * 避免漏掉某个页面导致图片不显示。
 *
 * @param {*} input 任意响应数据
 * @param {number} depth 递归深度保护
 * @returns {*} 处理后的数据（原对象结构不变）
 */
function normalizeImageFields(input, depth = 0) {
  if (depth > 6 || input === null || input === undefined) return input

  if (Array.isArray(input)) {
    return input.map((item) => normalizeImageFields(item, depth + 1))
  }

  if (typeof input !== 'object') return input

  const out = {}
  Object.keys(input).forEach((key) => {
    const val = input[key]
    if (typeof val === 'string' && IMAGE_FIELD_RE.test(key)) {
      out[key] = normalizeImage(val)
    } else if (val && typeof val === 'object') {
      out[key] = normalizeImageFields(val, depth + 1)
    } else {
      out[key] = val
    }
  })
  return out
}

module.exports = {
  pad,
  money,
  dateOnly,
  dateTime,
  currentTimeStr,
  normalizeImage,
  normalizeImageFields,
}
