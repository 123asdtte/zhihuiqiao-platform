<template>
  <div class="learning-resource-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">学习资源</h1>
        <p class="zh-page-subtitle">探索课程、视频、论文、图书与工具，提升学习与科研效率</p>
      </div>
      <el-button v-if="canPublish" type="primary" class="publish-btn" @click="goToPublish">
        <el-icon><Plus /></el-icon>
        发布学习资源
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-select v-model="searchForm.resourceType" placeholder="全部类型" clearable @change="handleSearch">
          <el-option label="在线课程" value="course" />
          <el-option label="教学视频" value="video" />
          <el-option label="学术论文" value="paper" />
          <el-option label="电子图书" value="book" />
          <el-option label="工具软件" value="tool" />
        </el-select>
        <el-select v-model="searchForm.subject" placeholder="全部学科" clearable @change="handleSearch">
          <el-option label="计算机科学" value="计算机科学" />
          <el-option label="人工智能" value="人工智能" />
          <el-option label="电子工程" value="电子工程" />
          <el-option label="数学" value="数学" />
          <el-option label="物理学" value="物理学" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="searchForm.difficultyLevel" placeholder="全部难度" clearable @change="handleSearch">
          <el-option label="初级" value="初级" />
          <el-option label="中级" value="中级" />
          <el-option label="高级" value="高级" />
        </el-select>
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索资源名称/描述"
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

    <!-- 学习资源卡片列表 -->
    <div v-loading="loading" class="resource-grid">
      <div class="resource-card" v-for="item in resourceList" :key="item.id">
        <div class="resource-image" @click="goToDetail(item.id)">
          <el-image
            :src="item.coverUrl || defaultImage"
            fit="cover"
            lazy
            style="width: 100%; height: 180px"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="resource-type-badge">{{ typeText(item.resourceType) }}</div>
          <div class="resource-difficulty" :class="difficultyClass(item.difficultyLevel)">
            {{ item.difficultyLevel || '未标注' }}
          </div>
        </div>
        <div class="resource-info">
          <h3 class="resource-name" @click="goToDetail(item.id)">{{ item.resourceName }}</h3>
          <div class="resource-meta">
            <span class="zh-tag">{{ item.subject || '通用' }}</span>
            <span class="resource-views">
              <el-icon><View /></el-icon>
              {{ item.views || 0 }}
            </span>
          </div>
          <p class="resource-desc">{{ item.description || '暂无描述' }}</p>
          <div class="resource-actions">
            <el-button
              link
              :type="item.isFavorited ? 'danger' : 'info'"
              size="small"
              @click.stop="handleToggleFavorite(item)"
            >
              <el-icon><Star /></el-icon>
              {{ item.isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button
              v-if="userStore.isStudent"
              link
              type="primary"
              size="small"
              class="learn-btn"
              @click.stop="handleStartLearning(item.id)"
            >
              开始学习
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="resourceList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无相关学习资源" />
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
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { debounce } from '@/utils/debounce'
import { Picture, View, Star, Search, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getLearningResourceList, saveLearningRecord, toggleFavoriteResource } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

// 默认占位图片
const defaultImage = 'https://placehold.co/300x160/e4e7ed/909399?text=暂无封面'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  resourceType: '',
  subject: '',
  difficultyLevel: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 资源列表
const resourceList = ref<any[]>([])
const loading = ref(false)

/**
 * 类型文本映射
 */
function typeText(type: string) {
  const map: Record<string, string> = {
    course: '在线课程',
    video: '教学视频',
    paper: '学术论文',
    book: '电子图书',
    tool: '工具软件'
  }
  return map[type] || type
}

/**
 * 难度等级样式
 */
function difficultyClass(level: string) {
  const map: Record<string, string> = {
    '初级': 'beginner',
    '中级': 'intermediate',
    '高级': 'advanced'
  }
  return map[level] || ''
}

/**
 * 加载学习资源列表
 */
async function loadResourceList() {
  loading.value = true
  try {
    const res: any = await getLearningResourceList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      resourceType: searchForm.resourceType,
      subject: searchForm.subject,
      difficultyLevel: searchForm.difficultyLevel
    })
    resourceList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载学习资源失败')
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
  loadResourceList()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  searchForm.keyword = ''
  searchForm.resourceType = ''
  searchForm.subject = ''
  searchForm.difficultyLevel = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadResourceList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadResourceList()
}

/**
 * 判断当前用户是否具备发布权限
 */
const canPublish = computed(() => {
  return userStore.userInfo?.roleType === 'teacher' || userStore.userInfo?.roleType === 'admin'
})

/**
 * 跳转发布页
 */
function goToPublish() {
  router.push('/app/learning/publish')
}

/**
 * 跳转资源详情页
 */
function goToDetail(id: number) {
  router.push(`/app/learning/detail/${id}`)
}

/**
 * 开始学习：创建学习记录
 */
async function handleStartLearning(resourceId: number) {
  try {
    const res: any = await saveLearningRecord({
      resourceId,
      progress: 0,
      status: 'learning'
    })
    if (res.code === 200) {
      ElMessage.success('已加入学习')
      router.push(`/app/learning/center?id=${resourceId}`)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('开始学习失败')
    console.error(error)
  }
}

/**
 * 收藏或取消收藏学习资源
 */
async function handleToggleFavorite(item: any) {
  try {
    const res: any = await toggleFavoriteResource(item.id)
    if (res.code === 200) {
      item.isFavorited = !item.isFavorited
      ElMessage.success(item.isFavorited ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('收藏操作失败')
    console.error(error)
  }
}

onMounted(() => {
  loadResourceList()
})
</script>

<style scoped lang="scss">
.learning-resource-page {
  padding-bottom: var(--zh-space-8);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--zh-space-5);
  margin-bottom: var(--zh-space-8);
}

.resource-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  overflow: hidden;
  box-shadow: var(--zh-shadow);
  transition: all var(--zh-transition);

  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--zh-shadow-md);

    .resource-image img,
    .resource-image .el-image {
      transform: scale(1.05);
    }
  }
}

