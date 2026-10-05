<template>
  <div class="home">
    <!-- 首页 Banner -->
    <section class="banner">
      <img
        v-for="(img, i) in bannerImgs"
        :key="i"
        :src="img"
        class="banner-bg"
        :class="{ active: bannerIdx === i }"
      />
      <div class="banner-overlay"></div>
      <button class="banner-arrow left" @click="bannerPrev">&#10094;</button>
      <button class="banner-arrow right" @click="bannerNext">&#10095;</button>
      <div class="banner-content">
        <h1 class="banner-title">优选好物，尽在掌中</h1>
        <p class="banner-subtitle">品质生活 · 一站直达</p>
        <div class="banner-actions">
          <el-button type="danger" size="large" round @click="$router.push('/user')">去购物</el-button>
        </div>
        <div class="banner-dots">
          <span
            v-for="(_, i) in bannerImgs.length"
            :key="i"
            class="banner-dot"
            :class="{ active: bannerIdx === i }"
            @click="bannerIdx = i"
          ></span>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-entry">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8" v-for="item in entries" :key="item.title">
          <el-card shadow="hover" class="entry-card" @click="$router.push(item.path)">
            <div class="entry-icon">{{ item.icon }}</div>
            <div class="entry-title">{{ item.title }}</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 推荐商品 -->
    <section class="recommend">
      <div class="section-header">
        <h2>推荐商品</h2>
      </div>
      <div class="carousel-outer">
        <button class="carousel-arrow left" @click="prev">&#10094;</button>
        <div class="carousel-wrap">
          <div
            class="carousel-track"
            :class="{ 'no-transition': !transitioning }"
            :style="{ transform: 'translateX(-' + currentIndex * (100 / 3) + '%)' }"
          >
            <div class="carousel-item" v-for="(item, i) in trackList" :key="item.id + '-' + i">
              <el-card shadow="hover" class="goods-card">
                <img :src="item.img" class="goods-img" />
                <div class="goods-info">
                  <h3 class="goods-name">{{ item.name }}</h3>
                  <span class="goods-price">¥{{ item.price }}</span>
                </div>
              </el-card>
            </div>
          </div>
        </div>
        <button class="carousel-arrow right" @click="next">&#10095;</button>
      </div>
      <div class="carousel-dots">
        <span
          v-for="(_, i) in totalItems"
          :key="i"
          class="dot"
          :class="{ active: realIndex === i }"
          @click="goTo(i)"
        ></span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { vipService } from '@/services/VipService'
import type { VipImage, VipGoods } from '@/services/VipService'

const entries = [
  { icon: '🛒', title: '商城购物', path: '/user' },
  { icon: '📋', title: '订单', path: '/manager/trade' },
  { icon: '👤', title: '个人中心', path: '/user/info' },
]

const bannerImgs = ref<string[]>([])
const bannerIdx = ref(0)
let bannerTimer: ReturnType<typeof setInterval> | null = null

function bannerPrev() {
  if (bannerImgs.value.length === 0) return
  bannerIdx.value = (bannerIdx.value - 1 + bannerImgs.value.length) % bannerImgs.value.length
}

function bannerNext() {
  if (bannerImgs.value.length === 0) return
  bannerIdx.value = (bannerIdx.value + 1) % bannerImgs.value.length
}

interface RecommendItem {
  id: number
  name: string
  price: string
  img: string
}

const carouselList = ref<RecommendItem[]>([])

const cloneCount = computed(() => Math.min(3, carouselList.value.length))
const trackList = computed(() => {
  const list = carouselList.value
  if (list.length === 0) return []
  return [...list, ...list.slice(0, cloneCount.value)]
})
const totalItems = computed(() => carouselList.value.length)

const currentIndex = ref(0)
const transitioning = ref(true)
const realIndex = computed(() => totalItems.value > 0 ? currentIndex.value % totalItems.value : 0)

let timer: ReturnType<typeof setInterval> | null = null

function next() {
  if (totalItems.value === 0) return
  currentIndex.value++
  if (currentIndex.value === totalItems.value) {
    setTimeout(() => {
      transitioning.value = false
      currentIndex.value = 0
      requestAnimationFrame(() => {
        transitioning.value = true
      })
    }, 500)
  }
}

function prev() {
  if (totalItems.value === 0) return
  if (currentIndex.value === 0) {
    transitioning.value = false
    currentIndex.value = totalItems.value
    requestAnimationFrame(() => {
      transitioning.value = true
      currentIndex.value = totalItems.value - 1
    })
  } else {
    currentIndex.value--
  }
}

function goTo(i: number) {
  currentIndex.value = i
}

function startAutoPlay() {
  timer = setInterval(next, 3000)
}

