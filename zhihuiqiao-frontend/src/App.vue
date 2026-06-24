<template>
  <router-view />
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { useWebSocket } from '@/composables/useWebSocket'

const userStore = useUserStore()
const notificationStore = useNotificationStore()
const { connect, disconnect } = useWebSocket()

/**
 * 应用启动时，如果用户已登录则初始化通知状态并建立 WebSocket 连接
 */
onMounted(() => {
  if (userStore.isLoggedIn) {
    notificationStore.fetchUnreadCount()
    connect()
  }
})

/**
 * 监听登录状态变化：登录时连接 WebSocket，登出时断开连接
 */
watch(
  () => userStore.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) {
      notificationStore.fetchUnreadCount()
      connect()
    } else {
      disconnect()
    }
  }
)
</script>
