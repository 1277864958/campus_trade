<template>
  <div class="auth-root">
    <div class="ambient-bg">
      <div class="color-blob blob-1"></div>
      <div class="color-blob blob-2"></div>
      <div class="color-blob blob-3"></div>
      <div class="glass-overlay"></div>
    </div>

    <div class="auth-card-wrapper animate-fade-up">
      <div class="auth-card">

        <div class="logo-area">
          <div class="logo-icon-wrapper">
            <span class="logo-icon">🎒</span>
          </div>
          <h1 class="logo-title">Campus Trade</h1>
          <p class="logo-sub">探索校园闲置的无限可能</p>
        </div>

        <div v-if="mode !== 'forgot'" class="custom-tabs">
          <div class="tab-slider" :class="'slider-' + mode"></div>
          <button :class="['tab-btn', { active: mode === 'login' }]" @click="mode = 'login'">登 录</button>
          <button :class="['tab-btn', { active: mode === 'register' }]" @click="mode = 'register'">注 册</button>
        </div>

        <div v-else class="forgot-header">
          <button class="back-link" @click="mode = 'login'">← 返回登录</button>
          <h2 class="forgot-title">找回密码</h2>
          <p class="forgot-desc">通过用户名和注册手机号验证身份</p>
        </div>

        <div class="form-container">
          <transition name="form-fade" mode="out-in">

            <el-form
                v-if="mode==='login'"
                key="login"
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                label-position="top"
                class="glass-form"
            >
              <el-form-item label="账号" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名或手机号" size="large" clearable class="custom-input" />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" class="custom-input" />
              </el-form-item>

              <el-form-item label="验证码" prop="captchaCode">
                <div class="captcha-row">
                  <el-input v-model="loginForm.captchaCode" placeholder="验证码" size="large" @keyup.enter="handleLogin" class="custom-input" />
                  <div class="captcha-img-box" @click="refreshCaptcha" title="点击刷新">
                    <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
                    <span v-else class="captcha-loading">加载中...</span>
                  </div>
                </div>
              </el-form-item>

              <el-button class="submit-btn login-btn" type="primary" size="large" :loading="loading" @click="handleLogin">
                开启探索之旅
              </el-button>
              <div class="form-footer">
                <button type="button" class="forgot-link" @click="mode = 'forgot'; refreshForgotCaptcha()">忘记密码？</button>
              </div>
            </el-form>

            <el-form
                v-else-if="mode==='register'"
                key="register"
                ref="regFormRef"
                :model="regForm"
                :rules="regRules"
                label-position="top"
                class="glass-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="regForm.username" placeholder="3~20位字母/数字/下划线" size="large" class="custom-input" />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input v-model="regForm.password" type="password" placeholder="6位以上" size="large" show-password class="custom-input" />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirm">
                <el-input v-model="regForm.confirm" type="password" placeholder="请再次输入密码" size="large" show-password class="custom-input" />
              </el-form-item>

              <el-form-item label="手机号 (选填)" prop="phone">
                <el-input v-model="regForm.phone" placeholder="用于找回密码" size="large" class="custom-input" />
              </el-form-item>

              <el-button class="submit-btn register-btn" type="primary" size="large" :loading="loading" @click="handleRegister">
                立即注册
              </el-button>
            </el-form>

            <el-form
                v-else
                key="forgot"
                ref="forgotFormRef"
                :model="forgotForm"
                :rules="forgotRules"
                label-position="top"
                class="glass-form"
            >
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px; border-radius: 12px;">
                管理员账号无法通过此方式重置密码
              </el-alert>

              <el-form-item label="用户名" prop="username">
                <el-input v-model="forgotForm.username" placeholder="请输入注册时的用户名" size="large" clearable class="custom-input" />
              </el-form-item>

              <el-form-item label="注册手机号" prop="phone">
                <el-input v-model="forgotForm.phone" placeholder="请输入注册时绑定的手机号" size="large" class="custom-input" />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="forgotForm.newPassword" type="password" placeholder="6位以上新密码" size="large" show-password class="custom-input" />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="请再次输入新密码" size="large" show-password class="custom-input" />
              </el-form-item>

              <el-form-item label="验证码" prop="captchaCode">
                <div class="captcha-row">
                  <el-input v-model="forgotForm.captchaCode" placeholder="验证码" size="large" @keyup.enter="handleResetPassword" class="custom-input" />
                  <div class="captcha-img-box" @click="refreshForgotCaptcha" title="点击刷新">
                    <img v-if="forgotCaptchaImg" :src="forgotCaptchaImg" alt="验证码" />
                    <span v-else class="captcha-loading">加载中...</span>
                  </div>
                </div>
              </el-form-item>

              <el-button class="submit-btn forgot-btn" type="primary" size="large" :loading="loading" @click="handleResetPassword">
                重置密码
              </el-button>
            </el-form>

          </transition>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>

