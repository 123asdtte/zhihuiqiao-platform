<template>
  <div class="project-list-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">科研项目</h1>
        <p class="zh-page-subtitle">浏览师生发布的科研项目，寻找感兴趣的科研机会</p>
      </div>
      <el-button type="primary" class="publish-btn" @click="router.push('/app/research/project/publish')">
        <el-icon><Plus /></el-icon>
        发布项目
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-select v-model="searchForm.projectType" placeholder="全部类型" clearable @change="handleSearch">
          <el-option label="基础研究" value="基础研究" />
          <el-option label="应用研究" value="应用研究" />
          <el-option label="技术开发" value="技术开发" />
          <el-option label="创新创业" value="创新创业" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
          <el-option label="招募中" value="recruiting" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已完成" value="completed" />
          <el-option label="已关闭" value="closed" />
        </el-select>
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索项目名称/描述"
          clearable
          style="width: 260px"
          @input="handleKeywordInput"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-button text @click="handleReset">重置筛选</el-button>
    </div>

    <!-- 项目卡片列表 -->
    <div v-loading="loading" class="project-grid">
      <div
        class="project-card"
        v-for="item in projectList"
        :key="item.id"
        @click="goToDetail(item.id)"
      >
        <div class="project-card-header">
          <span class="project-status" :class="item.status">{{ statusText(item.status) }}</span>
          <span class="project-views">
            <el-icon><View /></el-icon>
            {{ item.views }}
          </span>
        </div>

        <h3 class="project-name">{{ item.projectName }}</h3>

        <div class="project-tags">
          <span class="zh-tag">{{ item.projectType }}</span>
          <span class="project-field" v-if="item.researchFields">{{ item.researchFields }}</span>
        </div>

        <p class="project-desc">{{ item.projectDescription || '暂无项目简介' }}</p>

        <div class="project-progress">
          <div class="progress-info">
            <span>团队人数</span>
            <span class="progress-value">{{ item.currentMembers || 0 }} / {{ item.maxMembers || 0 }}</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: memberProgress(item.currentMembers, item.maxMembers) }"
            ></div>
          </div>
        </div>

        <div class="project-card-footer">
          <div class="publisher">
            <el-icon><User /></el-icon>
            <span>{{ item.publisherName || '未知发布者' }}</span>
          </div>
          <div class="project-date">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(item.startDate) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="projectList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无相关项目" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[8, 12, 16, 20]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { debounce } from '@/utils/debounce'
import { View, User, Calendar, Search, Plus } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/research'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  projectType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 项目列表
const projectList = ref<any[]>([])
const loading = ref(false)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

/**
 * 格式化日期
 */
function formatDate(date: string) {
  if (!date) return '待定'
  return date
}

/**
 * 成员进度百分比
 */
function memberProgress(current: number, max: number) {
  if (!max) return '0%'
  const progress = Math.min(((current || 0) / max) * 100, 100)
  return `${progress}%`
}

/**
 * 加载项目列表数据
 */
async function loadProjectList() {
  loading.value = true
  try {
    const res: any = await getProjectList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      projectType: searchForm.projectType,
      status: searchForm.status
    })
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 关键词输入防抖搜索
 */
const handleKeywordInput = debounce(() => {
  handleSearch()
}, 400)

/**
 * 搜索
 */
function handleSearch() {
  pagination.pageNum = 1
  loadProjectList()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  searchForm.keyword = ''
  searchForm.projectType = ''
  searchForm.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadProjectList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadProjectList()
}

/**
 * 跳转项目详情页
 */
function goToDetail(id: number) {
  router.push(`/app/research/projects/${id}`)
}

onMounted(() => {
  loadProjectList()
})
</script>

<style scoped lang="scss">
.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--zh-space-6);
}

.publish-btn {
  background: var(--zh-accent) !important;
  border-color: var(--zh-accent) !important;
  color: #fff !important;
  font-weight: 600;

  &:hover {
    background: var(--zh-accent-light) !important;
    border-color: var(--zh-accent-light) !important;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-4) var(--zh-space-5);
  margin-bottom: var(--zh-space-6);
  box-shadow: var(--zh-shadow-sm);

  .filter-group {
    display: flex;
    gap: var(--zh-space-3);
  }
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--zh-space-5);
  margin-bottom: var(--zh-space-8);
}

.project-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  cursor: pointer;
  box-shadow: var(--zh-shadow);
  transition: all var(--zh-transition);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: var(--zh-primary);
    opacity: 0;
    transition: opacity var(--zh-transition);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--zh-shadow-md);

    &::before {
      opacity: 1;
    }
  }

  .project-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--zh-space-4);

    .project-status {
      padding: 4px 10px;
      border-radius: 100px;
      font-size: 11px;
      font-weight: 600;
      color: #fff;
      background-color: var(--zh-text-tertiary);

      &.recruiting {
        background-color: var(--zh-success);
      }

      &.ongoing {
        background-color: var(--zh-secondary);
      }

      &.completed {
        background-color: var(--zh-text-tertiary);
      }

      &.closed {
        background-color: var(--zh-danger);
      }
    }

    .project-views {
      display: flex;
      align-items: center;
      gap: var(--zh-space-1);
      font-size: 12px;
      color: var(--zh-text-tertiary);
    }
  }

  .project-name {
    font-family: var(--zh-font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-3) 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 50px;
  }

  .project-tags {
    display: flex;
    align-items: center;
    gap: var(--zh-space-2);
    margin-bottom: var(--zh-space-3);
    flex-wrap: wrap;

    .project-field {
      font-size: 12px;
      color: var(--zh-text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 120px;
    }
  }

  .project-desc {
    font-size: 13px;
    color: var(--zh-text-secondary);
    line-height: 1.6;
    margin: 0 0 var(--zh-space-4) 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex: 1;
  }

  .project-progress {
    margin-bottom: var(--zh-space-4);

    .progress-info {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: var(--zh-text-secondary);
      margin-bottom: var(--zh-space-2);

      .progress-value {
        color: var(--zh-primary);
        font-weight: 600;
      }
    }

    .progress-bar {
      height: 6px;
      background: var(--zh-border-light);
      border-radius: 3px;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, var(--zh-primary) 0%, var(--zh-secondary) 100%);
        border-radius: 3px;
        transition: width var(--zh-transition);
      }
    }
  }

  .project-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: var(--zh-space-3);
    border-top: 1px solid var(--zh-border-light);
    font-size: 12px;
    color: var(--zh-text-tertiary);

    .publisher,
    .project-date {
      display: flex;
      align-items: center;
      gap: var(--zh-space-1);
    }
  }
}

.empty-state {
  padding: var(--zh-space-16) 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

@media (max-width: 1400px) {
  .project-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .project-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .project-grid {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
    gap: var(--zh-space-3);
    align-items: stretch;

    .filter-group {
      flex-direction: column;
    }
  }
}
</style>
