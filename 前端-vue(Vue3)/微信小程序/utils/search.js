/**
 * 搜索参数构建（对应原项目 utils/search.ts）
 */
function buildSearchParams(search, pageNum, pageSize, stringFields) {
  const params = {
    page: pageNum || 1,
    size: pageSize || 10,
  }
  Object.keys(search || {}).forEach((k) => {
    const v = search[k]
    if (v !== '' && v !== null && v !== undefined) {
      if (stringFields && stringFields.indexOf(k) >= 0) {
        params[k] = String(v)
      } else {
        params[k] = typeof v === 'string' ? v.trim() : v
      }
    }
  })
  return params
}

function buildTimeRangeParams(range, startKey, endKey) {
  if (range && range.length === 2) {
    const params = {}
    params[startKey] = range[0] + 'T00:00:00'
    params[endKey] = range[1] + 'T23:59:59'
    return params
  }
  return {}
}

module.exports = { buildSearchParams, buildTimeRangeParams }
