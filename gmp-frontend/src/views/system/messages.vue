<script setup lang="ts">
import { getMessages, getUnreadCount, markAsRead, markAllAsRead, deleteMessage } from '@/api/business'
import { getTemplateList, createTemplate, updateTemplate, deleteTemplate } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeTab = ref<'inbox'|'templates'>('inbox')
const unreadCount = ref(0)

// Templates
const tplList = ref<any[]>([])
const tplDialog = ref(false)
const tplForm = reactive<any>({ templateCode: '', templateName: '', titleTemplate: '', contentTemplate: '', msgType: 'INTERNAL', sysModule: 'HCP', enabled: 1 })
const tplEditId = ref<number | null>(null)

async function fetchMessages() {
  loading.value = true
  try {
    const [msgRes, countRes] = await Promise.all([
      getMessages({ pageNum: pageNum.value, pageSize: pageSize.value }),
      getUnreadCount().catch(() => ({ data: 0 })),
    ])
    const pageData = msgRes.data || {}
    list.value = pageData.records || []
    total.value = pageData.total || 0
    unreadCount.value = countRes.data || 0
  } finally { loading.value = false }
}

async function fetchTemplates() {
  try {
    const res = await getTemplateList()
    tplList.value = res.data || []
  } catch { /* */ }
}

async function handleMarkRead(row: any) {
  await markAsRead(row.id)
  if (row.readFlag === 0) unreadCount.value = Math.max(0, unreadCount.value - 1)
  row.readFlag = 1
  ElMessage.success('已标记为已读')
}

async function handleMarkAllRead() {
  await markAllAsRead()
  list.value.forEach((r: any) => { r.readFlag = 1 })
  unreadCount.value = 0
  ElMessage.success('全部已读')
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确认删除该消息？', '删除确认', { type: 'warning' })
  await deleteMessage(row.id)
  if (row.readFlag === 0) unreadCount.value = Math.max(0, unreadCount.value - 1)
  ElMessage.success('已删除')
  fetchMessages()
}

function handleTabChange(tab: any) {
  if (tab === 'templates') fetchTemplates()
  else fetchMessages()
}

// Template CRUD
function openTplCreate() {
  tplEditId.value = null
  Object.assign(tplForm, { templateCode: '', templateName: '', titleTemplate: '', contentTemplate: '', msgType: 'INTERNAL', sysModule: 'HCP', enabled: 1 })
  tplDialog.value = true
}
function openTplEdit(row: any) {
  tplEditId.value = row.id
  Object.assign(tplForm, row)
  tplDialog.value = true
}
async function handleTplSave() {
  try {
    if (tplEditId.value) {
      await updateTemplate(tplEditId.value, { ...tplForm })
    } else {
      await createTemplate({ ...tplForm })
    }
    ElMessage.success(tplEditId.value ? '更新成功' : '创建成功')
    tplDialog.value = false
    fetchTemplates()
  } catch { /* */ }
}
async function handleTplDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除模板 "${row.templateName}"？`, '删除确认', { type: 'warning' })
    await deleteTemplate(row.id)
    ElMessage.success('已删除')
    fetchTemplates()
  } catch { /* */ }
}

onMounted(fetchMessages)
</script>

<template>
  <el-card>
    <div class="page-header">
      <h3>消息中心
        <el-badge v-if="unreadCount > 0 && activeTab === 'inbox'" :value="unreadCount" style="margin-left:8px" />
      </h3>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="收件箱" name="inbox" />
      <el-tab-pane label="消息模板" name="templates" />
    </el-tabs>

    <!-- 收件箱 -->
    <template v-if="activeTab === 'inbox'">
      <div style="margin-bottom:12px;display:flex;justify-content:space-between;align-items:center">
        <span />
        <el-button size="small" :disabled="unreadCount===0" @click="handleMarkAllRead">全部已读</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe highlight-current-row>
        <el-table-column label="" width="48">
          <template #default="{ row }">
            <el-icon v-if="row.readFlag===0" color="#409EFF" :size="14"><InfoFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" :class-name="(r:any)=>r.readFlag===0?'unread-row':''" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.msgType==='WARNING'?'danger':row.msgType==='SYSTEM'?'info':'primary'">
              {{ ({SYSTEM:'系统',NOTIFICATION:'通知',WARNING:'预警',APPROVAL:'审批'} as Record<string,string>)[row.msgType] || row.msgType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="senderName" label="发送人" width="100" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.readFlag===0" link type="primary" size="small" @click="handleMarkRead(row)">标为已读</el-button>
            <span v-else style="color:#999;font-size:12px">已读</span>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length===0" description="暂无消息" />
      <el-pagination v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="total,prev,pager,next" style="margin-top:16px;justify-content:flex-end" @current-change="fetchMessages" @size-change="pageSize=$event;pageNum=1;fetchMessages()" />
    </template>

    <!-- 消息模板 -->
    <template v-if="activeTab === 'templates'">
      <div style="margin-bottom:12px">
        <el-button type="primary" size="small" @click="openTplCreate">新建模板</el-button>
      </div>
      <el-table :data="tplList" stripe size="small">
        <el-table-column prop="templateCode" label="编码" width="160" />
        <el-table-column prop="templateName" label="名称" width="180" />
        <el-table-column prop="titleTemplate" label="标题模板" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sysModule" label="模块" width="80" />
        <el-table-column prop="msgType" label="类型" width="80" />
        <el-table-column label="启用" width="60">
          <template #default="{ row }"><el-tag :type="row.enabled===1?'success':'info'" size="small">{{ row.enabled===1?'是':'否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openTplEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleTplDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="tplDialog" :title="tplEditId?'编辑模板':'新建模板'" width="560px" destroy-on-close>
        <el-form :model="tplForm" label-width="80px">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="编码" required><el-input v-model="tplForm.templateCode" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="名称" required><el-input v-model="tplForm.templateName" /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="模块"><el-select v-model="tplForm.sysModule" style="width:100%">
                <el-option v-for="m in ['HCP','DMS','TMS','QMS']" :key="m" :label="m" :value="m" /></el-select></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="类型"><el-select v-model="tplForm.msgType" style="width:100%">
                <el-option v-for="t in ['INTERNAL','EMAIL','SMS','FEISHU','WEIXIN']" :key="t" :label="t" :value="t" /></el-select></el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="标题模板"><el-input v-model="tplForm.titleTemplate" placeholder="支持 {{变量}}" /></el-form-item>
          <el-form-item label="内容模板"><el-input v-model="tplForm.contentTemplate" type="textarea" :rows="4" placeholder="支持变量和换行" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="tplForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="tplDialog=false">取消</el-button>
          <el-button type="primary" @click="handleTplSave">确定</el-button>
        </template>
      </el-dialog>
    </template>
  </el-card>
</template>

<style scoped>
.page-header { margin-bottom: 8px; }
</style>
