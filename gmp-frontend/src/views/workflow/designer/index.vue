<script setup lang="ts">
/**
 * 流程设计器 — 内嵌 Flowable BPMN Designer
 *
 * 完整功能：定时器、执行监听器、多实例、表单绑定、Flowable 扩展属性
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const modelId = route.query.modelId as string || ''

const designerUrl = ref('')

onMounted(() => {
  if (modelId) {
    designerUrl.value = `/bpmn/designer/index.html#/bpmn/designer?modelId=${modelId}`
  } else {
    designerUrl.value = '/bpmn/designer/index.html#/bpmn/designer'
  }
})

function handleIframeMessage(e: MessageEvent) {
  const data = e.data
  if (!data?.type) return
  switch (data.type) {
    case 'deploy-success':
      ElMessage.success('流程已保存并部署')
      break
    case 'deploy-error':
      ElMessage.error('部署失败: ' + data.message)
      break
  }
}

onMounted(() => {
  window.addEventListener('message', handleIframeMessage)
})
onBeforeUnmount(() => {
  window.removeEventListener('message', handleIframeMessage)
})
</script>

<template>
  <div class="designer-page">
    <iframe
      v-if="designerUrl"
      :src="designerUrl"
      class="designer-iframe"
      frameborder="0"
    />
  </div>
</template>

<style scoped>
.designer-page {
  width: 100%;
  height: calc(100vh - 84px);
  overflow: hidden;
}
.designer-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
