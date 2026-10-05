<template>
  <div class="page-container">
    <!-- 主内容区 -->
    <div class="main-layout">
      <!-- 左侧：聊天区 -->
      <div class="chat-wrapper">
        <div class="chat-header">
          <div class="header-left">
            <div class="status-dot online"></div>
            <span>智能管家在线</span>
          </div>
          <div class="header-right">
            <el-button size="small" text @click="handleNewChat" class="new-chat-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px;margin-right:4px">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              新对话
            </el-button>
            <span class="header-time">{{ currentTime }}</span>
          </div>
        </div>

        <div class="messages-container" ref="msgRef">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="message-row"
            :class="msg.role"
          >
            <div class="avatar-wrap">
              <img v-if="msg.role === 'user' && userStore.userInfoVO?.avatarPath" :src="userStore.userInfoVO.avatarPath" class="chat-avatar-img" />
              <div v-else class="avatar" :class="msg.role">
                {{ msg.role === 'user' ? '👤' : '🤖' }}
              </div>
              <span class="avatar-label">{{ msg.role === 'user' ? (userStore.userName || '我') : 'AI' }}</span>
            </div>
            <div class="bubble-wrap">
              <div class="bubble" :class="msg.role">
                <p>{{ msg.content }}</p>
              </div>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
          </div>

          <div v-if="isThinking" class="message-row ai">
            <div class="avatar-wrap">
              <div class="avatar ai">🤖</div>
              <span class="avatar-label">AI</span>
            </div>
            <div class="bubble-wrap">
              <div class="bubble ai thinking-bubble">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <button
            v-for="tag in quickTags"
            :key="tag"
            class="quick-btn"
            @click="quickSend(tag)"
          >{{ tag }}</button>
        </div>

        <div class="input-wrapper">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="描述你的需求，智能管家帮你推荐..."
            resize="none"
            @keyup.enter="handleEnter"
            class="chat-input"
          />
          <button
            class="send-btn"
            :class="{ loading }"
            @click="send"
            :disabled="!input.trim() || loading"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 右侧：推荐商品 -->
      <div class="recommend-panel">
        <div class="panel-header">
          <h3>✨ 为你精选</h3>
          <span class="panel-sub">基于你的偏好</span>
        </div>

        <div v-if="!hasQueried" class="panel-placeholder">
          <div class="placeholder-icon">🤖</div>
          <p class="placeholder-title">智能推荐</p>
          <p class="placeholder-desc">在左侧输入你的需求，<br/>智能管家将为你推荐最合适的商品</p>
        </div>

        <div class="goods-grid" v-else-if="recItems.length > 0">
          <el-card
            class="goods-card"
            shadow="hover"
            v-for="(item, idx) in recItems"
            :key="item.goodsId"
            :style="{ animationDelay: idx * 0.1 + 's' }"
          >
            <div class="goods-img-wrap">
              <img v-if="item.imagePath" :src="item.imagePath" class="goods-img" />
              <div v-else class="goods-img-placeholder">暂无图片</div>
              <div class="card-badge" v-if="idx === 0">热门</div>
            </div>
            <div class="goods-info">
              <h3 class="goods-name">{{ item.goodsName }}</h3>
              <p class="goods-desc" v-if="item.describe">{{ item.describe }}</p>
              <div class="goods-price-row">
                <span class="goods-price">¥{{ item.goodsPrice.toFixed(2) }}</span>
                <span class="goods-stock">库存 {{ item.goodsStock }}</span>
                <span class="goods-cart-badge" v-if="item.cartQuantity && item.cartQuantity > 0">🛒 已加 {{ item.cartQuantity }}</span>
              </div>
              <el-divider class="goods-divider" />
              <div class="merchant-info" v-if="item.userName" @click.stop="goMerchant(item.merchantId)">
                <div class="merchant-avatar">
                  <img v-if="item.merchantAvatar" :src="item.merchantAvatar" class="avatar-img" />
                  <el-icon v-else :size="16"><UserFilled /></el-icon>
                </div>
                <div class="merchant-detail">
                  <span class="merchant-name">{{ item.userName }}</span>
                  <span class="merchant-addr" v-if="item.address">📍 {{ item.address }}</span>
                </div>
              </div>
              <div class="goods-actions">
                <div class="qty-control">
                  <button class="qty-btn" @click.stop="decreaseQty(item.goodsId)">−</button>
                  <span class="qty-num" :class="{ active: (item.cartQuantity ?? 0) > 0, submitting: submitting[item.goodsId] }">{{ item.cartQuantity || 0 }}</span>
                  <button class="qty-btn" @click.stop="increaseQty(item.goodsId)">+</button>
                </div>
                <span class="auto-hint" v-if="submitting[item.goodsId]">⏳</span>
              </div>
            </div>
          </el-card>
        </div>

        <div v-else class="panel-empty">
          <div class="placeholder-icon">📭</div>
          <p class="placeholder-title">暂无匹配</p>
          <p class="placeholder-desc">换个关键词试试看~</p>
        </div>
      </div>
    </div>

    <el-dialog
      append-to-body
      v-model="confirmDialogVisible"
      title="移除商品"
      width="360px"
      :close-on-click-modal="false"
    >
      <p>确认从购物车中移除「{{ pendingRemoveItem?.goodsName }}」吗？</p>
      <template #footer>
        <el-button @click="cancelRemoveFromCart">取消</el-button>
        <el-button type="danger" @click="confirmRemoveFromCart">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { msgSuccess, msgError, msgWarning, msgInfo } from '../utils/message'
