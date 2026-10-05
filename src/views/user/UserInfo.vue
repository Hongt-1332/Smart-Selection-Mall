<template>
  <div>
    <div v-if="!isLoggedIn" class="not-logged-in">
      <div class="nl-card">
        <div class="nl-icon">🔒</div>
        <p class="nl-text">请先登录</p>
        <p class="nl-sub">登录后可查看和编辑个人信息</p>
        <el-button type="primary" size="large" round class="nl-btn" @click="goLogin">立即登录</el-button>
      </div>
    </div>

    <template v-else>
      <div class="info-page" v-loading="loading">
        <!-- ===== 顶部横幅 ===== -->
        <div class="profile-banner">
          <div class="banner-bg" />
          <div class="banner-inner">
            <!-- 头像 -->
            <div class="avatar-col">
              <el-upload
                class="avatar-uploader"
                action=""
                :http-request="handleUpload"
                :show-file-list="false"
                accept="image/*"
                :before-upload="beforeUpload"
              >
                <div class="avatar-ring">
                  <img v-if="imageUrl" :src="imageUrl" class="avatar-img" />
                  <el-icon v-else class="avatar-icon" :size="40"><UserFilled /></el-icon>
                  <div class="avatar-edit">
                    <el-icon :size="14"><Camera /></el-icon>
                  </div>
                </div>
              </el-upload>
              <p v-if="uploadLoading" class="upload-hint">上传中...</p>
            </div>

            <!-- 信息卡片 -->
            <div class="info-grid">
              <div class="info-cell">
                <span class="cell-label">账号</span>
                <span class="cell-val">{{ updateForm.account }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">用户名</span>
                <span class="cell-val">{{ updateForm.userName || '未设置' }}</span>
              </div>
              <div class="info-cell info-cell-balance">
                <span class="cell-label">余额</span>
                <span class="cell-val cell-val-balance">¥{{ updateForm.balance ? updateForm.balance.toFixed(2) : '0.00' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">VIP等级</span>
                <el-tag :type="userLevel > 0 ? 'warning' : 'info'" effect="dark" size="large" round>
                  {{ userLevel > 0 ? `${userLevelName}${vipRemainingTime > 0 ? ` · 剩余${vipRemainingTime}天` : ''}` : '普通用户' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 下方卡片 ===== -->
        <div class="bottom-section">
          <el-card class="section-card form-card-full">
            <template #header>
              <span class="card-hd">✏️ 编辑资料</span>
            </template>
            <div class="form-row">
              <el-form :model="updateForm" :rules="rules" ref="formRef" class="info-form" @keyup.enter="handleUpdate">
                <div class="form-fields">
                  <el-form-item label="用户名" prop="userName">
                    <el-input v-model="updateForm.userName" placeholder="2-20个字符" />
                  </el-form-item>
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="updateForm.email" placeholder="请输入邮箱" />
                  </el-form-item>
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="updateForm.phone" placeholder="请输入手机号" />
                  </el-form-item>
                </div>
                <el-button type="primary" class="save-btn" @click="handleUpdate" :loading="updateLoading">
                  保存修改
                </el-button>
              </el-form>
              <el-divider />
              <div class="desc-inline">
                <label class="desc-label">个人简介</label>
                <el-input
                  v-model="updateForm.describe"
                  type="textarea"
                  :rows="2"
                  placeholder="介绍一下自己..."
                  maxlength="200"
                  show-word-limit
                  resize="none"
                />
                <el-button type="primary" class="desc-save" @click="handleSaveDescribe" :loading="describeLoading">
                  保存简介
                </el-button>
              </div>
            </div>
          </el-card>

          <div class="bottom-row">
            <el-card class="section-card bg-card">
              <template #header>
                <span class="card-hd">🖼️ 背景管理</span>
              </template>
              <div class="bg-slots">
                <div v-for="(slot, idx) in 4" :key="idx" class="bg-slot">
                  <div class="slot-label">位置 {{ idx + 1 }}</div>
                  <div v-if="getSlotBg(idx + 1)" class="slot-img-wrap">
                    <img :src="getSlotBg(idx + 1)!.imagePath" class="slot-img" />
                  </div>
                  <div v-else class="slot-empty">
                    <div class="slot-placeholder">
                      <el-icon :size="20"><Picture /></el-icon>
                    </div>
                  </div>
                  <div class="slot-upload">
                    <el-upload
                      action=""
                      :http-request="(opt: any) => handleSlotUpload(opt, idx + 1)"
                      :show-file-list="false"
                      accept="image/*"
                      :before-upload="beforeUpload"
                    >
                      <el-button size="small" :loading="bgUploadLoading && uploadingSlot === (idx + 1)">+ 上传</el-button>
                    </el-upload>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card class="section-card quota-card">
              <template #header>
                <div class="card-hd-row">
                  <span class="card-hd">📊 本月额度</span>
                  <el-tag type="info" size="small" round>下月重置</el-tag>
                </div>
              </template>
              <div class="quota-grid">
                <div class="quota-block" v-for="q in quotaItems" :key="q.key">
                  <span class="q-icon">{{ q.icon }}</span>
                  <span class="q-name">{{ q.label }}</span>
                  <div class="q-bar">
                    <el-progress :percentage="getPercent(q.data)" :color="getColor(q.data)" :stroke-width="8" :show-text="false" />
                  </div>
                  <div class="q-num">
                    <span class="q-used">{{ q.data.used }}</span>
                    <span class="q-sep">/</span>
                    <span class="q-max">{{ q.data.max }}</span>
                    <span class="q-unit">{{ q.unit }}</span>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card class="section-card vip-card">
              <template #header>
                <div class="card-hd-row">
                  <span class="card-hd vip-hd">👑 我的会员权益</span>
                  <el-button link type="warning" class="vip-link" @click="showVipDialog = true">升级 →</el-button>
                </div>
              </template>
              <div class="vip-body">
                <div class="vip-crown">👑</div>
                <div class="vip-tier-name">{{ vipTier.name }}</div>
                <div class="vip-tier-desc">{{ vipTier.desc }}</div>
                <div class="vip-benefits">
                  <div class="benefit-item" v-for="(b, i) in vipTier.benefits" :key="i">
                    <span class="benefit-dot" />
                    <span>{{ b }}</span>
                  </div>
                  <div v-if="vipTier.benefits.length === 0" class="benefit-empty">升级 VIP 查看专属权益</div>
                </div>
                <el-button type="warning" size="large" round class="upgrade-btn" @click="showVipDialog = true">
                  升级 VIP
                </el-button>
              </div>
            </el-card>
          </div>
        </div>

        <!-- ===== VIP 购买弹窗 ===== -->
        <el-dialog append-to-body v-model="showVipDialog" title="选择 VIP 方案" width="720px" class="vip-dialog">
          <div class="vip-dialog-plans" v-if="vipPlans.length > 0">
            <div
              v-for="plan in vipPlans"
              :key="plan.id"
              :class="['vip-dialog-card', { featured: plan.featured }]"
              @click="selectedVipPlan = plan"
            >
              <div class="plan-icon">{{ plan.icon }}</div>
              <h3 class="plan-name">{{ plan.name }}</h3>
              <div class="plan-price">
                <span class="currency">¥</span>
                <span class="amount">{{ plan.price }}</span>
              </div>
              <el-divider />
              <ul class="plan-benefits">
                <li v-for="(b, bi) in plan.benefits.slice(0, 4)" :key="bi">
                  <span class="benefit-dot" />
                  <span>{{ b }}</span>
                </li>
              </ul>
              <el-button
                :type="plan.featured ? 'warning' : 'primary'"
                size="small"
                class="plan-select-btn"
                :loading="vipBuyLoading === plan.id"
                @click.stop="handleVipBuy(plan)"
              >
                {{ plan.featured ? '立即开通' : '选择' }}
              </el-button>
            </div>
          </div>
          <div v-else class="vip-dialog-empty">暂无可用方案</div>
        </el-dialog>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type UploadRequestOptions } from 'element-plus'
import { msgSuccess, msgError } from '../../utils/message'
import { UserFilled, Camera, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { userService } from '../../services/UserService'
import { getErrorMessage } from '../../utils/error'
import { getQuota, incrementAvatarUpload, setVipQuotaLimits } from '../../utils/quota'
import { vipService } from '../../services/VipService'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const formRef = ref<FormInstance>()
const loading = ref(false)
const updateLoading = ref(false)
const describeLoading = ref(false)
const uploadLoading = ref(false)
const dataLoaded = ref(false)

const quota = reactive({
  bgUpload: { max: 5, used: 0 },
  avatarUpload: { max: 3, used: 0 },
  goodsUpload: { max: 20, used: 0 },
  cartLimit: { max: 50, used: 0 },
})

const quotaItems = [
  { key: 'bgUpload', label: '背景上传', icon: '🖼️', unit: '次', data: quota.bgUpload },
  { key: 'goodsUpload', label: '商品上传', icon: '📦', unit: '次', data: quota.goodsUpload },
  { key: 'avatarUpload', label: '头像更新', icon: '👤', unit: '次', data: quota.avatarUpload },
  { key: 'cartLimit', label: '购物车', icon: '🛒', unit: '件', data: quota.cartLimit },
]

function getPercent(item: { max: number; used: number }) {
  return item.max > 0 ? Math.round((item.used / item.max) * 100) : 0
}

function getColor(item: { max: number; used: number }) {
  const pct = getPercent(item)
  if (pct >= 100) return '#f56c6c'
  if (pct >= 80) return '#e6a23c'
  return '#5b8def'
}

const vipTier = ref({ name: '普通用户', desc: '升级VIP享受更多特权', benefits: [] as string[], tier: 'free' })

const showVipDialog = ref(false)
const vipPlans = ref<{ id: number; level: number; name: string; icon: string; price: number; featured: boolean; benefits: string[] }[]>([])
const selectedVipPlan = ref<{ id: number; level: number; name: string; icon: string; price: number; benefits: string[] } | null>(null)
const vipBuyLoading = ref<number | false>(false)

function buildVipBenefits(config: { maxAddressQuantity?: number; monthlyUpdateGoods?: number; monthlyUpdateAvatar?: number; monthlyUpdateBackground?: number; maxCartQuantity?: number; maxGoodsQuantity?: number; maxAiQuantity?: number }): string[] {
  const b: string[] = []
  if (config.maxAddressQuantity && config.maxAddressQuantity > 0) b.push(`最多 ${config.maxAddressQuantity} 个收货地址`)
  if (config.monthlyUpdateGoods && config.monthlyUpdateGoods > 0) b.push(`每月 ${config.monthlyUpdateGoods} 次商品上传`)
  if (config.monthlyUpdateAvatar && config.monthlyUpdateAvatar > 0) b.push(`每月 ${config.monthlyUpdateAvatar} 次头像更新`)
  if (config.monthlyUpdateBackground && config.monthlyUpdateBackground > 0) b.push(`每月 ${config.monthlyUpdateBackground} 次背景更新`)
  if (config.maxCartQuantity && config.maxCartQuantity > 0) b.push(`购物车上限 ${config.maxCartQuantity} 件`)
  if (config.maxGoodsQuantity && config.maxGoodsQuantity > 0) b.push(`最多上架 ${config.maxGoodsQuantity} 件商品`)
  if (config.maxAiQuantity && config.maxAiQuantity > 0) b.push(`每月 ${config.maxAiQuantity} 次货物上传AI`)
  b.push('VIP 专属标识')
  return b
}

async function fetchVipLevel() {
  try {
    const res = await vipService.getVipConfig()
    if (res.code === 200 && res.data) {
      const configs = Array.isArray(res.data) ? res.data : (Object.values(res.data) as any[])
      const level = userLevel.value
      const currentConfig = configs.find((c: any) => c?.level === level) || (configs as Record<string, any>)[String(level)] || null

      if (currentConfig) {
        const name = currentConfig.levelName || (level > 0 ? `Lv.${level}` : '普通用户')
        const benefits = buildVipBenefits(currentConfig)
        vipTier.value = { name, desc: level > 0 ? `${name} · 尊享权益` : '普通用户权益', benefits, tier: level > 0 ? `lv${level}` : 'free' }
        if (level > 0) {
          setVipQuotaLimits({
            monthlyUpdateBackground: currentConfig.monthlyUpdateBackground,
            monthlyUpdateAvatar: currentConfig.monthlyUpdateAvatar,
            monthlyUpdateGoods: currentConfig.monthlyUpdateGoods,
            maxCartQuantity: currentConfig.maxCartQuantity,
          })
          fetchQuota()
        }
      }
      if (configs.length > 0) {
        const sorted = [...configs].sort((a, b) => a.level - b.level)
        vipPlans.value = sorted.map((config) => ({
          id: config.level,
          level: config.level,
          name: config.levelName || `Lv.${config.level}`,
          icon: '⭐',
          price: config.price,
          featured: config.level === 3,
          benefits: buildVipBenefits(config),
        }))
      }
    }
  } catch (e) {
    console.error('[fetchVipLevel] error:', e)
  }
}

async function handleVipBuy(plan: { id: number; level: number; name: string; price: number }) {
  vipBuyLoading.value = plan.id
  try {
    const res = await vipService.buyVip(plan.level, plan.price)
    if (res.code === 200) {
      msgSuccess(`已成功开通 ${plan.name}！`)
      showVipDialog.value = false
      await fetchUserInfo()
      fetchQuota()
      fetchVipLevel()
    } else {
      msgError(res.message || '购买失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    vipBuyLoading.value = false
  }
}

function fetchQuota() {
  const q = getQuota(quota.cartLimit.used)
  quota.bgUpload.max = q.bgUpload.max
  quota.avatarUpload.max = q.avatarUpload.max
  quota.goodsUpload.max = q.goodsUpload.max
  quota.cartLimit.max = q.cartLimit.max
}

async function fetchCartCount() {
  try {
    const res = await userService.getCartList({ page: 1, size: 1 })
    if (res.code === 200) quota.cartLimit.used = res.data.total
  } catch { /* ignore */ }
}

function goLogin() { router.push('/login') }

const updateForm = reactive({
  account: '', userName: '', email: '', phone: '', balance: 0, avatarPath: '', describe: '', delete: false,
})

const userLevel = ref(0)
const userLevelName = ref('普通用户')
const vipRemainingTime = ref(0)

const imageUrl = ref('')

async function loadAvatar(imagePath: string | undefined) {
  if (!imagePath) {
    imageUrl.value = ''
    return
  }
  try {
    const res = await userService.getImages([imagePath])
    if (res.code === 200 && res.data) {
      const keys = Object.keys(res.data)
      imageUrl.value = keys.length > 0 ? (res.data[imagePath] || imagePath) : imagePath
    } else {
      imageUrl.value = imagePath
    }
  } catch {
    imageUrl.value = imagePath
  }
}

async function loadBackgrounds() {
  const paths = backgroundList.value.map(bg => bg.imagePath).filter(Boolean)
  if (paths.length === 0) {
    console.warn('[UserInfo] 背景列表为空，新API未返回 backgrounds 字段')
    return
  }
  try {
    const res = await userService.getImages(paths)
    if (res.code === 200 && res.data) {
      backgroundList.value.forEach(bg => {
        const url = res.data[bg.imagePath]
        if (url) bg.imagePath = url
      })
    }
  } catch (e) {
    console.error('[UserInfo] 背景加载失败:', e)
  }
}

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '2-20个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

function beforeUpload(file: File) {
  if (!file.type.startsWith('image/')) { msgError('只能上传图片文件!'); return false }
  if (file.size / 1024 / 1024 > 5) { msgError('图片大小不能超过5MB!'); return false }
  return true
}

async function handleUpload(options: UploadRequestOptions) {
  uploadLoading.value = true
  try {
    const res = await userService.updateImage(options.file)
    if (res.code === 200) {
      incrementAvatarUpload()
      msgSuccess('头像上传成功')
      userStore.markDirty()
      await fetchUserInfo()
      fetchQuota()
    } else {
      msgError(res.message || '头像上传失败')
    }
  } catch (e: unknown) { handleError(e) } finally { uploadLoading.value = false }
}

async function fetchUserInfo() {
  loading.value = true
  try {
    const info = await userStore.fetchUserInfoVO()
    if (info) {
      updateForm.account = info.account || ''
      updateForm.userName = info.userName || ''
      updateForm.email = info.email || ''
      updateForm.phone = info.phone || ''
      updateForm.balance = info.balance ?? 0
      updateForm.describe = info.describe || ''
      updateForm.avatarPath = info.avatarPath || ''
      updateForm.delete = false
      userLevel.value = info.level ?? 0
      userLevelName.value = info.vipLevelName || '普通用户'
      vipRemainingTime.value = info.vipRemainingTime ?? 0
      quota.bgUpload.used = info.uploadBackground ?? 0
      quota.avatarUpload.used = info.updateAvatar ?? 0
      quota.goodsUpload.used = info.uploadGoods ?? 0
      setVipQuotaLimits({
        monthlyUpdateBackground: info.monthlyUpdateBackground,
        monthlyUpdateAvatar: info.monthlyUpdateAvatar,
        monthlyUpdateGoods: info.monthlyUpdateGoods,
        maxCartQuantity: info.maxCartQuantity,
      })
      if (info.backgrounds && Array.isArray(info.backgrounds)) {
        backgroundList.value = info.backgrounds.map((bg) => ({
          id: bg.id,
          imagePath: bg.imagePath,
          sequence: bg.sequence
        }))
      } else {
        backgroundList.value = []
      }
      await loadAvatar(info.avatarPath)
      await loadBackgrounds()
    }
  } catch (e: unknown) { handleError(e) } finally { loading.value = false }
}

async function handleUpdate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  updateLoading.value = true
  try {
    const res = await userService.update({ userName: updateForm.userName, email: updateForm.email, phone: updateForm.phone })
    if (res.code === 200) {
      msgSuccess('更新成功')
      userStore.markDirty()
      await userStore.fetchUserInfo()
    } else {
      msgError(res.message || '更新失败')
    }
  } catch (e: unknown) { handleError(e) } finally { updateLoading.value = false }
}

async function handleSaveDescribe() {
  describeLoading.value = true
  try {
    const res = await userService.updateDescribe(updateForm.describe)
    if (res.code === 200) {
      msgSuccess('简介已保存')
      userStore.markDirty()
    } else {
      msgError(res.message || '保存失败')
    }
  } catch (e: unknown) { handleError(e) } finally { describeLoading.value = false }
}

const backgroundList = ref<{ id?: number; imagePath: string; sequence: number }[]>([])
const bgUploadLoading = ref(false)
const uploadingSlot = ref(0)

function getSlotBg(slot: number) {
  return backgroundList.value.find(b => b.sequence === slot)
}

async function handleSlotUpload(options: UploadRequestOptions, slot: number) {
  bgUploadLoading.value = true
  uploadingSlot.value = slot
  try {
    const res = await userService.uploadImage(options.file)
    if (res.code === 200 && res.data) {
      const addRes = await userService.addBackground({ imagePath: res.data, sequence: slot })
      if (addRes.code === 200) {
        msgSuccess('背景上传成功')
        userStore.markDirty()
        const existing = backgroundList.value.findIndex(b => b.sequence === slot)
        if (existing >= 0) {
          backgroundList.value[existing] = { id: backgroundList.value[existing]!.id, imagePath: res.data, sequence: slot }
        } else {
          backgroundList.value.push({ imagePath: res.data, sequence: slot })
        }
        await fetchUserInfo()
      } else {
        msgError(addRes.message || '添加失败')
      }
    } else {
      msgError(res.message || '上传失败')
    }
  } catch (e: unknown) { handleError(e) } finally { bgUploadLoading.value = false; uploadingSlot.value = 0 }
}

function handleError(e: unknown) {
  const msg = getErrorMessage(e)
  if (msg.includes('token无效') || msg.includes('已过期')) {
    msgError('登录已过期，请重新登录')
    userStore.logout()
    router.push('/login')
  } else if (msg.includes('其他设备登录')) {
    msgError('账号已在其他设备登录，请重新登录')
    userStore.logout()
    router.push('/login')
  } else {
    msgError(msg)
  }
}

async function loadUserData() {
  if (dataLoaded.value) return
  dataLoaded.value = true
  await fetchUserInfo()
  fetchQuota()
  fetchCartCount()
  fetchVipLevel()
}

onMounted(() => {
  if (isLoggedIn.value) loadUserData()
})

watch(isLoggedIn, (val) => {
  if (val) loadUserData()
})
</script>

<style scoped>
/* ===== 未登录 ===== */
.not-logged-in {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 560px;
}

.nl-card {
  text-align: center;
  padding: 60px 80px;
  border-radius: 24px;
  background: linear-gradient(145deg, rgba(16,18,26,0.6), rgba(22,26,42,0.4));
  border: 1px solid rgba(255,255,255,0.08);
  backdrop-filter: blur(20px);
  box-shadow:
    0 4px 24px rgba(0,0,0,0.3),
    0 0 80px rgba(91,141,239,0.06),
    inset 0 1px 0 rgba(255,255,255,0.04);
  animation: floatIn 0.6s ease-out;
}

@keyframes floatIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.nl-icon { font-size: 56px; margin-bottom: 16px; animation: pulse 2s infinite; }
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
.nl-text { font-size: 22px; font-weight: 700; color: #e0e3ea; margin: 0 0 8px; }
.nl-sub { font-size: 14px; color: #6b7280; margin: 0 0 24px; }
.nl-btn {
  padding: 12px 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #5b8def, #7c5ce7) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(91,141,239,0.35);
  transition: all .3s;
}
.nl-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(91,141,239,0.45);
}

/* ===== 主页面 ===== */
.info-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 顶部横幅 ===== */
.profile-banner {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 8px 40px rgba(0,0,0,0.3),
    0 0 80px rgba(99,102,241,0.12);
}
.banner-bg {
  position: absolute;
  inset: 0;
  background:
    url('https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=1200&q=80') center/cover no-repeat;
}
.banner-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 160% 100% at 10% 20%, rgba(99,102,241,0.35), transparent 50%),
    radial-gradient(ellipse 140% 90% at 85% 10%, rgba(168,85,247,0.3), transparent 50%),
    radial-gradient(ellipse 120% 70% at 50% 90%, rgba(59,130,246,0.22), transparent 50%),
    radial-gradient(ellipse 90% 60% at 30% 50%, rgba(236,72,153,0.12), transparent 50%),
    linear-gradient(160deg, rgba(15,15,26,0.65) 0%, rgba(26,16,48,0.55) 25%, rgba(21,27,53,0.6) 55%, rgba(15,20,32,0.7) 100%);
}
.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 3px,
      rgba(255,255,255,0.008) 3px,
      rgba(255,255,255,0.008) 6px
    ),
    linear-gradient(180deg, rgba(255,255,255,0.02) 0%, transparent 40%, transparent 60%, rgba(255,255,255,0.01) 100%);
  pointer-events: none;
}
.banner-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 48px;
  padding: 44px 48px;
}

/* 头像 */
.avatar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.avatar-uploader :deep(.el-upload) { border: none; display: block; }

.avatar-ring {
  width: 128px;
  height: 128px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.12);
  background: linear-gradient(145deg, rgba(255,255,255,0.08), rgba(255,255,255,0.03));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  transition: all .35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow:
    0 0 0 6px rgba(99,102,241,0.08),
    0 4px 24px rgba(0,0,0,0.35),
    inset 0 2px 4px rgba(255,255,255,0.05);
}
.avatar-ring:hover {
  border-color: rgba(99,102,241,0.6);
  box-shadow:
    0 0 0 8px rgba(99,102,241,0.15),
    0 0 48px rgba(99,102,241,0.3),
    0 8px 32px rgba(0,0,0,0.4);
  transform: scale(1.05) translateY(-2px);
}
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.avatar-icon { color: rgba(255,255,255,0.12); }

.avatar-edit {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #151b35;
  opacity: 0;
  transition: all .3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(99,102,241,0.5);
}
.avatar-ring:hover .avatar-edit {
  opacity: 1;
  transform: scale(1);
}
.upload-hint { font-size: 12px; color: rgba(255,255,255,0.45); margin-top: 10px; }

/* 信息卡片 */
.info-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
.info-cell {
  padding: 20px 22px;
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
  border: 1px solid rgba(255,255,255,0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: all .35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
}
.info-cell::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(99,102,241,0.06), rgba(168,85,247,0.03));
  opacity: 0;
  transition: opacity .4s;
  pointer-events: none;
}
.info-cell:hover {
  background: linear-gradient(145deg, rgba(255,255,255,0.09), rgba(255,255,255,0.04));
  border-color: rgba(255,255,255,0.15);
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.25), 0 0 20px rgba(99,102,241,0.08);
}
.info-cell:hover::before { opacity: 1; }
.info-cell-balance {
  background: linear-gradient(145deg, rgba(99,102,241,0.12), rgba(168,85,247,0.06));
  border-color: rgba(99,102,241,0.22);
}
.info-cell-balance:hover {
  box-shadow: 0 12px 36px rgba(99,102,241,0.18), 0 0 24px rgba(99,102,241,0.1);
}
.cell-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: rgba(255,255,255,0.4);
  font-weight: 600;
}
.cell-val {
  font-size: 18px;
  font-weight: 700;
  color: #e8eaed;
  letter-spacing: 0.3px;
}
.cell-val-balance {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, #818cf8, #c084fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ===== 下方卡片 ===== */
.bottom-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 18px;
}

