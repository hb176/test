<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTokenExpireConfig, updateTokenExpireConfig } from '@/api/auth'
import { getConfigList, getConfigListByCategory, updateConfig, updatePasswordPolicy } from '@/api/system'

const activeTab = ref('token')
const loading = ref(false)
const saving = ref(false)

// 站点配置
const siteConfigs = ref<any[]>([])

// Token 配置
const expireMinutes = ref(30)

// 密码策略配置
const passwordPolicy = reactive({
  level: 'medium',           // low=低, medium=中, high=高
  minLength: 8,              // 最小长度
  maxLength: 32,             // 最大长度
  expireDays: 90,            // 过期天数(0=不过期)
  historyCount: 5            // 历史密码检查数量
})

const levelDescriptions = {
  low: '仅要求数字',
  medium: '要求数字 + 英文字母',
  high: '要求数字 + 英文字母 + 特殊字符'
}

async function loadSiteConfigs() {
  try {
    const res = await getConfigListByCategory('system')
    siteConfigs.value = res.data || []
  } catch { /* */ }
}

async function handleSaveSiteConfigs() {
  saving.value = true
  try {
    for (const c of siteConfigs.value) {
      await updateConfig({ id: c.id, configKey: c.configKey, configValue: c.configValue, category: c.category })
    }
    ElMessage.success('站点配置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function loadConfig() {
  loading.value = true
  try {
    // 加载 Token 配置
    const tokenRes = await getTokenExpireConfig()
    expireMinutes.value = tokenRes.data?.expireMinutes || 30

    // 加载密码策略配置
    try {
      const configRes = await getConfigList()
      const configs = configRes.data || []
      const levelConfig = configs.find((c: any) => c.configKey === 'password.level')
      const minLenConfig = configs.find((c: any) => c.configKey === 'password.min.length')
      const maxLenConfig = configs.find((c: any) => c.configKey === 'password.max.length')
      const expireConfig = configs.find((c: any) => c.configKey === 'password.expire.days')
      const historyConfig = configs.find((c: any) => c.configKey === 'password.history.count')

      if (levelConfig) passwordPolicy.level = levelConfig.configValue || 'medium'
      if (minLenConfig) passwordPolicy.minLength = parseInt(minLenConfig.configValue) || 8
      if (maxLenConfig) passwordPolicy.maxLength = parseInt(maxLenConfig.configValue) || 32
      if (expireConfig) passwordPolicy.expireDays = parseInt(expireConfig.configValue) || 90
      if (historyConfig) passwordPolicy.historyCount = parseInt(historyConfig.configValue) || 5
    } catch {
      // 密码策略配置加载失败使用默认值
    }
  } catch {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSaveToken() {
  if (expireMinutes.value < 1 || expireMinutes.value > 1440) {
    ElMessage.warning('过期时间必须在1-1440分钟之间')
    return
  }
  saving.value = true
  try {
    await updateTokenExpireConfig(expireMinutes.value)
    ElMessage.success('Token配置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleSavePassword() {
  if (passwordPolicy.minLength < 6) {
    ElMessage.warning('最小长度不能少于6位')
    return
  }
  if (passwordPolicy.maxLength < passwordPolicy.minLength) {
    ElMessage.warning('最大长度不能小于最小长度')
    return
  }
  saving.value = true
  try {
    await updatePasswordPolicy({
      level: passwordPolicy.level,
      minLength: String(passwordPolicy.minLength),
      maxLength: String(passwordPolicy.maxLength),
      expireDays: String(passwordPolicy.expireDays),
      historyCount: String(passwordPolicy.historyCount)
    })
    ElMessage.success('密码策略已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadConfig(); loadSiteConfigs() })
</script>

<template>
  <div class="settings-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="站点配置" name="site" />
      <el-tab-pane label="登录配置" name="token" />
      <el-tab-pane label="密码策略" name="password" />
    </el-tabs>

    <!-- 站点配置 -->
    <el-card v-show="activeTab === 'site'" shadow="never" style="margin-top:16px">
      <el-form label-width="160px" v-loading="loading">
        <el-form-item v-for="c in siteConfigs" :key="c.id" :label="c.description || c.configKey">
          <el-input v-model="c.configValue" :placeholder="c.description" />
        </el-form-item>
        <el-empty v-if="!siteConfigs.length && !loading" description="暂无站点配置项" />
        <el-form-item v-if="siteConfigs.length">
          <el-button type="primary" :loading="saving" @click="handleSaveSiteConfigs">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Token 配置 -->
    <el-card v-show="activeTab === 'token'" shadow="never" style="margin-top:16px">
      <el-form label-width="160px" v-loading="loading">
        <el-form-item label="登录过期时间">
          <el-input-number v-model="expireMinutes" :min="1" :max="1440" :step="5" />
          <span class="unit-text">分钟</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSaveToken">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 密码策略配置 -->
    <el-card v-show="activeTab === 'password'" shadow="never" style="margin-top:16px">
      <el-form label-width="160px" v-loading="loading">
        <el-form-item label="密码强度等级">
          <el-radio-group v-model="passwordPolicy.level">
            <el-radio value="low">
              <span>低</span>
              <el-tag size="small" type="info" style="margin-left:8px">{{ levelDescriptions.low }}</el-tag>
            </el-radio>
            <el-radio value="medium">
              <span>中</span>
              <el-tag size="small" type="warning" style="margin-left:8px">{{ levelDescriptions.medium }}</el-tag>
            </el-radio>
            <el-radio value="high">
              <span>高</span>
              <el-tag size="small" type="danger" style="margin-left:8px">{{ levelDescriptions.high }}</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider />

        <el-form-item label="密码最小长度">
          <el-input-number v-model="passwordPolicy.minLength" :min="6" :max="32" :step="1" />
          <span class="unit-text">位</span>
        </el-form-item>

        <el-form-item label="密码最大长度">
          <el-input-number v-model="passwordPolicy.maxLength" :min="8" :max="64" :step="1" />
          <span class="unit-text">位</span>
        </el-form-item>

        <el-form-item label="密码过期时间">
          <el-input-number v-model="passwordPolicy.expireDays" :min="0" :max="365" :step="1" />
          <span class="unit-text">天（0表示不过期）</span>
        </el-form-item>

        <el-form-item label="历史密码检查">
          <el-input-number v-model="passwordPolicy.historyCount" :min="0" :max="20" :step="1" />
          <span class="unit-text">次（0表示不检查）</span>
        </el-form-item>

        <el-divider />

        <el-form-item label="当前等级要求">
          <el-descriptions :column="1" border size="small" style="max-width:500px">
            <el-descriptions-item label="等级">{{ { low: '低', medium: '中', high: '高' }[passwordPolicy.level] }}</el-descriptions-item>
            <el-descriptions-item label="要求数字">是</el-descriptions-item>
            <el-descriptions-item label="要求字母">{{ passwordPolicy.level !== 'low' ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="要求特殊字符">{{ passwordPolicy.level === 'high' ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="最小长度">{{ passwordPolicy.minLength }} 位</el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSavePassword">保存密码策略</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.settings-page { max-width: 700px; }
.unit-text { margin-left: 10px; color: #909399; font-size: 14px; }
</style>
