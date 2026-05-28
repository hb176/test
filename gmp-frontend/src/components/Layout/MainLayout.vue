<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWorkflowStore } from '@/stores/workflow'
import { useActivityMonitor } from '@/composables/useActivityMonitor'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const workflowStore = useWorkflowStore()

useActivityMonitor()

const isCollapse = ref(true)
const searchVisible = ref(false)
const searchKeyword = ref('')

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

const defaultMenuGroups: (MenuItem | MenuGroup)[] = [
  { index: 'dashboard', title: '工作台', icon: 'HomeFilled' },
]

const menuGroups = computed<(MenuItem | MenuGroup)[]>(() => {
  const menus = userStore.menus
  if (!menus || menus.length === 0) return defaultMenuGroups

  const visible = menus.filter((m: any) => m.menuType !== 2)
  if (visible.length === 0) return defaultMenuGroups

  const childrenMap = new Map<number, any[]>()
  for (const m of visible) {
    const pid = m.parentId || 0
    if (!childrenMap.has(pid)) childrenMap.set(pid, [])
    childrenMap.get(pid)!.push(m)
  }

  function buildTree(parentId: number): MenuItem[] {
    const items = childrenMap.get(parentId) || []
    return items.map(m => ({
      index: m.formKey ? `/form/run/${m.formKey}` : (m.path || ''),
      title: m.menuName,
      icon: m.icon || 'Document',
    }))
  }

  const roots = childrenMap.get(0) || []
  const result: (MenuItem | MenuGroup)[] = []

  for (const root of roots) {
    const children = buildTree(root.id)
    if (children.length > 0) {
      result.push({
        title: root.menuName,
        icon: root.icon || 'Setting',
        children,
      })
    } else if (root.path) {
      result.push({
        index: root.path,
        title: root.menuName,
        icon: root.icon || 'Document',
      })
    }
  }

  return result.length > 0 ? result : defaultMenuGroups
})

