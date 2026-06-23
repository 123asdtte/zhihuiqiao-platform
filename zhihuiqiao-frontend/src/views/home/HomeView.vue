<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-banner" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.userInfo?.realName || '用户' }}</h2>
          <p>{{ roleGreeting }}</p>
        </div>
        <div class="welcome-icon">
          <el-icon :size="48"><TrendCharts /></el-icon>
        </div>
      </div>
    </el-card>

    <!-- 管理员：系统概览 -->
    <template v-if="userStore.isAdmin">
      <el-row :gutter="20" class="stat-row">
        <el-col :xs="12" :sm="6" v-for="stat in adminStats" :key="stat.label">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty description="管理后台请访问「系统管理」菜单" />
    </template>

    <!-- 学生：项目推荐 + 学习路径 -->
    <template v-if="userStore.isStudent">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <!-- 科研项目推荐 -->
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-header">
                <span class="section-title">
                  <el-icon><Search /></el-icon>
                  科研项目推荐
                </span>
                <el-tag type="success" effect="plain" size="small">基于 TF-IDF + 余弦相似度</el-tag>
              </div>
            </template>
            <el-skeleton :loading="loading.recommend" animated :count="3">
              <template v-if="recommendedProjects.length > 0">
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12" v-for="item in recommendedProjects" :key="item.project?.id" class="mb-16">
                    <el-card class="recommend-card" shadow="hover" @click="goToProject(item.project?.id)">
                      <div class="recommend-header">
                        <span class="recommend-name">{{ item.project?.projectName }}</span>
                        <el-tag :type="scoreType(item.score)" size="small" effect="dark">
                          {{ (item.score * 100).toFixed(0) }}%
                        </el-tag>
                      </div>
                      <div class="recommend-tags">
                        <el-tag size="small" type="info" v-if="item.project?.researchFields">
                          {{ item.project.researchFields }}
                        </el-tag>
                      </div>
                      <div class="recommend-reason">{{ item.reason }}</div>
                    </el-card>
                  </el-col>
                </el-row>
              </template>
              <el-empty v-else description="暂无推荐项目" :image-size="80" />
            </el-skeleton>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <!-- 学习路径概览 -->
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-header">
                <span class="section-title">
                  <el-icon><Reading /></el-icon>
                  学习路径
                </span>
                <el-tag type="warning" effect="plain" size="small">拓扑排序</el-tag>
              </div>
            </template>
            <el-skeleton :loading="loading.learning" animated>
              <div v-if="nextPoint">
                <h4 class="next-label">下一个推荐学习</h4>
                <div class="next-point-card">
                  <div class="next-point-name">{{ nextPoint.pointName }}</div>
                  <div class="next-point-meta" v-if="nextPoint.difficulty">
                    难度：<el-rate :model-value="nextPoint.difficulty" disabled :max="5" size="small" />
                  </div>
                  <div class="next-point-meta" v-if="nextPoint.estimatedMinutes">
                    <el-icon><Clock /></el-icon> 预计 {{ nextPoint.estimatedMinutes }} 分钟
                  </div>
                </div>
              </div>
              <div v-if="learningProgress !== null" class="progress-section">
                <h4 class="section-subtitle">课程进度</h4>
                <el-progress type="circle" :percentage="learningProgress" :width="100" :stroke-width="8" />
              </div>
              <el-empty v-if="!nextPoint && learningProgress === null" description="暂无数据" :image-size="80" />
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 教师/企业：需求推荐 -->
    <template v-if="userStore.isTeacher || userStore.isEnterprise">
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="section-title">
              <el-icon><Search /></el-icon>
              企业需求推荐
            </span>
            <el-tag type="success" effect="plain" size="small">基于科研能力匹配</el-tag>
          </div>
        </template>
        <el-skeleton :loading="loading.recommend" animated :count="3">
          <template v-if="recommendedDemands.length > 0">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12" :md="8" v-for="item in recommendedDemands" :key="item.demand?.id" class="mb-16">
                <el-card class="recommend-card" shadow="hover">
                  <div class="recommend-header">
                    <span class="recommend-name">{{ item.demand?.demandTitle }}</span>
                    <el-tag :type="scoreType(item.score)" size="small" effect="dark">
                      {{ (item.score * 100).toFixed(0) }}%
                    </el-tag>
                  </div>
                  <div class="recommend-tags">
                    <el-tag size="small" type="info" v-if="item.demand?.industryField">
                      {{ item.demand.industryField }}
                    </el-tag>
                  </div>
                  <div class="recommend-reason">{{ item.reason }}</div>
                </el-card>
              </el-col>
            </el-row>
          </template>
          <el-empty v-else description="暂无推荐需求" :image-size="80" />
        </el-skeleton>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { TrendCharts, Search, Reading, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { recommendProjects, recommendDemands } from '@/api/research'
import { getLearningProgress, getNextRecommended } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

const loading = reactive({
  recommend: true,
  learning: true
})

const recommendedProjects = ref<any[]>([])
const recommendedDemands = ref<any[]>([])
const nextPoint = ref<any>(null)
const learningProgress = ref<number | null>(null)

const roleGreeting = computed(() => {
  const roleMap: Record<string, string> = {
    student: '发现适合你的科研项目，开启科研之旅',
    teacher: '查看与企业需求的最新匹配，促进产学研合作',
    enterprise: '发现优秀教师团队，发布技术需求',
    admin: '管理系统运行，审核平台内容'
  }
  return roleMap[userStore.userInfo?.roleType ?? ''] || '欢迎使用智汇桥平台'
})

const adminStats = [
  { label: '科研项目', value: '--' },
  { label: '企业需求', value: '--' },
  { label: '注册用户', value: '--' },
  { label: '平台资源', value: '--' }
]

function scoreType(score: number) {
  if (score >= 0.6) return 'success'
  if (score >= 0.3) return 'warning'
  return 'info'
}

function goToProject(id: number) {
  if (id) router.push(`/app/research/projects/${id}`)
}

onMounted(async () => {
  const role = userStore.userInfo?.roleType
  const userId = userStore.userInfo?.id

  if (role === 'student' && userId) {
    try {
      const [projRes, progRes, nextRes] = await Promise.all([
        recommendProjects(userId, 6),
        getLearningProgress(userId, '默认课程').catch(() => ({ data: null })),
        getNextRecommended(userId, '默认课程').catch(() => ({ data: null }))
      ])
      recommendedProjects.value = projRes?.data || []
      learningProgress.value = progRes?.data ?? null
      nextPoint.value = nextRes?.data || null
    } catch (e: any) {
      ElMessage.error('加载推荐数据失败')
    }
    loading.recommend = false
    loading.learning = false
  }

  if ((role === 'teacher' || role === 'enterprise') && userId) {
    try {
      const res = await recommendDemands(userId, 6)
      recommendedDemands.value = res?.data || []
    } catch (e: any) {
      ElMessage.error('加载推荐数据失败')
    }
    loading.recommend = false
  }

  if (role === 'admin') {
    loading.recommend = false
    loading.learning = false
  }
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.welcome-banner {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
  color: #fff;

  :deep(.el-card__body) {
    padding: 28px 32px;
  }

  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .welcome-text {
    h2 {
      margin: 0 0 8px 0;
      font-size: 22px;
      color: #fff;
    }
    p {
      margin: 0;
      font-size: 14px;
      color: rgba(255, 255, 255, 0.85);
    }
  }

  .welcome-icon {
    opacity: 0.3;
    color: #fff;
  }
}

.stat-row {
  margin-bottom: 20px;

  .stat-card {
    text-align: center;
    margin-bottom: 20px;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
    }
    .stat-label {
      margin-top: 8px;
      font-size: 13px;
      color: #909399;
    }
  }
}

.section-card {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .section-subtitle {
    margin: 16px 0 12px 0;
    font-size: 14px;
    color: #606266;
  }
}

.mb-16 {
  margin-bottom: 16px;
}

.recommend-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .recommend-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .recommend-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  .recommend-tags {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
    flex-wrap: wrap;
  }

  .recommend-reason {
    font-size: 12px;
    color: #909399;
    line-height: 1.4;
  }
}

.next-label {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.next-point-card {
  background: #f0f9ff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #b3d8ff;

  .next-point-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .next-point-meta {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #606266;
    margin-top: 6px;
  }
}

.progress-section {
  margin-top: 20px;
  text-align: center;
}
</style>
