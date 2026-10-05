/**
 * SSE（Server-Sent Events）流式响应解析器
 *
 * 用途：配合后端 SseEmitter 的流式接口（/ai/chatStream），
 * 把 wx.request 的 enableChunked 分片数据解析成一个个事件。
 *
 * 两个难点：
 * 1. 小程序没有 TextDecoder，onChunkReceived 给的是 ArrayBuffer，
 *    需要手写 UTF-8 解码（且要处理「汉字被分片截断」的跨包情况）；
 * 2. SSE 会粘包 / 断包：一次分片可能含多条 event，一条 event 也可能跨分片。
 *    这里用一个缓冲区累积，按 \n\n 切分完整事件再解析。
 */

/**
 * UTF-8 字节流解码器（支持跨分片续传）
 *
 * 为什么不直接用 String.fromCharCode：中文是 3 字节，
 * 若被分片切开会产生乱码。这里保留未完成的字节，等下一片补齐。
 */
class Utf8Decoder {
  constructor() {
    /** 上一片未解码完的字节（不完整的多字节字符） */
    this.pending = []
  }

  /**
   * 解码一片数据
   * @param {ArrayBuffer} buffer
   * @returns {string} 已能解码出的文本
   */
  decode(buffer) {
    const bytes = this.pending.concat(Array.from(new Uint8Array(buffer)))
    this.pending = []

    let out = ''
    let i = 0
    while (i < bytes.length) {
      const b = bytes[i]
      let need = 0
      let cp = 0

      if (b < 0x80) {
        // 单字节 ASCII
        out += String.fromCharCode(b)
        i++
        continue
      } else if ((b & 0xe0) === 0xc0) {
        need = 1
        cp = b & 0x1f
      } else if ((b & 0xf0) === 0xe0) {
        need = 2
        cp = b & 0x0f
      } else if ((b & 0xf8) === 0xf0) {
        need = 3
        cp = b & 0x07
      } else {
        // 非法起始字节，跳过
        i++
        continue
      }

      // 字节不够，说明这个字符被分片截断，留给下一片
      if (i + need >= bytes.length) {
        this.pending = bytes.slice(i)
        break
      }

      let valid = true
      for (let k = 1; k <= need; k++) {
        const nb = bytes[i + k]
        if ((nb & 0xc0) !== 0x80) {
          valid = false
          break
        }
        cp = (cp << 6) | (nb & 0x3f)
      }

      if (!valid) {
        i++
        continue
      }

      // 按码点转成字符串（代理对处理）
      if (cp > 0xffff) {
        cp -= 0x10000
        out += String.fromCharCode(0xd800 + (cp >> 10), 0xdc00 + (cp & 0x3ff))
      } else {
        out += String.fromCharCode(cp)
      }
      i += need + 1
    }

    return out
  }
}

/**
 * SSE 事件流解析器
 *
 * 后端推送格式（SseEmitter 默认）：
 *   event:message
 *   data:你好
 *
 *   event:done
 *   data:[DONE]
 */
class SseParser {
  constructor() {
    this.buffer = ''
    /** 当前正在解析的事件块字段 */
    this.eventName = ''
    this.dataLines = []
  }

  /**
   * 喂入一段文本，返回本次解析出的完整事件数组
   * @param {string} text
   * @returns {Array<{event: string, data: string}>}
   */
  feed(text) {
    this.buffer += text
    const events = []

    // SSE 以空行分隔事件；兼容 \r\n
    let idx
    while ((idx = this.buffer.indexOf('\n\n')) >= 0) {
      const block = this.buffer.slice(0, idx)
      this.buffer = this.buffer.slice(idx + 2)
      const ev = this.parseBlock(block)
      if (ev) events.push(ev)
    }
    // 兼容 \r\n\r\n
    while ((idx = this.buffer.indexOf('\r\n\r\n')) >= 0) {
      const block = this.buffer.slice(0, idx)
      this.buffer = this.buffer.slice(idx + 4)
      const ev = this.parseBlock(block)
      if (ev) events.push(ev)
    }

    return events
  }

  /** 解析单个事件块 */
  parseBlock(block) {
    if (!block) return null
    let event = 'message'
    const dataParts = []

    block.split(/\r?\n/).forEach((line) => {
      if (!line || line.startsWith(':')) return
      const sep = line.indexOf(':')
      const field = sep >= 0 ? line.slice(0, sep) : line
      // SSE 规范：冒号后若有一个空格需去掉
      let value = sep >= 0 ? line.slice(sep + 1) : ''
      if (value.startsWith(' ')) value = value.slice(1)

      if (field === 'event') event = value
      else if (field === 'data') dataParts.push(value)
    })

    return { event, data: dataParts.join('\n') }
  }

  /** 流结束时的兜底：把残留内容也解析一次 */
  flush() {
    if (!this.buffer.trim()) return []
    const block = this.buffer
    this.buffer = ''
    const ev = this.parseBlock(block)
    return ev ? [ev] : []
  }
}

module.exports = { Utf8Decoder, SseParser }