import { ref, reactive, onMounted, watch } from 'vue'
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

const loginForm = reactive({ username: '', password: '', captchaCode: '', captchaId: '' })
const captchaImg = ref('')
const regForm   = reactive({ username: '', password: '', confirm: '', phone: '' })
const forgotFormRef = ref()
const forgotForm = reactive({ username: '', phone: '', newPassword: '', confirmPassword: '', captchaCode: '', captchaId: '' })
const forgotCaptchaImg = ref('')

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha() {
  try {
    const res = await request.get('/captcha')
    const { captchaId, base64Image } = res.data.data
    loginForm.captchaId = captchaId
    captchaImg.value = base64Image
  } catch (_) {}
}

onMounted(() => refreshCaptcha())
watch(mode, (v) => { if (v === 'login') refreshCaptcha(); if (v === 'forgot') refreshForgotCaptcha() })

async function refreshForgotCaptcha() {
  try {
    const res = await request.get('/captcha')
    const { captchaId, base64Image } = res.data.data
    forgotForm.captchaId = captchaId
    forgotCaptchaImg.value = base64Image
  } catch (_) {}
}

const forgotRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  newPassword: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, val, cb) =>
          val === forgotForm.newPassword ? cb() : cb(new Error('两次密码不一致')),
      trigger: 'blur'
    }
  ],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function handleResetPassword() {
  await forgotFormRef.value.validate()
  loading.value = true
  try {
    await request.post('/auth/reset-password', {
      username: forgotForm.username,
      phone: forgotForm.phone,
      newPassword: forgotForm.newPassword,
      captchaCode: forgotForm.captchaCode,
      captchaId: forgotForm.captchaId,
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    mode.value = 'login'
    loginForm.username = forgotForm.username
    Object.assign(forgotForm, { username: '', phone: '', newPassword: '', confirmPassword: '', captchaCode: '', captchaId: '' })
  } catch (e) {
    refreshForgotCaptcha()
    throw e
  } finally { loading.value = false }
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
    const res = await request.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      captchaCode: loginForm.captchaCode,
      captchaId: loginForm.captchaId,
    })
    const { accessToken, refreshToken, ...info } = res.data.data
    userStore.setTokens(accessToken, refreshToken)
    userStore.setUserInfo({ id: info.userId, username: info.username, role: info.role })
    await userStore.fetchMe()
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    refreshCaptcha()
    throw e
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
/* ── 页面底层容器 ── */
.auth-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* ── 炫酷动态流光背景 ── */
.ambient-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  background: #eef2f3;
}
.color-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.8;
  animation: float-blob 15s infinite alternate ease-in-out;
}
.blob-1 {
  width: 600px; height: 600px;
  background: rgba(102, 126, 234, 0.4);
  top: -100px; left: -100px;
  animation-delay: 0s;
}
.blob-2 {
  width: 500px; height: 500px;
  background: rgba(118, 75, 162, 0.3);
  bottom: -50px; right: -50px;
  animation-delay: -5s;
}
.blob-3 {
  width: 400px; height: 400px;
  background: rgba(67, 233, 123, 0.3);
  top: 40%; left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}
.glass-overlay {
  position: absolute; inset: 0;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(30px);
}

@keyframes float-blob {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -50px) scale(1.1); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1); }
}

/* ── 居中毛玻璃卡片 ── */
.auth-card-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
  perspective: 1000px;
}
.auth-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08),
  inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* 进场浮现动画 */
.animate-fade-up {
  animation: fadeUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}
