<template>
  <el-container class="manager-layout">
    <el-aside width="220px" class="manager-aside">
      <div class="sidebar-header">
        <el-icon :size="22" color="#fff"><Setting /></el-icon>
        <span>管理控制台</span>
      </div>
      <el-menu :default-active="activePath" @select="handleSelect">
        <el-menu-item index="/manager/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/trade">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/cart">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/address">
          <el-icon><Location /></el-icon>
          <span>地址管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/vip-config">
          <el-icon><TrophyBase /></el-icon>
          <span>VIP配置</span>
        </el-menu-item>
        <el-menu-item index="/manager/vip-trade">
          <el-icon><Money /></el-icon>
          <span>VIP交易</span>
        </el-menu-item>
        <el-menu-item index="/manager/ai">
          <el-icon><Cpu /></el-icon>
          <span>AI管理</span>
        </el-menu-item>
        <el-menu-item index="/manager/history-goods">
          <el-icon><Timer /></el-icon>
          <span>历史商品</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <div class="page-container">
        <RouterView :key="$route.fullPath" />
      </div>
    </el-main>
    <ConfirmDialog />
  </el-container>
</template>

<script setup lang="ts">
import ConfirmDialog from '../../components/ConfirmDialog.vue'
import { computed, onErrorCaptured } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Goods, List, ShoppingCart, Location, Setting, TrophyBase, Money, Cpu, Timer } from '@element-plus/icons-vue'
import { msgError } from '../../utils/message'

const route = useRoute()
const router = useRouter()
const activePath = computed(() => route.path)

function handleSelect(index: string) {
  if (index !== route.path) {
    router.push(index)
  }
}

onErrorCaptured((err) => {
  console.error('[Layout] 子页面渲染异常', err)
  msgError('页面渲染错误：' + String(err))
  return false
})
</script>

<style scoped>
.manager-aside {
  background: rgba(18, 20, 28, 0.5);
  backdrop-filter: blur(12px);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  min-height: calc(100vh - 60px);
}

.manager-aside .el-menu {
  background: transparent;
  border-right: none;
}

.manager-aside .el-menu-item {
  color: rgba(255, 255, 255, 0.55);
  font-size: 14px;
  margin: 2px 8px;
  border-radius: 6px;
}

.manager-aside .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.85);
}

.manager-aside .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(91,141,239,0.25), rgba(211,84,92,0.12));
  color: #fff;
  border-radius: 6px;
  margin: 2px 8px;
  width: auto;
  border-left: 3px solid;
  border-image: linear-gradient(180deg, #5b8def, #d3545c) 1;
  box-shadow: 0 0 20px rgba(91,141,239,0.1);
}

.manager-aside .el-menu-item.is-active .el-icon {
  color: #7aa3f2;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  color: #e5e6e8;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  margin-bottom: 8px;
  border-left: 3px solid;
  border-image: linear-gradient(180deg, #5b8def, #d3545c, #e6a23c, #67c23a) 1;
  background: linear-gradient(90deg, rgba(91,141,239,0.06), rgba(211,84,92,0.04), transparent);
}

.el-main {
  padding: 20px;
  background: transparent;
}

.page-container {
  background: rgba(20, 22, 30, 0.45);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 24px;
  min-height: calc(100vh - 100px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  overflow-x: hidden;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .manager-layout {
    flex-direction: column;
  }

  .manager-aside {
    width: 100% !important;
    min-height: auto;
  }

  .manager-aside .el-menu {
    display: flex;
    flex-wrap: wrap;
    gap: 2px;
  }

  .manager-aside .el-menu-item {
    flex: 1;
    min-width: 80px;
    justify-content: center;
    font-size: 12px;
    margin: 2px;
  }

  .sidebar-header {
    padding: 12px 16px;
    font-size: 14px;
    border-left: none;
  }

  .el-main {
    padding: 12px;
  }

  .page-container {
    padding: 16px;
    min-height: auto;
  }
}
</style>