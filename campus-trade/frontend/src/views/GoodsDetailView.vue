<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="goods">
      <el-row :gutter="24">
        <!-- 左：图片轮播 -->
        <el-col :span="10">
          <el-carousel height="360px" indicator-position="outside" :autoplay="false">
            <el-carousel-item v-for="url in goods.imageUrls" :key="url">
              <img :src="url" class="detail-img" />
            </el-carousel-item>
          </el-carousel>
        </el-col>

        <!-- 右：商品信息 -->
        <el-col :span="14">
          <div class="goods-info">
            <h1 class="goods-title">{{ goods.title }}</h1>

            <div class="price-row">
              <span class="price">¥{{ goods.price }}</span>
              <span v-if="goods.originalPrice" class="original">原价 ¥{{ goods.originalPrice }}</span>
              <el-tag :type="statusType(goods.status)" size="large">{{ statusLabel(goods.status) }}</el-tag>
            </div>

            <el-descriptions :column="2" border size="small" class="meta-desc">
              <el-descriptions-item label="分类">{{ goods.categoryName }}</el-descriptions-item>
              <el-descriptions-item label="地点">{{ goods.location || '校园内' }}</el-descriptions-item>
              <el-descriptions-item label="浏览">{{ goods.views }} 次</el-descriptions-item>
              <el-descriptions-item label="发布时间">{{ formatDate(goods.createdAt) }}</el-descriptions-item>
            </el-descriptions>

            <div class="desc-block">
              <p class="desc-label">商品描述</p>
              <p class="desc-text">{{ goods.description || '暂无描述' }}</p>
            </div>

            <!-- 卖家信息 -->
            <div class="seller-card">
              <el-avatar :size="44" :src="goods.sellerAvatar">{{ goods.sellerName?.[0] }}</el-avatar>
              <div class="seller-detail">
                <p class="seller-name">{{ goods.sellerName }}</p>
                <el-rate :model-value="sellerScore" disabled show-score size="small" />
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-row" v-if="!isSelf">
              <el-button
                type="primary" size="large"
                :disabled="goods.status !== 'ON_SALE'"
                @click="handleOrder"
              >立即购买</el-button>
              <el-button size="large" :icon="ChatDotRound" @click="handleChat">
                联系卖家
              </el-button>
              <el-button size="large" :icon="WarningFilled" @click="reportDialog = true" type="danger" plain>
                举报
              </el-button>
            </div>

            <!-- 自己的商品：编辑/上下架 -->
            <div class="action-row" v-else>
              <el-button type="primary" @click="$router.push(`/publish/${goods.id}`)">编辑商品</el-button>
              <el-button v-if="goods.status === 'DRAFT'" type="success" @click="changeStatus('on-sale')">上架</el-button>
              <el-button v-if="goods.status === 'ON_SALE'" @click="changeStatus('take-down')">下架</el-button>
              <el-button type="danger" plain @click="handleDelete">删除</el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 举报弹窗 -->
      <el-dialog v-model="reportDialog" title="举报商品" width="400px">
        <el-form>
          <el-form-item label="举报原因">
            <el-input v-model="reportReason" type="textarea" :rows="3" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reportDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :loading="reporting">提交举报</el-button>
        </template>
      </el-dialog>

      <!-- 下单弹窗 -->
      <el-dialog v-model="orderDialog" title="确认下单" width="400px">
        <el-form label-width="80px">
          <el-form-item label="商品">{{ goods.title }}</el-form-item>
          <el-form-item label="价格"><span class="price">¥{{ goods.price }}</span></el-form-item>
          <el-form-item label="备注">
            <el-input v-model="orderRemark" placeholder="可填写交接地点等备注" maxlength="200" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="orderDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmOrder" :loading="ordering">确认下单</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, WarningFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '../utils/request'
import { useUserStore } from '../stores/user'

const route     = useRoute()
const router    = useRouter()
const userStore = useUserStore()

