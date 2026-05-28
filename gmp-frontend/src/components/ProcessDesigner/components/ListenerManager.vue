<script setup lang="ts">
/**
 * ListenerManager — 监听器管理组件
 *
 * 表格 + 增删改查，支持执行监听器和任务监听器
 * 数据存储在 bpmn:ExtensionElements 中
 */
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ListenerForm from './ListenerForm.vue'

const props = defineProps<{
  listeners: any[]
  type: 'execution' | 'task'
}>()

const emit = defineEmits<{
  add: [listener: any]
  edit: [index: number, listener: any]
  delete: [index: number]
  'update:listeners': [listeners: any[]]
}>()

// ==================== 状态 ====================

const formVisible = ref(false)
const editIndex = ref(-1)
const formData = ref<any>({})

// ==================== 事件类型选项 ====================

const executionEventOptions = [
  { label: '开始 (start)', value: 'start' },
  { label: '结束 (end)', value: 'end' },
  { label: '触发 (take)', value: 'take' }
]

const taskEventOptions = [
  { label: '创建 (create)', value: 'create' },
  { label: '分配 (assignment)', value: 'assignment' },
  { label: '完成 (complete)', value: 'complete' },
  { label: '删除 (delete)', value: 'delete' }
]

const listenerTypeOptions = [
  { label: 'Java 类', value: 'class' },
  { label: '表达式', value: 'expression' },
  { label: '委托表达式', value: 'delegateExpression' }
]

// ==================== 计算属性 ====================

const eventOptions = props.type === 'execution' ? executionEventOptions : taskEventOptions

// ==================== 操作 ====================

function handleAdd() {
  editIndex.value = -1
  formData.value = {
    event: eventOptions[0]?.value || '',
    type: 'class',
    value: '',
    fields: []
  }
  formVisible.value = true
}

function handleEdit(index: number) {
  editIndex.value = index
  const listener = props.listeners[index]
  formData.value = {
    event: listener.event || '',
    type: getListenerType(listener),
    value: getListenerValue(listener),
    fields: listener.fields || []
  }
  formVisible.value = true
}

function handleDelete(index: number) {
  ElMessageBox.confirm('确定删除该监听器？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    emit('delete', index)
    ElMessage.success('已删除')
  }).catch(() => {})
}

function handleFormConfirm(data: any) {
  if (editIndex.value >= 0) {
    emit('edit', editIndex.value, data)
  } else {
    emit('add', data)
  }
  formVisible.value = false
}

// ==================== 工具函数 ====================

function getListenerType(listener: any): string {
  if (listener.class || listener['flowable:class']) return 'class'
  if (listener.expression || listener['flowable:expression']) return 'expression'
  if (listener.delegateExpression || listener['flowable:delegateExpression']) return 'delegateExpression'
  return 'class'
}

function getListenerValue(listener: any): string {
  const type = getListenerType(listener)
  if (type === 'class') return listener.class || listener['flowable:class'] || ''
  if (type === 'expression') return listener.expression || listener['flowable:expression'] || ''
  if (type === 'delegateExpression') return listener.delegateExpression || listener['flowable:delegateExpression'] || ''
  return ''
}

function getEventTypeLabel(event: string): string {
  const opt = eventOptions.find(o => o.value === event)
  return opt?.label || event
}

function getTypeLabel(type: string): string {
  const opt = listenerTypeOptions.find(o => o.value === type)
  return opt?.label || type
}
</script>

<template>
  <div class="listener-manager">
    <div class="header">
      <span class="title">{{ props.type === 'execution' ? '执行监听器' : '任务监听器' }}</span>
      <el-button type="primary" link size="small" @click="handleAdd">添加</el-button>
    </div>

    <el-table :data="props.listeners" size="small" empty-text="暂无监听器">
      <el-table-column prop="event" label="事件" width="100">
        <template #default="{ row }">
          {{ getEventTypeLabel(row.event) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          {{ getTypeLabel(getListenerType(row)) }}
        </template>
      </el-table-column>
      <el-table-column label="值" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getListenerValue(row) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ $index }">
          <el-button type="primary" link size="small" @click="handleEdit($index)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <ListenerForm
      v-model:visible="formVisible"
      :data="formData"
      :event-options="eventOptions"
      :listener-type-options="listenerTypeOptions"
      :title="editIndex >= 0 ? '编辑监听器' : '添加监听器'"
      @confirm="handleFormConfirm"
    />
  </div>
</template>

<style scoped>
.listener-manager {
  margin-bottom: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}
</style>
