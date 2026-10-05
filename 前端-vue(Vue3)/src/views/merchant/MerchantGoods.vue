<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <el-button type="primary" @click="openAddDialog">添加商品</el-button>
    </div>

    <div class="search-bar" @keyup.enter="handleSearch">
      <el-select v-model="search.launch" placeholder="上架状态" clearable style="width:140px" @change="handleSearch" popper-class="dark-popper">
        <el-option :value="1" label="已上架" />
        <el-option :value="0" label="已下架" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="goodsList" v-loading="loading" style="width: 100%">
      <el-table-column prop="goodsName" label="商品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="goodsPrice" label="价格" width="100">
        <template #default="{ row }">￥{{ row.goodsPrice.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="goodsStock" label="库存" width="70" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.imagePath" :src="row.imagePath" class="goods-thumb" />
          <span v-else class="no-img">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="上架" width="70">
        <template #default="{ row }">
          <el-tag :type="row.launch === 1 ? 'success' : 'info'" size="small">
            {{ row.launch === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button v-if="row.launch === 1" size="small" type="warning" @click="handleToggleLaunch(row, 0)">下架</el-button>
          <el-button v-else size="small" type="success" @click="handleToggleLaunch(row, 1)">上架</el-button>
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

    <el-dialog append-to-body v-model="dialogVisible" :title="isEdit ? '编辑商品' : '添加商品'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" @keyup.enter="handleSave">
        <el-form-item label="商品名称" prop="goodsName">
          <el-input v-model="form.goodsName" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.describe" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="价格" prop="goodsPrice">
          <el-input-number v-model="form.goodsPrice" :min="0.01" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="库存" prop="goodsStock">
          <el-input-number v-model="form.goodsStock" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="发货地址" prop="addressId" v-if="!isEdit">
          <el-select v-model="form.addressId" placeholder="请选择发货地址" style="width: 100%">
            <el-option
              v-for="addr in addressList"
              :key="addr.id"
              :label="`${addr.province}${addr.city}${addr.county}${addr.detail}`"
              :value="addr.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" v-if="!isEdit">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
            <span v-if="selectedFile" style="margin-left: 10px; color: #67c23a">已选择文件</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="更新图片" v-if="isEdit">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <el-button type="primary">选择新图片</el-button>
            <span v-if="selectedFile" style="margin-left: 10px; color: #67c23a">已选择文件</span>
          </el-upload>
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
import { confirm } from '../../utils/confirm'
import { merchantService } from '../../services/MerchantService'
import { userService } from '../../services/UserService'
import { getErrorMessage } from '../../utils/error'
import type { GoodsInfo, AddressInfo } from '../../services/UserService'

const goodsList = ref<GoodsInfo[]>([])
const search = reactive({
  launch: undefined as number | undefined,
})
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
const selectedFile = ref<File | null>(null)
const addressList = ref<AddressInfo[]>([])

const form = reactive({
  goodsName: '',
  describe: '',
  goodsPrice: 0.01,
  goodsStock: 0,
  addressId: null as number | null,
})

const rules = {
  goodsName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  goodsPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  goodsStock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  addressId: [{ required: true, message: '请选择发货地址', trigger: 'change' }],
}

function resetForm() {
  form.goodsName = ''
  form.describe = ''
  form.goodsPrice = 0.01
  form.goodsStock = 0
  form.addressId = null
  selectedFile.value = null
}

function handleFileChange(file: { raw: File }) {
  selectedFile.value = file.raw
}

async function fetchGoods() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: pageNum.value, size: pageSize.value }
    if (search.launch !== undefined) params.launch = search.launch
    const res = await merchantService.getGoodsList(params)
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

function handleSearch() {
  pageNum.value = 1
  fetchGoods()
}

function handleReset() {
  search.launch = undefined
  pageNum.value = 1
  fetchGoods()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchGoods()
}

function openAddDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  fetchAddresses()
  dialogVisible.value = true
}

async function fetchAddresses() {
  try {
    const res = await userService.getAddressList({ page: 1, size: 999 })
    if (res.code === 200 && res.data) {
      addressList.value = res.data.list
    }
  } catch {
    // ignore
  }
}

function openEditDialog(row: GoodsInfo) {
  isEdit.value = true
  editId.value = row.id
  form.goodsName = row.goodsName
  form.describe = row.describe || ''
  form.goodsPrice = row.goodsPrice
  form.goodsStock = row.goodsStock
  selectedFile.value = null
  dialogVisible.value = true
}

async function handleToggleLaunch(row: GoodsInfo, toLaunch: number) {
  const action = toLaunch === 1 ? '上架' : '下架'
  const ok = await confirm({ title: `${action}确认`, message: `确定要${action}商品「${row.goodsName}」吗？`, type: 'info', confirmText: action })
  if (!ok) return
  try {
    const res = await merchantService.updateLaunch(row.id, toLaunch)
    if (res.code === 200) {
      msgSuccess(`${action}成功`)
      fetchGoods()
    } else {
      msgError(`${action}失败`)
    }
  } catch (e: unknown) {
    if (e !== 'cancel') {
      msgError(getErrorMessage(e))
    }
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saveLoading.value = true
  try {
    let res
    if (isEdit.value && editId.value) {
      res = await merchantService.updateGoods({
        id: editId.value,
        goodsName: form.goodsName,
        describe: form.describe,
        goodsPrice: form.goodsPrice,
        goodsStock: form.goodsStock,
      })
      if (res.code === 200 && selectedFile.value) {
        const imgFormData = new FormData()
        imgFormData.append('file', selectedFile.value!)
        await merchantService.updateImage(editId.value, imgFormData)
      }
    } else {
      const formData = new FormData()
      formData.append('addressId', String(form.addressId))
      formData.append('goodsName', form.goodsName)
      formData.append('describe', form.describe ?? '')
      formData.append('goodsPrice', String(form.goodsPrice))
      formData.append('goodsStock', String(form.goodsStock))
      if (selectedFile.value) {
        formData.append('file', selectedFile.value)
      }
      res = await merchantService.addGoods(formData)
    }
    if (res.code === 200) {
      msgSuccess(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchGoods()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchGoods()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  padding: 14px 16px;
  background: rgba(20, 22, 30, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
}
.goods-thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.no-img { color: #909399; font-size: 12px; }
.pagination { margin-top: 20px; text-align: center; }
.page-text { margin-left: 8px; color: var(--el-text-color-regular); font-weight: 400; }
.search-bar :deep(.el-select .el-input__wrapper) {
  background: #1a1c26 !important;
  box-shadow: 0 0 0 1px #2a2d3a inset !important;
}
.search-bar :deep(.el-select .el-input__inner) {
  color: #c8ccd4 !important;
}
.search-bar :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #3a3d4a inset !important;
}
.search-bar :deep(.el-select .el-input__suffix) {
  color: #c8ccd4 !important;
}
.search-bar :deep(.el-select .el-input__suffix-inner .el-select__caret) {
  color: #c8ccd4 !important;
}
</style>

<style>
.dark-popper {
  background: #1a1c26 !important;
  border: 1px solid #2a2d3a !important;
}
.dark-popper .el-select-dropdown__item {
  color: #c8ccd4 !important;
}
.dark-popper .el-select-dropdown__item:hover,
.dark-popper .el-select-dropdown__item.is-hovering {
  background: rgba(255, 255, 255, 0.06) !important;
}
.dark-popper .el-select-dropdown__item.selected {
  color: #409EFF !important;
}
.dark-popper .el-popper__arrow::before {
  background: #1a1c26 !important;
  border-color: #2a2d3a !important;
}
</style>