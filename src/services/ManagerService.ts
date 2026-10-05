import { BaseApi } from './BaseApi'
import type { ApiResponse, PageResult } from './BaseApi'

interface UserInfo {
  id: number
  userName: string
  account: string
  avatarPath: string
  level: number
  vipLevelName?: string
  balance: number
  phone: string
  email: string
  describe: string
  vipCreateTime?: string
  vipDuration?: number
  vipRemainingTime?: string
  createTime: string
  uploadTime?: string
  uploadBackground: number
  uploadGoods: number
  updateAvatar: number
  delete: boolean
}

interface AddressInfo {
  id: number
  userId: number
  addressId: number
  country: string
  province: string
  city: string
  county: string
  detail: string
  userName: string
  createTime: string
  delete: boolean
}

interface CartInfo {
  id: number
  userId: number
  goodId: number
  quantity: number
  createTime: string
  userName: string
  goodsName: string
  goodsPrice: number
  imagePath: string
  launch: boolean
}

interface GoodsInfo {
  id: number
  userId: number
  userName: string
  goodsName: string
  describe: string
  goodsPrice: number
  goodsStock: number
  launch: boolean
  imagePath: string
  createTime: string
  delete: boolean
}

interface TradeInfo {
  id: number
  orderId: string
  userId: number
  goodId: number
  quantity: number
  originAddress: string
  targetAddress: string
  currentAddress: string
  createTime: string
  payTime: string | null
  cancelTime: string | null
  finishTime: string | null
  delete: boolean
  merchantName: string
  goodsName: string
  goodsPrice: number
  totalPrice: number
}

interface VipConfigInfo {
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

interface VipTradeInfo {
  id: number
  userId: number
  level: number
  money: number
  num: number
  createTime: string
  quantity: number
  addedTime: string
  vipExpireTime: string
}

interface AiInfo {
  id: number
  userName: string
  userId: number
  goodsId: string
  category: string
  kind: string
  name: string
  price: string
  simpleDescription: string
  features: string
  createTime: string
  delete: boolean
}

export class ManagerService extends BaseApi {

  getUserList(params: {
    page?: number
    size?: number
    userName?: string
    account?: string
  }): Promise<ApiResponse<PageResult<UserInfo>>> {
    return this.post('/page/manager/user', params)
  }

  getAddressList(params: {
    page?: number
    size?: number
    id?: number
  }): Promise<ApiResponse<PageResult<AddressInfo>>> {
    return this.post('/page/manager/address', params)
  }

  getCartList(params: {
    page?: number
    size?: number
    id?: number
  }): Promise<ApiResponse<PageResult<CartInfo>>> {
    return this.post('/page/manager/cart', params)
  }

  getGoodsList(params: {
    page?: number
    size?: number
    userName?: string
    goodsName?: string
    describe?: string
  }): Promise<ApiResponse<PageResult<GoodsInfo>>> {
    return this.post('/page/manager/goods', params)
  }

  getTradeList(params: {
    page?: number
    size?: number
    merchantName?: string
    goodsName?: string
  }): Promise<ApiResponse<PageResult<TradeInfo>>> {
    return this.post('/page/manager/trade', params)
  }

  getVipConfigList(params: {
    page?: number
    size?: number
  }): Promise<ApiResponse<PageResult<VipConfigInfo>>> {
    return this.post('/page/manager/vipConfig', params)
  }

  getVipTradeList(params: {
    page?: number
    size?: number
    id?: number
  }): Promise<ApiResponse<PageResult<VipTradeInfo>>> {
    return this.post('/page/manager/vipTrade', params)
  }

  updateVipTrade(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/viptrade', params)
  }

  getAiList(params: {
    page?: number
    size?: number
  }): Promise<ApiResponse<PageResult<AiInfo>>> {
    return this.post('/page/manager/ai', params)
  }

  getHistoryGoodsList(params: {
    page?: number
    size?: number
    goodsId?: string
  }): Promise<ApiResponse<PageResult<GoodsInfo>>> {
    return this.post('/page/manager/historyGoods', params)
  }

  updateUser(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/user', params)
  }

  updateAddress(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/address', params)
  }

  deleteCart(id: number): Promise<ApiResponse<null>> {
    return this.delete('/manager/cart/delete', undefined, { params: { id } })
  }

  updateGoods(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/goods', params)
  }

  updateTrade(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/trade', params)
  }

  saveVipConfig(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.post('/manager/config/vip', params)
  }

  updateVipConfig(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/config/vip', params)
  }

  updateAi(params: Record<string, unknown>): Promise<ApiResponse<null>> {
    return this.put('/manager/ai/updateAi', params)
  }

  async uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    const res = await this.upload<ApiResponse<string>>('/tool/image', formData)
    if (res.code === 200) return res.data
    throw new Error(res.message || '上传失败')
  }

  async getUnfinishedOrderCount(): Promise<ApiResponse<number>> {
    const res = await this.post<ApiResponse<PageResult<unknown>>>('/page/merchant/trade', { page: 1, size: 1 })
    if (res.code === 200 && res.data) {
      const count = (res.data as any).extra?.unfinishedCount
      return { code: 200, message: 'ok', data: typeof count === 'number' ? count : 0 }
    }
    return { code: res.code, message: res.message, data: 0 }
  }
}

export const managerService = new ManagerService()