<script setup lang="ts">
import { getDictPage, createDict, updateDict, deleteDict } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const formLoading = ref(false)
const editId = ref<number | null>(null)
const form = reactive({
  dictCode: '', dictName: '', itemLabel: '', itemValue: '',
  sortOrder: 0, status: 1, cssClass: '', remark: '',
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getDictPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openCreate() {
  editId.value = null
  Object.assign(form, { dictCode: '', dictName: '', itemLabel: '', itemValue: '', sortOrder: 0, status: 1, cssClass: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row: any) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  formLoading.value = true
  try {
    if (editId.value) {
      await updateDict({ id: editId.value, ...form })
    } else {
      await createDict(form)
    }
    ElMessage.success(editId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { formLoading.value = false }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除字典项 "${row.itemLabel}"？`, '删除确认', { type: 'warning' })
  await deleteDict(row.id)
  ElMessage.success('已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <div class="page-header">
      <h3>字典管理</h3>
      <el-button type="primary" size="small" @click="openCreate">新增字典项</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="dictCode" label="字典编码" width="150" />
      <el-table-column prop="dictName" label="字典名称" width="150" />
      <el-table-column prop="itemLabel" label="标签" width="120" />
      <el-table-column prop="itemValue" label="值" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, prev, pager, next" background @current-change="fetchData" @size-change="pageSize=$event;pageNum=1;fetchData()" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑字典项' : '新增字典项'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="字典编码" required><el-input v-model="form.dictCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字典名称" required><el-input v-model="form.dictName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="标签" required><el-input v-model="form.itemLabel" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="值" required><el-input v-model="form.itemValue" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="CSS类"><el-input v-model="form.cssClass" placeholder="可选" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
