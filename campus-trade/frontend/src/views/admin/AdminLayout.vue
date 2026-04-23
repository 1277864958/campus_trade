<template>
  <el-container class="admin-layout">
    <el-aside width="240px" class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" width="24" height="24">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="logo-info">
          <span class="logo-title">Campus Trade</span>
          <span class="logo-subtitle">Admin Panel</span>
        </div>
      </div>

      <div class="menu-section">
        <span class="menu-label">NAVIGATION</span>
        <el-menu
          :default-active="$route.path"
          router
          background-color="transparent"
          text-color="rgba(255,255,255,0.6)"
          active-text-color="#fff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/goods">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><Warning /></el-icon>
            <span>举报管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="sidebar-footer">
        <div class="footer-btn" @click="backHome">
          <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
            <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>返回前台</span>
        </div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <div class="admin-topbar">
        <div class="topbar-left">
          <span class="breadcrumb-text">Admin</span>
        </div>
        <div class="topbar-right">
          <div class="topbar-indicator"></div>
        </div>
      </div>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { DataLine, User, Warning, Grid } from '@element-plus/icons-vue'
const Goods = { template: '<svg viewBox="0 0 24 24" fill="currentColor" width="1em" height="1em"><path d="M19 6h-2c0-2.76-2.24-5-5-5S7 3.24 7 6H5c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-7-3c1.66 0 3 1.34 3 3H9c0-1.66 1.34-3 3-3z"/></svg>' }
const router = useRouter()
const backHome = () => router.push('/')
</script>
<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  background: linear-gradient(180deg, #1a1f3c 0%, #151929 50%, #0f1220 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 240px;
  z-index: 100;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.1) 100%);
  pointer-events: none;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 20px;
  position: relative;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  width: 42px;
  height: 42px;
  background: var(--gradient-primary);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  flex-shrink: 0;
}

.logo-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.3px;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.menu-section {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
  position: relative;
}

.menu-label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.25);
  letter-spacing: 1.5px;
  padding: 0 24px;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.menu-section :deep(.el-menu) {
  border-right: none;
  padding: 0 12px;
}

.menu-section :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
  border-radius: 10px;
  margin-bottom: 4px;
  padding: 0 16px !important;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.menu-section :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #fff !important;
}

.menu-section :deep(.el-menu-item.is-active) {
  background: var(--gradient-primary) !important;
  color: #fff !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

.menu-section :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, transparent 100%);
  pointer-events: none;
}

.menu-section :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 12px;
  width: 18px;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
}

.footer-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  font-weight: 500;
}

.footer-btn:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.main-container {
  margin-left: 240px;
  flex-direction: column;
  min-height: 100vh;
}

.admin-topbar {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.breadcrumb-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.topbar-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
}

.admin-main {
  background: #f0f2f5;
  padding: 28px 32px;
  min-height: calc(100vh - 64px);
}
</style>
