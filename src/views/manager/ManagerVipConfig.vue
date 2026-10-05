<template>
  <div>
    <h2 class="page-title">VIP配置管理</h2>

    <div style="margin-bottom: 16px">
      <el-button type="success" @click="openCreateDialog">新增配置</el-button>
    </div>

    <el-table :data="configList" v-loading="loading" style="width: 100%">
      <el-table-column prop="level" label="VIP等级" width="80" align="center" />
      <el-table-column
        v-for="f in vipFields"
        :key="f.key"
        :prop="f.key"
        :label="f.label"
        :min-width="f.width ?? 100"
        align="center"
      >
        <template v-if="f.key === 'price'" #default="{ row }">
          ¥{{ row.price?.toFixed(2) ?? '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="80" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && configList.length === 0" description="暂无VIP配置" />

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

    <el-dialog append-to-body v-model="dialogVisible" :title="isCreate ? '新增VIP配置' : '编辑VIP配置'" width="550px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="VIP等级">
          <el-input v-model.number="form.level" type="number" min="0" step="1" />
        </el-form-item>
        <el-form-item v-for="f in vipFields" :key="f.key" :label="f.label">
          <el-input
            v-if="f.type === 'number'"
            v-model.number="form[f.key]"
            type="number"
            :min="0"
            :step="f.key === 'price' ? 0.01 : 1"
          />
          <el-select v-else-if="f.type === 'boolean'" v-model="form[f.key]" clearable>
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
import { managerService } from '../../services/ManagerService'
import { getErrorMessage } from '../../utils/error'

interface VipField {
  key: string
  label: string
  type: 'number' | 'boolean'
  width?: number
}

const vipFields: VipField[] = [
  { key: 'maxAddressQuantity', label: '最大地址数', type: 'number', width: 110 },
  { key: 'monthlyUpdateGoods', label: '月更新商品', type: 'number', width: 110 },
  { key: 'monthlyUpdateAvatar', label: '月更新头像', type: 'number', width: 110 },
  { key: 'monthlyUpdateBackground', label: '月更新背景', type: 'number', width: 110 },
  { key: 'maxCartQuantity', label: '最大购物车', type: 'number', width: 110 },
  { key: 'maxGoodsQuantity', label: '最大商品数', type: 'number', width: 110 },
  { key: 'maxAiQuantity', label: '最大AI数', type: 'number', width: 100 },
  { key: 'price', label: '价格', type: 'number', width: 100 },
  { key: 'vipDuration', label: 'VIP持续天数', type: 'number', width: 110 },
]

function buildDefaultForm(): Record<string, number | boolean> {
  const obj: Record<string, number | boolean> = { level: 0 }
  vipFields.forEach((f) => {
    obj[f.key] = f.type === 'boolean' ? false : 0
  })
  return obj
}

const configList = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

const dialogVisible = ref(false)
const isCreate = ref(false)
const saveLoading = ref(false)

const form = reactive(buildDefaultForm())

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await managerService.getVipConfigList({ page: pageNum.value, size: pageSize.value })
    if (res.code === 200 && res.data) {
      configList.value = res.data.list
      total.value = res.data.total
      pages.value = res.data.pages
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) { pageNum.value = page; fetchConfigs() }

function openCreateDialog() {
  isCreate.value = true
  Object.assign(form, buildDefaultForm())
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isCreate.value = false
  form.level = row.level ?? 0
  vipFields.forEach((f) => {
    ;(form as any)[f.key] = row[f.key] ?? (f.type === 'boolean' ? false : 0)
  })
  dialogVisible.value = true
}

async function handleSave() {
  saveLoading.value = true
  try {
    const body: Record<string, unknown> = {}
    body.level = form.level
    vipFields.forEach((f) => {
      const v = (form as any)[f.key]
      if (v !== '' && v !== undefined && v !== null) {
        body[f.key] = f.type === 'boolean' ? (v ? 1 : 0) : v
      }
    })
    const res = isCreate.value
      ? await managerService.saveVipConfig(body as any)
      : await managerService.updateVipConfig(body as any)
    if (res.code === 200) {
      msgSuccess(isCreate.value ? '新增成功' : '修改成功')
      dialogVisible.value = false
      fetchConfigs()
    } else {
      msgError(res.message || '保存失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => { fetchConfigs() })
</script>

<style scoped>
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
</style>