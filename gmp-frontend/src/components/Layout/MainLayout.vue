<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorkflowStore } from '@/stores/workflow'
import { useActivityMonitor } from '@/composables/useActivityMonitor'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const workflowStore = useWorkflowStore()

useActivityMonitor()

const isCollapse = ref(true)

interface MenuItem {
  index: string
  title: string
  icon: string
  badgeKey?: string
}

interface MenuGroup {
  title: string
  icon: string
  children: MenuItem[]
}

const menuGroups: (MenuItem | MenuGroup)[] = [
  {
    index: 'dashboard',
    title: '工作台',
    icon: 'HomeFilled',
  },
  {
    title: '流程管理',
    icon: 'Promotion',
    children: [
      { index: 'workflow/todo', title: '我的待办', icon: 'List', badgeKey: 'todo' },
      { index: 'workflow/done', title: '我的已办', icon: 'Finished' },
      { index: 'workflow/started', title: '我的发起', icon: 'Promotion' },
      { index: 'workflow/definition', title: '流程定义', icon: 'SetUp' },
    ],
  },
  {
    title: '表单管理',
    icon: 'Document',
    children: [
      { index: 'form/definition', title: '表单定义', icon: 'Document' },
      { index: 'form/data', title: '表单数据', icon: 'Grid' },
    ],
  },
  {
    title: '质量管理',
    icon: 'Monitor',
    children: [
      { index: 'qms/deviation', title: '偏差管理', icon: 'Warning' },
      { index: 'qms/capa', title: 'CAPA管理', icon: 'CircleCheck' },
      { index: 'qms/change', title: '变更控制', icon: 'Switch' },
      { index: 'qms/audit', title: '审计管理', icon: 'Search' },
      { index: 'qrs/apqr', title: '质量回顾', icon: 'DataAnalysis' },
    ],
  },
  {
    title: '文件管理',
    icon: 'Folder',
    children: [
      { index: 'dms/document', title: '文件管理', icon: 'Folder' },
    ],
  },
  {
    title: '培训管理',
    icon: 'School',
    children: [
      { index: 'tms/course', title: '培训管理', icon: 'School' },
    ],
  },
  {
    title: '系统管理',
    icon: 'Setting',
    children: [
      { index: 'system/user', title: '用户管理', icon: 'User' },
      { index: 'system/role', title: '角色管理', icon: 'Avatar' },
      { index: 'system/menu', title: '菜单管理', icon: 'Menu' },
      { index: 'system/dept', title: '部门管理', icon: 'OfficeBuilding' },
      { index: 'system/dict', title: '字典管理', icon: 'Collection' },
      { index: 'system/log', title: '操作日志', icon: 'Tickets' },
      { index: 'system/settings', title: '系统设置', icon: 'Timer' },
    ],
  },
]

