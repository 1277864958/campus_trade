import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  // ── 公开页面 ──────────────────────────────────────────────
  { path: '/',        name: 'Home',        component: () => import('../views/HomeView.vue') },
  { path: '/goods/:id', name: 'GoodsDetail', component: () => import('../views/GoodsDetailView.vue') },
  { path: '/auth',    name: 'Auth',        component: () => import('../views/AuthView.vue') },

  // ── 需要登录 ──────────────────────────────────────────────
  { path: '/publish',  name: 'Publish',   component: () => import('../views/PublishView.vue'),   meta: { requiresAuth: true } },
  { path: '/publish/:id', name: 'EditGoods', component: () => import('../views/PublishView.vue'), meta: { requiresAuth: true } },
  { path: '/my-goods', name: 'MyGoods',   component: () => import('../views/MyGoodsView.vue'),   meta: { requiresAuth: true } },
  { path: '/orders',   name: 'Orders',    component: () => import('../views/OrderView.vue'),      meta: { requiresAuth: true } },
  { path: '/chat',     name: 'Chat',      component: () => import('../views/ChatView.vue'),       meta: { requiresAuth: true } },
  { path: '/chat/:sessionId', name: 'ChatSession', component: () => import('../views/ChatView.vue'), meta: { requiresAuth: true } },
  { path: '/profile',  name: 'Profile',   component: () => import('../views/ProfileView.vue'),   meta: { requiresAuth: true } },
  { path: '/history',  name: 'History',   component: () => import('../views/HistoryView.vue'),   meta: { requiresAuth: true } },

  // ── 管理后台（需要 ADMIN 角色）────────────────────────────
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '',          redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard',    component: () => import('../views/admin/DashBoard.vue') },
      { path: 'users',     name: 'UserMgmt',     component: () => import('../views/admin/UserMgmt.vue') },
      { path: 'goods',     name: 'GoodsMgmt',    component: () => import('../views/admin/GoodsMgmt.vue') },
      { path: 'reports',   name: 'ReportMgmt',   component: () => import('../views/admin/ReportMgmt.vue') },
      { path: 'categories',name: 'CategoryMgmt', component: () => import('../views/admin/CategoryMgmt.vue') },
    ]
  },

  // ── 404 ──────────────────────────────────────────────────
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.initFromStorage()

  if (to.meta.requiresAuth && !userStore.isLogin) {
    return next({ path: '/auth', query: { redirect: to.fullPath } })
  }
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next('/')
  }
  next()
})

export default router
