/**
 * AI 相关接口（对应原项目 services/AIService.ts）
 *
 * 两个对话接口：
 *   chat       — 一次性返回（等待时间长，约 7s）
 *   chatStream — SSE 流式返回，边生成边显示（推荐）
 */
const { request, getStoredToken } = require('../request')
const { BASE_URL } = require('../config')
const { Utf8Decoder, SseParser } = require('../sse')

const aiApi = {
  /** 原项目：post('/ai/chat', JSON.stringify(message))，body 为 JSON 字符串 */
  chat: (message) => request('/ai/chat', { method: 'POST', data: JSON.stringify(message), timeout: 30000, header: { 'Content-Type': 'application/json' } }),
  chatClear: () => request('/ai/chatClear', { method: 'DELETE', timeout: 30000 }),
  chatHistory: () => request('/ai/chatHistory', { method: 'GET', timeout: 30000 }),

  /**
   * 流式对话（SSE）。
   *
   * 后端推送三类事件：
   *   products — 商品数组（JSON），在开头推送一次
   *   message  — 文本片段，逐 token 推送
   *   done     — [DONE]，表示生成结束
   *
   * @param {string} message 用户消息
   * @param {object} handlers
   * @param {(products: Array) => void} [handlers.onProducts] 收到商品列表
   * @param {(chunk: string) => void}  [handlers.onMessage]  收到文本片段
   * @param {() => void}               [handlers.onDone]     生成结束
   * @param {(err: Error) => void}     [handlers.onError]    出错
   * @returns {{abort: Function}} 可调用 abort() 中断请求
   */
  chatStream(message, handlers = {}) {
    const { onProducts, onMessage, onDone, onError } = handlers
    const decoder = new Utf8Decoder()
    const parser = new SseParser()
    const token = getStoredToken()
    let finished = false
    let aborted = false

    const finish = () => {
      if (finished || aborted) return
      finished = true
      if (onDone) onDone()
    }

    const fail = (err) => {
      if (finished || aborted) return
      finished = true
      if (onError) onError(err)
    }

    /** 处理解析出的事件 */
    const handleEvents = (events) => {
      events.forEach((ev) => {
        if (ev.event === 'products') {
          try {
            const list = JSON.parse(ev.data)
            if (onProducts) onProducts(Array.isArray(list) ? list : [])
          } catch (e) {
            // 商品数据解析失败不影响对话
          }
        } else if (ev.event === 'message') {
          if (onMessage) onMessage(ev.data)
        } else if (ev.event === 'done') {
          finish()
        }
      })
    }

    const task = wx.request({
      url: BASE_URL + '/ai/chatStream',
      method: 'POST',
      data: JSON.stringify(message),
      header: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      timeout: 120000,
      // 开启分片接收，否则拿不到流式数据
      enableChunked: true,
      success() {
        // 流式请求正常结束时触发；兜底解析残留
        handleEvents(parser.flush())
        finish()
      },
      fail(err) {
        if (aborted) return
        fail(new Error((err && err.errMsg) || 'AI 服务连接失败'))
      },
    })

    // 分片到达：解码 → 解析 SSE → 分发事件
    if (task && typeof task.onChunkReceived === 'function') {
      task.onChunkReceived((res) => {
        if (aborted) return
        try {
          const text = decoder.decode(res.data)
          if (!text) return
          handleEvents(parser.feed(text))
        } catch (e) {
          // 单次分片解析失败不影响后续
        }
      })
    } else {
      // 基础库过低不支持分片，退回一次性接口
      return aiApi.chat(message) && { abort() {} }
    }

    return {
      abort() {
        aborted = true
        try {
          task.abort()
        } catch (e) {
          // ignore
        }
      },
    }
  },
}

module.exports = { aiApi }