import { aiService, type ProductDTO, type ChatMessage } from '../services/AIService'
import { userService } from '../services/UserService'
import { useUserStore } from '../stores/user'
import { UserFilled } from '@element-plus/icons-vue'
import { getErrorMessage } from '../utils/error'

const input = ref('')
const loading = ref(false)
const userStore = useUserStore()
const router = useRouter()
const msgRef = ref<HTMLElement | null>(null)
const currentTime = ref('')
let timeTimer: number | null = null
let sessionTimer: number | null = null
const SESSION_TIMEOUT = 10 * 60 * 1000

interface Message {
  role: 'user' | 'ai'
  content: string
  time: string
  _final?: boolean
}

const messages = ref<Message[]>([
  { role: 'ai', content: '你好！我是智能管家 🎯\n告诉我你喜欢什么，我来帮你找到最合适的~', time: getTimeStr() },
])
const isThinking = ref(false)
const hasQueried = ref(false)

const quickTags = ['蓝牙耳机推荐', '200元内背包', '送女生礼物', '机械键盘']

const recItems = ref<ProductDTO[]>([])
const cartTimers = reactive<Record<number, number>>({})
const submitting = reactive<Record<number, boolean>>({})
const confirmDialogVisible = ref(false)
const pendingRemoveItem = ref<ProductDTO | null>(null)

function increaseQty(goodsId: number) {
  const item = recItems.value.find((r) => r.goodsId === goodsId)
  if (!item) return
  item.cartQuantity = (item.cartQuantity || 0) + 1
  scheduleCartSubmit(goodsId)
}

function decreaseQty(goodsId: number) {
  const item = recItems.value.find((r) => r.goodsId === goodsId)
  if (!item) return
  const current = item.cartQuantity || 0
  if (current <= 0) return
  if (current === 1) {
    pendingRemoveItem.value = item
    confirmDialogVisible.value = true
    return
  }
  item.cartQuantity = current - 1
  scheduleCartSubmit(goodsId)
}

function confirmRemoveFromCart() {
  if (!pendingRemoveItem.value) return
  const item = pendingRemoveItem.value
  item.cartQuantity = 0
  confirmDialogVisible.value = false
  pendingRemoveItem.value = null
  scheduleCartSubmit(item.goodsId)
}

function cancelRemoveFromCart() {
  if (!pendingRemoveItem.value) return
  pendingRemoveItem.value.cartQuantity = 1
  confirmDialogVisible.value = false
  pendingRemoveItem.value = null
}

