<script setup lang="ts">const router = useRouter()</script>
<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h3>培训管理 (TMS)</h3>
      <el-space><el-button type="primary">新建课程</el-button><el-button>新建计划</el-button></el-space>
    </div>
    <el-empty description="培训矩阵管理 - 由可配置表单+流程引擎驱动" />
  </el-card>
</template>
