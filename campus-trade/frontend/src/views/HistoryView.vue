<template>
  <div class="history-page">
    <h2 class="page-title">浏览历史</h2>
    <div v-loading="loading" class="goods-grid">
      <div v-for="g in goods" :key="g.id" class="goods-card" @click="$router.push(`/goods/${g.id}`)">
        <img :src="g.coverUrl||'/default-goods.png'" class="card-img"/>
        <div class="card-body">
          <p class="card-title">{{ g.title }}</p>
          <p class="card-price">¥{{ g.price }}</p>
        </div>
      </div>
      <el-empty v-if="!loading&&!goods.length" description="暂无浏览记录"/>
    </div>
    <el-pagination v-if="total>12" class="pagination" v-model:current-page="page"
      :page-size="12" :total="total" layout="prev,pager,next" @current-change="load"/>
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
.history-page{max-width:1100px;margin:0 auto}
.page-title{font-size:22px;font-weight:700;margin-bottom:20px}
.goods-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px}
.goods-card{background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06);cursor:pointer}
.goods-card:hover{transform:translateY(-3px);transition:.2s}
.card-img{width:100%;height:150px;object-fit:cover}
.card-body{padding:10px}
.card-title{font-size:13px;font-weight:500;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;margin-bottom:4px}
.card-price{color:#e53935;font-weight:700}
.pagination{margin-top:24px;justify-content:center}
</style>
