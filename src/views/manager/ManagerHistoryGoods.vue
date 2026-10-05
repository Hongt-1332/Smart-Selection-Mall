<template>
  <div>
    <h2 class="page-title">历史商品管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input-number v-model="search.goodsId" :min="1" :step="1" controls-position="right" placeholder="搜索ID" style="width: 180px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="goodsList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userName" label="用户" width="100" show-overflow-tooltip />
      <el-table-column prop="goodsName" label="商品名" width="120" show-overflow-tooltip />
      <el-table-column prop="describe" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column prop="goodsPrice" label="价格" width="90">
        <template #default="{ row }">¥{{ row.goodsPrice?.toFixed(2) ?? '0.00' }}</template>
      </el-table-column>
      <el-table-column prop="goodsStock" label="库存" width="70" align="center" />
      <el-table-column prop="lunch" label="上架" width="70" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.lunch" type="warning" size="small">是</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="imagePath" label="图片" width="70" align="center">
        <template #default="{ row }">
          <img v-if="row.imagePath" :src="row.imagePath" class="goods-avatar" />
          <span v-else class="no-img">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="已删除" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.delete" type="danger" size="small">是</el-tag>
          <el-tag v-else type="success" size="small">否</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && goodsList.length === 0" description="暂无历史商品" />

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
import { ref, reactive, onMounted } from 'vue'
import { msgError } from '../../utils/message'
import { managerService } from '../../services/ManagerService'
import { getErrorMessage } from '../../utils/error'
import { buildSearchParams } from '../../utils/search'

const DEFAULT_SEARCH = {
  goodsId: null as number | null,
}

const search = reactive({ ...DEFAULT_SEARCH })
const goodsList = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

async function fetchGoods() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const res = await managerService.getHistoryGoodsList(params as any)
    if (res.code === 200 && res.data) {
      goodsList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchGoods() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchGoods()
}

function handlePageChange(page: number) { pageNum.value = page; fetchGoods() }

onMounted(() => { fetchGoods() })
</script>

<style scoped>
.goods-avatar { width: 36px; height: 36px; border-radius: 4px; object-fit: cover; }
.no-img { color: #909399; font-size: 12px; }
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>