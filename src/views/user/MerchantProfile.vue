<template>
  <div class="merchant-profile" v-loading="loading">
    <el-button class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <!-- 封面信息区 -->
    <div class="cover-section">
      <div
        class="cover-bg"
        :style="heroBgUrl ? { backgroundImage: `url(${heroBgUrl})` } : {}"
      />
      <div class="cover-overlay" />
      <div class="cover-content">
        <div class="avatar-box">
          <img v-if="merchantAvatar" :src="merchantAvatar" class="avatar-img" />
          <el-icon v-else :size="40"><UserFilled /></el-icon>
        </div>
        <div class="cover-center">
          <h2 class="cover-name">{{ profile.userName }}</h2>
          <p class="cover-addr" v-if="firstGoods?.address">📍 {{ firstGoods.address }}</p>
          <div class="cover-tags">
            <span class="tag-vip" v-if="profile.level > 0">
              <span class="vip-crown">👑</span>
              VIP{{ profile.level }} · {{ profile.levelName }}
            </span>
            <span class="tag-goods">🛍️ {{ goodsList.length }} 件商品</span>
          </div>
        </div>
        <div class="vip-card" v-if="profile.level > 0">
          <div class="vip-shine" />
          <div class="vip-badge">VIP{{ profile.level }}</div>
          <div class="vip-name">{{ profile.levelName }}</div>
          <div class="vip-sub">尊享会员</div>
        </div>
      </div>
    </div>

    <!-- 个人简介 -->
    <div class="section" v-if="profile.describe">
      <h3 class="section-title">📝 个人简介</h3>
      <div class="intro-box">
        <span class="desc-quote">"</span>
        <p class="intro-text">{{ profile.describe }}</p>
        <span class="desc-quote end">"</span>
      </div>
    </div>

    <!-- 背景图片区 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">🖼️ 背景图片</h3>
        <div class="bg-nav" v-if="bgImages.length > 4">
          <button class="nav-btn" @click="bgPrev" :disabled="bgOffset === 0">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <span class="nav-info">{{ bgOffset + 1 }}-{{ Math.min(bgOffset + 4, bgImages.length) }} / {{ bgImages.length }}</span>
          <button class="nav-btn" @click="bgNext" :disabled="bgOffset + 4 >= bgImages.length">
            <el-icon><ArrowRight /></el-icon>
          </button>
          <button class="nav-btn auto-btn" :class="{ active: bgAutoPlay }" @click="toggleAutoPlay">
            {{ bgAutoPlay ? '⏸' : '▶' }}
          </button>
        </div>
      </div>
      <div class="bg-gallery">
        <div
          v-for="slot in 4"
          :key="slot"
          class="bg-card"
          :class="{ empty: !displayBgSlots[slot - 1]! }"
        >
          <template v-if="displayBgSlots[slot - 1]!">
            <img :src="resolveImg(displayBgSlots[slot - 1]!.imagePath)" class="bg-card-img" />
            <div class="bg-card-seq">{{ displayBgSlots[slot - 1]!.sequence }}</div>
          </template>
          <template v-else>
            <div class="bg-placeholder">
              <el-icon :size="28"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 在售商品 -->
    <div class="section">
      <h3 class="section-title">
        🛍️ 在售商品
        <span class="goods-total">共 {{ goodsList.length }} 件</span>
      </h3>
      <div class="goods-grid" v-if="goodsList.length > 0">
        <el-card
          class="goods-card"
          shadow="hover"
          v-for="g in goodsList"
          :key="g.id"
        >
          <div class="goods-img-wrap">
            <img v-if="g.imagePath" :src="resolveImg(g.imagePath)" class="goods-img" />
            <div v-else class="goods-img-placeholder">{{ g.goodsName.charAt(0) }}</div>
            <div class="goods-img-overlay" />
          </div>
          <div class="goods-info">
            <h3 class="goods-name">{{ g.goodsName }}</h3>
            <p class="goods-desc" v-if="g.describe">{{ g.describe }}</p>
            <div class="goods-price-row">
              <span class="goods-price">¥{{ g.goodsPrice.toFixed(2) }}</span>
              <span class="goods-stock">库存 {{ g.goodsStock }}</span>
            </div>
            <div class="goods-cart-row" v-if="g.cartQuantity > 0">
              <span class="goods-cart-badge">🛒 已加 {{ g.cartQuantity }}</span>
            </div>
            <el-divider class="goods-divider" />
            <div class="goods-actions">
              <div class="qty-control">
                <button class="qty-btn" @click.stop="decreaseQty(g)">−</button>
                <span class="qty-num" :class="{ active: g.cartQuantity > 0, submitting: submitting[g.id] }">{{ g.cartQuantity }}</span>
                <button class="qty-btn" @click.stop="increaseQty(g)">+</button>
              </div>
              <span class="auto-hint" v-if="submitting[g.id]">⏳</span>
            </div>
          </div>
        </el-card>
      </div>
      <div v-else class="empty-wrap">
        <el-empty description="暂无在售商品" />
      </div>
    </div>

    <el-dialog append-to-body v-model="confirmVisible" title="确认移除" width="360px">
      <p>确认从购物车中移除该商品吗？</p>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRemove">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { msgSuccess, msgError } from '../../utils/message'
