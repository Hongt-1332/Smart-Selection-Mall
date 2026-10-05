<template>
  <div>
    <h2 class="page-title">商品管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.goodsName" placeholder="商品名" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-input v-model="search.merchantName" placeholder="商家名" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="search.delete" placeholder="是否删除" clearable style="width: 120px">
        <el-option :value="false" label="未删除" />
        <el-option :value="true" label="已删除" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <el-table :data="goodsList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="goodsName" label="商品名" width="120" show-overflow-tooltip />
      <el-table-column prop="userName" label="卖家" width="100" show-overflow-tooltip />
      <el-table-column prop="describe" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column prop="goodsPrice" label="价格" width="90">
        <template #default="{ row }">¥{{ row.goodsPrice?.toFixed(2) ?? '0.00' }}</template>
      </el-table-column>
      <el-table-column prop="goodsStock" label="库存" width="70" align="center" />
      <el-table-column prop="launch" label="上下架" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.launch" type="success" size="small">上架</el-tag>
          <el-tag v-else type="info" size="small">下架</el-tag>
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
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button v-if="!row.delete" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
          <el-button v-else size="small" type="warning" @click="handleUnban(row)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && goodsList.length === 0" description="暂无商品" />

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
        <el-form-item label="地址">
          <el-input v-model="search.address" clearable />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="search.describe" clearable />
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input-number v-model="search.priceStart" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最低价" />
          <span style="margin: 0 8px">~</span>
          <el-input-number v-model="search.priceEnd" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最高价" />
        </el-form-item>
        <el-form-item label="上下架">
          <el-select v-model="search.launch" placeholder="是否上架" clearable style="width: 100%">
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

    <el-dialog append-to-body v-model="dialogVisible" title="编辑商品" width="600px">
      <el-form :model="form" label-width="100px" @keyup.enter="handleSave">
        <el-form-item label="商品名">
          <el-input v-model="form.goodsName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.describe" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model.number="form.goodsPrice" type="number" min="0" step="0.01" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model.number="form.goodsStock" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item label="上下架">
          <el-select v-model="form.launch" clearable style="width: 200px">
            <el-option :value="true" label="上架" />
            <el-option :value="false" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片路径">
          <el-input v-model="form.imagePath" />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="form.createTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="已删除">
          <el-select v-model="form.delete" clearable style="width: 200px">
            <el-option :value="true" label="是" />
            <el-option :value="false" label="否" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
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
  merchantName: '', goodsName: '', address: '',
  describe: '', priceStart: null as number | null,
  priceEnd: null as number | null, launch: '' as boolean | '',
  createTimeRange: null as [string, string] | null,
  delete: '' as boolean | '',
}

const search = reactive({ ...DEFAULT_SEARCH })
const goodsList = ref<any[]>([])
const loading = ref(false)
const advVisible = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const saveLoading = ref(false)

const DEFAULT_FORM = {
  goodsName: '', describe: '', goodsPrice: 0 as number,
  goodsStock: 0 as number, launch: false as boolean,
  imagePath: '', createTime: '', delete: false as boolean,
}

const form = reactive({ ...DEFAULT_FORM })

async function fetchGoods() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value, ['priceStart', 'priceEnd'])
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getGoodsList(params as any)
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

function openEditDialog(row: any) {
  editId.value = row.id
  form.goodsName = row.goodsName ?? ''
  form.describe = row.describe ?? ''
  form.goodsPrice = row.goodsPrice ?? 0
  form.goodsStock = row.goodsStock ?? 0
  form.launch = row.launch === 1 || row.launch === true
  form.imagePath = row.imagePath ?? ''
  form.createTime = row.createTime ?? ''
  form.delete = row.delete === 1 || row.delete === true
  dialogVisible.value = true
}

async function handleSave() {
  saveLoading.value = true
  try {
    const body: Record<string, unknown> = { id: editId.value! }
    ;(Object.keys(DEFAULT_FORM) as (keyof typeof DEFAULT_FORM)[]).forEach((k) => {
      const v = form[k]
      if (v !== '' && v !== undefined && v !== null) {
        body[k] = k === 'launch' || k === 'delete' ? (v ? 1 : 0) : v
      }
    })
    const res = await managerService.updateGoods(body as any)
    if (res.code === 200) {
      msgSuccess('修改成功')
      dialogVisible.value = false
      fetchGoods()
    } else {
      msgError(res.message || '修改失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

async function handleBan(row: any) {
  const ok = await confirm({ title: '封禁确认', message: `确定要封禁商品「${row.goodsName}」吗？`, type: 'warning' })
  if (!ok) return
  try {
    const res = await managerService.updateGoods({ id: row.id, delete: true })
    if (res.code === 200) { msgSuccess('已封禁'); fetchGoods() }
    else msgError(res.message || '封禁失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

async function handleUnban(row: any) {
  const ok = await confirm({ title: '解封确认', message: `确定要解封商品「${row.goodsName}」吗？`, type: 'info' })
  if (!ok) return
  try {
    const res = await managerService.updateGoods({ id: row.id, delete: false })
    if (res.code === 200) { msgSuccess('已解封'); fetchGoods() }
    else msgError(res.message || '解封失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

onMounted(() => { fetchGoods() })
</script>

<style scoped>
.goods-avatar { width: 36px; height: 36px; border-radius: 4px; object-fit: cover; }
.no-img { color: #909399; font-size: 12px; }
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>