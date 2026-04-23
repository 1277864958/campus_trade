<template>
  <div class="order-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
      <p class="page-subtitle">查看和管理你的交易记录</p>
    </div>

    <div class="tabs-wrapper">
      <el-tabs v-model="activeTab" @tab-change="loadOrders">
        <el-tab-pane label="我的购买" name="purchases" />
        <el-tab-pane label="我的出售" name="sales" />
      </el-tabs>
    </div>

    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-meta">
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-time">{{ formatDate(order.createdAt) }}</span>
          </div>
          <span :class="['status-badge', `status-${order.status.toLowerCase()}`]">
            {{ statusLabel(order.status) }}
          </span>
        </div>

        <div class="order-body" @click="$router.push(`/goods/${order.goodsId}`)">
          <div class="goods-thumb-wrapper">
            <img :src="order.goodsCover || '/default-goods.png'" class="goods-thumb" />
          </div>
          <div class="goods-info">
            <p class="goods-title">{{ order.goodsTitle }}</p>
            <p class="goods-price">
              <span class="price-symbol">¥</span>{{ order.price }}
            </p>
            <p class="counterpart">
              <span class="counterpart-label">{{ activeTab === 'purchases' ? '卖家' : '买家' }}</span>
              {{ activeTab === 'purchases' ? order.sellerName : order.buyerName }}
            </p>
          </div>
        </div>

        <div class="order-footer">
          <el-button
            v-if="activeTab === 'sales' && order.status === 'PENDING'"
            class="action-btn action-btn-primary"
            size="small"
            @click="handleConfirm(order.id)"
          >确认订单</el-button>

          <el-button
            v-if="activeTab === 'purchases' && order.status === 'CONFIRMED'"
            class="action-btn action-btn-success"
            size="small"
            @click="handleFinish(order.id)"
          >确认收货</el-button>

          <el-button
            v-if="activeTab === 'purchases' && order.status === 'PENDING'"
            class="action-btn action-btn-danger"
            size="small"
            @click="handleCancel(order.id)"
          >取消订单</el-button>

          <el-button
            v-if="order.status === 'FINISHED' && !hasReviewed(order)"
            class="action-btn action-btn-warning"
            size="small"
            @click="openReview(order)"
          >去评价</el-button>

          <el-button
            class="action-btn action-btn-chat"
            size="small"
            :icon="ChatDotRound"
            @click="goChat(order)"
          >联系{{ activeTab === 'purchases' ? '卖家' : '买家' }}</el-button>
        </div>
      </div>

      <div v-if="!loading && !orders.length" class="empty-state">
        <el-empty description="暂无订单" />
      </div>
    </div>

    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        class="pagination"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="reviewDialog" title="交易评价" width="460px" class="review-dialog" :close-on-click-modal="false">
      <div class="review-content">
        <div class="review-field">
          <label class="review-label">评分</label>
          <el-rate v-model="reviewForm.score" :max="5" show-text />
        </div>
        <div class="review-field">
          <label class="review-label">评价</label>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="分享你的交易体验..."
            resize="none"
          />
        </div>
      </div>
      <template #footer>
        <div class="review-footer">
          <el-button class="action-btn action-btn-cancel" @click="reviewDialog = false">取消</el-button>
          <el-button class="action-btn action-btn-primary" @click="submitReview" :loading="submitting">提交评价</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '../utils/request'
import { useUserStore } from '../stores/user'

const router    = useRouter()
const userStore = useUserStore()

const activeTab   = ref('purchases')
const loading     = ref(false)
const orders      = ref([])
const total       = ref(0)
const currentPage = ref(1)
const pageSize    = 10

const reviewDialog  = ref(false)
const submitting    = ref(false)
const currentOrder  = ref(null)
const reviewForm    = reactive({ orderId: null, score: 5, content: '' })

onMounted(loadOrders)

async function loadOrders() {
  loading.value = true
  try {
    const api = activeTab.value === 'purchases' ? '/orders/purchases' : '/orders/sales'
    const res = await request.get(api, { params: { page: currentPage.value - 1, size: pageSize } })
    orders.value = res.data.data?.list || []
    total.value  = res.data.data?.total || 0
  } finally { loading.value = false }
}

async function handleConfirm(id) {
  await ElMessageBox.confirm('确认已与买家完成交易安排？', '确认订单', { type: 'info' })
  await request.put(`/orders/${id}/confirm`)
  ElMessage.success('订单已确认')
  loadOrders()
}

async function handleFinish(id) {
  await ElMessageBox.confirm('确认已收到商品？确认后订单将完成', '确认收货', { type: 'success' })
  await request.put(`/orders/${id}/finish`)
  ElMessage.success('订单已完成')
  loadOrders()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确认取消此订单？', '取消订单', { type: 'warning' })
  await request.put(`/orders/${id}/cancel`)
  ElMessage.success('订单已取消')
  loadOrders()
}

function openReview(order) {
  currentOrder.value     = order
  reviewForm.orderId     = order.id
  reviewForm.score       = 5
  reviewForm.content     = ''
  reviewDialog.value     = true
}

async function submitReview() {
  if (!reviewForm.score) return ElMessage.warning('请打分')
  submitting.value = true
  try {
    await request.post('/orders/review', { ...reviewForm })
    ElMessage.success('评价成功')
    reviewDialog.value = false
    loadOrders()
  } finally { submitting.value = false }
}