import { ArrowLeft, ArrowRight, UserFilled, Picture } from '@element-plus/icons-vue'
import { userService } from '../../services/UserService'
import { getErrorMessage } from '../../utils/error'
import type { MerchantGoodsInfo, MerchantBgInfo, MerchantProfileInfo } from '../../services/UserService'

const route = useRoute()
const router = useRouter()
const userId = Number(route.params.userId)
const loading = ref(false)
const confirmVisible = ref(false)
const pendingItem = ref<MerchantGoodsInfo | null>(null)
const submitting = reactive<Record<number, boolean>>({})
const cartTimers = reactive<Record<number, number>>({})
const imageMap = ref<Record<string, string>>({})
const bgOffset = ref(0)
const bgAutoPlay = ref(false)
let bgAutoTimer: ReturnType<typeof setInterval> | null = null

const profile = ref<MerchantProfileInfo>({
  userName: '',
  describe: '',
  level: 0,
  levelName: '',
  backgrounds: [],
  goods: [],
})

const bgImages = computed(() =>
  [...(profile.value.backgrounds || [])].sort((a, b) => a.sequence - b.sequence)
)
const goodsList = computed(() => profile.value.goods || [])
const firstGoods = computed(() => goodsList.value[0] || null)
const merchantAvatar = computed(() => {
  const raw = firstGoods.value?.merchantAvatar || ''
  return imageMap.value[raw] || raw
})
const heroBgUrl = computed(() => {
  const first = bgImages.value[0]
  if (!first) return ''
  return imageMap.value[first.imagePath] || ''
})

const displayBgSlots = computed<(MerchantBgInfo | null)[]>(() => {
  const slots: (MerchantBgInfo | null)[] = [null, null, null, null]
  const imgs = bgImages.value
  for (let i = 0; i < 4; i++) {
    const idx = bgOffset.value + i
    if (idx < imgs.length) {
      slots[i] = imgs[idx] ?? null
    }
  }
  return slots
})

function resolveImg(path: string): string {
  return imageMap.value[path] || path
}

function bgPrev() {
  if (bgOffset.value > 0) bgOffset.value -= 4
}

function bgNext() {
  if (bgOffset.value + 4 < bgImages.value.length) bgOffset.value += 4
}

function toggleAutoPlay() {
  bgAutoPlay.value = !bgAutoPlay.value
  if (bgAutoPlay.value) {
    startAutoPlay()
  } else {
    stopAutoPlay()
  }
}

function startAutoPlay() {
  stopAutoPlay()
  bgAutoTimer = setInterval(() => {
    if (bgOffset.value + 4 >= bgImages.value.length) {
      bgOffset.value = 0
    } else {
      bgOffset.value += 4
    }
  }, 4000)
}

function stopAutoPlay() {
  if (bgAutoTimer) {
    clearInterval(bgAutoTimer)
    bgAutoTimer = null
  }
}

watch(() => bgImages.value.length, (len) => {
  if (len <= 4) {
    bgOffset.value = 0
    bgAutoPlay.value = false
    stopAutoPlay()
  }
})

