<script setup lang="ts">
import { getFormDefinitionList, publishFormDefinition, archiveFormDefinition } from '@/api/form'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

async function fetchData() { loading.value = true; try { const res = await getFormDefinitionList({ pageNum:1, pageSize:10 }); list.value = res.data.records || []; total.value = res.data.total || 0 } finally { loading.value = false } }

async function handlePublish(row: any) { await publishFormDefinition(row.id); ElMessage.success('已发布'); fetchData() }
async function handleArchive(row: any) { await archiveFormDefinition(row.id); ElMessage.success('已归档'); fetchData() }

const router = useRouter()
onMounted(fetchData)
</script>

<template>
  <div>
    <el-card>
      <div style="display:flex;justify-content:space-between;margin-bottom:16px">
        <el-form inline><el-form-item><el-input placeholder="搜索表单名称" clearable /></el-form-item><el-form-item><el-button type="primary">查询</el-button></el-form-item></el-form>
        <el-button type="primary" @click="router.push('/form/designer')">新建表单</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="formName" label="表单名称" min-width="180" />
        <el-table-column prop="formKey" label="表单Key" width="160" />
        <el-table-column prop="version" label="版本" width="60" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'ARCHIVED' ? 'info' : ''">{{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'ARCHIVED' ? '已归档' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workflowKey" label="绑定流程" width="150" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/form/designer/${row.id}`)">编辑</el-button>
            <el-button link type="success" v-if="row.status !== 'PUBLISHED'" @click="handlePublish(row)">发布</el-button>
            <el-button link type="warning" v-if="row.status === 'PUBLISHED'" @click="handleArchive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
