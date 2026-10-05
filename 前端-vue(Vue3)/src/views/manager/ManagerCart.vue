<template>
  <div class="manager-page">
    <div class="page-header">
      <h2 class="page-title">购物车管理</h2>
      <div class="header-stats">
        <span class="stat-item">共 <strong>{{ total }}</strong> 条记录</span>
        <span class="stat-divider">|</span>
        <span class="stat-item">第 <strong>{{ pageNum }}</strong> / <strong>{{ pages || 1 }}</strong> 页</span>
      </div>
    </div>

    <el-card shadow="never" class="search-card">
      <div class="search-bar" @keyup.enter="handleSearch">
        <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 140px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-input v-model="search.goodsName" placeholder="商品名" clearable style="width: 140px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-select v-model="search.launch" placeholder="上下架" clearable style="width: 120px">
          <el-option :value="true" label="上架" />
          <el-option :value="false" label="下架" />
        </el-select>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="advVisible = true">高级搜索</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="cartList" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="75" align="center" />
        <el-table-column prop="userName" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="goodsName" label="商品名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="上下架" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.launch" type="success" size="small">上架</el-tag>
            <el-tag v-else type="info" size="small">下架</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && cartList.length === 0" description="暂无购物车记录" />

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
    </el-card>

    <el-dialog append-to-body v-model="advVisible" title="高级搜索" width="500px">
      <div @keyup.enter="advVisible = false; handleSearch()">
      <el-form label-width="100px">
        <el-form-item label="ID">
          <el-input-number v-model="search.id" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="search.goodsStock" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="search.quantity" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上下架">
          <el-select v-model="search.launch" placeholder="全部" clearable style="width: 100%">
            <el-option :value="true" label="上架" />
            <el-option :value="false" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="search.createTimeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" unlink-panels style="width: 100%" />
        </el-form-item>
      </el-form>
      </div>
      <template #footer>
        <el-button @click="advVisible = false">取消</el-button>
        <el-button type="primary" @click="advVisible = false; handleSearch()">搜索</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { msgSuccess, msgError } from '../../utils/message'
import { confirm } from '../../utils/confirm'
import { managerService } from '../../services/ManagerService'
import { getErrorMessage } from '../../utils/error'
import { buildSearchParams, buildTimeRangeParams } from '../../utils/search'

const DEFAULT_SEARCH = {
  id: null as number | null,
  userName: '', goodsName: '', goodsStock: null as number | null,
  quantity: null as number | null, launch: '' as boolean | '',
  createTimeRange: null as [string, string] | null,
}

const search = reactive({ ...DEFAULT_SEARCH })
const cartList = ref<any[]>([])
const loading = ref(false)
const advVisible = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

async function fetchCarts() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getCartList(params as any)
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

function handleSearch() { pageNum.value = 1; fetchCarts() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchCarts()
}

function handlePageChange(page: number) { pageNum.value = page; fetchCarts() }

async function handleDelete(row: any) {
  const ok = await confirm({ title: '删除确认', message: '确定要删除该购物车记录吗？', type: 'warning' })
  if (!ok) return
  try {
    const res = await managerService.deleteCart(row.id)
    if (res.code === 200) { msgSuccess('已删除'); fetchCarts() }
    else msgError(res.message || '删除失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

onMounted(() => { fetchCarts() })
</script>

<style scoped>
.manager-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #e0e3ea;
}

.header-stats {
  font-size: 13px;
  color: #8890a0;
}

.header-stats strong {
  color: #5b9eff;
  font-weight: 600;
}

.stat-divider {
  margin: 0 8px;
  color: #3a3f4a;
}

.search-card, .table-card {
  background: rgba(18, 20, 28, 0.55) !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
  border-radius: 12px !important;
}

.search-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.pagination {
  margin-top: 0;
  padding: 14px 20px;
  text-align: right;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.page-text {
  margin-left: 8px;
  color: var(--el-text-color-regular);
  font-weight: 400;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.03);
  --el-table-row-hover-bg-color: rgba(91, 158, 255, 0.06);
  --el-table-border-color: rgba(255, 255, 255, 0.05);
}

:deep(.el-table th.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.03);
  color: #8890a0;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.015);
}

:deep(.el-table .el-table__row:hover > td.el-table__cell) {
  background-color: rgba(91, 158, 255, 0.06) !important;
}
</style>