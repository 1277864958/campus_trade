<template>
  <div>
    <h2 class="page-title">商品管理</h2>
    <el-table :data="goods" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column label="封面" width="80">
        <template #default="{row}"><img :src="row.coverUrl||'/default-goods.png'" style="width:50px;height:50px;object-fit:cover;border-radius:4px"/></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip/>
      <el-table-column prop="price" label="价格" width="100"><template #default="{row}">¥{{row.price}}</template></el-table-column>
      <el-table-column prop="sellerName" label="卖家" width="120"/>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}"><el-tag :type="st(row.status)" size="small">{{sl(row.status)}}</el-tag></template>
      </el-table-column>
      <el-table-column prop="views" label="浏览" width="80"/>
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button size="small" type="danger" :disabled="row.status==='DRAFT'" @click="takeDown(row)">强制下架</el-button>
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
const goods=ref([]),loading=ref(false),total=ref(0),page=ref(1)
onMounted(load)
async function load(){
  loading.value=true
  try{const res=await request.get('/api/admin/goods',{params:{page:page.value-1,size:20}});goods.value=res.data.data?.list||[];total.value=res.data.data?.total||0}
  finally{loading.value=false}
}
async function takeDown(row){
  await ElMessageBox.confirm(`确认强制下架「${row.title}」？`,'下架确认',{type:'warning'})
  await request.put(`/api/admin/goods/${row.id}/take-down`);row.status='DRAFT';ElMessage.success('已强制下架')
}
const st=s=>({ON_SALE:'success',DRAFT:'danger',SOLD:'info',RESERVED:'warning'}[s]||'')
const sl=s=>({ON_SALE:'在售',DRAFT:'草稿',SOLD:'已售',RESERVED:'已预订'}[s]||s)
</script>
<style scoped>
.page-title{font-size:22px;font-weight:700;margin-bottom:20px}
.pagination{margin-top:16px;justify-content:flex-end}
</style>
