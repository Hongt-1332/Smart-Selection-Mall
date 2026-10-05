/**
 * 全局状态（对应原项目 stores/user.ts，用小程序全局数据 + 缓存实现）
 */
const { userApi } = require('./api/user')
const { getStoredToken, setStoredToken, removeStoredToken } = require('./request')

const USER_CACHE_KEY = 'user_store'

const store = {
  isLoggedIn: false,
  userInfo: null,
  userInfoVO: null,
  defaultAddress: null,
  dirty: true,
  userListCache: [],
  userListDirty: true,

  restore() {
    try {
      const saved = wx.getStorageSync(USER_CACHE_KEY)
      if (saved && typeof saved === 'object') {
        this.isLoggedIn = !!saved.isLoggedIn
      }
    } catch (e) {
      // ignore
    }
  },

  persist() {
    try {
      wx.setStorageSync(USER_CACHE_KEY, { isLoggedIn: this.isLoggedIn })
    } catch (e) {
      // ignore
    }
  },

  get userName() {
    return (this.userInfo && this.userInfo.userName) || ''
  },

  async checkSession() {
    const tokenExists = !!getStoredToken()
    if (tokenExists) this.isLoggedIn = true
    try {
      const res = await userApi.getUserInfo({ page: 1, size: 1 })
      if (res.code === 200 && res.data && res.data.list && res.data.list.length > 0) {
        this.userInfo = res.data.list[0]
        this.isLoggedIn = true
      } else if (!tokenExists) {
        this.isLoggedIn = false
        this.userInfo = null
      }
    } catch (e) {
      if (!tokenExists) {
        this.isLoggedIn = false
        this.userInfo = null
      }
    }
    this.persist()
  },

  async login(params) {
    const res = await userApi.login(params)
    if (res.code === 200) {
      this.isLoggedIn = true
      if (res.data && res.data.token) {
        setStoredToken(res.data.token)
      }
      this.markDirty()
      await this.fetchUserInfo()
      this.persist()
    }
    return res
  },

  async register(params) {
    return await userApi.register(params)
  },

  async logout() {
    try {
      await userApi.logout()
    } catch (e) {
      // ignore
    }
    this.isLoggedIn = false
    this.userInfo = null
    this.userInfoVO = null
    this.defaultAddress = null
    removeStoredToken()
    this.persist()
  },

  async fetchUserInfo() {
    try {
      const res = await userApi.getUserInfo({ page: 1, size: 1 })
      if (res.code === 200 && res.data && res.data.list && res.data.list.length > 0) {
        this.userInfo = res.data.list[0]
      }
    } catch (e) {
      // 静默
    }
    return this.userInfo
  },

  async fetchUserInfoVO(force = false) {
    if (!force && this.userInfoVO && !this.dirty) return this.userInfoVO
    try {
      const res = await userApi.getUserInfoVO()
      if (res.code === 200 && res.data) {
        this.userInfoVO = res.data
        this.dirty = false
      }
    } catch (e) {
      // 静默
    }
    return this.userInfoVO
  },

  markDirty() {
    this.dirty = true
    this.userListDirty = true
  },

  async fetchUserList(force = false) {
    if (!force && this.userListCache.length > 0 && !this.userListDirty) return this.userListCache
    try {
      const res = await userApi.getUserInfo({ page: 1, size: 100 })
      if (res.code === 200 && res.data && res.data.list) {
        this.userListCache = res.data.list
        this.userListDirty = false
      }
    } catch (e) {
      // 静默
    }
    return this.userListCache
  },

  async fetchDefaultAddress() {
    try {
      const res = await userApi.getAddressList({ page: 1, size: 10 })
      if (res.data && res.data.list && res.data.list.length > 0) {
        this.defaultAddress = res.data.list[0]
      }
    } catch (e) {
      this.defaultAddress = null
    }
    return this.defaultAddress
  },
}

module.exports = { store }
