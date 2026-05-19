<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTokenExpireConfig, updateTokenExpireConfig } from '@/api/auth'

const expireMinutes = ref(30)
const loading = ref(false)
const saving = ref(false)

async function loadConfig() {
  loading.value = true
  try {
    const res = await getTokenExpireConfig()
    expireMinutes.value = res.data.expireMinutes
  } catch {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (expireMinutes.value < 1 || expireMinutes.value > 1440) {
    ElMessage.warning('过期时间必须在1-1440分钟之间')
    return
  }
  saving.value = true
  try {
    await updateTokenExpireConfig(expireMinutes.value)
    ElMessage.success('保存成功，新登录的用户将生效')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<template>
  <div class="settings-page">
    <el-card header="系统设置" shadow="never">
      <el-form label-width="160px" v-loading="loading">
        <el-form-item label="登录过期时间">
          <el-input-number
            v-model="expireMinutes"
            :min="1"
            :max="1440"
            :step="5"
          />
          <span class="unit-text">分钟</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.settings-page { max-width: 600px; }
.unit-text { margin-left: 10px; color: #909399; font-size: 14px; }
</style>
