import { ElMessage } from 'element-plus'

const activeMessages = new Map<string, number>()

function dedup(key: string, fn: () => void, cooldown = 1500) {
  const now = Date.now()
  const last = activeMessages.get(key)
  if (last && now - last < cooldown) return
  activeMessages.set(key, now)
  fn()
  setTimeout(() => {
    if (activeMessages.get(key) === now) activeMessages.delete(key)
  }, cooldown)
}

export function msgSuccess(message: string) {
  dedup('success:' + message, () => ElMessage.success({ message, grouping: true }))
}

export function msgError(message: string) {
  dedup('error:' + message, () => ElMessage.error({ message, grouping: true }))
}

export function msgWarning(message: string) {
  dedup('warning:' + message, () => ElMessage.warning({ message, grouping: true }))
}

export function msgInfo(message: string) {
  dedup('info:' + message, () => ElMessage.info({ message, grouping: true }))
}