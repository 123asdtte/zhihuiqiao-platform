<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- Logo -->
      <div class="logo-area">
        <div class="logo-mark">
          <span class="logo-bridge">桥</span>
        </div>
        <div class="logo-text" v-show="!appStore.sidebarCollapsed">
          <div class="logo-title">智汇桥</div>
          <div class="logo-subtitle">智慧协同平台</div>
        </div>
      </div>

      <!-- 导航菜单 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="currentRoute"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
          background-color="transparent"
          text-color="var(--zh-text-secondary)"
          active-text-color="var(--zh-primary)"
        >
          <el-menu-item index="/app/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="/app/research">
            <template #title>
              <el-icon><Search /></el-icon>
              <span>科研撮合</span>
            </template>
            <el-menu-item index="/app/research/projects">科研项目</el-menu-item>
            <el-menu-item index="/app/research/demands">企业需求</el-menu-item>
            <el-menu-item v-if="userStore.isStudent || userStore.isAdmin" index="/app/research/applications">我的申请</el-menu-item>
            <!-- 我的项目：学生、教师、管理员均可管理自己发布的项目 -->
            <el-menu-item v-if="userStore.isStudent || userStore.isTeacher || userStore.isAdmin" index="/app/research/my-projects">我的项目</el-menu-item>
          </el-sub-menu>

          <!-- 资源流转：企业账号无对应权限，隐藏整组菜单 -->
          <el-sub-menu v-if="!userStore.isEnterprise" index="/app/resource">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>资源流转</span>
            </template>
            <el-menu-item index="/app/resource/list">闲置资源</el-menu-item>
            <el-menu-item index="/app/resource/bookings">我的预约</el-menu-item>
            <el-menu-item index="/app/resource/publish">发布资源</el-menu-item>
          </el-sub-menu>

          <!-- 教学辅助：企业账号无对应权限，隐藏整组菜单 -->
          <el-sub-menu v-if="!userStore.isEnterprise" index="/app/learning">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>教学辅助</span>
            </template>
            <el-menu-item index="/app/learning/resources">学习资源</el-menu-item>
            <!-- 学习中心仅学生与管理员可访问 -->
            <el-menu-item v-if="userStore.isStudent || userStore.isAdmin" index="/app/learning/center">学习中心</el-menu-item>
            <!-- 发布学习资源仅教师与管理员可访问 -->
            <el-menu-item v-if="userStore.isTeacher || userStore.isAdmin" index="/app/learning/publish">发布资源</el-menu-item>
          </el-sub-menu>

          <template v-if="userStore.isAdmin">
            <el-sub-menu index="/app/admin">
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
      </el-scrollbar>

      <!-- 底部折叠按钮 -->
      <div class="sidebar-footer">
        <div class="collapse-btn" @click="appStore.toggleSidebar">
          <el-icon :size="16">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-content" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- 顶部栏 -->
      <header class="top-header">
        <div class="breadcrumb-area">
          <span class="greeting">{{ greetingText }}，{{ userStore.userInfo?.username || '用户' }}</span>
        </div>

        <!-- 全局搜索入口 -->
        <div class="global-search-trigger" @click="goToSearch">
          <el-icon><Search /></el-icon>
          <span>全局搜索</span>
        </div>

        <div class="header-actions">
          <!-- 通知入口 -->
          <div class="action-item notification-action" @click="goToNotifications">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notice-badge">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </div>

          <!-- 用户下拉 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar" class="user-avatar">
                {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <div class="user-meta" v-show="!appStore.sidebarCollapsed">
                <div class="username">{{ userStore.userInfo?.username || '用户' }}</div>
                <div class="role">{{ roleText }}</div>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="notifications">
                  <el-icon><Bell /></el-icon>消息通知
                  <el-tag v-if="unreadCount > 0" type="danger" size="small" class="unread-tag">{{ unreadCount }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>

      <!-- AI 智能助手：仅登录后显示 -->
      <AiAssistant v-if="userStore.isLoggedIn" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { useWebSocket } from '@/composables/useWebSocket'
import {
  HomeFilled,
  Search,
  Box,
  Reading,
  Setting,
  Fold,
  Expand,
  Bell,
  ArrowDown,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import AiAssistant from '@/components/AiAssistant.vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const { connect, disconnect } = useWebSocket()

const currentRoute = computed(() => route.path)

// 使用全局 Store 中的未读数量，支持 WebSocket 实时更新
const unreadCount = computed(() => notificationStore.unreadCount)

/**
 * 根据时间返回问候语
 */
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

/**
 * 角色中文映射
 */
const roleText = computed(() => {
  const roleMap: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    enterprise: '企业用户'
  }
  return roleMap[userStore.userInfo?.roleType || ''] || '用户'
})

/**
 * 跳转到通知中心
 */
function goToNotifications() {
  router.push('/app/notifications')
}

/**
 * 跳转至全局搜索页
 */
function goToSearch() {
  router.push('/app/search')
}

/**
 * 处理用户下拉菜单命令
 */
function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/app/user/profile')
      break
    case 'notifications':
      router.push('/app/notifications')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        disconnect()
        userStore.logout()
        router.push('/login')
      }).catch(() => {})
      break
  }
}

