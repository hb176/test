<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  addDept,
  addUser,
  deleteDept,
  getDeptTree,
  getRoleList,
  getUserPage,
  updateDept,
  updateUser,
} from '@/api/system'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const selectedDeptId = ref<number | undefined>()
const deptTree = ref<any[]>([])
const flatDepts = ref<any[]>([])
const roleList = ref<any[]>([])
const selectedDeptIds = ref<number[]>([])
const selectedRoleIds = ref<number[]>([])
const dialogVisible = ref(false)
const deptDialogVisible = ref(false)
const deptEditVisible = ref(false)

const editing = reactive<any>({
  id: null,
  userId: '',
  userName: '',
  password: '',
  deptId: null,
  deptName: '',
  phone: '',
  mail: '',
  status: 'NORM',
})

const newDept = reactive({
  id: null as number | null,
  deptName: '',
  parentId: 0,
  deptCode: '',
  sortOrder: 0,
  leader: '',
  status: 1,
})

function buildDeptTree(items: any[]): any[] {
  return items
    .filter(d => d.parentId === 0)
    .map(d => ({ ...d, children: buildChildren(items, d.id) }))
}

function buildChildren(items: any[], pid: number): any[] {
  return items
    .filter(d => d.parentId === pid)
    .map(d => ({ ...d, children: buildChildren(items, d.id) }))
}

function resetDeptForm(parentId = 0) {
  Object.assign(newDept, {
    id: null,
    deptName: '',
    parentId,
    deptCode: '',
    sortOrder: 0,
    leader: '',
    status: 1,
  })
}

async function fetchDept() {
  const r = await getDeptTree()
  flatDepts.value = r.data || []
  deptTree.value = buildDeptTree(flatDepts.value)
}

async function fetchData() {
  loading.value = true
  try {
    const r = await getUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      deptId: selectedDeptId.value,
      status: statusFilter.value || undefined,
    })
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } finally {
    loading.value = false
  }
}

function selectDept(dept: any) {
  selectedDeptId.value = selectedDeptId.value === dept.id ? undefined : dept.id
  pageNum.value = 1
  fetchData()
}

function openAdd() {
  Object.assign(editing, {
    id: null,
    userId: '',
    userName: '',
    password: '',
    deptId: null,
    deptName: '',
    phone: '',
    mail: '',
    status: 'NORM',
  })
  selectedDeptIds.value = []
  selectedRoleIds.value = []
  dialogVisible.value = true
}

async function openEdit(row: any) {
  Object.assign(editing, row)
  editing.password = ''
  selectedDeptIds.value = row.deptIds
    ? row.deptIds.split(',').map(Number).filter(Boolean)
    : (row.deptId ? [row.deptId] : [])
  selectedRoleIds.value = []
  dialogVisible.value = true
  try {
    const r = await request.get(`/system/user/${row.id}/roles`)
    selectedRoleIds.value = r.data || []
  } catch (e) {
    selectedRoleIds.value = []
  }
}

