<template>
  <div>
    <div class="page-header">
    <h2 class="page-title">商品浏览</h2>
    <div class="balance-badge">
      <span class="balance-label">余额</span>
      <span class="balance-value">¥{{ userStore.userInfoVO?.balance?.toFixed(2) ?? '0.00' }}</span>
    </div>
  </div>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.goodsName" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <div class="goods-grid" v-loading="loading">
      <el-card class="goods-card" shadow="hover" v-for="goods in goodsList" :key="goods.id">
        <div class="goods-img-wrap">
          <img v-if="goods.imagePath" :src="goods.imagePath" class="goods-img" />
          <div v-else class="goods-img-placeholder">暂无图片</div>
        </div>
        <div class="goods-info">
          <h3 class="goods-name">{{ goods.goodsName }}</h3>
          <p class="goods-desc" v-if="goods.describe">{{ goods.describe }}</p>
          <div class="goods-price-row">
            <span class="goods-price">¥{{ goods.goodsPrice.toFixed(2) }}</span>
            <span class="goods-stock">库存 {{ goods.goodsStock }}</span>
            <span class="goods-cart-badge" v-if="goods.cartQuantity && goods.cartQuantity > 0">🛒 已加 {{ goods.cartQuantity }}</span>
          </div>
          <el-divider class="goods-divider" />
          <div class="merchant-info" @click="goMerchant(goods.merchantId || goods.userId)">
            <div class="merchant-avatar">
              <img v-if="merchantMap[goods.userName]?.avatarPath" :src="merchantMap[goods.userName]!.avatarPath" class="avatar-img" />
              <el-icon v-else :size="16"><UserFilled /></el-icon>
            </div>
            <div class="merchant-detail">
              <span class="merchant-name">{{ goods.userName }}</span>
              <span class="merchant-addr" v-if="goods.address">📍 {{ goods.address }}</span>
            </div>
          </div>
          <div class="goods-actions">
            <div class="qty-control">
              <button class="qty-btn" @click="decreaseQty(goods.id)">−</button>
              <span class="qty-num" :class="{ active: (goods.cartQuantity ?? 0) > 0, submitting: submitting[goods.id] }">{{ goods.cartQuantity || 0 }}</span>
              <button class="qty-btn" @click="increaseQty(goods.id, goods.goodsStock)">+</button>
            </div>
            <span class="auto-hint" v-if="submitting[goods.id]">⏳</span>
            <button class="buy-now-btn" @click="openBuyDialog(goods)">立即购买</button>
          </div>
        </div>
      </el-card>
      <div v-if="!loading && goodsList.length === 0" class="empty-full">
        <el-empty description="暂无商品" />
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

    <el-dialog append-to-body v-model="advVisible" title="高级搜索" width="480px">
      <div @keyup.enter="advVisible = false; handleSearch()">
      <el-form label-width="80px">
        <el-form-item label="商家名称">
          <el-input v-model="search.userName" placeholder="商家名称" clearable />
        </el-form-item>
        <el-form-item label="价格范围">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="search.priceMin" placeholder="最低价" clearable style="flex: 1"
              @input="search.priceMin = filterNumberInput(search.priceMin)" />
            <el-input v-model="search.priceMax" placeholder="最高价" clearable style="flex: 1"
              @input="search.priceMax = filterNumberInput(search.priceMax)" />
          </div>
        </el-form-item>
        
      </el-form>
      </div>
      <template #footer>
        <el-button @click="advVisible = false">取消</el-button>
        <el-button type="primary" @click="advVisible = false; handleSearch()">搜索</el-button>
      </template>
    </el-dialog>

    <el-dialog append-to-body v-model="buyDialogVisible" width="440px" class="buy-dialog" :show-close="false">
      <div class="buy-header" v-if="buyTarget">
        <div class="buy-header-img">
          <img v-if="buyTarget.imagePath" :src="buyTarget.imagePath" />
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
            <button class="buy-step" @click="buyQty = Math.min(buyTarget!.goodsStock, buyQty + 1)">+</button>
          </div>
          <span class="buy-field-extra">库存 {{ buyTarget?.goodsStock }}</span>
        </div>

        <div class="buy-field buy-field-addr">
          <span class="buy-field-label">地址</span>
          <div class="buy-addr-selected" v-if="selectedAddress">
            <span class="buy-addr-text">{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.county }} {{ selectedAddress.detail }}</span>
            <button class="buy-addr-change" @click="addrDialogVisible = true">更换</button>
          </div>
          <div class="buy-addr-selected" v-else-if="addressLoading">
            <span class="buy-addr-text" style="color:#909399">加载中...</span>
          </div>
          <div class="buy-addr-selected" v-else>
            <span class="buy-addr-text" style="color:#909399">请选择收货地址</span>
            <button class="buy-addr-change" @click="addrDialogVisible = true">选择</button>
          </div>
        </div>

        <div class="buy-summary">
          <div class="buy-summary-row">
            <span>商品单价</span>
            <span>¥{{ buyTarget ? buyTarget.goodsPrice.toFixed(2) : '0.00' }}</span>
          </div>
          <div class="buy-summary-row">
            <span>购买数量</span>
            <span>×{{ buyQty }}</span>
          </div>
          <div class="buy-summary-divider" />
          <div class="buy-summary-row buy-summary-total">
            <span>合计</span>
            <span>¥{{ buyTarget ? (buyTarget.goodsPrice * buyQty).toFixed(2) : '0.00' }}</span>
          </div>
        </div>
      </div>

      <div class="buy-footer">
        <button class="buy-cancel" @click="buyDialogVisible = false">取消</button>
        <button class="buy-confirm" :disabled="!selectedAddressId || buyLoading" @click="handleBuyNow">
          <span v-if="!buyLoading">确认购买 ¥{{ buyTarget ? (buyTarget.goodsPrice * buyQty).toFixed(2) : '0.00' }}</span>
          <span v-else>购买中...</span>
        </button>
      </div>
    </el-dialog>

    <el-dialog append-to-body v-model="addrDialogVisible" title="选择收货地址" width="420px" class="addr-dialog">
      <div v-if="addressLoading" class="addr-dlg-loading">加载中...</div>
      <div v-else-if="addressList.length === 0" class="addr-dlg-empty">暂无收货地址</div>
      <div v-else class="addr-dlg-list">
        <div
          v-for="addr in addressList"
          :key="addr.id"
          class="addr-dlg-item"
          :class="{ active: selectedAddressId === addr.id }"
          @click="selectedAddressId = addr.id; addrDialogVisible = false"
        >
          <div class="addr-dlg-radio">
            <div v-if="selectedAddressId === addr.id" class="addr-dlg-dot" />
          </div>
          <div class="addr-dlg-info">
            <span class="addr-dlg-region">{{ addr.province }} {{ addr.city }} {{ addr.county }}</span>
            <span class="addr-dlg-detail">{{ addr.detail }}</span>
          </div>
          <span v-if="addr.isDefault" class="addr-dlg-default">默认</span>
        </div>
      </div>
    </el-dialog>

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
import { buildSearchParams } from '../../utils/search'
import { filterNumberInput, showSearchResult } from '../../utils/validation'
import type { GoodsInfo, UserInfo, AddressInfo } from '../../services/UserService'

