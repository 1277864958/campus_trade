<template>
  <div class="chat-page">
    <!-- 左：会话列表 -->
    <div class="session-panel">
      <div class="panel-header">消息列表</div>
      <div v-loading="sessionsLoading" class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          :class="['session-item', { active: currentSessionId === s.sessionId }]"
          @click="selectSession(s)"
        >
          <el-avatar :src="s.otherUserAvatar" :size="40">{{ s.otherUserName?.[0] }}</el-avatar>
          <div class="session-info">
            <div class="session-top">
              <span class="session-name">{{ s.otherUserName }}</span>
              <span class="session-time">{{ formatTime(s.lastMessage?.createdAt) }}</span>
            </div>
            <div class="session-preview">
              <span class="last-msg">{{ s.lastMessage?.content || s.goodsTitle }}</span>
              <el-badge v-if="s.unreadCount" :value="s.unreadCount" class="unread-badge" />
            </div>
          </div>
        </div>
        <el-empty v-if="!sessionsLoading && !sessions.length" description="暂无会话" :image-size="60" />
      </div>
    </div>

    <!-- 右：聊天窗口 -->
    <div class="chat-panel">
      <template v-if="currentSession">
        <!-- 顶部标题 -->
        <div class="chat-header">
          <el-avatar :src="currentSession.otherUserAvatar" :size="32">
            {{ currentSession.otherUserName?.[0] }}
          </el-avatar>
          <div>
            <p class="chat-name">{{ currentSession.otherUserName }}</p>
            <p class="chat-goods">咨询：{{ currentSession.goodsTitle }}</p>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="msg-area" ref="msgAreaRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['msg-row', msg.senderId === userStore.userId ? 'self' : 'other']"
          >
            <el-avatar v-if="msg.senderId !== userStore.userId"
                       :src="currentSession.otherUserAvatar" :size="32">
              {{ currentSession.otherUserName?.[0] }}
            </el-avatar>
            <div class="bubble-wrap">
              <div class="bubble" :class="msg.contentType === 'IMAGE' ? 'img-bubble' : ''">
                <img v-if="msg.contentType === 'IMAGE'" :src="msg.content" class="msg-img" />
                <span v-else>{{ msg.content }}</span>
              </div>
              <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <el-button
          v-if="hasMore"
          text size="small"
          class="load-more"
          @click="loadMore"
          :loading="historyLoading"
        >加载更多历史消息</el-button>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 图片上传 -->
          <el-upload
            action="/api/files/image"
            :headers="uploadHeaders"
            :show-file-list="false"
            accept="image/*"
            :on-success="onImageSend"
          >
            <el-button :icon="Picture" circle />
          </el-upload>

          <el-input
            v-model="inputMsg"
            placeholder="输入消息，回车发送..."
            @keyup.enter.exact="sendMsg"
            class="msg-input"
            :maxlength="500"
          />
          <el-button type="primary" @click="sendMsg" :disabled="!inputMsg.trim()">发送</el-button>
        </div>
      </template>

      <!-- 未选中会话 -->
      <el-empty v-else description="选择一个会话开始聊天" class="no-chat" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'
import * as ws from '../utils/websocket'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'

const route     = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()

