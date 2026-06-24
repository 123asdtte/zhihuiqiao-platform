<template>
  <div class="notification-center-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>消息通知</h2>
      <p>查看系统消息、项目申请、资源预约等业务通知</p>
    </div>

    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="filterType" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
        </el-radio-group>
        <el-button type="primary" link :disabled="unreadCount === 0" @click="handleMarkAllRead">
          全部标记为已读
        </el-button>
      </div>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="list-card" shadow="never" v-loading="loading">
      <div v-if="notificationList.length > 0" class="notification-list">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleItemClick(item)"
        >
          <div class="item-left">
            <div class="item-icon" :class="item.type">
              <el-icon :size="20">
                <Bell v-if="item.type === 'system'" />
                <Search v-else-if="item.type === 'application' || item.type === 'project'" />
                <Box v-else-if="item.type === 'booking' || item.type === 'resource'" />
                <Reading v-else-if="item.type === 'learning'" />
                <Bell v-else />
              </el-icon>
            </div>
            <div class="item-content">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.content }}</div>
              <div class="item-time">{{ item.createTime }}</div>
            </div>
          </div>
          <div class="item-right">
            <el-tag v-if="item.isRead === 0" type="danger" size="small">未读</el-tag>
            <el-tag v-else type="info" size="small">已读</el-tag>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无通知消息" />

      <!-- 分页 -->
      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Search, Box, Reading } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'
import {
  getNotificationList,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from '@/api/notification'

const notificationStore = useNotificationStore()

// 通知列表
const notificationList = ref<any[]>([])
const loading = ref(false)
// 使用全局 Store 中的未读数量
const unreadCount = computed(() => notificationStore.unreadCount)

// 筛选类型：all 全部，unread 未读
const filterType = ref('all')

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/**
 * 加载通知列表
 */
async function loadNotifications() {
  loading.value = true
  try {
    const res: any = await getNotificationList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      onlyUnread: filterType.value === 'unread'
    })
    if (res.code === 200 && res.data) {
      notificationList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载通知列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 筛选切换
 */
function handleFilterChange() {
  pagination.pageNum = 1
  loadNotifications()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadNotifications()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadNotifications()
}

/**
 * 点击通知项：标记为已读并跳转关联页面
 */
async function handleItemClick(item: any) {
  if (item.isRead === 0) {
    try {
      const res: any = await markNotificationAsRead(item.id)
      if (res.code === 200) {
        item.isRead = 1
        notificationStore.decreaseUnreadCount(1)
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

/**
 * 全部标记为已读
 */
async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确定将所有通知标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await markAllNotificationsAsRead()
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      notificationList.value.forEach(item => (item.isRead = 1))
      notificationStore.setUnreadCount(0)
    }
  } catch (error) {
    // 用户取消或接口异常，不做处理
  }
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped lang="scss">
.notification-center-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      color: #303133;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .toolbar-card {
    margin-bottom: 20px;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .list-card {
    .notification-list {
      .notification-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16px;
        border-bottom: 1px solid #ebeef5;
        cursor: pointer;
        transition: background 0.2s;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: #f5f7fa;
        }

        &.unread {
          background: #f0f9ff;

          &:hover {
            background: #e6f2ff;
          }
        }

        .item-left {
          display: flex;
          gap: 16px;
          flex: 1;

          .item-icon {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f5f7fa;
            color: #909399;
            flex-shrink: 0;

            &.system {
              background: #ecf5ff;
              color: #409eff;
            }

            &.application,
            &.project {
              background: #f0f9eb;
              color: #67c23a;
            }

            &.booking,
            &.resource {
              background: #fdf6ec;
              color: #e6a23c;
            }

            &.learning {
              background: #fef0f0;
              color: #f56c6c;
            }
          }

          .item-content {
            flex: 1;

            .item-title {
              font-size: 15px;
              font-weight: 500;
              color: #303133;
              margin-bottom: 6px;
            }

            .item-desc {
              font-size: 13px;
              color: #606266;
              line-height: 1.5;
              margin-bottom: 6px;
            }

            .item-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .item-right {
          flex-shrink: 0;
          margin-left: 16px;
        }
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style>
