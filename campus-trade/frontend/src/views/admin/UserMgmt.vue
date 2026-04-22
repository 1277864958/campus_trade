<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <el-table :data="users" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="username" label="用户名" width="130"/>
      <el-table-column prop="nickname" label="昵称"/>
      <el-table-column prop="phone" label="手机号"/>
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{row}"><el-tag :type="row.role==='ADMIN'?'danger':''">{{row.role}}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{row.status?'正常':'禁用'}}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" :type="row.status?'danger':'success'" @click="toggleStatus(row)">
            {{row.status?'禁用':'启用'}}
          </el-button>
          <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="20" :total="total"
      layout="prev,pager,next,total" @current-change="load"/>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
const users=ref([]),loading=ref(false),total=ref(0),page=ref(1)
onMounted(load)
async function load(){
  loading.value=true
  try{const res=await request.get('/admin/users',{params:{page:page.value-1,size:20}});users.value=res.data.data?.list||[];total.value=res.data.data?.total||0}
  finally{loading.value=false}
}
async function toggleStatus(row){
  const ns=row.status?0:1
  await request.put(`/admin/users/${row.id}/status`,null,{params:{status:ns}})
  row.status=ns;ElMessage.success(ns?'已启用':'已禁用')
}
async function resetPwd(row){
  const{value:pwd}=await ElMessageBox.prompt('请输入新密码（至少6位）','重置密码',{inputType:'password',inputValidator:v=>v&&v.length>=6||'密码至少6位'})
  await request.put(`/admin/users/${row.id}/reset-password`,null,{params:{newPassword:pwd}})
  ElMessage.success('密码已重置')
}
</script>
<style scoped>
.page-title{font-size:22px;font-weight:700;margin-bottom:20px}
.pagination{margin-top:16px;justify-content:flex-end}
</style>
