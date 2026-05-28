<script setup lang="ts">
import { getTaskList, createTask, updateTask, deleteTask, enableTask, disableTask, executeTaskNow, getTaskLogs, validateCron } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const activeModule = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskList({ sysModule: activeModule.value || undefined })
    list.value = res.data || []
  } finally { loading.value = false }
}

// Dialog
const dialogVisible = ref(false)
const formLoading = ref(false)
const editId = ref<number | null>(null)
const form = reactive({
  taskCode: '', taskName: '', cronExpression: '', sysModule: 'HCP',
  beanName: '', methodName: '', description: '',
})

function openCreate() {
  editId.value = null
  Object.assign(form, { taskCode: '', taskName: '', cronExpression: '', sysModule: 'HCP', beanName: '', methodName: '', description: '' })
  dialogVisible.value = true
}
function openEdit(row: any) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSave() {
  // 校验 Cron 表达式
  if (form.cronExpression) {
    try {
      await validateCron(form.cronExpression)
    } catch {
      ElMessage.error('Cron 表达式格式不正确')
      return
    }
  }
  formLoading.value = true
  try {
    if (editId.value) await updateTask(editId.value, { ...form })
    else await createTask({ ...form })
    ElMessage.success(editId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { formLoading.value = false }
}
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除 "${row.taskName}"？`, '删除', { type: 'warning' })
    await deleteTask(row.id)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* */ }
}
async function handleToggle(row: any) {
  try {
    if (row.status === 1) { await disableTask(row.id); row.status = 0 }
    else { await enableTask(row.id); row.status = 1 }
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
    fetchData()
  } catch { /* */ }
}
async function handleExecute(row: any) {
  try {
    await ElMessageBox.confirm(`确认立即执行 "${row.taskName}"？`, '手动执行', { type: 'info' })
    await executeTaskNow(row.id)
    ElMessage.success('任务已触发')
  } catch { /* */ }
}

// Logs drawer
const logDrawer = ref(false)
const logTaskName = ref('')
const logList = ref<any[]>([])
const logLoading = ref(false)
async function showLogs(row: any) {
  logTaskName.value = row.taskName
  logDrawer.value = true
  logLoading.value = true
  try {
    const res = await getTaskLogs(row.id, { pageNum: 1, pageSize: 50 })
    logList.value = (res.data?.records || res.data) || []
  } finally { logLoading.value = false }
}

const modules = ['HCP', 'DMS', 'TMS', 'QMS']

onMounted(fetchData)
</script>

<template>
  <el-card>
    <div class="page-header">
      <h3>定时任务</h3>
      <el-button type="primary" @click="openCreate">新建任务</el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="activeModule" @change="fetchData" size="small">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button v-for="m in modules" :key="m" :value="m">{{ m }}</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" v-loading="loading" stripe size="small">
      <el-table-column prop="taskCode" label="任务编码" width="180" />
      <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="cronExpression" label="Cron表达式" width="140" />
      <el-table-column prop="sysModule" label="模块" width="70" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastExecTime" label="最后执行" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link size="small" :type="row.status===1?'warning':'success'" @click="handleToggle(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
          <el-button link type="success" size="small" @click="handleExecute(row)">执行</el-button>
          <el-button link size="small" @click="showLogs(row)">日志</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length===0" description="暂无定时任务" />

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId?'编辑任务':'新建任务'" width="550px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="任务编码" required><el-input v-model="form.taskCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务名称" required><el-input v-model="form.taskName" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Cron表达式" required>
          <el-input v-model="form.cronExpression" placeholder="如 0 0 8 * * ?" />
          <div style="font-size:11px;color:#999;margin-top:4px">
            常用: 每小时 0 0 * * * ? | 每天8点 0 0 8 * * ? | 每5分钟 0 */5 * * * ?
          </div>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="模块"><el-select v-model="form.sysModule" style="width:100%">
              <el-option v-for="m in modules" :key="m" :label="m" :value="m" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Bean名称"><el-input v-model="form.beanName" placeholder="Spring Bean" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="方法名"><el-input v-model="form.methodName" placeholder="要调用的方法名" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行日志抽屉 -->
    <el-drawer v-model="logDrawer" :title="`执行日志 - ${logTaskName}`" size="500px">
      <el-table :data="logList" v-loading="logLoading" size="small" max-height="calc(100vh - 120px)">
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="costMs" label="耗时(ms)" width="90" />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result==='SUCCESS'?'success':'danger'" size="small">{{ row.result==='SUCCESS'?'成功':'失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" min-width="200" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!logLoading && logList.length===0" description="暂无执行记录" />
    </el-drawer>
  </el-card>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.filter-bar { margin-bottom: 12px; }
</style>
