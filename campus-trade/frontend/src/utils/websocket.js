import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null
const subscribers = new Map()

/**
 * 连接 WebSocket（STOMP over SockJS）
 * @param {string} token  JWT access token
 * @param {function} onConnect 连接成功回调
 */
export function connect(token, onConnect) {
  if (stompClient?.connected) return

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,

    onConnect: () => {
      console.log('[WS] 连接成功')
      // 恢复所有订阅
      subscribers.forEach((cb, dest) => {
        stompClient.subscribe(dest, frame => cb(JSON.parse(frame.body)))
      })
      if (onConnect) onConnect()
    },

    onDisconnect: () => console.log('[WS] 已断开'),
    onStompError: frame => console.error('[WS] STOMP 错误', frame),
  })

  stompClient.activate()
}

/**
 * 断开连接
 */
export function disconnect() {
  stompClient?.deactivate()
  stompClient = null
  subscribers.clear()
}

/**
 * 订阅主题
 * @param {string}   destination  如 /topic/chat/1 或 /user/queue/notify
 * @param {function} callback     收到消息时的回调，参数为解析后的 JSON 对象
 */
export function subscribe(destination, callback) {
  subscribers.set(destination, callback)
  if (stompClient?.connected) {
    stompClient.subscribe(destination, frame => callback(JSON.parse(frame.body)))
  }
}

/**
 * 取消订阅
 */
export function unsubscribe(destination) {
  subscribers.delete(destination)
}

/**
 * 发送消息
 * @param {object} payload  { chatId, content, contentType }
 */
export function sendMessage(payload) {
  if (!stompClient?.connected) {
    console.warn('[WS] 未连接，无法发送消息')
    return false
  }
  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify(payload),
  })
  return true
}

export const isConnected = () => stompClient?.connected ?? false
