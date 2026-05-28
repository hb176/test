<script setup lang="ts">
/**
 * RoleTagInput — 角色选择器
 *
 * 弹窗式角色选择，左侧部门树，右侧角色列表
 * 支持多选、搜索、分页
 */
import { ref, watch, onMounted } from 'vue'
import { getOrgTree, getRolePagerModel } from '@/api/bpmn'
import request from '@/utils/request'

const props = defineProps<{
  modelValue?: string
  multiple?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// ==================== 状态 ====================

const expressionMode = ref(false)
const expressionValue = ref('')
const visible = ref(false)
const loading = ref(false)
const keyword = ref('')
const selectedRoles = ref<any[]>([])
const roleList = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const deptTree = ref<any[]>([])
const selectedDeptId = ref<string>('')

// ==================== 初始化 ====================

watch(() => props.modelValue, (val) => {
  if (val && (val.startsWith('${') || val.startsWith('#{'))) {
    expressionMode.value = true
    expressionValue.value = val
    selectedRoles.value = []
  } else {
    expressionMode.value = false
    expressionValue.value = ''
    if (val) {
      const codes = val.split(',').filter(Boolean)
      selectedRoles.value = codes.map(code => ({ sn: code, name: code }))
      // 异步获取真实角色名称
      loadRoleNames(codes)
    } else {
      selectedRoles.value = []
    }
  }
}, { immediate: true })

onMounted(() => {
  loadDeptTree()
})

// ==================== 角色名称回显 ====================

async function loadRoleNames(codes: string[]) {
  try {
    const res = await request.get('/system/role/list')
    if (res.data) {
      const roleMap = new Map<string, string>()
      for (const r of res.data) {
        roleMap.set(r.roleCode, r.roleName)
      }
      selectedRoles.value = codes.map(code => ({
        sn: code,
        name: roleMap.get(code) || code
      }))
    }
  } catch {
    // 保持显示编码
  }
}

// ==================== 部门树 ====================

async function loadDeptTree() {
  try {
    const res = await getOrgTree()
    // bpmnRequest 返回 res 直接，data 在 res.data 中
    if (res.data) {
      deptTree.value = buildTree(res.data)
    }
  } catch (err) {
    console.warn('加载部门树失败:', err)
  }
}

function buildTree(list: any[]): any[] {
  const map = new Map()
  const roots: any[] = []
  list.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })
  list.forEach(item => {
    const node = map.get(item.id)
    if (item.pid && map.has(item.pid)) {
      map.get(item.pid).children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function handleDeptClick(dept: any) {
  selectedDeptId.value = dept.id
  pageNum.value = 1
  loadRoles()
}

// ==================== 角色列表 ====================

async function loadRoles() {
  loading.value = true
  try {
    const res = await getRolePagerModel({
      query: { pageNum: pageNum.value, pageSize: pageSize.value },
      entity: { keyword: keyword.value, deptId: selectedDeptId.value }
    })
    // bpmnRequest 返回 res 直接，data 在 res.data 中
    if (res.data) {
      roleList.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.warn('加载角色列表失败:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRoles()
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadRoles()
}

// ==================== 选择 ====================

function handleSelect(role: any) {
  const roleSn = String(role.sn || role.roleCode || '')
  const roleName = role.name || role.roleName || roleSn
  if (props.multiple !== false) {
    const idx = selectedRoles.value.findIndex(r => String(r.sn) === roleSn)
    if (idx >= 0) {
      selectedRoles.value.splice(idx, 1)
    } else {
      selectedRoles.value.push({ sn: roleSn, name: roleName })
    }
  } else {
    selectedRoles.value = [{ sn: roleSn, name: roleName }]
  }
}

function isSelected(role: any) {
  return selectedRoles.value.some(r => String(r.sn) === String(role.sn || role.roleCode))
}

function handleRemove(role: any) {
  selectedRoles.value = selectedRoles.value.filter(r => r.sn !== role.sn)
}

function handleConfirm() {
  const value = selectedRoles.value.map(r => r.sn).join(',')
  emit('update:modelValue', value)
  visible.value = false
}

function handleOpen() {
  visible.value = true
  loadRoles()
}
</script>

<template>
  <div class="role-tag-input">
    <!-- 模式切换 -->
    <div class="mode-toggle">
      <el-tooltip :content="expressionMode ? '切换到选择模式' : '切换到表达式模式'">
        <el-button size="small" :type="expressionMode ? 'warning' : 'info'" link @click="expressionMode = !expressionMode">
          {{ expressionMode ? '{}' : '...' }}
        </el-button>
      </el-tooltip>
    </div>

    <!-- 表达式输入模式 -->
    <div v-if="expressionMode" class="expression-input">
      <el-input size="small" v-model="expressionValue" placeholder="${candidateGroup}" @change="$emit('update:modelValue', expressionValue)" />
    </div>

    <!-- 选择模式触发区域 -->
    <div v-else class="trigger" @click="handleOpen">
      <el-tag
        v-for="role in selectedRoles"
        :key="role.sn"
        closable
        size="small"
        type="warning"
        @close="handleRemove(role)"
      >
        {{ role.name || role.sn }}
      </el-tag>
      <span v-if="selectedRoles.length === 0" class="placeholder">
        {{ props.placeholder || '点击选择角色' }}
      </span>
    </div>

    <!-- 选择弹窗 -->
    <el-dialog
      v-model="visible"
      title="角色选择"
      width="700px"
      top="5vh"
      @open="handleOpen"
    >
      <div class="dialog-body">
        <!-- 左侧部门树 -->
        <div class="dept-tree">
          <div class="tree-header">组织架构</div>
          <el-tree
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleDeptClick"
          />
        </div>

        <!-- 右侧角色列表 -->
        <div class="role-list">
          <div class="list-header">
            <el-input
              v-model="keyword"
              placeholder="搜索角色"
              clearable
              size="small"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <el-table
            :data="roleList"
            v-loading="loading"
            size="small"
            height="350"
            @row-click="handleSelect"
            row-class-name="clickable-row"
          >
            <el-table-column width="40">
              <template #default="{ row }">
                <el-icon v-if="isSelected(row)" color="#409eff"><Check /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="角色名称" width="150" />
            <el-table-column prop="sn" label="角色编码" width="150" />
          </el-table>

          <div class="list-footer">
            <el-pagination
              small
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="pageNum"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="selected-info">
            已选 {{ selectedRoles.length }} 个角色
          </div>
          <div>
            <el-button @click="visible = false">取消</el-button>
            <el-button type="primary" @click="handleConfirm">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Check } from '@element-plus/icons-vue'
</script>

<style scoped>
.role-tag-input {
  width: 100%;
}

.mode-toggle {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 4px;
}

.expression-input {
  width: 100%;
}

.trigger {
  min-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  transition: border-color 0.2s;
}

.trigger:hover {
  border-color: #409eff;
}

.placeholder {
  color: #c0c4cc;
  font-size: 13px;
}

.dialog-body {
  display: flex;
  gap: 16px;
  height: 420px;
}

.dept-tree {
  width: 200px;
  flex-shrink: 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: auto;
}

.tree-header {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.role-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.list-header {
  padding: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.list-footer {
  padding: 8px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e4e7ed;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  font-size: 13px;
  color: #606266;
}

:deep(.clickable-row) {
  cursor: pointer;
}
</style>
