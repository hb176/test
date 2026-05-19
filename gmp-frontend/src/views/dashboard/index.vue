<script setup lang="ts">
import { getDashboard } from '@/api/business'
import { useWorkflowStore } from '@/stores/workflow'
import { Document, List, DataAnalysis, Warning } from '@element-plus/icons-vue'

const workflowStore = useWorkflowStore()
const dashboardData = ref<any>({})

onMounted(async () => {
  try {
    const res = await getDashboard()
    dashboardData.value = res.data || {}
  } catch (_) { /* placeholder */ }
  workflowStore.fetchTodoCount()
})

interface MenuAction {
  label: string
  path?: string
  type?: 'primary' | 'default'
}

interface SystemMenu {
  title: string
  actions: MenuAction[]
}

const systemMenus: SystemMenu[] = [
  {
    title: '质量管理',
    actions: [
      { label: '发起偏差', path: '/qms/deviation', type: 'primary' },
      { label: '偏差列表', path: '/qms/deviation' },
      { label: '发起CAPA', path: '/qms/capa', type: 'primary' },
      { label: 'CAPA列表', path: '/qms/capa' },
      { label: '变更控制', path: '/qms/change' },
      { label: '审计管理', path: '/qms/audit' },
    ],
  },
  {
    title: '文件管理',
    actions: [
      { label: '文件申请', path: '/dms/document', type: 'primary' },
      { label: '文件生效列表', path: '/dms/document' },
      { label: '打印文件列表', path: '/dms/document' },
      { label: '打印记录列表', path: '/dms/document' },
      { label: '文件归档', path: '/dms/document' },
    ],
  },
  {
    title: '培训管理',
    actions: [
      { label: '发起培训', path: '/tms/course', type: 'primary' },
      { label: '培训课程列表', path: '/tms/course' },
      { label: '培训记录', path: '/tms/course' },
      { label: '考核管理', path: '/tms/course' },
    ],
  },
  {
    title: '质量回顾',
    actions: [
      { label: '发起APQR', path: '/qrs/apqr', type: 'primary' },
      { label: '回顾列表', path: '/qrs/apqr' },
      { label: '数据统计', path: '/qrs/apqr' },
    ],
  },
  {
    title: '流程中心',
    actions: [
      { label: '我的待办', path: '/workflow/todo', type: 'primary' },
      { label: '发起流程', path: '/workflow/definition' },
      { label: '流程定义', path: '/workflow/definition' },
      { label: '我的已办', path: '/workflow/done' },
      { label: '我的发起', path: '/workflow/started' },
    ],
  },
  {
    title: '表单中心',
    actions: [
      { label: '新建表单', path: '/form/designer', type: 'primary' },
      { label: '表单列表', path: '/form/definition' },
      { label: '表单数据', path: '/form/data' },
    ],
  },
]

const statCards = computed(() => [
  { title: '我的待办', value: workflowStore.todoCount, icon: List, color: '#409EFF' },
  { title: '我的已办', value: '--', icon: Document, color: '#67C23A' },
  { title: '进行中的流程', value: '--', icon: DataAnalysis, color: '#E6A23C' },
  { title: '超时预警', value: workflowStore.overdueCount, icon: Warning, color: '#F56C6C' },
])
</script>

<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-left">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
            <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各系统菜单面板 -->
    <el-row :gutter="16" class="menu-panel-row">
      <el-col :span="4" v-for="sys in systemMenus" :key="sys.title">
        <div class="menu-panel">
          <div class="panel-title">{{ sys.title }}</div>
          <div
            v-for="act in sys.actions"
            :key="act.label"
            class="panel-link"
            :class="{ 'is-primary': act.type === 'primary' }"
            @click="$router.push(act.path)"
          >
            {{ act.label }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近待办 -->
    <el-card header="最近待办任务" style="margin-top:16px">
      <el-empty description="暂无待办任务" v-if="!workflowStore.todoCount" />
      <el-table :data="workflowStore.todoList" stripe v-else>
        <el-table-column prop="processTitle" label="流程标题" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="当前节点" width="120" />
        <el-table-column prop="startTime" label="发起时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button type="primary" link>处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.menu-panel-row { margin-top: 16px; }
.menu-panel { background: #fff; border-radius: 6px; padding: 12px 0; }
.panel-title { font-size: 13px; font-weight: 600; color: #303133; padding: 0 14px 8px; border-bottom: 1px solid #f0f0f0; margin-bottom: 4px; }
.panel-link { font-size: 12px; color: #606266; padding: 5px 14px; cursor: pointer; }
.panel-link:hover { color: #409EFF; background: #f5f7fa; }
.panel-link.is-primary { color: #409EFF; font-weight: 500; }

.stat-row { margin-bottom: 8px; }
.stat-content { display: flex; align-items: center; justify-content: space-between; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-title { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
