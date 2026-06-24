<template>
  <div class="learning-resource-detail-page">
    <!-- 顶部返回按钮 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回资源列表
      </el-button>
    </div>

    <!-- 资源详情主体 -->
    <div v-if="resource" v-loading="loading" class="detail-content">
      <el-row :gutter="24">
        <!-- 左侧：资源封面大图 -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="never">
            <div class="resource-cover-wrapper">
              <el-image
                :src="resource.coverUrl || defaultImage"
                fit="cover"
                style="width: 100%; height: 400px; border-radius: 8px"
                :preview-src-list="[resource.coverUrl || defaultImage]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：资源信息与操作 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="info-card">
            <h2 class="resource-name">{{ resource.resourceName }}</h2>
            <div class="resource-meta">
              <el-tag type="info">{{ typeText(resource.resourceType) }}</el-tag>
              <el-tag type="warning">{{ resource.difficultyLevel || '未标注' }}</el-tag>
              <el-tag type="success">{{ resource.subject || '通用' }}</el-tag>
            </div>

            <div class="info-section">
              <div class="info-item">
                <span class="label">浏览量：</span>
                <span class="value">{{ resource.views || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="label">点赞数：</span>
                <span class="value">{{ resource.likes || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="label">学科领域：</span>
                <span class="value">{{ resource.subject || '通用' }}</span>
              </div>
              <div class="info-item">
                <span class="label">难度等级：</span>
                <span class="value">{{ resource.difficultyLevel || '未标注' }}</span>
              </div>
            </div>

            <div class="info-section">
              <h4>资源描述</h4>
              <p class="description">{{ resource.description || '暂无描述' }}</p>
            </div>

            <div class="info-section">
              <h4>内容链接</h4>
              <el-link
                v-if="resource.contentUrl"
                type="primary"
                :href="resource.contentUrl"
                target="_blank"
                :underline="false"
              >
                {{ resource.contentUrl }}
              </el-link>
              <p v-else class="description">暂无内容链接</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" size="large" @click="handleStartLearning">
                开始学习
              </el-button>
              <el-button
                size="large"
                :type="isFavorited ? 'danger' : 'default'"
                :loading="favoriteLoading"
                @click="handleToggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 资源不存在或已删除 -->
    <el-empty v-else description="资源不存在或已删除" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Picture, Star } from '@element-plus/icons-vue'
import {
  getLearningResourceDetail,
  saveLearningRecord,
  toggleFavoriteResource
} from '@/api/learning'

const route = useRoute()
const router = useRouter()

// 默认占位图片
const defaultImage = 'https://placehold.co/800x400/e4e7ed/909399?text=暂无封面'

// 资源详情
const resource = ref<any>(null)
const loading = ref(false)

// 收藏状态与加载状态
const isFavorited = ref(false)
const favoriteLoading = ref(false)

/**
 * 资源类型文本映射
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
 * 加载学习资源详情
 */
async function loadResourceDetail() {
  const id = route.params.id as string
  loading.value = true
  try {
    const res: any = await getLearningResourceDetail(id)
    resource.value = res.data || null
    // 如果后端返回收藏状态，则同步到本地
    isFavorited.value = res.data?.isFavorited || false
  } catch (error) {
    ElMessage.error('加载学习资源详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 开始学习：保存学习记录并跳转到学习中心
 */
async function handleStartLearning() {
  if (!resource.value) return
  try {
    const res: any = await saveLearningRecord({
      resourceId: resource.value.id,
      progress: 0,
      status: 'learning'
    })
    if (res.code === 200) {
      ElMessage.success('已加入学习')
      router.push(`/app/learning/center?id=${resource.value.id}`)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('开始学习失败')
    console.error(error)
  }
}

/**
 * 收藏或取消收藏
 */
async function handleToggleFavorite() {
  if (!resource.value) return
  favoriteLoading.value = true
  try {
    const res: any = await toggleFavoriteResource(resource.value.id)
    if (res.code === 200) {
      // 切换收藏状态
      isFavorited.value = !isFavorited.value
      ElMessage.success(isFavorited.value ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('收藏操作失败')
    console.error(error)
  } finally {
    favoriteLoading.value = false
  }
}

/**
 * 返回学习资源列表
 */
function goBack() {
  router.push('/app/learning/resources')
}

onMounted(() => {
  loadResourceDetail()
})
</script>

<style scoped lang="scss">
.learning-resource-detail-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
  }

  .detail-content {
    .resource-cover-wrapper {
      .image-placeholder {
        width: 100%;
        height: 400px;
        background-color: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #909399;
        font-size: 60px;
        border-radius: 8px;
      }
    }

    .info-card {
      .resource-name {
        margin: 0 0 16px 0;
        font-size: 22px;
        color: #303133;
      }

      .resource-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-bottom: 20px;
      }

      .info-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #ebeef5;

        &:last-of-type {
          border-bottom: none;
        }

        h4 {
          margin: 0 0 10px 0;
          color: #303133;
        }

        .info-item {
          display: flex;
          margin-bottom: 10px;
          font-size: 14px;

          .label {
            color: #909399;
            width: 80px;
            flex-shrink: 0;
          }

          .value {
            color: #606266;
          }
        }

        .description {
          margin: 0;
          color: #606266;
          line-height: 1.6;
          font-size: 14px;
        }
      }

      .action-buttons {
        display: flex;
        gap: 12px;
        margin-top: 20px;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}
</style>
