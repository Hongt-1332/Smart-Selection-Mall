<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">AI上传</h2>
      <el-button type="primary" @click="openAddDialog">新增AI</el-button>
    </div>

    <el-table :data="aiList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="category" label="分类" width="110" />
      <el-table-column prop="kind" label="种类" width="110" />
      <el-table-column prop="name" label="名称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="simpleDescription" label="简述" min-width="140" show-overflow-tooltip />
      <el-table-column prop="features" label="特点" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.delete ? 'danger' : 'success'" size="small">
            {{ row.delete ? '已删除' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!row.delete" size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
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

    <el-dialog append-to-body v-model="dialogVisible" :title="isEdit ? '编辑AI' : '新增AI'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" @keyup.enter="handleSave">
        <el-form-item label="关联商品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="选择关联商品" clearable style="width:100%">
            <el-option v-for="g in goodsList" :key="g.id" :label="g.goodsName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="种类" prop="kind">
          <el-input v-model="form.kind" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" />
        </el-form-item>
        <el-form-item label="简述" prop="simpleDescription">
          <el-input v-model="form.simpleDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="特点" prop="features">
          <el-input v-model="form.features" type="textarea" :rows="2" />
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
import { type FormInstance } from 'element-plus'
import { msgSuccess, msgError } from '../../utils/message'
import { merchantService } from '../../services/MerchantService'
import { getErrorMessage } from '../../utils/error'

const aiList = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const saveLoading = ref(false)
const formRef = ref<FormInstance>()

const DEFAULT_FORM = {
  goodsId: null as number | null,
  category: '', kind: '', name: '', price: '',
  simpleDescription: '', features: '',
}

const form = reactive({ ...DEFAULT_FORM })
const goodsList = ref<{ id: number; goodsName: string }[]>([])

const rules = {
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  kind: [{ required: true, message: '请输入种类', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  simpleDescription: [{ required: true, message: '请输入简述', trigger: 'blur' }],
  features: [{ required: true, message: '请输入特点', trigger: 'blur' }],
}

async function fetchGoodsList() {
  try {
    const res = await merchantService.getAiGoodsList()
    if (res.code === 200 && res.data) {
      goodsList.value = res.data
    }
  } catch { /* 静默 */ }
}

async function fetchAi() {
  loading.value = true
  try {
    const res = await merchantService.getAiList({ page: pageNum.value, size: pageSize.value })
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

function handlePageChange(page: number) { pageNum.value = page; fetchAi() }

function openAddDialog() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, DEFAULT_FORM)
  fetchGoodsList()
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.category = row.category ?? ''
  form.kind = row.kind ?? ''
  form.name = row.name ?? ''
  form.price = row.price ?? ''
  form.simpleDescription = row.simpleDescription ?? ''
  form.features = row.features ?? ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saveLoading.value = true
  try {
    const body: Record<string, unknown> = {
      category: form.category,
      kind: form.kind,
      name: form.name,
      price: form.price,
      simpleDescription: form.simpleDescription,
      features: form.features,
    }
    if (!isEdit.value && form.goodsId !== null) body.goodsId = form.goodsId

    const res = isEdit.value
      ? await merchantService.updateAi({ id: editId.value!, ...body } as any)
      : await merchantService.addAi(body as any)

    if (res.code === 200) {
      msgSuccess(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchAi()
    } else {
      msgError(res.message || '保存失败')
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
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
</style>