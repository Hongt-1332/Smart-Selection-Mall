﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="user-bg" v-loading="loading">
    <!-- 封面信息区 -->
    <div class="cover-section">
      <div
        class="cover-bg"
        :style="{ backgroundImage: `url(https://picsum.photos/960/240)` }"
      />
      <div class="cover-overlay" />
      <div class="cover-content">
        <div class="avatar-box">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
          <el-icon v-else :size="40"><UserFilled /></el-icon>
        </div>
        <div class="cover-center">
          <h2 class="cover-name">{{ userInfo.userName || '未设置' }}</h2>
          <p class="cover-addr">@{{ userInfo.account }}</p>
          <div class="cover-tags">
            <span class="tag-vip" v-if="userInfo.level > 0">
              <span class="vip-crown">👑</span>
              VIP{{ userInfo.level }} · {{ userInfo.levelName }}
            </span>
            <span class="tag-bg">🖼️ {{ bgImages.length }} 张背景</span>
          </div>
        </div>
        <div class="vip-card" v-if="userInfo.level > 0">
          <div class="vip-shine" />
          <div class="vip-badge">VIP{{ userInfo.level }}</div>
          <div class="vip-name">{{ userInfo.levelName }}</div>
          <div class="vip-sub">剩余{{ userInfo.vipRemainingTime }}天</div>
        </div>
      </div>
    </div>

    <!-- 个人简介 -->
    <div class="section" v-if="userInfo.describe">
      <h3 class="section-title">📝 个人简介</h3>
      <div class="intro-box">
        <span class="desc-quote">"</span>
        <p class="intro-text">{{ userInfo.describe }}</p>
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
            <img :src="displayBgSlots[slot - 1]!.url" class="bg-card-img" />
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
    <div class="section" v-if="goodsList.length > 0">
      <h3 class="section-title">
        🛍️ 在售商品
        <span class="goods-total">共 {{ goodsList.length }} 件</span>
      </h3>
      <div class="goods-grid">
        <el-card
          class="goods-card"
          shadow="hover"
          v-for="g in goodsList"
          :key="g.id"
        >
          <div class="goods-img-wrap">
            <img v-if="g.imagePath" :src="resolveImg(g.imagePath)" class="goods-img" />
            <div v-else class="goods-img-placeholder">暂无图片</div>
          </div>
          <div class="goods-info">
            <h3 class="goods-name">{{ g.goodsName }}</h3>
            <p class="goods-desc" v-if="g.describe">{{ g.describe }}</p>
            <div class="goods-price-row">
              <span class="goods-price">¥{{ g.goodsPrice.toFixed(2) }}</span>
              <span class="goods-stock">库存 {{ g.goodsStock }}</span>
              <span class="goods-cart-badge" v-if="(fakeCart[g.id] ?? 0) > 0">🛒 已加 {{ fakeCart[g.id] ?? 0 }}</span>
            </div>
            <el-divider class="goods-divider" />
            <div class="merchant-info">
              <div class="merchant-avatar">
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
                <el-icon v-else :size="16"><UserFilled /></el-icon>
              </div>
              <div class="merchant-detail">
                <span class="merchant-name">{{ userInfo.userName }}</span>
                <span class="merchant-addr" v-if="g.address">📍 {{ g.address }}</span>
              </div>
            </div>
            <div class="goods-actions">
              <div class="qty-control">
                <button class="qty-btn" @click.stop="decreaseQty(g)">−</button>
                <span class="qty-num" :class="{ active: (fakeCart[g.id] ?? 0) > 0 }">{{ fakeCart[g.id] ?? 0 }}</span>
                <button class="qty-btn" @click.stop="increaseQty(g)">+</button>
              </div>
              <button class="buy-now-btn" @click="openBuyDialog(g)">立即购买</button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog append-to-body v-model="buyDialogVisible" width="440px" class="buy-dialog" :show-close="false">
      <template v-if="buyTarget">
      <div class="buy-header">
        <div class="buy-header-img">
          <img v-if="buyTarget.imagePath" :src="resolveImg(buyTarget.imagePath)" />
          <div v-else class="buy-header-img-ph">📦</div>
        </div>
        <div class="buy-header-info">
          <h3 class="buy-header-name">{{ buyTarget.goodsName }}</h3>
          <p class="buy-header-desc" v-if="buyTarget.describe">{{ buyTarget.describe }}</p>
          <div class="buy-header-price">¥{{ buyTarget.goodsPrice.toFixed(2) }}</div>
        </div>
        <button class="buy-close" @click="buyDialogVisible = false">✕</button>
      </div>

      <div class="buy-body">
        <div class="buy-field">
          <span class="buy-field-label">数量</span>
          <div class="buy-field-qty">
            <button class="buy-step" @click="buyQty = Math.max(1, buyQty - 1)">−</button>
            <span class="buy-step-val">{{ buyQty }}</span>
            <button class="buy-step" @click="buyQty = Math.min(buyTarget.goodsStock, buyQty + 1)">+</button>
          </div>
          <span class="buy-field-extra">库存 {{ buyTarget.goodsStock }}</span>
        </div>

        <div class="buy-summary">
          <div class="buy-summary-row">
            <span>商品单价</span>
            <span>¥{{ buyTarget.goodsPrice.toFixed(2) }}</span>
          </div>
          <div class="buy-summary-row">
            <span>购买数量</span>
            <span>×{{ buyQty }}</span>
          </div>
          <div class="buy-summary-divider" />
          <div class="buy-summary-row buy-summary-total">
            <span>合计</span>
            <span>¥{{ (buyTarget.goodsPrice * buyQty).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div class="buy-footer">
        <button class="buy-cancel" @click="buyDialogVisible = false">取消</button>
        <button class="buy-confirm" @click="handleFakeBuy">
          确认购买 ¥{{ (buyTarget.goodsPrice * buyQty).toFixed(2) }}
        </button>
      </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { msgError, msgSuccess } from '../../utils/message'
import { UserFilled, Picture, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { userService } from '../../services/UserService'
import type { UserBackgroundGoods } from '../../services/UserService'
import { useUserStore } from '../../stores/user'
import { getErrorMessage } from '../../utils/error'

const userStore = useUserStore()
const loading = ref(false)

const userInfo = reactive({
  userName: '',
  account: '',
  avatarPath: '',
  describe: '',
  level: 0,
  levelName: '普通用户',
  vipRemainingTime: 0,
})

const avatarUrl = ref('')
const goodsList = ref<UserBackgroundGoods[]>([])
const fakeCart = reactive<Record<number, number>>({})
const buyDialogVisible = ref(false)
const buyTarget = ref<UserBackgroundGoods | null>(null)
const buyQty = ref(1)
const imageMap = ref<Record<string, string>>({})
const bgOffset = ref(0)
const bgAutoPlay = ref(false)
let bgAutoTimer: ReturnType<typeof setInterval> | null = null

interface BgItem {
  id: number
  imagePath: string
  url: string
  sequence: number
}
const bgImages = ref<BgItem[]>([])

function resolveImg(path: string): string {
  return imageMap.value[path] || path
}

function increaseQty(g: UserBackgroundGoods) {
  const cur = fakeCart[g.id] ?? 0
  fakeCart[g.id] = cur + 1
}

function decreaseQty(g: UserBackgroundGoods) {
  const cur = fakeCart[g.id] ?? 0
  if (cur <= 0) return
  fakeCart[g.id] = cur - 1
}

function openBuyDialog(g: UserBackgroundGoods) {
  buyTarget.value = g
  buyQty.value = 1
  buyDialogVisible.value = true
}

function handleFakeBuy() {
  msgSuccess(`已模拟购买 ${buyQty.value} 件 ${buyTarget.value?.goodsName}`)
  buyDialogVisible.value = false
}

const displayBgSlots = computed<(BgItem | null)[]>(() => {
  const slots: (BgItem | null)[] = [null, null, null, null]
  const imgs = bgImages.value
  for (let i = 0; i < 4; i++) {
    const idx = bgOffset.value + i
    if (idx < imgs.length) {
      slots[i] = imgs[idx] ?? null
    }
  }
  return slots
})

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

async function fetchUserInfo() {
  loading.value = true
  try {
    const [info, bgRes] = await Promise.all([
      userStore.fetchUserInfoVO(),
      userService.getUserBackground(),
    ])

    if (info) {
      userInfo.account = info.account || ''
      userInfo.avatarPath = info.avatarPath || ''
      userInfo.vipRemainingTime = Number(info.vipRemainingTime) || 0

      if (info.avatarPath) {
        const imgRes = await userService.getImages([info.avatarPath])
        if (imgRes.code === 200 && imgRes.data) {
          avatarUrl.value = imgRes.data[info.avatarPath] || info.avatarPath
        } else {
          avatarUrl.value = info.avatarPath
        }
      } else {
        avatarUrl.value = ''
      }
    }

    if (bgRes.code === 200 && bgRes.data) {
      const bgData = bgRes.data
      userInfo.userName = bgData.userName || ''
      userInfo.describe = bgData.describe || ''
      userInfo.level = bgData.level ?? 0
      userInfo.levelName = bgData.levelName || '普通用户'
      goodsList.value = bgData.goods || []

      const paths: string[] = []
      ;(bgData.backgrounds || []).forEach((bg) => {
        if (bg.imagePath) paths.push(bg.imagePath)
      })
      goodsList.value.forEach((g) => {
        if (g.imagePath) paths.push(g.imagePath)
      })
      const uniquePaths = [...new Set(paths.filter(Boolean))]

      if (uniquePaths.length > 0) {
        const imgRes = await userService.getImages(uniquePaths)
        if (imgRes.code === 200 && imgRes.data) {
          imageMap.value = imgRes.data
        }
      }

      bgImages.value = (bgData.backgrounds || []).map((bg, index) => ({
        id: index + 1,
        imagePath: bg.imagePath,
        url: imageMap.value[bg.imagePath] || bg.imagePath,
        sequence: bg.sequence,
      }))
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await fetchUserInfo()
  }
})

onUnmounted(() => {
  stopAutoPlay()
})
</script>

<style scoped>
.user-bg {
  max-width: 960px;
  margin: 0 auto;
  animation: pageIn 0.5s ease-out;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
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

.tag-bg {
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
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.goods-card:hover { transform: translateY(-2px); }

.goods-img-wrap {
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 6px 6px 0 0;
}

.goods-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.goods-card:hover .goods-img { transform: scale(1.05); }

.goods-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(26, 28, 36, 0.35);
  backdrop-filter: blur(8px);
  color: #909399;
  font-size: 14px;
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

.goods-cart-badge {
  font-size: 12px;
  color: #67c23a;
  margin-left: auto;
  font-weight: 500;
}

.goods-divider {
  margin: 8px 0;
  border-color: #2c2e36;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-radius: 6px;
  transition: background 0.2s;
}
.merchant-info:hover {
  background: rgba(91, 141, 239, 0.08);
}

.merchant-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5b8def, #e6a23c);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-detail {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.merchant-name {
  font-size: 13px;
  font-weight: 500;
  color: #c0c4cc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-addr {
  font-size: 11px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.qty-num.active {
  color: #409eff;
}

.buy-now-btn {
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.buy-now-btn:hover {
  background: linear-gradient(135deg, #7c7ff7, #9d6ffa);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transform: translateY(-1px);
}

.goods-total {
  margin-left: auto;
  font-size: 13px;
  font-weight: 400;
  color: #505868;
}

/* ===== Buy Dialog ===== */
.buy-dialog :deep(.el-dialog) {
  background: rgba(18, 20, 28, 0.98);
  backdrop-filter: blur(32px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.04);
}

.buy-dialog :deep(.el-dialog__header) {
  display: none;
}

.buy-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.buy-dialog :deep(.el-dialog__footer) {
  display: none;
}

.buy-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(99,102,241,0.06), rgba(139,92,246,0.03));
  border-bottom: 1px solid rgba(255,255,255,0.06);
  position: relative;
}

.buy-header-img {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(26,28,36,0.5);
  border: 1px solid rgba(255,255,255,0.06);
}

.buy-header-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.buy-header-img-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.buy-header-info {
  flex: 1;
  min-width: 0;
}

.buy-header-name {
  font-size: 16px;
  font-weight: 700;
  color: #f1f2f4;
  margin: 0 0 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.buy-header-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.buy-header-price {
  font-size: 24px;
  font-weight: 800;
  color: #f56c6c;
  letter-spacing: -0.5px;
}

.buy-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: rgba(255,255,255,0.06);
  color: rgba(255,255,255,0.4);
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.buy-close:hover {
  background: rgba(255,255,255,0.12);
  color: #e5e6e8;
}

.buy-body {
  padding: 20px 24px;
}

.buy-field {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(255,255,255,0.04);
}

.buy-field-label {
  width: 40px;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255,255,255,0.4);
  flex-shrink: 0;
}

.buy-field-qty {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.08);
  overflow: hidden;
}

.buy-step {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #c0c4cc;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.buy-step:hover {
  background: rgba(99,102,241,0.1);
  color: #a5b4fc;
}

.buy-step-val {
  min-width: 44px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #e5e6e8;
  border-left: 1px solid rgba(255,255,255,0.06);
  border-right: 1px solid rgba(255,255,255,0.06);
  line-height: 36px;
}

.buy-field-extra {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.buy-summary {
  margin-top: 16px;
  padding: 16px;
  background: rgba(255,255,255,0.02);
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.04);
}

.buy-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: rgba(255,255,255,0.45);
  padding: 4px 0;
}

.buy-summary-divider {
  height: 1px;
  background: rgba(255,255,255,0.06);
  margin: 10px 0;
}

.buy-summary-total {
  font-size: 15px;
  color: #e5e6e8;
  font-weight: 600;
  padding-top: 8px;
}

.buy-summary-total span:last-child {
  font-size: 22px;
  font-weight: 800;
  color: #f56c6c;
  letter-spacing: -0.5px;
}

.buy-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 20px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.buy-cancel {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.1);
  background: rgba(255,255,255,0.04);
  color: #c0c4cc;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.buy-cancel:hover {
  background: rgba(255,255,255,0.08);
  border-color: rgba(255,255,255,0.15);
  color: #e5e6e8;
}

.buy-confirm {
  flex: 2;
  height: 44px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 4px 16px rgba(99,102,241,0.25);
}

.buy-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #7c7ff7, #9d6ffa);
  box-shadow: 0 6px 24px rgba(99,102,241,0.35);
  transform: translateY(-1px);
}

.buy-confirm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .cover-content {
    flex-direction: column;
    text-align: center;
    padding: 0 24px;
    gap: 16px;
  }
  .cover-name {
    font-size: 22px;
  }
  .cover-tags {
    justify-content: center;
  }
  .vip-card {
    width: 100px;
    height: 64px;
  }
  .bg-gallery {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>