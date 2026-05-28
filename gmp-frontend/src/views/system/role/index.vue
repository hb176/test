<script setup lang="ts">
import { getRoleList, getRoleById, addRole, updateRole, deleteRole, getMenuTree, saveRoleMenus, getRoleMenus, getDeptTree } from '@/api/system'

const loading = ref(false), list = ref<any[]>([])
const selectedRole = ref<any>(null)
const menuTree = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const deptList = ref<any[]>([])
const selectedDeptIds = ref<number[]>([])
const collapsedDirs = ref<Set<number>>(new Set())

const dialogVisible = ref(false)
const editing = reactive({ id: null as number | null, roleCode: '', roleName: '', roleLevel: 1, status: 1, description: '' })

function buildMenuTree(items: any[], parentId = 0): any[] {
  return items.filter((m: any) => m.parentId === parentId).map((m: any) => ({ ...m, children: buildMenuTree(items, m.id) }))
}
function flatSubIds(node: any): number[] {
  const ids: number[] = [node.id]
  if (node.children) for (const c of node.children) ids.push(...flatSubIds(c))
  return ids
}

async function fetch() {
  loading.value = true
  try { const r = await getRoleList(); list.value = r.data || [] }
  finally { loading.value = false }
}
async function selectRole(row: any) {
  const [roleRes, menuRes, roleMenuRes, deptRes] = await Promise.all([getRoleById(row.id), getMenuTree(), getRoleMenus(row.id), getDeptTree()])
  selectedRole.value = roleRes.data
  menuTree.value = buildMenuTree(menuRes.data || [])
  checkedMenuIds.value = roleMenuRes.data || []
  deptList.value = deptRes.data || []
  selectedDeptIds.value = (roleRes.data?.deptIds || '').split(',').filter(Boolean).map(Number)
  collapsedDirs.value = new Set()
}

// ---- role info save ----
async function saveRole() {
  const r = selectedRole.value
  if (!r) return
  await updateRole(r.id, {
    roleCode: r.roleCode,
    roleName: r.roleName,
    roleLevel: r.roleLevel,
    status: r.status,
    description: r.description,
    dataScope: r.dataScope,
    deptIds: r.dataScope === 'CUSTOM' ? selectedDeptIds.value.join(',') : ''
  })
  ElMessage.success('已保存')
}

// ---- menu perm helpers ----
function allIds(node: any): number[] {
  if (!node.children?.length) return [node.id]
  let ids: number[] = []
  for (const c of node.children) ids = ids.concat(allIds(c))
  return ids
}
function menuChecked(node: any) {
  const ids = allIds(node)
  return ids.length > 0 && ids.every(id => checkedMenuIds.value.includes(id))
}
function menuIndeterminate(node: any) {
  const ids = allIds(node)
  const c = ids.filter(id => checkedMenuIds.value.includes(id)).length
  return c > 0 && c < ids.length
}
function toggleDir(node: any, val?: boolean) {
  const ids = allIds(node)
  const v = typeof val === 'boolean' ? val : !ids.every(id => checkedMenuIds.value.includes(id))
  if (v) { for (const id of ids) { if (!checkedMenuIds.value.includes(id)) checkedMenuIds.value.push(id) } }
  else { const s = new Set(ids); checkedMenuIds.value = checkedMenuIds.value.filter(i => !s.has(i)) }
}
function toggleBtn(id: number) {
  const i = checkedMenuIds.value.indexOf(id)
  if (i >= 0) checkedMenuIds.value.splice(i, 1)
  else checkedMenuIds.value.push(id)
}
function toggleCollapse(dirId: number) {
  if (collapsedDirs.value.has(dirId)) collapsedDirs.value.delete(dirId)
  else collapsedDirs.value.add(dirId)
  collapsedDirs.value = new Set(collapsedDirs.value)
}
async function saveMenus() {
  if (!selectedRole.value) return
  await saveRoleMenus(selectedRole.value.id, checkedMenuIds.value)
  ElMessage.success('菜单权限已保存')
}

// ---- role crud ----
function openAdd() {
  Object.assign(editing, { id: null, roleCode: '', roleName: '', roleLevel: 1, status: 1, description: '' })
  dialogVisible.value = true
}
async function save() {
  if (editing.id) { await updateRole(editing.id, { ...editing }) } else { await addRole({ ...editing }) }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}
async function del() {
  if (!selectedRole.value) return
  await ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' })
  await deleteRole(selectedRole.value.id)
  ElMessage.success('删除成功'); selectedRole.value = null; fetch()
}

onMounted(fetch)
</script>

