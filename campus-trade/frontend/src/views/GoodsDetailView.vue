<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="goods">
      <div class="detail-card">
        <div class="detail-layout">
          <div class="image-section">
            <div class="carousel-wrapper">
              <el-carousel height="420px" indicator-position="outside" :autoplay="false">
                <el-carousel-item v-for="url in goods.imageUrls" :key="url">
                  <div class="img-container">
                    <img :src="url" class="detail-img" />
                  </div>
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>

          <div class="info-section">
            <div class="goods-info">
              <div class="title-area">
                <h1 class="goods-title">{{ goods.title }}</h1>
                <el-tag :type="statusType(goods.status)" size="large" class="status-tag" effect="dark" round>{{ statusLabel(goods.status) }}</el-tag>
              </div>

              <div class="price-row">
                <span class="price">¥{{ goods.price }}</span>
                <span v-if="goods.originalPrice" class="original">¥{{ goods.originalPrice }}</span>
              </div>

              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label">分类</span>
                  <span class="meta-value">{{ goods.categoryName }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">地点</span>
                  <span class="meta-value">{{ goods.location || '校园内' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">浏览</span>
                  <span class="meta-value">{{ goods.views }} 次</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发布时间</span>
                  <span class="meta-value">{{ formatDate(goods.createdAt) }}</span>
                </div>
              </div>

              <div class="desc-block">
                <p class="desc-label">商品描述</p>
                <p class="desc-text">{{ goods.description || '暂无描述' }}</p>
              </div>

              <div class="seller-card">
                <el-avatar :size="48" :src="goods.sellerAvatar" class="seller-avatar">{{ goods.sellerName?.[0] }}</el-avatar>
                <div class="seller-detail">
                  <p class="seller-name">{{ goods.sellerName }}</p>
                  <el-rate :model-value="sellerScore" disabled show-score size="small" />
                </div>
              </div>

              <div class="action-row" v-if="!isSelf">
                <el-button class="btn-buy" size="large" :disabled="goods.status !== 'ON_SALE'" @click="handleOrder">立即购买</el-button>
                <el-button class="btn-chat" size="large" :icon="ChatDotRound" @click="handleChat">联系卖家</el-button>
                <el-button class="btn-report" size="large" :icon="WarningFilled" @click="reportDialog = true">举报</el-button>
              </div>

              <div class="action-row" v-else>
                <el-button class="btn-buy" @click="$router.push(`/publish/${goods.id}`)">编辑商品</el-button>
                <el-button v-if="goods.status === 'DRAFT'" class="btn-success" @click="changeStatus('on-sale')">上架</el-button>
                <el-button v-if="goods.status === 'ON_SALE'" class="btn-chat" @click="changeStatus('take-down')">下架</el-button>
                <el-button class="btn-report" @click="handleDelete">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-dialog v-model="reportDialog" title="举报商品" width="440px" class="modern-dialog" :close-on-click-modal="false">
        <el-form>
          <el-form-item label="举报原因">
            <el-input v-model="reportReason" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请描述举报原因..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button class="btn-cancel" @click="reportDialog = false">取消</el-button>
            <el-button class="btn-buy" @click="submitReport" :loading="reporting">提交举报</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="orderDialog" title="确认下单" width="440px" class="modern-dialog" :close-on-click-modal="false">
        <div class="order-summary">
          <div class="order-item">
            <span class="order-label">商品</span>
            <span class="order-value">{{ goods.title }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">价格</span>
            <span class="order-value order-price">¥{{ goods.price }}</span>
          </div>
          <div class="order-remark">
            <span class="order-label">备注</span>
            <el-input v-model="orderRemark" placeholder="可填写交接地点等备注" maxlength="200" />
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button class="btn-cancel" @click="orderDialog = false">取消</el-button>
            <el-button class="btn-buy" @click="confirmOrder" :loading="ordering">确认下单</el-button>
          </div>
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
    const res = await request.get(`/goods/${route.params.id}`)
    goods.value = res.data.data
    const revRes = await request.get(`/orders/reviews/${goods.value.sellerId}`)
    const reviews = revRes.data.data || []
    if (reviews.length) {
      sellerScore.value = reviews.reduce((s, r) => s + r.score, 0) / reviews.length
    }
  } finally { loading.value = false }
})

async function handleChat() {
  if (!userStore.isLogin) return router.push('/auth')
  try {
    const res = await request.post('/chat/sessions', {
      sellerId: goods.value.sellerId,
      goodsId:  goods.value.id,
    })
    router.push(`/chat/${res.data.data.sessionId}`)
  } catch (e) {
    ElMessage.error('无法创建会话，请稍后重试')
  }
}

function handleOrder() {
  if (!userStore.isLogin) return router.push('/auth')
  orderDialog.value = true
}

async function confirmOrder() {
  ordering.value = true
  try {
    await request.post('/orders', { goodsId: goods.value.id, remark: orderRemark.value })
    ElMessage.success('下单成功！请等待卖家确认')
    orderDialog.value = false
    router.push('/orders')
  } finally { ordering.value = false }
}

async function submitReport() {
  if (!reportReason.value.trim()) return ElMessage.warning('请填写举报原因')
  reporting.value = true
  try {
    await request.post('/goods/report', { goodsId: goods.value.id, reason: reportReason.value })
    ElMessage.success('举报已提交，感谢您的反馈')
    reportDialog.value = false
    reportReason.value = ''
  } finally { reporting.value = false }
}

async function changeStatus(action) {
  await request.put(`/goods/${goods.value.id}/${action}`)
  ElMessage.success(action === 'on-sale' ? '已上架' : '已下架')
  goods.value.status = action === 'on-sale' ? 'ON_SALE' : 'DRAFT'
}

async function handleDelete() {
  await ElMessageBox.confirm('确认删除该商品？删除后不可恢复', '删除确认', { type: 'warning' })
  await request.delete(`/goods/${goods.value.id}`)
  ElMessage.success('已删除')
  router.push('/my-goods')
}

const formatDate = d => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : ''
const statusType  = s => ({ ON_SALE:'success', RESERVED:'warning', SOLD:'info', DRAFT:'danger' }[s] || '')
const statusLabel = s => ({ ON_SALE:'在售', RESERVED:'已预订', SOLD:'已售', DRAFT:'草稿' }[s] || s)
</script>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 8px 0;
  animation: fadeInUp 0.6s ease-out both;
}

