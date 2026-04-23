<template>
  <div class="profile-page">
    <div class="profile-banner">
      <div class="banner-pattern"></div>
    </div>

    <div class="profile-content">
      <div class="profile-header-card">
        <div class="avatar-section">
          <el-upload action="/api/files/image" :headers="headers" :show-file-list="false"
            accept="image/*" :on-success="onAvatarSuccess">
            <div class="avatar-wrapper">
              <el-avatar :src="form.avatarUrl" :size="100" class="avatar">
                {{ form.nickname?.[0]||'U' }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="20"><Upload /></el-icon>
              </div>
            </div>
          </el-upload>
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ form.nickname || form.username }}</h2>
          <div class="user-rating">
            <el-rate :model-value="avgScore" disabled show-score size="small" />
          </div>
          <p class="join-date">加入于 {{ formatDate(userStore.userInfo.createdAt) }}</p>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">
          <span class="section-icon">
            <el-icon><User /></el-icon>
          </span>
          <h3>个人信息</h3>
        </div>
        <el-form :model="form" label-width="80px" class="profile-form">
          <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="50"/></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" maxlength="20"/></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email"/></el-form-item>
          <el-form-item>
            <el-button class="gradient-btn" @click="save" :loading="saving">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="section-card">
        <div class="section-title">
          <span class="section-icon">
            <el-icon><Lock /></el-icon>
          </span>
          <h3>修改密码</h3>
        </div>
        <el-form :model="pwdForm" label-width="100px" class="profile-form">
          <el-form-item label="当前密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password/></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password/></el-form-item>
          <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password/></el-form-item>
          <el-form-item>
            <el-button class="gradient-btn-outline" @click="changePwd" :loading="pwdSaving">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="section-card">
        <div class="section-title">
          <span class="section-icon">
            <el-icon><ChatDotRound /></el-icon>
          </span>
          <h3>我的评价</h3>
        </div>
        <div class="review-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <span class="reviewer">{{ r.reviewerName }}</span>
              <el-rate :model-value="r.score" disabled size="small"/>
              <span class="review-date">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p class="review-content">{{ r.content }}</p>
          </div>
          <el-empty v-if="!reviews.length" description="暂无评价" :image-size="80"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
const userStore=useUserStore()
const saving=ref(false),pwdSaving=ref(false),reviews=ref([]),avgScore=ref(0)
const form=reactive({username:'',nickname:'',phone:'',email:'',avatarUrl:''})
const pwdForm=reactive({oldPassword:'',newPassword:'',confirmPassword:''})
const headers=computed(()=>({Authorization:`Bearer ${localStorage.getItem('access_token')}`}))
onMounted(async()=>{
  const res=await request.get('/auth/me')
  const u=res.data.data; Object.assign(form,u)
  const rv=await request.get(`/orders/reviews/${u.id}`)
  reviews.value=rv.data.data||[]
  if(reviews.value.length) avgScore.value=reviews.value.reduce((s,r)=>s+r.score,0)/reviews.value.length
})
async function save(){
  saving.value=true
  try{
    const res=await request.put('/auth/me',{nickname:form.nickname,phone:form.phone,email:form.email,avatarUrl:form.avatarUrl})
    userStore.setUserInfo(res.data.data); ElMessage.success('保存成功')
  }finally{saving.value=false}
}
async function changePwd(){
  if(pwdForm.newPassword!==pwdForm.confirmPassword) return ElMessage.error('两次密码不一致')
  pwdSaving.value=true
  try{
    await request.put('/auth/password',{oldPassword:pwdForm.oldPassword,newPassword:pwdForm.newPassword,confirmPassword:pwdForm.confirmPassword})
    ElMessage.success('密码修改成功'); Object.assign(pwdForm,{oldPassword:'',newPassword:'',confirmPassword:''})
  }finally{pwdSaving.value=false}
}
function onAvatarSuccess(res){ if(res.code===200){form.avatarUrl=res.data;ElMessage.success('头像已更新')} }
const formatDate=d=>d?dayjs(d).format('YYYY-MM-DD'):''
</script>
<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bannerShimmer {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.profile-page {
  max-width: 760px;
  margin: 0 auto;
  padding-bottom: 40px;
  animation: fadeIn 0.6s ease-out;
}

.profile-banner {
  height: 180px;
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  border-radius: 0 0 var(--radius-xl, 20px) var(--radius-xl, 20px);
  position: relative;
  overflow: hidden;
}

.banner-pattern {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 40%),
              radial-gradient(circle at 60% 80%, rgba(255,255,255,0.08) 0%, transparent 45%);
  background-size: 200% 200%;
  animation: bannerShimmer 8s ease infinite;
}

