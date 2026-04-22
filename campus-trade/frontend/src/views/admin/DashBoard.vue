<template>
  <div>
    <h2 class="page-title">数据统计</h2>
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card class="stat-card">
          <p class="stat-num">{{ s.value }}</p>
          <p class="stat-label">{{ s.label }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
const stat = ref({})
const stats = ref([])
onMounted(async () => {
  const res = await request.get('/admin/stat')
  stat.value = res.data.data || {}
  stats.value = [
    { label: '注册用户', value: stat.value.totalUsers || 0 },
    { label: '商品总数', value: stat.value.totalGoods || 0 },
    { label: '订单总数', value: stat.value.totalOrders || 0 },
    { label: '成交金额', value: '¥' + (stat.value.totalAmount || 0) },
    { label: '已完成订单', value: stat.value.finishedOrders || 0 },
    { label: '待处理举报', value: stat.value.pendingReports || 0 },
  ]
})
</script>
<style scoped>
.page-title{font-size:22px;font-weight:700;margin-bottom:20px}
.stat-cards{margin-bottom:24px}
.stat-card{text-align:center;padding:8px 0}
.stat-num{font-size:32px;font-weight:700;color:#2e7d32;margin-bottom:4px}
.stat-label{font-size:14px;color:#666}
</style>
