import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

window.alert = () => { console.warn('[blocked] window.alert') }
window.confirm = () => { console.warn('[blocked] window.confirm'); return false }
window.prompt = () => { console.warn('[blocked] window.prompt'); return null }

const app = createApp(App)

const pinia = createPinia()

// Pinia persistence plugin: auto-save stores to localStorage
pinia.use(({ store, options }) => {
  const opts = options as unknown as Record<string, unknown>
  const persistKey = opts.persist as string | undefined
  if (!persistKey) return

  const persistFields = opts.persistFields as string[] | undefined

  // Load from localStorage on init
  try {
    const saved = localStorage.getItem(persistKey)
    if (saved) {
      const parsed = JSON.parse(saved)
      if (persistFields) {
        // Only persist specified fields
        const partial: Record<string, unknown> = {}
        persistFields.forEach(field => {
          if (parsed[field] !== undefined) {
            partial[field] = parsed[field]
          }
        })
        store.$patch(partial as any)
      } else {
        // Persist all
        store.$patch(parsed as any)
      }
    }
  } catch { /* ignore */ }

  // Save to localStorage on every change
  store.$subscribe(() => {
    try {
      if (persistFields) {
        const partial: Record<string, unknown> = {}
        persistFields.forEach(field => {
          partial[field] = (store.$state as any)[field]
        })
        localStorage.setItem(persistKey, JSON.stringify(partial))
      } else {
        localStorage.setItem(persistKey, JSON.stringify(store.$state))
      }
    } catch { /* ignore */ }
  })
})

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')