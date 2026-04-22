<template>
  <div class="order-page">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="我的购买" name="purchases" />
      <el-tab-pane label="我的出售" name="sales" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusType(order.status)">{{ statusLabel(order.status) }}</el-tag>
          <span class="order-time">{{ formatDate(order.createdAt) }}</span>
        </div>

        <!-- 商品信息 -->
        <div class="order-body" @click="$router.push(`/goods/${order.goodsId}`)">
          <img :src="order.goodsCover || '/default-goods.png'" class="goods-thumb" />
          <div class="goods-info">
            <p class="goods-title">{{ order.goodsTitle }}</p>
            <p class="goods-price">¥{{ order.price }}</p>
            <p class="counterpart">
              {{ activeTab === 'purchases' ? '卖家：' : '买家：' }}
              {{ activeTab === 'purchases' ? order.sellerName : order.buyerName }}
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-footer">
          <!-- 卖家：确认订单 -->
          <el-button
            v-if="activeTab === 'sales' && order.status === 'PENDING'"
            type="primary" size="small"
            @click="handleConfirm(order.id)"
          >确认订单</el-button>

          <!-- 买家：确认收货 -->
          <el-button
            v-if="activeTab === 'purchases' && order.status === 'CONFIRMED'"
            type="success" size="small"
            @click="handleFinish(order.id)"
          >确认收货</el-button>

          <!-- 买家：取消订单 -->
          <el-button
            v-if="activeTab === 'purchases' && order.status === 'PENDING'"
            type="danger" plain size="small"
            @click="handleCancel(order.id)"
          >取消订单</el-button>

          <!-- 评价（已完成且未评价） -->
          <el-button
            v-if="order.status === 'FINISHED' && !hasReviewed(order)"
            type="warning" plain size="small"
            @click="openReview(order)"
          >去评价</el-button>

          <!-- 联系对方 -->
          <el-button
            size="small" :icon="ChatDotRound"
            @click="goChat(order)"
          >联系{{ activeTab === 'purchases' ? '卖家' : '买家' }}</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && !orders.length" description="暂无订单" />
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > pageSize"
      class="pagination"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadOrders"
    />

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialog" title="交易评价" width="420px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.score" :max="5" show-text />
        </el-form-item>
        <el-form-item label="评价">
          <el-input
            v-model="reviewForm.content"
            type="textarea" :rows="3"
            maxlength="500" show-word-limit
            placeholder="分享你的交易体验..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
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
  const sellerId = activeTab.value === 'purchases' ? order.sellerId : order.buyerId
  const res = await request.post('/chat/sessions', { sellerId, goodsId: order.goodsId })
  router.push(`/chat/${res.data.data.sessionId}`)
}

const hasReviewed = o =>
  activeTab.value === 'purchases' ? o.buyerReviewed : o.sellerReviewed

const formatDate = d => d ? dayjs(d).format('MM-DD HH:mm') : ''
const statusType  = s => ({ PENDING:'warning', CONFIRMED:'primary', FINISHED:'success', CANCELED:'info' }[s] || '')
const statusLabel = s => ({ PENDING:'待确认', CONFIRMED:'已确认', FINISHED:'已完成', CANCELED:'已取消' }[s] || s)
</script>

<style scoped>
.order-page { max-width:900px; margin:0 auto; }
.page-title { font-size:22px; font-weight:700; margin-bottom:20px; }
.order-list { display:flex; flex-direction:column; gap:16px; margin-top:16px; }
.order-card { background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,.06); }
.order-header { display:flex; align-items:center; gap:12px; padding:12px 16px; background:#f8f9fa; font-size:13px; }
.order-no { color:#666; flex:1; }
.order-time { color:#999; margin-left:auto; }
.order-body { display:flex; gap:12px; padding:16px; cursor:pointer; }
.order-body:hover { background:#fafafa; }
.goods-thumb { width:80px; height:80px; border-radius:6px; object-fit:cover; flex-shrink:0; }
.goods-title { font-weight:500; margin-bottom:6px; }
.goods-price { color:#e53935; font-weight:700; font-size:16px; margin-bottom:4px; }
.counterpart { font-size:13px; color:#666; }
.order-footer { padding:12px 16px; display:flex; gap:8px; border-top:1px solid #f0f0f0; }
.pagination { margin-top:24px; justify-content:center; }
</style>
