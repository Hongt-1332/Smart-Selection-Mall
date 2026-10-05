import { BaseApi } from './BaseApi'
import type { ApiResponse } from './BaseApi'

export interface VipGoods {
  id: number
  userId: number
  goodsId: string
  addressId: number
  goodsName: string
  describe: string | null
  goodsPrice: number
  goodsStock: number
  imagePath: string
  launch: boolean
  createTime: string
  delete: boolean
  userName: string
  merchantAvatar: string
  merchantId: number
  address: string
  inCart: boolean
  cartQuantity: number
}

export interface VipImage {
  imageUrl: string
  imagePath: string
}

export interface VipConfig {
  level: number
  levelName: string
  maxAddressQuantity: number
  monthlyUpdateGoods: number
  monthlyUpdateAvatar: number
  monthlyUpdateBackground: number
  maxCartQuantity: number
  maxGoodsQuantity: number
  maxAiQuantity: number
  price: number
  vipDuration: number
  createTime: string
}

export interface VipLevelResponse {
  user: {
    id: number
    level: number
    vipCreateTime: string | null
    vipDuration: number
  }
  levelConfigs: Record<string, VipConfig>
}

export class VipService extends BaseApi {
  getVipGoods(): Promise<ApiResponse<VipGoods[]>> {
    return this.get('/vip/goods')
  }

  getVipImages(): Promise<ApiResponse<VipImage[]>> {
    return this.get('/vip/image')
  }

  getHomeImages(): Promise<ApiResponse<VipImage[]>> {
    return this.get('/vip/homeImage')
  }

  getLoginImages(): Promise<ApiResponse<VipImage[]>> {
    return this.get('/vip/homeImage')
  }

  getVipConfig(): Promise<ApiResponse<VipConfig[]>> {
    return this.get('/vip/config')
  }

  getVipLevel(): Promise<ApiResponse<VipLevelResponse>> {
    return this.get('/vip/level')
  }

  buyVip(level: number, money: number, num: number = 1): Promise<ApiResponse<string>> {
    return this.post('/vip/buy', { level, money, num })
  }
}

export const vipService = new VipService()