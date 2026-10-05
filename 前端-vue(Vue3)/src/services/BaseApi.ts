import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  hasPreviousPage: boolean
  hasNextPage: boolean
  list: T[]
  extra?: Record<string, unknown>
}

export interface ImageItem {
  name: string
  data: string
}

const TOKEN_KEY = 'auth_token'

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setStoredToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    console.warn('[Token] failed to save token to localStorage')
  }
}

export function removeStoredToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // ignore
  }
}

export class BaseApi {
  protected client: AxiosInstance

  constructor(baseURL: string = '', timeout: number = 10000) {
    this.client = axios.create({
      baseURL,
      timeout,
      withCredentials: true,
    })

    this.client.interceptors.request.use((config) => {
      const token = getStoredToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json'
      }
      return config
    })

    this.client.interceptors.response.use(
      (res) => {
        return res
      },
      (err) => {
        const status = err.response?.status
        const msg = err.response?.data?.message || err.message || '请求失败'

        if (status === 401 || status === 403) {
          return err.response
        }
        return Promise.reject(new Error(msg))
      },
    )
  }

  protected async get<T>(endpoint: string, params?: Record<string, string | number | boolean | undefined>): Promise<T> {
    const res = await this.client.get(endpoint, { params })
    return res.data as T
  }

  protected async post<T>(endpoint: string, data?: unknown): Promise<T> {
    const res = await this.client.post(endpoint, data)
    return res.data as T
  }

  protected async put<T>(endpoint: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const res = await this.client.put(endpoint, data, config)
    return res.data as T
  }

  protected async delete<T>(endpoint: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const mergedConfig: AxiosRequestConfig = data !== undefined ? { ...config, data } : (config || {})
    const res = await this.client.delete(endpoint, mergedConfig)
    return res.data as T
  }

  protected async upload<T>(endpoint: string, formData: FormData): Promise<T> {
    const res = await this.client.post(endpoint, formData)
    return res.data as T
  }

  protected async putUpload<T>(endpoint: string, formData: FormData): Promise<T> {
    const res = await this.client.put(endpoint, formData)
    return res.data as T
  }

  async getImages(paths: string[]): Promise<ApiResponse<Record<string, string>>> {
    const res = await this.client.post('/image/getImages', paths)
    const apiRes = res.data as ApiResponse<ImageItem[]>
    if (apiRes.code === 200 && Array.isArray(apiRes.data)) {
      const map: Record<string, string> = {}
      apiRes.data.forEach((item) => {
        map[item.name] = item.data
      })
      return { code: apiRes.code, message: apiRes.message, data: map }
    }
    return apiRes as unknown as ApiResponse<Record<string, string>>
  }
}