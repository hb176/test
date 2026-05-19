<script setup lang="ts">
import { getMyStarted, withdrawProcess, urgeProcess } from '@/api/workflow'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyStarted({ pageNum: pageNum.value, pageSize: 10 })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function handleWithdraw(row: any) {
  await ElMessageBox.confirm('确定撤回该流程吗？', '确认撤回')
  await withdrawProcess(row.id)
  ElMessage.success('已撤回')
  fetchData()
}

async function handleUrge(row: any) {
  await urgeProcess(row.id)
  ElMessage.success('催办已发送')
}

const router = useRouter()
onMounted(fetchData)
</script>

<template>
  <div>
    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="processTitle" label="流程标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="processKey" label="流程类型" width="120" />
        <el-table-column prop="processStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.processStatus === 'APPROVING' ? 'warning' : row.processStatus === 'COMPLETED' ? 'success' : 'danger'">
              {{ row.processStatus === 'APPROVING' ? '审批中' : row.processStatus === 'COMPLETED' ? '已完成' : row.processStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="发起时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/workflow/detail/${row.id}`)">查看</el-button>
            <el-button link type="warning" v-if="row.processStatus === 'APPROVING'" @click="handleWithdraw(row)">撤回</el-button>
            <el-button link v-if="row.processStatus === 'APPROVING'" @click="handleUrge(row)">催办</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="pageNum" :total="total" layout="total,prev,pager,next" @change="fetchData" />
      </div>
    </el-card>
  </div>
</template>
