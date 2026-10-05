/**
 * 从 .env 读取配置并写入：
 *  1. WX_APPID     → project.private.config.json（真实 AppID，永不入库）
 *  2. WX_BASE_URL  → utils/env.generated.js（供运行时 config.js 读取）
 *
 * 背景：
 *  - project.private.config.json 是"私有、优先级最高"的覆盖配置，微信开发者
 *    工具会用它覆盖 project.config.json 中相同字段（.gitignore 已忽略）。
 *  - utils/env.generated.js 同理，运行时读取，不在版本库中。
 * 这样 AppID 与后端地址都可按环境切换（生产用 https，本地开发用 http）。
 *
 * 用法：
 *   node scripts/apply-env.mjs     # 每次打开微信开发者工具前运行一次
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const envPath = path.join(root, '.env')
const privateConfigPath = path.join(root, 'project.private.config.json')
const envGeneratedPath = path.join(root, 'utils', 'env.generated.js')

if (!fs.existsSync(envPath)) {
  console.error('[apply-env] 未找到 .env 文件，请先复制 .env.example 为 .env 并完成填写。')
  process.exit(1)
}

function readEnv(key, fallback = '') {
  for (const line of fs.readFileSync(envPath, 'utf8').split(/\r?\n/)) {
    const m = line.match(new RegExp(`^\\s*${key}\\s*=\\s*(.*?)\\s*$`))
    if (m) {
      const v = m[1].trim().replace(/^["']|["']$/g, '')
      if (v) return v
    }
  }
  return fallback
}

// 1) AppID → project.private.config.json
const appid = readEnv('WX_APPID')
if (!appid) {
  console.error('[apply-env] .env 中未找到有效的 WX_APPID。')
  process.exit(1)
}
const privateConfig = JSON.parse(fs.readFileSync(privateConfigPath, 'utf8'))
privateConfig.appid = appid
fs.writeFileSync(privateConfigPath, JSON.stringify(privateConfig, null, 2) + '\n')

// 2) BASE_URL → utils/env.generated.js（本地默认 http://127.0.0.1:8081，生产填 https://你的域名）
const baseUrl = readEnv('WX_BASE_URL', 'http://127.0.0.1:8081')
const generated = `// 本文件由 scripts/apply-env.mjs 自动生成，请勿手动修改（已被 .gitignore 忽略）\nmodule.exports = {\n  WX_BASE_URL: '${baseUrl}',\n}\n`
fs.writeFileSync(envGeneratedPath, generated)

console.log(`[apply-env] 已写入:\n  - project.private.config.json appid = ${appid}\n  - env.generated.js WX_BASE_URL = ${baseUrl}`)