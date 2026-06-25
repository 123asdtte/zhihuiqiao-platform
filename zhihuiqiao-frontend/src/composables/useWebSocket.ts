import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { ElNotification, ElMessage } from 'element-plus'
import router from '@/router'

/**
 * 判断 JWT Token 是否已过期
 */
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (!payload.exp) return false
    return Date.now() >= payload.exp * 1000
  } catch (error) {
    return true
  }
}

/**
 * WebSocket 连接状态类型
 */
type WebSocketStatus = 'connecting' | 'connected' | 'disconnected' | 'error'

/**
 * 通知消息类型
 */
interface NotificationMessage {
  id: number
  title: string
  content: string
  type: string
  relatedId?: number
  relatedType?: string
  isRead: number
  createTime: string
}

/**
 * WebSocket 实时通知 composable
 * 用于建立与后端的 WebSocket 连接，接收实时通知推送
 */
export function useWebSocket() {
  const userStore = useUserStore()
  const notificationStore = useNotificationStore()

  // WebSocket 实例
  let socket: WebSocket | null = null
  // 重连定时器
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  // 当前连接状态
  const status = ref<WebSocketStatus>('disconnected')
  // 是否主动关闭连接
  let isManualClose = false

  /**
   * 建立 WebSocket 连接
   */
  function connect() {
    // 未登录时不连接
    if (!userStore.token) {
      return
    }

    // Token 已过期时不再连接，自动登出并跳转登录页
    if (isTokenExpired(userStore.token)) {
      userStore.logout()
      router.push('/login')
      ElMessage.warning('登录已过期，请重新登录')
      return
    }

    // 避免重复连接
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
      return
    }

    isManualClose = false
    status.value = 'connecting'

    // 构建 WebSocket URL，将 Token 通过 query 参数传递给后端
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/ws/notification?token=${encodeURIComponent(userStore.token)}`

    try {
      socket = new WebSocket(wsUrl)

      socket.onopen = () => {
        status.value = 'connected'
        notificationStore.setConnected(true)
        console.log('WebSocket 连接已建立')
      }

      socket.onmessage = (event) => {
        try {
          const message: NotificationMessage = JSON.parse(event.data)
          // 更新通知 Store
          notificationStore.receiveNotification(message)
          // 桌面右下角弹出提示
          ElNotification({
            title: message.title,
            message: message.content,
            type: 'info',
            duration: 5000
          })
        } catch (error) {
          console.error('解析 WebSocket 消息失败', error)
        }
      }

      socket.onclose = () => {
        status.value = 'disconnected'
        notificationStore.setConnected(false)
        console.log('WebSocket 连接已关闭')
        // 非主动关闭时，尝试自动重连
        if (!isManualClose) {
          scheduleReconnect()
        }
      }

      socket.onerror = (error) => {
        status.value = 'error'
        console.error('WebSocket 连接发生错误', error)
      }
    } catch (error) {
      status.value = 'error'
      console.error('创建 WebSocket 连接失败', error)
    }
  }

  /**
   * 关闭 WebSocket 连接
   */
  function disconnect() {
    isManualClose = true
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (socket) {
      socket.close()
      socket = null
    }
    status.value = 'disconnected'
    notificationStore.setConnected(false)
  }

  /**
   * 自动重连，5 秒后尝试重新建立连接
   */
  function scheduleReconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }
    reconnectTimer = setTimeout(() => {
      console.log('WebSocket 尝试重连...')
      connect()
    }, 5000)
  }

  // 组件卸载时自动关闭连接
  onUnmounted(() => {
    disconnect()
  })

  return {
    status,
    connect,
    disconnect
  }
}
