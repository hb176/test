<script setup lang="ts">const router = useRouter()</script>
<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3>文件管理 (DMS)</h3>
      <el-button type="primary">新建文件</el-button>
    </div>
    <el-empty description="文件生命周期管理 - 由可配置表单+流程引擎驱动" />
  </el-card>
</template>
