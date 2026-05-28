<script setup lang="ts">
import { getDocumentPage, createDocument, submitForReview, obsoleteDocument } from '@/api/business'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')

const dialogVisible = ref(false)
const formLoading = ref(false)
const form = reactive({ title: '', businessType: 'DMS_DOCUMENT', summary: '' })

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已生效', value: 'COMPLETED' },
  { label: '已作废', value: 'OBSOLETED' },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getDocumentPage({
      pageNum: pageNum.value, pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function handleReset() { keyword.value = ''; statusFilter.value = ''; handleSearch() }

function openCreate() {
  form.title = ''; form.summary = ''
  dialogVisible.value = true
}

async function handleCreate() {
  formLoading.value = true
  try {
    const res = await createDocument({ title: form.title, summary: form.summary })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    const newId = res.data?.id
    if (newId) {
      router.push(`/dms/document/${newId}`)
    } else {
      fetchData()
    }
  } finally { formLoading.value = false }
}

function goDetail(row: any) {
  router.push(`/dms/document/${row.id}`)
}

async function handleSubmit(row: any) {
  try {
    await ElMessageBox.confirm('确认提交审核？提交后将启动审批流程。', '提交审核', { type: 'info' })
    await submitForReview(row.id)
    ElMessage.success('已提交审核')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleObsolete(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '文件作废', { type: 'warning' })
    await obsoleteDocument(row.id, { reason: value })
    ElMessage.success('文件已作废')
    fetchData()
  } catch { /* cancelled */ }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

function getStatusTag(status: string): { type: TagType; text: string } {
  const map: Record<string, { type: TagType; text: string }> = {
    DRAFT: { type: 'info', text: '草稿' },
    APPROVING: { type: 'warning', text: '审批中' },
    COMPLETED: { type: 'success', text: '已生效' },
    OBSOLETED: { type: 'danger', text: '已作废' },
    REJECTED: { type: 'danger', text: '已驳回' },
  }
  return map[status] || { type: 'info' as TagType, text: status || '未知' }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <!-- 搜索区 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索文件名称/编号" clearable style="width:240px" @keyup.enter="handleSearch" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width:140px">
        <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <div style="flex:1" />
      <el-button type="primary" @click="openCreate">新建文件</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="businessNo" label="文件编号" width="160" />
      <el-table-column prop="title" label="文件名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.businessStatus).type" size="small">
            {{ getStatusTag(row.businessStatus).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="initiatorName" label="创建人" width="100" />
      <el-table-column prop="initiatedAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goDetail(row)">查看</el-button>
          <el-button v-if="row.businessStatus==='DRAFT'" link type="success" size="small" @click="handleSubmit(row)">提交</el-button>
          <el-button v-if="row.businessStatus==='COMPLETED'" link type="warning" size="small" @click="handleObsolete(row)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" layout="total, prev, pager, next" background
        @current-change="fetchData" @size-change="pageSize=$event;pageNum=1;fetchData()" />
    </div>

    <!-- 新建对话框 -->
    <el-dialog v-model="dialogVisible" title="新建文件" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="文件名称" required>
          <el-input v-model="form.title" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="文件描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.search-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
