<template>
  <div>
    <h2 class="page-title">举报管理</h2>
    <el-radio-group v-model="filterStatus" @change="load" style="margin-bottom:16px">
      <el-radio-button label="">全部</el-radio-button>
      <el-radio-button label="PENDING">待处理</el-radio-button>
      <el-radio-button label="HANDLED">已处理</el-radio-button>
      <el-radio-button label="DISMISSED">已驳回</el-radio-button>
    </el-radio-group>
    <el-table :data="reports" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="reporterName" label="举报人" width="120"/>
      <el-table-column prop="goodsTitle" label="被举报商品" show-overflow-tooltip/>
      <el-table-column prop="reason" label="举报原因" show-overflow-tooltip/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}"><el-tag :type="st(row.status)" size="small">{{sl(row.status)}}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <template v-if="row.status==='PENDING'">
            <el-button size="small" type="danger" @click="handle(row,'handle')">处理（下架）</el-button>
            <el-button size="small" @click="handle(row,'dismiss')">驳回</el-button>
          </template>
          <span v-else class="handled-text">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="20" :total="total"
      layout="prev,pager,next,total" @current-change="load"/>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const reports=ref([]),loading=ref(false),total=ref(0),page=ref(1),filterStatus=ref('')
onMounted(load)
async function load(){
  loading.value=true
  try{
    const res=await request.get('/api/admin/reports',{params:{status:filterStatus.value||undefined,page:page.value-1,size:20}})
    reports.value=res.data.data?.list||[];total.value=res.data.data?.total||0
  }finally{loading.value=false}
}
async function handle(row,action){
  await request.put(`/api/admin/reports/${row.id}/handle`,null,{params:{action}})
  ElMessage.success('处理成功');load()
}
const st=s=>({PENDING:'warning',HANDLED:'success',DISMISSED:'info'}[s]||'')
const sl=s=>({PENDING:'待处理',HANDLED:'已处理',DISMISSED:'已驳回'}[s]||s)
</script>
<style scoped>
.page-title{font-size:22px;font-weight:700;margin-bottom:20px}
.pagination{margin-top:16px;justify-content:flex-end}
.handled-text{font-size:13px;color:#999}
</style>