function scheduleCartSubmit(goodsId: number) {
  if (cartTimers[goodsId]) clearTimeout(cartTimers[goodsId])
  cartTimers[goodsId] = window.setTimeout(() => {
    const item = recItems.value.find((r) => r.goodsId === goodsId)
    if (!item) return
    submitCartChange(goodsId, item.cartQuantity || 0)
  }, 300)
}

async function submitCartChange(goodsId: number, qty: number) {
  if (qty < 0) return
  if (submitting[goodsId]) return
  submitting[goodsId] = true
  try {
    const res = await userService.handleCart({ goodId: goodsId, num: qty })
    if (res.code === 200) {
      const item = recItems.value.find((r) => r.goodsId === goodsId)
      if (item) {
        const wasZero = !item.cartQuantity || item.cartQuantity === 0
        item.cartQuantity = qty
        if (qty === 0) item.cartId = undefined
        if (qty === 0) {
          msgSuccess('已从购物车移除')
        } else if (wasZero && qty > 0) {
          msgSuccess('加入购物车成功')
        }
      }
    } else {
      msgError(res.message || '操作失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    submitting[goodsId] = false
  }
}

function getTimeStr() {
  const now = new Date()
  return `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}`
}

function updateTime() {
  currentTime.value = getTimeStr()
}

function resetSession() {
  messages.value = [
    { role: 'ai', content: '你好！我是智能管家 🎯\n告诉我你喜欢什么，我来帮你找到最合适的~', time: getTimeStr() },
  ]
  recItems.value = []
  hasQueried.value = false
  Object.keys(cartTimers).forEach((k) => { clearTimeout(cartTimers[Number(k)]) })
  Object.keys(cartTimers).forEach((k) => delete cartTimers[Number(k)])
  Object.keys(submitting).forEach((k) => delete submitting[Number(k)])
  confirmDialogVisible.value = false
  pendingRemoveItem.value = null
  msgInfo('会话已过期，对话记录已清除')
}

async function fetchHistory() {
  try {
    const res = await aiService.chatHistory()
    if (res.code === 200 && res.data) {
      let chatList: ChatMessage[]
      if (typeof res.data === 'string') {
        chatList = JSON.parse(res.data as string)
      } else {
        chatList = res.data as unknown as ChatMessage[]
      }
      if (!Array.isArray(chatList) || chatList.length === 0) return
      const list: Message[] = []
      chatList.forEach((item: ChatMessage) => {
        if (item.type === 'USER') {
          const userItem = item as { type: 'USER'; contents: { text: string; type: string }[] }
          let text = userItem.contents?.map((c) => c.text).join('') || ''
          if (text) {
            const m = text.match(/"([^"]*)"/)
            if (m) {
              text = m[1] || ''
            }
          }
          if (text) list.push({ role: 'user', content: text || '', time: '' })
        } else if (item.type === 'AI') {
          const aiItem = item as { type: 'AI'; text: string }
          if (aiItem.text) list.push({ role: 'ai', content: aiItem.text, time: '' })
        }
      })
      if (list.length > 0) {
        messages.value = list
      }
    }
  } catch {
    // ignore
  }
}

function startSessionTimer() {
  if (sessionTimer) clearTimeout(sessionTimer)
  sessionTimer = window.setTimeout(() => {
    resetSession()
  }, SESSION_TIMEOUT)
}

onMounted(() => {
  updateTime()
  timeTimer = window.setInterval(updateTime, 60000)
  startSessionTimer()
  userStore.fetchUserInfoVO()
  fetchHistory().then(() => scrollToBottom())
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (sessionTimer) clearTimeout(sessionTimer)
})

function quickSend(tag: string) {
  input.value = tag
  send()
}

async function handleNewChat() {
  try {
    const res = await aiService.chatClear()
    if (res.code === 200) {
      msgSuccess('已开始新对话')
    } else {
      msgWarning(res.message || '清除会话返回异常')
    }
  } catch (e: unknown) {
    msgError('清除会话失败: ' + getErrorMessage(e))
  }
  messages.value = [
    { role: 'ai', content: '你好！我是智能管家 🎯\n告诉我你喜欢什么，我来帮你找到最合适的~', time: getTimeStr() },
  ]
  recItems.value = []
  hasQueried.value = false
  Object.keys(cartTimers).forEach((k) => { clearTimeout(cartTimers[Number(k)]) })
  Object.keys(cartTimers).forEach((k) => delete cartTimers[Number(k)])
  Object.keys(submitting).forEach((k) => delete submitting[Number(k)])
  confirmDialogVisible.value = false
  pendingRemoveItem.value = null
  startSessionTimer()
}

function handleEnter(e: KeyboardEvent) {
  if (e.shiftKey) return
  e.preventDefault()
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return

  startSessionTimer()

  messages.value.push({ role: 'user', content: text, time: getTimeStr() })
  input.value = ''
  loading.value = true
  isThinking.value = true

  scrollToBottom()

  let aiContent = ''
  let streamDone = false

  function finishStream() {
    if (streamDone) return
    streamDone = true
    isThinking.value = false
    loading.value = false
    scrollToBottom()
  }

  aiService.chatStream(
    text,
    (chunk) => {
      aiContent += chunk
      const last = messages.value[messages.value.length - 1]
      if (last && last.role === 'ai' && !last._final) {
        last.content = aiContent
      } else {
        messages.value.push({ role: 'ai', content: aiContent, time: getTimeStr(), _final: false })
      }
      scrollToBottom()
    },
    (products) => {
      hasQueried.value = true
      recItems.value = products.map((p) => ({ ...p, goodsId: Number(p.goodsId) }))
    },
    () => {
      const last = messages.value[messages.value.length - 1]
      if (last) (last as any)._final = true
      finishStream()
    },
    (_err) => {
      if (!aiContent) {
        messages.value.push({ role: 'ai', content: '抱歉，暂时无法处理你的请求，请稍后再试~', time: getTimeStr() })
      }
      finishStream()
    },
  )
}

function goMerchant(userId: number | undefined) {
  if (!userId) return
  router.push(`/user/merchant/${userId}`)
}

function scrollToBottom() {
  nextTick(() => {
    if (msgRef.value) {
      msgRef.value.scrollTop = msgRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.page-container {
  width: 100%;
  height: calc(100vh - 100px);
  padding: 0;
  background: rgba(20, 22, 30, 0.5);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

/* ===== Main Layout ===== */
.main-layout {
  display: grid;
  grid-template-columns: 1fr 460px;
  gap: 0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ===== Chat Wrapper ===== */
.chat-wrapper {
  display: flex;
  flex-direction: column;
  background: rgba(18, 20, 28, 0.6);
  backdrop-filter: blur(8px);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
  min-height: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: rgba(26, 28, 38, 0.5);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #e5e7eb;
  font-weight: 600;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-time {
  font-size: 12px;
  color: #6a7080;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.new-chat-btn {
  color: #a5b4fc !important;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: all 0.2s;
}

.new-chat-btn:hover {
  color: #c7d2fe !important;
}

/* ===== Messages Container ===== */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 18px;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
}

.message-row {
  display: flex;
  gap: 12px;
  max-width: 85%;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar-wrap {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.avatar-label {
  font-size: 10px;
  color: #6b7280;
  text-align: center;
  white-space: nowrap;
}

.chat-avatar-img {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: rgba(30, 32, 44, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.avatar.user {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
}

.avatar.ai {
  background: linear-gradient(135deg, #059669, #10b981);
  border: none;
}

.bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-row.user .bubble-wrap {
  align-items: flex-end;
}

.bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  max-width: 100%;
}

.bubble.user {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.bubble.ai {
  background: rgba(30, 32, 44, 0.5);
  color: #e5e7eb;
  border-bottom-left-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.bubble p {
  margin: 0;
}

.msg-time {
  font-size: 11px;
  color: #4b5563;
  padding: 0 4px;
}

.thinking-bubble {
  padding: 16px 20px;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6b7280;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

/* ===== Quick Actions ===== */
.quick-actions {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  overflow-x: auto;

  &::-webkit-scrollbar {
    height: 0;
  }
}

.quick-btn {
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(99, 102, 241, 0.06);
  border: 1px solid rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.25s ease;
}

.quick-btn:hover {
  background: rgba(99, 102, 241, 0.18);
  border-color: rgba(99, 102, 241, 0.35);
  color: #fff;
  transform: translateY(-1px);
}

/* ===== Input Wrapper ===== */
.input-wrapper {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  background: rgba(14, 16, 22, 0.55);
  backdrop-filter: blur(8px);
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-textarea__inner) {
  background: rgba(30, 32, 44, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  color: #e5e7eb;
  font-size: 14px;
  line-height: 1.5;
  transition: all 0.25s;
  resize: none !important;
}

.chat-input :deep(.el-textarea__inner):focus {
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.send-btn {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.send-btn svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.send-btn.loading {
  animation: btn-loading 1s ease-in-out infinite;
}

@keyframes btn-loading {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}

/* ===== Recommend Panel ===== */
.recommend-panel {
  background: linear-gradient(180deg, rgba(30, 34, 50, 0.55), rgba(22, 26, 40, 0.55));
  backdrop-filter: blur(8px);
  padding: 20px;
  overflow-y: auto;
  height: 100%;
  border-left: 1px solid rgba(99, 102, 241, 0.1);

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2px;
  }
}

.panel-header {
  margin-bottom: 18px;
}

.panel-header h3 {
  font-size: 15px;
  color: #e5e7eb;
  margin: 0 0 4px;
  font-weight: 600;
}

.panel-sub {
  font-size: 12px;
  color: #8b8fa3;
}

/* ===== Placeholder (未询问) ===== */
.panel-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
  min-height: 400px;
}

.placeholder-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.placeholder-title {
  font-size: 18px;
  font-weight: 600;
  color: #c8cad4;
  margin: 0 0 8px;
}

.placeholder-desc {
  font-size: 13px;
  color: #7a7f92;
  line-height: 1.6;
  margin: 0 0 20px;
}

.placeholder-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.placeholder-tags span {
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  font-size: 13px;
  transition: all 0.2s;
  cursor: default;
}

.placeholder-tags span:hover {
  background: rgba(99, 102, 241, 0.2);
  border-color: rgba(99, 102, 241, 0.4);
  color: #c7d2fe;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.goods-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  animation: cardFadeIn 0.5s ease-out both;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.goods-card:hover { transform: translateY(-2px); }

.goods-img-wrap {
  position: relative;
  width: 100%;
  height: 160px;
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
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  padding: 4px;
  line-height: 1.3;
}

.card-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 8px;
  background: linear-gradient(135deg, #ef4444, #f97316);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 6px;
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

.goods-divider {
  margin: 8px 0;
  border-color: #2c2e36;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  cursor: pointer;
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

.goods-cart-badge {
  font-size: 12px;
  color: #67c23a;
  margin-left: auto;
  font-weight: 500;
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

.panel-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 60px 20px;
  min-height: 300px;
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  .recommend-panel {
    display: none;
  }
}

@media (max-width: 640px) {
  .chat-header {
    padding: 10px 14px;
  }
  .messages-container {
    padding: 12px;
    gap: 12px;
  }
  .message-row {
    max-width: 95%;
  }
  .bubble {
    padding: 10px 12px;
    font-size: 13px;
  }
  .quick-actions {
    padding: 10px 14px;
    gap: 6px;
  }
  .quick-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  .input-wrapper {
    padding: 10px 14px;
    gap: 8px;
  }
  .send-btn {
    width: 40px;
    height: 40px;
  }
  .send-btn svg {
    width: 16px;
    height: 16px;
  }
}
</style>