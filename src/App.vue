<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'
import BackgroundEffects from './components/BackgroundEffects.vue'
import { ref, onMounted } from 'vue'

const userStore = useUserStore()
const route = useRoute()
const mobileMenuOpen = ref(false)

onMounted(async () => {
  await userStore.checkSession()
})

const mobileMenuItems = [
  { label: '首页', path: '/' },
  { label: '用户中心', path: '/user' },
  { label: '商户中心', path: '/merchant' },
  { label: '管理中心', path: '/manager' },
  { label: '智能推荐', path: '/ai-recommend' },
  { label: '背景', path: '/background' },
  { label: '关于', path: '/about' },
]

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

function closeMobileMenu() {
  mobileMenuOpen.value = false
}

function getHeaderMeteorStyle(i: number) {
  const seed = i * 11 + 7
  const size = 2 + (seed % 4)
  const colors = [
    'rgba(255,255,255,0.85)',
    'rgba(200,220,255,0.8)',
    'rgba(255,220,255,0.75)',
  ]
  return {
    left: `${(seed * 7 + 5) % 100}%`,
    top: `${(seed * 3) % 40}%`,
    width: size + 'px',
    height: size + 'px',
    animationDelay: `${(i * 1.2) % 8}s`,
    animationDuration: `${4 + (i % 5)}s`,
    '--hm-color': colors[seed % colors.length],
    '--hm-glow': (size * 3) + 'px',
  }
}
</script>

<template>
  <div class="app-wrapper">
    <BackgroundEffects />
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="header-meteors">
          <div v-for="i in 18" :key="'hm-'+i" class="header-meteor" :style="getHeaderMeteorStyle(i)"></div>
        </div>
        <div class="header-left">
          <h1 class="app-title"><span>优</span>选商城</h1>
          <el-menu mode="horizontal" :default-active="route.path" router :ellipsis="false" class="desktop-menu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/user">用户中心</el-menu-item>
            <el-menu-item index="/merchant">商户中心</el-menu-item>
            <el-menu-item index="/manager">管理中心</el-menu-item>
            <el-menu-item index="/ai-recommend">智能推荐</el-menu-item>
            <el-menu-item index="/background">背景</el-menu-item>
            <el-menu-item index="/about">关于</el-menu-item>
          </el-menu>
        </div>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <span class="user-name">{{ userStore.userName }}</span>
            <el-button type="danger" size="small" @click="userStore.logout()">退出登录</el-button>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
          </template>
          <button class="mobile-menu-btn" @click="toggleMobileMenu">
            <span></span><span></span><span></span>
          </button>
        </div>
      </el-header>

      <!-- 移动端下拉菜单 -->
      <div class="mobile-menu" :class="{ open: mobileMenuOpen }" @click.self="closeMobileMenu">
        <div class="mobile-menu-inner">
          <a v-for="item in mobileMenuItems" :key="item.path" :href="item.path" class="mobile-menu-item" :class="{ active: route.path === item.path }" @click.prevent="closeMobileMenu(); $router.push(item.path)">{{ item.label }}</a>
        </div>
      </div>

      <el-main class="app-main">
        <RouterView />
      </el-main>
      <footer class="app-footer">
        <p>&copy; 2026 智选商城 &nbsp;|&nbsp; All Rights Reserved &nbsp;|&nbsp; <span class="tm">®</span></p>
      </footer>
    </el-container>
  </div>
</template>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.app-container {
  min-height: 100vh;
  background: #0a0c10 !important;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(16, 18, 26, 0.55) !important;
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 100;
  flex-shrink: 0;
  min-width: 0;
  overflow: hidden;
}

.header-meteors {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.header-meteor {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%,
    transparent 30%,
    var(--hm-color, rgba(255,255,255,0.7)) 55%,
    transparent 100%
  );
  box-shadow:
    0 0 calc(var(--hm-glow, 10px) * 0.6) calc(var(--hm-glow, 10px) * 0.3) var(--hm-color, rgba(255,255,255,0.4)),
    0 0 var(--hm-glow, 10px) calc(var(--hm-glow, 10px) * 0.6) var(--hm-color, rgba(255,255,255,0.2));
  filter: blur(1px);
  animation: header-meteor-fly linear infinite;
  opacity: 0;
}

@keyframes header-meteor-fly {
  0%   { opacity: 0; transform: translate(0, 0) rotate(-35deg) scale(0.15); }
  3%   { opacity: 0.65; }
  12%  { opacity: 0.75; transform: translate(80px, 100px) rotate(-35deg) scale(1); }
  35%  { opacity: 0.4; transform: translate(200px, 260px) rotate(-35deg) scale(0.6); }
  60%  { opacity: 0.1; transform: translate(320px, 420px) rotate(-35deg) scale(0.25); }
  100% { opacity: 0; transform: translate(450px, 600px) rotate(-35deg) scale(0); }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
  min-width: 0;
  overflow: hidden;
}

.app-title {
  font-size: 22px;
  color: #5b9eff;
  margin: 0;
  white-space: nowrap;
  font-weight: 700;
  letter-spacing: 1px;
}

.app-title span {
  color: #ef5c64;
}

.header-left .el-menu--horizontal {
  border-bottom: none;
}

.header-left .el-menu--horizontal .el-menu-item.is-active {
  color: #5b9eff;
  border-bottom-color: #5b9eff;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: #8890a0;
}

.app-main {
  flex: 1;
  min-height: 0;
  background: transparent !important;
  padding: 20px;
  overflow-y: auto;
}

.app-footer {
  text-align: center;
  padding: 16px 24px;
  font-size: 13px;
  color: #505868;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(16, 18, 26, 0.5);
  backdrop-filter: blur(12px);
  flex-shrink: 0;
  letter-spacing: 1px;
}

.app-footer p {
  margin: 0;
}

.app-footer .tm {
  color: #5b9eff;
  font-weight: 700;
}

/* ===== 移动端菜单按钮 ===== */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  z-index: 200;
}

.mobile-menu-btn span {
  display: block;
  width: 22px;
  height: 2px;
  background: #8890a0;
  border-radius: 1px;
  transition: all 0.3s;
}

/* ===== 移动端下拉菜单 ===== */
.mobile-menu {
  display: none;
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  z-index: 150;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}

.mobile-menu.open {
  opacity: 1;
  pointer-events: auto;
}

.mobile-menu-inner {
  background: rgba(18, 20, 28, 0.6);
  backdrop-filter: blur(20px);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.mobile-menu-item {
  padding: 14px 20px;
  color: #8890a0;
  font-size: 15px;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.mobile-menu-item:hover,
.mobile-menu-item.active {
  background: rgba(91,141,239,0.15);
  color: #5b9eff;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .header-left {
    gap: 16px;
  }

  .desktop-menu .el-menu-item {
    font-size: 13px;
    padding: 0 10px;
  }
}

@media (max-width: 640px) {
  .desktop-menu {
    display: none !important;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .mobile-menu {
    display: block;
  }

  .app-header {
    padding: 0 16px;
  }

  .app-main {
    padding: 12px;
  }

  .user-name {
    display: none;
  }

  .app-title {
    font-size: 18px;
  }

  .app-footer {
    font-size: 11px;
    padding: 12px 16px;
  }
}
</style>