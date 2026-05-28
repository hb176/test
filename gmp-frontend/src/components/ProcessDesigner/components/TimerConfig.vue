<script setup lang="ts">
/**
 * TimerConfig — 定时器配置组件
 *
 * 支持三种定时器类型：
 * - date: 指定日期时间触发（ISO 8601）
 * - duration: 持续时间（ISO 8601，如 PT2H30M）
 * - cycle: 周期触发（ISO 8601，如 R3/PT1H）
 */
import { ref, watch } from 'vue'

const props = defineProps<{
  element: any
}>()

const emit = defineEmits<{
  update: [data: { type: string; value: string } | null]
}>()

const timerType = ref<'date' | 'duration' | 'cycle'>('date')
const timerValue = ref('')

// 从元素加载定时器配置
watch(() => props.element, (el) => {
  if (!el) return
  const bo = el.businessObject || el
  if (!bo || typeof bo.get !== 'function') return
  const eventDefs = bo.get('eventDefinitions') || []
  const timerDef = eventDefs.find((e: any) => e.$instanceOf('bpmn:TimerEventDefinition'))
  if (!timerDef) {
    timerType.value = 'date'
    timerValue.value = ''
    return
  }
  if (timerDef.timeDate) {
    timerType.value = 'date'
    timerValue.value = timerDef.timeDate.body || ''
  } else if (timerDef.timeDuration) {
    timerType.value = 'duration'
    timerValue.value = timerDef.timeDuration.body || ''
  } else if (timerDef.timeCycle) {
    timerType.value = 'cycle'
    timerValue.value = timerDef.timeCycle.body || ''
  }
}, { immediate: true })

function handleChange() {
  if (!timerValue.value) {
    emit('update', null)
  } else {
    emit('update', { type: timerType.value, value: timerValue.value })
  }
}

const presets = [
  { label: '1小时后', type: 'duration' as const, value: 'PT1H' },
  { label: '1天后', type: 'duration' as const, value: 'P1D' },
  { label: '3天后', type: 'duration' as const, value: 'P3D' },
  { label: '每小时', type: 'cycle' as const, value: 'R/PT1H' },
  { label: '每天9点', type: 'cycle' as const, value: 'R/0 9 * * *' }
]

function applyPreset(preset: typeof presets[0]) {
  timerType.value = preset.type
  timerValue.value = preset.value
  handleChange()
}
</script>

<template>
  <div class="timer-config">
    <div class="prop-row">
      <div class="prop-label">类型</div>
      <el-radio-group v-model="timerType" size="small" @change="handleChange">
        <el-radio-button value="date">日期</el-radio-button>
        <el-radio-button value="duration">持续时间</el-radio-button>
        <el-radio-button value="cycle">周期</el-radio-button>
      </el-radio-group>
    </div>
    <div class="prop-row">
      <div class="prop-label">值</div>
      <el-input size="small" v-model="timerValue" @change="handleChange"
        :placeholder="timerType === 'date' ? '2026-06-01T09:00:00' : timerType === 'duration' ? 'PT2H30M' : 'R3/PT1H'" />
    </div>
    <div class="prop-row">
      <div class="prop-label">快捷设置</div>
      <div class="preset-list">
        <el-tag v-for="p in presets" :key="p.value" size="small" class="preset-tag" @click="applyPreset(p)">{{ p.label }}</el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.timer-config .prop-row { margin-bottom: 10px; }
.timer-config .prop-row:last-child { margin-bottom: 0; }
.prop-label { font-size: 12px; color: #606266; margin-bottom: 4px; font-weight: 500; }
.preset-list { display: flex; flex-wrap: wrap; gap: 6px; }
.preset-tag { cursor: pointer; }
</style>