async function fetchMerchant() {
  if (!userId) return
  loading.value = true
  try {
    const res = await userService.getMerchantBackground(userId)
    if (res.code === 200 && res.data) {
      profile.value = res.data

      const paths: string[] = []
      profile.value.backgrounds.forEach((bg) => {
        if (bg.imagePath) paths.push(bg.imagePath)
      })
      profile.value.goods.forEach((g) => {
        if (g.imagePath) paths.push(g.imagePath)
        if (g.merchantAvatar) paths.push(g.merchantAvatar)
      })
      const uniquePaths = [...new Set(paths.filter(Boolean))]
      if (uniquePaths.length > 0) {
        const imgRes = await userService.getImages(uniquePaths)
        if (imgRes.code === 200 && imgRes.data) {
          imageMap.value = imgRes.data
        }
      }
    } else {
      msgError(res.message || '获取商家信息失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function increaseQty(item: MerchantGoodsInfo) {
  item.cartQuantity += 1
  scheduleCartSubmit(item)
}

function decreaseQty(item: MerchantGoodsInfo) {
  if (item.cartQuantity <= 1) {
    pendingItem.value = item
    confirmVisible.value = true
    return
  }
  item.cartQuantity -= 1
  scheduleCartSubmit(item)
}

async function confirmRemove() {
  if (!pendingItem.value) return
  const item = pendingItem.value
  confirmVisible.value = false
  pendingItem.value = null
  try {
    const res = await userService.handleCart({ goodId: item.id, num: 0 })
    if (res.code === 200) {
      item.cartQuantity = 0
      item.inCart = false
      msgSuccess('已从购物车移除')
    } else {
      msgError(res.message || '操作失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

function scheduleCartSubmit(item: MerchantGoodsInfo) {
  const id = item.id
  if (cartTimers[id]) clearTimeout(cartTimers[id])
  cartTimers[id] = window.setTimeout(() => {
    delete cartTimers[id]
    submitCartChange(item)
  }, 300)
}

async function submitCartChange(item: MerchantGoodsInfo) {
  const id = item.id
  if (submitting[id]) return
  submitting[id] = true
  try {
    const res = await userService.handleCart({ goodId: item.id, num: item.cartQuantity })
    if (res.code === 200) {
      item.inCart = item.cartQuantity > 0
      if (item.cartQuantity === 0) {
        msgSuccess('已从购物车移除')
      } else if (item.cartQuantity === 1) {
        msgSuccess('加入购物车成功')
      }
    } else {
      msgError(res.message || '操作失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    submitting[id] = false
  }
}

onMounted(() => {
  fetchMerchant()
})

onUnmounted(() => {
  Object.keys(cartTimers).forEach((k) => clearTimeout(cartTimers[Number(k)]))
  stopAutoPlay()
})
</script>

<style scoped>
.merchant-profile {
  max-width: 960px;
  margin: 0 auto;
  animation: pageIn 0.5s ease-out;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.back-btn {
  margin-bottom: 14px;
  font-size: 14px;
  color: #c0c4cc;
  background: rgba(26, 28, 36, 0.35);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  padding: 8px 16px;
  transition: all 0.2s;
}

.back-btn:hover {
  color: #5b8def;
  border-color: #5b8def;
  background: rgba(91, 141, 239, 0.1);
}

/* ===== Cover Section ===== */
.cover-section {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  margin-bottom: 28px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3), 0 0 80px rgba(91, 141, 239, 0.06);
}

.cover-bg {
  height: 240px;
  background: linear-gradient(135deg, #0f1729 0%, #1a2744 30%, #2d3a6e 60%, #1a1f36 100%);
  background-size: cover;
  background-position: center;
  transition: background-image 0.8s ease;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 41, 0.15) 0%, rgba(15, 23, 41, 0.55) 50%, rgba(15, 23, 41, 0.92) 100%);
  pointer-events: none;
}

.cover-content {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 40px;
}

.avatar-box {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b8def, #3d6fd6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  overflow: hidden;
  flex-shrink: 0;
  border: 3px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.4), 0 0 50px rgba(91, 141, 239, 0.25);
  animation: avatarGlow 3s ease-in-out infinite;
}

@keyframes avatarGlow {
  0%, 100% { box-shadow: 0 4px 24px rgba(91, 141, 239, 0.3), 0 0 50px rgba(91, 141, 239, 0.15); }
  50% { box-shadow: 0 4px 30px rgba(91, 141, 239, 0.5), 0 0 60px rgba(91, 141, 239, 0.3); }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-center {
  flex: 1;
  min-width: 0;
}

.cover-name {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
  letter-spacing: 0.5px;
}

.cover-addr {
  margin: 0 0 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
}

.cover-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tag-vip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15), rgba(230, 162, 60, 0.05));
  border: 1px solid rgba(230, 162, 60, 0.3);
  color: #e6a23c;
  font-size: 12px;
  font-weight: 600;
}

.vip-crown {
  font-size: 14px;
}

.tag-goods {
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(91, 141, 239, 0.1);
  border: 1px solid rgba(91, 141, 239, 0.2);
  color: #8bb4f0;
  font-size: 12px;
  font-weight: 500;
}

.vip-card {
  width: 130px;
  height: 78px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2a1a0a 0%, #3d2a10 50%, #2a1a0a 100%);
  border: 1px solid rgba(230, 162, 60, 0.35);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 24px rgba(230, 162, 60, 0.12);
  position: relative;
  overflow: hidden;
}

.vip-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent 40%, rgba(230, 162, 60, 0.08) 50%, transparent 60%);
  animation: vipShine 4s ease-in-out infinite;
}

@keyframes vipShine {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

.vip-badge {
  font-size: 11px;
  font-weight: 700;
  color: #e6a23c;
  letter-spacing: 2px;
  position: relative;
  z-index: 1;
}

.vip-name {
  font-size: 14px;
  font-weight: 600;
  color: #f0d48a;
  margin: 2px 0;
  position: relative;
  z-index: 1;
}

.vip-sub {
  font-size: 10px;
  color: #9a8a6a;
  letter-spacing: 1px;
  position: relative;
  z-index: 1;
}

/* ===== Section ===== */
.section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #e0e3ea;
}

.goods-total {
  margin-left: auto;
  font-size: 13px;
  font-weight: 400;
  color: #505868;
}

/* ===== BG Nav ===== */
.bg-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-btn {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(26, 28, 36, 0.35);
  backdrop-filter: blur(8px);
  color: #c0c4cc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  padding: 0;
  font-size: 12px;
}

.nav-btn:hover:not(:disabled) {
  border-color: #5b8def;
  color: #5b8def;
  background: rgba(91, 141, 239, 0.1);
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.auto-btn.active {
  background: rgba(91, 141, 239, 0.2);
  border-color: #5b8def;
  color: #5b8def;
}

.nav-info {
  font-size: 12px;
  color: #8890a0;
  min-width: 80px;
  text-align: center;
}

/* ===== BG Gallery ===== */
.bg-gallery {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.bg-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  aspect-ratio: 16 / 10;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(26, 28, 36, 0.35);
}

.bg-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.35);
  border-color: rgba(91, 141, 239, 0.3);
}

.bg-card.empty {
  border-style: dashed;
  border-color: rgba(255, 255, 255, 0.08);
}

.bg-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s;
}

.bg-card:hover .bg-card-img {
  transform: scale(1.08);
}

.bg-card-seq {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 24px;
  height: 24px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  color: #e6a23c;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.bg-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.12);
  font-size: 12px;
}

/* ===== Intro ===== */
.intro-box {
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  position: relative;
  backdrop-filter: blur(8px);
}

.desc-quote {
  font-size: 36px;
  color: #5b8def;
  font-family: Georgia, serif;
  line-height: 1;
  position: absolute;
  opacity: 0.3;
}

.desc-quote:first-child { top: 8px; left: 14px; }
.desc-quote.end { bottom: -8px; right: 14px; top: auto; left: auto; }

.intro-text {
  margin: 0;
  font-size: 14px;
  color: #c0c4cc;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 0 20px;
}

/* ===== Goods Grid ===== */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.goods-card {
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.goods-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

.goods-img-wrap {
  width: 100%;
  height: 160px;
  overflow: hidden;
  position: relative;
}

.goods-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.goods-card:hover .goods-img { transform: scale(1.08); }

.goods-img-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
  pointer-events: none;
}

.goods-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: rgba(255, 255, 255, 0.5);
  font-size: 36px;
  font-weight: 700;
}

