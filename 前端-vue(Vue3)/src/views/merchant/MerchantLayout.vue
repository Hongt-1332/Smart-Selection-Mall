<template>
  <el-container class="merchant-layout">
    <el-aside width="220px" class="merchant-aside">
      <div class="sidebar-header">
        <el-icon :size="22" color="#fff"><Shop /></el-icon>
        <span>商户管理</span>
      </div>
      <el-menu :default-active="activePath" @select="handleSelect">
        <el-menu-item index="/merchant">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/trade">
          <el-icon><List /></el-icon>
          <span>订单管理<em v-if="unfinishedCount > 0" class="nav-badge">{{ unfinishedCount }}</em></span>
        </el-menu-item>
        <el-menu-item index="/merchant/ai">
          <el-icon><Cpu /></el-icon>
          <span>AI上传</span>
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
import { computed, onErrorCaptured, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Goods, List, Shop, Cpu } from '@element-plus/icons-vue'
import { msgError } from '../../utils/message'
import { managerService } from '../../services/ManagerService'

const route = useRoute()
const router = useRouter()
const activePath = computed(() => route.path)
const unfinishedCount = ref(0)

function handleSelect(index: string) {
  if (index !== route.path) {
    router.push(index)
  }
}

async function fetchUnfinishedCount() {
  try {
    const res = await managerService.getUnfinishedOrderCount()
    if (res.code === 200) unfinishedCount.value = res.data
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchUnfinishedCount()
})

onErrorCaptured((err) => {
  console.error('[Layout] 子页面渲染异常', err)
  msgError('页面渲染错误：' + String(err))
  return false
})
</script>

<style scoped>
.merchant-aside {
  background: rgba(18, 20, 28, 0.5);
  backdrop-filter: blur(12px);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  min-height: calc(100vh - 60px);
}

.merchant-aside .el-menu {
  background: transparent;
  border-right: none;
}

.merchant-aside .el-menu-item {
  color: rgba(255, 255, 255, 0.55);
  font-size: 14px;
  margin: 2px 8px;
  border-radius: 6px;
}

.merchant-aside .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.85);
}

.merchant-aside .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(103,194,58,0.25), rgba(91,141,239,0.12));
  color: #fff;
  border-radius: 6px;
  margin: 2px 8px;
  width: auto;
  border-left: 3px solid;
  border-image: linear-gradient(180deg, #67c23a, #5b8def) 1;
  box-shadow: 0 0 20px rgba(103,194,58,0.1);
}

.merchant-aside .el-menu-item.is-active .el-icon {
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
  border-image: linear-gradient(180deg, #67c23a, #5b8def, #e6a23c) 1;
  background: linear-gradient(90deg, rgba(103,194,58,0.06), rgba(91,141,239,0.04), transparent);
}

.nav-badge {
  display: inline-block;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  font-style: normal;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 8px;
  vertical-align: middle;
  line-height: 1.4;
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
  .merchant-layout {
    flex-direction: column;
  }

  .merchant-aside {
    width: 100% !important;
    min-height: auto;
  }

  .merchant-aside .el-menu {
    display: flex;
    flex-wrap: wrap;
    gap: 2px;
  }

  .merchant-aside .el-menu-item {
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