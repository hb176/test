<script setup lang="ts">
import { getCourse, getPlan, assignTraining, getAssignmentsByCourse, completeAssignment, getCourseStats } from '@/api/business'
import { getUserPage } from '@/api/system'
import { useRoute, useRouter } from 'vue-router'
import FileUpload from '@/components/FileUpload.vue'

const route = useRoute()
const router = useRouter()
const id = computed(() => Number(route.params.id))
const type = computed(() => route.params.type as string)
const loading = ref(false)
const item = ref<any>(null)

// 分配相关
const assignDialogVisible = ref(false)
const userList = ref<any[]>([])
const selectedUserIds = ref<number[]>([])
const userSearchKeyword = ref('')
const assignments = ref<any[]>([])
const courseStats = ref({ total: 0, completed: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res = type.value === 'course' ? await getCourse(id.value) : await getPlan(id.value)
    item.value = res.data
    if (type.value === 'course') {
      await Promise.all([fetchAssignments(), fetchStats()])
    }
  } finally { loading.value = false }
}

async function fetchAssignments() {
  try {
    const res = await getAssignmentsByCourse(id.value)
    assignments.value = res.data || []
  } catch { /* */ }
}

async function fetchStats() {
  try {
    const res = await getCourseStats(id.value)
    courseStats.value = res.data || { total: 0, completed: 0 }
  } catch { /* */ }
}

async function fetchUsers() {
  try {
    const res = await getUserPage({ pageNum: 1, pageSize: 100, keyword: userSearchKeyword.value || undefined })
    userList.value = res.data?.records || res.data || []
  } catch { /* */ }
}

function openAssign() {
  selectedUserIds.value = []
  fetchUsers()
  assignDialogVisible.value = true
}

async function handleAssign() {
  if (!selectedUserIds.value.length) { ElMessage.warning('请选择学员'); return }
  const selectedUsers = userList.value.filter((u: any) => selectedUserIds.value.includes(u.id))
  await assignTraining({
    courseId: id.value,
    userIds: selectedUserIds.value,
    userNames: selectedUsers.map((u: any) => u.userName || u.name || ''),
  })
  ElMessage.success('分配成功')
  assignDialogVisible.value = false
  fetchAssignments()
  fetchStats()
}

async function handleCompleteAssignment(assignmentId: number) {
  const { value } = await ElMessageBox.prompt('请输入培训分数（0-100）', '完成培训', {
    inputPattern: /^\d{1,3}$/,
    inputErrorMessage: '请输入0-100的数字',
    inputPlaceholder: '分数',
  })
  await completeAssignment(assignmentId, Number(value))
  ElMessage.success('已完成')
  fetchAssignments()
  fetchStats()
}

const progressPercent = computed(() => {
  if (!courseStats.value.total) return 0
  return Math.round((courseStats.value.completed / courseStats.value.total) * 100)
})

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const statusMap: Record<string, { type: TagType; text: string }> = {
  DRAFT: { type: 'info', text: '草稿' },
  APPROVING: { type: 'warning', text: '审批中' },
  COMPLETED: { type: 'success', text: '已完成' },
  IN_PROGRESS: { type: 'primary', text: '进行中' },
  CANCELLED: { type: 'danger', text: '已取消' },
}
const assignmentStatusMap: Record<string, { type: TagType; text: string }> = {
  ASSIGNED: { type: 'warning', text: '已分配' },
  IN_PROGRESS: { type: 'primary', text: '学习中' },
  COMPLETED: { type: 'success', text: '已完成' },
}

function getTag(s: string) { return statusMap[s] || { type: 'info' as TagType, text: s || '未知' } }
function getAssignTag(s: string) { return assignmentStatusMap[s] || { type: 'info' as TagType, text: s || '未知' } }
function goBack() { router.back() }

onMounted(fetchData)
</script>

<template>
  <div v-loading="loading">
    <el-card shadow="never" class="detail-header">
      <div class="header-left">
        <el-button @click="goBack" text>← 返回</el-button>
        <h3 style="margin:0 0 0 12px">{{ item?.title || (type === 'course' ? '课程详情' : '计划详情') }}</h3>
        <el-tag v-if="item" :type="getTag(item.businessStatus).type" size="small" style="margin-left:12px">{{ getTag(item.businessStatus).text }}</el-tag>
      </div>
      <div class="header-actions">
        <el-button v-if="type === 'course' && item?.businessStatus !== 'DRAFT'" type="primary" @click="openAssign">分配培训</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <!-- 基本信息 -->
        <el-card shadow="never">
          <template #header>基本信息</template>
          <el-descriptions :column="2" border size="small" v-if="item">
            <el-descriptions-item label="编号">{{ item.businessNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ item.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ type === 'course' ? '培训课程' : '培训计划' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getTag(item.businessStatus).type" size="small">{{ getTag(item.businessStatus).text }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建人">{{ item.initiatorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ item.initiatedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ item.summary || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 培训内容/材料 -->
        <el-card shadow="never" style="margin-top:16px">
          <template #header>培训材料</template>
          <FileUpload :record-id="id" business-type="TMS_COURSE" :business-id="id" />
        </el-card>

        <!-- 分配列表（仅课程） -->
        <el-card v-if="type === 'course'" shadow="never" style="margin-top:16px">
          <template #header>
            <span>培训分配</span>
            <el-tag v-if="assignments.length" size="small" style="margin-left:8px">{{ assignments.length }}人</el-tag>
          </template>
          <el-table :data="assignments" size="small" v-if="assignments.length">
            <el-table-column prop="userName" label="学员" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getAssignTag(row.status).type" size="small">{{ getAssignTag(row.status).text }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="分数" width="80" />
            <el-table-column prop="completedAt" label="完成时间" width="160" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button v-if="row.status === 'ASSIGNED'" link type="success" size="small" @click="handleCompleteAssignment(row.id)">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无分配记录" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 培训进度 -->
        <el-card shadow="never">
          <template #header>培训进度</template>
          <div style="text-align:center;padding:20px">
            <el-progress type="circle" :percentage="progressPercent" :width="120" />
            <p style="margin-top:12px;color:#999">
              {{ courseStats.completed }}/{{ courseStats.total }} 人已完成
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配培训" width="500px">
      <el-form label-width="60px">
        <el-form-item label="搜索">
          <el-input v-model="userSearchKeyword" placeholder="搜索用户" clearable @keyup.enter="fetchUsers" />
        </el-form-item>
        <el-form-item label="学员">
          <el-checkbox-group v-model="selectedUserIds">
            <el-checkbox v-for="u in userList" :key="u.id" :value="u.id" :label="u.userName || u.name" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; }
.header-actions { display: flex; gap: 8px; }
</style>
