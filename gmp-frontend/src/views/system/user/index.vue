<script setup lang="ts">
import { getUserPage, addUser, updateUser, getDeptTree, addDept, updateDept, deleteDept, getRoleList } from '@/api/system'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false), list = ref<any[]>([]), total = ref(0)
const pageNum = ref(1), pageSize = ref(10)
const keyword = ref(''), statusFilter = ref(''), selectedDeptId = ref<number | undefined>()
const deptTree = ref<any[]>([])
const flatDepts = ref<any[]>([])
const roleList = ref<any[]>([])
const selectedDeptIds = ref<number[]>([])
const selectedRoleIds = ref<number[]>([])
const dialogVisible = ref(false), deptDialogVisible = ref(false), deptEditVisible = ref(false)
const editing = reactive<any>({ id: null, userId: '', userName: '', deptId: null, deptName: '', phone: '', mail: '', status: 'NORM' })
const newDept = reactive({ id: null as number | null, deptName: '', parentId: 0, deptCode: '', sortOrder: 0, leader: '', status: 1 })

function buildDeptTree(items: any[]): any[] {
  return items.filter(d => d.parentId === 0).map(d => ({ ...d, children: buildChildren(items, d.id) }))
}
function buildChildren(items: any[], pid: number): any[] {
  return items.filter(d => d.parentId === pid).map(d => ({ ...d, children: buildChildren(items, d.id) }))
}

async function fetchDept() {
  const r = await getDeptTree(); flatDepts.value = r.data || []; deptTree.value = buildDeptTree(flatDepts.value)
}
function openDeptEdit(dept: any) { Object.assign(newDept, dept); deptEditVisible.value = true }
async function saveDeptEdit() { await updateDept(newDept.id!, newDept); ElMessage.success('已更新'); deptEditVisible.value = false; fetchDept() }
async function toggleDept(dept: any) {
  await updateDept(dept.id, { ...dept, status: dept.status === 1 ? 0 : 1 }); ElMessage.success('操作成功'); fetchDept()
}
async function fetch() {
  loading.value = true
  try {
    const r = await getUserPage({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined, deptId: selectedDeptId.value, status: statusFilter.value || undefined })
    list.value = r.data?.records || []; total.value = r.data?.total || 0
  } finally { loading.value = false }
}
function selectDept(dept: any) { selectedDeptId.value = selectedDeptId.value === dept.id ? undefined : dept.id; pageNum.value = 1; fetch() }
function openAdd() { Object.assign(editing, { id: null, userId: '', userName: '', deptId: null, deptName: '', phone: '', mail: '', status: 'NORM' }); selectedDeptIds.value = []; selectedRoleIds.value = []; dialogVisible.value = true }
function openEdit(row: any) { Object.assign(editing, row); selectedDeptIds.value = row.deptIds ? row.deptIds.split(',').map(Number).filter(Boolean) : (row.deptId ? [row.deptId] : []); selectedRoleIds.value = []; request.get(`/system/user/${row.id}/roles`).then(r => { selectedRoleIds.value = r.data || r.data?.data || [] }).catch(()=>{}); dialogVisible.value = true }
async function save() {
  const firstDept = flatDepts.value.find((d: any) => d.id === selectedDeptIds.value[0])
  let deptName = firstDept?.deptName || ''
  if (selectedDeptIds.value.length > 1 && firstDept) {
    let cur = firstDept
    while (cur.parentId && cur.parentId > 0) {
      const p = flatDepts.value.find((d: any) => d.id === cur.parentId)
      if (!p) break; cur = p; deptName = cur.deptName
    }
  }
  const payload = { ...editing, deptId: selectedDeptIds.value[0] || null, deptName, deptIds: selectedDeptIds.value.join(',') }
  if (editing.id) {
    await updateUser(editing.id, payload)
    await request.put(`/system/user/${editing.id}/roles`, selectedRoleIds.value)
  } else {
    const r = await addUser(payload)
    const newId = r?.data?.id || r?.id
    if (newId && selectedRoleIds.value.length) {
      await request.put(`/system/user/${newId}/roles`, selectedRoleIds.value).catch(() => {})
    }
  }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}
async function toggleStatus(row: any) {
  const ns = row.status === 'NORM' ? 'DISABLED' : 'NORM'
  await ElMessageBox.confirm(`确定${ns==='DISABLED'?'禁用':'启用'}该用户？`, '提示', { type: 'warning' })
  await request.put(`/system/user/${row.id}/status`, { status: ns }); ElMessage.success('操作成功'); fetch()
}
function goDetail(row: any) { router.push(`/system/user/${row.id}`) }
async function saveDept() {
  await addDept(newDept); ElMessage.success('部门已新增'); deptDialogVisible.value = false; fetchDept()
}

onMounted(async () => { fetchDept(); fetch(); const r = await getRoleList(); roleList.value = r.data || [] })
</script>

