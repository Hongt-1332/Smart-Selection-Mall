<template>
  <Transition name="confirm-fade">
    <div v-if="visible" class="confirm-overlay" @click.self="handleConfirmCancel">
      <Transition name="confirm-zoom" appear>
        <div v-if="visible" class="confirm-card">
          <div class="confirm-glow" :class="`confirm-glow--${options.type || 'warning'}`" />
          <div class="confirm-header">
            <div :class="['confirm-icon-wrap', `confirm-icon-wrap--${options.type || 'warning'}`]">
              <el-icon :size="24">
                <Warning v-if="options.type === 'warning' || !options.type" />
                <CircleCheck v-else-if="options.type === 'success'" />
                <InfoFilled v-else-if="options.type === 'info'" />
                <CircleClose v-else-if="options.type === 'error'" />
              </el-icon>
            </div>
            <h3 class="confirm-title">{{ options.title || '提示' }}</h3>
          </div>
          <p class="confirm-message">{{ options.message }}</p>
          <div class="confirm-divider" />
          <div class="confirm-actions">
            <button class="confirm-btn confirm-btn--cancel" @click="handleConfirmCancel">
              {{ options.cancelText || '取消' }}
            </button>
            <button :class="['confirm-btn', `confirm-btn--${options.type || 'warning'}`]" @click="handleConfirmOk">
              {{ options.confirmText || '确定' }}
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { Warning, CircleCheck, InfoFilled, CircleClose } from '@element-plus/icons-vue'
import { useConfirmState, handleConfirmOk, handleConfirmCancel } from '../utils/confirm'

const { visible, options } = useConfirmState()
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.confirm-card {
  position: relative;
  background: rgba(20, 22, 30, 0.98);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 36px 32px 28px;
  width: 380px;
  max-width: 90vw;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.03) inset,
    0 8px 40px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.confirm-glow {
  position: absolute;
  top: -80px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 120px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.06;
  pointer-events: none;
}
.confirm-glow--warning { background: #E6A23C; }
.confirm-glow--success { background: #67C23A; }
.confirm-glow--info    { background: #5b9eff; }
.confirm-glow--error   { background: #F56C6C; }

.confirm-header {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.confirm-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}
.confirm-icon-wrap--warning {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.25), rgba(230, 162, 60, 0.12));
  border: 1px solid rgba(230, 162, 60, 0.3);
  color: #F0A030;
}
.confirm-icon-wrap--success {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.25), rgba(103, 194, 58, 0.12));
  border: 1px solid rgba(103, 194, 58, 0.3);
  color: #67C23A;
}
.confirm-icon-wrap--info {
  background: linear-gradient(135deg, rgba(91, 158, 255, 0.25), rgba(91, 158, 255, 0.12));
  border: 1px solid rgba(91, 158, 255, 0.3);
  color: #5b9eff;
}
.confirm-icon-wrap--error {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.25), rgba(245, 108, 108, 0.12));
  border: 1px solid rgba(245, 108, 108, 0.3);
  color: #F56C6C;
}

.confirm-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #e8eaee;
  letter-spacing: 0.5px;
}

.confirm-message {
  position: relative;
  margin: 0 0 20px;
  text-align: center;
  font-size: 14px;
  line-height: 1.7;
  color: #9ca0b0;
}

.confirm-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.06), transparent);
  margin-bottom: 20px;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

.confirm-btn:active {
  transform: scale(0.97);
}

.confirm-btn--cancel {
  background: rgba(255, 255, 255, 0.04);
  color: #8890a0;
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(8px);
}
.confirm-btn--cancel:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #b8bac4;
  border-color: rgba(255, 255, 255, 0.12);
}

.confirm-btn--warning {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.28), rgba(230, 162, 60, 0.18));
  border: 1px solid rgba(230, 162, 60, 0.25);
  color: #F0A030;
}
.confirm-btn--warning:hover {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.40), rgba(230, 162, 60, 0.28));
  border-color: rgba(230, 162, 60, 0.4);
  box-shadow: 0 0 20px rgba(230, 162, 60, 0.15);
  transform: translateY(-1px);
}

.confirm-btn--success {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.28), rgba(103, 194, 58, 0.18));
  border: 1px solid rgba(103, 194, 58, 0.25);
  color: #67C23A;
}
.confirm-btn--success:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.40), rgba(103, 194, 58, 0.28));
  border-color: rgba(103, 194, 58, 0.4);
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.15);
  transform: translateY(-1px);
}

.confirm-btn--info {
  background: linear-gradient(135deg, rgba(91, 158, 255, 0.28), rgba(91, 158, 255, 0.18));
  border: 1px solid rgba(91, 158, 255, 0.25);
  color: #5b9eff;
}
.confirm-btn--info:hover {
  background: linear-gradient(135deg, rgba(91, 158, 255, 0.40), rgba(91, 158, 255, 0.28));
  border-color: rgba(91, 158, 255, 0.4);
  box-shadow: 0 0 20px rgba(91, 158, 255, 0.15);
  transform: translateY(-1px);
}

.confirm-btn--error {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.28), rgba(245, 108, 108, 0.18));
  border: 1px solid rgba(245, 108, 108, 0.25);
  color: #F56C6C;
}
.confirm-btn--error:hover {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.40), rgba(245, 108, 108, 0.28));
  border-color: rgba(245, 108, 108, 0.4);
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.15);
  transform: translateY(-1px);
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.2s ease;
}
.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.confirm-zoom-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.confirm-zoom-leave-active {
  transition: all 0.15s ease;
}
.confirm-zoom-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(12px);
}
.confirm-zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>