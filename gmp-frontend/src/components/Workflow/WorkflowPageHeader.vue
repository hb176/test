<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflow'

const props = defineProps<{
  current: 'todo' | 'done' | 'started'
  title: string
  subtitle?: string
}>()

const router = useRouter()
const workflowStore = useWorkflowStore()

const tabs = computed(() => [
  { key: 'todo', label: '我的待办', path: '/workflow/todo', count: workflowStore.todoCount },
  { key: 'done', label: '我的已办', path: '/workflow/done' },
  { key: 'started', label: '我的发起', path: '/workflow/started' },
  { key: 'definition', label: '发起流程', path: '/workflow/definition' },
])

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <section class="workflow-page-header gmp-panel">
    <div class="workflow-title">
      <div class="workflow-icon">
        <el-icon><Operation /></el-icon>
      </div>
      <div>
        <h1>{{ title }}</h1>
        <p>{{ subtitle || '统一查看流程任务、处理记录和发起记录。' }}</p>
      </div>
    </div>

    <div class="workflow-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        :class="{ active: current === tab.key }"
        @click="go(tab.path)"
      >
        <span>{{ tab.label }}</span>
        <em v-if="tab.count">{{ tab.count }}</em>
      </button>
    </div>
  </section>
</template>

<style scoped>
.workflow-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
}

.workflow-title {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.workflow-icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: #eef5ff;
  color: var(--gmp-primary);
  font-size: 22px;
}

.workflow-title h1 {
  margin: 0;
  color: var(--gmp-text);
  font-size: 20px;
  font-weight: 800;
}

.workflow-title p {
  margin: 5px 0 0;
  color: var(--gmp-text-muted);
  font-size: 13px;
}

.workflow-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.workflow-tabs button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--gmp-border);
  border-radius: 6px;
  background: #fff;
  color: #475569;
  cursor: pointer;
  font-size: 13px;
}

.workflow-tabs button:hover,
.workflow-tabs button.active {
  border-color: var(--gmp-primary);
  background: #eef5ff;
  color: var(--gmp-primary);
}

.workflow-tabs em {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--gmp-danger);
  color: #fff;
  font-size: 11px;
  font-style: normal;
  line-height: 18px;
  text-align: center;
}

@media (max-width: 860px) {
  .workflow-page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .workflow-tabs {
    justify-content: flex-start;
  }
}
</style>