async function goChat(order) {
  try {
    const sellerId = activeTab.value === 'purchases' ? order.sellerId : order.buyerId
    const res = await request.post('/chat/sessions', { sellerId, goodsId: order.goodsId })
    router.push(`/chat/${res.data.data.sessionId}`)
  } catch (e) {
    ElMessage.error('无法创建会话，请稍后重试')
  }
}

const hasReviewed = o =>
  activeTab.value === 'purchases' ? o.buyerReviewed : o.sellerReviewed

const formatDate = d => d ? dayjs(d).format('MM-DD HH:mm') : ''
const statusType  = s => ({ PENDING:'warning', CONFIRMED:'primary', FINISHED:'success', CANCELED:'info' }[s] || '')
const statusLabel = s => ({ PENDING:'待确认', CONFIRMED:'已确认', FINISHED:'已完成', CANCELED:'已取消' }[s] || s)
</script>

<style scoped>
.order-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 0 16px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header {
  margin-bottom: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.tabs-wrapper {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  padding: 4px 20px 0;
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.06);
  margin-bottom: 20px;
}

.tabs-wrapper :deep(.el-tabs__header) {
  margin: 0;
}

.tabs-wrapper :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tabs-wrapper :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  transition: color 0.3s ease;
}

.tabs-wrapper :deep(.el-tabs__item.is-active) {
  color: var(--primary);
}

.tabs-wrapper :deep(.el-tabs__item:hover) {
  color: var(--primary);
}

.tabs-wrapper :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.order-card {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.06);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.12);
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-no {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  font-family: 'SF Mono', 'Fira Code', monospace;
  letter-spacing: 0.3px;
}

.order-time {
  font-size: 12px;
  color: var(--text-muted);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.status-pending {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: #fff;
}

.status-confirmed {
  background: var(--gradient-primary);
  color: #fff;
}

.status-finished {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: #fff;
}

.status-canceled {
  background: linear-gradient(135deg, #cbd5e1 0%, #94a3b8 100%);
  color: #fff;
}

.order-body {
  display: flex;
  gap: 16px;
  padding: 18px 20px;
  cursor: pointer;
  transition: background 0.25s ease;
}

.order-body:hover {
  background: rgba(102, 126, 234, 0.03);
}

.goods-thumb-wrapper {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.goods-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.order-body:hover .goods-thumb {
  transform: scale(1.05);
}

.goods-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  min-width: 0;
}

.goods-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  line-height: 1.2;
}

.price-symbol {
  font-size: 14px;
  font-weight: 700;
}

.counterpart {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.counterpart-label {
  color: var(--text-muted);
  margin-right: 4px;
}

.order-footer {
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 1px solid var(--border-light);
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 10px !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  padding: 8px 18px !important;
  height: auto !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.action-btn-primary {
  background: var(--gradient-primary) !important;
  color: #fff !important;
}

.action-btn-primary:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4) !important;
  transform: translateY(-1px);
}

.action-btn-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%) !important;
  color: #fff !important;
}

.action-btn-success:hover {
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.4) !important;
  transform: translateY(-1px);
}

.action-btn-danger {
  background: rgba(239, 68, 68, 0.1) !important;
  color: #ef4444 !important;
}

.action-btn-danger:hover {
  background: rgba(239, 68, 68, 0.18) !important;
  transform: translateY(-1px);
}

.action-btn-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%) !important;
  color: #fff !important;
}

.action-btn-warning:hover {
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4) !important;
  transform: translateY(-1px);
}

.action-btn-chat {
  background: rgba(102, 126, 234, 0.08) !important;
  color: var(--primary) !important;
  margin-left: auto !important;
}

.action-btn-chat:hover {
  background: rgba(102, 126, 234, 0.16) !important;
  transform: translateY(-1px);
}

.action-btn-cancel {
  background: rgba(100, 116, 139, 0.08) !important;
  color: var(--text-secondary) !important;
}

.action-btn-cancel:hover {
  background: rgba(100, 116, 139, 0.15) !important;
}

.empty-state {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  padding: 60px 20px;
}

.empty-state :deep(.el-empty__description p) {
  color: var(--text-muted);
  font-size: 15px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  padding-bottom: 20px;
}

.pagination-wrapper :deep(.el-pagination) {
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-md);
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.06);
}

.pagination-wrapper :deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 8px;
  font-weight: 700;
}

.pagination-wrapper :deep(.el-pager li) {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.25s ease;
}

.pagination-wrapper :deep(.el-pager li:hover) {
  color: var(--primary);
}

.review-dialog :deep(.el-dialog) {
  border-radius: var(--radius-xl) !important;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.review-dialog :deep(.el-dialog__header) {
  background: var(--gradient-primary);
  padding: 20px 24px;
  margin: 0;
}

.review-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.review-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
}

.review-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #fff;
}

.review-dialog :deep(.el-dialog__body) {
  padding: 28px 24px 12px;
}

.review-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 24px;
}

.review-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.review-content :deep(.el-rate) {
  height: auto;
}

.review-content :deep(.el-rate__icon) {
  font-size: 28px;
}

.review-content :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: 14px;
  font-size: 14px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.review-content :deep(.el-textarea__inner:focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}

.review-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
