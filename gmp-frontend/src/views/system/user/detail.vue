<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { updateUser, resetPassword, getRoleList, getDeptTree, getRoleMenus, saveRoleMenus } from '@/api/system'

const route = useRoute()
const router = useRouter()
const userId = Number(route.params.id)
const loading = ref(false), user = reactive<any>({})
const pwdForm = reactive({ password: '' })
const pwdDialogVisible = ref(false)
const deptTree = ref<any[]>([])
const selectedDepts = ref<number[]>([])
const roleList = ref<any[]>([])
const selectedRoles = ref<number[]>([])

async function fetch() {
  loading.value = true
  try {
    const [userRes, deptRes, roleRes, userRoleRes] = await Promise.all([
      request.get(`/system/user/${userId}`),
      getDeptTree(),
      getRoleList(),
      request.get(`/system/user/${userId}/roles`).catch(() => ({ data: [] }))
    ])
    Object.assign(user, userRes.data?.data || userRes.data || {})
    deptTree.value = deptRes.data || []
    roleList.value = roleRes.data || []
    selectedDepts.value = user.deptIds ? user.deptIds.split(',').map(Number) : (user.deptId ? [user.deptId] : [])
    selectedRoles.value = userRoleRes.data || []
  } finally { loading.value = false }
}
const displayDeptName = computed(() => {
  const ids = selectedDepts.value
  if (!ids.length) return ''
  const first = deptTree.value.find((d: any) => d.id === ids[0])
  if (!first) return ''
  if (ids.length === 1) return first.deptName
  let cur = first, name = cur.deptName
  while (cur.parentId && cur.parentId > 0) {
    const p = deptTree.value.find((d: any) => d.id === cur.parentId)
    if (!p) break; cur = p; name = cur.deptName
  }
  return name
})
async function save() {
  user.deptIds = selectedDepts.value.join(',')
  user.deptId = selectedDepts.value[0] || null
  user.deptName = displayDeptName.value
  await updateUser(userId, user)
  await request.put(`/system/user/${userId}/roles`, selectedRoles.value)
  ElMessage.success('已保存')
  await fetch()
}

async function savePwd() { await resetPassword(userId, pwdForm.password); ElMessage.success('密码已重置'); pwdDialogVisible.value = false }
async function toggleStatus() {
  const ns = user.status === 'NORM' ? 'DISABLED' : 'NORM'
  await ElMessageBox.confirm(`确定${ns==='DISABLED'?'禁用':'启用'}？`, '提示', { type: 'warning' })
  await request.put(`/system/user/${userId}/status`, { status: ns })
  ElMessage.success('操作成功'); fetch()
}

onMounted(fetch)
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div style="display:flex;align-items:center;gap:8px">
          <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon></el-button>
          <b>用户详情 - {{ user.userName || userId }}</b>
        </div>
        <div style="display:flex;gap:8px">
          <el-button @click="pwdDialogVisible=true">重置密码</el-button>
          <el-button :type="user.status==='NORM'?'danger':'success'" @click="toggleStatus">{{ user.status==='NORM'?'禁用':'启用' }}</el-button>
        </div>
      </div>
    </template>

    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="用户编号">{{ user.id }}</el-descriptions-item>
      <el-descriptions-item label="登录账号">{{ user.userId }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ user.userName }}</el-descriptions-item>
      <el-descriptions-item label="工号">{{ user.jobNum || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态"><el-tag :type="user.status==='NORM'?'success':'danger'" size="small">{{ user.status==='NORM'?'正常':'禁用' }}</el-tag></el-descriptions-item>
      <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user.mail || '-' }}</el-descriptions-item>
      <el-descriptions-item label="用户类型">{{ user.userType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ user.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属部门" :span="2">
        <span v-if="displayDeptName">{{ displayDeptName }}</span>
        <span v-else style="color:#c0c4cc">未分配</span>
      </el-descriptions-item>
    </el-descriptions>

    <el-divider />
    <h4>编辑信息</h4>
    <el-form :model="user" label-width="80px" style="max-width:600px;margin-top:12px">
      <el-form-item label="登录账号"><el-input :model-value="user.userId" disabled /></el-form-item>
      <el-form-item label="用户名"><el-input v-model="user.userName" /></el-form-item>
      <el-form-item label="工号"><el-input v-model="user.jobNum" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="user.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="user.mail" /></el-form-item>
      <el-form-item label="所属部门">
        <el-select v-model="selectedDepts" multiple placeholder="选择部门" style="width:100%">
          <el-option v-for="d in deptTree" :key="d.id" :label="d.deptName" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色分配">
        <el-checkbox-group v-model="selectedRoles" style="display:flex;flex-wrap:wrap;gap:4px 20px">
          <el-checkbox v-for="r in roleList" :key="r.id" :label="r.id" :value="r.id">{{ r.roleName }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <el-button type="primary" @click="save" style="margin-left:80px">保存修改</el-button>

    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px">
      <el-form><el-form-item label="新密码"><el-input v-model="pwdForm.password" type="password" show-password /></el-form-item></el-form>
      <template #footer><el-button @click="pwdDialogVisible=false">取消</el-button><el-button type="primary" @click="savePwd">确定</el-button></template>
    </el-dialog>
  </el-card>
</template>
