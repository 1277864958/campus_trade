import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

export const useChatStore = defineStore('chat', () => {
  const unreadMap = ref({})   // { sessionId: count }

  const totalUnread = computed(() =>
    Object.values(unreadMap.value).reduce((a, b) => a + b, 0))

  async function fetchUnread() {
    try {
      const res = await request.get('/chat/unread')
      const list = res.data.data || []
      const map = {}
      list.forEach(item => { map[item.sessionId] = item.unreadCount })
      unreadMap.value = map
    } catch (_) {}
  }

  function setUnread(sessionId, count) {
    unreadMap.value = { ...unreadMap.value, [sessionId]: count }
  }

  function clearUnread(sessionId) {
    setUnread(sessionId, 0)
  }

  return { unreadMap, totalUnread, fetchUnread, setUnread, clearUnread }
})
