<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <div class="profile-header">
        <el-upload action="/api/files/image" :headers="headers" :show-file-list="false"
          accept="image/*" :on-success="onAvatarSuccess">
          <el-avatar :src="form.avatarUrl" :size="80" class="avatar">
            {{ form.nickname?.[0]||'U' }}
          </el-avatar>
        </el-upload>
        <div>
          <h2>{{ form.nickname || form.username }}</h2>
          <el-rate :model-value="avgScore" disabled show-score size="small" />
          <p class="join-date">加入于 {{ formatDate(userStore.userInfo.createdAt) }}</p>
        </div>
      </div>

      <el-form :model="form" label-width="80px" class="profile-form">
        <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" maxlength="50"/></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" maxlength="20"/></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email"/></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>

      <el-divider>修改密码</el-divider>
      <el-form :model="pwdForm" label-width="100px">
        <el-form-item label="当前密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password/></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password/></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password/></el-form-item>
        <el-form-item>
          <el-button @click="changePwd" :loading="pwdSaving">修改密码</el-button>
        </el-form-item>
      </el-form>

      <el-divider>我的评价</el-divider>
      <div class="review-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-header">
            <span class="reviewer">{{ r.reviewerName }}</span>
            <el-rate :model-value="r.score" disabled size="small"/>
            <span class="review-date">{{ formatDate(r.createdAt) }}</span>
          </div>
          <p class="review-content">{{ r.content }}</p>
        </div>
        <el-empty v-if="!reviews.length" description="暂无评价" :image-size="60"/>
      </div>
    </el-card>
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
.profile-page{max-width:700px;margin:0 auto}
.profile-card{padding:8px}
.profile-header{display:flex;align-items:center;gap:20px;margin-bottom:24px}
.profile-header h2{font-size:20px;font-weight:700;margin-bottom:4px}
.join-date{font-size:12px;color:#999;margin-top:4px}
.avatar{cursor:pointer}
.profile-form{max-width:500px}
.review-list{display:flex;flex-direction:column;gap:12px}
.review-item{padding:12px;background:#f8f9fa;border-radius:8px}
.review-header{display:flex;align-items:center;gap:10px;margin-bottom:6px}
.reviewer{font-weight:500}
.review-date{font-size:12px;color:#999;margin-left:auto}
.review-content{font-size:14px;color:#555;line-height:1.6}
</style>