<template>
  <div class="role-page">
    <!-- 左侧角色列表 -->
    <el-card class="role-left">
      <template #header><div class="card-header"><b>角色列表</b><el-button type="primary" size="small" @click="openAdd">新增</el-button></div></template>
      <div class="role-list-wrap">
        <div v-for="item in list" :key="item.id" class="role-item" :class="{ active: selectedRole?.id === item.id }" @click="selectRole(item)">
          <div class="role-item-info"><span class="role-name">{{ item.roleName }}</span><el-tag :type="item.status===1?'success':'danger'" size="small">{{ item.status===1?'启用':'禁用' }}</el-tag></div>
          <div class="role-item-bottom">
            <span class="role-code">{{ item.roleCode }}</span>
            <el-tag v-if="item.dataScope" size="small" type="info" effect="plain">
              {{ ({ ALL: '全部', DEPT: '本部门', DEPT_AND_CHILDREN: '部门+子', SELF: '本人', CUSTOM: '自定义' } as Record<string,string>)[item.dataScope] || item.dataScope }}
            </el-tag>
          </div>
        </div>
        <el-empty v-if="!loading && list.length===0" description="暂无角色" />
      </div>
    </el-card>

    <!-- 右侧详情 -->
    <div class="role-right" v-if="selectedRole">
      <!-- 角色信息(可编辑) -->
      <el-card>
        <template #header><div class="card-header"><b>角色信息</b><div><el-button size="small" type="primary" @click="saveRole">保存</el-button><el-button size="small" type="danger" @click="del">删除</el-button></div></div></template>
        <el-form :model="selectedRole" label-width="80px" size="small" style="max-width:420px">
          <el-form-item label="角色编码"><el-input v-model="selectedRole.roleCode" /></el-form-item>
          <el-form-item label="角色名称"><el-input v-model="selectedRole.roleName" /></el-form-item>
          <el-form-item label="显示顺序"><el-input-number v-model="selectedRole.roleLevel" :min="0" controls-position="right" style="width:100%" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="selectedRole.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
          <el-form-item label="数据权限">
            <el-select v-model="selectedRole.dataScope" style="width:100%">
              <el-option label="全部数据" value="ALL" />
              <el-option label="本部门数据" value="DEPT" />
              <el-option label="本部门及子部门" value="DEPT_AND_CHILDREN" />
              <el-option label="仅本人数据" value="SELF" />
              <el-option label="自定义部门" value="CUSTOM" />
            </el-select>
          </el-form-item>
          <el-form-item label="限定部门" v-if="selectedRole.dataScope === 'CUSTOM'">
            <el-select v-model="selectedDeptIds" multiple placeholder="选择可见部门" style="width:100%"><el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id" /></el-select>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="selectedRole.description" type="textarea" :rows="2" /></el-form-item>
        </el-form>
      </el-card>

      <!-- 菜单权限（可折叠+横向） -->
      <el-card style="flex:1;min-height:0">
        <template #header><div class="card-header"><b>菜单权限</b><el-button type="primary" size="small" @click="saveMenus">保存权限</el-button></div></template>
        <div class="menu-scroll">
          <div v-for="dir in menuTree" :key="dir.id" class="menu-dir">
            <div class="menu-dir-head" @click="toggleCollapse(dir.id)">
              <el-icon :size="14" style="transition:transform .2s" :style="collapsedDirs.has(dir.id)?{}:{transform:'rotate(90deg)'}"><ArrowRight /></el-icon>
              <el-checkbox :model-value="menuChecked(dir)" :indeterminate="menuIndeterminate(dir)" @change="toggleDir(dir, $event as boolean)" @click.stop><b>{{ dir.menuName }}</b></el-checkbox>
            </div>
            <div v-show="!collapsedDirs.has(dir.id)" class="menu-dir-body">
              <div v-for="sub in dir.children" :key="sub.id" class="menu-sub-row">
                <el-checkbox :model-value="menuChecked(sub)" :indeterminate="menuIndeterminate(sub)" @change="toggleDir(sub, $event as boolean)" style="min-width:100px;flex-shrink:0">{{ sub.menuName }}</el-checkbox>
                <span v-if="sub.children?.length" class="menu-btns">
                  <el-checkbox v-for="btn in sub.children" :key="btn.id" :model-value="checkedMenuIds.includes(btn.id)" @change="toggleBtn(btn.id)" size="small">{{ btn.menuName }}</el-checkbox>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!selectedRole" description="请从左侧选择一个角色" style="flex:1;background:#fff;border-radius:8px;padding:60px 0" />

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增角色" width="500px" destroy-on-close>
      <el-form :model="editing" label-width="80px">
        <el-form-item label="角色编码"><el-input v-model="editing.roleCode" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="editing.roleName" /></el-form-item>
        <el-form-item label="显示顺序"><el-input-number v-model="editing.roleLevel" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="editing.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="editing.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-page { display: flex; gap: 16px; height: calc(100vh - 144px); }
.role-left { width: 320px; flex-shrink: 0; display: flex; flex-direction: column; }
.role-left :deep(.el-card__body) { flex: 1; overflow: auto; padding: 0; }
.role-right { flex: 1; display: flex; flex-direction: column; gap: 16px; overflow: auto; min-width: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.role-list-wrap { padding: 8px; }
.role-item { padding: 12px 16px; cursor: pointer; border-radius: 6px; margin-bottom: 4px; transition: all 0.15s; }
.role-item:hover { background: #f0f5ff; }
.role-item.active { background: #e6f0ff; border-left: 3px solid #409EFF; padding-left: 13px; }
.role-item-info { display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px; }
.role-item-bottom { display: flex; align-items: center; justify-content: space-between; }
.role-name { font-weight: 500; color: #303133; }
.role-code { font-size: 12px; color: #909399; }

/* menu */
.menu-scroll { overflow: auto; }
.menu-dir { margin-bottom: 6px; border: 1px solid #ebeef5; border-radius: 6px; }
.menu-dir-head { display: flex; align-items: center; gap: 6px; padding: 8px 12px; cursor: pointer; background: #fafafa; border-radius: 6px; user-select: none; }
.menu-dir-head:hover { background: #f0f5ff; }
.menu-dir-body { padding: 6px 12px 10px; }
.menu-sub-row { padding: 4px 0 4px 12px; }
.menu-sub-row > .el-checkbox { margin-bottom: 2px; }
.menu-btns { display: flex; flex-wrap: wrap; gap: 2px 14px; padding: 2px 0 0 18px; }
:deep(.menu-dir-head .el-checkbox__label) { font-size: 14px; font-weight: 600; }
:deep(.menu-sub-row .el-checkbox__label) { font-size: 13px; }
:deep(.menu-btns .el-checkbox__label) { font-size: 12px; color: #606266; }
</style>