const router = useRouter()
const userStore = useUserStore()
const goodsList = ref<GoodsInfo[]>([])
const merchantMap = ref<Record<string, UserInfo>>({})
const pendingTimers = new Map<number, ReturnType<typeof setTimeout>>()
const submitting = reactive<Record<number, boolean>>({})
const search = reactive({
  goodsName: '',
  userName: '',
  priceMin: '',
  priceMax: '',
})
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const pages = ref(0)
const advVisible = ref(false)
const cartCount = ref(0)

const buyDialogVisible = ref(false)
const buyTarget = ref<GoodsInfo | null>(null)
const buyQty = ref(1)
const buyLoading = ref(false)
const addressList = ref<AddressInfo[]>([])
const addressLoading = ref(false)
const selectedAddressId = ref<number | null>(null)
const addrDialogVisible = ref(false)

const selectedAddress = computed(() => {
  if (!selectedAddressId.value) return null
  return addressList.value.find(a => a.id === selectedAddressId.value) || null
})

async function openBuyDialog(goods: GoodsInfo) {
  buyTarget.value = goods
  buyQty.value = 1
  selectedAddressId.value = null
  buyDialogVisible.value = true
  fetchAddressList()
}

async function fetchAddressList() {
  addressLoading.value = true
  try {
    const res = await userService.getAddressList({ page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      addressList.value = res.data.list
      const defaultAddr = res.data.list.find((a) => a.isDefault)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
      }
    }
  } catch {
    // ignore
  } finally {
    addressLoading.value = false
  }
}

