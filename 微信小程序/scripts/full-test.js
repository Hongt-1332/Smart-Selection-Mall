// scripts/full-test.js — 全页面巡检 v2
// 修正：page.data() 是方法；截图改用 base64 返回值；增加 DOM 结构探针
const fs = require('fs')
const path = require('path')
const automator = require('miniprogram-automator')

const OUT_DIR = path.join(__dirname, 'shots')
const REPORT = path.join(__dirname, 'report2.json')

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
  const mp = await automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' })
  console.log('connected')

  const errors = []
  mp.on('console', (m) => { if (m.type === 'error') errors.push((m.args || []).join(' ').slice(0, 200)) })
  mp.on('exception', (e) => errors.push('EXC: ' + (e.message || e).slice(0, 200)))

  const report = []
  for (const p of PAGES) {
    const name = p.split('?')[0].replace(/\//g, '_')
    const entry = { page: p }
    errors.length = 0
    try {
      const page = await mp.reLaunch('/' + p)
      await page.waitFor(1500)

      // 页面数据快照
      try {
        const d = (await page.data()) || {}
        entry.dataKeys = Object.keys(d).slice(0, 25)
        const listKey = ['goodsList', 'cartList', 'trades', 'addressList', 'messages', 'plans', 'bgs', 'vipPlans', 'configList', 'userList']
          .find((k) => Array.isArray(d[k]))
        entry.mainListLen = listKey ? d[listKey].length : null
      } catch (e) { entry.dataErr = String(e.message || e).slice(0, 100) }

      // DOM 结构探针
      try {
        const root = await page.$('view')
        entry.rootClass = root ? await root.attribute('class') : null
        entry.cardCount = (await page.$$('.card')).length
        entry.btnCount = (await page.$$('.btn')).length
        entry.imgCount = (await page.$$('image')).length
        const card = await page.$('.card')
        if (card) {
          entry.cardBg = await card.style('background-color')
          entry.cardColor = await card.style('color')
        }
      } catch (e) { entry.domErr = String(e.message || e).slice(0, 100) }

      // 截图（base64 方案）
      try {
        const b64 = await mp.screenshot()
        if (b64 && b64.length > 1000) {
          fs.writeFileSync(path.join(OUT_DIR, name + '.png'), Buffer.from(b64, 'base64'))
          entry.shot = b64.length
        } else {
          entry.shot = 'empty:' + (b64 ? b64.length : 0)
        }
      } catch (e) { entry.shot = 'err:' + String(e.message || e).slice(0, 80) }

      entry.consoleErrors = errors.slice(0, 5)
    } catch (e) {
      entry.fatal = String(e.message || e).slice(0, 200)
    }
    report.push(entry)
    console.log(
      '✓', p,
      '| card:', entry.cardCount, '| img:', entry.imgCount,
      '| list:', entry.mainListLen,
      '| cardBg:', entry.cardBg,
      '| err:', (entry.consoleErrors || []).length,
      entry.fatal ? '| FATAL ' + entry.fatal : ''
    )
  }

  fs.writeFileSync(REPORT, JSON.stringify(report, null, 2))
  console.log('done ->', REPORT)
  await mp.disconnect()
  process.exit(0)
})().catch((e) => { console.error('FATAL', e.message || e); process.exit(1) })
