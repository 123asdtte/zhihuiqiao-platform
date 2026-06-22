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
        </el-sub-menu>

        <el-sub-menu index="resource">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>资源流转</span>
          </template>
          <el-menu-item index="/app/resource/list">资源列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="learning">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>教学辅助</span>
          </template>
          <el-menu-item index="/app/learning/resources">学习资源</el-menu-item>
          <el-menu-item v-if="userStore.isStudent" index="/app/learning/center">学习中心</el-menu-item>
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
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  Search,
  Box,
  Reading,
  Setting,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/app/user/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
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
