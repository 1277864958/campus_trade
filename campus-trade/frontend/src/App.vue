<template>
  <el-container class="app-layout" v-if="!isAdminRoute">
    <!-- 顶部导航 -->
    <el-header class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">🎒 校园二手</router-link>

        <!-- 搜索框 -->
        <div class="search-wrap">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品..."
            class="search-input"
            @keyup.enter="doSearch"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
        </div>

        <!-- 右侧操作区 -->
        <div class="nav-right">
          <template v-if="userStore.isLogin">
            <!-- 发布按钮 -->
            <el-button type="primary" :icon="Plus" @click="$router.push('/publish')">
              发布商品
            </el-button>

            <!-- 消息图标 -->
            <el-badge :value="totalUnread || 0" :hidden="!totalUnread" class="chat-badge">
              <el-button :icon="ChatDotRound" circle @click="$router.push('/chat')" />
            </el-badge>

            <!-- 用户头像下拉 -->
            <el-dropdown trigger="click" @command="handleCommand">
              <el-avatar
                :src="userStore.userInfo.avatarUrl"
                :size="36"
                class="avatar-btn"
              >{{ userStore.userInfo.nickname?.[0] || 'U' }}</el-avatar>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="my-goods">我的商品</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="history">浏览历史</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else>
            <el-button @click="$router.push('/auth')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 页面主体 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>

  <!-- 管理后台不显示公共导航，直接渲染 -->
  <router-view v-else />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Plus, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from './stores/user'
import { useChatStore } from './stores/chat'

const router     = useRouter()
const route      = useRoute()
const userStore  = useUserStore()
const chatStore  = useChatStore()

const searchKeyword = ref('')
const isAdminRoute  = computed(() => route.path.startsWith('/admin'))
const totalUnread   = computed(() => chatStore.totalUnread)

function doSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/', query: { keyword: searchKeyword.value.trim() } })
}

function handleCommand(cmd) {
  const map = {
    profile:  '/profile',
    'my-goods': '/my-goods',
    orders:   '/orders',
    history:  '/history',
    admin:    '/admin',
    chat:     '/chat',
  }
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/auth')
  } else if (map[cmd]) {
    router.push(map[cmd])
  }
}

onMounted(() => {
  userStore.initFromStorage()
  if (userStore.isLogin) chatStore.fetchUnread()
})
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #f5f6fa; font-family: 'PingFang SC', 'Helvetica Neue', sans-serif; }

.app-layout { min-height: 100vh; }

.navbar {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0,0,0,.08);
  position: sticky; top: 0; z-index: 100;
  padding: 0;
}
.navbar-inner {
  max-width: 1200px; margin: 0 auto;
  height: 60px; display: flex; align-items: center; gap: 20px; padding: 0 16px;
}
.logo {
  font-size: 20px; font-weight: 700; color: #2e7d32;
  text-decoration: none; white-space: nowrap;
}
.search-wrap { flex: 1; max-width: 480px; }
.search-input { width: 100%; }
.nav-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.avatar-btn { cursor: pointer; }
.chat-badge { line-height: 1; }

.main-content {
  max-width: 1200px; margin: 0 auto;
  padding: 24px 16px; min-height: calc(100vh - 60px);
}
</style>
