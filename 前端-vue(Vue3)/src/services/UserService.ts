import { BaseApi } from './BaseApi'
import type { ApiResponse, PageResult } from './BaseApi'

export interface BackgroundImage {
  id?: number
  imagePath: string
  sequence: number
}

export interface UserInfoVO {
  id: number
  userName: string
  account: string
  phone: string
  email: string
  balance: number
  avatarPath: string
  describe?: string
  level: number
  vipLevelName: string
  vipCreateTime: string | null
  vipDuration: number | null
  vipRemainingTime: number
  createTime: string
  uploadTime: string
  uploadBackground: number
  uploadGoods: number
  updateAvatar: number
  delete: boolean
  maxAddressQuantity: number
  monthlyUpdateGoods: number
  monthlyUpdateAvatar: number
  monthlyUpdateBackground: number
  maxCartQuantity: number
  maxGoodsQuantity: number
  maxAiQuantity: number
  backgrounds?: BackgroundImage[]
}

export interface UserInfo {
  id: number
  addressId: number | null
  userName: string
  account: string
  phone: string
  email: string
  describe?: string
  balance: number
  avatarPath?: string
  level?: number
  vipLevelName?: string
  vipCreateTime?: string
  vipDuration?: number
  vipRemainingTime?: string
  createTime: string
  uploadTime?: string
  uploadBackground?: number
  uploadGoods?: number
  updateAvatar?: number
  delete: boolean
}

export interface AddressInfo {
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
  isDefault: boolean
}

export interface CartInfo {
  id: number
  cartId: number
  userId: number
  goodId: number
  goodsId: string
  addressId: number | null
  preId: number | null
  goodsName: string
  describe: string
  goodsPrice: number
  goodsStock: number
  imagePath: string
  launch: boolean
  createTime: string
  delete: boolean
  quantity: number
  userName: string
  merchantAvatar: string
  merchantId: number
  address: string
}

export interface GoodsInfo {
  id: number
  userId: number
  userName: string
  address?: string
  goodsName: string
  goodsPrice: number
  goodsStock: number
  imagePath: string
  describe?: string
  launch: number
  preId?: number
  createTime: string
  delete: number
  merchantAvatar?: string
  merchantId?: number
  inCart?: boolean
  cartId?: number
  cartQuantity?: number
}

export interface TradeInfo {
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
  merchantName?: string
  goodsName: string
  goodsPrice: number
  totalPrice: number
}

export interface MerchantBgInfo {
  imagePath: string
  sequence: number
}

export interface MerchantGoodsInfo {
  id: number
  userId: number
  goodsId: string
  addressId: number | null
  preId: number | null
  goodsName: string
  describe: string
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

export interface MerchantProfileInfo {
  userName: string
  describe: string
  level: number
  levelName: string
  backgrounds: MerchantBgInfo[]
  goods: MerchantGoodsInfo[]
}

export interface UserBackgroundGoods {
  id: number
  userId: number
  goodsId: number
  addressId: number | null
  preId: number | null
  goodsName: string
  describe: string
  goodsPrice: number
  goodsStock: number
  imagePath: string
  launch: boolean
  createTime: string
  address: string
}

export interface UserBackgroundVO {
  userName: string
  describe: string
  level: number
  levelName: string
  backgrounds: MerchantBgInfo[]
  goods: UserBackgroundGoods[]
}

export interface BackgroundInfo {
  id: number
  userId: number
  imagePath: string
  sequence: number
  createTime: string
  describe?: string
}

export interface CaptchaData {
  captchaImage: string
}

export class UserService extends BaseApi {
  login(params: { account: string; password: string; captchaCode: string }): Promise<ApiResponse<{ token: string; userId: string }>> {
    return this.post('/user/login', params)
  }

  register(params: {
    userName: string
    account: string
    password: string
    confirmPassword: string
    email: string
    phone: string
    captchaCode: string
  }): Promise<ApiResponse<null>> {
    return this.post('/user/register', params)
  }

  logout(): Promise<ApiResponse<null>> {
    return this.post('/user/logout')
  }

  getUserInfoVO(): Promise<ApiResponse<UserInfoVO>> {
    return this.post('/page/user/user')
  }

