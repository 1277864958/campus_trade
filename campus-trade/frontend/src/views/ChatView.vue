<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="session-panel">
        <div class="panel-header">
          <div class="panel-header-inner">
            <span class="panel-title">消息列表</span>
            <span class="session-count">{{ sessions.length }}</span>
          </div>
        </div>
        <div v-loading="sessionsLoading" class="session-list">
          <div
            v-for="s in sessions"
            :key="s.sessionId"
            :class="['session-item', { active: currentSessionId === s.sessionId }]"
            @click="selectSession(s)"
          >
            <div class="session-avatar-wrap">
              <el-avatar :src="s.otherUserAvatar" :size="44">{{ s.otherUserName?.[0] }}</el-avatar>
              <span v-if="s.unreadCount" class="online-dot"></span>
            </div>
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
          <el-empty v-if="!sessionsLoading && !sessions.length" description="暂无会话" :image-size="80" class="empty-sessions" />
        </div>
      </div>

      <div class="chat-panel">
        <template v-if="currentSession">
          <div class="chat-header">
            <div class="chat-header-left">
              <el-avatar :src="currentSession.otherUserAvatar" :size="38" class="chat-header-avatar">
                {{ currentSession.otherUserName?.[0] }}
              </el-avatar>
              <div class="chat-header-info">
                <p class="chat-name">{{ currentSession.otherUserName }}</p>
                <p class="chat-goods">
                  <span class="goods-tag">咨询</span>
                  {{ currentSession.goodsTitle }}
                </p>
              </div>
            </div>
          </div>

          <div class="msg-area" ref="msgAreaRef">
            <el-button
              v-if="hasMore"
              text
              size="small"
              class="load-more"
              @click="loadMore"
              :loading="historyLoading"
            >加载更多历史消息</el-button>
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['msg-row', msg.senderId === userStore.userId ? 'self' : 'other']"
            >
              <el-avatar
                v-if="msg.senderId !== userStore.userId"
                :src="currentSession.otherUserAvatar"
                :size="34"
                class="msg-avatar"
              >
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

          <div class="input-area">
            <div class="input-inner">
              <el-upload
                action="/api/files/image"
                :headers="uploadHeaders"
                :show-file-list="false"
                accept="image/*"
                :on-success="onImageSend"
              >
                <el-button :icon="Picture" circle class="upload-btn" />
              </el-upload>
              <el-input
                v-model="inputMsg"
                placeholder="输入消息，回车发送..."
                @keyup.enter.exact="sendMsg"
                class="msg-input"
                :maxlength="500"
              />
              <el-button class="send-btn" @click="sendMsg" :disabled="!inputMsg.trim()">发送</el-button>
            </div>
          </div>
        </template>

        <div v-else class="no-chat">
          <div class="no-chat-inner">
            <div class="no-chat-icon">💬</div>
            <p class="no-chat-title">开始聊天</p>
            <p class="no-chat-desc">选择左侧会话开始交流</p>
          </div>
        </div>
      </div>
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
  await trySelectFromRoute()
  connectWs()
})

watch(() => route.params.sessionId, async (newId) => {
  if (newId) {
    await loadSessions()
    await trySelectFromRoute()
  }
})

async function trySelectFromRoute() {
  const sid = route.params.sessionId
  if (!sid) return
  let s = sessions.value.find(s => s.sessionId == sid)
  if (!s) {
    await loadSessions()
    s = sessions.value.find(s => s.sessionId == sid)
  }
  if (s) selectSession(s)
}

function connectWs() {
  ws.connect(localStorage.getItem('access_token'), () => {
    ws.subscribe(`/user/${userStore.userId}/queue/notify`, data => {
      chatStore.setUnread(data.sessionId, data.unreadCount)
      const s = sessions.value.find(s => s.sessionId === data.sessionId)
      if (s) s.unreadCount = data.unreadCount
    })
  })
}

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
  if (currentSessionId.value) {
    ws.unsubscribe(`/topic/chat/${currentSessionId.value}`)
  }
  currentSession.value   = s
  currentSessionId.value = s.sessionId
  messages.value         = []
  currentPage.value      = 0
  hasMore.value          = false

  await loadHistory()

  ws.subscribe(`/topic/chat/${s.sessionId}`, msg => {
    messages.value.push(msg)
    scrollBottom()
  })

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

async function sendMsg() {
  const content = inputMsg.value.trim()
  if (!content) return
  const payload = { chatId: currentSessionId.value, content, contentType: 'TEXT' }
  const ok = ws.sendMessage(payload)
  if (!ok) {
    try {
      const res = await request.post('/chat/send', payload)
      messages.value.push(res.data.data)
      scrollBottom()
    } catch (e) {
      ElMessage.error('发送失败，请检查网络连接')
      return
    }
  }
  inputMsg.value = ''
}

