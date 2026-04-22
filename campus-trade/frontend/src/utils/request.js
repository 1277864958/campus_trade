import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// ── 请求拦截：注入 Token ──────────────────────────────────────
request.interceptors.request.use(config => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

// ── 响应拦截：统一错误处理 + Token 自动刷新 ─────────────────
let isRefreshing = false
let pendingQueue = []

request.interceptors.response.use(
  res => {
    const data = res.data
    // 后端业务错误（code !== 200）
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
    return res
  },
  async err => {
    const originalReq = err.config
    const status = err.response?.status

    // 401 且不是 refresh 接口本身 → 尝试刷新 Token
    if (status === 401 && !originalReq._retry
        && !originalReq.url?.includes('/auth/refresh')) {
      originalReq._retry = true
      const refreshToken = localStorage.getItem('refresh_token')

      if (!refreshToken) {
        redirectToLogin(); return Promise.reject(err)
      }

      if (!isRefreshing) {
        isRefreshing = true
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefresh } = res.data.data
          localStorage.setItem('access_token', accessToken)
          localStorage.setItem('refresh_token', newRefresh)
          // 重放等待队列
          pendingQueue.forEach(cb => cb(accessToken))
          pendingQueue = []
          originalReq.headers['Authorization'] = `Bearer ${accessToken}`
          return request(originalReq)
        } catch (_) {
          redirectToLogin()
          return Promise.reject(err)
        } finally {
          isRefreshing = false
        }
      }

      // 已在刷新中，加入等待队列
      return new Promise(resolve => {
        pendingQueue.push(token => {
          originalReq.headers['Authorization'] = `Bearer ${token}`
          resolve(request(originalReq))
        })
      })
    }

    const msg = err.response?.data?.message || err.message || '网络错误'
    if (status !== 401) ElMessage.error(msg)
    return Promise.reject(err)
  }
)

function redirectToLogin() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('user_info')
  window.location.href = '/auth'
}

export default request
