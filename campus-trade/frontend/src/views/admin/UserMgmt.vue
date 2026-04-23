<template>
  <div class="mgmt-page">
    <div class="page-header">
      <div class="page-title-section">
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">Manage platform users and permissions</p>
      </div>
    </div>

    <div class="content-card">
      <el-table :data="users" v-loading="loading" class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <span class="role-badge" :class="row.role === 'ADMIN' ? 'role-admin' : 'role-user'">
              {{ row.role }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status ? 'status-active' : 'status-disabled'"></span>
            <span :class="row.status ? 'text-active' : 'text-disabled'">{{ row.status ? '正常' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              size="small"
              :class="row.status ? 'btn-outline-danger' : 'btn-outline-success'"
              @click="toggleStatus(row)"
            >
              {{ row.status ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" class="btn-outline-default" @click="resetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="prev,pager,next,total"
          @current-change="load"
        />
      </div>
    </div>
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
.mgmt-page {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 24px;
}

.page-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-desc {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}

.content-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  padding: 24px;
  overflow: hidden;
}

.content-card :deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(226, 232, 240, 0.6);
  border: none;
}

.content-card :deep(.el-table::before) {
  display: none;
}

.content-card :deep(.el-table th.el-table__cell) {
  background: rgba(248, 250, 252, 0.8);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  padding: 14px 12px;
}

.content-card :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  padding: 14px 12px;
  font-size: 13px;
  color: var(--text-primary);
}

.content-card :deep(.el-table__body tr:hover > td) {
  background: rgba(102, 126, 234, 0.03) !important;
}

.role-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.role-admin {
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.12) 0%, rgba(254, 225, 64, 0.12) 100%);
  color: #e11d48;
}

.role-user {
  background: rgba(102, 126, 234, 0.08);
  color: var(--primary);
}

.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.status-active {
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.4);
}

.status-disabled {
  background: #ef4444;
  box-shadow: 0 0 6px rgba(239, 68, 68, 0.3);
}

.text-active {
  color: #16a34a;
  font-weight: 500;
  font-size: 13px;
}

.text-disabled {
  color: #ef4444;
  font-weight: 500;
  font-size: 13px;
}

.btn-outline-danger {
  border: 1px solid rgba(239, 68, 68, 0.3) !important;
  color: #ef4444 !important;
  background: rgba(239, 68, 68, 0.04) !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.btn-outline-danger:hover {
  background: #ef4444 !important;
  color: #fff !important;
  border-color: #ef4444 !important;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.25) !important;
}

.btn-outline-success {
  border: 1px solid rgba(34, 197, 94, 0.3) !important;
  color: #16a34a !important;
  background: rgba(34, 197, 94, 0.04) !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.btn-outline-success:hover {
  background: #22c55e !important;
  color: #fff !important;
  border-color: #22c55e !important;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.25) !important;
}

.btn-outline-default {
  border: 1px solid rgba(148, 163, 184, 0.3) !important;
  color: var(--text-secondary) !important;
  background: rgba(148, 163, 184, 0.04) !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
  transition: all 0.25s ease !important;
}

.btn-outline-default:hover {
  background: var(--primary) !important;
  color: #fff !important;
  border-color: var(--primary) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25) !important;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(241, 245, 249, 0.8);
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
}

.pagination-wrap :deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 8px;
  font-weight: 600;
}
</style>
