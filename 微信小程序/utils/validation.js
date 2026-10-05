/**
 * 输入校验与格式化（对应原项目 utils/validation.ts）
 */
const { msgSuccess, msgWarning } = require('./message')

function filterNumberInput(value, allowDecimal = true) {
  if (!value) return ''
  let filtered = ''
  let hasDecimal = false
  const str = String(value)
  for (let i = 0; i < str.length; i++) {
    const char = str[i]
    if (char >= '0' && char <= '9') {
      filtered += char
    } else if (allowDecimal && char === '.' && !hasDecimal) {
      filtered += char
      hasDecimal = true
    }
  }
  return filtered
}

function formatPrice(price) {
  const num = parseFloat(price)
  if (isNaN(num) || num < 0) return '0.00'
  return Math.max(0.01, num).toFixed(2)
}

function formatQuantity(qty) {
  const num = parseInt(qty, 10)
  if (isNaN(num) || num < 0) return '0'
  return String(Math.max(0, num))
}

function showSuccess(message) {
  msgSuccess(message)
}

function showSearchResult(total, keyword) {
  if (total > 0) {
    const msg = keyword ? `找到 ${total} 条关于「${keyword}」的结果` : `找到 ${total} 条结果`
    msgSuccess(msg)
  } else {
    const msg = keyword ? `未找到关于「${keyword}」的结果` : '未找到任何结果'
    msgWarning(msg)
  }
}

module.exports = {
  filterNumberInput,
  formatPrice,
  formatQuantity,
  showSuccess,
  showSearchResult,
}
