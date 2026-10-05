// scripts/style-probe.js — 关键布局/颜色探针（3 个代表页面）
const automator = require('miniprogram-automator')

;(async () => {
  const mp = await automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' })

  const probe = async (page, sel, props) => {
    const el = await page.$(sel)
    if (!el) return null
    const out = {}
    for (const p of props) out[p] = await el.style(p)
    return out
  }

  for (const p of ['pages/user/goods/goods', 'pages/user/info/info', 'pages/merchant/goods/goods']) {
    const page = await mp.reLaunch('/' + p)
    await page.waitFor(1500)
    console.log('===', p)
    console.log(' .page bg      :', JSON.stringify(await probe(page, '.page', ['background-color'])))
    console.log(' .page-title   :', JSON.stringify(await probe(page, '.page-title', ['color'])))
    console.log(' .search-input :', JSON.stringify(await probe(page, '.search-input', ['box-sizing', 'width', 'border-color'])))
    console.log(' .goods-card   :', JSON.stringify(await probe(page, '.goods-card', ['box-sizing', 'width', 'background-color'])))
    console.log(' .input        :', JSON.stringify(await probe(page, '.input', ['box-sizing', 'width'])))
    console.log(' .btn          :', JSON.stringify(await probe(page, '.btn', ['box-sizing'])))
  }

  await mp.disconnect()
  process.exit(0)
})().catch((e) => { console.error('FATAL', e.message || e); process.exit(1) })
