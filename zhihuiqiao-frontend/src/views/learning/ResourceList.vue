<template>
  <div class="learning-resource-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <h2>学习资源</h2>
        <p>探索课程、视频、论文、图书与工具，提升学习与科研效率</p>
      </div>
      <!-- 发布学习资源按钮：仅教师或管理员可见 -->
      <el-button
        v-if="canPublish"
        type="primary"
        @click="goToPublish"
      >
        发布学习资源
      </el-button>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.resourceType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option label="在线课程" value="course" />
            <el-option label="教学视频" value="video" />
            <el-option label="学术论文" value="paper" />
            <el-option label="电子图书" value="book" />
            <el-option label="工具软件" value="tool" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科领域">
          <el-select v-model="searchForm.subject" placeholder="全部学科" clearable @change="handleSearch">
            <el-option label="计算机科学" value="计算机科学" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="电子工程" value="电子工程" />
            <el-option label="数学" value="数学" />
            <el-option label="物理学" value="物理学" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="searchForm.difficultyLevel" placeholder="全部难度" clearable @change="handleSearch">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索资源名称/描述"
            clearable
            style="width: 240px"
            @input="handleKeywordInput"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资源列表 -->
    <el-row v-loading="loading" :gutter="20" class="resource-list">
      <el-col v-for="item in resourceList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="resource-card" shadow="hover">
          <div class="resource-image" @click="goToDetail(item.id)">
            <el-image
              :src="item.coverUrl || defaultImage"
              fit="cover"
              lazy
              style="width: 100%; height: 160px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="resource-type">{{ typeText(item.resourceType) }}</div>
          </div>
          <div class="resource-info">
            <h3 class="resource-name" @click="goToDetail(item.id)">{{ item.resourceName }}</h3>
            <div class="resource-meta">
              <el-tag size="small" type="info">{{ item.subject || '通用' }}</el-tag>
              <span class="difficulty">{{ item.difficultyLevel || '未标注' }}</span>
            </div>
            <p class="resource-desc">{{ item.description }}</p>
            <div class="resource-footer">
              <span class="resource-views">
                <el-icon><View /></el-icon>
                {{ item.views || 0 }}
              </span>
              <div class="resource-actions">
                <!-- 收藏按钮 -->
                <el-button
                  link
                  :type="item.isFavorited ? 'danger' : 'info'"
                  size="small"
                  @click.stop="handleToggleFavorite(item)"
                >
                  <el-icon><Star /></el-icon>
                  {{ item.isFavorited ? '已收藏' : '收藏' }}
                </el-button>
                <!-- 开始学习按钮：仅学生可见 -->
                <el-button
                  v-if="userStore.isStudent"
                  link
                  type="primary"
                  size="small"
                  @click.stop="handleStartLearning(item.id)"
                >
                  开始学习
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="resourceList.length === 0 && !loading" description="暂无相关学习资源" />

    <!-- 分页 -->
    <div class="pagination-wrapper">
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
import { Picture, View, Star } from '@element-plus/icons-vue'
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
 * 关键词输入防抖搜索，减少频繁请求
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
 * 判断当前用户是否具备发布权限（教师或管理员）
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
      // 切换当前资源的收藏状态
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
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .header-title {
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
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .resource-list {
    margin-bottom: 20px;

    .resource-card {
      margin-bottom: 20px;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }

      .resource-image {
        position: relative;
        cursor: pointer;

        .resource-type {
          position: absolute;
          top: 8px;
          left: 8px;
          background: rgba(64, 158, 255, 0.9);
          color: #fff;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
        }
      }

      .resource-info {
        padding-top: 12px;

        .resource-name {
          margin: 0 0 8px 0;
          font-size: 16px;
          color: #303133;
          cursor: pointer;

          &:hover {
            color: #409eff;
          }
        }

        .resource-meta {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 8px;

          .difficulty {
            color: #909399;
            font-size: 13px;
          }
        }

        .resource-desc {
          margin: 0 0 12px 0;
          color: #606266;
          font-size: 13px;
          line-height: 1.5;
          height: 40px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .resource-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .resource-views {
            color: #909399;
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 4px;
          }

          .resource-actions {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 20px 0;
  }
}

.image-placeholder {
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 32px;
}
</style>