.profile-content {
  padding: 0 16px;
  margin-top: -80px;
  position: relative;
  z-index: 1;
}

.profile-header-card {
  background: var(--bg-card, rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl, 20px);
  border: 1px solid var(--border-light, rgba(226, 232, 240, 0.8));
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.1), 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 20px;
  animation: fadeIn 0.6s ease-out 0.1s both;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  padding: 4px;
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.35);
}

.avatar-wrapper :deep(.el-avatar) {
  border: 3px solid #fff;
  font-size: 32px;
  font-weight: 700;
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: #fff;
}

.avatar-overlay {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary, #1e293b);
  margin: 0 0 6px 0;
  line-height: 1.3;
}

.user-rating {
  margin-bottom: 4px;
}

.user-rating :deep(.el-rate) {
  height: auto;
}

.join-date {
  font-size: 13px;
  color: var(--text-muted, #94a3b8);
  margin: 0;
}

.section-card {
  background: var(--bg-card, rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg, 16px);
  border: 1px solid var(--border-light, rgba(226, 232, 240, 0.8));
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04), 0 1px 4px rgba(0,0,0,0.02);
  margin-bottom: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.section-card:nth-child(2) {
  animation: fadeIn 0.6s ease-out 0.2s both;
}

.section-card:nth-child(3) {
  animation: fadeIn 0.6s ease-out 0.3s both;
}

.section-card:nth-child(4) {
  animation: fadeIn 0.6s ease-out 0.4s both;
}

.section-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.06), 0 2px 8px rgba(0,0,0,0.03);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light, rgba(226, 232, 240, 0.8));
}

.section-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.section-title h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary, #1e293b);
  margin: 0;
}

.profile-form {
  max-width: 520px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary, #64748b);
  font-size: 14px;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md, 12px);
  padding: 4px 14px;
  box-shadow: 0 0 0 1px var(--border-light, rgba(226, 232, 240, 0.8));
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
  background: rgba(255,255,255,0.6);
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3);
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.25);
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: rgba(241, 245, 249, 0.6);
}

.gradient-btn {
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  border: none;
  color: #fff;
  border-radius: var(--radius-md, 12px);
  padding: 10px 32px;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.3px;
  transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.3s ease;
  height: auto;
}

.gradient-btn:hover,
.gradient-btn:focus {
  background: var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
}

.gradient-btn:active {
  transform: translateY(0);
}

.gradient-btn-outline {
  background: transparent;
  border: 2px solid transparent;
  background-image: linear-gradient(#fff, #fff), var(--gradient-primary, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
  background-origin: border-box;
  background-clip: padding-box, border-box;
  color: var(--primary, #667eea);
  border-radius: var(--radius-md, 12px);
  padding: 10px 32px;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.3px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: auto;
}

.gradient-btn-outline:hover,
.gradient-btn-outline:focus {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.2);
  color: var(--primary-dark, #764ba2);
}

.gradient-btn-outline:active {
  transform: translateY(0);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-item {
  padding: 18px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.04) 0%, rgba(118, 75, 162, 0.04) 100%);
  border-radius: var(--radius-md, 12px);
  border: 1px solid var(--border-light, rgba(226, 232, 240, 0.8));
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.review-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.08);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.reviewer {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary, #1e293b);
}

.review-date {
  font-size: 12px;
  color: var(--text-muted, #94a3b8);
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: var(--text-secondary, #64748b);
  line-height: 1.7;
  margin: 0;
}

.review-list :deep(.el-empty) {
  padding: 40px 0;
}

.review-list :deep(.el-empty__description p) {
  color: var(--text-muted, #94a3b8);
  font-size: 14px;
}

@media (max-width: 640px) {
  .profile-header-card {
    flex-direction: column;
    text-align: center;
    padding: 24px 20px;
  }

  .profile-banner {
    height: 140px;
  }

  .profile-content {
    padding: 0 12px;
    margin-top: -60px;
  }

  .section-card {
    padding: 20px;
  }

  .profile-form {
    max-width: 100%;
  }

  .profile-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
