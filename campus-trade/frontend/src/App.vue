<template>
  <el-container class="app-layout" v-if="!isAdminRoute">
    <el-header class="navbar">
      <div class="navbar-inner">
        <router-link to="/" class="logo">
          <span class="logo-icon-mini">🎒</span>
          <span class="logo-text">Campus Trade</span>
        </router-link>

        <div class="search-wrap">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的宝贝..."
            class="search-input"
            @keyup.enter="doSearch"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
        </div>

        <div class="nav-right">
          <template v-if="userStore.isLogin">
            <el-button class="publish-btn" type="primary" :icon="Plus" @click="$router.push('/publish')">
              发布商品
            </el-button>

            <el-badge :value="totalUnread || 0" :hidden="!totalUnread" class="chat-badge">
              <el-button :icon="ChatDotRound" circle class="icon-btn" @click="$router.push('/chat')" />
            </el-badge>

            <el-dropdown trigger="click" @command="handleCommand">
              <div class="avatar-wrapper">
                <el-avatar
                  :src="userStore.userInfo.avatarUrl"
                  :size="36"
                  class="avatar-btn"
                >{{ userStore.userInfo.nickname?.[0] || 'U' }}</el-avatar>
                <span class="avatar-name">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              </div>
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
            <el-button class="login-btn" @click="$router.push('/auth')">登录 / 注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <footer class="app-footer">
      <p>© 2025 Campus Trade · 校园二手交易平台</p>
    </footer>
  </el-container>

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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

:root {
  --primary: #667eea;
  --primary-dark: #764ba2;
  --gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --gradient-accent: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --bg-main: #f0f2f5;
  --bg-card: rgba(255, 255, 255, 0.85);
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;
  --border-light: rgba(226, 232, 240, 0.8);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.12);
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
}

* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  background: var(--bg-main);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
}

.app-layout { min-height: 100vh; display: flex; flex-direction: column; }

.navbar {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-light);
  position: sticky; top: 0; z-index: 100;
  padding: 0;
  transition: box-shadow 0.3s ease;
}
.navbar:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}
.navbar-inner {
  max-width: 1280px; margin: 0 auto;
  height: 64px; display: flex; align-items: center; gap: 24px; padding: 0 24px;
}

.logo {
  display: flex; align-items: center; gap: 10px;
  text-decoration: none; white-space: nowrap;
  transition: transform 0.2s ease;
}
.logo:hover { transform: scale(1.02); }
.logo-icon-mini {
  display: inline-flex; align-items: center; justify-content: center;
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  border-radius: 10px;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(142, 197, 252, 0.3);
}
.logo-text {
  font-size: 18px; font-weight: 800; color: var(--text-primary);
  letter-spacing: -0.5px;
}

.search-wrap { flex: 1; max-width: 520px; }
.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(241, 245, 249, 0.8);
  box-shadow: inset 0 0 0 1px var(--border-light);
  transition: all 0.3s ease;
  height: 40px;
}
.search-input :deep(.el-input__wrapper:hover) {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04), inset 0 0 0 1px #cbd5e1;
}
.search-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15), inset 0 0 0 1px var(--primary) !important;
}
.search-input :deep(.el-input-group__append) {
  border-radius: 0 12px 12px 0;
  background: var(--gradient-primary);
  border: none;
  box-shadow: none;
}
.search-input :deep(.el-input-group__append .el-button) {
  color: #fff;
}

.nav-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }

.publish-btn {
  border-radius: 10px;
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
  transition: all 0.3s ease;
}
.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
}

.icon-btn {
  border: 1px solid var(--border-light);
  background: rgba(241, 245, 249, 0.6);
  transition: all 0.2s ease;
}
.icon-btn:hover {
  background: #fff;
  border-color: var(--primary);
  color: var(--primary);
}

.avatar-wrapper {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 20px;
  transition: background 0.2s ease;
}
.avatar-wrapper:hover { background: rgba(241, 245, 249, 0.8); }
.avatar-btn {
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #fff;
}
.avatar-name {
  font-size: 13px; font-weight: 600; color: var(--text-secondary);
  max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.login-btn {
  border-radius: 10px;
  font-weight: 600;
  border: 1px solid var(--primary);
  color: var(--primary);
  transition: all 0.3s ease;
}
.login-btn:hover {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.chat-badge { line-height: 1; }

.main-content {
  max-width: 1280px; margin: 0 auto; width: 100%;
  padding: 28px 24px; min-height: calc(100vh - 64px - 60px);
  flex: 1;
}

.app-footer {
  text-align: center;
  padding: 20px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.5);
}
</style>
