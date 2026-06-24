<template>
  <el-container class="main-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🌉</span>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">智汇桥</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="appStore.sidebarCollapsed"
        router
        class="sidebar-menu"
        background-color="#1d1e2c"
        text-color="#a3a6b7"
        active-text-color="#409eff"
      >
        <el-menu-item index="/app/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="research">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>科研撮合</span>
          </template>
          <el-menu-item index="/app/research/projects">科研项目</el-menu-item>
          <el-menu-item index="/app/research/demands">企业需求</el-menu-item>
          <el-menu-item v-if="userStore.isStudent" index="/app/research/applications">我的申请</el-menu-item>
          <el-menu-item v-if="userStore.isStudent || userStore.isTeacher" index="/app/research/profile">科研画像</el-menu-item>
          <el-menu-item v-if="userStore.isTeacher || userStore.isEnterprise || userStore.isAdmin" index="/app/research/project/publish">发布科研项目</el-menu-item>
          <el-menu-item v-if="userStore.isEnterprise || userStore.isAdmin" index="/app/research/demand/publish">发布企业需求</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="resource">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>资源流转</span>
          </template>
          <el-menu-item index="/app/resource/list">资源列表</el-menu-item>
          <el-menu-item index="/app/resource/bookings">我的预约</el-menu-item>
          <el-menu-item index="/app/resource/publish">发布闲置资源</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="learning">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>教学辅助</span>
          </template>
          <el-menu-item index="/app/learning/resources">学习资源</el-menu-item>
          <el-menu-item v-if="userStore.isStudent || userStore.isAdmin" index="/app/learning/center">学习中心</el-menu-item>
          <el-menu-item v-if="userStore.isTeacher || userStore.isAdmin" index="/app/learning/publish">发布学习资源</el-menu-item>
        </el-sub-menu>

        <template v-if="userStore.isAdmin">
          <el-sub-menu index="admin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/app/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/app/admin/audit">内容审核</el-menu-item>
            <el-menu-item index="/app/admin/dashboard">数据看板</el-menu-item>
            <el-menu-item index="/app/admin/operation-logs">操作日志</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <!-- 消息通知入口 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon class="notification-icon" @click="goToNotifications">
              <Bell />
            </el-icon>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import {
  HomeFilled,
  Search,
  Box,
  Reading,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const currentRoute = computed(() => route.path)

// 使用全局 Store 中的未读数量，支持 WebSocket 实时更新
const unreadCount = computed(() => notificationStore.unreadCount)

/**
 * 跳转到通知中心
 */
function goToNotifications() {
  router.push('/app/notifications')
}

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/app/user/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  // 页面加载时获取一次未读数量，后续由 WebSocket 实时推送更新
  notificationStore.fetchUnreadCount()
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #1d1e2c;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .logo-icon {
      font-size: 24px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
      letter-spacing: 2px;
    }
  }

  .sidebar-menu {
    border-right: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
  height: 60px;
  background: #fff;

  .header-left {
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      transition: color 0.2s;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notification-badge {
      cursor: pointer;

      .notification-icon {
        font-size: 20px;
        color: #606266;
        transition: color 0.2s;

        &:hover {
          color: #409eff;
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.main-content {
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
