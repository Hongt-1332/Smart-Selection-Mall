// scripts/verify-index.js — 首页二次验证：等 app 启动完成后再进首页
const automator = require('miniprogram-automator')

;(async () => {
  const mp = await automator.connect({ wsEndpoint: 'ws://127.0.0.1:9420' })
  console.log('connected')

  const errors = []
  mp.on('console', (m) => { if (m.type === 'error') errors.push((m.args || []).join(' ').slice(0, 200)) })
  mp.on('exception', (e) => errors.push('EXC: ' + (e.message || e).slice(0, 200)))

  const page = await mp.reLaunch('/pages/index/index')
  await page.waitFor(2500)

  const d = page.data || {}
  console.log('data.bannerImgs.length =', (d.bannerImgs || []).length)
  console.log('data.goodsList.length  =', (d.goodsList || []).length)
  console.log('data.entries.length    =', (d.entries || []).length)
  if ((d.goodsList || [])[0]) {
    const g = d.goodsList[0]
    console.log('goods[0].goodsName =', g.goodsName)
    console.log('goods[0].imagePath 前50字符 =', String(g.imagePath || '').slice(0, 50))
    console.log('goods[0].merchantAvatar 有值 =', !!g.merchantAvatar)
  }

  const cards = await page.$$('.goods-card')
  console.log('渲染出的 .goods-card 数量 =', cards.length)
  const imgs = await page.$$('.goods-img')
  console.log('渲染出的 .goods-img 数量 =', imgs.length)
  if (imgs[0]) {
    console.log('第一张商品图 src 前60 =', String(await imgs[0].attribute('src')).slice(0, 60))
  }

  await new Promise((r) => setTimeout(r, 800))
  console.log('--- console errors during this run:', errors.length)
  errors.slice(0, 5).forEach((e) => console.log('  ', e))

  try {
    await mp.screenshot({ path: 'c:/Users/KaiXin/WeChatProjects/ismartShop/scripts/shots/index-verify.png' })
    console.log('screenshot ok')
  } catch (e) {
    console.log('screenshot failed:', String(e.message || e).slice(0, 120))
  }

  await mp.disconnect()
  process.exit(0)
})().catch((e) => { console.error('FATAL', e.message || e); process.exit(1) })
