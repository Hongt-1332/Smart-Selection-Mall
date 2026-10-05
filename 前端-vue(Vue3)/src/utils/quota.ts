const STORAGE_KEY = 'user_quota'

interface QuotaData {
  month: number
  year: number
  bgUpload: number
  avatarUpload: number
  goodsUpload: number
}

interface QuotaInfo {
  bgUpload: { max: number; used: number }
  avatarUpload: { max: number; used: number }
  goodsUpload: { max: number; used: number }
  cartLimit: { max: number; used: number }
}

const DEFAULT_QUOTA_LIMITS = {
  monthlyUpdateBackground: 5,
  monthlyUpdateAvatar: 3,
  monthlyUpdateGoods: 20,
  maxCartQuantity: 50,
}

let vipQuotaLimits = { ...DEFAULT_QUOTA_LIMITS }

export function setVipQuotaLimits(limits: {
  monthlyUpdateBackground?: number
  monthlyUpdateAvatar?: number
  monthlyUpdateGoods?: number
  maxCartQuantity?: number
}) {
  vipQuotaLimits = { ...DEFAULT_QUOTA_LIMITS, ...limits }
}

function getCurrentMonthKey(): { month: number; year: number } {
  const now = new Date()
  return { month: now.getMonth() + 1, year: now.getFullYear() }
}

function loadQuota(): QuotaData {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      return JSON.parse(raw) as QuotaData
    }
  } catch {
    // ignore
  }
  const { month, year } = getCurrentMonthKey()
  return { month, year, bgUpload: 0, avatarUpload: 0, goodsUpload: 0 }
}

function saveQuota(data: QuotaData): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

function ensureCurrentMonth(): QuotaData {
  const data = loadQuota()
  const { month, year } = getCurrentMonthKey()
  if (data.year !== year || data.month !== month) {
    const reset: QuotaData = { month, year, bgUpload: 0, avatarUpload: 0, goodsUpload: 0 }
    saveQuota(reset)
    return reset
  }
  return data
}

export function incrementBgUpload(): number {
  const data = ensureCurrentMonth()
  data.bgUpload++
  saveQuota(data)
  return data.bgUpload
}

export function incrementAvatarUpload(): number {
  const data = ensureCurrentMonth()
  data.avatarUpload++
  saveQuota(data)
  return data.avatarUpload
}

export function incrementGoodsUpload(): number {
  const data = ensureCurrentMonth()
  data.goodsUpload++
  saveQuota(data)
  return data.goodsUpload
}

export function getQuota(cartCount: number = 0): QuotaInfo {
  const data = ensureCurrentMonth()
  return {
    bgUpload: { max: vipQuotaLimits.monthlyUpdateBackground, used: data.bgUpload },
    avatarUpload: { max: vipQuotaLimits.monthlyUpdateAvatar, used: data.avatarUpload },
    goodsUpload: { max: vipQuotaLimits.monthlyUpdateGoods, used: data.goodsUpload },
    cartLimit: { max: vipQuotaLimits.maxCartQuantity, used: cartCount },
  }
}

export function isBgUploadAvailable(): boolean {
  const data = ensureCurrentMonth()
  return data.bgUpload < vipQuotaLimits.monthlyUpdateBackground
}

export function isAvatarUploadAvailable(): boolean {
  const data = ensureCurrentMonth()
  return data.avatarUpload < vipQuotaLimits.monthlyUpdateAvatar
}

export function isCartFull(cartCount: number): boolean {
  return cartCount >= vipQuotaLimits.maxCartQuantity
}