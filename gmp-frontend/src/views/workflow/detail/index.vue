<script setup lang="ts">
/**
 * 流程详情页 - 审批页面核心
 *
 * 页面布局（左-右分栏）：
 * ┌──────────────┬─────────────────┐
 * │  表单数据    │   流程信息      │
 * │  (动态渲染)  │   审批轨迹      │
 * │              │   BPMN流程图    │
 * │              │   审批操作按钮  │
 * └──────────────┴─────────────────┘
 */
import { getInstanceDetail, completeTask, delegateTask, transferTask, addSign, getProcessTrace } from '@/api/workflow'
import { getFormDataById } from '@/api/form'
import { saveSignature } from '@/api/system'
import SignaturePad from '@/components/SignaturePad.vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const instanceId = Number(route.params.id)
const loading = ref(true)
const instance = ref<any>({})
const formData = ref<any>({})
const traceList = ref<any[]>([])
const activeTab = ref('form')

// 审批对话框状态
const approveDialogVisible = ref(false)
const approveForm = reactive({
  approveResult: 'APPROVED',  // APPROVED / REJECTED
  comment: '',
  delegateUserId: '',
  transferUserId: '',
})
const submitting = ref(false)
const signaturePadRef = ref<InstanceType<typeof SignaturePad> | null>(null)

onMounted(async () => {
  try {
    const [instRes, traceRes] = await Promise.all([
      getInstanceDetail(instanceId),
      getProcessTrace(instanceId),
    ])
    instance.value = instRes.data || {}
    traceList.value = traceRes.data || []

    // 加载关联的表单数据
    if (instance.value.formDataId) {
      const formRes = await getFormDataById(instance.value.formDataId)
      formData.value = formRes.data || {}
    }
  } catch (e) {
    ElMessage.error('加载流程详情失败')
  } finally {
    loading.value = false
  }
})

/** 提交审批（通过/驳回） */
async function handleSubmitApproval() {
  // 非委派操作需要签名
  if (approveForm.approveResult !== 'DELEGATED') {
    if (!signaturePadRef.value || signaturePadRef.value.isEmpty()) {
      ElMessage.warning('请先签名')
      return
    }
  }
  submitting.value = true
  try {
    const currentTask = instance.value.currentTasks?.[0]
    if (!currentTask) { ElMessage.error('未找到当前任务'); return }

    // 保存签名
    if (approveForm.approveResult !== 'DELEGATED' && signaturePadRef.value) {
      await saveSignature({
        taskId: String(currentTask.taskId),
        processInstanceId: String(instanceId),
        userId: 0,
        userName: '',
        signatureData: signaturePadRef.value.toDataURL(),
      })
    }

    await completeTask(currentTask.taskId, {
      approveResult: approveForm.approveResult,
      comment: approveForm.comment,
    })
    ElMessage.success(approveForm.approveResult === 'APPROVED' ? '审批通过' : '已驳回')
    router.back()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
    approveDialogVisible.value = false
  }
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''
}
</script>

<template>
  <div v-loading="loading">
    <!-- 流程标题栏 -->
    <el-card class="process-header">
      <div class="header-info">
        <h3>{{ instance.processTitle || '流程详情' }}</h3>
        <el-tag :type="instance.processStatus === 'APPROVING' ? 'warning' : instance.processStatus === 'COMPLETED' ? 'success' : 'danger'">
          {{ instance.processStatus === 'APPROVING' ? '审批中' : instance.processStatus === 'COMPLETED' ? '已完成' : instance.processStatus }}
        </el-tag>
        <span class="meta-text">发起人: {{ instance.startUserName }}</span>
        <span class="meta-text">发起时间: {{ formatTime(instance.startTime) }}</span>
      </div>
    </el-card>

    <!-- 左右分栏 -->
    <el-row :gutter="16" style="margin-top:12px">
      <!-- 左侧：表单数据 -->
      <el-col :span="14">
        <el-card header="表单数据">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="表单内容" name="form">
              <el-descriptions :column="1" border v-if="formData.formData">
                <!-- 简化的JSON展示，实际应用中使用FormRender组件渲染 -->
                <pre style="max-height:500px;overflow:auto;background:#f5f7fa;padding:12px;border-radius:4px">{{ JSON.stringify(JSON.parse(formData.formData || '{}'), null, 2) }}</pre>
              </el-descriptions>
              <el-empty v-else description="无表单数据" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 右侧：流程信息 + 审批操作 -->
      <el-col :span="10">
        <!-- 审批轨迹 -->
        <el-card header="审批轨迹">
          <el-timeline v-if="traceList.length">
            <el-timeline-item
              v-for="item in traceList"
              :key="item.id"
              :timestamp="formatTime(item.startTime)"
              :type="item.endTime ? 'success' : 'warning'"
            >
              <div><strong>{{ item.activityName }}</strong></div>
              <div style="font-size:12px;color:#909399" v-if="item.assignee">
                办理人: {{ item.assignee }}
                <span v-if="item.comment"> | 意见: {{ item.comment }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审批记录" />
        </el-card>

        <!-- 当前审批操作 -->
        <el-card header="审批操作" style="margin-top:12px" v-if="instance.processStatus === 'APPROVING'">
          <el-space direction="vertical" style="width:100%">
            <el-button type="success" style="width:100%" @click="approveForm.approveResult = 'APPROVED'; approveDialogVisible = true">
              <el-icon><Check /></el-icon> 同意
            </el-button>
            <el-button type="danger" style="width:100%" @click="approveForm.approveResult = 'REJECTED'; approveDialogVisible = true">
              <el-icon><Close /></el-icon> 驳回
            </el-button>
            <el-divider />
            <el-button style="width:100%" @click="approveForm.approveResult = 'DELEGATED'; approveDialogVisible = true">
              <el-icon><Share /></el-icon> 委派
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审批意见弹窗 -->
    <el-dialog v-model="approveDialogVisible" :title="approveForm.approveResult === 'APPROVED' ? '同意 - 填写审批意见' : approveForm.approveResult === 'REJECTED' ? '驳回 - 填写驳回原因' : '填写委派信息'" width="500px">
      <el-form :model="approveForm">
        <el-form-item label="审批意见" v-if="approveForm.approveResult !== 'DELEGATED'">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
        <el-form-item label="委派人" v-if="approveForm.approveResult === 'DELEGATED'">
          <el-select v-model="approveForm.delegateUserId" filterable placeholder="请选择委派人" style="width:100%">
            <el-option label="张三" value="1001" />
            <el-option label="李四" value="1002" />
          </el-select>
        </el-form-item>
        <el-form-item label="签名" v-if="approveForm.approveResult !== 'DELEGATED'">
          <SignaturePad ref="signaturePadRef" :width="440" :height="150" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitApproval">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.process-header { margin-bottom: 0; }
.header-info { display: flex; align-items: center; gap: 16px; }
.header-info h3 { margin: 0; }
.meta-text { color: #909399; font-size: 13px; }
</style>
