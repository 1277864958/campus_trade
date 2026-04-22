<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="sidebar">
      <div class="sidebar-logo">🎒 管理后台</div>
      <el-menu :default-active="$route.path" router background-color="#1a2035" text-color="#a0aec0" active-text-color="#fff">
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine/></el-icon>数据统计</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User/></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/admin/goods"><el-icon><Goods/></el-icon>商品管理</el-menu-item>
        <el-menu-item index="/admin/reports"><el-icon><Warning/></el-icon>举报管理</el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Grid/></el-icon>分类管理</el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text style="color:#a0aec0" @click="backHome">← 返回前台</el-button>
      </div>
    </el-aside>
    <el-main class="admin-main">
      <router-view />
    </el-main>
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
.admin-layout { min-height:100vh; }
.sidebar { background:#1a2035; display:flex; flex-direction:column; }
.sidebar-logo { color:#fff; font-size:16px; font-weight:700; padding:20px 16px; border-bottom:1px solid rgba(255,255,255,.1); }
.sidebar-footer { margin-top:auto; padding:16px; }
.admin-main { background:#f5f6fa; padding:24px; }
</style>
