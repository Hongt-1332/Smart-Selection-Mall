const fs = require('fs')
const p = require('path')

const ROOT = 'C:/Users/KaiXin/IdeaProjects/Web/src/main/java'

function walk(dir, out) {
  let entries
  try { entries = fs.readdirSync(dir, { withFileTypes: true }) } catch (e) { return }
  for (const e of entries) {
    const f = p.join(dir, e.name)
    if (e.isDirectory()) walk(f, out)
    else if (f.endsWith('.java')) out.push(f)
  }
  return out
}

walk(ROOT, []).forEach((f) => {
  const s = fs.readFileSync(f, 'utf8')
  if (!/setPayTime|payTime|insertTrade|createTrade/.test(s)) return
  if (/import |package /.test(s.split('\n')[0]) === false) { /* noop */ }
  console.log('==== ' + f.replace(/\\/g, '/'))
  s.split('\n').forEach((l, i) => {
    if (/setPayTime|PayTime|insert\(|setCancelTime|setFinishTime|setCreateTime/.test(l)) {
      console.log((i + 1) + ': ' + l.trim())
    }
  })
})
