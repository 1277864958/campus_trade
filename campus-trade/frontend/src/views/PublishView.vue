<template>
  <div class="publish-page">
    <el-page-header @back="$router.back()" :content="isEdit ? '编辑商品' : '发布商品'" />

    <el-card class="publish-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">

        <!-- 商品图片（最多9张） -->
        <el-form-item label="商品图片" prop="imageUrls">
          <div class="upload-area">
            <div
              v-for="(url, idx) in form.imageUrls"
              :key="idx"
              class="img-preview"
            >
              <img :src="url" />
              <span class="remove-btn" @click="removeImg(idx)">×</span>
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
              <div class="upload-btn">
                <el-icon size="28"><Plus /></el-icon>
                <p>上传图片</p>
                <p class="tip">{{ form.imageUrls.length }}/9</p>
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
            :props="{ value:'id', label:'name', children:'children' }"
            @change="onCategoryChange"
            placeholder="请选择分类"
            style="width:100%"
          />
        </el-form-item>

        <el-form-item label="售价（元）" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :max="99999" :precision="2" controls-position="right" style="width:200px" />
        </el-form-item>

        <el-form-item label="原价（元）">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" controls-position="right" style="width:200px" placeholder="选填" />
        </el-form-item>

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

        <el-form-item>
          <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
          <el-button type="primary" @click="publish" :loading="saving">立即发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
  const catRes = await request.get('/api/categories')
  categoryOptions.value = catRes.data.data || []

  if (isEdit.value) {
    const res = await request.get(`/api/goods/${route.params.id}`)
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
      await request.put(`/api/goods/${route.params.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      const res = await request.post('/api/goods', payload)
      ElMessage.success(status === 'ON_SALE' ? '发布成功' : '草稿已保存')
    }
    router.push('/my-goods')
  } finally { saving.value = false }
}

const saveDraft = () => submit('DRAFT')
const publish   = () => submit('ON_SALE')
</script>

<style scoped>
.publish-page { max-width:800px; margin:0 auto; }
.publish-card { margin-top:20px; }
.upload-area  { display:flex; flex-wrap:wrap; gap:10px; }
.img-preview  { width:100px; height:100px; position:relative; border-radius:6px; overflow:hidden; border:1px solid #eee; }
.img-preview img { width:100%; height:100%; object-fit:cover; }
.remove-btn   { position:absolute; top:2px; right:4px; cursor:pointer; color:#fff; background:rgba(0,0,0,.5); border-radius:50%; width:18px; height:18px; display:flex; align-items:center; justify-content:center; font-size:14px; }
.cover-badge  { position:absolute; bottom:0; left:0; right:0; text-align:center; background:rgba(0,0,0,.5); color:#fff; font-size:11px; padding:2px 0; }
.upload-btn   { width:100px; height:100px; border:1px dashed #d9d9d9; border-radius:6px; display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer; color:#8c929a; font-size:12px; gap:2px; }
.upload-btn:hover { border-color:#409eff; color:#409eff; }
.field-tip { font-size:12px; color:#999; margin-top:6px; }
.tip { font-size:11px; }
</style>
