<template>
  <div>
    <h2 class="page-title">VIP交易管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-input-number v-model="search.level" :min="0" :step="1" controls-position="right" placeholder="VIP等级" style="width: 130px" @keyup.enter="handleSearch" />
      <el-select v-model="search.delete" placeholder="是否删除" clearable style="width: 120px">
        <el-option :value="false" label="未删除" />
        <el-option :value="true" label="已删除" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <el-table :data="tradeList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userName" label="用户名" min-width="110" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" min-width="80" align="center" />
      <el-table-column prop="level" label="VIP等级" min-width="80" align="center" />
      <el-table-column prop="money" label="金额" min-width="100">
        <template #default="{ row }">¥{{ row.money?.toFixed(2) ?? '0.00' }}</template>
      </el-table-column>
      <el-table-column prop="num" label="数量" min-width="80" align="center" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="已删除" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.delete" type="danger" size="small">是</el-tag>
          <el-tag v-else type="success" size="small">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" size="small" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && tradeList.length === 0" description="暂无VIP交易" />

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

    <el-dialog append-to-body v-model="advVisible" title="高级搜索" width="500px">
      <div @keyup.enter="advVisible = false; handleSearch()">
      <el-form label-width="100px">
        <el-form-item label="ID">
          <el-input-number v-model="search.id" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="search.moneyStart" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最低价" />
          <span style="margin: 0 8px">~</span>
          <el-input-number v-model="search.moneyEnd" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最高价" />
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

    <el-dialog append-to-body v-model="editVisible" title="编辑VIP交易" width="500px" @open="handleEditOpen">
      <el-form :model="editForm" ref="editFormRef" label-width="100px" v-if="editVisible" @keyup.enter="handleSave">
        <el-form-item label="用户ID">
          <el-input v-model.number="editForm.userId" type="number" min="1" step="1" />
        </el-form-item>
        <el-form-item label="VIP等级">
          <el-input v-model.number="editForm.level" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input v-model.number="editForm.money" type="number" min="0" step="0.01" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model.number="editForm.num" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="editForm.createTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { msgSuccess, msgError } from '../../utils/message'
import { managerService } from '../../services/ManagerService'
import { getErrorMessage } from '../../utils/error'
import { buildSearchParams, buildTimeRangeParams } from '../../utils/search'

const DEFAULT_SEARCH = {
  id: null as number | null,
  userName: '', level: null as number | null,
  moneyStart: null as number | null,
  moneyEnd: null as number | null,
  createTimeRange: null as [string, string] | null,
  delete: '' as boolean | '',
}

const search = reactive({ ...DEFAULT_SEARCH })
const tradeList = ref<any[]>([])
const loading = ref(false)
const advVisible = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

const editVisible = ref(false)
const saveLoading = ref(false)
const editFormRef = ref()
const editTargetRow = ref<any>(null)

const DEFAULT_EDIT_FORM = {
  userId: 0 as number,
  level: 0 as number,
  money: 0 as number,
  num: 0 as number,
  createTime: '',
}

const editForm = reactive({ ...DEFAULT_EDIT_FORM })

async function fetchTrades() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getVipTradeList(params as any)
    if (res.code === 200 && res.data) {
      tradeList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchTrades() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchTrades()
}

function handlePageChange(page: number) { pageNum.value = page; fetchTrades() }

function openEditDialog(row: any) {
  editTargetRow.value = row
  editVisible.value = true
}

function handleEditOpen() {
  const row = editTargetRow.value
  if (!row) return
  editForm.userId = row.userId ?? 0
  editForm.level = row.level ?? 0
  editForm.money = row.money ?? 0
  editForm.num = row.num ?? 0
  editForm.createTime = row.createTime ?? ''
}

async function handleSave() {
  saveLoading.value = true
  try {
    const row = editTargetRow.value
    const body: Record<string, unknown> = { id: row.id }
    if (editForm.userId !== undefined && editForm.userId !== null) body.userId = editForm.userId
    if (editForm.level !== undefined && editForm.level !== null) body.level = editForm.level
    if (editForm.money !== undefined && editForm.money !== null) body.money = editForm.money
    if (editForm.num !== undefined && editForm.num !== null) body.num = editForm.num
    if (editForm.createTime) body.createTime = editForm.createTime

    const res = await managerService.updateVipTrade(body)
    if (res.code === 200) {
      msgSuccess('修改成功')
      editVisible.value = false
      fetchTrades()
    } else {
      msgError(res.message || '修改失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => { fetchTrades() })
</script>

<style scoped>
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>