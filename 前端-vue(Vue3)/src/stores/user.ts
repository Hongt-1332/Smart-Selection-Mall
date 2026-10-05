import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { userService } from '../services/UserService'
import { getStoredToken, setStoredToken, removeStoredToken } from '../services/BaseApi'
import type { UserInfo, UserInfoVO, AddressInfo } from '../services/UserService'

export const useUserStore = defineStore('user', () => {
  const isLoggedIn = ref(false)
  const isAdmin = ref(false)
  const userInfo = ref<UserInfo | null>(null)
  const userInfoVO = ref<UserInfoVO | null>(null)
  const defaultAddress = ref<AddressInfo | null>(null)
  const dirty = ref(true)
  const userListCache = ref<UserInfo[]>([])
  const userListDirty = ref(true)

  const userName = computed(() => userInfo.value?.userName ?? '')

  async function checkSession() {
    const tokenExists = !!getStoredToken()
    // If token exists, keep logged in first
    if (tokenExists) {
      isLoggedIn.value = true
    }
    try {
      const res = await userService.getUserInfoVO()
      if (res.code === 200 && res.data) {
        userInfo.value = res.data as any
        isLoggedIn.value = true
      } else if (!tokenExists && !isAdmin.value) {
        isLoggedIn.value = false
        userInfo.value = null
      }
    } catch {
      if (!tokenExists && !isAdmin.value) {
        isLoggedIn.value = false
        userInfo.value = null
      }
    }
  }

  async function login(params: { account: string; password: string; captchaCode: string }) {
    const res = await userService.login(params)
    if (res.code === 200) {
      isLoggedIn.value = true
      if (res.data?.token) {
        setStoredToken(res.data.token)
      }
      markDirty()
      await fetchUserInfo()
    }
    return res
  }

  async function register(params: {
    userName: string
    account: string
    password: string
    confirmPassword: string
    email: string
    phone: string
    captchaCode: string
  }) {
    return await userService.register(params)
  }

  async function logout() {
    try {
      await userService.logout()
    } finally {
      isLoggedIn.value = false
      isAdmin.value = false
      userInfo.value = null
      defaultAddress.value = null
      removeStoredToken()
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await userService.getUserInfoVO()
      if (res.code === 200 && res.data) {
        userInfo.value = res.data as any
      }
    } catch {
      // fetchUserInfo 失败不影响登录状态，仅静默处理
    }
  }

  async function fetchUserInfoVO(force = false) {
    if (!force && userInfoVO.value && !dirty.value) return userInfoVO.value
    try {
      const res = await userService.getUserInfoVO()
      if (res.code === 200 && res.data) {
        userInfoVO.value = res.data
        dirty.value = false
      }
    } catch {
      // 静默处理
    }
    return userInfoVO.value
  }

  function markDirty() {
    dirty.value = true
    userListDirty.value = true
  }

  async function fetchUserList(force = false): Promise<UserInfo[]> {
    if (!force && userListCache.value.length > 0 && !userListDirty.value) return userListCache.value
    try {
      const res = await userService.getUserInfoVO()
      if (res.code === 200 && res.data) {
        userListCache.value = [res.data as any]
        userListDirty.value = false
      }
    } catch {
      // 静默处理
    }
    return userListCache.value
  }

  async function fetchDefaultAddress() {
    try {
      const res = await userService.getAddressList({ page: 1, size: 10 })
      if (res.data.list.length > 0) {
        defaultAddress.value = res.data.list[0] ?? null
      }
    } catch {
      defaultAddress.value = null
    }
  }

  return {
    isLoggedIn,
    isAdmin,
    userInfo,
    userInfoVO,
    defaultAddress,
    dirty,
    userListCache,
    userName,
    checkSession,
    login,
    register,
    logout,
    fetchUserInfo,
    fetchUserInfoVO,
    fetchUserList,
    markDirty,
    fetchDefaultAddress,
  }
}, { persist: 'user_store', persistFields: ['isLoggedIn'] } as any)