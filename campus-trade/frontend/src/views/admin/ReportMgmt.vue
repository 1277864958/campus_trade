<template>
  <div class="mgmt-page">
    <div class="page-header">
      <div class="page-title-section">
        <h2 class="page-title">举报管理</h2>
        <p class="page-desc">Review and handle user reports</p>
      </div>
    </div>

    <div class="content-card">
      <div class="filter-bar">
        <el-radio-group v-model="filterStatus" @change="load" class="modern-radio-group">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待处理</el-radio-button>
          <el-radio-button label="HANDLED">已处理</el-radio-button>
          <el-radio-button label="DISMISSED">已驳回</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="reports" v-loading="loading" class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="goodsTitle" label="被举报商品" show-overflow-tooltip />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="report-status" :class="'report-status--' + row.status">
              {{ sl(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" class="btn-outline-danger" @click="handle(row, 'handle')">
                处理（下架）
              </el-button>
              <el-button size="small" class="btn-outline-default" @click="handle(row, 'dismiss')">
                驳回
              </el-button>
            </template>
            <span v-else class="handled-text">
              <svg viewBox="0 0 24 24" fill="none" width="14" height="14" style="vertical-align: -2px; margin-right: 4px;">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              已处理
            </span>
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
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const reports=ref([]),loading=ref(false),total=ref(0),page=ref(1),filterStatus=ref('')
onMounted(load)
async function load(){
  loading.value=true
  try{
    const res=await request.get('/admin/reports',{params:{status:filterStatus.value||undefined,page:page.value-1,size:20}})
    reports.value=res.data.data?.list||[];total.value=res.data.data?.total||0
  }finally{loading.value=false}
}
async function handle(row,action){
  await request.put(`/admin/reports/${row.id}/handle`,null,{params:{action}})
  ElMessage.success('处理成功');load()
}
const st=s=>({PENDING:'warning',HANDLED:'success',DISMISSED:'info'}[s]||'')
const sl=s=>({PENDING:'待处理',HANDLED:'已处理',DISMISSED:'已驳回'}[s]||s)
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

.filter-bar {
  margin-bottom: 20px;
}

.modern-radio-group :deep(.el-radio-button__inner) {
  border: none;
  background: rgba(241, 245, 249, 0.8);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 13px;
  padding: 8px 18px;
  transition: all 0.25s ease;
}

.modern-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 10px 0 0 10px;
}

.modern-radio-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 10px 10px 0;
}

.modern-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  border-color: transparent;
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

.report-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.report-status--PENDING {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.report-status--HANDLED {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.report-status--DISMISSED {
  background: rgba(148, 163, 184, 0.12);
  color: var(--text-muted);
}

.handled-text {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
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
