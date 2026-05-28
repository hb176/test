<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getUnreadCount, getDocumentPage } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { useWorkflowStore } from '@/stores/workflow'
import {
  DataAnalysis, Files, List, Grid,
  Operation, School, SetUp, Warning, Bell, Clock,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const workflowStore = useWorkflowStore()
const loading = ref(false)
const unreadCount = ref(0)
const recentDocs = ref<any[]>([])
const runningTasks = ref(0)

interface QuickAction {
  label: string
  path: string
  primary?: boolean
}

interface QuickModule {
  title: string
  desc: string
  icon: any
  tone: string
  actions: QuickAction[]
}

const quickModules: QuickModule[] = [
  {
    title: '质量管理',
    desc: '偏差、CAPA、变更、审计',
    icon: Warning,
    tone: 'blue',
    actions: [
      { label: '发起偏差', path: '/qms/deviation', primary: true },
      { label: 'CAPA列表', path: '/qms/capa' },
      { label: '变更控制', path: '/qms/change' },
      { label: '审计管理', path: '/qms/audit' },
    ],
  },
  {
    title: '文件管理',
    desc: '文件申请、生效、归档',
    icon: Files,
    tone: 'green',
    actions: [
      { label: '文件申请', path: '/dms/document', primary: true },
      { label: '文件列表', path: '/dms/document' },
      { label: '打印记录', path: '/dms/document' },
    ],
  },
  {
    title: '培训管理',
    desc: '课程、记录、考核',
    icon: School,
    tone: 'amber',
    actions: [
      { label: '发起培训', path: '/tms/course', primary: true },
      { label: '培训课程', path: '/tms/course' },
      { label: '考核管理', path: '/tms/course' },
    ],
  },
  {
    title: '质量回顾',
    desc: 'APQR与数据统计',
    icon: DataAnalysis,
    tone: 'red',
    actions: [
      { label: '发起APQR', path: '/qrs/apqr', primary: true },
      { label: '回顾列表', path: '/qrs/apqr' },
      { label: '数据统计', path: '/qrs/apqr' },
    ],
  },
  {
    title: '流程中心',
    desc: '待办、已办、发起记录',
    icon: Operation,
    tone: 'indigo',
    actions: [
      { label: '我的待办', path: '/workflow/todo', primary: true },
      { label: '发起流程', path: '/workflow/definition' },
      { label: '我的已办', path: '/workflow/done' },
      { label: '我的发起', path: '/workflow/started' },
    ],
  },
  {
    title: '表单中心',
    desc: '表单设计与数据',
    icon: Grid,
    tone: 'slate',
    actions: [
      { label: '新建表单', path: '/form/designer', primary: true },
      { label: '表单列表', path: '/form/definition' },
      { label: '表单数据', path: '/form/data' },
    ],
  },
]

const statCards = computed(() => [
  {
    title: '待办任务',
    value: workflowStore.todoCount,
    icon: List,
    color: 'var(--gmp-primary)',
    path: '/workflow/todo',
  },
  {
    title: '未读消息',
    value: unreadCount.value,
    icon: Bell,
    color: 'var(--gmp-warning)',
    path: '/system/messages',
  },
  {
    title: '我的文件',
    value: recentDocs.value.length,
    icon: Files,
    color: 'var(--gmp-success)',
    path: '/dms/document',
  },
  {
    title: '运行中任务',
    value: runningTasks.value || '--',
    icon: Clock,
    color: 'var(--gmp-text-muted)',
    path: '/system/tasks',
  },
])

const displayName = computed(() => userStore.displayName || userStore.username || '用户')
const todayText = computed(() => dayjs().format('YYYY年MM月DD日'))
const recentTodoList = computed(() => workflowStore.todoList.slice(0, 5))

function go(path: string) {
  router.push(path)
}

function formatTime(t: string) {
  return t ? dayjs(t).format('MM-DD HH:mm') : '--'
}

onMounted(async () => {
  loading.value = true
  try {
    const results = await Promise.allSettled([
      workflowStore.fetchTodoCount(),
      workflowStore.fetchTodoList(1, 5),
      getUnreadCount().then(r => { unreadCount.value = r.data || 0 }),
      getDocumentPage({ pageNum: 1, pageSize: 3 }).then(r => { recentDocs.value = r.data?.records || [] }),
      import('@/api/system').then(m => m.getTaskList({ status: 1 })).then((r: any) => { runningTasks.value = (r.data || []).length }),
    ])
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="gmp-page dashboard-page">
    <section class="workbench-hero gmp-panel">
      <div class="hero-copy">
        <div class="hero-eyebrow">GMP WORKBENCH</div>
        <h1>{{ displayName }}，欢迎回来</h1>
        <p>{{ todayText }} · 关注待办、质量风险和关键业务入口</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="go('/workflow/todo')">
          <el-icon><List /></el-icon>
          处理待办
        </el-button>
        <el-button @click="go('/workflow/definition')">
          <el-icon><SetUp /></el-icon>
          发起流程
        </el-button>
      </div>
    </section>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in statCards" :key="card.title" :xs="24" :sm="12" :lg="6">
        <div class="stat-card gmp-card" @click="go(card.path)">
          <div>
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
          <div class="stat-icon" :style="{ color: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <section class="module-section gmp-card">
          <div class="gmp-card-header">
            <div>
              <h2 class="gmp-section-title">常用业务入口</h2>
              <p class="gmp-section-subtitle">按业务域聚合高频操作，减少在菜单中来回查找。</p>
            </div>
          </div>
          <div class="module-grid">
            <article v-for="mod in quickModules" :key="mod.title" class="module-card" :class="`tone-${mod.tone}`">
              <div class="module-head">
                <div class="module-icon">
                  <el-icon :size="24"><component :is="mod.icon" /></el-icon>
                </div>
                <div>
                  <h3>{{ mod.title }}</h3>
                  <p>{{ mod.desc }}</p>
                </div>
              </div>
              <div class="module-actions">
                <button
                  v-for="action in mod.actions"
                  :key="action.label"
                  type="button"
                  :class="{ primary: action.primary }"
                  @click="go(action.path)"
                >
                  {{ action.label }}
                </button>
              </div>
            </article>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :lg="8">
        <section class="risk-card gmp-card stable">
          <div class="gmp-card-header">
            <div>
              <h2 class="gmp-section-title">系统概览</h2>
            </div>
          </div>
          <div class="risk-status">
            <div>
              <strong>待办: {{ workflowStore.todoCount }}</strong>
              <span>可认领: {{ workflowStore.claimableCount }} · 运行中定时任务: {{ runningTasks }}</span>
            </div>
          </div>
          <el-button plain class="risk-button" @click="go('/workflow/todo')">处理待办</el-button>
        </section>

        <section class="todo-card gmp-card">
          <div class="gmp-card-header">
            <h2 class="gmp-section-title">最近待办</h2>
            <el-button link type="primary" @click="go('/workflow/todo')">全部</el-button>
          </div>
          <el-skeleton v-if="loading" :rows="4" animated />
          <div v-else-if="recentTodoList.length" class="todo-list">
            <div v-for="task in recentTodoList" :key="task.id" class="todo-item" @click="go(`/workflow/detail/${task.instanceId || task.id}`)">
              <div class="todo-main">
                <strong>{{ task.processTitle || task.name || '未命名流程' }}</strong>
                <span>{{ task.nodeName || task.activityName || '当前节点' }}</span>
              </div>
              <small>{{ formatTime(task.createTime || task.startTime) }}</small>
            </div>
          </div>
          <el-empty v-else description="暂无待办" :image-size="70" />
        </section>

        <section class="todo-card gmp-card">
          <div class="gmp-card-header">
            <h2 class="gmp-section-title">最近文件</h2>
            <el-button link type="primary" @click="go('/dms/document')">全部</el-button>
          </div>
          <el-skeleton v-if="loading" :rows="3" animated />
          <div v-else-if="recentDocs.length" class="todo-list">
            <div v-for="doc in recentDocs" :key="doc.id" class="todo-item" @click="go(`/dms/document/${doc.id}`)">
              <div class="todo-main">
                <strong>{{ doc.title }}</strong>
                <span>{{ doc.businessNo || '-' }}</span>
              </div>
              <small>{{ doc.initiatedAt ? formatTime(doc.initiatedAt) : '--' }}</small>
            </div>
          </div>
          <el-empty v-else description="暂无文件" :image-size="70" />
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-page {
  min-width: 0;
}

.workbench-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(31, 111, 235, 0.12), rgba(34, 197, 94, 0.08)),
    #fff;
}

.hero-eyebrow {
  color: var(--gmp-primary);
  font-size: 12px;
  font-weight: 800;
}

.hero-copy h1 {
  margin: 8px 0 6px;
  color: var(--gmp-text);
  font-size: 24px;
  font-weight: 800;
}

.hero-copy p {
  margin: 0;
  color: var(--gmp-text-muted);
  font-size: 14px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.stat-row {
  row-gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 118px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.stat-card:hover {
  box-shadow: var(--gmp-shadow);
  transform: translateY(-2px);
}

.stat-title {
  color: var(--gmp-text-muted);
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  color: var(--gmp-text);
  font-size: 32px;
  font-weight: 800;
}

.stat-icon {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  border-radius: 8px;
  background: #f8fafc;
}

.module-section,
.risk-card,
.todo-card {
  padding: 18px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.module-card {
  min-height: 154px;
  padding: 16px;
  border: 1px solid var(--gmp-border);
  border-radius: 8px;
  background: #fff;
}

.module-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.module-icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: #eef5ff;
  color: var(--gmp-primary);
}

.module-head h3 {
  margin: 0;
  color: var(--gmp-text);
  font-size: 16px;
}

.module-head p {
  margin: 4px 0 0;
  color: var(--gmp-text-muted);
  font-size: 12px;
}

.module-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.module-actions button {
  height: 28px;
  padding: 0 10px;
  border: 1px solid var(--gmp-border);
  border-radius: 6px;
  background: #fff;
  color: #475569;
  cursor: pointer;
  font-size: 12px;
}

.module-actions button:hover,
.module-actions button.primary {
  border-color: var(--gmp-primary);
  background: #eef5ff;
  color: var(--gmp-primary);
}

.tone-green .module-icon { background: #ecfdf3; color: var(--gmp-success); }
.tone-amber .module-icon { background: #fff7ed; color: var(--gmp-warning); }
.tone-red .module-icon { background: #fef2f2; color: var(--gmp-danger); }
.tone-indigo .module-icon { background: #eef2ff; color: #4f46e5; }
.tone-slate .module-icon { background: #f1f5f9; color: #334155; }

.risk-card {
  margin-bottom: 16px;
}

.risk-status {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 94px;
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
  color: var(--gmp-primary);
}

.risk-card.warning .risk-status {
  background: #fff7ed;
  color: var(--gmp-warning);
}

.risk-card.attention .risk-status {
  background: #eef5ff;
  color: var(--gmp-primary);
}

.risk-status strong,
.risk-status span {
  display: block;
}

.risk-status strong {
  color: var(--gmp-text);
  font-size: 16px;
}

.risk-status span {
  margin-top: 4px;
  color: var(--gmp-text-muted);
  font-size: 13px;
}

.risk-button {
  width: 100%;
  margin-top: 12px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 10px;
  border: 1px solid var(--gmp-border);
  border-radius: 8px;
  cursor: pointer;
}

.todo-item:hover {
  border-color: #bfdbfe;
  background: #f8fbff;
}

.todo-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.todo-main strong,
.todo-main span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-main strong {
  color: var(--gmp-text);
  font-size: 14px;
}

.todo-main span,
.todo-item small {
  color: var(--gmp-text-muted);
  font-size: 12px;
}

@media (max-width: 1200px) {
  .module-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .workbench-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-actions {
    justify-content: flex-start;
  }
}
</style>
