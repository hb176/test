<script setup lang="ts">
import { ref, watch } from 'vue'
import { uploadFile, getFilesByRecord, deleteFileRecord } from '@/api/file'
import type { UploadFile, UploadRawFile } from 'element-plus'

interface FileItem {
  id?: number
  fileName: string
  fileSize?: number
  bucketName?: string
  objectName?: string
  fileId?: number
}

const props = withDefaults(defineProps<{
  businessType?: string
  businessId?: number
  recordId?: number
  multiple?: boolean
  limit?: number
  disabled?: boolean
}>(), { multiple: true, limit: 5, disabled: false })

const emit = defineEmits<{ change: [files: FileItem[]] }>()
const fileList = ref<FileItem[]>([])
const uploading = ref(false)

async function loadFiles() {
  if (!props.recordId) return
  try {
    const res = await getFilesByRecord(props.recordId)
    fileList.value = (res.data || []).map((f: any) => ({
      id: f.id,
      fileName: f.originalFileName || f.name,
      fileSize: f.fileSize,
      bucketName: f.bucket,
      objectName: f.storagePath,
      fileId: f.id,
    }))
  } catch { /* ignore */ }
}

async function handleUpload(rawFile: UploadRawFile) {
  if (!rawFile) return
  uploading.value = true
  try {
    const res = await uploadFile(rawFile, props.businessType, props.businessId)
    const data = res.data || {}
    fileList.value.push({
      id: data.fileId,
      fileName: data.fileName,
      fileSize: data.fileSize,
      bucketName: data.bucketName,
      objectName: data.objectName,
      fileId: data.fileId,
    })
    emit('change', fileList.value)
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

function beforeUpload(rawFile: UploadRawFile) {
  handleUpload(rawFile)
  return false // 阻止 el-upload 自动上传
}

async function handleRemove(item: FileItem) {
  try {
    await ElMessageBox.confirm(`确认删除文件 "${item.fileName}"？`, '删除确认', { type: 'warning' })
    if (item.id) {
      await deleteFileRecord(item.id)
    }
    fileList.value = fileList.value.filter(f => f !== item)
    emit('change', fileList.value)
    ElMessage.success('已删除')
  } catch { /* cancelled */ }
}

function handleDownload(item: FileItem) {
  if (!item.bucketName || !item.objectName) return
  const url = `/api/file/download/${item.bucketName}/${item.objectName}?fileName=${encodeURIComponent(item.fileName)}`
  const a = document.createElement('a')
  a.href = url
  a.download = item.fileName
  a.click()
}

function formatSize(bytes?: number) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

watch(() => props.recordId, (id) => { if (id) loadFiles() }, { immediate: true })
</script>

<template>
  <div>
    <el-upload
      :multiple="multiple"
      :limit="limit"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :disabled="disabled || uploading"
      accept="*"
      style="display:inline-block"
    >
      <el-button :loading="uploading" :disabled="disabled" size="small" type="primary">上传文件</el-button>
    </el-upload>
    <div v-if="fileList.length" style="margin-top:8px">
      <div v-for="(f, i) in fileList" :key="i" class="file-item">
        <el-icon style="color:#409EFF;cursor:pointer" @click="handleDownload(f)"><Document /></el-icon>
        <span class="file-name" @click="handleDownload(f)">{{ f.fileName }}</span>
        <span class="file-size">{{ formatSize(f.fileSize) }}</span>
        <el-button v-if="!disabled" link type="danger" size="small" @click="handleRemove(f)">删除</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.file-item { display: flex; align-items: center; gap: 8px; padding: 4px 0; font-size: 13px; }
.file-name { color: #409EFF; cursor: pointer; }
.file-name:hover { text-decoration: underline; }
.file-size { color: #909399; font-size: 12px; }
</style>
