<script setup lang="ts">
import { getLogPage } from '@/api/system'

const loading = ref(false), list = ref<any[]>([]), total = ref(0)
const pageNum = ref(1), pageSize = ref(10)
const operType = ref(''), keyword = ref('')

async function fetch() {
  loading.value = true
  try {
    const r = await getLogPage({ pageNum: pageNum.value, pageSize: pageSize.value, operType: operType.value || undefined, keyword: keyword.value || undefined })
    list.value = r.data?.records || []; total.value = r.data?.total || 0
  } finally { loading.value = false }
}
onMounted(fetch)
</script>

<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <div style="display:flex;gap:12px">
        <el-select v-model="operType" placeholder="操作类型" clearable style="width:120px" @change="fetch">
          <el-option label="登录" value="LOGIN" /><el-option label="查询" value="QUERY" /><el-option label="新增" value="ADD" /><el-option label="修改" value="UPDATE" /><el-option label="删除" value="DELETE" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索描述" clearable style="width:200px" @keyup.enter="fetch" />
        <el-button @click="fetch">查询</el-button>
      </div>
    </div>
    <el-table :data="list" v-loading="loading" border stripe size="small">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="operType" label="操作类型" width="90" align="center">
        <template #default="{ row }"><el-tag :type="row.operType==='ADD'?'success':row.operType==='DELETE'?'danger':row.operType==='UPDATE'?'warning':''" size="small">{{ row.operType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="description" label="操作描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="requestUrl" label="请求URL" width="200" show-overflow-tooltip />
      <el-table-column prop="requestMethod" label="请求方法" width="80" align="center" />
      <el-table-column prop="costTime" label="耗时(ms)" width="90" align="center" />
      <el-table-column prop="result" label="结果" width="90" align="center">
        <template #default="{ row }"><el-tag :type="row.result==='SUCCESS'?'success':'danger'" size="small">{{ row.result==='SUCCESS'?'成功':'失败' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="operUserName" label="操作人" width="100" />
      <el-table-column prop="operIp" label="IP" width="130" />
      <el-table-column prop="createTime" label="时间" width="160" />
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next" :page-sizes="[10,20,50]" style="margin-top:16px;justify-content:flex-end" @current-change="fetch" @size-change="pageSize=$event;pageNum=1;fetch()" />
  </el-card>
</template>
