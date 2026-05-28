<script setup lang="ts">
import { getCoursePage, createCourse, getPlanPage, createTrainingPlan } from '@/api/business'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeTab = ref<'course'|'plan'>('course')
const keyword = ref('')

const dialogVisible = ref(false)
const formLoading = ref(false)
const form = reactive({ title: '', summary: '' })

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined }
    const res = activeTab.value === 'course' ? await getCoursePage(params) : await getPlanPage(params)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchData() }
function handleTabChange() { pageNum.value = 1; keyword.value = ''; fetchData() }

function openCreate() {
  form.title = ''; form.summary = ''
  dialogVisible.value = true
}

async function handleCreate() {
  if (!form.title.trim()) { ElMessage.warning('请输入名称'); return }
  formLoading.value = true
  try {
    if (activeTab.value === 'course') {
      await createCourse({ title: form.title, summary: form.summary })
    } else {
      await createTrainingPlan({ title: form.title, summary: form.summary })
    }
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { formLoading.value = false }
}

function goDetail(row: any) {
  router.push(`/tms/${activeTab.value}/${row.id}`)
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const statusMap: Record<string, { type: TagType; text: string }> = {
  DRAFT: { type: 'info', text: '草稿' },
  APPROVING: { type: 'warning', text: '审批中' },
  COMPLETED: { type: 'success', text: '已完成' },
  IN_PROGRESS: { type: 'primary', text: '进行中' },
  CANCELLED: { type: 'danger', text: '已取消' },
}
function getTag(s: string) { return statusMap[s] || { type: 'info' as TagType, text: s || '未知' } }

onMounted(fetchData)
</script>

<template>
  <el-card>
    <div class="page-header">
      <h3>培训管理 (TMS)</h3>
      <el-button type="primary" @click="openCreate">{{ activeTab === 'course' ? '新建课程' : '新建计划' }}</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="培训课程" name="course" />
      <el-tab-pane label="培训计划" name="plan" />
    </el-tabs>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索名称/编号" clearable style="width:260px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="businessNo" label="编号" width="150" />
      <el-table-column prop="title" label="名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getTag(row.businessStatus).type" size="small">{{ getTag(row.businessStatus).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="initiatorName" label="创建人" width="100" />
      <el-table-column prop="initiatedAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, prev, pager, next" background @current-change="fetchData" @size-change="pageSize=$event;pageNum=1;fetchData()" />
    </div>

    <el-dialog v-model="dialogVisible" :title="activeTab === 'course' ? '新建课程' : '新建计划'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item :label="activeTab === 'course' ? '课程名称' : '计划名称'" required>
          <el-input v-model="form.title" :placeholder="activeTab === 'course' ? '如：GMP基础培训' : '如：2026年度培训计划'" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="描述（可选）" />
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
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.search-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
