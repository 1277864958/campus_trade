<template>
  <div class="auth-root">
    <div class="bg-circles"><span class="c1"/><span class="c2"/><span class="c3"/></div>
    <div class="auth-card">
      <div class="logo-area">
        <div class="logo-icon">🎒</div>
        <h1 class="logo-title">校园二手交易平台</h1>
        <p class="logo-sub">让闲置发挥价值</p>
      </div>

      <el-tabs v-model="mode" class="auth-tabs">
        <el-tab-pane label="登  录" name="login" />
        <el-tab-pane label="注  册" name="register" />
      </el-tabs>

      <!-- 登录表单 -->
      <el-form v-if="mode==='login'" ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
        <el-form-item label="账号（用户名/手机号）" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入账号" size="large" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"
            size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleLogin">
          登  录
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else ref="regFormRef" :model="regForm" :rules="regRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="regForm.username" placeholder="3~20位字母/数字/下划线" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="regForm.password" type="password" placeholder="6位以上" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="regForm.confirm" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item label="手机号（选填）" prop="phone">
          <el-input v-model="regForm.phone" placeholder="用于登录和找回密码" size="large" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="handleRegister">
          注  册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../stores/user'

const router    = useRouter()
const route     = useRoute()
const userStore = useUserStore()

const mode     = ref('login')
const loading  = ref(false)
const loginFormRef = ref(), regFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const regForm   = reactive({ username: '', password: '', confirm: '', phone: '' })

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3~20位字母/数字/下划线', trigger: 'blur' }
  ],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirm:  [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, val, cb) =>
        val === regForm.password ? cb() : cb(new Error('两次密码不一致')),
      trigger: 'blur'
    }
  ],
  phone: [{ pattern: /^(1[3-9]\d{9})?$/, message: '手机号格式不正确', trigger: 'blur' }],
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm)
    const { accessToken, refreshToken, ...info } = res.data.data
    userStore.setTokens(accessToken, refreshToken)
    userStore.setUserInfo({ id: info.userId, username: info.username, role: info.role })
    await userStore.fetchMe()
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } finally { loading.value = false }
}

async function handleRegister() {
  await regFormRef.value.validate()
  loading.value = true
  try {
    await request.post('/auth/register', {
      username: regForm.username,
      password: regForm.password,
      phone:    regForm.phone || undefined,
    })
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.username = regForm.username
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-root {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #e3f2fd 50%, #fce4ec 100%);
  position: relative; overflow: hidden;
}
.bg-circles { position: fixed; inset: 0; pointer-events: none; }
.c1, .c2, .c3 { position: absolute; border-radius: 50%; opacity: .15; animation: float 8s ease-in-out infinite; }
.c1 { width:400px; height:400px; background:#43a047; top:-100px; left:-100px; }
.c2 { width:280px; height:280px; background:#1e88e5; bottom:-60px; right:-60px; animation-delay:3s; }
.c3 { width:180px; height:180px; background:#e91e63; top:50%; right:12%; animation-delay:5s; }
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-24px)} }

.auth-card {
  position: relative; width: 420px;
  background: rgba(255,255,255,.9); backdrop-filter: blur(16px);
  border-radius: 24px; padding: 40px 36px 32px;
  box-shadow: 0 20px 60px rgba(0,0,0,.12);
}
.logo-area { text-align: center; margin-bottom: 28px; }
.logo-icon { font-size: 40px; }
.logo-title { font-size: 20px; font-weight: 700; color: #2e7d32; margin: 8px 0 4px; }
.logo-sub { font-size: 13px; color: #90a4ae; margin: 0; }
.auth-tabs { margin-bottom: 8px; }
.submit-btn { width: 100%; margin-top: 8px; font-size: 16px; font-weight: 600; }
</style>