async function handleBuyNow() {
  if (!buyTarget.value || !selectedAddressId.value) return
  const ok = await confirm({ message: '确定购买？', type: 'warning' })
  if (!ok) return
  buyLoading.value = true
  try {
    const res = await userService.buyGoods({
      goodsId: buyTarget.value.id,
      quantity: buyQty.value,
      shippingAddressId: selectedAddressId.value,
    })
    if (res.code === 200) {
      msgSuccess('购买成功')
      buyDialogVisible.value = false
      fetchGoods()
      fetchCartCount()
    } else {
      msgError(res.message || '购买失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    buyLoading.value = false
  }
}

async function fetchGoods() {
  loading.value = true
  try {
    const raw = buildSearchParams(search, pageNum.value, pageSize.value)
    const params: Record<string, unknown> = { ...raw }
    if (search.priceMin) params.priceMin = Number(search.priceMin)
    if (search.priceMax) params.priceMax = Number(search.priceMax)
    const res = await userService.getGoodsList(params as any)
    if (res.code === 200 && res.data) {
      goodsList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
      fetchMerchantInfo(res.data.list)

      if (search.goodsName || search.userName || search.priceMin || search.priceMax) {
        showSearchResult(res.data.total, search.goodsName || undefined)
      }
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

async function fetchMerchantInfo(goodsList: GoodsInfo[]) {
  const names = [...new Set(goodsList.map((g) => g.userName))]
  const newNames = names.filter((n) => !merchantMap.value[n])
  if (newNames.length === 0) return
  try {
    const list = await userStore.fetchUserList()
    list.forEach((u) => {
      if (newNames.includes(u.userName)) {
        merchantMap.value[u.userName] = u
      }
    })
  } catch {
    // ignore
  }
}

function goMerchant(userId: number) {
  router.push({ name: 'user-merchant', params: { userId } })
}

async function fetchCartCount() {
  try {
    const res = await userService.getCartCount()
    if (res.code === 200) {
      cartCount.value = res.data
    }
  } catch {
    // ignore
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchGoods()
}

function handleReset() {
  search.goodsName = ''; search.userName = ''
  search.priceMin = ''; search.priceMax = ''
  pageNum.value = 1
  fetchGoods()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchGoods()
}

function increaseQty(goodsId: number, maxStock: number) {
  const item = goodsList.value.find((g) => g.id === goodsId)
  if (!item) return
  const val = item.cartQuantity || 0
  if (val < maxStock) {
    item.cartQuantity = val + 1
    scheduleCartSubmit(goodsId)
  }
}

function decreaseQty(goodsId: number) {
  const item = goodsList.value.find((g) => g.id === goodsId)
  if (!item) return
  const val = item.cartQuantity || 0
  if (val <= 0) return
  if (val === 1) {
    confirmRemoveFromCart(goodsId)
    return
  }
  item.cartQuantity = val - 1
  scheduleCartSubmit(goodsId)
}

async function confirmRemoveFromCart(goodsId: number) {
  const ok = await confirm({ title: '移除商品', message: '确认从购物车中删除该商品吗？', type: 'warning', confirmText: '确认删除' })
  if (!ok) {
    const item = goodsList.value.find((g) => g.id === goodsId)
    if (item) item.cartQuantity = 1
    return
  }
  const item = goodsList.value.find((g) => g.id === goodsId)
  if (item) item.cartQuantity = 0
  submitCartChange(goodsId, 0)
}

function scheduleCartSubmit(goodsId: number) {
  const existing = pendingTimers.get(goodsId)
  if (existing) clearTimeout(existing)
  pendingTimers.set(goodsId, setTimeout(() => {
    pendingTimers.delete(goodsId)
    const item = goodsList.value.find((g) => g.id === goodsId)
    if (!item) return
    submitCartChange(goodsId, item.cartQuantity || 0)
  }, 300))
}

async function submitCartChange(goodsId: number, qty: number) {
  if (qty < 0) return
  if (submitting[goodsId]) return
  submitting[goodsId] = true
  try {
    const res = await userService.handleCart({ goodId: goodsId, num: qty })

    if (res.code === 200) {
      if (qty === 0) {
        msgSuccess('已从购物车移除')
      } else {
        const item = goodsList.value.find((g) => g.id === goodsId)
        const wasZero = !item || !item.cartQuantity || item.cartQuantity === 0
        if (wasZero && qty > 0) {
          msgSuccess('加入购物车成功')
        }
      }
      fetchCartCount()
    } else {
      msgError(res.message || '操作失败')
      rollbackQty(goodsId)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
    rollbackQty(goodsId)
  } finally {
    submitting[goodsId] = false
  }
}

function rollbackQty(_goodsId: number) {
  fetchGoods()
}

onMounted(() => {
  fetchGoods()
  fetchCartCount()
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

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
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

.buy-field-addr {
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.buy-addr-selected {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.buy-addr-text {
  flex: 1;
  font-size: 13px;
  color: #d0d2d8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.buy-addr-change {
  flex-shrink: 0;
  padding: 4px 14px;
  border-radius: 6px;
  border: 1px solid rgba(99,102,241,0.25);
  background: rgba(99,102,241,0.08);
  color: #a5b4fc;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.buy-addr-change:hover {
  background: rgba(99,102,241,0.15);
  border-color: rgba(99,102,241,0.4);
}

.addr-dialog :deep(.el-dialog) {
  background: rgba(18, 20, 28, 0.98);
  backdrop-filter: blur(32px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
}

.addr-dialog :deep(.el-dialog__title) {
  color: #f1f2f4;
  font-weight: 700;
}

.addr-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255,255,255,0.06);
  margin: 0;
  padding: 18px 20px;
}

.addr-dialog :deep(.el-dialog__body) {
  padding: 16px;
}

.addr-dlg-loading,
.addr-dlg-empty {
  text-align: center;
  padding: 32px 0;
  color: #909399;
  font-size: 13px;
}

.addr-dlg-list {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.addr-dlg-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.addr-dlg-item:hover {
  background: rgba(255,255,255,0.04);
}

.addr-dlg-item.active {
  background: rgba(99,102,241,0.06);
  border-color: rgba(99,102,241,0.2);
}

.addr-dlg-radio {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.addr-dlg-item.active .addr-dlg-radio {
  border-color: #6366f1;
}

.addr-dlg-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.addr-dlg-info {
  flex: 1;
  min-width: 0;
}

.addr-dlg-region {
  font-size: 14px;
  font-weight: 500;
  color: #d0d2d8;
}

.addr-dlg-detail {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.addr-dlg-default {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #fbbf24;
  background: rgba(251,191,36,0.1);
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid rgba(251,191,36,0.15);
  flex-shrink: 0;
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