import { ref } from 'vue'

export interface ConfirmOptions {
  title?: string
  message: string
  type?: 'success' | 'warning' | 'info' | 'error'
  confirmText?: string
  cancelText?: string
}

const visible = ref(false)
const options = ref<ConfirmOptions>({ message: '' })
let _resolve: ((value: boolean) => void) | null = null

export function useConfirmState() {
  return { visible, options }
}

export function confirm(opts: ConfirmOptions): Promise<boolean> {
  options.value = { type: 'warning', confirmText: '确定', cancelText: '取消', ...opts }
  visible.value = true
  return new Promise((resolve) => {
    _resolve = resolve
  })
}

export function handleConfirmOk() {
  visible.value = false
  _resolve?.(true)
  _resolve = null
}

export function handleConfirmCancel() {
  visible.value = false
  _resolve?.(false)
  _resolve = null
}