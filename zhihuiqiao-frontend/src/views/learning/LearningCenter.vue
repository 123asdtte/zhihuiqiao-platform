<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学习中心</h2>
      <p>基于知识图谱拓扑排序，生成个性化学习路径</p>
    </div>

    <!-- 课程选择 -->
    <el-card class="course-selector" shadow="never">
      <el-form inline>
        <el-form-item label="选择课程">
          <el-select v-model="courseName" placeholder="请选择课程" style="width: 200px" @change="loadAllData">
            <el-option label="大数据分析" value="大数据分析" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="软件工程" value="软件工程" />
            <el-option label="计算机网络" value="计算机网络" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAllData">切换课程</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 整体加载 -->
    <template v-if="!loading.init">
      <!-- 进度 + 下一个推荐 -->
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <el-card class="progress-card" shadow="hover">
            <template #header><span class="card-title">学习进度</span></template>
            <div class="progress-content">
              <el-progress type="dashboard" :percentage="learningProgress" :width="140" :stroke-width="10" color="#409eff" />
              <div class="progress-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ pathResult?.totalPoints || 0 }}</span>
                  <span class="stat-label">总知识点</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ pathResult?.completedPoints || 0 }}</span>
                  <span class="stat-label">已完成</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ remainingCount }}</span>
                  <span class="stat-label">待学习</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="16">
          <el-card class="next-card" shadow="hover">
            <template #header><span class="card-title">下一个推荐学习</span></template>
            <el-skeleton :loading="loading.next" animated>
              <template v-if="nextPoint">
                <div class="next-point">
                  <h3 class="next-name">{{ nextPoint.pointName }}</h3>
                  <p class="next-desc">{{ nextPoint.description }}</p>
                  <div class="next-meta">
                    <span class="meta-item">
                      难度：
                      <el-rate :model-value="nextPoint.difficulty" disabled :max="5" size="small" />
                    </span>
                    <span class="meta-item" v-if="nextPoint.estimatedMinutes">
                      <el-icon><Clock /></el-icon> 预计 {{ nextPoint.estimatedMinutes }} 分钟
                    </span>
                  </div>
                </div>
              </template>
              <el-empty v-else-if="!loading.next" description="恭喜！所有知识点已完成" :image-size="80" />
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <!-- 学习路径步骤条 -->
      <el-card class="path-card" shadow="never">
        <template #header>
          <div class="section-header">
            <span class="card-title">
              <el-icon><List /></el-icon>
              推荐学习路径
            </span>
            <el-tag type="warning" effect="plain" size="small">拓扑排序</el-tag>
          </div>
        </template>

        <el-skeleton :loading="loading.path" animated :count="5">
          <template v-if="pathList.length > 0">
            <el-steps :active="pathResult?.completedPoints || 0" finish-status="success" align-center :space="200">
              <el-step
                v-for="(step, idx) in pathList"
                :key="step.pointId"
                :title="step.pointName"
                :description="'难度 ' + step.difficulty"
                :status="stepStatus(Number(idx))"
              />
            </el-steps>
          </template>
          <el-empty v-else-if="!loading.path" description="暂无学习路径" :image-size="80" />
        </el-skeleton>
      </el-card>

      <!-- 难度分层路径 -->
      <el-card class="leveled-card" shadow="never">
        <template #header>
          <span class="card-title"><el-icon><TrendCharts /></el-icon> 按难度分层</span>
        </template>

        <el-skeleton :loading="loading.leveled" animated :count="3">
          <template v-if="Object.keys(leveledData).length > 0">
            <div v-for="(points, level) in leveledData" :key="level" class="level-group">
              <div class="level-header">
                <el-tag :type="levelType(Number(level))" size="small" effect="dark">
                  难度 {{ level }}
                </el-tag>
                <span class="level-count">{{ points.length }} 个知识点</span>
              </div>
              <el-timeline>
                <el-timeline-item
                  v-for="pt in points"
                  :key="pt.pointId"
                  :timestamp="'难度 ' + pt.difficulty"
                  :color="levelColor(Number(level))"
                >
                  <h4 class="timeline-title">{{ pt.pointName }}</h4>
                  <p class="timeline-desc">{{ pt.description }}</p>
                </el-timeline-item>
              </el-timeline>
            </div>
          </template>
          <el-empty v-else-if="!loading.leveled" description="暂无分层数据" :image-size="80" />
        </el-skeleton>
      </el-card>

      <!-- 算法说明 -->
      <el-card class="algo-info" shadow="never">
        <div class="algo-info-content">
          <el-icon><Reading /></el-icon>
          <span>
            本课程共 <strong>{{ pathResult?.totalPoints || 0 }}</strong> 个知识点，
            已通过 <strong>Kahn 拓扑排序</strong> 算法优化学习顺序，
            确保学习每个知识点前，其前置知识已完成。
            {{ pathResult?.hasCycle ? ' 警告：知识点依赖关系存在环路，请检查数据！' : '' }}
          </span>
        </div>
      </el-card>
    </template>

    <!-- 初始加载 -->
    <el-skeleton :loading="loading.init" animated :count="8" v-else />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, List, TrendCharts, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { recommendLearningPath, getNextRecommended, getLeveledPath, getLearningProgress } from '@/api/learning'