.section-card {
  background: rgba(18, 20, 32, 0.6) !important;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  transition: all .3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}
.section-card:hover {
  border-color: rgba(255, 255, 255, 0.13) !important;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.25);
  transform: translateY(-1px);
}

.card-hd {
  font-size: 14px;
  font-weight: 600;
  color: #c8cdd5;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.3px;
}

.card-hd-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-card :deep(.el-card__header) {
  padding: 14px 20px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  background: transparent;
}
.section-card :deep(.el-card__body) {
  padding: 16px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* ===== 表单 ===== */
.form-row {
  display: flex;
  flex-direction: column;
}
.info-form {
  display: flex;
  align-items: flex-end;
  gap: 14px;
}
.form-fields {
  flex: 1;
  display: flex;
  gap: 14px;
}
.form-fields :deep(.el-form-item) {
  margin-bottom: 0;
  flex: 1;
}
.form-fields :deep(.el-form-item__label) {
  font-size: 12px;
  padding-bottom: 5px;
  color: #8892a0;
  font-weight: 500;
}
.form-card-full :deep(.el-divider) {
  margin: 12px 0;
  border-color: rgba(255, 255, 255, 0.05);
}

.desc-inline {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.desc-label {
  font-size: 12px;
  color: #8892a0;
  font-weight: 500;
  white-space: nowrap;
  padding-top: 8px;
  min-width: 60px;
}
.desc-inline :deep(.el-textarea) {
  flex: 1;
}
.desc-save {
  flex-shrink: 0;
  align-self: flex-end;
  background: linear-gradient(135deg, #6366f1, #8b5cf6) !important;
  border: none !important;
  font-weight: 600;
  letter-spacing: 0.3px;
  border-radius: 8px;
  transition: all .25s;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
  margin-bottom: 0;
}
.desc-save:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.3);
}

.form-card-full :deep(.el-input__wrapper),
.form-card-full :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.045) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  box-shadow: none !important;
  border-radius: 8px;
  transition: all .25s;
}
.form-card-full :deep(.el-input__wrapper:hover),
.form-card-full :deep(.el-textarea__inner:hover) {
  border-color: rgba(99, 102, 241, 0.35) !important;
  background: rgba(255, 255, 255, 0.06) !important;
}
.form-card-full :deep(.el-input__wrapper.is-focus),
.form-card-full :deep(.el-textarea__inner:focus) {
  border-color: #6366f1 !important;
  background: rgba(255, 255, 255, 0.06) !important;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1) !important;
}
.form-card-full :deep(.el-input__inner),
.form-card-full :deep(.el-textarea__inner) {
  color: #c8cdd5;
  font-size: 13px;
}
.form-card-full :deep(.el-input__inner::placeholder),
.form-card-full :deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.2);
}

