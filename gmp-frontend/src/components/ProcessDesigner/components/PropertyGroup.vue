<script setup lang="ts">
/**
 * PropertyGroup — 属性面板通用折叠分组
 */
defineProps<{
  title: string
  icon?: string
  modelValue?: boolean
}>()

defineEmits<{
  'update:modelValue': [val: boolean]
}>()
</script>

<template>
  <div class="property-group">
    <div class="group-header" @click="$emit('update:modelValue', !modelValue)">
      <div class="group-title">
        <el-icon v-if="icon" class="group-icon"><component :is="icon" /></el-icon>
        <span>{{ title }}</span>
      </div>
      <el-icon class="arrow" :class="{ open: modelValue }"><ArrowRight /></el-icon>
    </div>
    <div v-show="modelValue" class="group-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.property-group {
  border-bottom: 1px solid #e8e8e8;
}
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  user-select: none;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  background: #f5f7fa;
}
.group-header:hover {
  background: #ecf0f5;
}
.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
}
.group-icon {
  font-size: 14px;
  color: #409eff;
}
.arrow {
  font-size: 12px;
  color: #909399;
  transition: transform 0.2s;
}
.arrow.open {
  transform: rotate(90deg);
}
.group-body {
  padding: 10px 12px;
}
</style>
