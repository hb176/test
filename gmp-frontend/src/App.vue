<script setup lang="ts">
/**
 * GMP系统根组件 - 路由出口
 */
</script>

<template>
  <router-view />
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

/* 退出确认弹窗样式 */
.logout-confirm-box {
  border-radius: 10px;
  padding-bottom: 8px;
}
.logout-confirm-box .el-message-box__header {
  background: linear-gradient(135deg, #1a365d 0%, #2d5a87 100%);
  padding: 18px 24px;
  border-radius: 10px 10px 0 0;
}
.logout-confirm-box .el-message-box__title {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
}
.logout-confirm-box .el-message-box__headerbtn {
  top: 14px;
  right: 16px;
}
.logout-confirm-box .el-message-box__headerbtn .el-icon {
  color: rgba(255,255,255,0.7);
  font-size: 20px;
}
.logout-confirm-box .el-message-box__headerbtn:hover .el-icon {
  color: #fff;
}
.logout-confirm-box .el-message-box__content {
  padding: 28px 24px 12px;
  text-align: center;
}
.logout-confirm-box .el-message-box__btns {
  padding: 8px 24px 18px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
.logout-confirm-btn {
  background: linear-gradient(135deg, #1a365d 0%, #2d5a87 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 6px !important;
  font-weight: 500;
}
.logout-confirm-btn:hover {
  background: linear-gradient(135deg, #f56c6c 0%, #e04b4b 100%) !important;
}
.logout-confirm-btn:active {
  background: linear-gradient(135deg, #d9534f 0%, #c9302c 100%) !important;
}
</style>