.resource-image {
  position: relative;
  overflow: hidden;
  cursor: pointer;

  .el-image,
  img {
    transition: transform var(--zh-transition-slow);
  }

  .image-placeholder {
    width: 100%;
    height: 180px;
    background: var(--zh-bg-warm);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--zh-text-tertiary);
    font-size: 36px;
  }

  .resource-type-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 5px 12px;
    border-radius: 100px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    background: var(--zh-primary);
    box-shadow: var(--zh-shadow-sm);
  }

  .resource-difficulty {
    position: absolute;
    bottom: 12px;
    right: 12px;
    padding: 4px 10px;
    border-radius: 100px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    background: var(--zh-text-tertiary);

    &.beginner { background: var(--zh-success); }
    &.intermediate { background: var(--zh-warning); }
    &.advanced { background: var(--zh-danger); }
  }
}

.resource-info {
  padding: var(--zh-space-5);

  .resource-name {
    font-family: var(--zh-font-display);
    font-size: 17px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-3) 0;
    line-height: 1.4;
    cursor: pointer;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 48px;

    &:hover {
      color: var(--zh-secondary);
    }
  }

  .resource-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--zh-space-3);

    .resource-views {
      display: flex;
      align-items: center;
      gap: var(--zh-space-1);
      font-size: 12px;
      color: var(--zh-text-tertiary);
    }
  }

  .resource-desc {
    font-size: 13px;
    color: var(--zh-text-secondary);
    line-height: 1.6;
    margin: 0 0 var(--zh-space-4) 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 42px;
  }

  .resource-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: var(--zh-space-3);
    border-top: 1px solid var(--zh-border-light);

    .learn-btn {
      color: var(--zh-accent) !important;
      font-weight: 600;

      &:hover {
        color: var(--zh-accent-light) !important;
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
}

@media (max-width: 1400px) {
  .resource-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }
}
</style>
