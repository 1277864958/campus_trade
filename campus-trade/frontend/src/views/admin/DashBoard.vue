<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="page-title-section">
        <h2 class="page-title">数据统计</h2>
        <p class="page-desc">Overview of your platform metrics</p>
      </div>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8" v-for="(s, index) in stats" :key="s.label">
        <div class="stat-card" :class="'stat-card--' + index">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" :class="'icon-theme--' + index">
              <svg v-if="index === 0" viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/><path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg v-else-if="index === 1" viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="3" y1="6" x2="21" y2="6" stroke="currentColor" stroke-width="2"/><path d="M16 10a4 4 0 0 1-8 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg v-else-if="index === 2" viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2"/><line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2"/><line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2"/></svg>
              <svg v-else-if="index === 3" viewBox="0 0 24 24" fill="none" width="22" height="22"><line x1="12" y1="1" x2="12" y2="23" stroke="currentColor" stroke-width="2"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg v-else-if="index === 4" viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2"/><line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2"/></svg>
            </div>
            <div class="stat-content">
              <p class="stat-num">{{ s.value }}</p>
              <p class="stat-label">{{ s.label }}</p>
            </div>
          </div>
          <div class="stat-decoration"></div>
        </div>
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
.dashboard {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 28px;
}

.page-title-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
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

.stat-cards {
  margin-bottom: 24px;
}

.stat-cards .el-col {
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04), 0 1px 3px rgba(0, 0, 0, 0.02);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08), 0 4px 8px rgba(0, 0, 0, 0.04);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 18px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon-wrap {
  transform: scale(1.08);
}

.icon-theme--0 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.35);
}

.icon-theme--1 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(79, 172, 254, 0.35);
}

.icon-theme--2 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(67, 233, 123, 0.35);
}

.icon-theme--3 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(250, 112, 154, 0.35);
}

.icon-theme--4 {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(161, 140, 209, 0.35);
}

.icon-theme--5 {
  background: linear-gradient(135deg, #fccb90 0%, #d57eeb 100%);
  color: #fff;
  box-shadow: 0 4px 14px rgba(213, 126, 235, 0.35);
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 500;
}

.stat-decoration {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  opacity: 0.04;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.stat-card--0 .stat-decoration { background: #667eea; }
.stat-card--1 .stat-decoration { background: #4facfe; }
.stat-card--2 .stat-decoration { background: #43e97b; }
.stat-card--3 .stat-decoration { background: #fa709a; }
.stat-card--4 .stat-decoration { background: #a18cd1; }
.stat-card--5 .stat-decoration { background: #d57eeb; }

.stat-card:hover .stat-decoration {
  opacity: 0.08;
}
</style>
