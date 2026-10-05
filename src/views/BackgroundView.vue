<template>
  <div class="bg-page">
    <BackgroundEffects />
  </div>
</template>

<script setup lang="ts">
import BackgroundEffects from '../components/BackgroundEffects.vue'
</script>

<style scoped>
.bg-page {
  position: fixed;
  inset: 0;
  background: rgba(20, 22, 30, 0.45);
  backdrop-filter: blur(12px);
  overflow: hidden;
}
</style>