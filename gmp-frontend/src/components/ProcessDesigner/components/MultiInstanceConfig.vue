<script setup lang="ts">
/**
 * MultiInstanceConfig — 多实例配置组件
 *
 * 支持串行/并行多实例、完成条件、集合/元素变量
 */
import { ref, watch } from 'vue'
import type { CheckboxValueType } from 'element-plus'

const props = defineProps<{
  data: any
}>()

const emit = defineEmits<{
  update: [data: any]
}>()

const enabled = ref(false)
const isSequential = ref(false)
const completionCondition = ref('')
const collection = ref('')
const elementVariable = ref('')

watch(() => props.data, (d) => {
  if (d) {
    enabled.value = true
    isSequential.value = d.isSequential || false
    completionCondition.value = d.completionCondition || ''
    collection.value = d.collection || ''
    elementVariable.value = d.elementVariable || ''
  } else {
    enabled.value = false
    isSequential.value = false
    completionCondition.value = ''
    collection.value = ''
    elementVariable.value = ''
  }
}, { immediate: true })

function handleToggle(val: CheckboxValueType) {
  if (typeof val !== 'boolean') return
  if (!val) {
    emit('update', null)
  } else {
    emitData()
  }
}

function emitData() {
  emit('update', {
    isSequential: isSequential.value,
    completionCondition: completionCondition.value,
    collection: collection.value,
    elementVariable: elementVariable.value
  })
}
</script>

<template>
  <div class="multi-instance-config">
    <div class="prop-row prop-row-inline">
      <el-checkbox v-model="enabled" @change="handleToggle">启用多实例</el-checkbox>
    </div>

    <template v-if="enabled">
      <div class="prop-row">
        <div class="prop-label">执行方式</div>
        <el-radio-group v-model="isSequential" size="small" @change="emitData">
          <el-radio-button :value="false">并行</el-radio-button>
          <el-radio-button :value="true">串行</el-radio-button>
        </el-radio-group>
      </div>

      <div class="prop-row">
        <div class="prop-label">集合变量</div>
        <el-input size="small" v-model="collection" @change="emitData" placeholder="如 assigneeList" />
      </div>

      <div class="prop-row">
        <div class="prop-label">元素变量</div>
        <el-input size="small" v-model="elementVariable" @change="emitData" placeholder="如 assignee" />
      </div>

      <div class="prop-row">
        <div class="prop-label">完成条件</div>
        <el-input size="small" v-model="completionCondition" @change="emitData" placeholder="${nrOfCompletedInstances >= 1}" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.multi-instance-config .prop-row { margin-bottom: 10px; }
.multi-instance-config .prop-row:last-child { margin-bottom: 0; }
.prop-label { font-size: 12px; color: #606266; margin-bottom: 4px; font-weight: 500; }
.prop-row-inline { display: flex; align-items: center; }
</style>
