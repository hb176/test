<script setup lang="ts">
import { getMenuTree } from '@/api/system'
import request from '@/utils/request'

const loading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const editing = reactive<any>({ id: null, parentId: 0, menuName: '', menuType: 1, path: '', permission: '', icon: '', sortOrder: 0, visible: 1 })

function buildTree(items: any[], parentId = 0): any[] {
  return items.filter((m: any) => m.parentId === parentId).map((m: any) => ({ ...m, children: buildTree(items, m.id) }))
}
async function fetch() {
  loading.value = true
  try { const r = await getMenuTree(); treeData.value = buildTree(r.data || []) }
  finally { loading.value = false }
}
function openAdd(parentId = 0) {
  Object.assign(editing, { id: null, parentId, menuName: '', menuType: parentId === 0 ? 0 : 1, path: '', permission: '', icon: '', sortOrder: 0, visible: 1 })
  dialogVisible.value = true
}
function openEdit(row: any) { Object.assign(editing, row); dialogVisible.value = true }
async function save() {
  if (editing.id) { await request.put(`/system/menu/${editing.id}`, editing) } else { await request.post('/system/menu', editing) }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}
async function del(id: number) {
  await ElMessageBox.confirm('删除后子菜单也将删除，确定？', '确认', { type: 'warning' })
  await request.delete(`/system/menu/${id}`); ElMessage.success('删除成功'); fetch()
}
onMounted(fetch)
</script>

<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <h3 style="margin:0">菜单管理</h3>
      <el-button type="primary" @click="openAdd(0)">新增目录</el-button>
    </div>
    <el-table :data="treeData" v-loading="loading" row-key="id" border stripe>
      <el-table-column prop="menuName" label="菜单名称" min-width="180" />
      <el-table-column prop="menuType" label="类型" width="80" align="center">
        <template #default="{ row }"><el-tag :type="row.menuType===0?'':row.menuType===1?'success':'warning'" size="small">{{ {0:'目录',1:'菜单',2:'按钮'}[row.menuType] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" width="160" />
      <el-table-column prop="permission" label="权限标识" width="180" />
      <el-table-column prop="icon" label="图标" width="80" />
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column prop="visible" label="可见" width="70" align="center">
        <template #default="{ row }">{{ row.visible===1?'是':'否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAdd(row.id)">新增子项</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing.id?'编辑菜单':'新增菜单'" width="560px" destroy-on-close>
      <el-form :model="editing" label-width="80px">
        <el-form-item label="上级ID"><el-input-number v-model="editing.parentId" :min="0" disabled /></el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="editing.menuName" /></el-form-item>
        <el-form-item label="菜单类型"><el-radio-group v-model="editing.menuType"><el-radio :value="0">目录</el-radio><el-radio :value="1">菜单</el-radio><el-radio :value="2">按钮</el-radio></el-radio-group></el-form-item>
        <el-form-item label="路由路径"><el-input v-model="editing.path" placeholder="/system/role" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="editing.permission" placeholder="system:user:add" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="editing.icon" placeholder="Setting" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editing.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="可见"><el-switch v-model="editing.visible" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>