const sessions        = ref([])
const messages        = ref([])
const sessionsLoading = ref(false)
const historyLoading  = ref(false)
const inputMsg        = ref('')
const msgAreaRef      = ref(null)
const currentSessionId = ref(null)
const currentSession   = ref(null)
const currentPage      = ref(0)
const hasMore          = ref(false)

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('access_token')}`
}))

onMounted(async () => {
  await loadSessions()
  // 从路由参数选中会话
  if (route.params.sessionId) {
    const s = sessions.value.find(s => s.sessionId == route.params.sessionId)
    if (s) selectSession(s)
  }
  // 连接 WebSocket
  ws.connect(localStorage.getItem('access_token'), () => {
    // 订阅个人未读推送
    ws.subscribe(`/user/${userStore.userId}/queue/notify`, data => {
      chatStore.setUnread(data.sessionId, data.unreadCount)
      // 更新会话列表中的未读数
      const s = sessions.value.find(s => s.sessionId === data.sessionId)
      if (s) s.unreadCount = data.unreadCount
    })
  })
})

onUnmounted(() => {
  if (currentSessionId.value) {
    ws.unsubscribe(`/topic/chat/${currentSessionId.value}`)
  }
})

async function loadSessions() {
  sessionsLoading.value = true
  try {
    const res = await request.get('/chat/sessions')
    sessions.value = res.data.data || []
  } finally { sessionsLoading.value = false }
}

async function selectSession(s) {
  // 取消旧订阅
  if (currentSessionId.value) {
    ws.unsubscribe(`/topic/chat/${currentSessionId.value}`)
  }
  currentSession.value   = s
  currentSessionId.value = s.sessionId
  messages.value         = []
  currentPage.value      = 0
  hasMore.value          = false

  await loadHistory()

  // 订阅新会话
  ws.subscribe(`/topic/chat/${s.sessionId}`, msg => {
    messages.value.push(msg)
    scrollBottom()
  })

  // 清零未读
  s.unreadCount = 0
  chatStore.clearUnread(s.sessionId)
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await request.get(
      `/chat/sessions/${currentSessionId.value}/messages`,
      { params: { page: currentPage.value, size: 20 } }
    )
    const data = res.data.data
    const newMsgs = data.list || []
    if (currentPage.value === 0) {
      messages.value = newMsgs
      await nextTick(); scrollBottom()
    } else {
      messages.value = [...newMsgs, ...messages.value]
    }
    hasMore.value = data.total > (currentPage.value + 1) * 20
  } finally { historyLoading.value = false }
}

async function loadMore() {
  currentPage.value++
  await loadHistory()
}

function sendMsg() {
  const content = inputMsg.value.trim()
  if (!content) return
  const ok = ws.sendMessage({ chatId: currentSessionId.value, content, contentType: 'TEXT' })
  if (!ok) { ElMessage.error('发送失败，请检查网络连接'); return }
  inputMsg.value = ''
}

function onImageSend(res) {
  if (res.code !== 200) return ElMessage.error('图片发送失败')
  ws.sendMessage({ chatId: currentSessionId.value, content: res.data, contentType: 'IMAGE' })
}

function scrollBottom() {
  nextTick(() => {
    if (msgAreaRef.value) msgAreaRef.value.scrollTop = msgAreaRef.value.scrollHeight
  })
}

const formatTime = d => {
  if (!d) return ''
  const t = dayjs(d)
  return dayjs().diff(t, 'hour') < 24 ? t.format('HH:mm') : t.format('MM-DD HH:mm')
}
</script>

<style scoped>
.chat-page { display:flex; height:calc(100vh - 110px); background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 2px 12px rgba(0,0,0,.08); }

/* 左侧会话列表 */
.session-panel { width:260px; border-right:1px solid #f0f0f0; display:flex; flex-direction:column; flex-shrink:0; }
.panel-header { padding:16px; font-weight:700; font-size:16px; border-bottom:1px solid #f0f0f0; }
.session-list { flex:1; overflow-y:auto; }
.session-item { display:flex; align-items:center; gap:10px; padding:12px 16px; cursor:pointer; transition:background .15s; }
.session-item:hover { background:#f8f9fa; }
.session-item.active { background:#e8f5e9; }
.session-info { flex:1; overflow:hidden; }
.session-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:4px; }
.session-name { font-weight:500; font-size:14px; }
.session-time { font-size:11px; color:#999; }
.session-preview { display:flex; justify-content:space-between; align-items:center; }
.last-msg { font-size:12px; color:#999; overflow:hidden; white-space:nowrap; text-overflow:ellipsis; max-width:130px; }

/* 右侧聊天窗口 */
.chat-panel { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.chat-header { padding:12px 16px; border-bottom:1px solid #f0f0f0; display:flex; align-items:center; gap:10px; }
.chat-name   { font-weight:600; font-size:15px; }
.chat-goods  { font-size:12px; color:#999; }

.msg-area { flex:1; overflow-y:auto; padding:16px; display:flex; flex-direction:column; gap:12px; }
.load-more { align-self:center; }

.msg-row { display:flex; align-items:flex-end; gap:8px; }
.msg-row.self { flex-direction:row-reverse; }

.bubble-wrap { display:flex; flex-direction:column; max-width:60%; }
.msg-row.self .bubble-wrap { align-items:flex-end; }

.bubble { padding:10px 14px; border-radius:18px; background:#f0f0f0; font-size:14px; line-height:1.6; word-break:break-all; }
.msg-row.self .bubble { background:#2e7d32; color:#fff; }
.img-bubble { padding:4px; }
.msg-img { max-width:200px; max-height:200px; border-radius:8px; display:block; }
.msg-time { font-size:11px; color:#ccc; margin-top:4px; }

.input-area { border-top:1px solid #f0f0f0; padding:12px 16px; display:flex; align-items:center; gap:8px; }
.msg-input { flex:1; }

.no-chat { flex:1; display:flex; align-items:center; justify-content:center; }
</style>
