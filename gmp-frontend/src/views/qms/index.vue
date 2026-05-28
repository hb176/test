<script setup lang="ts">
/**
 * QMS通用业务页面 - 偏差/CAPA/变更/审计等
 * 通过路由meta或URL参数区分子业务类型
 * 所有QMS子模块共享此页面框架，通过表单+流程引擎驱动
 */
import { getDeviationPage, getCapaPage, getAuditPage } from '@/api/business'

const route = useRoute()
const router = useRouter()
const pageTitle = computed(() => route.meta.title as string || 'QMS')
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 根据路由路径判断业务类型
const businessType = computed(() => {
  const path = route.path
  if (path.includes('/deviation')) return 'deviation'
  if (path.includes('/capa')) return 'capa'
  if (path.includes('/change')) return 'change'
  if (path.includes('/audit')) return 'audit'
  return 'deviation' // 默认
})

async function fetchData() {
  loading.value = true
  try {
    let res
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    switch (businessType.value) {
      case 'deviation':
        res = await getDeviationPage(params)
        break
      case 'capa':
        res = await getCapaPage(params)
        break
      case 'audit':
        res = await getAuditPage(params)
        break
      default:
        res = await getDeviationPage(params)
    }
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  ElMessage.info('创建功能由可配置表单引擎驱动，打开对应的表单')
}

function handleView(row: any) {
  ElMessage.info(`查看记录: ${row.id}`)
}

onMounted(fetchData)
</script>
<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3>{{ pageTitle }}</h3>
      <el-button type="primary" @click="handleCreate">新建</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="businessNo" label="业务编号" width="150" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="businessStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.businessStatus === 'COMPLETED' ? 'success' : row.businessStatus === 'APPROVING' ? 'warning' : 'info'">
            {{ row.businessStatus || '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="initiatorName" label="发起人" width="100" />
      <el-table-column prop="initiatedAt" label="发起时间" width="160" />
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
