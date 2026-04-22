import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const token       = ref('')
  const refreshToken = ref('')
  const userInfo    = ref({})

  const isLogin  = computed(() => !!token.value)
  const isAdmin  = computed(() => userInfo.value.role === 'ADMIN')
  const userId   = computed(() => userInfo.value.id)

  function initFromStorage() {
    token.value        = localStorage.getItem('access_token')  || ''
    refreshToken.value = localStorage.getItem('refresh_token') || ''
    const info = localStorage.getItem('user_info')
    userInfo.value     = info ? JSON.parse(info) : {}
  }

  function setTokens(access, refresh) {
    token.value        = access
    refreshToken.value = refresh
    localStorage.setItem('access_token',  access)
    localStorage.setItem('refresh_token', refresh)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('user_info', JSON.stringify(info))
  }

  async function fetchMe() {
    try {
      const res = await request.get('/api/auth/me')
      setUserInfo(res.data.data)
    } catch (_) {}
  }

  function logout() {
    token.value = ''; refreshToken.value = ''; userInfo.value = {}
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
  }

  return { token, refreshToken, userInfo, isLogin, isAdmin, userId,
           initFromStorage, setTokens, setUserInfo, fetchMe, logout }
})
