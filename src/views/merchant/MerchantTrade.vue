<template>
  <div>
    <h2 class="page-title">订单管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.goodsName" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <el-table :data="tradeList" v-loading="loading" style="width: 100%">
      <el-table-column prop="goodsName" label="商品名称" min-width="160" />
      <el-table-column prop="userName" label="买家" width="120" />
      <el-table-column prop="goodsPrice" label="单价" width="100">
        <template #default="{ row }">¥{{ row.goodsPrice.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column label="总价" width="100">
        <template #default="{ row }">¥{{ row.totalPrice.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!row.cancelTime && !row.finishTime" type="danger" size="small" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="!row.cancelTime && !row.finishTime" type="success" size="small" @click="handleFinish(row)">完成</el-button>
          <el-tag v-else-if="row.cancelTime" type="danger" size="small">已取消</el-tag>
          <el-tag v-else type="success" size="small">已完成</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && tradeList.length === 0" description="暂无订单" />

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
        <el-form-item label="买家">
          <el-input v-model="search.userName" placeholder="买家名称" clearable />
        </el-form-item>
        <el-form-item label="数量范围">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="search.minQty" placeholder="最小" clearable style="flex: 1"
              @input="search.minQty = filterNumberInput(search.minQty, false)" />
            <el-input v-model="search.maxQty" placeholder="最大" clearable style="flex: 1"
              @input="search.maxQty = filterNumberInput(search.maxQty, false)" />
          </div>
        </el-form-item>
        <el-form-item label="总价范围">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="search.minTotal" placeholder="最小" clearable style="flex: 1"
              @input="search.minTotal = filterNumberInput(search.minTotal)" />
            <el-input v-model="search.maxTotal" placeholder="最大" clearable style="flex: 1"
              @input="search.maxTotal = filterNumberInput(search.maxTotal)" />
          </div>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width: 100%">
            <el-option value="pending" label="处理中" />
            <el-option value="finished" label="已完成" />
            <el-option value="canceled" label="已取消" />
          </el-select>
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
import { merchantService } from '../../services/MerchantService'
import { getErrorMessage } from '../../utils/error'
import { buildSearchParams } from '../../utils/search'
import { filterNumberInput, showSearchResult } from '../../utils/validation'
import type { TradeInfo } from '../../services/UserService'

const tradeList = ref<TradeInfo[]>([])
const search = reactive({
  goodsName: '',
  userName: '',
  minQty: '',
  maxQty: '',
  minTotal: '',
  maxTotal: '',
  status: ''
})
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)
const advVisible = ref(false)

async function fetchTrades() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const res = await merchantService.getTradeList(params as any)
    if (res.code === 200 && res.data) {
      tradeList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages

      if (search.goodsName || search.userName || search.minQty || search.maxQty || search.minTotal || search.maxTotal || search.status) {
        showSearchResult(res.data.total, search.goodsName || undefined)
      }
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchTrades()
}

function handleReset() {
  search.goodsName = ''; search.userName = ''
  search.minQty = ''; search.maxQty = ''
  search.minTotal = ''; search.maxTotal = ''
  search.status = ''
  pageNum.value = 1
  fetchTrades()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchTrades()
}

async function handleCancel(row: TradeInfo) {
  const ok = await confirm({ message: '确定要取消该订单吗？' })
  if (!ok) return
  try {
    const res = await merchantService.cancelTrade(row.id)
    if (res.code === 200) {
      msgSuccess('已取消')
      fetchTrades()
    } else if (res.code === 400 && res.message === '订单已撤销') {
      fetchTrades()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

async function handleFinish(row: TradeInfo) {
  const ok = await confirm({ message: '确定要完成该订单吗？' })
  if (!ok) return
  try {
    const res = await merchantService.finishTrade(row.id)
    if (res.code === 200) {
      msgSuccess('已完成')
      fetchTrades()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

onMounted(() => {
  fetchTrades()
})
</script>

<style scoped>
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>