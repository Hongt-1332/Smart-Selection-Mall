import { BaseApi } from './BaseApi'
import type { ApiResponse, PageResult } from './BaseApi'
import type { GoodsInfo, TradeInfo, BackgroundInfo } from './UserService'

export interface AiInfo {
  id?: number
  userId?: number
  goodsId?: number
  category: string
  kind: string
  name: string
  price: string
  simpleDescription: string
  features: string
  createTime?: string
  delete?: boolean
}

export class MerchantService extends BaseApi {
  addGoods(formData: FormData): Promise<ApiResponse<string>> {
    return this.upload('/merchant/goods/add', formData)
  }

  updateGoods(params: {
    id: number
    addressId?: number
    goodsName?: string
    describe?: string
    goodsPrice?: number
    goodsStock?: number
    launch?: number
  }): Promise<ApiResponse<string>> {
    return this.put('/merchant/goods/update', params)
  }

  updateImage(id: number, formData: FormData): Promise<ApiResponse<string>> {
    return this.putUpload(`/merchant/goods/updateImage?id=${id}`, formData)
  }

  updateDescribe(id: number, describe: string): Promise<ApiResponse<string>> {
    return this.put('/merchant/goods/updateDescribe', null, { params: { id, describe } })
  }

  updateLaunch(id: number, launch: number): Promise<ApiResponse<string>> {
    return this.put('/merchant/goods/updateLaunch', null, { params: { id, launch: launch === 1 } })
  }

  cancelTrade(id: number): Promise<ApiResponse<null>> {
    return this.put('/merchant/trade/cancel', null, { params: { id } })
  }

  finishTrade(id: number): Promise<ApiResponse<null>> {
    return this.put('/merchant/trade/finishTrade', null, { params: { id } })
  }

  getGoodsList(params: {
    page?: number; size?: number
    desc?: boolean
    launch?: number
  }): Promise<ApiResponse<PageResult<GoodsInfo>>> {
    return this.post('/page/merchant/goods', params)
  }

  getTradeList(params: {
    page?: number; size?: number
    userName?: string; goodsName?: string
    originAddress?: string; targetAddress?: string; currentAddress?: string
  }): Promise<ApiResponse<PageResult<TradeInfo>>> {
    return this.post('/page/merchant/trade', params)
  }

  addAi(params: Omit<AiInfo, 'id'>): Promise<ApiResponse<string>> {
    return this.post('/merchant/ai/add', params)
  }

  getAiGoodsList(): Promise<ApiResponse<{ id: number; goodsName: string }[]>> {
    return this.get('/merchant/ai/goodsList')
  }

  updateAi(params: { id: number; category: string; kind: string; name: string; price: string; simpleDescription: string; features: string }): Promise<ApiResponse<string>> {
    return this.put('/merchant/ai/update', params)
  }

  getAiList(params: {
    page?: number; size?: number
    desc?: boolean
    category?: string; kind?: string; name?: string
    priceStart?: string; priceEnd?: string
    simpleDescription?: string; features?: string
    createTimeStart?: string; createTimeEnd?: string
    delete?: boolean
  }): Promise<ApiResponse<PageResult<AiInfo>>> {
    return this.post('/page/merchant/ai', params)
  }

  getUserList(params: {
    page?: number; size?: number; userName?: string
  }): Promise<ApiResponse<PageResult<{ id: number; userName: string; avatarPath: string; email: string; describe: string; level: number; createTime: string }>>> {
    return this.post('/page/merchant/user', params)
  }

  getBackgroundList(params: {
    page?: number; size?: number
  }): Promise<ApiResponse<PageResult<BackgroundInfo>>> {
    return this.post('/page/merchant/background', params)
  }
}

export const merchantService = new MerchantService()