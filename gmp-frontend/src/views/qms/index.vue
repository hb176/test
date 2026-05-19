<script setup lang="ts">
/**
 * QMS通用业务页面 - 偏差/CAPA/变更/审计等
 * 通过路由meta或URL参数区分子业务类型
 * 所有QMS子模块共享此页面框架，通过表单+流程引擎驱动
 */
const route = useRoute()
const router = useRouter()
const pageTitle = computed(() => route.meta.title as string || 'QMS')

function handleCreate() {
  ElMessage.info('创建功能由可配置表单引擎驱动，打开对应的表单')
}
</script>
<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3>{{ pageTitle }}</h3>
      <el-button type="primary" @click="handleCreate">新建</el-button>
    </div>
    <el-empty :description="pageTitle + ' - 数据由可配置表单+流程引擎驱动'" />
  </el-card>
</template>
