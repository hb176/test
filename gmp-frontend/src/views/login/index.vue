<script setup lang="ts">
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const form = reactive({
  username: 'admin',
  password: '',
  captchaKey: '',
  captchaCode: '',
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const loading = ref(false)

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.loginAction(form.username, form.password, form.captchaKey, form.captchaCode)
    ElMessage.success('登录成功')
  } catch {
    // 错误已在 request 拦截器中统一提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">GMP 可配置表单与流程平台</h2>
      <p class="login-subtitle">基于可配置表单+流程的质量管理套件</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <p class="login-hint">默认管理员: admin / admin123</p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex; align-items: center; justify-content: center;
  height: 100vh; background: linear-gradient(135deg, #1a365d 0%, #2d5a87 100%);
}
.login-card {
  width: 420px; padding: 40px; background: #fff; border-radius: 8px; box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}
.login-title { text-align: center; margin-bottom: 4px; color: #1a365d; }
.login-subtitle { text-align: center; color: #909399; margin-bottom: 32px; font-size: 13px; }
.login-hint { text-align: center; color: #c0c4cc; font-size: 12px; margin-top: 16px; }
</style>