.detail-card {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.detail-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1), 0 4px 12px rgba(0, 0, 0, 0.06);
}

.detail-layout {
  display: flex;
  gap: 0;
}

.image-section {
  flex: 0 0 440px;
  min-height: 420px;
}

.carousel-wrapper {
  height: 100%;
  padding: 24px;
}

.carousel-wrapper :deep(.el-carousel) {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.carousel-wrapper :deep(.el-carousel__indicators--outside) {
  margin-top: 12px;
}

.carousel-wrapper :deep(.el-carousel__indicator .el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-muted);
  opacity: 0.4;
  transition: all 0.3s ease;
}

.carousel-wrapper :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  width: 24px;
  border-radius: 4px;
  background: var(--primary);
  opacity: 1;
}

.img-container {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.detail-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.4s ease;
}

.detail-img:hover {
  transform: scale(1.03);
}

.info-section {
  flex: 1;
  min-width: 0;
}

.goods-info {
  padding: 28px 32px 28px 8px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title-area {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.goods-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.4;
  letter-spacing: -0.3px;
  flex: 1;
}

.status-tag {
  flex-shrink: 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 50%, #f0932b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.original {
  font-size: 15px;
  color: var(--text-muted);
  text-decoration: line-through;
  font-weight: 500;
}

.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: rgba(241, 245, 249, 0.6);
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.5);
  transition: all 0.2s ease;
}

.meta-item:hover {
  background: rgba(241, 245, 249, 0.9);
  border-color: rgba(102, 126, 234, 0.2);
}

.meta-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.desc-block {
  padding: 16px 20px;
  background: rgba(241, 245, 249, 0.4);
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.4);
}

.desc-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.desc-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: var(--bg-card);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.seller-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-1px);
}

.seller-avatar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #fff;
  flex-shrink: 0;
}

.seller-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.seller-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 4px;
}

.btn-buy {
  background: var(--gradient-primary);
  border: none;
  color: #fff;
  font-weight: 700;
  border-radius: var(--radius-md);
  padding: 12px 28px;
  font-size: 15px;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  letter-spacing: 0.3px;
}

.btn-buy:hover,
.btn-buy:focus {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-buy:active {
  transform: translateY(0);
}

.btn-buy.is-disabled {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

.btn-chat {
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 12px 24px;
  font-size: 15px;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}

.btn-chat:hover,
.btn-chat:focus {
  background: #fff;
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.btn-report {
  background: rgba(254, 242, 242, 0.8);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #ef4444;
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 12px 24px;
  font-size: 15px;
  transition: all 0.3s ease;
}

.btn-report:hover,
.btn-report:focus {
  background: rgba(254, 226, 226, 0.9);
  border-color: rgba(239, 68, 68, 0.4);
  color: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
}

.btn-success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
  color: #fff;
  font-weight: 700;
  border-radius: var(--radius-md);
  padding: 12px 24px;
  font-size: 15px;
  box-shadow: 0 4px 14px rgba(34, 197, 94, 0.3);
  transition: all 0.3s ease;
}

.btn-success:hover,
.btn-success:focus {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.4);
}

.btn-cancel {
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 10px 24px;
  transition: all 0.3s ease;
}

.btn-cancel:hover,
.btn-cancel:focus {
  background: #fff;
  color: var(--text-primary);
  border-color: #cbd5e1;
}

.modern-dialog :deep(.el-dialog) {
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-light);
}

.modern-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-light);
  margin-right: 0;
}

.modern-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.modern-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--border-light);
}

.modern-dialog :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.modern-dialog :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}

.modern-dialog :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.modern-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12), 0 0 0 1px var(--primary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: rgba(241, 245, 249, 0.5);
  border-radius: var(--radius-md);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.order-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.order-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-price {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 50%, #f0932b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.order-remark {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-remark .order-label {
  padding-left: 2px;
}
</style>
