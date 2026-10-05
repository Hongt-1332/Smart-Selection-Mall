<template>
  <div class="login-page">
    <div v-if="bgImages.length > 0" class="bg-slideshow">
      <div
        v-for="(img, idx) in bgImages"
        :key="idx"
        class="bg-slide"
        :class="{ active: idx === currentBgIndex }"
        :style="{ backgroundImage: `url(${img})` }"
      />
    </div>
    <div class="login-card">
      <div class="logo">🛒</div>

      <div class="tabs-row">
        <button :class="['tab', { active: activeTab === 'login' }]" @click="activeTab = 'login'">登录</button>
        <button :class="['tab', { active: activeTab === 'register' }]" @click="activeTab = 'register'">注册</button>
      </div>

      <div v-show="activeTab === 'login'" class="form-wrap">
        <form @submit.prevent="handleLogin">
          <div class="field">
            <label class="label">账号</label>
            <el-input v-model="loginForm.account" placeholder="请输入账号" />
          </div>
          <div class="field">
            <label class="label">密码</label>
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </div>
          <div class="field">
            <label class="label">验证码</label>
            <div class="captcha-row">
              <el-input v-model="loginForm.captchaCode" placeholder="请输入验证码" />
              <img v-if="captchaImage" :src="captchaImage" class="captcha-img" @click="refreshCaptcha" alt="验证码" />
              <span v-else class="captcha-placeholder" @click="refreshCaptcha">加载中</span>
            </div>
          </div>
          <el-button type="primary" :loading="loading" native-type="submit" class="submit-btn">登 录</el-button>
        </form>
      </div>

      <div v-show="activeTab === 'register'" class="form-wrap">
        <form @submit.prevent="handleRegister">
          <div class="field">
            <label class="label">用户名</label>
            <el-input v-model="registerForm.userName" placeholder="请输入用户名" />
            <span class="tip">2-20个字符，支持中英文、数字</span>
          </div>
          <div class="field">
            <label class="label">账号</label>
            <el-input v-model="registerForm.account" placeholder="请输入账号" />
            <span class="tip">8-20个字符，仅字母和数字</span>
          </div>
          <div class="field">
            <label class="label">密码</label>
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
            <span class="tip">8-20个字符，需包含字母和数字</span>
          </div>
          <div class="field">
            <label class="label">确认密码</label>
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" show-password />
            <span class="tip">与上方密码保持一致</span>
          </div>
          <div class="field">
            <label class="label">邮箱</label>
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            <span class="tip">用于找回密码和接收通知</span>
          </div>
          <div class="field">
            <label class="label">手机号</label>
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
            <span class="tip">11位中国大陆手机号</span>
          </div>
          <div class="field">
            <label class="label">验证码</label>
            <div class="captcha-row">
              <el-input v-model="registerForm.captchaCode" placeholder="请输入验证码" />
              <img v-if="captchaImage" :src="captchaImage" class="captcha-img" @click="refreshCaptcha" alt="验证码" />
              <span v-else class="captcha-placeholder" @click="refreshCaptcha">加载中</span>
            </div>
          </div>
          <el-button type="primary" :loading="loading" native-type="submit" class="submit-btn">注 册</el-button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { msgSuccess, msgError, msgWarning } from '../utils/message'
import { useUserStore } from '../stores/user'
import { userService } from '../services/UserService'
import { vipService } from '../services/VipService'
import { getErrorMessage } from '../utils/error'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const captchaImage = ref('')
const bgImages = ref<string[]>([])
const currentBgIndex = ref(0)

const loginForm = reactive({
  account: '',
  password: '',
  captchaCode: '',
})

const registerForm = reactive({
  userName: '',
  account: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  captchaCode: '',
})

async function refreshCaptcha() {
  try {
    const res = await userService.getCaptcha()
    if (res.code === 200 && res.data) {
      captchaImage.value = res.data.captchaImage
    }
  } catch {
    captchaImage.value = ''
  }
}

