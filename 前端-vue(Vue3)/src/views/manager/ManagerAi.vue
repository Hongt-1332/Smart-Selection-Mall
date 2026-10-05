<template>
  <div>
    <h2 class="page-title">AI管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-input v-model="search.name" placeholder="名称" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-input v-model="search.category" placeholder="分类" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="search.delete" placeholder="是否删除" clearable style="width: 120px">
        <el-option :value="false" label="未删除" />
        <el-option :value="true" label="已删除" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <el-table :data="aiList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userName" label="用户名" width="110" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="kind" label="种类" width="100" />
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="simpleDescription" label="简述" min-width="140" show-overflow-tooltip />
      <el-table-column prop="features" label="特点" min-width="140" show-overflow-tooltip />
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

    <el-empty v-if="!loading && aiList.length === 0" description="暂无AI" />

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
        <el-form-item label="种类">
          <el-input v-model="search.kind" clearable />
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input-number v-model="search.priceStart" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最低价" />
          <span style="margin: 0 8px">~</span>
          <el-input-number v-model="search.priceEnd" :min="0" :precision="2" controls-position="right" style="width: 130px" placeholder="最高价" />
        </el-form-item>
        <el-form-item label="简述">
          <el-input v-model="search.simpleDescription" clearable />
        </el-form-item>
        <el-form-item label="特点">
          <el-input v-model="search.features" clearable />
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

    <el-dialog append-to-body v-model="dialogVisible" title="编辑AI" width="600px">
      <el-form :model="form" label-width="100px" @keyup.enter="handleSave">
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="种类">
          <el-input v-model="form.kind" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model.number="form.price" type="number" min="0" step="0.01" />
        </el-form-item>
        <el-form-item label="简述">
          <el-input v-model="form.simpleDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="特点">
          <el-input v-model="form.features" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="上传图片">
          <div v-if="form.imagePath" class="upload-preview">
            <img :src="form.imagePath" class="upload-img" />
            <el-button size="small" type="danger" @click="form.imagePath = ''">移除</el-button>
          </div>
          <div v-else>
            <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
            <el-button type="primary" @click="($refs.fileInput as HTMLInputElement).click()" :loading="uploading">选择文件</el-button>
          </div>
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
  userName: '', category: '', kind: '', name: '',
  priceStart: null as number | null, priceEnd: null as number | null,
  simpleDescription: '', features: '',
  createTimeRange: null as [string, string] | null,
  delete: '' as boolean | '',
}

const search = reactive({ ...DEFAULT_SEARCH })
const aiList = ref<any[]>([])
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
  category: '', kind: '', name: '', price: 0 as number,
  simpleDescription: '', features: '', createTime: '',
  imagePath: '', delete: false as boolean,
}

const form = reactive({ ...DEFAULT_FORM })
const uploading = ref(false)

async function fetchAi() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value, ['priceStart', 'priceEnd'])
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getAiList(params as any)
    if (res.code === 200 && res.data) {
      aiList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchAi() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchAi()
}

function handlePageChange(page: number) { pageNum.value = page; fetchAi() }

function openEditDialog(row: any) {
  editId.value = row.id
  form.category = row.category ?? ''
  form.kind = row.kind ?? ''
  form.name = row.name ?? ''
  form.price = row.price ?? 0
  form.simpleDescription = row.simpleDescription ?? ''
  form.features = row.features ?? ''
  form.createTime = row.createTime ?? ''
  form.imagePath = row.imagePath ?? ''
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
        body[k] = k === 'delete' ? (v ? 1 : 0) : v
      }
    })
    const res = await managerService.updateAi(body as any)
    if (res.code === 200) {
      msgSuccess('修改成功')
      dialogVisible.value = false
      fetchAi()
    } else {
      msgError(res.message || '修改失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

async function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const path = await managerService.uploadImage(file)
    form.imagePath = path
    msgSuccess('上传成功')
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    uploading.value = false
  }
}

async function handleBan(row: any) {
  const ok = await confirm({ title: '封禁确认', message: '确定要封禁该AI吗？', type: 'warning' })
  if (!ok) return
  try {
    const res = await managerService.updateAi({ id: row.id, delete: true })
    if (res.code === 200) { msgSuccess('已封禁'); fetchAi() }
    else msgError(res.message || '封禁失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

async function handleUnban(row: any) {
  const ok = await confirm({ title: '解封确认', message: '确定要解封该AI吗？', type: 'info' })
  if (!ok) return
  try {
    const res = await managerService.updateAi({ id: row.id, delete: false })
    if (res.code === 200) { msgSuccess('已解封'); fetchAi() }
    else msgError(res.message || '解封失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

function formatPrice(val: unknown): string {
  const n = Number(val)
  return isNaN(n) ? '0.00' : n.toFixed(2)
}

onMounted(() => { fetchAi() })
</script>

<style scoped>
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
.upload-preview { display: flex; align-items: center; gap: 12px; }
.upload-img { width: 80px; height: 80px; border-radius: 6px; object-fit: cover; border: 1px solid rgba(255,255,255,0.1); }
</style>