<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getTaskTodo } from '@/api/workflow'
import WorkflowPageHeader from '@/components/Workflow/WorkflowPageHeader.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = reactive({ processKey: '', keyword: '' })
const router = useRouter()

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskTodo({ pageNum: pageNum.value, pageSize: pageSize.value, ...filters })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleProcess(row: any) {
  router.push(`/workflow/detail/${row.instanceId || row.id}`)
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}

onMounted(fetchData)
</script>

<template>
  <div class="gmp-page workflow-list-page">
    <WorkflowPageHeader
      current="todo"
      title="我的待办"
      subtitle="集中处理需要当前用户审批、确认或补充资料的流程任务。"
    />

    <el-card class="gmp-table-card">
      <div class="gmp-toolbar">
        <div>
          <h2 class="gmp-section-title">待处理任务</h2>
          <p class="gmp-section-subtitle">点击行或办理按钮进入流程详情。</p>
        </div>
      </div>

      <el-form :model="filters" inline class="workflow-filter">
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

      <el-table :data="list" v-loading="loading" stripe highlight-current-row @row-click="handleProcess">
        <el-table-column prop="processTitle" label="流程标题" show-overflow-tooltip min-width="220" />
        <el-table-column prop="processKey" label="流程类型" width="130" />
        <el-table-column prop="nodeName" label="当前节点" width="130" />
        <el-table-column prop="startUserName" label="发起人" width="110" />
        <el-table-column prop="startTime" label="发起时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="到达时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleProcess(row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="workflow-pagination">
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
  </div>
</template>

<style scoped>
.workflow-filter {
  margin-bottom: 4px;
}

.workflow-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
