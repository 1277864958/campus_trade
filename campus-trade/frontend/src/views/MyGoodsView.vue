<template>
  <div class="mygoods-page">
    <div class="page-header">
      <h2>我的商品</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/publish')">发布商品</el-button>
    </div>
    <el-tabs v-model="activeStatus" @tab-change="loadGoods">
      <el-tab-pane label="全部"  name="" />
      <el-tab-pane label="在售"  name="ON_SALE" />
      <el-tab-pane label="草稿"  name="DRAFT" />
      <el-tab-pane label="已售"  name="SOLD" />
    </el-tabs>
    <div v-loading="loading" class="goods-grid">
      <div v-for="g in goods" :key="g.id" class="goods-card">
        <img :src="g.coverUrl||'/default-goods.png'" class="card-img" @click="$router.push(`/goods/${g.id}`)"/>
        <div class="card-body">
          <p class="card-title">{{ g.title }}</p>
          <p class="card-price">¥{{ g.price }}</p>
          <el-tag :type="st(g.status)" size="small">{{ sl(g.status) }}</el-tag>
          <div class="card-actions">
            <el-button size="small" @click="$router.push(`/publish/${g.id}`)">编辑</el-button>
            <el-button v-if="g.status==='DRAFT'" size="small" type="success" @click="onSale(g)">上架</el-button>
            <el-button v-if="g.status==='ON_SALE'" size="small" @click="takeDown(g)">下架</el-button>
            <el-button size="small" type="danger" plain @click="del(g)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading&&!goods.length" description="暂无商品" />
    </div>
    <el-pagination v-if="total>12" class="pagination" v-model:current-page="page"
      :page-size="12" :total="total" layout="prev,pager,next" @current-change="loadGoods"/>
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
    const res=await request.get('/api/goods/my',{params:{status:activeStatus.value||undefined,page:page.value-1,size:12}})
    goods.value=res.data.data?.list||[]; total.value=res.data.data?.total||0
  }finally{loading.value=false}
}
async function onSale(g){ await request.put(`/api/goods/${g.id}/on-sale`); ElMessage.success('已上架'); loadGoods() }
async function takeDown(g){ await request.put(`/api/goods/${g.id}/take-down`); ElMessage.success('已下架'); loadGoods() }
async function del(g){
  await ElMessageBox.confirm('确认删除该商品？','删除',{type:'warning'})
  await request.delete(`/api/goods/${g.id}`); ElMessage.success('已删除'); loadGoods()
}
const st=s=>({ON_SALE:'success',DRAFT:'danger',SOLD:'info',RESERVED:'warning'}[s]||'')
const sl=s=>({ON_SALE:'在售',DRAFT:'草稿',SOLD:'已售',RESERVED:'已预订'}[s]||s)
</script>
<style scoped>
.mygoods-page{max-width:1100px;margin:0 auto}
.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}
.page-header h2{font-size:22px;font-weight:700}
.goods-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px;margin-top:16px}
.goods-card{background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)}
.card-img{width:100%;height:150px;object-fit:cover;cursor:pointer}
.card-body{padding:10px}
.card-title{font-size:13px;font-weight:500;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;margin-bottom:4px}
.card-price{color:#e53935;font-weight:700;margin-bottom:6px}
.card-actions{display:flex;flex-wrap:wrap;gap:4px;margin-top:8px}
.pagination{margin-top:24px;justify-content:center}
</style>