<template>
  <div class="user-page">
    <!-- 左侧部门树 -->
    <el-card class="user-left">
      <template #header><b>部门结构</b></template>
      <div class="dept-tree-wrap">
        <div v-for="d in deptTree" :key="d.id">
          <div class="dept-node" :class="{ active: selectedDeptId === d.id }" @click="selectDept(d)">
            <el-icon><OfficeBuilding /></el-icon><span>{{ d.deptName }}</span>
            <span v-if="d.status!==1" class="dept-disabled-tag">禁用</span>
            <span class="dept-count">{{ d.children?.length || 0 }}</span>
            <div class="dept-actions" @click.stop>
              <el-button link size="small" @click="openDeptEdit(d)"><el-icon><Edit /></el-icon></el-button>
              <el-button link size="small" :type="d.status===1?'danger':'success'" @click="toggleDept(d)">{{ d.status===1?'禁用':'启用' }}</el-button>
            </div>
          </div>
          <div v-if="d.children?.length" style="padding-left:12px">
            <div v-for="c in d.children" :key="c.id" class="dept-node sub" :class="{ active: selectedDeptId === c.id }" @click="selectDept(c)">
              <el-icon><OfficeBuilding /></el-icon><span :style="c.status!==1?{color:'#c0c4cc',textDecoration:'line-through'}:{}">{{ c.deptName }}</span>
              <div class="dept-actions" @click.stop>
                <el-button link size="small" @click="openDeptEdit(c)"><el-icon><Edit /></el-icon></el-button>
                <el-button link size="small" :type="c.status===1?'danger':'success'" @click="toggleDept(c)">{{ c.status===1?'禁用':'启用' }}</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="dept-add-btn">
        <el-button type="primary" size="small" style="width:100%" @click="deptDialogVisible=true">+ 新增部门</el-button>
      </div>
    </el-card>

    <!-- 右侧用户表格 -->
    <div class="user-right">
      <el-card>
        <div style="display:flex;justify-content:space-between;margin-bottom:16px">
          <div style="display:flex;gap:12px">
            <el-input v-model="keyword" placeholder="用户名/账号" clearable style="width:180px" @keyup.enter="fetch" />
            <el-select v-model="statusFilter" placeholder="状态" clearable style="width:100px" @change="fetch"><el-option label="正常" value="NORM" /><el-option label="禁用" value="DISABLED" /></el-select>
            <el-button @click="fetch">查询</el-button>
          </div>
          <el-button type="primary" @click="openAdd">新增用户</el-button>
        </div>
        <el-table :data="list" v-loading="loading" border stripe @row-click="goDetail" style="cursor:pointer">
          <el-table-column prop="id" label="编号" width="70" />
          <el-table-column prop="userId" label="登录账号" width="120" />
          <el-table-column prop="userName" label="用户名" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="mail" label="邮箱" min-width="150" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }"><el-tag :type="row.status==='NORM'?'success':'danger'" size="small">{{ row.status==='NORM'?'正常':'禁用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="goDetail(row)">详情</el-button>
              <el-button link :type="row.status==='NORM'?'danger':'success'" @click.stop="toggleStatus(row)">{{ row.status==='NORM'?'禁用':'启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next" :page-sizes="[10,20,50]" style="margin-top:16px;justify-content:flex-end" @current-change="fetch" @size-change="pageSize=$event;pageNum=1;fetch()" />
      </el-card>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing.id?'编辑用户':'新增用户'" width="560px" destroy-on-close>
      <el-form :model="editing" label-width="80px">
        <el-form-item label="登录账号"><el-input v-model="editing.userId" placeholder="英文账号" :disabled="!!editing.id" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="editing.userName" placeholder="显示名称" /></el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="selectedDeptIds" multiple placeholder="选择部门" style="width:100%">
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.deptName" :value="d.id" :disabled="d.status !== 1" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds" style="display:flex;flex-wrap:wrap;gap:4px 16px">
            <el-checkbox v-for="r in roleList" :key="r.id" :label="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="editing.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editing.mail" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="editing.status" active-value="NORM" inactive-value="DISABLED" active-text="正常" inactive-text="禁用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>

    <!-- 新增/编辑部门弹窗 -->
    <el-dialog v-model="deptDialogVisible" title="新增部门" width="460px" destroy-on-close>
      <el-form :model="newDept" label-width="70px">
        <el-form-item label="上级部门">
          <el-select v-model="newDept.parentId" placeholder="无(顶级部门)" clearable style="width:100%">
            <el-option :value="0" label="无(顶级部门)" />
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.deptName" :value="d.id" :disabled="d.status!==1" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="newDept.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="newDept.deptCode" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="newDept.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="deptDialogVisible=false">取消</el-button><el-button type="primary" @click="saveDept">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="deptEditVisible" title="编辑部门" width="460px" destroy-on-close>
      <el-form :model="newDept" label-width="70px">
        <el-form-item label="上级部门">
          <el-select v-model="newDept.parentId" placeholder="无(顶级部门)" clearable style="width:100%">
            <el-option :value="0" label="无(顶级部门)" />
            <el-option v-for="d in flatDepts" :key="d.id" :label="d.deptName" :value="d.id" :disabled="d.status!==1 || d.id===newDept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="newDept.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="newDept.deptCode" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="newDept.leader" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="newDept.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="deptEditVisible=false">取消</el-button><el-button type="primary" @click="saveDeptEdit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-page { display: flex; gap: 16px; }
.user-left { width: 240px; flex-shrink: 0; }
.user-left :deep(.el-card__body) { padding: 8px 12px; }
.user-right { flex: 1; min-width: 0; }
.dept-tree-wrap { max-height: 400px; overflow: auto; }
.dept-node { display: flex; align-items: center; gap: 6px; padding: 6px 8px; border-radius: 4px; cursor: pointer; font-size: 13px; color: #303133; }
.dept-node:hover { background: #f0f5ff; }
.dept-node:hover .dept-actions { display: flex; }
.dept-node.active { background: #e6f0ff; color: #409EFF; font-weight: 500; }
.dept-node.sub { padding-left: 6px; }
.dept-actions { display: none; align-items: center; gap: 2px; margin-left: auto; flex-shrink: 0; }
.dept-disabled-tag { font-size: 10px; color: #f56c6c; background: #fef0f0; padding: 0 4px; border-radius: 2px; }
.dept-count { font-size: 11px; color: #909399; background: #f0f2f5; padding: 1px 6px; border-radius: 8px; }
.dept-add-btn { margin-top: 12px; padding-top: 12px; border-top: 1px solid #ebeef5; }
</style>
