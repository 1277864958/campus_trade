<template>
  <div class="history-page">
    <div class="page-header">
      <h2 class="page-title">浏览历史</h2>
      <p class="page-subtitle">回顾您最近浏览过的商品</p>
    </div>

    <div v-loading="loading" class="goods-grid">
      <div
        v-for="g in goods"
        :key="g.id"
        class="goods-card"
        @click="$router.push(`/goods/${g.id}`)"
      >
        <div class="card-img-wrapper">
          <img :src="g.coverUrl || '/default-goods.png'" class="card-img" />
          <div class="img-overlay">
            <span class="view-text">查看详情</span>
          </div>
        </div>
        <div class="card-body">
          <p class="card-title">{{ g.title }}</p>
          <p class="card-price">¥{{ g.price }}</p>
        </div>
      </div>
      <el-empty v-if="!loading && !goods.length" description="暂无浏览记录" class="empty-state" />
    </div>

    <div class="pagination-wrapper" v-if="total > 12">
      <el-pagination
        v-model:current-page="page"
        :page-size="12"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
const loading=ref(false),goods=ref([]),total=ref(0),page=ref(1)
onMounted(load)
async function load(){
  loading.value=true
  try{
    const res=await request.get('/goods/history',{params:{page:page.value-1,size:12}})
    goods.value=res.data.data?.list||[]; total.value=res.data.data?.total||0
  }finally{loading.value=false}
}
</script>

<style scoped>
.history-page {
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
  margin-bottom: 32px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
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
  cursor: pointer;
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
}

.card-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.goods-card:hover .card-img {
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

.goods-card:hover .img-overlay {
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

.card-body {
  padding: 14px 16px 18px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.card-price {
  font-size: 22px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
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
