import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    allowedHosts: ['frp-use.com', 'localhost'],
    proxy: {
      '/page': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/user': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/manager': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/merchant': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/tool': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/ai': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/vip': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
      '/image': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' },
      },
    },
  },
})