.save-btn {
  padding: 9px 22px;
  font-size: 13px;
  border-radius: 8px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6) !important;
  border: none !important;
  font-weight: 600;
  letter-spacing: 0.4px;
  transition: all .25s;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}
.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.35);
}

/* ===== 额度 ===== */
.quota-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  flex: 1;
}

.quota-block {
  text-align: center;
  padding: 14px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: all .3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}
.quota-block::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 50%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.35), transparent);
  opacity: 0;
  transition: opacity .3s;
}
.quota-block:hover {
  background: rgba(255, 255, 255, 0.055);
  border-color: rgba(255, 255, 255, 0.12);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
}
.quota-block:hover::after { opacity: 1; }
.q-icon { font-size: 20px; }
.q-name { font-size: 11px; color: #8892a0; font-weight: 500; }
.q-bar { width: 100%; padding: 0 2px; }
.q-bar :deep(.el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 5px;
}
.q-bar :deep(.el-progress-bar__inner) {
  border-radius: 5px;
  transition: width 0.6s ease;
}
.q-num { display: flex; align-items: baseline; gap: 2px; }
.q-used { font-size: 17px; font-weight: 700; color: #dde1e8; }
.q-sep { font-size: 11px; color: #4a5070; margin: 0 2px; }
.q-max { font-size: 12px; color: #7a8296; }
.q-unit { font-size: 10px; color: #7a8296; margin-left: 3px; }

/* ===== VIP ===== */
.vip-card {
  background: linear-gradient(165deg, rgba(36, 26, 56, 0.55), rgba(20, 24, 40, 0.5)) !important;
  backdrop-filter: blur(16px);
  border: 1px solid rgba(212, 175, 55, 0.22) !important;
  position: relative;
}
.vip-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.5), transparent);
}

.vip-hd {
  color: #d4af37;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.25);
}

.vip-link {
  font-size: 12px;
  color: #d4af37;
  text-decoration: none;
  opacity: .7;
  transition: all .2s;
  font-weight: 500;
}
.vip-link:hover {
  opacity: 1;
  text-shadow: 0 0 12px rgba(212, 175, 55, 0.4);
}

.vip-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.vip-crown {
  font-size: 40px;
  filter: drop-shadow(0 4px 14px rgba(212, 175, 55, 0.3));
  animation: crownFloat 3s ease-in-out infinite;
}
@keyframes crownFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
.vip-tier-name {
  font-size: 17px;
  font-weight: 700;
  color: #e5e6e8;
}
.vip-tier-desc { font-size: 11px; color: #8892a0; margin-top: -6px; }

.vip-benefits {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(212, 175, 55, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #b0b8c8;
  transition: all .2s;
}
.benefit-item:hover {
  color: #d4af37;
  transform: translateX(4px);
}

.benefit-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4af37, #f0d77a);
  flex-shrink: 0;
  box-shadow: 0 0 6px rgba(212, 175, 55, 0.4);
}

.benefit-empty {
  font-size: 12px;
  color: #8892a0;
  text-align: center;
  padding: 8px 0;
}

.upgrade-btn {
  width: 100%;
  margin-top: auto;
  background: linear-gradient(135deg, #d4af37 0%, #b8962e 50%, #d4af37 100%) !important;
  background-size: 200% 100% !important;
  border: none !important;
  font-weight: 700 !important;
  letter-spacing: 1.5px;
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.25);
  transition: all .3s;
  border-radius: 12px;
}
.upgrade-btn:hover {
  background-position: 100% 0 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(212, 175, 55, 0.35);
}

/* ===== 背景管理 ===== */
.bg-slots {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.bg-slot {
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.07);
  background: rgba(255, 255, 255, 0.025);
  overflow: hidden;
  transition: all .3s;
}
.bg-slot:hover {
  border-color: rgba(255, 255, 255, 0.13);
  transform: translateY(-2px);
}
.slot-label {
  font-size: 10px;
  color: #8892a0;
  padding: 6px 10px 0;
  font-weight: 500;
}
.slot-img-wrap {
  position: relative;
  aspect-ratio: 16/9;
  margin: 4px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.slot-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.slot-img-wrap:hover .slot-img { transform: scale(1.05); }
.slot-empty {
  aspect-ratio: 16/9;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px dashed rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  margin: 4px;
  background: rgba(255, 255, 255, 0.015);
  color: rgba(255, 255, 255, 0.15);
}
.slot-upload {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

/* ===== VIP 弹窗 ===== */
.vip-dialog :deep(.el-dialog) {
  background: linear-gradient(160deg, rgba(26,29,46,0.7), rgba(20,24,38,0.6));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}
.vip-dialog :deep(.el-dialog__title) { color: #e0e3ea; font-weight: 600; }
.vip-dialog :deep(.el-dialog__headerbtn .el-dialog__close) { color: #6b7280; }

.vip-dialog-plans {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 18px;
}

.vip-dialog-card {
  position: relative;
  text-align: center;
  padding: 28px 18px;
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(255,255,255,0.04), rgba(255,255,255,0.01));
  border: 2px solid rgba(255,255,255,0.07);
  cursor: pointer;
  transition: all .35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.vip-dialog-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(212,175,55,0.5), transparent);
  opacity: 0;
  transition: opacity .3s;
}
.vip-dialog-card:hover {
  border-color: rgba(212,175,55,0.35);
  background: linear-gradient(145deg, rgba(255,255,255,0.06), rgba(255,255,255,0.03));
  transform: translateY(-5px);
  box-shadow: 0 12px 36px rgba(0,0,0,0.3);
}
.vip-dialog-card:hover::before { opacity: 1; }
.vip-dialog-card.featured {
  border-color: rgba(212,175,55,0.5);
  background: linear-gradient(165deg, rgba(212,175,55,0.1), rgba(212,175,55,0.03));
  box-shadow: 0 0 40px rgba(212,175,55,0.1);
}
.plan-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #d4af37, #b8962e);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 5px 14px;
  border-radius: 0 14px 0 14px;
  box-shadow: 0 2px 8px rgba(212,175,55,0.3);
}
.plan-icon { font-size: 36px; margin-bottom: 10px; }
.plan-name { font-size: 16px; font-weight: 600; color: #e0e3ea; margin: 0 0 10px; }
.plan-price { margin-bottom: 6px; }
.plan-price .currency { font-size: 17px; color: #d4af37; font-weight: 600; }
.plan-price .amount { font-size: 32px; color: #fff; font-weight: 800; text-shadow: 0 2px 8px rgba(0,0,0,0.3); }
.vip-dialog-card .plan-benefits {
  list-style: none;
  padding: 0;
  margin: 0 0 18px;
  text-align: left;
}
.vip-dialog-card .plan-benefits li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #8890a0;
  padding: 4px 0;
  transition: all .2s;
}
.vip-dialog-card .plan-benefits li:hover { color: #b0b8c8; transform: translateX(4px); }
.plan-select-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
}

.vip-dialog-empty {
  text-align: center;
  padding: 48px;
  color: #6b7280;
  font-size: 14px;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .bottom-row { grid-template-columns: 1fr 1fr; }
  .info-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 900px) {
  .bottom-row { grid-template-columns: 1fr; }
  .banner-inner { gap: 32px; padding: 36px 32px; }
  .avatar-ring { width: 110px; height: 110px; }
  .info-form { flex-direction: column; align-items: stretch; }
  .form-fields { flex-direction: column; gap: 0; }
  .desc-inline { flex-direction: column; }
  .desc-save { align-self: stretch; }
}
@media (max-width: 700px) {
  .bottom-row { grid-template-columns: 1fr; }
  .banner-inner { flex-direction: column; gap: 28px; padding: 32px 24px; text-align: center; }
  .info-grid { grid-template-columns: 1fr; }
  .quota-grid { grid-template-columns: 1fr 1fr; }
  .save-btn { width: 100%; }
}
</style>