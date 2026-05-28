<script setup lang="ts">
/**
 * FormRunner — 通用表单渲染页
 *
 * 从菜单点击进入，根据 formKey 加载表单定义并动态渲染
 * 提交时保存表单数据，如果关联了流程则自动启动
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFormDefinitionByKey } from '@/api/form'
import { createFormData } from '@/api/form'
import { startProcess } from '@/api/workflow'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const formDef = ref<any>(null)
const formFields = ref<any[]>([])
const formData = ref<Record<string, any>>({})
const formKey = computed(() => route.params.formKey as string)

onMounted(async () => {
  if (!formKey.value) {
    ElMessage.error('缺少表单标识')
    return
  }
  await loadFormDefinition()
})

async function loadFormDefinition() {
  loading.value = true
  try {
    const res = await getFormDefinitionByKey(formKey.value)
    if (res.data) {
      formDef.value = res.data
      parseFields(res.data.editContent)
    } else {
      ElMessage.error('表单定义不存在')
    }
  } catch (e: any) {
    ElMessage.error('加载表单失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

function parseFields(editContent: string) {
  if (!editContent) return
  try {
    const schema = JSON.parse(editContent)
    const fields = schema.fields || schema.items || []
    formFields.value = fields
    // 初始化表单数据
    const init: Record<string, any> = {}
    for (const f of fields) {
      init[f.fieldKey || f.key] = f.defaultValue || ''
    }
    formData.value = init
  } catch (e) {
    console.warn('解析表单Schema失败:', e)
  }
}

async function handleSubmit() {
  // 简单校验必填
  for (const f of formFields.value) {
    const key = f.fieldKey || f.key
    if (f.required && !formData.value[key]) {
      ElMessage.warning(`请填写 ${f.fieldName || f.label || key}`)
      return
    }
  }

  submitting.value = true
  try {
    // 1. 保存表单数据
    const saveRes = await createFormData({
      formKey: formKey.value,
      formDefId: formDef.value.id,
      formData: JSON.stringify(formData.value),
      dataStatus: 'DRAFT'
    })

    const formId = saveRes.data?.id

    // 2. 如果关联了流程，启动流程
    if (formDef.value.workflowKey) {
      await startProcess({
        processKey: formDef.value.workflowKey,
        formKey: formKey.value,
        formId: formId,
        businessKey: formKey.value + '_' + formId,
        variables: formData.value
      })
      ElMessage.success('提交成功，流程已启动')
    } else {
      ElMessage.success('保存成功')
    }

    router.back()
  } catch (e: any) {
    ElMessage.error('提交失败: ' + (e.message || ''))
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="form-runner" v-loading="loading">
    <el-card v-if="formDef">
      <template #header>
        <div class="form-header">
          <span>{{ formDef.name }}</span>
          <el-tag v-if="formDef.workflowKey" type="success" size="small">关联流程</el-tag>
        </div>
      </template>

      <el-form :model="formData" label-width="120px" label-position="right">
        <template v-for="field in formFields" :key="field.fieldKey || field.key">
          <el-form-item
            :label="field.fieldName || field.label || field.fieldKey"
            :required="field.required"
          >
            <!-- 文本 -->
            <el-input
              v-if="!field.fieldType || field.fieldType === 'TEXT'"
              v-model="formData[field.fieldKey || field.key]"
              :placeholder="field.placeholder"
            />

            <!-- 数字 -->
            <el-input-number
              v-else-if="field.fieldType === 'NUMBER'"
              v-model="formData[field.fieldKey || field.key]"
              :placeholder="field.placeholder"
              style="width: 100%"
            />

            <!-- 日期 -->
            <el-date-picker
              v-else-if="field.fieldType === 'DATE'"
              v-model="formData[field.fieldKey || field.key]"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />

            <!-- 下拉选择 -->
            <el-select
              v-else-if="field.fieldType === 'SELECT'"
              v-model="formData[field.fieldKey || field.key]"
              placeholder="请选择"
              style="width: 100%"
            >
              <el-option
                v-for="opt in (field.options || [])"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>

            <!-- 文本域 -->
            <el-input
              v-else-if="field.fieldType === 'TEXTAREA'"
              v-model="formData[field.fieldKey || field.key]"
              type="textarea"
              :rows="3"
              :placeholder="field.placeholder"
            />

            <!-- 通用兜底 -->
            <el-input
              v-else
              v-model="formData[field.fieldKey || field.key]"
              :placeholder="field.placeholder"
            />
          </el-form-item>
        </template>
      </el-form>

      <div class="form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ formDef.workflowKey ? '提交流程' : '保存' }}
        </el-button>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="表单不存在" />
  </div>
</template>

<style scoped>
.form-runner {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}
</style>
