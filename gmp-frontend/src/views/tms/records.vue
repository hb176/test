<script setup lang="ts">
import { getTrainingRecords, evaluateTraining } from '@/api/business'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await getTrainingRecords({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleEvaluate(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('评估结果（通过/不通过/备注）', '培训评估', { inputPattern: /.+/ })
    await evaluateTraining(row.id, { title: value })
    ElMessage.success('评估完成')
    fetchData()
  } catch { /* cancelled */ }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const st: Record<string, { type: TagType; text: string }> = {
  DRAFT: { type: 'info', text: '草稿' },
  APPROVING: { type: 'warning', text: '进行中' },
  COMPLETED: { type: 'success', text: '已完成' },
}
function getTag(s: string) { return st[s] || { type: 'info' as TagType, text: s || '未知' } }

onMounted(fetchData)
</script>

<template>
  <el-card>
    <div class="page-header">
      <h3>培训记录</h3>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索名称/编号" clearable style="width:260px" @keyup.enter="pageNum=1;fetchData()" />
      <el-button type="primary" @click="pageNum=1;fetchData()">搜索</el-button>
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
      <el-table-column prop="initiatorName" label="学员" width="100" />
      <el-table-column prop="initiatedAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.businessStatus!=='COMPLETED'" link type="success" size="small" @click="handleEvaluate(row)">评估</el-button>
          <el-tag v-else type="success" size="small">已评估</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, prev, pager, next" background @current-change="fetchData" @size-change="pageSize=$event;pageNum=1;fetchData()" />
    </div>
  </el-card>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
