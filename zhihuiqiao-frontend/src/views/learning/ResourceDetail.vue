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
              <el-button v-if="!recordId" type="primary" size="large" @click="handleStartLearning">
                开始学习
              </el-button>
              <el-button v-else type="primary" size="large" @click="openNoteDialog">
                写笔记
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
              <el-button size="large" @click="openReviewDialog">
                {{ reviewForm.rating ? '修改评价' : '评价' }}
              </el-button>
            </div>

            <!-- 我的学习状态 -->
            <div v-if="recordId" class="my-learning-section">
              <h4>我的学习</h4>
              <div class="learning-progress">
                <span class="label">学习进度：</span>
                <el-progress :percentage="myRecord?.progress || 0" style="flex: 1" />
              </div>
              <div v-if="myRecord?.lastPosition" class="learning-position">
                <span class="label">上次位置：</span>
                <span class="value">{{ myRecord.lastPosition }}</span>
              </div>
              <div v-if="myRecord?.note" class="learning-note">
                <span class="label">我的笔记：</span>
                <p class="note-content">{{ myRecord.note }}</p>
              </div>
              <div v-if="myRecord?.rating" class="learning-review">
                <span class="label">我的评价：</span>
                <el-rate v-model="myRecord.rating" disabled show-score />
                <p v-if="myRecord.comment" class="comment-content">{{ myRecord.comment }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 资源不存在或已删除 -->
    <el-empty v-else description="资源不存在或已删除" />

    <!-- 学习笔记弹窗 -->
    <el-dialog v-model="noteDialogVisible" title="学习笔记" width="520px">
      <el-form :model="noteForm" label-width="80px">
        <el-form-item label="笔记内容">
          <el-input
            v-model="noteForm.note"
            type="textarea"
            :rows="6"
            placeholder="记录你的学习心得、重点摘录或疑问..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNote">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学习评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="学习评价" width="420px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="分享你的学习体验，帮助其他同学..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Picture, Star } from '@element-plus/icons-vue'
import {
  getLearningResourceDetail,
  saveLearningRecord,
  toggleFavoriteResource,
  updateLearningNote,
  updateLearningReview
} from '@/api/learning'

const route = useRoute()
const router = useRouter()

// 默认占位图片
const defaultImage = 'https://placehold.co/800x400/e4e7ed/909399?text=暂无封面'

// 资源详情
const resource = ref<any>(null)
const loading = ref(false)

// 当前用户学习记录
const myRecord = ref<any>(null)
const recordId = ref<number | null>(null)

// 收藏状态与加载状态
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 笔记与评价弹窗
const noteDialogVisible = ref(false)
const noteForm = reactive({ note: '' })
const reviewDialogVisible = ref(false)
const reviewForm = reactive({ rating: 0, comment: '' })

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
    if (res.code === 200 && res.data) {
      resource.value = res.data.resource || null
      myRecord.value = res.data.myRecord || null
      recordId.value = res.data.myRecord?.id || null
      isFavorited.value = res.data.myRecord?.isFavorited || false
      noteForm.note = res.data.myRecord?.note || ''
      reviewForm.rating = res.data.myRecord?.rating || 0
      reviewForm.comment = res.data.myRecord?.comment || ''
    }
  } catch (error) {
    ElMessage.error('加载学习资源详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 开始学习：保存学习记录
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
      await loadResourceDetail()
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
      ElMessage.success(isFavorited.value ? '已取消收藏' : '收藏成功')
      await loadResourceDetail()
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
 * 打开笔记弹窗
 */
function openNoteDialog() {
  if (!recordId.value) {
    ElMessage.warning('请先开始学习')
    return
  }
  noteDialogVisible.value = true
}

/**
 * 保存学习笔记
 */
async function submitNote() {
  if (!recordId.value) return
  try {
    const res: any = await updateLearningNote(recordId.value, noteForm.note)
    if (res.code === 200) {
      ElMessage.success('笔记保存成功')
      noteDialogVisible.value = false
      await loadResourceDetail()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存笔记失败')
    console.error(error)
  }
}

/**
 * 打开评价弹窗
 */
function openReviewDialog() {
  if (!recordId.value) {
    ElMessage.warning('请先开始学习')
    return
  }
  reviewDialogVisible.value = true
}

/**
 * 提交学习评价
 */
async function submitReview() {
  if (!recordId.value) return
  try {
    const res: any = await updateLearningReview(recordId.value, reviewForm.rating, reviewForm.comment)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      reviewDialogVisible.value = false
      await loadResourceDetail()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交评价失败')
    console.error(error)
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

      .my-learning-section {
        margin-top: 24px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;

        h4 {
          margin: 0 0 16px 0;
          color: #303133;
        }

        .learning-progress,
        .learning-position,
        .learning-note,
        .learning-review {
          display: flex;
          align-items: flex-start;
          margin-bottom: 14px;
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

        .learning-progress {
          align-items: center;
        }

        .note-content,
        .comment-content {
          flex: 1;
          margin: 0;
          color: #606266;
          line-height: 1.6;
          background: #f5f7fa;
          padding: 10px 12px;
          border-radius: 4px;
        }
      }
    }
  }
}
</style>