  update(params: { userName?: string; email?: string; phone?: string }): Promise<ApiResponse<null>> {
    return this.put('/user/update', params)
  }

  updateImage(file: File): Promise<ApiResponse<string>> {
    const formData = new FormData()
    formData.append('file', file)
    return this.upload('/user/updateImage', formData)
  }

  uploadImage(file: File): Promise<ApiResponse<string>> {
    const formData = new FormData()
    formData.append('file', file)
    return this.upload('/tool/image', formData)
  }

  updateDescribe(describe: string): Promise<ApiResponse<null>> {
    return this.put('/user/updateDescribe', null, { params: { describe } })
  }

  setDefaultAddress(addressId: number): Promise<ApiResponse<null>> {
    return this.put('/user/setDefaultAddress', null, { params: { addressId } })
  }

  getUserInfo(params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<UserInfo>>> {
    return this.post('/page/user/user', params)
  }

  getAddressList(params: {
    page?: number; size?: number
    userId?: number
  }): Promise<ApiResponse<PageResult<AddressInfo>>> {
    return this.post('/page/user/address', params)
  }

  getCartList(params: {
    page: number; size: number; desc?: boolean
  }): Promise<ApiResponse<PageResult<CartInfo>>> {
    return this.post('/page/user/cart', params)
  }

  getGoodsList(params: {
    page?: number; size?: number
    userName?: string; goodsName?: string; address?: string
    priceMin?: number; priceMax?: number
  }): Promise<ApiResponse<PageResult<GoodsInfo>>> {
    return this.post('/page/user/goods', params)
  }

  getTradeList(params: {
    page?: number; size?: number
    merchantName?: string; goodsName?: string
    originAddress?: string; targetAddress?: string; currentAddress?: string
    createTimeStart?: string; createTimeEnd?: string
  }): Promise<ApiResponse<PageResult<TradeInfo>>> {
    return this.post('/page/user/trade', params)
  }

  addAddress(params: {
    country: string
    province: string
    city: string
    county: string
    detail: string
  }): Promise<ApiResponse<null>> {
    return this.post('/user/address/add', params)
  }

  updateAddress(params: {
    id: number
    country?: string
    province?: string
    city?: string
    county?: string
    detail?: string
  }): Promise<ApiResponse<null>> {
    return this.put('/user/address/update', params)
  }

  deleteAddress(id: number): Promise<ApiResponse<null>> {
    return this.delete('/user/address/delete', undefined, { params: { id } })
  }

  handleCart(params: { goodId: number; num: number }): Promise<ApiResponse<string>> {
    return this.post('/user/cart', params)
  }

  getCartCount(): Promise<ApiResponse<number>> {
    return this.get('/user/cart/count')
  }

  buyGoods(params: { goodsId: number; quantity: number; shippingAddressId: number }): Promise<ApiResponse<null>> {
    return this.post('/user/goods/buy', params)
  }

  buyFromCart(cartIds: number[]): Promise<ApiResponse<string>> {
    return this.post('/user/cart/buy', cartIds)
  }

  cancelTrade(id: number): Promise<ApiResponse<null>> {
    return this.put('/user/trade/cancel', null, { params: { id } })
  }

  addBackground(params: { imagePath: string; sequence?: number }): Promise<ApiResponse<null>> {
    return this.post('/user/background/add', params)
  }

  updateBackground(params: { id: number; imagePath?: string; sequence?: number }): Promise<ApiResponse<null>> {
    return this.put('/user/background/update', params)
  }

  getBackgroundList(params: { page?: number; size?: number }): Promise<ApiResponse<PageResult<BackgroundInfo>>> {
    return this.post('/page/user/background', params)
  }

  getCaptcha(): Promise<ApiResponse<CaptchaData>> {
    return this.get('/tool/captcha')
  }

  getMerchantBackground(userId: number): Promise<ApiResponse<MerchantProfileInfo>> {
    return this.get('/page/merchant/merchantBackground', { id: userId })
  }

  getUserBackground(): Promise<ApiResponse<UserBackgroundVO>> {
    return this.post('/page/user/userBackground')
  }
}

export const userService = new UserService()