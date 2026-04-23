<template>
  <div class="mygoods-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的商品</h2>
        <p class="page-subtitle">管理您发布的所有商品</p>
      </div>
      <button class="publish-btn" @click="$router.push('/publish')">
        <el-icon><Plus /></el-icon>
        <span>发布商品</span>
      </button>
    </div>

    <div class="tabs-wrapper">
      <el-tabs v-model="activeStatus" @tab-change="loadGoods">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="在售" name="ON_SALE" />
        <el-tab-pane label="草稿" name="DRAFT" />
        <el-tab-pane label="已售" name="SOLD" />
      </el-tabs>
    </div>

    <div v-loading="loading" class="goods-grid">
      <div v-for="g in goods" :key="g.id" class="goods-card">
        <div class="card-img-wrapper" @click="$router.push(`/goods/${g.id}`)">
          <img :src="g.coverUrl || '/default-goods.png'" class="card-img" />
          <div class="img-overlay">
            <span class="view-text">查看详情</span>
          </div>
          <span class="status-badge" :class="'status-' + g.status">{{ sl(g.status) }}</span>
        </div>
        <div class="card-body">
          <p class="card-title">{{ g.title }}</p>
          <p class="card-price">¥{{ g.price }}</p>
          <div class="card-actions">
            <button class="action-btn action-edit" @click="$router.push(`/publish/${g.id}`)">编辑</button>
            <button v-if="g.status === 'DRAFT'" class="action-btn action-sale" @click="onSale(g)">上架</button>
            <button v-if="g.status === 'ON_SALE'" class="action-btn action-down" @click="takeDown(g)">下架</button>
            <button class="action-btn action-delete" @click="del(g)">删除</button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !goods.length" description="暂无商品" class="empty-state" />
    </div>

    <div class="pagination-wrapper" v-if="total > 12">
      <el-pagination
        v-model:current-page="page"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadGoods"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'
const activeStatus=ref(''), loading=ref(false), goods=ref([]), total=ref(0), page=ref(1)
onMounted(loadGoods)
async function loadGoods(){
  loading.value=true
  try{
    const res=await request.get('/goods/my',{params:{status:activeStatus.value||undefined,page:page.value-1,size:12}})
    goods.value=res.data.data?.list||[]; total.value=res.data.data?.total||0
  }finally{loading.value=false}
}
async function onSale(g){ await request.put(`/goods/${g.id}/on-sale`); ElMessage.success('已上架'); loadGoods() }
async function takeDown(g){ await request.put(`/goods/${g.id}/take-down`); ElMessage.success('已下架'); loadGoods() }
async function del(g){
  await ElMessageBox.confirm('确认删除该商品？','删除',{type:'warning'})
  await request.delete(`/goods/${g.id}`); ElMessage.success('已删除'); loadGoods()
}
const st=s=>({ON_SALE:'success',DRAFT:'danger',SOLD:'info',RESERVED:'warning'}[s]||'')
const sl=s=>({ON_SALE:'在售',DRAFT:'草稿',SOLD:'已售',RESERVED:'已预订'}[s]||s)
</script>

<style scoped>
.mygoods-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.55);
}

.publish-btn:active {
  transform: translateY(0);
}

.tabs-wrapper {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 4px 16px;
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.tabs-wrapper :deep(.el-tabs__header) {
  margin: 0;
}

.tabs-wrapper :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tabs-wrapper :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: 3px;
}

.tabs-wrapper :deep(.el-tabs__item) {
  font-weight: 600;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.tabs-wrapper :deep(.el-tabs__item.is-active) {
  color: var(--primary);
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.goods-card {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.card-img-wrapper {
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.card-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-img-wrapper:hover .card-img {
  transform: scale(1.08);
}

.img-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.35s ease;
}

.card-img-wrapper:hover .img-overlay {
  opacity: 1;
}

.view-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 20px;
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  backdrop-filter: blur(4px);
}

.status-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.status-ON_SALE {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.status-DRAFT {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.status-SOLD {
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  color: var(--text-primary);
}

.status-RESERVED {
  background: linear-gradient(135deg, #f6d365, #fda085);
}

.card-body {
  padding: 14px 16px 16px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-price {
  font-size: 20px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 12px 0;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.25s ease;
}

.action-edit {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary);
}

.action-edit:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.action-sale {
  background: rgba(67, 233, 123, 0.12);
  color: #0d9e4f;
}

.action-sale:hover {
  background: rgba(67, 233, 123, 0.25);
  transform: translateY(-1px);
}

.action-down {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.action-down:hover {
  background: rgba(245, 158, 11, 0.2);
  transform: translateY(-1px);
}

.action-delete {
  background: rgba(239, 68, 68, 0.08);
  color: #ef4444;
}

.action-delete:hover {
  background: rgba(239, 68, 68, 0.18);
  transform: translateY(-1px);
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px;
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.pagination-wrapper :deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 8px;
}
</style>