async function save() {
  const firstDept = flatDepts.value.find((d: any) => d.id === selectedDeptIds.value[0])
  let deptName = firstDept?.deptName || ''
  if (selectedDeptIds.value.length > 1 && firstDept) {
    let cur = firstDept
    while (cur.parentId && cur.parentId > 0) {
      const p = flatDepts.value.find((d: any) => d.id === cur.parentId)
      if (!p) break
      cur = p
      deptName = cur.deptName
    }
  }

  const payload = {
    ...editing,
    deptId: selectedDeptIds.value[0] || null,
    deptName,
    deptIds: selectedDeptIds.value.join(','),
  }

  if (editing.id) {
    await updateUser(editing.id, payload)
    await request.put(`/system/user/${editing.id}/roles`, selectedRoleIds.value)
  } else {
    const r = await addUser(payload)
    const newId = (r as any)?.data?.id || (r as any)?.id
    if (newId && selectedRoleIds.value.length) {
      await request.put(`/system/user/${newId}/roles`, selectedRoleIds.value)
    }
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

async function toggleStatus(row: any) {
  const ns = row.status === 'NORM' ? 'DISABLED' : 'NORM'
  await ElMessageBox.confirm(`确定${ns === 'DISABLED' ? '禁用' : '启用'}该用户吗？`, '提示', { type: 'warning' })
  await request.put(`/system/user/${row.id}/status`, { status: ns })
  ElMessage.success('操作成功')
  fetchData()
}

function goDetail(row: any) {
  router.push(`/system/user/${row.id}`)
}

function openDeptAdd(parentId = 0) {
  resetDeptForm(parentId)
  deptDialogVisible.value = true
}

function openDeptEdit(dept: any) {
  Object.assign(newDept, dept)
  deptEditVisible.value = true
}

async function saveDept() {
  await addDept(newDept)
  ElMessage.success('部门已新增')
  deptDialogVisible.value = false
  fetchDept()
}

async function saveDeptEdit() {
  await updateDept(newDept.id!, newDept)
  ElMessage.success('已更新')
  deptEditVisible.value = false
  fetchDept()
}

async function toggleDept(dept: any) {
  await updateDept(dept.id, { ...dept, status: dept.status === 1 ? 0 : 1 })
  ElMessage.success('操作成功')
  fetchDept()
}

async function removeDept(dept: any) {
  await ElMessageBox.confirm(`确定删除部门「${dept.deptName}」吗？`, '删除确认', { type: 'warning' })
  await deleteDept(dept.id)
  ElMessage.success('删除成功')
  fetchDept()
}

onMounted(async () => {
  await Promise.all([fetchDept(), fetchData()])
  const r = await getRoleList()
  roleList.value = r.data || []
})
</script>

<template>
  <div class="gmp-page user-page">
    <section class="user-hero gmp-panel">
      <div>
        <h1>用户管理</h1>
        <p>维护账号、所属部门、角色授权和启用状态。</p>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </section>

    <div class="user-content">
      <aside class="dept-panel gmp-card">
        <div class="gmp-card-header">
          <div>
            <h2 class="gmp-section-title">部门结构</h2>
            <p class="gmp-section-subtitle">点击部门筛选用户。</p>
          </div>
          <el-button link type="primary" @click="openDeptAdd()">新增</el-button>
        </div>

        <div class="dept-tree-wrap">
          <template v-for="d in deptTree" :key="d.id">
            <div class="dept-node" :class="{ active: selectedDeptId === d.id }" @click="selectDept(d)">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="dept-name" :class="{ disabled: d.status !== 1 }">{{ d.deptName }}</span>
              <span v-if="d.status !== 1" class="dept-disabled-tag">禁用</span>
              <span class="dept-count">{{ d.children?.length || 0 }}</span>
              <div class="dept-actions" @click.stop>
                <el-button link size="small" @click="openDeptEdit(d)"><el-icon><Edit /></el-icon></el-button>
                <el-button link size="small" :type="d.status === 1 ? 'danger' : 'success'" @click="toggleDept(d)">
                  {{ d.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button link size="small" type="danger" @click="removeDept(d)">删除</el-button>
              </div>
            </div>

            <div v-if="d.children?.length" class="dept-children">
              <div
                v-for="c in d.children"
                :key="c.id"
                class="dept-node sub"
                :class="{ active: selectedDeptId === c.id }"
                @click="selectDept(c)"
              >
                <el-icon><OfficeBuilding /></el-icon>
                <span class="dept-name" :class="{ disabled: c.status !== 1 }">{{ c.deptName }}</span>
                <div class="dept-actions" @click.stop>
                  <el-button link size="small" @click="openDeptEdit(c)"><el-icon><Edit /></el-icon></el-button>
                  <el-button link size="small" :type="c.status === 1 ? 'danger' : 'success'" @click="toggleDept(c)">
                    {{ c.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button link size="small" type="danger" @click="removeDept(c)">删除</el-button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </aside>

      <section class="user-table-wrap">
        <el-card class="gmp-table-card">
          <div class="gmp-toolbar">
            <div>
              <h2 class="gmp-section-title">用户列表</h2>
              <p class="gmp-section-subtitle">支持按部门、账号、姓名和状态筛选。</p>
            </div>
            <div class="gmp-filter-row">
              <el-input v-model="keyword" placeholder="用户姓名 / 登录账号" clearable style="width:220px" @keyup.enter="fetchData" />
              <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px" @change="fetchData">
                <el-option label="正常" value="NORM" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
              <el-button @click="fetchData">查询</el-button>
              <el-button type="primary" @click="openAdd">新增用户</el-button>
            </div>
          </div>

          <el-table :data="list" v-loading="loading" stripe @row-click="goDetail">
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="userId" label="登录账号" width="130" />
            <el-table-column prop="userName" label="用户姓名" min-width="130" show-overflow-tooltip />
            <el-table-column prop="deptName" label="部门" min-width="140" show-overflow-tooltip />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="mail" label="邮箱" min-width="170" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'NORM' ? 'success' : 'danger'" size="small">
                  <span class="gmp-status-dot" />
                  {{ row.status === 'NORM' ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="goDetail(row)">详情</el-button>
                <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
                <el-button link :type="row.status === 'NORM' ? 'danger' : 'success'" @click.stop="toggleStatus(row)">
                  {{ row.status === 'NORM' ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-pagination">
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              layout="total,sizes,prev,pager,next"
              :page-sizes="[10,20,50]"
              @current-change="fetchData"
              @size-change="pageSize=$event;pageNum=1;fetchData()"
            />
          </div>
        </el-card>
      </section>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing.id ? '编辑用户' : '新增用户'" width="620px" destroy-on-close>
      <el-form :model="editing" label-width="86px" class="user-dialog-form">
        <el-form-item label="登录账号">
          <el-input v-model="editing.userId" placeholder="英文账号" :disabled="!!editing.id" />
        </el-form-item>
        <el-form-item label="用户姓名">
          <el-input v-model="editing.userName" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="editing.password"
            type="password"
            show-password
            :placeholder="editing.id ? '留空则不修改密码' : '请输入登录密码'"
          />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="selectedDeptIds" multiple placeholder="选择部门" style="width:100%">
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.deptName" :value="d.id" :disabled="d.status !== 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds" class="role-checks">
            <el-checkbox v-for="r in roleList" :key="r.id" :label="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editing.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editing.mail" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editing.status" active-value="NORM" inactive-value="DISABLED" active-text="正常" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deptDialogVisible" title="新增部门" width="480px" destroy-on-close>
      <el-form :model="newDept" label-width="86px">
        <el-form-item label="上级部门">
          <el-select v-model="newDept.parentId" placeholder="无（顶级部门）" clearable style="width:100%">
            <el-option :value="0" label="无（顶级部门）" />
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.deptName" :value="d.id" :disabled="d.status !== 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="newDept.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="newDept.deptCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="newDept.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deptEditVisible" title="编辑部门" width="480px" destroy-on-close>
      <el-form :model="newDept" label-width="86px">
        <el-form-item label="上级部门">
          <el-select v-model="newDept.parentId" placeholder="无（顶级部门）" clearable style="width:100%">
            <el-option :value="0" label="无（顶级部门）" />
            <el-option
              v-for="d in flatDepts"
              :key="d.id"
              :label="d.deptName"
              :value="d.id"
              :disabled="d.status !== 1 || d.id === newDept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="newDept.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="newDept.deptCode" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="newDept.leader" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="newDept.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDeptEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-page {
  min-width: 0;
}

.user-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  background:
    linear-gradient(135deg, rgba(31, 111, 235, 0.1), rgba(20, 35, 58, 0.04)),
    #fff;
}

.user-hero h1 {
  margin: 0;
  color: var(--gmp-text);
  font-size: 22px;
  font-weight: 800;
}

.user-hero p {
  margin: 6px 0 0;
  color: var(--gmp-text-muted);
  font-size: 13px;
}

.user-content {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.dept-panel {
  padding: 16px;
}

.dept-tree-wrap {
  max-height: calc(100vh - 280px);
  min-height: 360px;
  overflow: auto;
}

.dept-node {
  display: flex;
  align-items: center;
  gap: 7px;
  min-height: 36px;
  padding: 7px 8px;
  border-radius: 6px;
  color: var(--gmp-text);
  cursor: pointer;
  font-size: 13px;
}

.dept-node:hover {
  background: #f1f6ff;
}

.dept-node:hover .dept-actions {
  display: flex;
}

.dept-node.active {
  background: #eef5ff;
  color: var(--gmp-primary);
  font-weight: 700;
}

.dept-node.sub {
  padding-left: 10px;
}

.dept-children {
  margin-left: 14px;
  border-left: 1px dashed var(--gmp-border-strong);
  padding-left: 6px;
}

.dept-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-name.disabled {
  color: #94a3b8;
  text-decoration: line-through;
}

.dept-disabled-tag {
  flex-shrink: 0;
  padding: 0 4px;
  border-radius: 4px;
  background: #fef2f2;
  color: var(--gmp-danger);
  font-size: 10px;
}

.dept-count {
  flex-shrink: 0;
  margin-left: auto;
  padding: 1px 6px;
  border-radius: 9px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 11px;
}

.dept-actions {
  display: none;
  align-items: center;
  gap: 2px;
  margin-left: auto;
  flex-shrink: 0;
}

.user-table-wrap {
  min-width: 0;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.role-checks {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
}

@media (max-width: 1080px) {
  .user-content {
    grid-template-columns: 1fr;
  }

  .dept-tree-wrap {
    max-height: 320px;
    min-height: 0;
  }
}

@media (max-width: 720px) {
  .user-hero {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