const activeMenu = computed(() => '/' + route.path.replace(/^\//, ''))

const activeGroup = computed(() => {
  const p = route.path.replace(/^\//, '')
  for (const entry of menuGroups) {
    if ('children' in entry && entry.children.some(c => p.startsWith(c.index))) {
      return entry
    }
  }
  return null
})

function handleMenuSelect(index: string) {
  router.push('/' + index)
}

function handleGroupClick(entry: MenuItem | MenuGroup) {
  if ('children' in entry && entry.children.length > 0) {
    router.push('/' + entry.children[0].index)
  }
}

function handleLogout() {
  ElMessageBox.confirm(
    '<p style="margin:0;font-size:15px;color:#303133;">确定要退出登录吗？</p>',
    '退出确认',
    {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'logout-confirm-box',
      confirmButtonClass: 'logout-confirm-btn',
      dangerouslyUseHTMLString: true,
    }
  ).then(() => {
    userStore.logoutAction()
  }).catch(() => {})
}

onMounted(() => {
  workflowStore.fetchTodoCount()
})
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="layout-aside">
      <div class="logo" @click="isCollapse = !isCollapse">
        <el-icon :size="22"><Setting /></el-icon>
        <span v-show="!isCollapse" class="logo-text">GMP 质量平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
      >
        <template v-for="entry in menuGroups" :key="entry.title || entry.index">
          <el-sub-menu v-if="'children' in entry" :index="entry.title">
            <template #title>
              <div class="menu-group-title" @click="handleGroupClick(entry)">
                <el-icon><component :is="entry.icon" /></el-icon>
                <span v-show="!isCollapse" class="menu-group-text">{{ entry.title }}</span>
              </div>
            </template>
            <el-menu-item
              v-for="child in entry.children"
              :key="child.index"
              :index="child.index"
            >
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
              <el-badge
                v-if="child.badgeKey === 'todo' && workflowStore.todoCount > 0"
                :value="workflowStore.todoCount"
                class="menu-badge"
              />
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="entry.index">
            <el-icon><component :is="entry.icon" /></el-icon>
            <span>{{ entry.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-button class="menu-toggle" text @click="isCollapse = !isCollapse">
            <el-icon :size="22">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="activeGroup">{{ activeGroup.title }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="workflowStore.todoCount" :hidden="!workflowStore.todoCount" class="header-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click">
            <span class="user-info">
              {{ userStore.displayName || userStore.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 横向子菜单 -->
      <div v-if="activeGroup" class="sub-nav">
        <div class="sub-nav-inner">
          <span class="sub-nav-label" @click="handleGroupClick(activeGroup!)">{{ activeGroup.title }}</span>
          <div class="sub-nav-divider" />
          <div
            v-for="child in activeGroup.children"
            :key="child.index"
            class="sub-nav-item"
            :class="{ active: activeMenu === '/' + child.index }"
            @click="handleMenuSelect(child.index)"
          >
            <span>{{ child.title }}</span>
          </div>
        </div>
      </div>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside { background-color: #304156; overflow: hidden; transition: width 0.28s; }
.logo { display: flex; align-items: center; gap: 12px; height: 56px; padding: 0 20px; color: #fff; font-size: 17px; font-weight: bold; cursor: pointer; white-space: nowrap; }
.logo-text { overflow: hidden; }

/* Header */
.layout-header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06); height: 56px; flex-shrink: 0; padding: 0 20px; z-index: 1; }
.header-left { display: flex; align-items: center; gap: 12px; }
.menu-toggle { padding: 4px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.header-badge { cursor: pointer; }
.user-info { display: flex; align-items: center; gap: 4px; color: #303133; cursor: pointer; font-size: 14px; }
.user-info:hover { color: #409EFF; }

/* Sub-nav */
.sub-nav { background: #fff; border-bottom: 1px solid #e8eaed; }
.sub-nav-inner { display: flex; align-items: center; height: 48px; padding: 0 20px; }
.sub-nav-label { font-size: 15px; font-weight: 600; color: #303133; white-space: nowrap; cursor: pointer; }
.sub-nav-label:hover { color: #409EFF; }
.sub-nav-divider { width: 1px; height: 20px; background: #dcdfe6; margin: 0 20px; }
.sub-nav-item { position: relative; display: flex; align-items: center; height: 100%; padding: 0 16px; font-size: 14px; color: #606266; cursor: pointer; transition: all 0.2s; user-select: none; }
.sub-nav-item:hover { color: #303133; background: #f5f7fa; }
.sub-nav-item.active { color: #409EFF; }
.sub-nav-item.active::after { content: ''; position: absolute; bottom: 0; left: 12px; right: 12px; height: 3px; background: #409EFF; border-radius: 3px 3px 0 0; }

/* Main */
.layout-main { background: #f0f2f5; padding: 20px; }

.menu-badge { margin-left: 8px; }

:deep(.el-menu) { border-right: none; }
:deep(.el-sub-menu .el-menu) { background-color: #263445 !important; }
:deep(.el-sub-menu__title) { color: #bfcbd9; }
:deep(.el-sub-menu__title:hover) { color: #fff; }
:deep(.menu-group-title) { display: flex; align-items: center; gap: 12px; cursor: pointer; }
:deep(.el-menu--collapse) { width: 64px; }
:deep(.el-menu--collapse .el-sub-menu__title) { padding: 0 20px !important; }
:deep(.el-breadcrumb) { font-size: 14px; }
:deep(.el-breadcrumb__inner) { font-weight: 400; }
</style>
