<script setup lang="ts">
/**
 * FormTagInput — 表单选择器
 *
 * 弹窗式表单选择，调用 getCustomFormPagerModel API
 * 支持搜索、分页
 */
import { ref, watch } from 'vue'
import { getCustomFormPagerModel } from '@/api/bpmn'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// ==================== 状态 ====================

const visible = ref(false)
const loading = ref(false)
const keyword = ref('')
const formList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedForm = ref<any>(null)

// ==================== 初始化 ====================

watch(() => props.modelValue, (val) => {
  try {
    if (val && typeof val === 'string' && val.length > 0) {
      selectedForm.value = { code: val, name: val }
    } else {
      selectedForm.value = null
    }
  } catch {
    selectedForm.value = null
  }
}, { immediate: true })

// ==================== 表单列表 ====================

async function loadForms() {
  loading.value = true
  try {
    const res = await getCustomFormPagerModel({
      query: { pageNum: pageNum.value, pageSize: pageSize.value },
      entity: { keyword: keyword.value }
    })
    // bpmnRequest 返回 res 直接，data 在 res.data 中
    if (res.data) {
      formList.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.warn('加载表单列表失败:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadForms()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadForms()
}

// ==================== 选择 ====================

function handleSelect(form: any) {
  selectedForm.value = form
}

function handleConfirm() {
  if (selectedForm.value) {
    emit('update:modelValue', selectedForm.value.code)
  }
  visible.value = false
}

function handleClear() {
  selectedForm.value = null
  emit('update:modelValue', '')
}

function handleOpen() {
  visible.value = true
  loadForms()
}
</script>

<template>
  <div class="form-tag-input">
    <!-- 触发区域 -->
    <div class="trigger" @click="handleOpen">
      <el-tag
        v-if="selectedForm"
        closable
        size="small"
        type="success"
        @close="handleClear"
      >
        {{ selectedForm.name || selectedForm.code }}
      </el-tag>
      <span v-else class="placeholder">
        {{ props.placeholder || '点击选择表单' }}
      </span>
    </div>

    <!-- 选择弹窗 -->
    <el-dialog
      v-model="visible"
      title="表单选择"
      width="600px"
      top="10vh"
      @open="handleOpen"
    >
      <div class="dialog-body">
        <div class="list-header">
          <el-input
            v-model="keyword"
            placeholder="搜索表单"
            clearable
            size="small"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-table
          :data="formList"
          v-loading="loading"
          size="small"
          height="350"
          highlight-current-row
          @row-click="handleSelect"
          row-class-name="clickable-row"
        >
          <el-table-column prop="code" label="表单编码" width="200" />
          <el-table-column prop="name" label="表单名称" />
          <el-table-column prop="categoryName" label="分类" width="100" />
        </el-table>

        <div class="list-footer">
          <el-pagination
            small
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNum"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="handleClear" v-if="selectedForm">清除选择</el-button>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.form-tag-input {
  width: 100%;
}

.trigger {
  min-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  transition: border-color 0.2s;
}

.trigger:hover {
  border-color: #409eff;
}

.placeholder {
  color: #c0c4cc;
  font-size: 13px;
}

.dialog-body {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.list-header {
  padding: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.list-footer {
  padding: 8px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e4e7ed;
}

:deep(.clickable-row) {
  cursor: pointer;
}
</style>
