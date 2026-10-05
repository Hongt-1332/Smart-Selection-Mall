import { msgSuccess, msgWarning } from './message'
import { confirm } from './confirm'

/**
 * 数字输入过滤器 - 只允许输入数字和小数点
 * @param value 输入值
 * @param allowDecimal 是否允许小数点（默认true）
 * @returns 过滤后的值
 */
export function filterNumberInput(value: string, allowDecimal: boolean = true): string {
  if (!value) return ''

  let filtered = ''
  let hasDecimal = false

  for (const char of value) {
    if (char >= '0' && char <= '9') {
      filtered += char
    } else if (allowDecimal && char === '.' && !hasDecimal) {
      filtered += char
      hasDecimal = true
    }
    // 忽略其他字符
  }

  return filtered
}

/**
 * 价格格式化 - 确保价格格式正确
 * @param price 价格字符串
 * @returns 格式化后的价格字符串
 */
export function formatPrice(price: string): string {
  const num = parseFloat(price)
  if (isNaN(num) || num < 0) return '0.00'
  return Math.max(0.01, num).toFixed(2)
}

/**
 * 数量格式化 - 确保数量为正整数
 * @param qty 数量字符串
 * @returns 格式化后的数量字符串
 */
export function formatQuantity(qty: string): string {
  const num = parseInt(qty, 10)
  if (isNaN(num) || num < 0) return '0'
  return String(Math.max(0, num))
}

/**
 * 显示操作成功消息
 * @param message 成功消息内容
 */
export function showSuccess(message: string): void {
  msgSuccess(message)
}

/**
 * 显示搜索结果提示
 * @param total 结果总数
 * @param keyword 搜索关键词（可选）
 */
export function showSearchResult(total: number, keyword?: string): void {
  if (total > 0) {
    const msg = keyword ? `找到 ${total} 条关于「${keyword}」的结果` : `找到 ${total} 条结果`
    msgSuccess(msg)
  } else {
    const msg = keyword ? `未找到关于「${keyword}」的结果` : '未找到任何结果'
    msgWarning(msg)
  }
}

/**
 * 显示操作确认对话框
 * @param message 提示信息
 * @param title 标题（默认"确认操作"）
 * @param type 类型（默认"warning"）
 * @returns Promise<boolean> 用户是否确认
 */
export async function showConfirm(
  message: string,
  title: string = '确认操作',
  type: 'warning' | 'info' | 'success' | 'error' = 'warning'
): Promise<boolean> {
  return confirm({ title, message, type })
}

/**
 * 防抖函数
 * @param fn 要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(fn: T, delay: number = 300): T {
  let timer: ReturnType<typeof setTimeout> | null = null
  return ((...args: any[]) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }) as T
}