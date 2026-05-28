import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getTokenExpiresAt, removeToken } from '@/utils/auth'

/**
 * 路由配置 - GMP系统全部页面路由
 *
 * 路由守卫：
 * - 白名单路径（/login）无需登录
 * - 其他所有路径需要Token验证
 * - 动态路由可根据用户权限在后端加载
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/components/Layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台', icon: 'HomeFilled' } },

      // ========== 流程中心（运行时） ==========
      { path: 'workflow/todo', name: 'WorkflowTodo', component: () => import('@/views/workflow/todo/index.vue'), meta: { title: '我的待办', icon: 'List' } },
      { path: 'workflow/done', name: 'WorkflowDone', component: () => import('@/views/workflow/done/index.vue'), meta: { title: '我的已办', icon: 'Finished' } },
      { path: 'workflow/started', name: 'WorkflowStarted', component: () => import('@/views/workflow/started/index.vue'), meta: { title: '我的发起', icon: 'Promotion' } },
      { path: 'workflow/detail/:id', name: 'WorkflowDetail', component: () => import('@/views/workflow/detail/index.vue'), meta: { title: '流程详情', hidden: true } },

      // ========== 流程设计 ==========
      { path: 'workflow/definition', name: 'WorkflowDefinition', component: () => import('@/views/workflow/definition/index.vue'), meta: { title: '流程定义', icon: 'SetUp' } },
      { path: 'workflow/designer/:id?', name: 'WorkflowDesigner', component: () => import('@/views/workflow/designer/index.vue'), meta: { title: '流程设计器', hidden: true } },

      // ========== 表单管理 ==========
      { path: 'form/definition', name: 'FormDefinition', component: () => import('@/views/form/definition/index.vue'), meta: { title: '表单定义', icon: 'Document' } },
      { path: 'form/designer/:id?', name: 'FormDesigner', component: () => import('@/views/form/designer/index.vue'), meta: { title: '表单设计器', hidden: true } },
      { path: 'form/data', name: 'FormData', component: () => import('@/views/form/data/index.vue'), meta: { title: '表单数据', icon: 'Grid' } },
      { path: 'form/run/:formKey', name: 'FormRunner', component: () => import('@/views/common/FormRunner.vue'), meta: { title: '填写表单', hidden: true } },

      // ========== QMS 质量管理 ==========
      { path: 'qms/deviation', name: 'QmsDeviation', component: () => import('@/views/qms/index.vue'), meta: { title: '偏差管理', icon: 'Warning' } },
      { path: 'qms/capa', name: 'QmsCapa', component: () => import('@/views/qms/index.vue'), meta: { title: 'CAPA管理', icon: 'CircleCheck' } },
      { path: 'qms/change', name: 'QmsChange', component: () => import('@/views/qms/index.vue'), meta: { title: '变更控制', icon: 'Switch' } },
      { path: 'qms/audit', name: 'QmsAudit', component: () => import('@/views/qms/index.vue'), meta: { title: '审计管理', icon: 'Search' } },

      // ========== DMS 文件管理 ==========
      { path: 'dms/document', name: 'DmsDocument', component: () => import('@/views/dms/index.vue'), meta: { title: '文件管理', icon: 'Folder' } },
      { path: 'dms/document/:id', name: 'DmsDocumentDetail', component: () => import('@/views/dms/detail.vue'), meta: { title: '文件详情', hidden: true } },

      // ========== TMS 培训管理 ==========
      { path: 'tms/course', name: 'TmsCourse', component: () => import('@/views/tms/index.vue'), meta: { title: '培训管理', icon: 'School' } },
      { path: 'tms/plan', name: 'TmsPlan', component: () => import('@/views/tms/index.vue'), meta: { title: '培训计划', icon: 'Notebook' } },
      { path: 'tms/:type(course|plan)/:id', name: 'TmsDetail', component: () => import('@/views/tms/detail.vue'), meta: { title: '培训详情', hidden: true } },
      { path: 'tms/records', name: 'TmsRecords', component: () => import('@/views/tms/records.vue'), meta: { title: '培训记录', icon: 'Tickets' } },

      // ========== QRS 质量回顾 ==========
      { path: 'qrs/apqr', name: 'QrsApqr', component: () => import('@/views/qrs/index.vue'), meta: { title: '质量回顾', icon: 'DataAnalysis' } },

      // ========== 系统管理 ==========
      { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'system/user/:id', name: 'SystemUserDetail', component: () => import('@/views/system/user/detail.vue'), meta: { title: '用户详情', hidden: true } },
      { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理', icon: 'Avatar' } },
      { path: 'system/menu', name: 'SystemMenu', component: () => import('@/views/system/menu/index.vue'), meta: { title: '菜单管理', icon: 'Menu' } },
      { path: 'system/dept', name: 'SystemDept', component: () => import('@/views/system/dept/index.vue'), meta: { title: '部门管理', icon: 'OfficeBuilding' } },
      { path: 'system/dict', name: 'SystemDict', component: () => import('@/views/system/dict/index.vue'), meta: { title: '字典管理', icon: 'Collection' } },
      { path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志', icon: 'Tickets' } },
      { path: 'system/settings', name: 'SystemSettings', component: () => import('@/views/system/settings/index.vue'), meta: { title: '系统设置', icon: 'Timer' } },
      { path: 'system/messages', name: 'SystemMessages', component: () => import('@/views/system/messages.vue'), meta: { title: '消息中心', icon: 'Bell' } },
      { path: 'system/tasks', name: 'SystemTasks', component: () => import('@/views/system/tasks.vue'), meta: { title: '定时任务', icon: 'Clock' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 未登录跳转登录页
// 前端路由仅控制页面可见性，真正的权限校验在后端 @PreAuthorize
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || 'GMP系统'
  if (to.meta.noAuth) {
    next()
  } else {
    const token = getToken()
    const expiresAt = getTokenExpiresAt()
    const isValid = token && expiresAt && Date.now() < expiresAt
    if (isValid) {
      next()
    } else {
      // token 不存在或已过期，清理残留并跳转登录页
      if (token) removeToken()
      next('/login')
    }
  }
})

export default router
