<script setup lang="ts">
/**
 * 表单设计器页面 - 可配置表单的可视化设计
 *
 * 设计理念：
 * 1. 左侧：可用字段类型面板（拖拽源）
 * 2. 中间：表单预览区（字段布局编排）
 * 3. 右侧：选中字段的属性配置面板
 *
 * 支持的字段类型:
 * TEXT(文本), TEXTAREA(多行文本), NUMBER(数字), DATE(日期),
 * SELECT(下拉), RADIO(单选), CHECKBOX(多选), SWITCH(开关),
 * FILE(文件上传), RICH_TEXT(富文本), TABLE(子表格)
 */
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createFormDefinition, updateFormDefinition, getFormDefinitionById } from '@/api/form'

const route = useRoute()
const router = useRouter()
const formId = route.params.id as string | undefined
const isEdit = !!formId

// 表单基本属性
const formDef = reactive({
  formKey: '',
  formName: '',
  category: '',
  description: '',
})

// 字段列表（中间画布的字段）
interface FormField {
  key: string
  type: string
  label: string
  required: boolean
  placeholder: string
  rules: any[]
  options?: { label: string; value: string }[]
}
const fields = ref<FormField[]>([])

// 当前选中的字段
const selectedField = ref<FormField | null>(null)
const selectedIndex = ref(-1)

// 左侧可用字段类型
const fieldTypes = [
  { type: 'TEXT', label: '单行文本', icon: 'Edit' },
  { type: 'TEXTAREA', label: '多行文本', icon: 'Document' },
  { type: 'NUMBER', label: '数字', icon: 'Sort' },
  { type: 'DATE', label: '日期', icon: 'Calendar' },
  { type: 'SELECT', label: '下拉选择', icon: 'ArrowDown' },
  { type: 'RADIO', label: '单选按钮', icon: 'Check' },
  { type: 'CHECKBOX', label: '多选框', icon: 'Select' },
  { type: 'SWITCH', label: '开关', icon: 'Switch' },
  { type: 'FILE', label: '文件上传', icon: 'Upload' },
  { type: 'RICH_TEXT', label: '富文本', icon: 'EditPen' },
  { type: 'TABLE', label: '子表格', icon: 'Grid' },
]

function addField(type: string) {
  const fieldType = fieldTypes.find(f => f.type === type)
  const field: FormField = {
    key: 'field_' + Date.now(),
    type,
    label: fieldType?.label || type,
    required: false,
    placeholder: '请输入' + (fieldType?.label || ''),
    rules: [],
    options: type === 'SELECT' || type === 'RADIO' || type === 'CHECKBOX'
      ? [{ label: '选项1', value: '1' }, { label: '选项2', value: '2' }]
      : undefined,
  }
  fields.value.push(field)
  selectField(fields.value.length - 1)
}

function selectField(index: number) {
  selectedIndex.value = index
  selectedField.value = fields.value[index]
}

function removeField(index: number) {
  fields.value.splice(index, 1)
  if (selectedIndex.value === index) {
    selectedField.value = null
    selectedIndex.value = -1
  }
}

function moveField(index: number, direction: 'up' | 'down') {
  const target = direction === 'up' ? index - 1 : index + 1
  if (target < 0 || target >= fields.value.length) return
  const temp = fields.value[index]
  fields.value[index] = fields.value[target]
  fields.value[target] = temp
  selectField(target)
}

/** 生成JSON Schema */
function generateSchema() {
  return JSON.stringify({
    formKey: formDef.formKey,
    formName: formDef.formName,
    version: 1,
    fields: fields.value.map(f => ({
      fieldKey: f.key,
      fieldName: f.label,
      fieldType: f.type,
      required: f.required,
      placeholder: f.placeholder,
      rules: f.rules,
      options: f.options,
    })),
  }, null, 2)
}

/** 保存表单定义 */
async function handleSave() {
  if (!formDef.formKey || !formDef.formName) {
    ElMessage.warning('请填写表单Key和名称')
    return
  }
  const formData = JSON.parse(generateSchema())
  const payload = {
    formKey: formData.formKey,
    formName: formData.formName,
    category: formDef.category,
    description: formDef.description,
    editContent: JSON.stringify(formData),
  }
  try {
    if (isEdit && formId) {
      await updateFormDefinition(Number(formId), payload)
    } else {
      await createFormDefinition(payload)
    }
    ElMessage.success('表单定义已保存')
    router.back()
  } catch (e: any) {
    ElMessage.error('保存失败: ' + (e.message || '未知错误'))
  }
}

/** 加载已有表单 */
async function loadForm() {
  if (!isEdit || !formId) return
  try {
    const res = await getFormDefinitionById(Number(formId))
    const def = res.data
    formDef.formKey = def.formKey || def.code || ''
    formDef.formName = def.formName || def.name || ''
    formDef.category = def.category || ''
    formDef.description = def.description || ''
    if (def.editContent) {
      const schema = typeof def.editContent === 'string' ? JSON.parse(def.editContent) : def.editContent
      if (schema.fields) {
        fields.value = schema.fields.map((f: any) => ({
          key: f.fieldKey,
          type: f.fieldType,
          label: f.fieldName,
          required: f.required || false,
          placeholder: f.placeholder || '',
          rules: f.rules || [],
          options: f.options || undefined,
        }))
      }
    }
  } catch (e: any) {
    ElMessage.warning('加载表单定义失败')
  }
}

onMounted(() => { loadForm() })

/** 预览JSON Schema */
const previewVisible = ref(false)
function handlePreview() {
  previewVisible.value = true
}
</script>

