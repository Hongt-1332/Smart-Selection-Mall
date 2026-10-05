<template>
  <div>
    <h2 class="page-title">VIP 会员</h2>

    <div class="vip-banner">
      <div class="vip-banner-bg" />
      <div class="vip-banner-content">
        <div class="crown-icon">??</div>
        <h1>升级 VIP，解锁专属特权</h1>
        <p>享受更多商品折扣、优先发货、专属客服等尊享服务</p>
      </div>
    </div>

    <div class="vip-plans">
      <el-card
        v-for="plan in plans"
        :key="plan.id"
        :class="['plan-card', { featured: plan.featured }]"
        shadow="hover"
      >
        <div class="plan-icon">{{ plan.icon }}</div>
        <h3 class="plan-name">{{ plan.name }}</h3>
        <div class="plan-price">
          <span class="currency">￥</span>
          <span class="amount">{{ plan.price }}</span>
          <span class="period">/ {{ plan.period }}</span>
        </div>

        <el-divider class="plan-divider" />

        <ul class="plan-benefits">
          <li v-for="(benefit, bi) in plan.benefits" :key="bi">
            <el-icon class="benefit-icon"><Check /></el-icon>
            <span>{{ benefit }}</span>
          </li>
        </ul>

        <el-button
          :type="plan.featured ? 'warning' : 'primary'"
          size="large"
          class="plan-btn"
          :loading="loading === plan.id"
          @click="handleBuy(plan)"
        >
          {{ plan.featured ? '立即开通' : '选择此方案' }}
        </el-button>
      </el-card>
    </div>

    <el-dialog append-to-body v-model="payVisible" title="确认购买" width="440px" class="pay-dialog">
      <div class="pay-info" v-if="selectedPlan">
        <div class="pay-plan-name">{{ selectedPlan.icon }} {{ selectedPlan.name }}</div>
        <div class="pay-price">￥{{ selectedPlan.price }}</div>
        <div class="pay-duration">有效期：{{ selectedPlan.period }}</div>
        <el-divider />
        <div class="pay-benefits">
          <div v-for="(b, bi) in selectedPlan.benefits" :key="bi" class="pay-benefit-item">
            <el-icon color="#67c23a"><Check /></el-icon>
            <span>{{ b }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmPay" :loading="payLoading">确认支付 ￥{{ selectedPlan?.price }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { msgSuccess, msgError } from '../../utils/message'
import { Check } from '@element-plus/icons-vue'
import { vipService, type VipConfig } from '../../services/VipService'
import { getErrorMessage } from '../../utils/error'

interface VipPlan {
  id: number
  level: number
  name: string
  icon: string
  price: number
  period: string
  featured: boolean
  benefits: string[]
}

const plans = ref<VipPlan[]>([])
const loading = ref<number | false>(false)
const payVisible = ref(false)
const payLoading = ref(false)
const selectedPlan = ref<VipPlan | null>(null)

function buildBenefits(config: VipConfig): string[] {
  const b: string[] = []
  if (config.maxAddressQuantity > 0) b.push(`最多 ${config.maxAddressQuantity} 个收货地址`)
  if (config.monthlyUpdateGoods > 0) b.push(`每月 ${config.monthlyUpdateGoods} 次商品上传`)
  if (config.monthlyUpdateAvatar > 0) b.push(`每月 ${config.monthlyUpdateAvatar} 次头像更新`)
  if (config.monthlyUpdateBackground > 0) b.push(`每月 ${config.monthlyUpdateBackground} 次背景更新`)
  if (config.maxCartQuantity > 0) b.push(`购物车上限 ${config.maxCartQuantity} 件`)
  if (config.maxGoodsQuantity > 0) b.push(`最多上架 ${config.maxGoodsQuantity} 件商品`)
  if (config.maxAiQuantity > 0) b.push(`每月 ${config.maxAiQuantity} 次AI推荐`)
  b.push('VIP 专属标识')
  return b
}

async function fetchVipConfigs() {
  try {
    const res = await vipService.getVipConfig()
    if (res.code === 200 && res.data) {
      const configs = [...res.data]
      const sorted = configs.sort((a, b) => a.level - b.level)
      plans.value = sorted.map((config) => ({
        id: config.level,
        level: config.level,
        name: config.levelName || `Lv.${config.level} 会员`,
        icon: '?',
        price: config.price,
        period: `Lv.${config.level}`,
        featured: config.level === 3,
        benefits: buildBenefits(config),
      }))
    }
  } catch {
    // ignore
  }
}

function handleBuy(plan: VipPlan) {
  selectedPlan.value = plan
  payVisible.value = true
}

async function confirmPay() {
  if (!selectedPlan.value) return
  payLoading.value = true
  try {
    const res = await vipService.buyVip(selectedPlan.value.level, selectedPlan.value.price)
    if (res.code === 200) {
      msgSuccess(`已成功开通${selectedPlan.value.name}！`)
      payVisible.value = false
    } else {
      msgError(res.message || '购买失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    payLoading.value = false
  }
}

onMounted(() => {
  fetchVipConfigs()
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  color: #e5e6e8;
  margin: 0 0 24px;
  font-weight: 600;
  letter-spacing: 1px;
}

.vip-banner {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 32px;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vip-banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 25%, #1a1a2e 50%, #1a1a2e 75%, #2d1b2e 100%);
  z-index: 0;
}

.vip-banner-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(230, 162, 60, 0.15) 0%, transparent 40%),
              radial-gradient(circle at 70% 50%, rgba(91, 141, 239, 0.12) 0%, transparent 40%),
              radial-gradient(circle at 50% 80%, rgba(211, 84, 92, 0.1) 0%, transparent 30%);
  animation: bannerGlow 8s ease-in-out infinite;
}

@keyframes bannerGlow {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(5%, -3%); }
}

.vip-banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 20px;
}

