<script setup lang="ts">
import { getDocument, submitForReview, obsoleteDocument, reReviewDocument, getVersionHistory } from '@/api/business'
import { useRoute, useRouter } from 'vue-router'
import FileUpload from '@/components/FileUpload.vue'

const route = useRoute()
const router = useRouter()
const id = computed(() => Number(route.params.id))
const loading = ref(false)
const doc = ref<any>(null)
const versions = ref<any[]>([])
const activeTab = ref('info')

async function fetchData() {
  loading.value = true
  try {
    const [docRes, verRes] = await Promise.all([
      getDocument(id.value),
      getVersionHistory(id.value).catch(() => ({ data: [] })),
    ])
    doc.value = docRes.data
    versions.value = verRes.data || []
  } finally { loading.value = false }
}

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('确认提交审核？', '提交审核', { type: 'info' })
    await submitForReview(id.value)
    ElMessage.success('已提交审核')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleObsolete() {
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因', '文件作废', { type: 'warning' })
    await obsoleteDocument(id.value, { reason: value })
    ElMessage.success('文件已作废')
    fetchData()
  } catch { /* cancelled */ }
}

async function handleReReview() {
  try {
    await ElMessageBox.confirm('发起文件复审？', '复审确认', { type: 'info' })
    await reReviewDocument(id.value)
    ElMessage.success('复审流程已启动')
    fetchData()
  } catch { /* cancelled */ }
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

function getStatusTag(status: string): { type: TagType; text: string } {
  const map: Record<string, { type: TagType; text: string }> = {
    DRAFT: { type: 'info', text: '草稿' },
    APPROVING: { type: 'warning', text: '审批中' },
    COMPLETED: { type: 'success', text: '已生效' },
    OBSOLETED: { type: 'danger', text: '已作废' },
    REJECTED: { type: 'danger', text: '已驳回' },
  }
  return map[status] || { type: 'info' as TagType, text: status || '未知' }
}

function goBack() { router.back() }
function notImplemented(msg: string) { ElMessage.info(msg) }

onMounted(fetchData)
</script>

<template>
  <div v-loading="loading">
    <!-- 顶部操作栏 -->
    <el-card shadow="never" class="detail-header">
      <div class="header-left">
        <el-button @click="goBack" text>← 返回</el-button>
        <h3 style="margin:0 0 0 12px">{{ doc?.title || '文件详情' }}</h3>
        <el-tag v-if="doc" :type="getStatusTag(doc.businessStatus).type" size="small" style="margin-left:12px">
          {{ getStatusTag(doc.businessStatus).text }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button v-if="doc?.businessStatus==='DRAFT'" type="success" @click="handleSubmit">提交审核</el-button>
        <el-button v-if="doc?.businessStatus==='COMPLETED'" type="primary" @click="handleReReview">发起复审</el-button>
        <el-button v-if="doc?.businessStatus==='COMPLETED'" type="danger" plain @click="handleObsolete">作废</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <!-- 基本信息 -->
        <el-card shadow="never">
          <template #header>基本信息</template>
          <el-descriptions :column="2" border size="small" v-if="doc">
            <el-descriptions-item label="文件编号">{{ doc.businessNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文件名称">{{ doc.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTag(doc.businessStatus).type" size="small">{{ getStatusTag(doc.businessStatus).text }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="所属模块">DMS</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ doc.initiatorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ doc.initiatedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ doc.completedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急程度">{{ doc.urgency || 'NORMAL' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ doc.summary || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="加载中..." />
        </el-card>

        <!-- 附件管理 -->
        <el-card shadow="never" style="margin-top:16px">
          <template #header>附件管理</template>
          <FileUpload
            :record-id="id"
            business-type="DMS_DOCUMENT"
            :business-id="id"
            :disabled="doc?.businessStatus !== 'DRAFT'"
          />
        </el-card>

        <!-- 版本历史 -->
        <el-card shadow="never" style="margin-top:16px">
          <template #header>版本历史</template>
          <el-table :data="versions" size="small" v-if="versions.length > 0">
            <el-table-column prop="id" label="版本" width="80" />
            <el-table-column prop="title" label="版本说明" min-width="200" />
            <el-table-column prop="createdAt" label="创建时间" width="160" />
            <el-table-column label="操作" width="100">
              <template #default><el-button link type="primary" size="small">查看</el-button></template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无版本记录" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 审批历史 -->
        <el-card shadow="never">
          <template #header>审批流程</template>
          <el-timeline v-if="doc">
            <el-timeline-item timestamp="创建" placement="top">
              {{ doc.initiatorName || '用户' }} 创建了文件
            </el-timeline-item>
            <el-timeline-item v-if="doc.businessStatus!=='DRAFT'" timestamp="审批中" type="primary" placement="top">
              文件已提交审批
            </el-timeline-item>
            <el-timeline-item v-if="doc.businessStatus==='COMPLETED'" timestamp="生效" type="success" placement="top">
              文件已生效
            </el-timeline-item>
            <el-timeline-item v-if="doc.businessStatus==='OBSOLETED'" timestamp="作废" type="danger" placement="top">
              文件已作废
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无数据" />
        </el-card>

        <!-- 快捷操作 -->
        <el-card shadow="never" style="margin-top:16px">
          <template #header>快捷操作</template>
          <el-space direction="vertical" fill>
            <el-button v-if="doc?.businessStatus==='COMPLETED'" type="primary" plain style="width:100%" @click="notImplemented('打印功能开发中')">
              打印文件
            </el-button>
            <el-button plain style="width:100%" @click="notImplemented('分享功能开发中')">
              分享文件
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.detail-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; }
.header-actions { display: flex; gap: 8px; }
</style>
