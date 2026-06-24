import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUnreadCount } from '@/api/notification'

/**
 * 通知消息对象
 */
export interface NotificationMessage {
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
 * 消息通知 Store
 * 管理未读通知数量、最近收到的通知列表，并提供实时推送状态
 */
export const useNotificationStore = defineStore('notification', () => {
  // 未读通知数量
  const unreadCount = ref(0)
  // 最近收到的通知列表（最多保留 20 条）
  const recentNotifications = ref<NotificationMessage[]>([])
  // WebSocket 连接状态
  const isConnected = ref(false)

  const hasUnread = computed(() => unreadCount.value > 0)

  /**
   * 从后端获取未读通知数量
   */
  async function fetchUnreadCount() {
    try {
      const res: any = await getUnreadCount()
      unreadCount.value = res.data || 0
    } catch (error) {
      console.error('获取未读通知数量失败', error)
    }
  }

  /**
   * 收到一条新的实时通知
   */
  function receiveNotification(notification: NotificationMessage) {
    recentNotifications.value.unshift(notification)
    if (recentNotifications.value.length > 20) {
      recentNotifications.value.pop()
    }
    unreadCount.value += 1
  }

  /**
   * 设置未读数量
   */
  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  /**
   * 减少未读数量
   */
  function decreaseUnreadCount(count: number = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  /**
   * 设置 WebSocket 连接状态
   */
  function setConnected(connected: boolean) {
    isConnected.value = connected
  }

  return {
    unreadCount,
    recentNotifications,
    isConnected,
    hasUnread,
    fetchUnreadCount,
    receiveNotification,
    setUnreadCount,
    decreaseUnreadCount,
    setConnected
  }
})