async function fetchBgImages() {
  try {
    const res = await vipService.getLoginImages()
    if (res.code === 200 && res.data) {
      bgImages.value = res.data.map((img) => img.imageUrl)
    }
  } catch {
    bgImages.value = []
  }
}

async function handleLogin() {
  if (!loginForm.account || !loginForm.password || !loginForm.captchaCode) {
    msgWarning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    const res = await userStore.login(loginForm)
    if (res.code === 200) {
      msgSuccess('登录成功')
      const redirect = (route.query.redirect as string) || '/user'
      router.push(redirect)
    } else {
      msgError(res.message || '登录失败')
      refreshCaptcha()
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.userName || !registerForm.account || !registerForm.password || !registerForm.confirmPassword || !registerForm.email || !registerForm.phone || !registerForm.captchaCode) {
    msgWarning('请填写完整信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    msgError('两次密码输入不一致')
    return
  }
  if (registerForm.account.length < 8 || registerForm.account.length > 20) {
    msgError('账号需8-20个字符')
    return
  }
  if (registerForm.password.length < 8 || registerForm.password.length > 20) {
    msgError('密码需8-20个字符')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    msgError('手机号格式不正确')
    return
  }
  loading.value = true
  try {
    const res = await userStore.register(registerForm)
    if (res.code === 200) {
      msgSuccess('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.account = registerForm.account
    } else {
      msgError(res.message || '注册失败')
      refreshCaptcha()
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
onMounted(() => {
  fetchBgImages()
})
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.bg-slideshow {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.bg-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1.2s ease-in-out;
}
.bg-slide.active {
  opacity: 1;
}

.login-card {
  position: relative;
  z-index: 1;
}

.login-card {
  width: 420px;
  padding: 40px 36px 32px;
  background: rgba(18, 20, 28, 0.55);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.logo {
  text-align: center;
  font-size: 40px;
  margin-bottom: 24px;
}

/* ===== Tabs ===== */
.tabs-row {
  display: flex;
  margin-bottom: 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.tab {
  flex: 1;
  background: none;
  border: none;
  padding: 10px 0;
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  position: relative;
  transition: color 0.25s;
}
.tab:hover {
  color: rgba(255, 255, 255, 0.65);
}
.tab.active {
  color: #5b9eff;
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #5b9eff;
  border-radius: 2px;
}

/* ===== Form ===== */
.form-wrap {
  min-height: 0;
}
.field {
  margin-bottom: 20px;
}
.label {
  display: block;
  font-size: 13px;
  color: #8890a0;
  margin-bottom: 6px;
  font-weight: 500;
}
.tip {
  display: block;
  font-size: 12px;
  color: #505868;
  margin-top: 4px;
  padding-left: 2px;
}

/* ===== Input ===== */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset;
  transition: all 0.25s;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.15) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #5b9eff inset, 0 0 0 3px rgba(91, 158, 255, 0.12);
}
:deep(.el-input__inner) {
  color: #e0e3ea;
  font-size: 14px;
}
:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.15);
}
:deep(.el-input__suffix) {
  color: #505868;
}

/* ===== Captcha ===== */
.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captcha-row .el-input {
  flex: 1;
}
.captcha-img {
  height: 36px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  flex-shrink: 0;
}
.captcha-img:hover {
  border-color: rgba(91, 158, 255, 0.3);
}
.captcha-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 36px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  font-size: 12px;
  color: #505868;
  cursor: pointer;
  flex-shrink: 0;
}
.captcha-placeholder:hover {
  border-color: rgba(255, 255, 255, 0.15);
  color: #8890a0;
}

/* ===== Button ===== */
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 8px;
  margin-top: 4px;
  background: #5b9eff !important;
  border: none !important;
  transition: all 0.3s;
}
.submit-btn:hover {
  background: #6bacff !important;
  box-shadow: 0 4px 16px rgba(91, 158, 255, 0.3);
}

/* ===== Responsive ===== */
@media (max-width: 480px) {
  .login-card {
    width: 100%;
    padding: 32px 24px 28px;
  }
}
</style>