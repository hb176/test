<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getMyStarted, urgeProcess, withdrawProcess } from '@/api/workflow'
import WorkflowPageHeader from '@/components/Workflow/WorkflowPageHeader.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const router = useRouter()

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyStarted({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}

function statusText(status: string) {
  if (status === 'APPROVING') return '审批中'
  if (status === 'COMPLETED') return '已完成'
  return status || '--'
}

function statusType(status: string) {
  if (status === 'APPROVING') return 'warning'
  if (status === 'COMPLETED') return 'success'
  return 'danger'
}

onMounted(fetchData)
</script>

<template>
  <div class="gmp-page workflow-list-page">
    <WorkflowPageHeader
      current="started"
      title="我的发起"
      subtitle="跟踪本人发起流程的当前状态，并对审批中的流程执行撤回或催办。"
    />

    <el-card class="gmp-table-card">
      <div class="gmp-toolbar">
        <div>
          <h2 class="gmp-section-title">发起记录</h2>
          <p class="gmp-section-subtitle">关注审批中流程的处理进度。</p>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="processTitle" label="流程标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="processKey" label="流程类型" width="130" />
        <el-table-column prop="processStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.processStatus)">
              {{ statusText(row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="发起时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/workflow/detail/${row.id}`)">查看</el-button>
            <el-button
              v-if="row.processStatus === 'APPROVING'"
              link
              type="warning"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
            <el-button v-if="row.processStatus === 'APPROVING'" link @click="handleUrge(row)">催办</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="workflow-pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total,prev,pager,next"
          @current-change="fetchData"
          @size-change="pageSize=$event;pageNum=1;fetchData()"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.workflow-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
