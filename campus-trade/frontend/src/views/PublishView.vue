<template>
  <div class="publish-page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        <span>返回</span>
      </button>
      <h2 class="page-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
    </div>

    <div class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">

        <el-form-item label="商品图片" prop="imageUrls">
          <div class="upload-zone">
            <div
              v-for="(url, idx) in form.imageUrls"
              :key="idx"
              class="img-preview"
            >
              <img :src="url" />
              <div class="img-actions">
                <span class="remove-btn" @click="removeImg(idx)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </span>
              </div>
              <span v-if="idx === 0" class="cover-badge">封面</span>
            </div>
            <el-upload
              v-if="form.imageUrls.length < 9"
              action="/api/files/image"
              :headers="uploadHeaders"
              :show-file-list="false"
              accept="image/*"
              :on-success="onImgSuccess"
              :on-error="onImgError"
              :before-upload="beforeUpload"
            >
              <div class="upload-trigger">
                <div class="upload-icon-wrapper">
                  <el-icon size="24"><Plus /></el-icon>
                </div>
                <p class="upload-label">上传图片</p>
                <p class="upload-count">{{ form.imageUrls.length }}/9</p>
              </div>
            </el-upload>
          </div>
          <p class="field-tip">第一张图片为封面；支持 JPG/PNG/WEBP，单张不超过 10MB</p>
        </el-form-item>

        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题（5~100字）" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="categoryPath"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            @change="onCategoryChange"
            placeholder="请选择分类"
            style="width: 100%"
          />
        </el-form-item>

        <div class="price-row">
          <el-form-item label="售价（元）" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :max="99999" :precision="2" controls-position="right" style="width: 200px" />
          </el-form-item>
          <el-form-item label="原价（元）">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" controls-position="right" style="width: 200px" placeholder="选填" />
          </el-form-item>
        </div>

        <el-form-item label="交接地点">
          <el-input v-model="form.location" placeholder="如：图书馆一楼大厅、宿舍楼" maxlength="100" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="描述商品成色、购买时间、使用情况等..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <div class="form-actions">
          <button type="button" class="btn btn-draft" @click="saveDraft" :disabled="saving">
            <span v-if="saving" class="btn-loading"></span>
            保存草稿
          </button>
          <button type="button" class="btn btn-publish" @click="publish" :disabled="saving">
            <span v-if="saving" class="btn-loading"></span>
            立即发布
          </button>
          <button type="button" class="btn btn-cancel" @click="$router.back()">取消</button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

const route  = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const formRef = ref(null)
const saving  = ref(false)
const categoryPath    = ref([])
const categoryOptions = ref([])

const form = reactive({
  title:         '',
  categoryId:    null,
  price:         null,
  originalPrice: null,
  location:      '',
  description:   '',
  imageUrls:     [],
})

const rules = {
  title:       [{ required: true, message: '请输入商品标题', trigger: 'blur' },
                { min: 2, max: 100, message: '标题长度 2~100 字', trigger: 'blur' }],
  categoryId:  [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price:       [{ required: true, message: '请输入售价', trigger: 'blur' }],
  description: [{ required: true, message: '请填写商品描述', trigger: 'blur' }],
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('access_token')}`
}))

onMounted(async () => {
  const catRes = await request.get('/categories')
  categoryOptions.value = catRes.data.data || []

  if (isEdit.value) {
    const res = await request.get(`/goods/${route.params.id}`)
    const g = res.data.data
    Object.assign(form, {
      title:         g.title,
      categoryId:    g.categoryId,
      price:         g.price,
      originalPrice: g.originalPrice,
      location:      g.location,
      description:   g.description,
      imageUrls:     g.imageUrls || [],
    })
  }
})

function onCategoryChange(val) {
  form.categoryId = val[val.length - 1]
}

function onImgSuccess(res) {
  if (res.code === 200) form.imageUrls.push(res.data)
  else ElMessage.error('上传失败')
}

function onImgError() { ElMessage.error('图片上传失败，请重试') }

function beforeUpload(file) {
  const ok = ['image/jpeg','image/png','image/webp'].includes(file.type)
  if (!ok) ElMessage.error('仅支持 JPG/PNG/WEBP 格式')
  return ok
}

function removeImg(idx) { form.imageUrls.splice(idx, 1) }

async function submit(status) {
  await formRef.value.validate()
  if (!form.imageUrls.length) return ElMessage.warning('请至少上传一张图片')
  saving.value = true
  try {
    const payload = { ...form, status }
    if (isEdit.value) {
      await request.put(`/goods/${route.params.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      const res = await request.post('/goods', payload)
      ElMessage.success(status === 'ON_SALE' ? '发布成功' : '草稿已保存')
    }
    router.push('/my-goods')
  } finally { saving.value = false }
}

const saveDraft = () => submit('DRAFT')
const publish   = () => submit('ON_SALE')
</script>

<style scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.back-btn:hover {
  color: var(--primary);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateX(-2px);
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.form-card {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  padding: 36px 32px;
}

.form-card :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--text-primary);
}

.form-card :deep(.el-input__wrapper),
.form-card :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border-light);
  transition: all 0.25s ease;
}

.form-card :deep(.el-input__wrapper:hover),
.form-card :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.4);
}

.form-card :deep(.el-input__wrapper.is-focus),
.form-card :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3);
}

.form-card :deep(.el-cascader) {
  width: 100%;
}

.upload-zone {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.img-preview {
  width: 110px;
  height: 110px;
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 2px solid var(--border-light);
  transition: all 0.25s ease;
}

.img-preview:hover {
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.img-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-actions {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.img-preview:hover .img-actions {
  opacity: 1;
}

.remove-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.9);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.remove-btn:hover {
  transform: scale(1.15);
}

.cover-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 0;
}

.upload-trigger {
  width: 110px;
  height: 110px;
  border: 2px dashed var(--border-light);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  gap: 4px;
  background: rgba(102, 126, 234, 0.02);
}

.upload-trigger:hover {
  border-color: var(--primary);
  background: rgba(102, 126, 234, 0.06);
  transform: translateY(-2px);
}

.upload-trigger:hover .upload-icon-wrapper {
  background: rgba(102, 126, 234, 0.15);
  color: var(--primary);
}

.upload-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 50%;
  color: var(--text-muted);
  transition: all 0.3s ease;
}

.upload-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0;
}

.upload-count {
  font-size: 11px;
  color: var(--text-muted);
  margin: 0;
}

.field-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.price-row {
  display: flex;
  gap: 24px;
}

.price-row .el-form-item {
  flex: 1;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  padding-left: 100px;
}

.btn {
  padding: 12px 32px;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-loading {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.btn-publish {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-publish:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.55);
}

.btn-draft {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary);
}

.btn-draft:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.18);
  transform: translateY(-1px);
}

.btn-draft .btn-loading {
  border-color: rgba(102, 126, 234, 0.3);
  border-top-color: var(--primary);
}

.btn-cancel {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
}

.btn-cancel:hover {
  border-color: rgba(102, 126, 234, 0.3);
  color: var(--text-primary);
  transform: translateY(-1px);
}
</style>
