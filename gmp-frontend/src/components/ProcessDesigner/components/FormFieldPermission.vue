<script setup lang="ts">
/**
 * FormFieldPermission — 节点表单字段权限配置
 *
 * 展示表单字段列表，支持设置只读/隐藏/必填
 * 调用后端 BpmnFormShowController API
 */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  processKey: string
  activityId: string
  formCode: string
}>()

const visible = ref(false)
const loading = ref(false)
const fields = ref<any[]>([])

async function loadFields() {
  if (!props.formCode) {
    ElMessage.warning('请先选择表单')
    return
  }
  loading.value = true
  try {
    const res = await fetch('/flow/form/show/getFormItemShowsByActivityId', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        processKey: props.processKey,
        activityId: props.activityId,
        formCode: props.formCode
      })
    })
    const data = await res.json()
    fields.value = data.data || []
    visible.value = true
  } catch (e) {
    ElMessage.error('获取字段列表失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  try {
    const res = await fetch('/flow/form/show/saveOne', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        processKey: props.processKey,
        activityId: props.activityId,
        fields: fields.value.map(f => ({
          fieldKey: f.code || f.key,
          fieldName: f.name,
          readonly: f.readonly || false,
          hidden: f.hidden || false,
          required: f.required || false
        }))
      })
    })
    const data = await res.json()
    if (data.success) {
      ElMessage.success('保存成功')
      visible.value = false
    } else {
      ElMessage.error(data.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}
</script>

<template>
  <div class="form-field-permission">
    <el-button size="small" :loading="loading" @click="loadFields">配置字段权限</el-button>

    <el-dialog v-model="visible" title="节点表单字段权限" width="600px" append-to-body>
      <el-table :data="fields" size="small" max-height="400">
        <el-table-column prop="name" label="字段名称" width="150" />
        <el-table-column prop="code" label="字段Key" width="150" show-overflow-tooltip />
        <el-table-column label="只读" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.readonly" />
          </template>
        </el-table-column>
        <el-table-column label="隐藏" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.hidden" />
          </template>
        </el-table-column>
        <el-table-column label="必填" width="70" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.required" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
