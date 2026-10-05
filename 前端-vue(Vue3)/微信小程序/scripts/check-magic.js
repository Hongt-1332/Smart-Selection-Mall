const fs = require('fs')
const p = require('path')

const dir = 'C:/Users/KaiXin/IdeaProjects/Web/src/main/resources/vip/homeimage'
fs.readdirSync(dir).forEach((f) => {
  const full = p.join(dir, f)
  if (!fs.statSync(full).isFile()) return
  const buf = fs.readFileSync(full)
  const head = buf.slice(0, 16)
  let type = '未知'
  if (head.slice(0, 4).toString() === 'RIFF' && buf.slice(8, 12).toString() === 'WEBP') type = '真 webp (RIFF/WEBP)'
  else if (head[0] === 0x89 && head[1] === 0x50) type = '实际是 PNG'
  else if (head[0] === 0xff && head[1] === 0xd8) type = '实际是 JPEG'
  else if (head.slice(0, 3).toString() === 'GIF') type = '实际是 GIF'
  console.log(f, '|', (buf.length / 1024).toFixed(1) + ' KB', '|', type, '| 魔数:', head.toString('hex').slice(0, 24))
})
