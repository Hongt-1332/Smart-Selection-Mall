// scripts/connect-test.js — 诊断自动化连接
const automator = require('miniprogram-automator')

;(async () => {
  console.log('尝试连接 ws://127.0.0.1:9420 ...')
  const timeout = new Promise((_, rej) => setTimeout(() => rej(new Error('连接超时(15s)')), 15000))
  try {
    const mp = await Promise.race([
      automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' }),
      timeout,
    ])
    console.log('✓ 连接成功')
    const sys = await mp.systemInfo()
    console.log('SDKVersion:', sys.SDKVersion, '| theme:', sys.theme)
    const page = await mp.currentPage()
    console.log('当前页面:', page && page.path)
    await mp.disconnect()
    process.exit(0)
  } catch (e) {
    console.error('✗ 连接失败:', e.message || e)
    process.exit(1)
  }
})()
