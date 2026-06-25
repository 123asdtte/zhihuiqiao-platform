<template>
  <div class="audit-manage-page zh-page">
    <!-- 页面标题区域 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">内容审核</h1>
        <p class="zh-page-subtitle">对平台发布的科研项目、企业需求、闲置资源与学习资源进行审核管理</p>
      </div>
    </div>

    <!-- 顶部统计卡片：展示四类内容待审核数量 -->
    <div class="stats-section">
      <div class="stat-card" :class="{ active: activeTab === 'project' }" @click="activeTab = 'project'">
        <div class="stat-header">
          <el-icon class="stat-icon"><Search /></el-icon>
          <span class="stat-label">科研项目</span>
        </div>
        <div class="stat-value">{{ stats.projectPendingCount }}</div>
        <div class="stat-hint">待审核</div>
      </div>
      <div class="stat-card" :class="{ active: activeTab === 'demand' }" @click="activeTab = 'demand'">
        <div class="stat-header">
          <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
          <span class="stat-label">企业需求</span>
        </div>
        <div class="stat-value">{{ stats.demandPendingCount }}</div>
        <div class="stat-hint">待审核</div>
      </div>
      <div class="stat-card" :class="{ active: activeTab === 'resource' }" @click="activeTab = 'resource'">
        <div class="stat-header">
          <el-icon class="stat-icon"><Box /></el-icon>
          <span class="stat-label">闲置资源</span>
        </div>
        <div class="stat-value">{{ stats.resourcePendingCount }}</div>
        <div class="stat-hint">待审核</div>
      </div>
      <div class="stat-card" :class="{ active: activeTab === 'learning' }" @click="activeTab = 'learning'">
        <div class="stat-header">
          <el-icon class="stat-icon"><Reading /></el-icon>
          <span class="stat-label">学习资源</span>
        </div>
        <div class="stat-value">{{ stats.learningPendingCount }}</div>
        <div class="stat-hint">待审核</div>
      </div>
    </div>

    <!-- 标签页内容区 -->
    <el-card class="tabs-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 科研项目标签页 -->
        <el-tab-pane label="科研项目" name="project">
          <div class="tab-toolbar">
            <el-input
              v-model="tabState.project.keyword"
              placeholder="搜索项目名称"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch('project')"
            />
            <el-select
              v-model="tabState.project.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
              @change="handleSearch('project')"
            >
              <el-option label="待审核" value="pending_audit" />
              <el-option label="招募中" value="recruiting" />
              <el-option label="进行中" value="ongoing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已关闭" value="closed" />
            </el-select>
            <el-button type="primary" @click="handleSearch('project')">搜索</el-button>
          </div>
          <el-table :data="tabState.project.list" stripe border v-loading="tabState.project.loading" style="width: 100%">
            <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType('project', row.status)" size="small">
                  {{ statusText('project', row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publisherId" label="发布人ID" min-width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 'pending_audit'">
                  <el-button type="success" link size="small" @click="handleAudit('project', row, 'pass')">通过</el-button>
                  <el-button type="danger" link size="small" @click="handleAudit('project', row, 'reject')">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 'recruiting' || row.status === 'ongoing' || row.status === 'open' || row.status === 'available'">
                  <el-button type="danger" link size="small" @click="handleAudit('project', row, 'close')">关闭</el-button>
                </template>
                <span v-else class="no-action">--</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="tabState.project.pagination.pageNum"
              v-model:page-size="tabState.project.pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="tabState.project.pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="(s) => handleSizeChange('project', s)"
              @current-change="(p) => handlePageChange('project', p)"
            />
          </div>
        </el-tab-pane>

        <!-- 企业需求标签页 -->
        <el-tab-pane label="企业需求" name="demand">
          <div class="tab-toolbar">
            <el-input
              v-model="tabState.demand.keyword"
              placeholder="搜索需求标题"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch('demand')"
            />
            <el-select
              v-model="tabState.demand.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
              @change="handleSearch('demand')"
            >
              <el-option label="待审核" value="pending_audit" />
              <el-option label="进行中" value="open" />
              <el-option label="已关闭" value="closed" />
            </el-select>
            <el-button type="primary" @click="handleSearch('demand')">搜索</el-button>
          </div>
          <el-table :data="tabState.demand.list" stripe border v-loading="tabState.demand.loading" style="width: 100%">
            <el-table-column prop="demandTitle" label="需求标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType('demand', row.status)" size="small">
                  {{ statusText('demand', row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publisherId" label="发布人ID" min-width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 'pending_audit'">
                  <el-button type="success" link size="small" @click="handleAudit('demand', row, 'pass')">通过</el-button>
                  <el-button type="danger" link size="small" @click="handleAudit('demand', row, 'reject')">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 'open'">
                  <el-button type="danger" link size="small" @click="handleAudit('demand', row, 'close')">关闭</el-button>
                </template>
                <span v-else class="no-action">--</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="tabState.demand.pagination.pageNum"
              v-model:page-size="tabState.demand.pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="tabState.demand.pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="(s) => handleSizeChange('demand', s)"
              @current-change="(p) => handlePageChange('demand', p)"
            />
          </div>
        </el-tab-pane>

        <!-- 闲置资源标签页 -->
        <el-tab-pane label="闲置资源" name="resource">
          <div class="tab-toolbar">
            <el-input
              v-model="tabState.resource.keyword"
              placeholder="搜索资源名称"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch('resource')"
            />
            <el-select
              v-model="tabState.resource.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
              @change="handleSearch('resource')"
            >
              <el-option label="待审核" value="pending_audit" />
              <el-option label="可预约" value="available" />
              <el-option label="已租出" value="rented" />
              <el-option label="已下架" value="unavailable" />
            </el-select>
            <el-button type="primary" @click="handleSearch('resource')">搜索</el-button>
          </div>
          <el-table :data="tabState.resource.list" stripe border v-loading="tabState.resource.loading" style="width: 100%">
            <el-table-column prop="resourceName" label="资源名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType('resource', row.status)" size="small">
                  {{ statusText('resource', row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publisherId" label="发布人ID" min-width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 'pending_audit'">
                  <el-button type="success" link size="small" @click="handleAudit('resource', row, 'pass')">通过</el-button>
                  <el-button type="danger" link size="small" @click="handleAudit('resource', row, 'reject')">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 'available' || row.status === 'rented' || row.status === 'booked'">
                  <el-button type="danger" link size="small" @click="handleAudit('resource', row, 'close')">下架</el-button>
                </template>
                <span v-else class="no-action">--</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="tabState.resource.pagination.pageNum"
              v-model:page-size="tabState.resource.pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="tabState.resource.pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="(s) => handleSizeChange('resource', s)"
              @current-change="(p) => handlePageChange('resource', p)"
            />
          </div>
        </el-tab-pane>

        <!-- 学习资源标签页 -->
        <el-tab-pane label="学习资源" name="learning">
          <div class="tab-toolbar">
            <el-input
              v-model="tabState.learning.keyword"
              placeholder="搜索资源标题"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch('learning')"
            />
            <el-select
              v-model="tabState.learning.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
              @change="handleSearch('learning')"
            >
              <el-option label="待审核" :value="2" />
              <el-option label="已上架" :value="1" />
              <el-option label="已下架" :value="0" />
              <el-option label="已拒绝" :value="-1" />
            </el-select>
            <el-button type="primary" @click="handleSearch('learning')">搜索</el-button>
          </div>
          <el-table :data="tabState.learning.list" stripe border v-loading="tabState.learning.loading" style="width: 100%">
            <el-table-column prop="resourceName" label="资源名称" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType('learning', row.status)" size="small">
                  {{ statusText('learning', row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publisherId" label="发布人ID" min-width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 2">
                  <el-button type="success" link size="small" @click="handleAudit('learning', row, 'pass')">通过</el-button>
                  <el-button type="danger" link size="small" @click="handleAudit('learning', row, 'reject')">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 1">
                  <el-button type="danger" link size="small" @click="handleAudit('learning', row, 'close')">下架</el-button>
                </template>
                <span v-else class="no-action">--</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="tabState.learning.pagination.pageNum"
              v-model:page-size="tabState.learning.pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="tabState.learning.pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="(s) => handleSizeChange('learning', s)"
              @current-change="(p) => handlePageChange('learning', p)"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, OfficeBuilding, Box, Reading } from '@element-plus/icons-vue'
import {
  getAuditStats,
  getAuditProjects,
  getAuditDemands,
  getAuditResources,
  getAuditLearningResources,
  updateAuditProjectStatus,
  updateAuditDemandStatus,
  updateAuditResourceStatus,
  updateAuditLearningResourceStatus
} from '@/api/admin'

// ==================== 类型定义 ====================

type TabType = 'project' | 'demand' | 'resource' | 'learning'

interface TabItem {
  list: any[]
  loading: boolean
  keyword: string
  status: string | number | undefined
  pagination: {
    pageNum: number
    pageSize: number
    total: number
  }
}

// ==================== 统计数据 ====================

/**
 * 审核统计信息：四类内容数量
 */
const stats = reactive({
  projectCount: 0,
  projectPendingCount: 0,
  demandCount: 0,
  demandPendingCount: 0,
  resourceCount: 0,
  resourcePendingCount: 0,
  learningResourceCount: 0,
  learningPendingCount: 0
})

// ==================== 标签页状态 ====================

/**
 * 当前激活的标签页
 */
const activeTab = ref<TabType>('project')

/**
 * 各标签页的列表、加载状态、关键词与分页信息
 */
const tabState = reactive<Record<TabType, TabItem>>({
  project: {
    list: [],
    loading: false,
    keyword: '',
    status: 'pending_audit',
    pagination: { pageNum: 1, pageSize: 10, total: 0 }
  },
  demand: {
    list: [],
    loading: false,
    keyword: '',
    status: 'pending_audit',
    pagination: { pageNum: 1, pageSize: 10, total: 0 }
  },
  resource: {
    list: [],
    loading: false,
    keyword: '',
    status: 'pending_audit',
    pagination: { pageNum: 1, pageSize: 10, total: 0 }
  },
  learning: {
    list: [],
    loading: false,
    keyword: '',
    status: 2,
    pagination: { pageNum: 1, pageSize: 10, total: 0 }
  }
})

// ==================== 接口与配置映射 ====================

/**
 * 各类型对应的列表查询接口
 */
const fetchApiMap: Record<TabType, (params: any) => Promise<any>> = {
  project: getAuditProjects,
  demand: getAuditDemands,
  resource: getAuditResources,
  learning: getAuditLearningResources
}

/**
 * 各类型对应的状态更新接口
 */
const updateApiMap: Record<TabType, any> = {
  project: updateAuditProjectStatus,
  demand: updateAuditDemandStatus,
  resource: updateAuditResourceStatus,
  learning: updateAuditLearningResourceStatus
}

// ==================== 辅助函数 ====================

/**
 * 状态文本映射：根据内容类型与状态值返回中文文本
 * @param type 内容类型
 * @param status 状态值
 */
function statusText(type: TabType, status: string | number) {
  const projectMap: Record<string, string> = {
    pending_audit: '待审核',
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭',
    rejected: '已拒绝'
  }
  const demandMap: Record<string, string> = {
    pending_audit: '待审核',
    open: '进行中',
    closed: '已关闭',
    rejected: '已拒绝'
  }
  const resourceMap: Record<string, string> = {
    pending_audit: '待审核',
    available: '可预约',
    unavailable: '已下架',
    rented: '已租出',
    booked: '已预约',
    rejected: '已拒绝'
  }

  if (type === 'project') return projectMap[status as string] || (status as string)
  if (type === 'demand') return demandMap[status as string] || (status as string)
  if (type === 'resource') return resourceMap[status as string] || (status as string)
  if (type === 'learning') {
    const learningMap: Record<number, string> = { 2: '待审核', 1: '已上架', 0: '已下架', '-1': '已拒绝' }
    return learningMap[status as number] || String(status)
  }
  return String(status)
}

/**
 * 状态对应的 tag 类型，用于显示不同颜色
 * @param type 内容类型
 * @param status 状态值
 */
function statusTagType(type: TabType, status: string | number) {
  if (type === 'project') {
    const map: Record<string, any> = {
      pending_audit: 'warning',
      recruiting: 'success',
      ongoing: 'primary',
      completed: 'info',
      closed: 'danger',
      rejected: 'info'
    }
    return map[status as string] || 'info'
  }
  if (type === 'demand') {
    const map: Record<string, any> = {
      pending_audit: 'warning',
      open: 'success',
      closed: 'danger',
      rejected: 'info'
    }
    return map[status as string] || 'info'
  }
  if (type === 'resource') {
    const map: Record<string, any> = {
      pending_audit: 'warning',
      available: 'success',
      unavailable: 'danger',
      rented: 'primary',
      booked: 'warning',
      rejected: 'info'
    }
    return map[status as string] || 'info'
  }
  if (type === 'learning') {
    const map: Record<number, any> = { 2: 'warning', 1: 'success', 0: 'danger', '-1': 'info' }
    return map[status as number] || 'info'
  }
  return 'info'
}

// ==================== 数据加载 ====================

/**
 * 加载顶部统计卡片数据
 */
async function loadStats() {
  try {
    const res: any = await getAuditStats()
    if (res.code === 200 && res.data) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  }
}

/**
 * 加载指定标签页的列表数据
 * @param type 标签页类型
 */
async function loadList(type: TabType) {
  const state = tabState[type]
  state.loading = true
  try {
    const params: any = {
      pageNum: state.pagination.pageNum,
      pageSize: state.pagination.pageSize,
      keyword: state.keyword
    }
    if (state.status !== undefined && state.status !== '') {
      params.status = state.status
    }
    const res: any = await fetchApiMap[type](params)
    state.list = res.data?.records || []
    state.pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载列表数据失败')
    console.error(error)
  } finally {
    state.loading = false
  }
}

// ==================== 事件处理 ====================

/**
 * 切换标签页时加载对应列表
 */
function handleTabChange() {
  const type = activeTab.value
  // 首次切换到该标签页或列表为空时重新加载
  if (tabState[type].list.length === 0) {
    loadList(type)
  }
}

/**
 * 搜索按钮：重置到第一页并加载
 * @param type 标签页类型
 */
function handleSearch(type: TabType) {
  tabState[type].pagination.pageNum = 1
  loadList(type)
}

/**
 * 页码变化
 * @param type 标签页类型
 * @param page 新页码
 */
function handlePageChange(type: TabType, page: number) {
  tabState[type].pagination.pageNum = page
  loadList(type)
}

/**
 * 每页条数变化
 * @param type 标签页类型
 * @param size 新页大小
 */
function handleSizeChange(type: TabType, size: number) {
  tabState[type].pagination.pageSize = size
  tabState[type].pagination.pageNum = 1
  loadList(type)
}

/**
 * 审核/上下架/关闭/拒绝操作
 * @param type 标签页类型
 * @param row 当前行数据
 * @param action 操作类型：pass 通过，reject 拒绝，close 关闭/下架
 */
async function handleAudit(type: TabType, row: any, action: 'pass' | 'reject' | 'close') {
  // 根据类型与动作确定目标状态
  const statusMap: Record<TabType, { pass: string | number; reject: string | number; close: string | number }> = {
    project: { pass: 'recruiting', reject: 'rejected', close: 'closed' },
    demand: { pass: 'open', reject: 'rejected', close: 'closed' },
    resource: { pass: 'available', reject: 'rejected', close: 'unavailable' },
    learning: { pass: 1, reject: -1, close: 0 }
  }
  const targetStatus = statusMap[type][action]

  try {
    await updateApiMap[type](row.id, targetStatus)
    ElMessage.success(action === 'pass' ? '已通过' : action === 'reject' ? '已拒绝' : '已关闭/下架')
    // 操作成功后刷新当前标签页数据，并重新加载统计
    loadList(type)
    loadStats()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadStats()
  loadList('project')
})
</script>

<style scoped lang="scss">
.audit-manage-page {
  .page-header-section {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: var(--zh-space-6);
  }

  .stats-section {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--zh-space-5);
    margin-bottom: var(--zh-space-6);
  }

  .stat-card {
    background: var(--zh-bg-elevated);
    border: 1px solid var(--zh-border-light);
    border-radius: var(--zh-radius);
    padding: var(--zh-space-5);
    cursor: pointer;
    box-shadow: var(--zh-shadow);
    transition: all var(--zh-transition);

    &:hover,
    &.active {
      border-color: var(--zh-primary-soft);
      box-shadow: var(--zh-shadow-md);
      transform: translateY(-2px);
    }

    &.active {
      background: var(--zh-primary-soft);
    }

    .stat-header {
      display: flex;
      align-items: center;
      gap: var(--zh-space-2);
      margin-bottom: var(--zh-space-3);

      .stat-icon {
        font-size: 24px;
        color: var(--zh-primary);
      }

      .stat-label {
        color: var(--zh-text-secondary);
        font-size: 14px;
      }
    }

    .stat-value {
      font-family: var(--zh-font-display);
      font-size: 32px;
      font-weight: 700;
      color: var(--zh-primary);
      line-height: 1.2;
    }

    .stat-hint {
      font-size: 12px;
      color: var(--zh-text-tertiary);
      margin-top: var(--zh-space-1);
    }
  }

  .tabs-card {
    .tab-toolbar {
      display: flex;
      align-items: center;
      gap: var(--zh-space-3);
      margin-bottom: var(--zh-space-4);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--zh-space-4);
    }
  }

  .no-action {
    color: var(--zh-text-tertiary);
    font-size: 13px;
  }
}

@media (max-width: 1200px) {
  .audit-manage-page .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .audit-manage-page .stats-section {
    grid-template-columns: 1fr;
  }
}
</style>
