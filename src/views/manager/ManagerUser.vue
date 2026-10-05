<template>
  <div class="manager-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <div class="header-stats">
        <span class="stat-item">共 <strong>{{ total }}</strong> 个用户</span>
        <span class="stat-divider">|</span>
        <span class="stat-item">第 <strong>{{ pageNum }}</strong> / <strong>{{ pages || 1 }}</strong> 页</span>
      </div>
    </div>

    <el-card shadow="never" class="search-card">
      <div class="search-bar" @keyup.enter="handleSearch">
        <el-input v-model="search.userName" placeholder="用户名" clearable style="width: 140px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-input v-model="search.account" placeholder="账号" clearable style="width: 140px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-input v-model="search.phone" placeholder="手机号" clearable style="width: 140px" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-select v-model="search.delete" placeholder="删除状态" clearable style="width: 120px">
          <el-option :value="false" label="未删除" />
          <el-option :value="true" label="已删除" />
        </el-select>
        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="advVisible = true">高级搜索</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="userList" v-loading="loading" style="width: 100%" @row-click="openDetail" row-class-name="clickable-row" stripe>
        <el-table-column prop="id" label="ID" width="75" align="center" />
        <el-table-column label="头像" width="60" align="center">
          <template #default="{ row }">
            <img v-if="row.avatarPath" :src="row.avatarPath" class="user-avatar" />
            <el-icon v-else :size="24" class="avatar-placeholder"><UserFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" min-width="110" show-overflow-tooltip />
        <el-table-column prop="account" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level && row.level > 0 ? 'warning' : 'info'" effect="dark" round>
              Lv.{{ row.level ?? 0 }}<template v-if="row.level && row.level > 0"> {{ row.vipLevelName || '' }}</template>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" min-width="110" align="right">
          <template #default="{ row }">¥{{ row.balance?.toFixed(2) ?? '0.00' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.delete" type="danger" size="small">已封禁</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="openDetail(row)">详情</el-button>
            <el-button size="small" type="success" @click.stop="openEditDialog(row)">编辑</el-button>
            <el-button v-if="!row.delete" size="small" type="danger" @click.stop="handleBan(row)">封禁</el-button>
            <el-button v-else size="small" type="warning" @click.stop="handleUnban(row)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && userList.length === 0" description="暂无用户" />

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

    <!-- 高级搜索 -->
    <el-dialog append-to-body v-model="advVisible" title="高级搜索" width="500px">
      <div @keyup.enter="advVisible = false; handleSearch()">
      <el-form label-width="100px">
        <el-form-item label="ID">
          <el-input-number v-model="search.id" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="search.email" clearable />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="search.describe" clearable />
        </el-form-item>
        <el-form-item label="用户等级">
          <el-input-number v-model="search.level" :min="0" :step="1" controls-position="right" style="width: 100%" />
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

    <!-- 详情弹窗 -->
    <el-dialog append-to-body v-model="detailVisible" title="用户详情" width="640px">
      <div v-if="!detailRow" style="text-align:center;padding:40px;color:#6b7280;">加载中...</div>
      <div v-else>
        <div class="detail-header">
          <img v-if="detailRow.avatarPath" :src="detailRow.avatarPath" class="detail-avatar" />
          <div v-else class="detail-avatar-placeholder">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <div class="detail-header-info">
            <span class="detail-name">{{ detailRow.userName }}</span>
            <span class="detail-id">ID: {{ detailRow.id }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">账号</span>
            <span class="detail-value">{{ detailRow.account }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机号</span>
            <span class="detail-value">{{ detailRow.phone || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱</span>
            <span class="detail-value">{{ detailRow.email || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">余额</span>
            <span class="detail-value detail-price">¥{{ detailRow.balance?.toFixed(2) ?? '0.00' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">等级</span>
            <el-tag :type="detailRow.level && detailRow.level > 0 ? 'warning' : 'info'" effect="dark" round>
              Lv.{{ detailRow.level ?? 0 }}<template v-if="detailRow.level && detailRow.level > 0"> {{ detailRow.vipLevelName || '' }}</template>
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <el-tag v-if="detailRow.delete" type="danger" size="small">已封禁</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </div>
          <div class="detail-item detail-full">
            <span class="detail-label">描述</span>
            <span class="detail-value">{{ detailRow.describe || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ detailRow.createTime || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">上传时间</span>
            <span class="detail-value">{{ detailRow.uploadTime || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">VIP购买时间</span>
            <span class="detail-value">{{ detailRow.vipCreateTime || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">VIP持续天数</span>
            <span class="detail-value">{{ detailRow.vipDuration ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">上传背景次数</span>
            <span class="detail-value">{{ detailRow.uploadBackground ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">上传商品次数</span>
            <span class="detail-value">{{ detailRow.uploadGoods ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">更新头像次数</span>
            <span class="detail-value">{{ detailRow.updateAvatar ?? '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">头像路径</span>
            <span class="detail-value detail-path">{{ detailRow.avatarPath || '-' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog append-to-body v-model="dialogVisible" title="编辑用户" width="660px" @open="handleDialogOpen">
      <el-form :model="form" ref="formRef" label-width="110px" v-if="dialogVisible" @keyup.enter="handleSave">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.userName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号">
              <el-input v-model="form.account" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="余额">
              <el-input v-model.number="form.balance" type="number" min="0" step="0.01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级(Lv)">
              <el-input v-model.number="form.level" type="number" min="0" step="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.describe" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="头像路径">
          <el-input v-model="form.avatarPath" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上传时间">
              <el-date-picker v-model="form.uploadTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建时间">
              <el-date-picker v-model="form.createTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="VIP购买时间">
              <el-date-picker v-model="form.vipCreateTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="VIP持续天数">
              <el-input v-model.number="form.vipDuration" type="number" min="0" step="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上传背景次数">
          <el-input v-model.number="form.uploadBackground" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item label="上传商品次数">
          <el-input v-model.number="form.uploadGoods" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item label="更新头像次数">
          <el-input v-model.number="form.updateAvatar" type="number" min="0" step="1" />
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
import { UserFilled } from '@element-plus/icons-vue'
import { managerService } from '../../services/ManagerService'
import { getErrorMessage } from '../../utils/error'
import { buildSearchParams, buildTimeRangeParams } from '../../utils/search'

const DEFAULT_SEARCH = {
  id: null as number | null,
  userName: '', account: '', phone: '', email: '',
  describe: '', level: null as number | null,
  createTimeRange: null as [string, string] | null,
  delete: '' as boolean | '',
}

const search = reactive({ ...DEFAULT_SEARCH })
const userList = ref<any[]>([])
const loading = ref(false)
const advVisible = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

const detailVisible = ref(false)
const detailRow = ref<any>(null)

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const saveLoading = ref(false)
const editTargetRow = ref<any>(null)

const DEFAULT_FORM = {
  userName: '', account: '', balance: 0 as number,
  email: '', phone: '', describe: '', level: 0 as number,
  avatarPath: '', uploadTime: '', vipCreateTime: '',
  vipDuration: 0 as number, createTime: '',
  delete: false as boolean,
  uploadBackground: 0 as number,
  uploadGoods: 0 as number,
  updateAvatar: 0 as number,
}

const form = reactive({ ...DEFAULT_FORM })

async function fetchUsers() {
  loading.value = true
  try {
    const params = buildSearchParams(search, pageNum.value, pageSize.value)
    const timeRange = buildTimeRangeParams(search.createTimeRange, 'createTimeStart', 'createTimeEnd')
    Object.assign(params, timeRange)

    const res = await managerService.getUserList(params as any)
    if (res.code === 200 && res.data) {
      userList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; fetchUsers() }

function handleReset() {
  Object.assign(search, DEFAULT_SEARCH)
  pageNum.value = 1
  fetchUsers()
}

function handlePageChange(page: number) { pageNum.value = page; fetchUsers() }

function openDetail(row: any) {
  detailRow.value = row
  detailVisible.value = true
}

function openEditDialog(row: any) {
  editId.value = row.id
  editTargetRow.value = row
  dialogVisible.value = true
}

function handleDialogOpen() {
  const row = editTargetRow.value
  if (!row) return
  form.userName = row.userName ?? ''
  form.account = row.account ?? ''
  form.balance = row.balance ?? 0
  form.email = row.email ?? ''
  form.phone = row.phone ?? ''
  form.describe = row.describe ?? ''
  form.level = row.level ?? 0
  form.avatarPath = row.avatarPath ?? ''
  form.uploadTime = row.uploadTime ?? ''
  form.vipCreateTime = row.vipCreateTime ?? ''
  form.vipDuration = row.vipDuration ?? 0
  form.createTime = row.createTime ?? ''
  form.delete = row.delete ?? false
  form.uploadBackground = row.uploadBackground ?? 0
  form.uploadGoods = row.uploadGoods ?? 0
  form.updateAvatar = row.updateAvatar ?? 0
}

async function handleSave() {
  saveLoading.value = true
  try {
    const body: Record<string, unknown> = { id: editId.value! }
    ;(Object.keys(DEFAULT_FORM) as (keyof typeof DEFAULT_FORM)[]).forEach((k) => {
      const v = form[k]
      if (v !== null && v !== '' && v !== undefined) body[k] = v
    })
    const res = await managerService.updateUser(body as any)
    if (res.code === 200) {
      msgSuccess('修改成功')
      dialogVisible.value = false
      fetchUsers()
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
  const ok = await confirm({ title: '封禁确认', message: `确定要封禁用户「${row.userName}」吗？`, type: 'warning' })
  if (!ok) return
  try {
    const res = await managerService.updateUser({ id: row.id, delete: true })
    if (res.code === 200) { msgSuccess('已封禁'); fetchUsers() }
    else msgError(res.message || '封禁失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

async function handleUnban(row: any) {
  const ok = await confirm({ title: '解封确认', message: `确定要解封用户「${row.userName}」吗？`, type: 'info' })
  if (!ok) return
  try {
    const res = await managerService.updateUser({ id: row.id, delete: false })
    if (res.code === 200) { msgSuccess('已解封'); fetchUsers() }
    else msgError(res.message || '解封失败')
  } catch (e: unknown) { msgError(getErrorMessage(e)) }
}

onMounted(() => { fetchUsers() })
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

.clickable-row {
  cursor: pointer;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.avatar-placeholder {
  color: #6b7280;
}

.text-muted {
  color: #6b7280;
  font-size: 13px;
}

.level-num {
  font-weight: 700;
  color: #5b9eff;
  font-family: 'SF Mono', 'Consolas', monospace;
}

/* 表格样式优化 */
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

/* 详情弹窗 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-avatar {
  width: 64px; height: 64px; border-radius: 50%;
  object-fit: cover; border: 2px solid rgba(91, 141, 239, 0.3);
}

.detail-avatar-placeholder {
  width: 64px; height: 64px; border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  display: flex; align-items: center; justify-content: center;
  color: #6b7280;
}

.detail-header-info {
  display: flex; flex-direction: column; gap: 4px;
}

.detail-name {
  font-size: 18px; font-weight: 600; color: #e0e3ea;
}

.detail-id {
  font-size: 13px; color: #6b7280;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}

.detail-item {
  display: flex; flex-direction: column; gap: 2px;
}

.detail-full {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px; color: #6b7280; font-weight: 500;
}

.detail-value {
  font-size: 14px; color: #c0c4cc;
}

.detail-price {
  color: #e6a23c; font-weight: 600;
}

.detail-path {
  word-break: break-all; font-size: 12px;
}
</style>