onMounted(() => {
  // 先获取当前未读数量作为初始值
  notificationStore.fetchUnreadCount()
  // 建立 WebSocket 连接，接收实时通知推送
  connect()
})
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--zh-bg);
}

// ==================== 侧边栏 ====================
.sidebar {
  width: 260px;
  height: 100%;
  background: var(--zh-bg-elevated);
  border-right: 1px solid var(--zh-border-light);
  display: flex;
  flex-direction: column;
  transition: width var(--zh-transition);
  position: relative;
  z-index: 100;
  box-shadow: var(--zh-shadow);

  &.collapsed {
    width: 72px;
  }
}

.logo-area {
  height: 80px;
  padding: 0 var(--zh-space-5);
  display: flex;
  align-items: center;
  gap: var(--zh-space-3);
  border-bottom: 1px solid var(--zh-border-light);

  .logo-mark {
    width: 42px;
    height: 42px;
    border-radius: var(--zh-radius);
    background: var(--zh-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: var(--zh-accent);
    }

    .logo-bridge {
      color: #fff;
      font-family: var(--zh-font-display);
      font-size: 20px;
      font-weight: 700;
    }
  }

  .logo-text {
    overflow: hidden;

    .logo-title {
      font-family: var(--zh-font-display);
      font-size: 20px;
      font-weight: 700;
      color: var(--zh-primary);
      line-height: 1.2;
    }

    .logo-subtitle {
      font-size: 11px;
      color: var(--zh-text-tertiary);
      letter-spacing: 0.1em;
      margin-top: 2px;
    }
  }
}

.menu-scrollbar {
  flex: 1;
  padding: var(--zh-space-4) var(--zh-space-3);
}

.sidebar-menu {
  border-right: none;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    border-radius: var(--zh-radius-sm);
    margin-bottom: var(--zh-space-1);
    font-size: 14px;
    transition: all var(--zh-transition-fast);

    &:hover {
      background: var(--zh-primary-soft) !important;
      color: var(--zh-primary) !important;
    }

    .el-icon {
      font-size: 18px;
      color: inherit;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: var(--zh-primary-soft) !important;
    color: var(--zh-primary) !important;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: var(--zh-accent);
      border-radius: 0 2px 2px 0;
    }
  }

  :deep(.el-sub-menu.is-active .el-sub-menu__title) {
    color: var(--zh-primary) !important;
    font-weight: 600;
  }

  :deep(.el-sub-menu .el-menu-item) {
    height: 40px;
    line-height: 40px;
    padding-left: 52px !important;
  }
}

.sidebar-footer {
  padding: var(--zh-space-4);
  border-top: 1px solid var(--zh-border-light);
  display: flex;
  justify-content: flex-end;

  .collapse-btn {
    width: 32px;
    height: 32px;
    border-radius: var(--zh-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--zh-text-tertiary);
    transition: all var(--zh-transition-fast);

    &:hover {
      background: var(--zh-primary-soft);
      color: var(--zh-primary);
    }
  }
}

// ==================== 主内容区 ====================
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: margin-left var(--zh-transition);
}

.top-header {
  height: 72px;
  padding: 0 var(--zh-space-8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(250, 250, 248, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--zh-border-light);
  position: sticky;
  top: 0;
  z-index: 50;

  .greeting {
    font-family: var(--zh-font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--zh-primary);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
}

.global-search-trigger {
  display: flex;
  align-items: center;
  gap: var(--zh-space-2);
  padding: 8px 16px;
  border-radius: 100px;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  color: var(--zh-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    border-color: var(--zh-primary-soft);
    color: var(--zh-primary);
    box-shadow: var(--zh-shadow-sm);
  }
}

.action-item {
  width: 40px;
  height: 40px;
  border-radius: var(--zh-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--zh-text-secondary);
  transition: all var(--zh-transition-fast);

  &:hover {
    background: var(--zh-primary-soft);
    color: var(--zh-primary);
  }
}

.notification-action {
  position: relative;
}

:deep(.notice-badge .el-badge__content) {
  background-color: var(--zh-danger);
  border: 2px solid var(--zh-bg);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--zh-space-3);
  padding: var(--zh-space-2) var(--zh-space-3);
  border-radius: var(--zh-radius);
  cursor: pointer;
  transition: background var(--zh-transition-fast);

  &:hover {
    background: var(--zh-bg-warm);
  }

  .user-avatar {
    background: var(--zh-primary);
    color: #fff;
    font-weight: 600;
    flex-shrink: 0;
  }

  .user-meta {
    line-height: 1.3;

    .username {
      font-size: 14px;
      font-weight: 600;
      color: var(--zh-text-primary);
    }

    .role {
      font-size: 11px;
      color: var(--zh-text-tertiary);
    }
  }

  .dropdown-icon {
    color: var(--zh-text-tertiary);
    font-size: 12px;
  }
}

.unread-tag {
  margin-left: var(--zh-space-2);
}

.page-container {
  flex: 1;
  overflow-y: auto;
  position: relative;
}

// ==================== 页面过渡动画 ====================
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
