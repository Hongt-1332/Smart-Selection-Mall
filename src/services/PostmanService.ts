import { BaseApi } from './BaseApi'
import type { ApiResponse } from './BaseApi'

export class PostmanService extends BaseApi {
  updateCurrentAddress(params: { id: number; currentAddress: string }): Promise<ApiResponse<null>> {
    return this.post('/postman/updateCurrentAddress', params)
  }
}

export const postmanService = new PostmanService()