const activeMenu = computed(() => '/' + route.path.replace(/^\//, ''))

const activeGroup = computed(() => {
  const p = route.path.replace(/^\//, '')
  for (const entry of menuGroups.value) {
    if ('children' in entry && entry.children.some(c => p.startsWith(c.index.replace(/^\//, '')))) {
      return entry
    }
  }
  return null
})

const flatMenuItems = computed(() => {
  const items: Array<MenuItem & { groupTitle?: string }> = []
  for (const entry of menuGroups.value) {
    if ('children' in entry) {
      entry.children.forEach(child => items.push({ ...child, groupTitle: entry.title }))
    } else {
      items.push(entry)
    }
  }
  return items.filter(item => item.index)
})

const filteredMenuItems = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return flatMenuItems.value.slice(0, 8)
  return flatMenuItems.value.filter(item => {
    return item.title.toLowerCase().includes(keyword) || item.index.toLowerCase().includes(keyword)
  }).slice(0, 10)
})

function handleMenuSelect(index: string) {
  router.push(index.startsWith('/') ? index : '/' + index)
}

function handleGroupClick(entry: MenuItem | MenuGroup) {
  if ('children' in entry && entry.children.length > 0) {
    handleMenuSelect(entry.children[0].index)
  }
}

function handleSearchSelect(item: MenuItem) {
  searchVisible.value = false
  searchKeyword.value = ''
  handleMenuSelect(item.index)
}

function handleTodoClick() {
  router.push('/workflow/todo')
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

onMounted(async () => {
  if (userStore.isLoggedIn && userStore.menus.length === 0) {
    await userStore.fetchUserInfo()
  }
  workflowStore.fetchTodoCount()
})
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '68px' : '248px'" class="layout-aside">
      <div class="logo" @click="isCollapse = !isCollapse">
        <div class="logo-mark">
          <el-icon :size="22"><Setting /></el-icon>
        </div>
        <div v-show="!isCollapse" class="logo-copy">
          <span class="logo-text">GMP 质量平台</span>
          <span class="logo-subtitle">Quality Suite</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        @select="handleMenuSelect"
      >
        <template v-for="entry in menuGroups" :key="'children' in entry ? entry.title : entry.index">
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

    <el-container class="layout-body">
      <el-header class="layout-header">
        <div class="header-left">
          <el-button class="menu-toggle" text @click="isCollapse = !isCollapse">
            <el-icon :size="22">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div class="header-title-block">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="activeGroup">{{ activeGroup.title }}</el-breadcrumb-item>
              <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
            <span class="header-route-title">{{ route.meta.title || '工作台' }}</span>
          </div>
        </div>

        <div class="header-right">
          <el-popover
            v-model:visible="searchVisible"
            placement="bottom-end"
            width="360"
            trigger="click"
            popper-class="layout-search-popover"
          >
            <template #reference>
              <el-button class="header-action" text>
                <el-icon><Search /></el-icon>
                <span>菜单搜索</span>
              </el-button>
            </template>
            <div class="search-panel">
              <el-input v-model="searchKeyword" placeholder="搜索菜单或路径" clearable autofocus>
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <div class="search-results">
                <div
                  v-for="item in filteredMenuItems"
                  :key="item.index"
                  class="search-result"
                  @click="handleSearchSelect(item)"
                >
                  <el-icon><component :is="item.icon" /></el-icon>
                  <div class="search-result-copy">
                    <span>{{ item.title }}</span>
                    <small>{{ item.groupTitle || item.index }}</small>
                  </div>
                </div>
                <el-empty v-if="filteredMenuItems.length === 0" description="未找到菜单" :image-size="56" />
              </div>
            </div>
          </el-popover>

          <el-button class="header-action todo-action" text @click="handleTodoClick">
            <el-badge :value="workflowStore.todoCount" :hidden="!workflowStore.todoCount" class="header-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
            <span>待办</span>
          </el-button>

          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="30">{{ (userStore.displayName || userStore.username || 'U').slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ userStore.displayName || userStore.username }}</span>
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

      <div v-if="activeGroup" class="sub-nav">
        <div class="sub-nav-inner">
          <span class="sub-nav-label" @click="handleGroupClick(activeGroup!)">{{ activeGroup.title }}</span>
          <div class="sub-nav-divider" />
          <div
            v-for="child in activeGroup.children"
            :key="child.index"
            class="sub-nav-item"
            :class="{ active: activeMenu === (child.index.startsWith('/') ? child.index : '/' + child.index) }"
            @click="handleMenuSelect(child.index)"
          >
            <el-icon><component :is="child.icon" /></el-icon>
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
.layout-container {
  height: 100vh;
  background: var(--gmp-bg);
}

.layout-aside {
  overflow: hidden;
  background: #14233a;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width 0.24s ease;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 16px;
  color: #fff;
  cursor: pointer;
  white-space: nowrap;
}

.logo-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: linear-gradient(135deg, #1f6feb 0%, #22c55e 100%);
}

.logo-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.logo-text {
  overflow: hidden;
  color: #f8fafc;
  font-size: 16px;
  font-weight: 700;
  text-overflow: ellipsis;
}

.logo-subtitle {
  margin-top: 2px;
  color: #94a3b8;
  font-size: 11px;
}

.layout-body {
  min-width: 0;
}

.layout-header {
  display: flex;
  z-index: 2;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  flex-shrink: 0;
  padding: 0 20px;
  border-bottom: 1px solid var(--gmp-border);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 1px 6px rgba(15, 23, 42, 0.05);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.menu-toggle {
  width: 34px;
  height: 34px;
  padding: 0;
}

.header-title-block {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.header-route-title {
  color: var(--gmp-text);
  font-size: 15px;
  font-weight: 700;
}

.header-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 10px;
  color: #475569;
  border-radius: 6px;
}

.header-action:hover {
  color: var(--gmp-primary);
  background: #eef5ff;
}

.todo-action {
  padding-right: 12px;
}

.header-badge {
  line-height: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--gmp-text);
  cursor: pointer;
  font-size: 14px;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-results {
  max-height: 320px;
  overflow: auto;
}

.search-result {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.search-result:hover {
  background: #f1f6ff;
}

.search-result-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.search-result-copy span {
  color: var(--gmp-text);
  font-size: 14px;
  font-weight: 600;
}

.search-result-copy small {
  overflow: hidden;
  color: var(--gmp-text-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-nav {
  background: #fff;
  border-bottom: 1px solid var(--gmp-border);
}

.sub-nav-inner {
  display: flex;
  align-items: center;
  height: 46px;
  padding: 0 20px;
  overflow-x: auto;
}

.sub-nav-label {
  color: var(--gmp-text);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
}

.sub-nav-label:hover {
  color: var(--gmp-primary);
}

.sub-nav-divider {
  width: 1px;
  height: 18px;
  flex-shrink: 0;
  margin: 0 16px;
  background: var(--gmp-border-strong);
}

.sub-nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  height: 100%;
  padding: 0 14px;
  color: #64748b;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}

.sub-nav-item:hover {
  color: var(--gmp-text);
  background: #f8fafc;
}

.sub-nav-item.active {
  color: var(--gmp-primary);
  font-weight: 700;
}

.sub-nav-item.active::after {
  position: absolute;
  right: 12px;
  bottom: 0;
  left: 12px;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: var(--gmp-primary);
  content: '';
}

.layout-main {
  min-width: 0;
  background:
    radial-gradient(circle at top right, rgba(31, 111, 235, 0.08), transparent 32%),
    var(--gmp-bg);
  padding: 20px;
  overflow: auto;
}

.menu-badge {
  margin-left: 8px;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 44px;
  margin: 3px 10px;
  border-radius: 8px;
  line-height: 44px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1f6feb 0%, #174ea6 100%) !important;
  box-shadow: 0 8px 20px rgba(31, 111, 235, 0.24);
}

:deep(.el-sub-menu .el-menu) {
  background: rgba(15, 23, 42, 0.28) !important;
}

:deep(.menu-group-title) {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  cursor: pointer;
}

:deep(.el-menu--collapse) {
  width: 68px;
}

:deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 18px !important;
}

:deep(.el-breadcrumb) {
  font-size: 12px;
}

:deep(.el-breadcrumb__inner) {
  color: #64748b;
  font-weight: 400;
}

@media (max-width: 900px) {
  .layout-header {
    gap: 10px;
    padding: 0 12px;
  }

  .header-action span,
  .user-name,
  .header-title-block :deep(.el-breadcrumb) {
    display: none;
  }

  .layout-main {
    padding: 12px;
  }
}
</style>
