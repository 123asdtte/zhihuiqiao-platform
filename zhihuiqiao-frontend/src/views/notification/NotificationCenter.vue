<template>
  <div class="notification-center-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">消息通知</h1>
        <p class="zh-page-subtitle">查看系统消息、项目申请、资源预约等业务通知</p>
      </div>
      <div class="page-header-actions">
        <el-button
          text
          class="setting-btn"
          @click="openSettings"
        >
          <el-icon><Setting /></el-icon>
          通知设置
        </el-button>
        <el-button
          type="primary"
          :disabled="unreadCount === 0"
          class="mark-all-btn"
          @click="handleMarkAllRead"
        >
          <el-icon><Check /></el-icon>
          全部标记为已读
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stats-card unread-card">
        <div class="stats-icon">
          <el-icon :size="24"><Bell /></el-icon>
        </div>
        <div class="stats-info">
          <div class="stats-value">{{ unreadCount }}</div>
          <div class="stats-label">未读通知</div>
        </div>
      </div>
      <div class="stats-card total-card">
        <div class="stats-icon">
          <el-icon :size="24"><MessageBox /></el-icon>
        </div>
        <div class="stats-info">
          <div class="stats-value">{{ pagination.total }}</div>
          <div class="stats-label">消息总数</div>
        </div>
      </div>
      <div class="stats-card system-card">
        <div class="stats-icon">
          <el-icon :size="24"><SetUp /></el-icon>
        </div>
        <div class="stats-info">
          <div class="stats-value">{{ typeCount.system }}</div>
          <div class="stats-label">系统消息</div>
        </div>
      </div>
      <div class="stats-card business-card">
        <div class="stats-icon">
          <el-icon :size="24"><Connection /></el-icon>
        </div>
        <div class="stats-info">
          <div class="stats-value">{{ typeCount.business }}</div>
          <div class="stats-label">业务消息</div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-tabs">
        <div
          v-for="tab in readTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: readFilter === tab.value }"
          @click="handleReadFilterChange(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.value === 'unread' && unreadCount > 0" class="tab-badge">{{ unreadCount }}</span>
        </div>
      </div>
      <div class="filter-tabs type-tabs">
        <div
          v-for="tab in typeTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: typeFilter === tab.value }"
          @click="handleTypeFilterChange(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 通知设置抽屉 -->
    <el-drawer
      v-model="settingDrawerVisible"
      title="通知设置"
      size="420px"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <div v-loading="settingLoading" class="setting-content">
        <p class="setting-desc">选择你希望接收的通知类型，关闭后将不再收到对应类型的推送与未读计数。</p>
        <div class="setting-list">
          <div v-for="item in settings" :key="item.type" class="setting-item">
            <div class="setting-info">
              <div class="setting-title">{{ settingTypeLabel(item.type) }}</div>
              <div class="setting-subtitle">{{ settingTypeDesc(item.type) }}</div>
            </div>
            <el-switch
              v-model="item.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleSettingChange(item)"
            />
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 通知列表 -->
    <div class="notification-list-card" v-loading="loading">
      <div v-if="notificationList.length > 0" class="notification-list">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleItemClick(item)"
        >
          <div class="item-timeline">
            <div class="timeline-dot" :class="item.type"></div>
            <div class="timeline-line"></div>
          </div>
          <div class="item-content">
            <div class="item-body">
              <div class="item-icon" :class="item.type">
                <el-icon :size="20">
                  <Bell v-if="item.type === 'system'" />
                  <Search v-else-if="item.type === 'application' || item.type === 'project'" />
                  <Box v-else-if="item.type === 'booking' || item.type === 'resource'" />
                  <Reading v-else-if="item.type === 'learning'" />
                  <Bell v-else />
                </el-icon>
              </div>
              <div class="item-main">
                <div class="item-header">
                  <h3 class="item-title">{{ item.title }}</h3>
                  <span class="item-type" :class="item.type">{{ typeLabel(item.type) }}</span>
                </div>
                <p class="item-desc">{{ item.content }}</p>
                <div class="item-footer">
                  <span class="item-time">
                    <el-icon><Clock /></el-icon>
                    {{ item.createTime }}
                  </span>
                  <span class="item-status">
                    <span v-if="item.isRead === 0" class="status-dot unread"></span>
                    {{ item.isRead === 0 ? '未读' : '已读' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无通知消息" class="empty-state" />

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
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Search,
  Box,
  Reading,
  Check,
  Clock,
  MessageBox,
  SetUp,
  Connection,
  Setting
} from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'
import {
  getNotificationList,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  getNotificationSettings,
  updateNotificationSettings,
  type NotificationSettingItem
} from '@/api/notification'
import { getApplicationById } from '@/api/research'

const router = useRouter()
const notificationStore = useNotificationStore()

// 通知列表
const notificationList = ref<any[]>([])
const loading = ref(false)
const unreadCount = computed(() => notificationStore.unreadCount)

// 已读状态筛选
const readFilter = ref('all')
const readTabs = [
  { label: '全部消息', value: 'all' },
  { label: '未读消息', value: 'unread' }
]

// 通知类型筛选
const typeFilter = ref('all')
const typeTabs = [
  { label: '全部类型', value: 'all' },
  { label: '系统', value: 'system' },
  { label: '项目申请', value: 'application' },
  { label: '科研项目', value: 'project' },
  { label: '资源预约', value: 'booking' },
  { label: '闲置资源', value: 'resource' },
  { label: '学习资源', value: 'learning' }
]

// 通知设置
const settingDrawerVisible = ref(false)
const settingLoading = ref(false)
const settings = ref<NotificationSettingItem[]>([])

// 类型统计
const typeCount = computed(() => {
  const count = { system: 0, business: 0 }
  notificationList.value.forEach(item => {
    if (item.type === 'system') {
      count.system++
    } else {
      count.business++
    }
  })
  return count
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 类型标签映射
function typeLabel(type: string) {
  const map: Record<string, string> = {
    system: '系统',
    application: '项目申请',
    project: '科研项目',
    booking: '资源预约',
    resource: '闲置资源',
    learning: '学习资源'
  }
  return map[type] || '通知'
}

// 通知设置类型标签映射
function settingTypeLabel(type: string) {
  const map: Record<string, string> = {
    system: '系统通知',
    application: '项目申请通知',
    project: '科研项目通知',
    booking: '资源预约通知',
    resource: '闲置资源通知',
    learning: '学习资源通知'
  }
  return map[type] || '通知'
}

// 通知设置类型描述映射
function settingTypeDesc(type: string) {
  const map: Record<string, string> = {
    system: '平台公告、系统维护、账号安全等重要消息',
    application: '项目申请提交、审核结果、入组确认等',
    project: '项目动态、任务更新、成果归档等',
    booking: '预约申请、审批结果、归还提醒等',
    resource: '资源发布、状态变更、损坏上报等',
    learning: '学习资源更新、学习提醒、评价回复等'
  }
  return map[type] || '相关通知'
}

// 加载通知列表
async function loadNotifications() {
  loading.value = true
  try {
    const res: any = await getNotificationList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      onlyUnread: readFilter.value === 'unread',
      type: typeFilter.value === 'all' ? undefined : typeFilter.value
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

// 已读状态筛选切换
function handleReadFilterChange(value: string) {
  readFilter.value = value
  pagination.pageNum = 1
  loadNotifications()
}

// 通知类型筛选切换
function handleTypeFilterChange(value: string) {
  typeFilter.value = value
  pagination.pageNum = 1
  loadNotifications()
}

// 打开通知设置
async function openSettings() {
  settingDrawerVisible.value = true
  await loadNotificationSettings()
}

// 加载通知设置
async function loadNotificationSettings() {
  settingLoading.value = true
  try {
    const res: any = await getNotificationSettings()
    if (res.code === 200 && res.data) {
      settings.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载通知设置失败')
    console.error(error)
  } finally {
    settingLoading.value = false
  }
}

// 设置项开关切换
async function handleSettingChange(item: NotificationSettingItem) {
  try {
    await updateNotificationSettings([item])
    ElMessage.success('通知设置已更新')
  } catch (error) {
    ElMessage.error('更新通知设置失败')
    // 失败后恢复原始状态，重新加载
    await loadNotificationSettings()
  }
}

// 分页大小变化
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadNotifications()
}

// 页码变化
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadNotifications()
}

// 点击通知项：标记为已读，并根据通知类型跳转对应业务页面
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

  // 项目申请通知：根据申请ID查询所属项目ID，跳转到项目详情页处理申请
  if (item.type === 'application' && item.relatedId) {
    try {
      const res: any = await getApplicationById(item.relatedId)
      if (res.code === 200 && res.data?.projectId) {
        // 跳转到项目详情页并自动打开申请管理抽屉，方便发布者直接处理申请
        router.push({
          path: `/app/research/projects/${res.data.projectId}`,
          query: { openApplications: '1' }
        })
      } else {
        ElMessage.warning('无法定位到对应项目')
      }
    } catch (error) {
      console.error('查询申请详情失败', error)
      ElMessage.error('跳转失败，请手动前往我的项目处理')
    }
  }
}

// 全部标记为已读
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
  padding-bottom: var(--zh-space-12);
}

// ==================== 页面标题区 ====================
.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--zh-space-6);
  gap: var(--zh-space-4);
}

