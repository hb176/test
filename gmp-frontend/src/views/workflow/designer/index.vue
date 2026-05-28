<script setup lang="ts">
/**
 * 流程设计器页面
 *
 * 使用自研 ProcessDesigner 组件替代原有的 iframe 方案
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProcessDesigner from '@/components/ProcessDesigner/index.vue'

const route = useRoute()
const router = useRouter()

const modelId = computed(() => (route.params.id || route.query.modelId) as string || '')

function handleSave(xml: string, savedModelId?: string) {
  ElMessage.success('流程已保存')
  // 如果是新建且拿到了新 modelId，更新路由
  if (savedModelId && !modelId.value) {
    router.replace(`/workflow/designer/${savedModelId}`)
  }
}

function handleDeploy(modelId: string) {
  ElMessage.success('流程已部署')
}
</script>

<template>
  <div class="designer-page">
    <ProcessDesigner
      :model-id="modelId"
      @save="handleSave"
      @deploy="handleDeploy"
    />
  </div>
</template>

<style scoped>
.designer-page {
  width: 100%;
  height: calc(100vh - 84px);
  overflow: hidden;
}
</style>
