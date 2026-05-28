<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getTaskDone } from '@/api/workflow'
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
    const res = await getTaskDone({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}

onMounted(fetchData)
</script>

<template>
  <div class="gmp-page workflow-list-page">
    <WorkflowPageHeader
      current="done"
      title="我的已办"
      subtitle="查看本人已处理的流程、审批意见和处理结果。"
    />

    <el-card class="gmp-table-card">
      <div class="gmp-toolbar">
        <div>
          <h2 class="gmp-section-title">已处理记录</h2>
          <p class="gmp-section-subtitle">保留审批意见和流程结果，便于追溯。</p>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="processTitle" label="流程标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="processKey" label="流程类型" width="130" />
        <el-table-column prop="comment" label="我的审批意见" min-width="160" show-overflow-tooltip />
        <el-table-column prop="approveResult" label="审批结果" width="110">
          <template #default="{ row }">
            <el-tag :type="row.approveResult === 'APPROVED' ? 'success' : 'danger'">
              {{ row.approveResult === 'APPROVED' ? '同意' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="170">
          <template #default="{ row }">{{ formatTime(row.finishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/workflow/detail/${row.instanceId}`)">查看</el-button>
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