.page-header-actions {
  .mark-all-btn {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-2);
  }
}

// ==================== 统计卡片 ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--zh-space-5);
  margin-bottom: var(--zh-space-6);
}

.stats-card {
  background: var(--zh-bg-elevated);
  border-radius: var(--zh-radius);
  border: 1px solid var(--zh-border-light);
  box-shadow: var(--zh-shadow);
  padding: var(--zh-space-5);
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
  transition: all var(--zh-transition-fast);

  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--zh-shadow-md);
  }

  .stats-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--zh-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stats-info {
    flex: 1;

    .stats-value {
      font-family: var(--zh-font-display);
      font-size: 26px;
      font-weight: 700;
      color: var(--zh-primary);
      line-height: 1.2;
    }

    .stats-label {
      font-size: 13px;
      color: var(--zh-text-secondary);
      margin-top: 2px;
    }
  }

  &.unread-card .stats-icon {
    background: rgba(181, 66, 59, 0.1);
    color: var(--zh-danger);
  }

  &.total-card .stats-icon {
    background: var(--zh-primary-soft);
    color: var(--zh-primary);
  }

  &.system-card .stats-icon {
    background: var(--zh-accent-soft);
    color: #8a6d1b;
  }

  &.business-card .stats-icon {
    background: rgba(45, 125, 70, 0.1);
    color: var(--zh-success);
  }
}

