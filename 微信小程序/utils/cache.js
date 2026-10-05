/**
 * 本地缓存工具
 *
 * 用途：接口请求失败或返回空数据时，用上次成功拉取的数据兜底渲染，
 * 避免页面出现「空白 / 信息缺失」的异常状态。
 *
 * 设计要点：
 * - 只缓存最近一次成功的数据，控制体积（storage 单 key 上限 1MB）
 * - 读取失败静默返回 null，不抛异常，保证页面健壮
 * - 带时间戳，便于判断缓存新鲜度
 */

const PREFIX = 'cache_'

/** 各缓存键的容量上限（条数），避免超出 storage 限制 */
const LIMITS = {
  goods: 60,
  indexGoods: 20,
}

function keyOf(name) {
  return PREFIX + name
}

/**
 * 写入缓存
 * @param {string} name 缓存名（如 'goods'）
 * @param {*} data 数据
 */
function setCache(name, data) {
  try {
    const limit = LIMITS[name] || 50
    let payload = data
    if (Array.isArray(data) && data.length > limit) {
      payload = data.slice(0, limit)
    }
    wx.setStorageSync(keyOf(name), {
      t: Date.now(),
      d: payload,
    })
  } catch (e) {
    // 存储失败（超限等）静默忽略，不影响主流程
  }
}

/**
 * 读取缓存
 * @param {string} name 缓存名
 * @param {number} [maxAge] 最大有效期（毫秒），不传则不过期
 * @returns {*} 缓存数据，无缓存返回 null
 */
function getCache(name, maxAge) {
  try {
    const raw = wx.getStorageSync(keyOf(name))
    if (!raw || typeof raw !== 'object') return null
    if (maxAge && raw.t && Date.now() - raw.t > maxAge) return null
    return raw.d === undefined ? null : raw.d
  } catch (e) {
    return null
  }
}

/** 清除指定缓存 */
function clearCache(name) {
  try {
    wx.removeStorageSync(keyOf(name))
  } catch (e) {
    // ignore
  }
}

module.exports = { setCache, getCache, clearCache }