<template>
  <div class="form-designer">
    <!-- 顶栏 -->
    <div class="designer-header">
      <el-form :model="formDef" inline size="small">
        <el-form-item label="表单Key" required><el-input v-model="formDef.formKey" placeholder="如 DEVIATION_FORM" /></el-form-item>
        <el-form-item label="表单名称" required><el-input v-model="formDef.formName" placeholder="如 偏差报告" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formDef.category"><el-option label="QMS" value="QMS" /><el-option label="DMS" value="DMS" /></el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button @click="handlePreview">预览JSON</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 三栏布局 -->
    <div class="designer-body">
      <!-- 左侧：字段类型面板 -->
      <div class="field-palette">
        <h4>字段类型</h4>
        <el-button
          v-for="ft in fieldTypes"
          :key="ft.type"
          style="width:100%;margin-bottom:4px"
          @click="addField(ft.type)"
        >
          <el-icon><component :is="ft.icon" /></el-icon>
          {{ ft.label }}
        </el-button>
      </div>

      <!-- 中间：表单预览区 -->
      <div class="form-preview">
        <h4>表单预览</h4>
        <el-empty v-if="!fields.length" description="从左侧拖拽或点击添加字段" />
        <div v-else>
          <div
            v-for="(field, index) in fields"
            :key="field.key"
            class="field-item"
            :class="{ selected: selectedIndex === index }"
            @click="selectField(index)"
          >
            <div class="field-header">
              <span class="field-label">
                {{ field.label }}
                <span v-if="field.required" style="color:red">*</span>
              </span>
              <span class="field-type-tag">{{ field.type }}</span>
              <div class="field-actions">
                <el-button link size="small" @click.stop="moveField(index, 'up')" :disabled="index===0">↑</el-button>
                <el-button link size="small" @click.stop="moveField(index, 'down')" :disabled="index===fields.length-1">↓</el-button>
                <el-button link size="small" type="danger" @click.stop="removeField(index)">✕</el-button>
              </div>
            </div>
            <!-- 字段预览 -->
            <div class="field-render">
              <el-input v-if="field.type==='TEXT'" :placeholder="field.placeholder" disabled />
              <el-input v-else-if="field.type==='TEXTAREA'" type="textarea" :placeholder="field.placeholder" disabled />
              <el-input-number v-else-if="field.type==='NUMBER'" disabled style="width:100%" />
              <el-date-picker v-else-if="field.type==='DATE'" disabled style="width:100%" />
              <el-select v-else-if="field.type==='SELECT'" disabled style="width:100%" />
              <el-radio-group v-else-if="field.type==='RADIO'" disabled>
                <el-radio v-for="o in field.options" :key="o.value" :label="o.value">{{ o.label }}</el-radio>
              </el-radio-group>
              <el-upload v-else-if="field.type==='FILE'" disabled><el-button>上传文件</el-button></el-upload>
              <div v-else class="field-placeholder">{{ field.type }} 组件</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：字段属性配置面板 -->
      <div class="props-panel" v-if="selectedField">
        <h4>字段属性</h4>
        <el-form size="small" label-width="80px">
          <el-form-item label="字段Key"><el-input v-model="selectedField.key" /></el-form-item>
          <el-form-item label="标签"><el-input v-model="selectedField.label" /></el-form-item>
          <el-form-item label="提示文字"><el-input v-model="selectedField.placeholder" /></el-form-item>
          <el-form-item label="必填">
            <el-switch v-model="selectedField.required" />
          </el-form-item>
          <!-- 选项配置（SELECT/RADIO/CHECKBOX） -->
          <template v-if="['SELECT','RADIO','CHECKBOX'].includes(selectedField.type)">
            <el-form-item label="选项列表">
              <div v-for="(opt, oi) in selectedField.options" :key="oi" style="display:flex;gap:4px;margin-bottom:4px">
                <el-input v-model="opt.label" size="small" placeholder="标签" />
                <el-input v-model="opt.value" size="small" placeholder="值" />
                <el-button size="small" type="danger" @click="selectedField.options!.splice(oi,1)">✕</el-button>
              </div>
              <el-button size="small" @click="selectedField.options!.push({label:'新选项',value:''})">+ 添加选项</el-button>
            </el-form-item>
          </template>
        </el-form>
      </div>
    </div>

    <!-- JSON预览弹窗 -->
    <el-dialog v-model="previewVisible" title="JSON Schema 预览" width="700px">
      <pre style="background:#f5f7fa;padding:16px;border-radius:4px;max-height:500px;overflow:auto">{{ generateSchema() }}</pre>
    </el-dialog>
  </div>
</template>

<style scoped>
.form-designer { display: flex; flex-direction: column; height: calc(100vh - 120px); }
.designer-header { background:#fff; padding:8px 16px; border-bottom:1px solid #e6e6e6; }
.designer-body { display:flex; flex:1; gap:12px; padding:12px; overflow:hidden; }
.field-palette { width:180px; background:#fff; padding:12px; border-radius:4px; overflow-y:auto; }
.form-preview { flex:1; background:#fff; padding:16px; border-radius:4px; overflow-y:auto; }
.props-panel { width:280px; background:#fff; padding:12px; border-radius:4px; overflow-y:auto; }
.field-item { border:1px dashed #dcdfe6; padding:10px; margin-bottom:8px; border-radius:4px; cursor:pointer; }
.field-item.selected { border-color:#409EFF; border-style:solid; background:#ecf5ff; }
.field-header { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.field-type-tag { font-size:11px; color:#909399; background:#f0f2f5; padding:0 6px; border-radius:3px; }
.field-actions { margin-left:auto; }
.field-render { padding:4px 0; }
.field-placeholder { color:#c0c4cc; font-style:italic; }
</style>