@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(40px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

/* ── Logo 区域 ── */
.logo-area { text-align: center; margin-bottom: 30px; }
.logo-icon-wrapper {
  display: inline-flex; justify-content: center; align-items: center;
  width: 64px; height: 64px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  border-radius: 20px;
  box-shadow: 0 10px 20px rgba(142, 197, 252, 0.3);
  margin-bottom: 16px;
  animation: float-icon 4s ease-in-out infinite;
}
@keyframes float-icon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.logo-icon { font-size: 32px; }
.logo-title { font-size: 24px; font-weight: 800; color: #1e293b; margin: 0 0 6px; letter-spacing: -0.5px; }
.logo-sub { font-size: 14px; color: #64748b; margin: 0; font-weight: 500; }

/* ── 胶囊式滑动 Tabs ── */
.custom-tabs {
  position: relative; display: flex;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12px; padding: 4px; margin-bottom: 30px;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}
.tab-btn {
  flex: 1; height: 38px;
  border: none; background: transparent;
  font-size: 15px; font-weight: 600; color: #64748b;
  cursor: pointer; position: relative; z-index: 2;
  transition: color 0.3s; border-radius: 8px;
}
.tab-btn.active { color: #0f172a; }
.tab-slider {
  position: absolute; top: 4px; bottom: 4px; left: 4px; width: calc(50% - 4px);
  background: #fff; border-radius: 8px; z-index: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.slider-register { transform: translateX(100%); }

.forgot-header { text-align: center; margin-bottom: 24px; }
.back-link {
  background: none; border: none; color: #667eea; font-size: 14px; font-weight: 600;
  cursor: pointer; margin-bottom: 12px; display: inline-block;
  transition: all 0.2s;
}
.back-link:hover { color: #764ba2; transform: translateX(-3px); }
.forgot-title { font-size: 20px; font-weight: 800; color: #1e293b; margin: 8px 0 6px; }
.forgot-desc { font-size: 13px; color: #94a3b8; margin: 0; }

.form-footer { text-align: center; margin-top: 16px; }
.forgot-link {
  background: none; border: none; color: #94a3b8; font-size: 13px; font-weight: 500;
  cursor: pointer; transition: color 0.2s;
}
.forgot-link:hover { color: #667eea; }

.forgot-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 8px 20px rgba(245, 87, 108, 0.3);
}
.forgot-btn:hover {
  box-shadow: 0 12px 24px rgba(245, 87, 108, 0.4); transform: translateY(-2px);
}

/* ── 表单切换动画 ── */
.form-container { position: relative; }
.form-fade-enter-active, .form-fade-leave-active { transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.form-fade-enter-from { opacity: 0; transform: translateX(20px); }
.form-fade-leave-to { opacity: 0; transform: translateX(-20px); }

/* ── 深度定制 Element Plus 表单 ── */
.glass-form :deep(.el-form-item) { margin-bottom: 24px; }
.glass-form :deep(.el-form-item__label) {
  color: #475569; font-size: 14px; font-weight: 600; padding-bottom: 6px;
}
.glass-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px; height: 48px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02), inset 0 0 0 1px rgba(226, 232, 240, 1);
  transition: all 0.3s ease;
}
.glass-form :deep(.el-input__inner) { font-size: 15px; color: #1e293b; }
.glass-form :deep(.el-input__wrapper:hover) {
  background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.05), inset 0 0 0 1px #cbd5e1;
}
.glass-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15), inset 0 0 0 1px #667eea !important;
}

/* ── 验证码专属美化 ── */
.captcha-row { display: flex; gap: 12px; width: 100%; }
.captcha-row :deep(.el-input) { flex: 1; }
.captcha-img-box {
  width: 130px; height: 48px; flex-shrink: 0;
  background: rgba(255,255,255,0.8);
  border-radius: 12px; overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 1);
  cursor: pointer; position: relative;
  transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.captcha-img-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.08), inset 0 0 0 1px #cbd5e1;
}
.captcha-img-box:active { transform: translateY(0) scale(0.95); }
.captcha-img-box img { width: 100%; height: 100%; object-fit: fill; display: block; }
.captcha-loading { font-size: 13px; color: #94a3b8; }

/* ── 超感按钮 ── */
.submit-btn {
  width: 100%; height: 50px; border-radius: 12px; border: none;
  font-size: 16px; font-weight: bold; letter-spacing: 1px; color: #fff;
  margin-top: 10px; cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
/* 登录渐变 */
.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 20px rgba(118, 75, 162, 0.3);
}
.login-btn:hover {
  box-shadow: 0 12px 24px rgba(118, 75, 162, 0.4); transform: translateY(-2px);
}
/* 注册渐变 */
.register-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 8px 20px rgba(79, 172, 254, 0.3);
}
.register-btn:hover {
  box-shadow: 0 12px 24px rgba(79, 172, 254, 0.4); transform: translateY(-2px);
}
.submit-btn:active { transform: translateY(1px); }

/* ── 移动端适配 ── */
@media (max-width: 480px) {
  .auth-card { padding: 30px 24px; border-radius: 20px; }
  .logo-title { font-size: 22px; }
  .logo-icon-wrapper { width: 56px; height: 56px; }
  .logo-icon { font-size: 28px; }
}
</style>