const goods        = ref(null)
const loading      = ref(true)
const sellerScore  = ref(0)
const reportDialog = ref(false)
const reportReason = ref('')
const reporting    = ref(false)
const orderDialog  = ref(false)
const orderRemark  = ref('')
const ordering     = ref(false)

const isSelf = computed(() =>
  userStore.isLogin && goods.value?.sellerId === userStore.userId)

onMounted(async () => {
  try {
    const res = await request.get(`/api/goods/${route.params.id}`)
    goods.value = res.data.data
    // 获取卖家评分
    const revRes = await request.get(`/api/orders/reviews/${goods.value.sellerId}`)
    const reviews = revRes.data.data || []
    if (reviews.length) {
      sellerScore.value = reviews.reduce((s, r) => s + r.score, 0) / reviews.length
    }
  } finally { loading.value = false }
})

async function handleChat() {
  if (!userStore.isLogin) return router.push('/auth')
  const res = await request.post('/api/chat/sessions', {
    sellerId: goods.value.sellerId,
    goodsId:  goods.value.id,
  })
  router.push(`/chat/${res.data.data.sessionId}`)
}

function handleOrder() {
  if (!userStore.isLogin) return router.push('/auth')
  orderDialog.value = true
}

async function confirmOrder() {
  ordering.value = true
  try {
    await request.post('/api/orders', { goodsId: goods.value.id, remark: orderRemark.value })
    ElMessage.success('下单成功！请等待卖家确认')
    orderDialog.value = false
    router.push('/orders')
  } finally { ordering.value = false }
}

async function submitReport() {
  if (!reportReason.value.trim()) return ElMessage.warning('请填写举报原因')
  reporting.value = true
  try {
    await request.post('/api/goods/report', { goodsId: goods.value.id, reason: reportReason.value })
    ElMessage.success('举报已提交，感谢您的反馈')
    reportDialog.value = false
    reportReason.value = ''
  } finally { reporting.value = false }
}

async function changeStatus(action) {
  await request.put(`/api/goods/${goods.value.id}/${action}`)
  ElMessage.success(action === 'on-sale' ? '已上架' : '已下架')
  goods.value.status = action === 'on-sale' ? 'ON_SALE' : 'DRAFT'
}

async function handleDelete() {
  await ElMessageBox.confirm('确认删除该商品？删除后不可恢复', '删除确认', { type: 'warning' })
  await request.delete(`/api/goods/${goods.value.id}`)
  ElMessage.success('已删除')
  router.push('/my-goods')
}

const formatDate = d => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : ''
const statusType  = s => ({ ON_SALE:'success', RESERVED:'warning', SOLD:'info', DRAFT:'danger' }[s] || '')
const statusLabel = s => ({ ON_SALE:'在售', RESERVED:'已预订', SOLD:'已售', DRAFT:'草稿' }[s] || s)
</script>

<style scoped>
.detail-page { max-width:1000px; margin:0 auto; }
.detail-img { width:100%; height:100%; object-fit:contain; background:#f5f5f5; border-radius:8px; }
.goods-info { padding: 0 8px; }
.goods-title { font-size:22px; font-weight:700; margin-bottom:16px; line-height:1.4; }
.price-row { display:flex; align-items:center; gap:12px; margin-bottom:20px; }
.price { font-size:28px; font-weight:700; color:#e53935; }
.original { font-size:14px; color:#bbb; text-decoration:line-through; }
.meta-desc { margin-bottom:20px; }
.desc-block { margin-bottom:20px; }
.desc-label { font-weight:600; margin-bottom:8px; color:#333; }
.desc-text { color:#666; line-height:1.8; white-space:pre-wrap; }
.seller-card { display:flex; align-items:center; gap:12px; padding:16px; background:#f8f9fa; border-radius:8px; margin-bottom:20px; }
.seller-name { font-weight:600; margin-bottom:4px; }
.action-row { display:flex; gap:12px; flex-wrap:wrap; }
</style>