async function onImageSend(res) {
  if (res.code !== 200) return ElMessage.error('图片发送失败')
  const payload = { chatId: currentSessionId.value, content: res.data, contentType: 'IMAGE' }
  const ok = ws.sendMessage(payload)
  if (!ok) {
    try {
      const httpRes = await request.post('/chat/send', payload)
      messages.value.push(httpRes.data.data)
      scrollBottom()
    } catch (e) {
      ElMessage.error('图片发送失败')
    }
  }
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
.chat-page {
  height: calc(100vh - 120px);
  padding: 0;
}

.chat-container {
  display: flex;
  height: 100%;
  background: var(--bg-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.session-panel {
  width: 300px;
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(248, 250, 252, 0.5);
}

.panel-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--border-light);
}

.panel-header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-weight: 700;
  font-size: 18px;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.session-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 12px;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-list::-webkit-scrollbar {
  width: 4px;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 4px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.25s ease;
  margin-bottom: 4px;
  position: relative;
}

.session-item:hover {
  background: rgba(102, 126, 234, 0.06);
}

.session-item.active {
  background: var(--gradient-primary);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.session-item.active .session-name,
.session-item.active .session-time,
.session-item.active .last-msg {
  color: #fff;
}

.session-item.active .session-time {
  opacity: 0.8;
}

.session-item.active .last-msg {
  opacity: 0.85;
}

.session-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.session-avatar-wrap :deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.session-item.active .session-avatar-wrap :deep(.el-avatar) {
  border-color: rgba(255, 255, 255, 0.4);
}

.online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  background: #22c55e;
  border: 2px solid #fff;
  border-radius: 50%;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  transition: color 0.25s ease;
}

.session-time {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
  transition: color 0.25s ease;
}

.session-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.last-msg {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  flex: 1;
  transition: color 0.25s ease;
}

.unread-badge :deep(.el-badge__content) {
  background: var(--gradient-primary);
  border: none;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
}

.session-item.active .unread-badge :deep(.el-badge__content) {
  background: #fff;
  color: var(--primary);
}

.empty-sessions {
  padding: 40px 0;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.4);
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-header-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-header-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.chat-name {
  font-weight: 700;
  font-size: 15px;
  color: var(--text-primary);
  letter-spacing: -0.2px;
}

.chat-goods {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.goods-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 6px;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: var(--primary);
  font-size: 11px;
  font-weight: 600;
}

.msg-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.3) 0%, rgba(255, 255, 255, 0.1) 100%);
}

.msg-area::-webkit-scrollbar {
  width: 5px;
}

.msg-area::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.25);
  border-radius: 5px;
}

.load-more {
  align-self: center;
  color: var(--primary);
  font-size: 13px;
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(102, 126, 234, 0.06);
  transition: all 0.2s ease;
}

.load-more:hover {
  background: rgba(102, 126, 234, 0.12);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  animation: msgFadeIn 0.3s ease;
}

@keyframes msgFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.msg-row.self {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  margin-top: 2px;
}

.bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: 65%;
}

.msg-row.self .bubble-wrap {
  align-items: flex-end;
}

.bubble {
  padding: 12px 18px;
  border-radius: 20px;
  background: rgba(241, 245, 249, 0.9);
  font-size: 14px;
  line-height: 1.65;
  word-break: break-all;
  color: var(--text-primary);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.bubble:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.msg-row.other .bubble {
  border-radius: 4px 20px 20px 20px;
}

.msg-row.self .bubble {
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: 20px 4px 20px 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.msg-row.self .bubble:hover {
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.35);
}

.img-bubble {
  padding: 6px;
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.msg-row.self .img-bubble {
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.msg-img {
  max-width: 220px;
  max-height: 220px;
  border-radius: 14px;
  display: block;
  object-fit: cover;
}

.msg-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 5px;
  padding: 0 4px;
}

.input-area {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--border-light);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.input-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 8px 8px 12px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.input-inner:focus-within {
  background: #fff;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.upload-btn {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  width: 36px;
  height: 36px;
}

.upload-btn:hover {
  color: var(--primary);
  background: rgba(102, 126, 234, 0.08);
}

.msg-input {
  flex: 1;
}

.msg-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding: 0;
}

.msg-input :deep(.el-input__inner) {
  font-size: 14px;
}

.send-btn {
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 600;
  padding: 8px 24px;
  height: 38px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: var(--gradient-primary);
  color: #fff;
}

.no-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-chat-inner {
  text-align: center;
}

.no-chat-icon {
  font-size: 56px;
  margin-bottom: 16px;
  filter: grayscale(0.2);
}

.no-chat-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.no-chat-desc {
  font-size: 14px;
  color: var(--text-muted);
}
</style>
