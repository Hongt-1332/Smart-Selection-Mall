<template>
  <div>
    <div class="page-header">
    <h2 class="page-title">智能购物</h2>
    <div class="balance-badge">
      <span class="balance-label">余额</span>
      <span class="balance-value">¥{{ userStore.userInfoVO?.balance?.toFixed(2) ?? '0.00' }}</span>
    </div>
  </div>

    <div class="cart-toolbar" v-if="cartList.length > 0">
      <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
      <span class="cart-summary">已选 <strong>{{ selectedIds.length }}</strong> 件，合计 <strong class="total-price">¥{{ selectedTotal.toFixed(2) }}</strong></span>
      <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleBuy">结算</el-button>
    </div>

    <div class="goods-grid" v-loading="loading">
      <el-card
        class="goods-card"
        shadow="hover"
        v-for="item in cartList"
        :key="item.id"
        :class="{ selected: selectedIds.includes(item.id) }"
      >
        <div class="goods-img-wrap">
          <img v-if="item.imagePath" :src="item.imagePath" class="goods-img" />
          <div v-else class="goods-img-placeholder">暂无图片</div>
        </div>
        <div class="goods-info">
          <h3 class="goods-name">{{ item.goodsName }}</h3>
          <p class="goods-desc" v-if="item.describe">{{ item.describe }}</p>
          <div class="goods-price-row">
            <span class="goods-price">¥{{ item.goodsPrice.toFixed(2) }}</span>
            <span class="goods-stock">库存 {{ item.goodsStock }}</span>
            <span class="goods-subtotal">小计 ¥{{ (item.goodsPrice * item.quantity).toFixed(2) }}</span>
          </div>
          <el-divider class="goods-divider" />
          <div class="merchant-info" @click="goMerchant(item.merchantId || item.userId)">
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
            <el-checkbox
              :model-value="selectedIds.includes(item.id)"
              @change="(val: boolean) => toggleSelect(item.id, val)"
            />
            <div class="qty-control">
              <button class="qty-btn" @click="decreaseQty(item)">−</button>
              <span class="qty-num" :class="{ active: item.quantity > 0, submitting: submitting[item.id] }">{{ item.quantity }}</span>
              <button class="qty-btn" @click="increaseQty(item)">+</button>
            </div>
            <span class="auto-hint" v-if="submitting[item.id]">⏳</span>
            <button class="delete-btn" @click="handleDelete(item)">删除</button>
          </div>
        </div>
      </el-card>
      <div v-if="!loading && cartList.length === 0" class="empty-full">
        <el-empty description="智能购物车是空的" />
      </div>
    </div>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      layout="prev, pager, next, total, slot"
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      :page-count="pages"
      @current-change="handlePageChange"
    >
      <span class="page-text">共 {{ pages }} 页</span>
    </el-pagination>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { msgSuccess, msgError } from '../../utils/message'
import { confirm } from '../../utils/confirm'
import { UserFilled } from '@element-plus/icons-vue'
import { userService } from '../../services/UserService'
import { useUserStore } from '../../stores/user'
import { getErrorMessage } from '../../utils/error'
import type { CartInfo } from '../../services/UserService'

const router = useRouter()
const userStore = useUserStore()
const cartList = ref<CartInfo[]>([])
const loading = ref(false)
const pendingTimers = new Map<number, ReturnType<typeof setTimeout>>()
const submitting = reactive<Record<number, boolean>>({})
const selectedIds = ref<number[]>([])
const selectAll = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const pages = ref(0)

const selectedTotal = computed(() => {
  return cartList.value
    .filter((item) => selectedIds.value.includes(item.id))
    .reduce((sum, item) => sum + item.goodsPrice * item.quantity, 0)
})

