<template>
  <div>
    <h2 class="page-title">地址管理</h2>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 130px" @keyup.enter="handleSearch" @clear="handleSearch" />
      <el-select v-model="search.delete" placeholder="是否删除" clearable style="width: 120px">
        <el-option :value="false" label="未删除" />
        <el-option :value="true" label="已删除" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="advVisible = true">高级搜索</el-button>
    </div>

    <el-table :data="addressList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userName" label="用户名" width="110" show-overflow-tooltip />
      <el-table-column prop="country" label="国家" width="100" />
      <el-table-column prop="province" label="省" width="100" show-overflow-tooltip />
      <el-table-column prop="city" label="市" width="100" show-overflow-tooltip />
      <el-table-column prop="county" label="区/县" width="100" show-overflow-tooltip />
      <el-table-column prop="detail" label="详细地址" min-width="160" show-overflow-tooltip />
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

    <el-empty v-if="!loading && addressList.length === 0" description="暂无地址" />

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

    <el-dialog append-to-body v-model="dialogVisible" title="编辑地址" width="600px">
      <el-form :model="form" label-width="100px" @keyup.enter="handleSave">
        <el-form-item label="国家">
          <el-input v-model="form.country" />
        </el-form-item>
        <el-form-item label="省">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.county" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" type="textarea" :rows="2" />
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
  userName: '',
  createTimeRange: null as [string, string] | null,
  delete: '' as boolean | '',
}

const search = reactive({ ...DEFAULT_SEARCH })
const addressList = ref<any[]>([])
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
  country: '', province: '', city: '', county: '',
  detail: '', createTime: '', delete: null as boolean | null,
}

const form = reactive({ ...DEFAULT_FORM })

async function fetchAddresses() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getAddressList(params as any)
    if (res.code === 200 && res.data) {
      addressList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchAddresses() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchAddresses()
}

function handlePageChange(page: number) { pageNum.value = page; fetchAddresses() }

function openEditDialog(row: any) {
  editId.value = row.id
  form.country = row.country ?? ''
  form.province = row.province ?? ''
  form.city = row.city ?? ''
  form.county = row.county ?? ''
  form.detail = row.detail ?? ''
  form.createTime = row.createTime ?? ''
  form.delete = row.delete ?? null
  dialogVisible.value = true
}

async function handleSave() {
  saveLoading.value = true
  try {
    const body: Record<string, unknown> = { id: editId.value! }
    ;(Object.keys(DEFAULT_FORM) as (keyof typeof DEFAULT_FORM)[]).forEach((k) => {
      const v = form[k]
      if (v !== null && v !== '' && v !== undefined) body[k] = v
    })
    const res = await managerService.updateAddress(body as any)
    if (res.code === 200) {
      msgSuccess('修改成功')
      dialogVisible.value = false
      fetchAddresses()
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
  const ok = await confirm({ title: '封禁确认', message: '确定要封禁该地址吗？', type: 'warning' })
  if (!ok) return
  try {
    const res = await managerService.updateAddress({ id: row.id, delete: true })
    if (res.code === 200) { msgSuccess('已封禁'); fetchAddresses() }
    else msgError(res.message || '封禁失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

async function handleUnban(row: any) {
  const ok = await confirm({ title: '解封确认', message: '确定要解封该地址吗？', type: 'info' })
  if (!ok) return
  try {
    const res = await managerService.updateAddress({ id: row.id, delete: false })
    if (res.code === 200) { msgSuccess('已解封'); fetchAddresses() }
    else msgError(res.message || '解封失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

onMounted(() => { fetchAddresses() })
</script>

<style scoped>
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>