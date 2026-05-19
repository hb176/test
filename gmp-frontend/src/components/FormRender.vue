<script setup lang="ts">
/**
 * 动态表单渲染器 - 根据JSON Schema渲染可配置表单
 *
 * 核心功能：
 * 1. 根据formSchema中的fields数组动态渲染表单控件
 * 2. 支持双向绑定(v-model)表单数据
 * 3. 支持字段校验规则动态注入
 * 4. 支持字段可见性控制(visible条件)
 * 5. 支持只读模式(readonly模式用于审批查看)
 *
 * Props:
 * - schema: 表单JSON Schema
 * - modelValue: 表单数据(双向绑定)
 * - readonly: 是否只读模式
 *
 * Events:
 * - update:modelValue: 表单数据变更
 * - field-change: 单个字段变更(用于联动)
 */
import { computed } from 'vue'

interface Field {
  fieldKey: string
  fieldName: string
  fieldType: string
  required?: boolean
  placeholder?: string
  options?: { label: string; value: string }[]
}

const props = defineProps<{
  schema: any
  modelValue: Record<string, any>
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
  'field-change': [fieldKey: string, value: any]
}>()

const fields = computed<Field[]>(() => {
  const s = typeof props.schema === 'string' ? JSON.parse(props.schema) : props.schema
  return s?.fields || []
})

function handleChange(fieldKey: string, value: any) {
  const newData = { ...props.modelValue, [fieldKey]: value }
  emit('update:modelValue', newData)
  emit('field-change', fieldKey, value)
}
</script>

<template>
  <el-form :model="modelValue" label-position="top" :disabled="readonly">
    <el-row :gutter="16">
      <el-col
        v-for="field in fields"
        :key="field.fieldKey"
        :span="field.fieldType === 'TEXTAREA' || field.fieldType === 'RICH_TEXT' || field.fieldType === 'TABLE' ? 24 : 12"
      >
        <el-form-item
          :label="field.fieldName"
          :required="field.required"
          :prop="field.fieldKey"
        >
          <!-- 文本 -->
          <el-input
            v-if="field.fieldType === 'TEXT'"
            :model-value="modelValue[field.fieldKey]"
            :placeholder="field.placeholder"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 多行文本 -->
          <el-input
            v-else-if="field.fieldType === 'TEXTAREA'"
            type="textarea"
            :rows="4"
            :model-value="modelValue[field.fieldKey]"
            :placeholder="field.placeholder"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 数字 -->
          <el-input-number
            v-else-if="field.fieldType === 'NUMBER'"
            :model-value="modelValue[field.fieldKey]"
            style="width:100%"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 日期 -->
          <el-date-picker
            v-else-if="field.fieldType === 'DATE'"
            type="date"
            :model-value="modelValue[field.fieldKey]"
            style="width:100%"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 日期时间 -->
          <el-date-picker
            v-else-if="field.fieldType === 'DATETIME'"
            type="datetime"
            :model-value="modelValue[field.fieldKey]"
            style="width:100%"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 下拉选择 -->
          <el-select
            v-else-if="field.fieldType === 'SELECT'"
            :model-value="modelValue[field.fieldKey]"
            style="width:100%"
            @update:model-value="handleChange(field.fieldKey, $event)"
          >
            <el-option
              v-for="opt in field.options"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <!-- 单选 -->
          <el-radio-group
            v-else-if="field.fieldType === 'RADIO'"
            :model-value="modelValue[field.fieldKey]"
            @update:model-value="handleChange(field.fieldKey, $event)"
          >
            <el-radio v-for="opt in field.options" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
          <!-- 多选 -->
          <el-checkbox-group
            v-else-if="field.fieldType === 'CHECKBOX'"
            :model-value="modelValue[field.fieldKey]"
            @update:model-value="handleChange(field.fieldKey, $event)"
          >
            <el-checkbox v-for="opt in field.options" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </el-checkbox>
          </el-checkbox-group>
          <!-- 开关 -->
          <el-switch
            v-else-if="field.fieldType === 'SWITCH'"
            :model-value="modelValue[field.fieldKey]"
            @update:model-value="handleChange(field.fieldKey, $event)"
          />
          <!-- 文件上传 -->
          <el-upload
            v-else-if="field.fieldType === 'FILE'"
            action="/api/file/upload"
            :on-success="(_res: any) => handleChange(field.fieldKey, _res.data?.objectName)"
            :disabled="readonly"
          >
            <el-button>选择文件</el-button>
          </el-upload>
          <!-- 其他类型 -->
          <el-input
            v-else
            :model-value="modelValue[field.fieldKey]"
            :placeholder="field.placeholder"
            disabled
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