async function fetchCart() {
  loading.value = true
  try {
    const res = await userService.getCartList({ page: pageNum.value, size: pageSize.value })
    if (res.code === 200 && res.data) {
      cartList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchCart()
}

function toggleSelect(id: number, checked: boolean) {
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter((i) => i !== id)
  }
  selectAll.value = cartList.value.length > 0 && selectedIds.value.length === cartList.value.length
}

function handleSelectAll(checked: boolean) {
  if (checked) {
    selectedIds.value = cartList.value.map((item) => item.id)
  } else {
    selectedIds.value = []
  }
}

function goMerchant(userId: number) {
  router.push({ name: 'user-merchant', params: { userId } })
}

function increaseQty(item: CartInfo) {
  if (item.quantity < item.goodsStock) {
    item.quantity += 1
    scheduleUpdate(item.id)
  }
}

function decreaseQty(item: CartInfo) {
  if (item.quantity <= 1) {
    confirmRemoveFromCart(item)
    return
  }
  item.quantity -= 1
  scheduleUpdate(item.id)
}

async function confirmRemoveFromCart(item: CartInfo) {
  const ok = await confirm({ title: '移除商品', message: '确认从购物车中删除该商品吗？', type: 'warning', confirmText: '确认删除' })
  if (!ok) { item.quantity = 1; return }
  try {
    const res = await userService.handleCart({ goodId: item.cartId, num: 0 })
    if (res.code === 200) {
      msgSuccess('删除成功')
      selectedIds.value = selectedIds.value.filter((id) => id !== item.id)
      fetchCart()
    } else {
      msgError(res.message)
      item.quantity = 1
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
    item.quantity = 1
  }
}

function scheduleUpdate(cartId: number) {
  const existing = pendingTimers.get(cartId)
  if (existing) clearTimeout(existing)
  pendingTimers.set(cartId, setTimeout(() => {
    pendingTimers.delete(cartId)
    const item = cartList.value.find((c) => c.id === cartId)
    if (!item) return
    submitQuantityChange(item)
  }, 300))
}

async function submitQuantityChange(item: CartInfo) {
  const cartId = item.id
  if (submitting[cartId]) return
  submitting[cartId] = true
  try {
    const res = await userService.handleCart({ goodId: item.cartId, num: item.quantity })
    if (res.code === 200) {
      if (item.quantity === 0) {
        msgSuccess('已从购物车移除')
      }
    } else {
      msgError(res.message)
      fetchCart()
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
    fetchCart()
  } finally {
    submitting[cartId] = false
  }
}

async function handleDelete(item: CartInfo) {
  const ok = await confirm({ message: '确定要删除该商品吗？' })
  if (!ok) return
  try {
    const res = await userService.handleCart({ goodId: item.cartId, num: 0 })
    if (res.code === 200) {
      msgSuccess('删除成功')
      selectedIds.value = selectedIds.value.filter((id) => id !== item.id)
      fetchCart()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

async function handleBuy() {
  if (selectedIds.value.length === 0) return
  const ok = await confirm({
    title: '确认结算',
    message: `确认结算 ${selectedIds.value.length} 件商品，合计 ¥${selectedTotal.value.toFixed(2)}？`,
    type: 'info',
    confirmText: '确认购买'
  })
  if (!ok) return
  try {
    const cartIds = cartList.value
      .filter((item) => selectedIds.value.includes(item.id))
      .map((item) => item.cartId)
    const res = await userService.buyFromCart(cartIds)
    if (res.code === 200) {
      msgSuccess(res.data || '购买成功')
      selectedIds.value = []
      selectAll.value = false
      fetchCart()
    } else {
      msgError(res.message || '购买失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

onMounted(() => {
  fetchCart()
  userStore.fetchUserInfoVO()
})

onUnmounted(() => {
  pendingTimers.forEach((t) => clearTimeout(t))
  pendingTimers.clear()
})
</script>

<style scoped>
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }

.balance-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #fef3e2, #fdebd0);
  border: 1px solid #f5dab1;
  border-radius: 20px;
  padding: 4px 14px;
  flex-shrink: 0;
}
.balance-label {
  font-size: 12px;
  color: #b88230;
}
.balance-value {
  font-size: 15px;
  font-weight: 700;
  color: #e6a23c;
}

.cart-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(26, 28, 36, 0.35);
  backdrop-filter: blur(8px);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.cart-summary {
  flex: 1;
  font-size: 14px;
  color: #c0c4cc;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.goods-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.goods-card.selected {
  box-shadow: 0 0 0 2px #5b8def;
}

.goods-card:hover { transform: translateY(-2px); }

.goods-img-wrap {
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 6px 6px 0 0;
  position: relative;
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

.goods-subtotal {
  font-size: 12px;
  color: #e6a23c;
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

.goods-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #2c2e36;
}

.delete-btn {
  padding: 4px 14px;
  border-radius: 6px;
  border: 1px solid rgba(245,108,108,0.3);
  background: rgba(245,108,108,0.1);
  color: #f56c6c;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.delete-btn:hover {
  background: rgba(245,108,108,0.2);
  border-color: rgba(245,108,108,0.5);
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

.goods-actions :deep(.el-checkbox__inner) {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid rgba(255, 255, 255, 0.18);
  background: rgba(26, 28, 36, 0.5);
  transition: all 0.25s;
}

.goods-actions :deep(.el-checkbox__inner::after) {
  width: 6px;
  height: 10px;
  left: 6px;
  top: 2px;
  border-width: 2px;
}

.goods-actions :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: #6366f1;
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.4);
}

.goods-actions :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #fff;
}

.goods-actions :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: #6366f1;
}

.cart-toolbar :deep(.el-checkbox__inner) {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid rgba(255, 255, 255, 0.18);
  background: rgba(26, 28, 36, 0.5);
  transition: all 0.25s;
}

.cart-toolbar :deep(.el-checkbox__inner::after) {
  width: 6px;
  height: 10px;
  left: 6px;
  top: 2px;
  border-width: 2px;
}

.cart-toolbar :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: #6366f1;
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.4);
}

.cart-toolbar :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #fff;
}

.cart-toolbar :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: #6366f1;
}

.cart-toolbar :deep(.el-checkbox__label) {
  color: #c0c4cc;
  font-size: 14px;
  font-weight: 500;
}

.empty-full {
  grid-column: 1 / -1;
}

.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
  }
  .goods-img-wrap { height: 140px; }
  .goods-price { font-size: 16px; }
  .goods-actions { flex-wrap: wrap; gap: 6px; }
}
</style>