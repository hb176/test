<script setup lang="ts">
import { getDefinitionList, deleteDefinition } from '@/api/workflow'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await getDefinitionList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除流程"${row.processName}"？删除后不可恢复。`, '确认删除')
  await deleteDefinition(row.id)
  ElMessage.success('已删除')
  fetchData()
}

function handleEdit(row: any) {
  router.push(`/workflow/designer/${row.id}`)
}

function handleDeployNew() {
  router.push('/workflow/designer')
}

function formatTime(t: string) { return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '' }

function handleSearch() { pageNum.value = 1; fetchData() }

const router = useRouter()
onMounted(fetchData)
</script>

<template>
  <div>
    <el-card>
      <div style="display:flex;justify-content:space-between;margin-bottom:16px">
        <div style="display:flex;gap:8px">
          <el-input v-model="keyword" placeholder="搜索流程名称" clearable style="width:240px" @keyup.enter="handleSearch" />
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
        <el-button type="primary" @click="handleDeployNew">新建流程</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="processName" label="流程名称" min-width="180" />
        <el-table-column prop="processKey" label="流程Key" width="160" />
        <el-table-column prop="version" label="版本" width="60" />
        <el-table-column prop="category" label="分类" width="80" />
        <el-table-column prop="createTime" label="更新时间" width="160">
          <template #default="{ row }">{{ formatTime(row.lastUpdateTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="total,prev,pager,next" @change="fetchData" />
      </div>
    </el-card>
  </div>
</template>
