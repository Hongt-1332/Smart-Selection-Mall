<template>
  <div class="address-page">
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-box">📍</div>
        <div>
          <h2 class="page-title">收货地址</h2>
          <p class="page-sub">管理您的配送地址</p>
        </div>
      </div>
      <el-button type="primary" size="large" class="btn-add" @click="openAddDialog">
        <el-icon><Plus /></el-icon>添加地址
      </el-button>
    </div>

    <div v-if="!loading && addressList.length > 0" class="address-grid">
      <div
        v-for="item in addressList"
        :key="item.id"
        class="address-card"
        :class="{ 'is-default': item.isDefault }"
      >
        <div class="card-accent" />
        <div class="card-body">
          <div class="card-fields">
            <div class="field-row">
              <div class="field-icon">🌍</div>
              <span class="field-label">国家</span>
              <span class="field-value">{{ item.country }}</span>
            </div>
            <div class="field-row">
              <div class="field-icon">🏛️</div>
              <span class="field-label">省份</span>
              <span class="field-value">{{ item.province }}</span>
            </div>
            <div class="field-row">
              <div class="field-icon">🏙️</div>
              <span class="field-label">城市</span>
              <span class="field-value">{{ item.city }}</span>
            </div>
            <div class="field-row">
              <div class="field-icon">📍</div>
              <span class="field-label">区县</span>
              <span class="field-value">{{ item.county }}</span>
            </div>
            <div class="field-row field-row-detail">
              <div class="detail-left">
                <div class="field-icon">🏠</div>
                <span class="field-label">详细</span>
                <span class="field-value">{{ item.detail }}</span>
              </div>
              <span v-if="item.isDefault" class="default-pill">默认</span>
            </div>
          </div>

          <div class="card-footer">
            <button class="action-btn edit-btn" @click="openEditDialog(item)">
              <el-icon :size="15"><Edit /></el-icon>
              <span>编辑</span>
            </button>
            <button v-if="!item.isDefault" class="action-btn default-btn" @click="handleSetDefault(item)">
              <el-icon :size="15"><Star /></el-icon>
              <span>设为默认</span>
            </button>
            <button class="action-btn del-btn" @click="handleDelete(item)">
              <el-icon :size="15"><Delete /></el-icon>
              <span>删除</span>
            </button>
          </div>
        </div>
      </div>

      <div class="address-card add-card" @click="openAddDialog">
        <div class="add-card-inner">
          <div class="add-icon-ring">
            <el-icon :size="28"><Plus /></el-icon>
          </div>
          <span class="add-text">添加新地址</span>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">📭</div>
      <p class="empty-title">暂无收货地址</p>
      <p class="empty-desc">添加一个地址开始购物吧</p>
      <el-button type="primary" @click="openAddDialog">添加第一个地址</el-button>
    </div>

    <el-dialog append-to-body v-model="dialogVisible" :title="isEdit ? '编辑地址' : '添加地址'" width="520px" class="address-dialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @keyup.enter="handleSave">
        <el-form-item label="国家" prop="country">
          <el-input v-model="form.country" placeholder="请输入国家" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="省份/州" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="城市" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="区县" prop="county">
              <el-input v-model="form.county" placeholder="区/县" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="3" placeholder="街道、门牌号、小区等详细地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">{{ isEdit ? '保存修改' : '确认添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { type FormInstance } from 'element-plus'
import { msgSuccess, msgError } from '../../utils/message'
import { confirm } from '../../utils/confirm'
import { Plus, Edit, Delete, Star } from '@element-plus/icons-vue'
import { userService } from '../../services/UserService'
import { getErrorMessage } from '../../utils/error'
import type { AddressInfo } from '../../services/UserService'

const addressList = ref<AddressInfo[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const saveLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  country: '',
  province: '',
  city: '',
  county: '',
  detail: '',
})

const rules = {
  country: [{ required: true, message: '请输入国家', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  county: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

function resetForm() {
  form.country = ''
  form.province = ''
  form.city = ''
  form.county = ''
  form.detail = ''
}

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await userService.getAddressList({ page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      addressList.value = res.data.list
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: AddressInfo) {
  isEdit.value = true
  editId.value = row.id
  form.country = row.country
  form.province = row.province
  form.city = row.city
  form.county = row.county
  form.detail = row.detail
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saveLoading.value = true
  try {
    let res
    if (isEdit.value && editId.value) {
      res = await userService.updateAddress({ id: editId.value, ...form })
    } else {
      res = await userService.addAddress(form)
    }
    if (res.code === 200) {
      msgSuccess(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchAddresses()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  } finally {
    saveLoading.value = false
  }
}

async function handleSetDefault(row: AddressInfo) {
  try {
    const res = await userService.setDefaultAddress(row.id)
    if (res.code === 200) {
      msgSuccess('已设为默认地址')
      fetchAddresses()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    msgError(getErrorMessage(e))
  }
}

async function handleDelete(row: AddressInfo) {
  const ok = await confirm({ title: '确认删除', message: '确定要删除该地址吗？删除后无法恢复。', type: 'warning' })
  if (!ok) return
  try {
    const res = await userService.deleteAddress(row.id)
    if (res.code === 200) {
      msgSuccess('删除成功')
      fetchAddresses()
    } else {
      msgError(res.message)
    }
  } catch (e: unknown) {
    if (e !== 'cancel') {
      msgError(getErrorMessage(e))
    }
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.address-page {
  width: 100%;
}

/* ===== Header ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding: 24px 28px;
  background: linear-gradient(135deg, rgba(16,18,26,0.8), rgba(22,26,40,0.6));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon-box {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.1));
  border-radius: 12px;
  font-size: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 2px;
  color: #f1f2f4;
  letter-spacing: -0.3px;
}

.page-sub {
  margin: 0;
  font-size: 12px;
  color: rgba(255,255,255,0.35);
}

.btn-add {
  border-radius: 10px;
  font-weight: 600;
  height: 40px;
  padding: 0 20px;
}

/* ===== Grid ===== */
.address-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
}

/* ===== Card ===== */
.address-card {
  flex: 1 1 280px;
  min-width: 260px;
  position: relative;
  background: linear-gradient(160deg, rgba(16,18,26,0.7), rgba(22,26,40,0.5));
  backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.06);
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
}

.address-card:hover {
  border-color: rgba(99,102,241,0.25);
  box-shadow: 0 12px 32px rgba(0,0,0,0.3), 0 0 0 1px rgba(99,102,241,0.08);
  transform: translateY(-3px);
}

.card-accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, rgba(99,102,241,0.4), rgba(139,92,246,0.15));
  opacity: 0;
  transition: opacity 0.35s;
}

.address-card:hover .card-accent {
  opacity: 1;
}

.is-default .card-accent {
  opacity: 1;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.card-body {
  padding: 18px;
  position: relative;
}

.default-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.default-pill {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #fbbf24;
  background: linear-gradient(135deg, rgba(251,191,36,0.15), rgba(245,158,11,0.08));
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(251,191,36,0.25);
  box-shadow: 0 0 12px rgba(251,191,36,0.1), inset 0 1px 0 rgba(255,255,255,0.06);
  flex-shrink: 0;
  align-self: center;
}

.detail-left {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

/* ===== Fields ===== */
.card-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-bottom: 14px;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(255,255,255,0.025);
  transition: background 0.2s;
}

.field-row:hover {
  background: rgba(255,255,255,0.05);
}

.field-row-detail {
  grid-column: 1 / -1;
  justify-content: space-between;
}

.field-icon {
  font-size: 13px;
  width: 18px;
  text-align: center;
  flex-shrink: 0;
}

.field-label {
  font-size: 11px;
  color: rgba(255,255,255,0.3);
  font-weight: 500;
  width: 32px;
  flex-shrink: 0;
}

.field-value {
  font-size: 13px;
  color: #d0d2d8;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-row-detail .field-value {
  white-space: normal;
  line-height: 1.4;
}

/* ===== Footer ===== */
.card-footer {
  display: flex;
  justify-content: center;
  gap: 15%;
  padding-top: 12px;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border-radius: 8px;
  border: 1px solid transparent;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  color: rgba(255,255,255,0.5);
}

.edit-btn {
  border-color: rgba(255,255,255,0.08);
}

.edit-btn:hover {
  background: rgba(99,102,241,0.12);
  border-color: rgba(99,102,241,0.25);
  color: #a5b4fc;
}

.del-btn {
  border-color: rgba(255,255,255,0.06);
}

.del-btn:hover {
  background: rgba(239,68,68,0.12);
  border-color: rgba(239,68,68,0.25);
  color: #fca5a5;
}

.default-btn {
  border-color: rgba(255,255,255,0.08);
}

.default-btn:hover {
  background: rgba(234,179,8,0.12);
  border-color: rgba(234,179,8,0.25);
  color: #fde047;
}

/* ===== Add Card ===== */
.add-card {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px dashed rgba(255,255,255,0.1);
  background: rgba(255,255,255,0.02);
  border-radius: 16px;
  min-height: 180px;
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1.2);
}

.add-card:hover {
  border-color: rgba(99,102,241,0.3);
  background: rgba(99,102,241,0.04);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

.add-card-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.add-icon-ring {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 2px dashed rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.4);
  transition: all 0.3s;
}

.add-card:hover .add-icon-ring {
  border-color: rgba(99,102,241,0.4);
  color: #a5b4fc;
  background: rgba(99,102,241,0.08);
}

.add-text {
  font-size: 14px;
  color: rgba(255,255,255,0.4);
  font-weight: 500;
  transition: color 0.3s;
}

.add-card:hover .add-text {
  color: #a5b4fc;
}

/* ===== Empty ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #e5e6e8;
  margin: 0 0 6px;
}

.empty-desc {
  font-size: 13px;
  color: rgba(255,255,255,0.35);
  margin: 0 0 24px;
}

/* ===== Dialog ===== */
.address-dialog :deep(.el-dialog) {
  background: linear-gradient(160deg, rgba(16,18,26,0.97), rgba(22,26,40,0.93));
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
}

.address-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 18px;
  margin: 0;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.address-dialog :deep(.el-dialog__title) {
  color: #f1f2f4;
  font-weight: 700;
  font-size: 17px;
}

.address-dialog :deep(.el-dialog__body) {
  padding: 24px 24px 8px;
}

.address-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 24px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.address-dialog :deep(.el-form-item__label) {
  color: rgba(255,255,255,0.5);
  font-size: 13px;
  font-weight: 500;
}

.address-dialog :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.address-dialog :deep(.el-input__wrapper:hover) {
  border-color: rgba(255,255,255,0.15);
}

.address-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(99,102,241,0.4);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

.address-dialog :deep(.el-input__inner) {
  color: #e5e6e8;
}

.address-dialog :deep(.el-textarea__inner) {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  color: #e5e6e8;
  border-radius: 8px;
}

.address-dialog :deep(.el-textarea__inner:focus) {
  border-color: rgba(99,102,241,0.4);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

.address-dialog :deep(.el-button--default) {
  background: rgba(255,255,255,0.06);
  border-color: rgba(255,255,255,0.1);
  color: #c0c4cc;
}

.address-dialog :deep(.el-button--default:hover) {
  border-color: rgba(255,255,255,0.2);
  color: #e5e6e8;
}
</style>