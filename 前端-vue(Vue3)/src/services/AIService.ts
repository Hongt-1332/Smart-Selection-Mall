import { BaseApi } from './BaseApi'
import type { ApiResponse } from './BaseApi'

export interface ProductDTO {
  id: number
  goodsId: number
  goodsName: string
  goodsPrice: number
  describe?: string
  imagePath?: string
  goodsStock?: number
  userName?: string
  merchantAvatar?: string
  merchantId?: number
  address?: string
  cartQuantity?: number
  cartId?: number
  inCart?: boolean
  createTime?: string
  delete?: boolean
  launch?: boolean
  userId?: number
  preId?: number | null
  addressId?: number
  priceMax?: number | null
  priceMin?: number | null
}

export interface ChatResponse {
  message: string
  products: ProductDTO[]
}

export interface ChatMessageUser {
  type: 'USER'
  contents: { text: string; type: string }[]
}

export interface ChatMessageAI {
  type: 'AI'
  text: string
  toolExecutionRequests: unknown[]
  attributes: Record<string, unknown>
}

export interface ChatMessageSystem {
  type: 'SYSTEM'
  contents: { text: string; type: string }[]
}

export type ChatMessage = ChatMessageUser | ChatMessageAI | ChatMessageSystem

export class AIService extends BaseApi {
  constructor() {
    super('', 30000)
  }

  chat(message: string): Promise<ApiResponse<ChatResponse>> {
    return this.post('/ai/chat', message)
  }

  chatStream(
    message: string,
    onMessage: (chunk: string) => void,
    onProducts: (products: ProductDTO[]) => void,
    onDone: () => void,
    onError: (err: string) => void,
  ): void {
    const token = localStorage.getItem('auth_token') || ''
    fetch('/ai/chatStream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : '',
      },
      body: message,
    })
      .then((response) => {
        if (!response.ok) {
          onError('请求失败: ' + response.status)
          return
        }
        const reader = response.body?.getReader()
        if (!reader) {
          onError('无法读取流')
          return
        }
        const decoder = new TextDecoder()
        let buffer = ''
        let event = ''
        function read(): Promise<void> {
          return reader!.read().then(({ done, value }) => {
            if (done) {
              onDone()
              return
            }
            buffer += decoder.decode(value, { stream: true })
            const lines = buffer.split('\n')
            buffer = lines.pop() || ''
            for (const line of lines) {
              if (line.startsWith('event:')) {
                event = line.slice(6).trim()
              } else if (line.startsWith('data:')) {
                const data = line.slice(5).trim()
                if (data === '[DONE]') {
                  onDone()
                  return
                }
                if (event === 'products') {
                  try {
                    onProducts(JSON.parse(data))
                  } catch { /* ignore */ }
                } else if (event === 'message') {
                  onMessage(data)
                }
                event = ''
              }
            }
            return read()
          })
        }
        return read()
      })
      .catch((e) => {
        onError(e.message || '流式请求异常')
      })
  }

  chatClear(): Promise<ApiResponse<string>> {
    return this.delete('/ai/chatClear')
  }

  chatHistory(): Promise<ApiResponse<string>> {
    return this.get('/ai/chatHistory')
  }
}

export const aiService = new AIService()