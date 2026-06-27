<template>
  <div class="ai-recommend-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">AI 科研项目推荐</h1>
        <p class="zh-page-subtitle">基于 DeepSeek AI 与你的科研画像，为你智能匹配最合适的科研项目</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" :loading="loading" @click="loadRecommendations">
          <el-icon><Refresh /></el-icon>
          重新推荐
        </el-button>
      </div>
    </div>

    <!-- AI 介绍卡片 -->
    <div class="ai-intro-card">
      <div class="ai-intro-icon">
        <el-icon :size="32"><Cpu /></el-icon>
      </div>
      <div class="ai-intro-content">
        <h3>DeepSeek 智能匹配</h3>
        <p>根据你的研究方向、技能特长、科研兴趣与合作意向，从全平台科研项目中筛选出最匹配的项目。</p>
      </div>
    </div>

    <!-- 推荐列表 -->
    <div v-loading="loading" class="recommend-list-wrapper">
      <template v-if="recommendList.length > 0">
        <div class="recommend-list">
          <div
            v-for="(item, index) in recommendList"
            :key="item.project?.id || index"
            class="recommend-card"
            @click="goToDetail(item.project?.id)"
          >
            <div class="recommend-reason">
              <el-icon><Star-Filled /></el-icon>
              <span>{{ item.reason || '与你匹配度较高' }}</span>
            </div>
            <div class="project-info">
              <h3 class="project-name">{{ item.project?.projectName }}</h3>
              <p class="project-desc">{{ item.project?.projectDescription }}</p>
              <div class="project-meta">
                <span class="meta-item">
                  <el-icon><Collection /></el-icon>
                  {{ item.project?.projectType || '未分类' }}
                </span>
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  {{ item.project?.publisherName || '未知' }}
                </span>
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ item.project?.startTime }} 至 {{ item.project?.endTime }}
                </span>
              </div>
            </div>
            <div class="recommend-actions">
              <el-button type="primary" size="small" @click.stop="goToDetail(item.project?.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-else description="暂无推荐项目，建议先完善科研画像" class="empty-state">
        <el-button type="primary" @click="router.push('/app/research/profile')">
          去完善科研画像
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Cpu, Collection, User, Calendar } from '@element-plus/icons-vue'
import { getRecommendedProjects } from '@/api/ai'

const router = useRouter()

// 推荐列表
const recommendList = ref<any[]>([])
const loading = ref(false)

/**
 * 加载 AI 推荐结果
 */
async function loadRecommendations() {
  loading.value = true
  try {
    const res: any = await getRecommendedProjects()
    if (res.code === 200 && res.data) {
      recommendList.value = res.data || []
    } else {
      ElMessage.warning(res.message || '推荐加载失败')
    }
  } catch (error) {
    ElMessage.error('AI 推荐加载失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转项目详情页
 */
function goToDetail(projectId: number | string) {
  if (!projectId) {
    return
  }
  router.push(`/app/research/projects/${projectId}`)
}

onMounted(() => {
  loadRecommendations()
})
</script>

<style scoped lang="scss">
.ai-recommend-page {
  padding-bottom: var(--zh-space-12);
}

// AI 介绍卡片
.ai-intro-card {
  display: flex;
  align-items: center;
  gap: var(--zh-space-5);
  background: linear-gradient(135deg, var(--zh-primary) 0%, #2c4a6e 100%);
  color: #fff;
  border-radius: var(--zh-radius);
  padding: var(--zh-space-6);
  margin-bottom: var(--zh-space-6);
  box-shadow: var(--zh-shadow);

  .ai-intro-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .ai-intro-content {
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: var(--zh-space-2);
    }

    p {
      font-size: 14px;
      opacity: 0.9;
      line-height: 1.6;
      margin: 0;
    }
  }
}

// 推荐列表
.recommend-list-wrapper {
  min-height: 300px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-4);
}

.recommend-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  box-shadow: var(--zh-shadow);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--zh-shadow-md);
    border-color: var(--zh-primary);
  }

  .recommend-reason {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-2);
    padding: var(--zh-space-1) var(--zh-space-3);
    border-radius: 100px;
    background: rgba(26, 54, 93, 0.08);
    color: var(--zh-primary);
    font-size: 13px;
    font-weight: 600;
    margin-bottom: var(--zh-space-3);

    .el-icon {
      color: var(--zh-accent);
    }
  }

  .project-info {
    .project-name {
      font-size: 18px;
      font-weight: 600;
      color: var(--zh-text-primary);
      margin-bottom: var(--zh-space-2);
    }

    .project-desc {
      font-size: 14px;
      color: var(--zh-text-secondary);
      line-height: 1.6;
      margin-bottom: var(--zh-space-3);
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .project-meta {
      display: flex;
      flex-wrap: wrap;
      gap: var(--zh-space-4);

      .meta-item {
        display: inline-flex;
        align-items: center;
        gap: var(--zh-space-1);
        font-size: 13px;
        color: var(--zh-text-tertiary);

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .recommend-actions {
    margin-top: var(--zh-space-4);
    display: flex;
    justify-content: flex-end;
  }
}

.empty-state {
  padding: var(--zh-space-16) 0;
}
</style>
