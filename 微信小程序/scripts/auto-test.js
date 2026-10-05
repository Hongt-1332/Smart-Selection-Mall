// scripts/auto-test.js — 全页面自动化巡检
// 用法：node auto-test.js
// 产出：
//   scripts/report.json   每页的控制台错误 + 关键元素计算样式
//   scripts/shots/*.png   每页截图（真实渲染结果）
const path = require('path')
const fs = require('fs')
const automator = require('miniprogram-automator')

const PROJECT = 'c:/Users/KaiXin/WeChatProjects/ismartShop'
const CLI = 'C:/Program Files (x86)/Tencent/微信web开发者工具/cli.bat'
const OUT_DIR = path.join(__dirname, 'shots')
const REPORT = path.join(__dirname, 'report.json')

const PAGES = [
  'pages/index/index',
  'pages/login/login',
  'pages/about/about',
  'pages/ai-recommend/ai-recommend',
  'pages/background/background',
  'pages/user/goods/goods',
  'pages/user/cart/cart',
  'pages/user/address/address',
  'pages/user/trade/trade',
  'pages/user/info/info',
  'pages/user/bg/bg',
  'pages/user/vip/vip',
  'pages/user/merchant/merchant?userId=1',
  'pages/merchant/goods/goods',
  'pages/merchant/trade/trade',
  'pages/merchant/ai/ai',
]

;(async () => {
  fs.mkdirSync(OUT_DIR, { recursive: true })

  console.log('连接开发者工具自动化端口 ws://127.0.0.1:9420 ...')
  const miniProgram = await automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' })
  console.log('已连接')

  const logs = []
  miniProgram.on('console', (msg) => {
    const args = (msg.args || []).map((a) =>
      typeof a === 'object' ? JSON.stringify(a).slice(0, 300) : String(a).slice(0, 300)
    )
    logs.push({ type: msg.type, args })
  })
  miniProgram.on('exception', (err) => {
    logs.push({ type: 'exception', args: [err && (err.message || String(err))] })
  })

  const report = []
  for (const p of PAGES) {
    const name = p.split('?')[0].replace(/\//g, '_')
    const entry = { page: p, checks: {}, console: [], error: null }
    logs.length = 0
    try {
      const page = await miniProgram.reLaunch('/' + p)
      await page.waitFor(1200)

      // 关键计算样式探针
      const probe = async (sel, prop) => {
        try {
          const el = await page.$(sel)
          if (!el) return null
          return await el.style(prop)
        } catch (e) {
          return 'probeErr:' + String(e.message || e).slice(0, 80)
        }
      }
      entry.checks.pageHeight = await probe('.page', 'min-height')
      entry.checks.inputBoxSizing = await probe('.input', 'box-sizing')
      entry.checks.inputWidth = await probe('.input', 'width')
      entry.checks.cardBg = await probe('.card', 'background-color')
      entry.checks.btnBoxSizing = await probe('.btn', 'box-sizing')

      entry.console = logs.slice()
      await miniProgram.screenshot({ path: path.join(OUT_DIR, name + '.png') })
    } catch (e) {
      entry.error = String(e.message || e)
      entry.console = logs.slice()
    }
    report.push(entry)
    const errs = entry.console.filter((l) => l.type === 'error' || l.type === 'exception').length
    console.log(`✓ ${p}${entry.error ? ' [ERROR] ' + entry.error : ''} (console错误:${errs})`)
  }

  fs.writeFileSync(REPORT, JSON.stringify(report, null, 2))
  console.log('报告已写入', REPORT)

  // 断开但不关闭 IDE，便于继续人工查看
  try { await miniProgram.disconnect() } catch (e) { /* ignore */ }
  process.exit(0)
})().catch((e) => {
  console.error('FATAL:', e && (e.message || e))
  process.exit(1)
})
