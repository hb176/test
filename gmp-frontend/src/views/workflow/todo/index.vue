<script setup lang="ts">
import { getTaskTodo } from '@/api/workflow'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = reactive({ processKey: '', keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskTodo({ pageNum: pageNum.value, pageSize: pageSize.value, ...filters })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleProcess(row: any) {
  // 跳转到流程详情页（含表单和审批操作）
  router.push(`/workflow/detail/${row.instanceId || row.id}`)
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}

const router = useRouter()
onMounted(fetchData)
</script>

<template>
  <div>
    <el-card>
      <!-- 搜索栏 -->
      <el-form :model="filters" inline>
        <el-form-item label="流程类型">
          <el-select v-model="filters.processKey" clearable placeholder="全部流程" style="width:200px">
            <el-option label="偏差处理" value="QMS_DEVIATION" />
            <el-option label="CAPA" value="QMS_CAPA" />
            <el-option label="变更控制" value="QMS_CHANGE" />
            <el-option label="文件审批" value="DMS_DOCUMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="标题/编号" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 待办列表 -->
      <el-table :data="list" v-loading="loading" stripe highlight-current-row @row-click="handleProcess" style="cursor:pointer">
        <el-table-column prop="processTitle" label="流程标题" show-overflow-tooltip min-width="200" />
        <el-table-column prop="processKey" label="流程类型" width="120" />
        <el-table-column prop="nodeName" label="当前审批节点" width="130" />
        <el-table-column prop="startUserName" label="发起人" width="100" />
        <el-table-column prop="startTime" label="发起时间" width="160">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="到达时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleProcess(row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>
