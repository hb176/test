<script setup lang="ts">
import { getApqrPage, getDashboard } from '@/api/business'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dashboard = ref<any>({})

async function fetchData() {
  loading.value = true
  try {
    const res = await getApqrPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchDashboard() {
  try {
    const res = await getDashboard()
    dashboard.value = res.data || {}
  } catch (e) {
    // ignore
  }
}

function handleCreate() {
  ElMessage.info('创建功能由可配置表单引擎驱动')
}

function handleView(row: any) {
  ElMessage.info(`查看APQR: ${row.id}`)
}

onMounted(() => {
  fetchData()
  fetchDashboard()
})
</script>
<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3>质量回顾 (QRS)</h3>
      <el-button type="primary" @click="handleCreate">新建APQR</el-button>
    </div>

    <!-- 仪表盘概览 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-statistic title="总报告数" :value="dashboard.totalReports || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="进行中" :value="dashboard.inProgress || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已完成" :value="dashboard.completed || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="待审核" :value="dashboard.pending || 0" />
      </el-col>
    </el-row>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="businessNo" label="报告编号" width="150" />
      <el-table-column prop="title" label="报告标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="businessStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.businessStatus === 'COMPLETED' ? 'success' : row.businessStatus === 'APPROVING' ? 'warning' : 'info'">
            {{ row.businessStatus || '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="initiatorName" label="创建人" width="100" />
      <el-table-column prop="initiatedAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display:flex;justify-content:flex-end;margin-top:16px">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        @size-change="pageSize=$event;pageNum=1;fetchData()"
      />
    </div>
  </el-card>
</template>