// ==================== 筛选工具栏 ====================
.filter-toolbar {
  margin-bottom: var(--zh-space-5);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--zh-space-4);
}

.filter-tabs {
  display: inline-flex;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: 100px;
  padding: 4px;
  box-shadow: var(--zh-shadow-sm);
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: var(--zh-space-2);
  padding: var(--zh-space-2) var(--zh-space-5);
  border-radius: 100px;
  font-size: 14px;
  font-weight: 500;
  color: var(--zh-text-secondary);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    color: var(--zh-primary);
  }

  &.active {
    color: #fff;
    background: var(--zh-primary);
    box-shadow: var(--zh-shadow-sm);
  }

  .tab-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: 100px;
    background: var(--zh-danger);
    color: #fff;
    font-size: 11px;
    font-weight: 600;
  }
}

// ==================== 通知列表卡片 ====================
.notification-list-card {
  background: var(--zh-bg-elevated);
  border-radius: var(--zh-radius);
  border: 1px solid var(--zh-border-light);
  box-shadow: var(--zh-shadow);
  padding: var(--zh-space-6);
  min-height: 400px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-1);
}

.notification-item {
  display: flex;
  gap: var(--zh-space-4);
  padding: var(--zh-space-4) 0;
  border-radius: var(--zh-radius-sm);
  cursor: pointer;
  transition: all var(--zh-transition-fast);
  position: relative;

  &:hover {
    background: var(--zh-bg-warm);
  }

  &.unread {
    background: rgba(26, 54, 93, 0.03);

    &:hover {
      background: rgba(26, 54, 93, 0.06);
    }

    .item-content {
      border-left-color: var(--zh-accent);
    }

    .item-title {
      color: var(--zh-primary);
      font-weight: 600;
    }
  }

  .item-timeline {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 24px;
    flex-shrink: 0;
    padding-top: var(--zh-space-3);

    .timeline-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: var(--zh-border);
      border: 2px solid var(--zh-bg-elevated);
      box-shadow: 0 0 0 2px var(--zh-border);
      z-index: 1;

      &.system { background: var(--zh-accent); box-shadow: 0 0 0 2px var(--zh-accent-soft); }
      &.application,
      &.project { background: var(--zh-success); box-shadow: 0 0 0 2px rgba(45, 125, 70, 0.15); }
      &.booking,
      &.resource { background: var(--zh-warning); box-shadow: 0 0 0 2px rgba(199, 122, 36, 0.15); }
      &.learning { background: var(--zh-danger); box-shadow: 0 0 0 2px rgba(181, 66, 59, 0.15); }
    }

    .timeline-line {
      flex: 1;
      width: 1px;
      background: var(--zh-border-light);
      margin-top: 4px;
    }
  }

  &:last-child .timeline-line {
    display: none;
  }

  .item-content {
    flex: 1;
    min-width: 0;
    border-left: 3px solid transparent;
    padding-left: var(--zh-space-4);
    transition: border-color var(--zh-transition-fast);
  }

  .item-body {
    display: flex;
    gap: var(--zh-space-4);
  }

  .item-icon {
    width: 44px;
    height: 44px;
    border-radius: var(--zh-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: var(--zh-bg-warm);
    color: var(--zh-text-tertiary);

    &.system { background: var(--zh-accent-soft); color: #8a6d1b; }
    &.application,
    &.project { background: rgba(45, 125, 70, 0.1); color: var(--zh-success); }
    &.booking,
    &.resource { background: rgba(199, 122, 36, 0.1); color: var(--zh-warning); }
    &.learning { background: rgba(181, 66, 59, 0.1); color: var(--zh-danger); }
  }

  .item-main {
    flex: 1;
    min-width: 0;
  }

  .item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--zh-space-3);
    margin-bottom: var(--zh-space-2);
  }

  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--zh-text-primary);
    margin: 0;
    flex: 1;
    min-width: 0;
  }

  .item-type {
    flex-shrink: 0;
    padding: 3px 10px;
    border-radius: 100px;
    font-size: 11px;
    font-weight: 600;
    background: var(--zh-bg-warm);
    color: var(--zh-text-secondary);

    &.system { background: var(--zh-accent-soft); color: #8a6d1b; }
    &.application,
    &.project { background: rgba(45, 125, 70, 0.1); color: var(--zh-success); }
    &.booking,
    &.resource { background: rgba(199, 122, 36, 0.1); color: var(--zh-warning); }
    &.learning { background: rgba(181, 66, 59, 0.1); color: var(--zh-danger); }
  }

  .item-desc {
    font-size: 13px;
    color: var(--zh-text-secondary);
    line-height: 1.7;
    margin: 0 0 var(--zh-space-3) 0;
  }

  .item-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--zh-space-4);
    font-size: 12px;
    color: var(--zh-text-tertiary);

    .item-time {
      display: inline-flex;
      align-items: center;
      gap: var(--zh-space-1);
    }

    .item-status {
      display: inline-flex;
      align-items: center;
      gap: var(--zh-space-1);

      .status-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: var(--zh-text-tertiary);

        &.unread {
          background: var(--zh-danger);
          box-shadow: 0 0 0 2px rgba(181, 66, 59, 0.2);
        }
      }
    }
  }
}

.empty-state {
  padding: var(--zh-space-16) 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--zh-space-8);
  padding-top: var(--zh-space-6);
  border-top: 1px solid var(--zh-border-light);
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .notification-item {
    .item-body {
      flex-direction: column;
      gap: var(--zh-space-3);
    }

    .item-timeline {
      display: none;
    }

    .item-content {
      padding-left: 0;
      border-left: none;
    }
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-tabs {
    width: 100%;
    display: flex;
  }

  .filter-tab {
    flex: 1;
    justify-content: center;
  }
}

// ==================== 通知设置抽屉 ====================
.setting-content {
  padding: var(--zh-space-2) var(--zh-space-2) var(--zh-space-6);
}

.setting-desc {
  font-size: 13px;
  color: var(--zh-text-secondary);
  line-height: 1.6;
  margin: 0 0 var(--zh-space-5) 0;
}

.setting-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-3);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--zh-space-4);
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
}

.setting-info {
  flex: 1;
  min-width: 0;
  padding-right: var(--zh-space-4);
}

.setting-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--zh-text-primary);
  margin-bottom: 4px;
}

.setting-subtitle {
  font-size: 12px;
  color: var(--zh-text-secondary);
  line-height: 1.5;
}
</style>
