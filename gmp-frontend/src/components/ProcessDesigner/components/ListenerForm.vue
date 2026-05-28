<script setup lang="ts">
/**
 * ListenerForm — 监听器编辑弹窗
 *
 * 支持配置事件类型、监听器类型、值、字段列表
 */
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  data: any
  eventOptions: any[]
  listenerTypeOptions: any[]
  title?: string
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  confirm: [data: any]
}>()

// ==================== 状态 ====================

const formRef = ref()
const formData = ref({
  event: '',
  type: 'class',
  value: '',
  fields: [] as any[]
})

// ==================== 监听 ====================

watch(() => props.visible, (val) => {
  if (val && props.data) {
    formData.value = { ...props.data, fields: [...(props.data.fields || [])] }
  }
})

// ==================== 字段操作 ====================

function addField() {
  formData.value.fields.push({ name: '', value: '', type: 'string' })
}

function removeField(index: number) {
  formData.value.fields.splice(index, 1)
}

// ==================== 提交 ====================

function handleConfirm() {
  emit('confirm', { ...formData.value })
  emit('update:visible', false)
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    :title="props.title || '监听器配置'"
    width="550px"
    @close="handleClose"
  >
    <el-form :model="formData" label-width="100px" size="small">
      <el-form-item label="事件类型" required>
        <el-select v-model="formData.event" placeholder="请选择事件类型" style="width: 100%">
          <el-option
            v-for="opt in props.eventOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="监听器类型" required>
        <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
          <el-option
            v-for="opt in props.listenerTypeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="值" required>
        <el-input
          v-model="formData.value"
          :placeholder="formData.type === 'class' ? '请输入 Java 类全路径' : '请输入表达式'"
        />
      </el-form-item>

      <!-- 字段列表 -->
      <el-form-item label="字段">
        <div class="fields-section">
          <div v-for="(field, index) in formData.fields" :key="index" class="field-row">
            <el-input
              v-model="field.name"
              placeholder="字段名"
              size="small"
              style="width: 120px"
            />
            <el-select v-model="field.type" size="small" style="width: 90px">
              <el-option label="字符串" value="string" />
              <el-option label="表达式" value="expression" />
            </el-select>
            <el-input
              v-model="field.value"
              :placeholder="field.type === 'expression' ? '${expr}' : '值'"
              size="small"
              style="flex: 1"
            />
            <el-button
              type="danger"
              :icon="'Delete'"
              size="small"
              circle
              @click="removeField(index)"
            />
          </div>
          <el-button type="primary" link size="small" @click="addField">
            + 添加字段
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.fields-section {
  width: 100%;
}

.field-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
</style>
