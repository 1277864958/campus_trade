import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null
let onConnectCallback = null
const subscribers = new Map()

export function connect(token, onConnect) {
  if (stompClient?.connected) {
    if (onConnect) onConnect()
    return
  }

  onConnectCallback = onConnect

  if (stompClient) {
    try { stompClient.deactivate() } catch (_) {}
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,

    onConnect: () => {
      console.log('[WS] 连接成功')
      subscribers.forEach((cb, dest) => {
        stompClient.subscribe(dest, frame => {
          try { cb(JSON.parse(frame.body)) } catch (_) {}
        })
      })
      if (onConnectCallback) onConnectCallback()
    },

    onDisconnect: () => console.log('[WS] 已断开'),

    onStompError: frame => {
      console.error('[WS] STOMP 错误', frame.headers?.message)
    },

    onWebSocketError: (evt) => {
      console.error('[WS] WebSocket 连接错误', evt)
    },
  })

  stompClient.activate()
}

export function disconnect() {
  if (stompClient) {
    try { stompClient.deactivate() } catch (_) {}
    stompClient = null
  }
  subscribers.clear()
  onConnectCallback = null
}

export function subscribe(destination, callback) {
  subscribers.set(destination, callback)
  if (stompClient?.connected) {
    stompClient.subscribe(destination, frame => {
      try { callback(JSON.parse(frame.body)) } catch (_) {}
    })
  }
}

export function unsubscribe(destination) {
  subscribers.delete(destination)
}

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