.goods-info { padding: 12px 4px 4px; }

.goods-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #e5e6e8;
}

.goods-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}

.goods-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 700;
}

.goods-stock {
  font-size: 12px;
  color: #909399;
}

.goods-cart-row {
  margin-top: 4px;
}

.goods-cart-badge {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(103, 194, 58, 0.08);
  border: 1px solid rgba(103, 194, 58, 0.15);
}

.goods-divider {
  margin: 8px 0;
  border-color: #2c2e36;
}

.goods-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #2c2e36;
}

.qty-control { display: flex; align-items: center; gap: 0; }

.qty-btn {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(26, 28, 36, 0.35);
  backdrop-filter: blur(8px);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  padding: 0;
  color: #c0c4cc;
}

.qty-btn:hover {
  border-color: #5b8def;
  color: #5b8def;
  background: rgba(91, 141, 239, 0.1);
}

.qty-num {
  min-width: 32px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  color: #e5e6e8;
}

.qty-num.active { color: #409eff; }

.qty-num.submitting {
  animation: pulse 0.6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.auto-hint {
  font-size: 14px;
  animation: pulse 0.6s ease-in-out infinite;
}

.empty-wrap {
  padding: 40px 0;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .goods-grid { grid-template-columns: repeat(3, 1fr); }
  .cover-content { gap: 16px; padding: 0 24px; }
  .vip-card { width: 110px; height: 68px; }
}
@media (max-width: 640px) {
  .goods-grid { grid-template-columns: repeat(2, 1fr); }
  .bg-gallery { grid-template-columns: repeat(2, 1fr); }
  .cover-content { flex-wrap: wrap; justify-content: center; }
  .cover-center { text-align: center; }
  .cover-tags { justify-content: center; }
}
</style>