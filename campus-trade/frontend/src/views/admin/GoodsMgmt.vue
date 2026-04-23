<template>
  <div class="mgmt-page">
    <div class="page-header">
      <div class="page-title-section">
        <h2 class="page-title">商品管理</h2>
        <p class="page-desc">Review and manage all listed products</p>
      </div>
    </div>

    <div class="content-card">
      <el-table :data="goods" v-loading="loading" class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <div class="cover-wrap">
              <img :src="row.coverUrl || '/default-goods.png'" class="cover-img" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">&yen;{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="goods-status" :class="'goods-status--' + row.status">
              {{ sl(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览" width="80">
          <template #default="{ row }">
            <span class="views-text">{{ row.views }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button
              size="small"
              class="btn-outline-danger"
              :disabled="row.status === 'DRAFT'"
              @click="takeDown(row)"
            >
              强制下架
            </el-button>
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
const goods=ref([]),loading=ref(false),total=ref(0),page=ref(1)
onMounted(load)
async function load(){
  loading.value=true
  try{const res=await request.get('/admin/goods',{params:{page:page.value-1,size:20}});goods.value=res.data.data?.list||[];total.value=res.data.data?.total||0}
  finally{loading.value=false}
}
async function takeDown(row){
  await ElMessageBox.confirm(`确认强制下架「${row.title}」？`,'下架确认',{type:'warning'})
  await request.put(`/admin/goods/${row.id}/take-down`);row.status='DRAFT';ElMessage.success('已强制下架')
}
const st=s=>({ON_SALE:'success',DRAFT:'danger',SOLD:'info',RESERVED:'warning'}[s]||'')
const sl=s=>({ON_SALE:'在售',DRAFT:'草稿',SOLD:'已售',RESERVED:'已预订'}[s]||s)
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

.cover-wrap {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.cover-wrap:hover .cover-img {
  transform: scale(1.1);
}

.price-text {
  font-weight: 700;
  color: #e11d48;
  font-size: 14px;
}

.views-text {
  color: var(--text-muted);
  font-size: 13px;
}

.goods-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.goods-status--ON_SALE {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.goods-status--DRAFT {
  background: rgba(239, 68, 68, 0.08);
  color: #ef4444;
}

.goods-status--SOLD {
  background: rgba(148, 163, 184, 0.12);
  color: var(--text-muted);
}

.goods-status--RESERVED {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
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

.btn-outline-danger.is-disabled {
  opacity: 0.4 !important;
  cursor: not-allowed !important;
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