.crown-icon {
  font-size: 48px;
  margin-bottom: 12px;
  animation: crownFloat 2s ease-in-out infinite;
}

@keyframes crownFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.vip-banner-content h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #e6a23c, #f5d78e, #e6a23c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}

.vip-banner-content p {
  font-size: 15px;
  color: #a0a4b8;
  margin: 0;
}

.vip-plans {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.plan-card {
  position: relative;
  border-radius: 14px;
  text-align: center;
  padding: 32px 24px 24px;
  transition: all 0.3s ease;
  background: rgba(22, 24, 34, 0.35) !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
  backdrop-filter: blur(8px);
}

.plan-card:hover {
  transform: translateY(-6px);
  border-color: rgba(230, 162, 60, 0.3) !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
}

.plan-card.featured {
  border: 2px solid rgba(230, 162, 60, 0.5) !important;
  background: linear-gradient(180deg, rgba(230, 162, 60, 0.08), rgba(22, 24, 34, 0.35)) !important;
  transform: scale(1.04);
}

.plan-card.featured:hover {
  transform: scale(1.04) translateY(-6px);
}

.plan-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  white-space: nowrap;
}

.plan-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.plan-name {
  font-size: 18px;
  color: #e5e6e8;
  margin: 0 0 12px;
  font-weight: 600;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  margin-bottom: 4px;
}

.currency {
  font-size: 20px;
  color: #e6a23c;
  font-weight: 600;
}

.amount {
  font-size: 42px;
  color: #e6a23c;
  font-weight: 700;
  line-height: 1;
}

.period {
  font-size: 14px;
  color: #909399;
}

.plan-divider {
  margin: 20px 0;
  border-color: rgba(255, 255, 255, 0.06);
}

.plan-benefits {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
  text-align: left;
}

.plan-benefits li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #c0c4cc;
}

.benefit-icon {
  color: #67c23a;
  font-size: 16px;
  flex-shrink: 0;
}

.plan-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
}

.pay-info {
  text-align: center;
  padding: 8px 0;
}

.pay-plan-name {
  font-size: 18px;
  color: #e5e6e8;
  font-weight: 600;
  margin-bottom: 8px;
}

.pay-price {
  font-size: 48px;
  color: #e6a23c;
  font-weight: 700;
  margin-bottom: 4px;
}

.pay-duration {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.pay-benefits {
  text-align: left;
  padding: 0 20px;
}

.pay-benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
  font-size: 13px;
  color: #c0c4cc;
}

@media (max-width: 900px) {
  .vip-plans {
    grid-template-columns: 1fr;
    max-width: 420px;
    margin: 0 auto;
  }

  .plan-card.featured {
    transform: none;
  }

  .plan-card.featured:hover {
    transform: translateY(-6px);
  }

  .vip-banner {
    height: 180px;
  }

  .vip-banner-content h1 {
    font-size: 22px;
  }
}
</style>