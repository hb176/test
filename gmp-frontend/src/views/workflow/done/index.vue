<script setup lang="ts">
import { getTaskDone } from '@/api/workflow'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskDone({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
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
        <el-table-column prop="comment" label="我的审批意见" min-width="150" show-overflow-tooltip />
        <el-table-column prop="approveResult" label="审批结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.approveResult === 'APPROVED' ? 'success' : 'danger'">
              {{ row.approveResult === 'APPROVED' ? '同意' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="160" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/workflow/detail/${row.instanceId}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="total,prev,pager,next" @change="fetchData" />
      </div>
    </el-card>
  </div>
</template>