onMounted(async () => {
  try {
    const imgRes = await vipService.getHomeImages()
    if (imgRes.code === 200 && imgRes.data) {
      bannerImgs.value = imgRes.data.map((img: VipImage) => img.imageUrl)
    }
  } catch {}
  if (bannerImgs.value.length === 0) {
    bannerImgs.value = [
      'https://picsum.photos/seed/shop1/1200/400',
      'https://picsum.photos/seed/shop2/1200/400',
      'https://picsum.photos/seed/shop3/1200/400',
    ]
  }

  try {
    const goodsRes = await vipService.getVipGoods()
    if (goodsRes.code === 200 && goodsRes.data) {
      carouselList.value = goodsRes.data.map((g: VipGoods) => ({
        id: Number(g.goodsId),
        name: g.goodsName,
        price: String(g.goodsPrice),
        img: g.imagePath,
      }))
    }
  } catch {}
  if (carouselList.value.length === 0) {
    carouselList.value = [
      { id: 1, name: '无线蓝牙耳机', price: '299.00', img: 'https://picsum.photos/seed/headphone/400/300' },
      { id: 2, name: '智能运动手表', price: '899.00', img: 'https://picsum.photos/seed/watch/400/300' },
      { id: 3, name: '机械键盘', price: '349.00', img: 'https://picsum.photos/seed/keyboard/400/300' },
      { id: 4, name: '降噪头戴耳机', price: '599.00', img: 'https://picsum.photos/seed/earphone/400/300' },
      { id: 5, name: '便携充电宝', price: '129.00', img: 'https://picsum.photos/seed/powerbank/400/300' },
    ]
  }

  startAutoPlay()
  bannerTimer = setInterval(() => {
    if (bannerImgs.value.length > 0) {
      bannerIdx.value = (bannerIdx.value + 1) % bannerImgs.value.length
    }
  }, 4000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (bannerTimer) clearInterval(bannerTimer)
})
</script>
<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  position: relative;
}

.home::before {
  content: '';
  position: fixed;
  top: 50%;
  left: 50%;
  width: 600px;
  height: 600px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(91,141,239,0.05) 0%, transparent 65%);
  pointer-events: none;
  z-index: -1;
}

/* Banner */
.banner {
  margin: 24px 0;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  height: 360px;
}

.banner-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 1s ease;
}

.banner-bg.active {
  opacity: 1;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(10,12,16,0.75) 0%, rgba(10,12,16,0.4) 50%, rgba(10,12,16,0.75) 100%);
}

.banner-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  width: 44px;
  height: 44px;
  border: none;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 20px;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-arrow:hover {
  background: rgba(0, 0, 0, 0.6);
}

.banner-arrow.left {
  left: 16px;
}

.banner-arrow.right {
  right: 16px;
}

.banner-content {
  text-align: center;
  padding: 80px 20px;
  color: #e5e6e8;
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 42px;
  margin: 0 0 12px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #7aa3f2, #d3545c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.banner-subtitle {
  font-size: 18px;
  opacity: 0.6;
  margin: 0 0 32px;
  color: #b0b1b8;
}

.banner-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.banner-actions .el-button--large {
  padding: 12px 40px;
  font-size: 16px;
}

.banner-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.banner-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: background 0.3s;
}

.banner-dot.active {
  background: #fff;
}

/* 快捷入口 */
.quick-entry {
  margin-bottom: 40px;
}

.entry-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 16px;
}

.entry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.entry-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.entry-title {
  font-size: 15px;
  font-weight: 600;
  color: #e5e6e8;
}

/* 推荐商品 */
.recommend {
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 22px;
  color: #e5e6e8;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 4px solid #5b8def;
}

.carousel-outer {
  display: flex;
  align-items: center;
  gap: 0;
}

.carousel-arrow {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: #8890a0;
  font-size: 16px;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-arrow:hover {
  background: rgba(91, 141, 239, 0.2);
  color: #5b8def;
}

.carousel-wrap {
  overflow: hidden;
  border-radius: 8px;
  flex: 1;
}

.carousel-track {
  display: flex;
  transition: transform 0.5s ease;
}

.carousel-track.no-transition {
  transition: none;
}

.carousel-item {
  flex: 0 0 calc(100% / 3);
  padding: 0 8px;
  box-sizing: border-box;
}

.goods-card {
  text-align: center;
}

.goods-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
}

.goods-info {
  padding: 12px 0 4px;
}

.goods-name {
  font-size: 15px;
  margin: 0 0 8px;
  color: #c8c9cc;
  font-weight: 500;
}

.goods-price {
  color: #d3545c;
  font-size: 20px;
  font-weight: bold;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: background 0.3s;
}

.dot.active {
  background: #5b8def;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .banner {
    height: 260px;
  }
  .banner-title {
    font-size: 28px;
  }
  .banner-subtitle {
    font-size: 14px;
  }
  .carousel-item {
    flex: 0 0 50%;
  }
  .banner-content {
    padding: 50px 20px;
  }
}

@media (max-width: 640px) {
  .home {
    padding: 0 8px;
  }
  .banner {
    height: 200px;
    border-radius: 10px;
    margin: 12px 0;
  }
  .banner-title {
    font-size: 22px;
  }
  .banner-subtitle {
    font-size: 12px;
  }
  .banner-content {
    padding: 30px 12px;
  }
  .banner-arrow {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
  .carousel-item {
    flex: 0 0 100%;
  }
  .goods-img {
    height: 160px;
  }
  .section-header h2 {
    font-size: 18px;
  }
  .entry-card {
    margin-bottom: 10px;
  }
}
</style>