const userStore = useUserStore()

const courseName = ref('大数据分析')
const loading = reactive({
  init: true,
  next: true,
  path: true,
  leveled: true
})

const pathResult = ref<any>(null)
const nextPoint = ref<any>(null)
const leveledData = ref<Record<string, any[]>>({})
const learningProgress = ref(0)

const pathList = computed(() => pathResult.value?.path || [])

const remainingCount = computed(() => {
  const total = pathResult.value?.totalPoints || 0
  const completed = pathResult.value?.completedPoints || 0
  return Math.max(0, total - completed)
})

function stepStatus(idx: number): 'success' | 'process' | 'wait' {
  const completed = pathResult.value?.completedPoints || 0
  if (idx < completed) return 'success'
  if (idx === completed) return 'process'
  return 'wait'
}

function levelType(level: number): 'success' | 'warning' | 'danger' {
  if (level <= 2) return 'success'
  if (level <= 3) return 'warning'
  return 'danger'
}

function levelColor(level: number) {
  if (level <= 2) return '#67c23a'
  if (level <= 3) return '#e6a23c'
  return '#f56c6c'
}

async function loadAllData() {
  loading.init = true
  loading.next = true
  loading.path = true
  loading.leveled = true

  const userId = userStore.userInfo?.id
  if (!userId) {
    loading.init = false
    return
  }

  try {
    const [pathRes, nextRes, leveledRes, progressRes] = await Promise.all([
      recommendLearningPath(userId, courseName.value).catch(() => ({ data: null })),
      getNextRecommended(userId, courseName.value).catch(() => ({ data: null })),
      getLeveledPath(userId, courseName.value).catch(() => ({ data: {} })),
      getLearningProgress(userId, courseName.value).catch(() => ({ data: 0 }))
    ])

    pathResult.value = pathRes?.data || null
    nextPoint.value = nextRes?.data || null
    leveledData.value = leveledRes?.data || {}
    learningProgress.value = progressRes?.data ?? 0
  } catch (e: any) {
    ElMessage.error('加载学习数据失败')
  } finally {
    loading.init = false
    loading.next = false
    loading.path = false
    loading.leveled = false
  }
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;

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

.course-selector {
  margin-bottom: 20px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-card {
  margin-bottom: 20px;

  .progress-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .progress-stats {
    display: flex;
    justify-content: space-around;
    width: 100%;
  }

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    .stat-value {
      font-size: 22px;
      font-weight: 700;
      color: #303133;
    }

    .stat-label {
      font-size: 12px;
      color: #909399;
    }
  }
}

.next-card {
  margin-bottom: 20px;

  .next-point {
    .next-name {
      margin: 0 0 8px 0;
      font-size: 20px;
      color: #303133;
    }

    .next-desc {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
    }

    .next-meta {
      display: flex;
      align-items: center;
      gap: 20px;
      flex-wrap: wrap;
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #606266;
    }
  }
}

.path-card {
  margin-bottom: 20px;

  :deep(.el-step) {
    flex-shrink: 0;
  }
}

.leveled-card {
  margin-bottom: 20px;

  .level-group {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .level-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .level-count {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .timeline-title {
    margin: 0 0 4px 0;
    font-size: 14px;
    color: #303133;
  }

  .timeline-desc {
    margin: 0;
    font-size: 13px;
    color: #606266;
  }
}

.algo-info {
  margin-bottom: 20px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;

  .algo-info-content {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
  }
}
</style>
