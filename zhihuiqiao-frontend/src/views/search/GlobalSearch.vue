<template>
  <div class="global-search-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">全局搜索</h1>
        <p class="zh-page-subtitle">一次搜索，发现科研项目、闲置资源、学习资源与企业需求</p>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="输入关键词，例如：深度学习、图像处理、实验室设备..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 类型筛选标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="activeType" size="large" @change="handleTypeChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="project">科研项目</el-radio-button>
        <el-radio-button label="resource">闲置资源</el-radio-button>
        <el-radio-button label="learning">学习资源</el-radio-button>
        <el-radio-button label="demand">企业需求</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 结果列表 -->
    <div v-loading="loading" class="search-results">
      <!-- 科研项目结果 -->
      <section v-if="showSection('project')" class="result-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Search /></el-icon>
            科研项目
            <el-tag type="info" size="small">{{ projectResults.length }}</el-tag>
          </h3>
          <el-button v-if="activeType === 'all' && projectResults.length > 0" link type="primary" @click="activeType = 'project'">
            查看更多
          </el-button>
        </div>
        <div v-if="projectResults.length > 0" class="result-grid">
          <div
            v-for="item in displayedProjects"
            :key="item.id"
            class="result-card"
            @click="goToProject(item.id)"
          >
            <div class="card-title">{{ item.projectName }}</div>
            <div class="card-meta">
              <el-tag size="small" type="info">{{ item.projectType || '未分类' }}</el-tag>
              <span class="meta-item">{{ item.publisherName || '未知发布人' }}</span>
            </div>
            <p class="card-desc">{{ item.projectDescription || '暂无简介' }}</p>
            <div class="card-footer">
              <span><el-icon><View /></el-icon> {{ item.views || 0 }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="未找到相关科研项目" />
      </section>

      <!-- 闲置资源结果 -->
      <section v-if="showSection('resource')" class="result-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Box /></el-icon>
            闲置资源
            <el-tag type="info" size="small">{{ resourceResults.length }}</el-tag>
          </h3>
          <el-button v-if="activeType === 'all' && resourceResults.length > 0" link type="primary" @click="activeType = 'resource'">
            查看更多
          </el-button>
        </div>
        <div v-if="resourceResults.length > 0" class="result-grid">
          <div
            v-for="item in displayedResources"
            :key="item.id"
            class="result-card"
            @click="goToResource(item.id)"
          >
            <div class="card-title">{{ item.resourceName }}</div>
            <div class="card-meta">
              <el-tag size="small" type="info">{{ item.resourceType || '未分类' }}</el-tag>
              <span class="meta-item price">
                <span v-if="item.rentalPrice > 0">¥{{ item.rentalPrice }}/天</span>
                <span v-else class="free">免费</span>
              </span>
            </div>
            <p class="card-desc">{{ item.description || '暂无描述' }}</p>
            <div class="card-footer">
              <span><el-icon><View /></el-icon> {{ item.views || 0 }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="未找到相关闲置资源" />
      </section>

      <!-- 学习资源结果 -->
      <section v-if="showSection('learning')" class="result-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Reading /></el-icon>
            学习资源
            <el-tag type="info" size="small">{{ learningResults.length }}</el-tag>
          </h3>
          <el-button v-if="activeType === 'all' && learningResults.length > 0" link type="primary" @click="activeType = 'learning'">
            查看更多
          </el-button>
        </div>
        <div v-if="learningResults.length > 0" class="result-grid">
          <div
            v-for="item in displayedLearning"
            :key="item.id"
            class="result-card"
            @click="goToLearning(item.id)"
          >
            <div class="card-title">{{ item.resourceName }}</div>
            <div class="card-meta">
              <el-tag size="small" type="info">{{ item.resourceType || '未分类' }}</el-tag>
              <el-tag v-if="item.subject" size="small" type="success">{{ item.subject }}</el-tag>
            </div>
            <p class="card-desc">{{ item.description || '暂无描述' }}</p>
            <div class="card-footer">
              <span><el-icon><View /></el-icon> {{ item.views || 0 }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="未找到相关学习资源" />
      </section>

      <!-- 企业需求结果 -->
      <section v-if="showSection('demand')" class="result-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><OfficeBuilding /></el-icon>
            企业需求
            <el-tag type="info" size="small">{{ demandResults.length }}</el-tag>
          </h3>
          <el-button v-if="activeType === 'all' && demandResults.length > 0" link type="primary" @click="activeType = 'demand'">
            查看更多
          </el-button>
        </div>
        <div v-if="demandResults.length > 0" class="result-grid">
          <div
            v-for="item in displayedDemands"
            :key="item.id"
            class="result-card"
            @click="goToDemand(item.id)"
          >
            <div class="card-title">{{ item.demandTitle }}</div>
            <div class="card-meta">
              <el-tag size="small" type="info">{{ item.demandType || '未分类' }}</el-tag>
              <span class="meta-item">{{ item.publisherName || '未知企业' }}</span>
            </div>
            <p class="card-desc">{{ item.demandDescription || '暂无描述' }}</p>
            <div class="card-footer">
              <span><el-icon><View /></el-icon> {{ item.views || 0 }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="未找到相关企业需求" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Box, Reading, OfficeBuilding, View } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/research'
import { getResourceList } from '@/api/resource'
import { getLearningResourceList } from '@/api/learning'
import { getDemandList } from '@/api/research'

const route = useRoute()
const router = useRouter()

// 搜索关键词与类型过滤
const keyword = ref('')
const activeType = ref<'all' | 'project' | 'resource' | 'learning' | 'demand'>('all')
const loading = ref(false)

// 各类搜索结果
const projectResults = ref<any[]>([])
const resourceResults = ref<any[]>([])
const learningResults = ref<any[]>([])
const demandResults = ref<any[]>([])

// 全部模式下每个类型最多展示 4 条
const PREVIEW_LIMIT = 4

const displayedProjects = computed(() => activeType.value === 'all' ? projectResults.value.slice(0, PREVIEW_LIMIT) : projectResults.value)
const displayedResources = computed(() => activeType.value === 'all' ? resourceResults.value.slice(0, PREVIEW_LIMIT) : resourceResults.value)
const displayedLearning = computed(() => activeType.value === 'all' ? learningResults.value.slice(0, PREVIEW_LIMIT) : learningResults.value)
const displayedDemands = computed(() => activeType.value === 'all' ? demandResults.value.slice(0, PREVIEW_LIMIT) : demandResults.value)

/**
 * 判断当前是否应该展示某类结果
 */
function showSection(type: 'project' | 'resource' | 'learning' | 'demand') {
  return activeType.value === 'all' || activeType.value === type
}

/**
 * 格式化时间，仅保留日期部分
 */
function formatTime(time: string) {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN')
}

/**
 * 执行全局搜索
 * 并发查询四类资源，关键字为空时返回空结果
 */
async function handleSearch() {
  const query = keyword.value.trim()
  if (!query) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 同步更新 URL 参数，便于刷新或分享
  router.replace({ path: '/app/search', query: { keyword: query, type: activeType.value } })

  loading.value = true
  try {
    const [projectRes, resourceRes, learningRes, demandRes] = await Promise.allSettled([
      getProjectList({ pageNum: 1, pageSize: activeType.value === 'project' ? 20 : 8, keyword: query }),
      getResourceList({ pageNum: 1, pageSize: activeType.value === 'resource' ? 20 : 8, keyword: query }),
      getLearningResourceList({ pageNum: 1, pageSize: activeType.value === 'learning' ? 20 : 8, keyword: query }),
      getDemandList({ pageNum: 1, pageSize: activeType.value === 'demand' ? 20 : 8, keyword: query })
    ])

    projectResults.value = extractRecords(projectRes)
    resourceResults.value = extractRecords(resourceRes)
    learningResults.value = extractRecords(learningRes)
    demandResults.value = extractRecords(demandRes)
  } catch (error) {
    ElMessage.error('搜索失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 从 Promise.allSettled 结果中提取 records 列表
 */
function extractRecords(result: PromiseSettledResult<any>) {
  if (result.status === 'fulfilled' && result.value?.code === 200) {
    return result.value.data?.records || []
  }
  return []
}

/**
 * 切换类型时重新搜索，若当前无关键词则仅切换视图
 */
function handleTypeChange() {
  if (keyword.value.trim()) {
    handleSearch()
  }
}

/**
 * 页面跳转辅助函数
 */
function goToProject(id: number | string) {
  router.push(`/app/research/projects/${id}`)
}
function goToResource(id: number | string) {
  router.push(`/app/resource/${id}`)
}
function goToLearning(id: number | string) {
  router.push(`/app/learning/detail/${id}`)
}
function goToDemand(id: number | string) {
  router.push(`/app/research/demands/${id}`)
}

// 监听 URL 参数变化，回填搜索条件并自动搜索
watch(
  () => route.query,
  (query) => {
    const q = query.keyword as string
    const t = query.type as any
    if (q && q !== keyword.value) {
      keyword.value = q
      activeType.value = ['all', 'project', 'resource', 'learning', 'demand'].includes(t) ? t : 'all'
      handleSearch()
    }
  },
  { immediate: true }
)

onMounted(() => {
  const q = route.query.keyword as string
  if (q) {
    keyword.value = q
    activeType.value = (route.query.type as any) || 'all'
    handleSearch()
  }
})
</script>

<style scoped lang="scss">
.global-search-page {
  padding-bottom: var(--zh-space-10);
}

.search-bar {
  max-width: 720px;
  margin: 0 auto var(--zh-space-6);

  :deep(.el-input__inner) {
    height: 48px;
    font-size: 15px;
  }

  :deep(.el-button) {
    height: 48px;
    padding: 0 28px;
  }
}

.filter-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: var(--zh-space-6);
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-8);
}

.result-section {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  box-shadow: var(--zh-shadow);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--zh-space-4);

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--zh-space-2);
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--zh-primary);

    .el-icon {
      color: var(--zh-accent);
    }
  }
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--zh-space-4);
}

.result-card {
  padding: var(--zh-space-4);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  background: var(--zh-bg-warm);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    border-color: var(--zh-primary-soft);
    box-shadow: var(--zh-shadow-sm);
    transform: translateY(-2px);
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--zh-text-primary);
    margin-bottom: var(--zh-space-2);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .card-meta {
    display: flex;
    align-items: center;
    gap: var(--zh-space-2);
    margin-bottom: var(--zh-space-2);

    .meta-item {
      font-size: 13px;
      color: var(--zh-text-secondary);
    }

    .price {
      color: var(--zh-danger);
      font-weight: 500;

      .free {
        color: var(--zh-success);
      }
    }
  }

  .card-desc {
    font-size: 13px;
    color: var(--zh-text-secondary);
    line-height: 1.6;
    margin: 0 0 var(--zh-space-3);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--zh-text-tertiary);

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }
}

@media (max-width: 992px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
}
</style>
