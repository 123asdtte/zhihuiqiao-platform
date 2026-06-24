<template>
  <div class="learning-center-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>学习中心</h2>
      <p>管理你的学习进度、收藏与已完成资源</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stat.learning }}</div>
          <div class="stat-label">正在学习</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stat.completed }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stat.favorite }}</div>
          <div class="stat-label">我的收藏</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 标签页 -->
    <el-card class="record-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="正在学习" name="learning" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="我的收藏" name="favorite" />
      </el-tabs>

      <el-table v-loading="loading" :data="recordList" style="width: 100%">
        <el-table-column label="资源封面" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.coverUrl || defaultImage"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="resourceName" label="资源名称" min-width="180" />
        <el-table-column prop="resourceType" label="类型" width="100">
          <template #default="{ row }">
            {{ typeText(row.resourceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="学习进度" width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :status="row.status === 'completed' ? 'success' : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleUpdateProgress(row)">更新进度</el-button>
            <el-button v-if="row.status !== 'favorite'" link type="success" @click="handleComplete(row)">完成</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="recordList.length === 0 && !loading" :description="emptyText" />
    </el-card>

    <!-- 更新进度弹窗 -->
    <el-dialog v-model="progressDialogVisible" title="更新学习进度" width="420px">
      <el-form :model="progressForm" label-width="100px">
        <el-form-item label="学习进度">
          <el-slider v-model="progressForm.progress" :max="100" show-input />
        </el-form-item>
        <el-form-item label="上次位置">
          <el-input v-model="progressForm.lastPosition" placeholder="请输入视频秒数或页码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyLearningRecords, saveLearningRecord, deleteLearningRecord } from '@/api/learning'

// 默认占位图片
const defaultImage = 'https://placehold.co/80x60/e4e7ed/909399?text=暂无封面'

// 标签页
const activeTab = ref('learning')
const loading = ref(false)
const recordList = ref<any[]>([])

// 统计数据
const stat = reactive({
  learning: 0,
  completed: 0,
  favorite: 0
})

// 空状态文案
const emptyText = computed(() => {
  const map: Record<string, string> = {
    learning: '暂无正在学习的资源，快去资源列表开始学习吧',
    completed: '暂无已完成资源',
    favorite: '暂无收藏资源'
  }
  return map[activeTab.value]
})

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
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    learning: '学习中',
    completed: '已完成',
    favorite: '已收藏'
  }
  return map[status] || status
}

/**
 * 状态标签类型
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    learning: 'primary',
    completed: 'success',
    favorite: 'warning'
  }
  return map[status] || 'info'
}

/**
 * 加载学习记录
 */
async function loadRecords() {
  loading.value = true
  try {
    const res: any = await getMyLearningRecords(activeTab.value)
    recordList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载学习记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载统计数据
 */
async function loadStats() {
  try {
    const [learningRes, completedRes, favoriteRes] = await Promise.all([
      getMyLearningRecords('learning'),
      getMyLearningRecords('completed'),
      getMyLearningRecords('favorite')
    ])
    stat.learning = (learningRes.data || []).length
    stat.completed = (completedRes.data || []).length
    stat.favorite = (favoriteRes.data || []).length
  } catch (error) {
    console.error(error)
  }
}

/**
 * 切换标签页
 */
function handleTabChange() {
  loadRecords()
}

// 更新进度弹窗
const progressDialogVisible = ref(false)
const progressForm = reactive({
  id: null as number | null,
  resourceId: null as number | null,
  progress: 0,
  lastPosition: 0
})

/**
 * 打开更新进度弹窗
 */
function handleUpdateProgress(row: any) {
  progressForm.id = row.id
  progressForm.resourceId = row.resourceId
  progressForm.progress = row.progress || 0
  progressForm.lastPosition = row.lastPosition || 0
  progressDialogVisible.value = true
}

/**
 * 提交进度更新
 */
async function submitProgress() {
  try {
    const res: any = await saveLearningRecord({
      id: progressForm.id,
      resourceId: progressForm.resourceId,
      progress: progressForm.progress,
      lastPosition: progressForm.lastPosition,
      status: progressForm.progress >= 100 ? 'completed' : 'learning'
    })
    if (res.code === 200) {
      ElMessage.success('进度更新成功')
      progressDialogVisible.value = false
      loadRecords()
      loadStats()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新进度失败')
    console.error(error)
  }
}

/**
 * 标记完成
 */
async function handleComplete(row: any) {
  try {
    const res: any = await saveLearningRecord({
      id: row.id,
      resourceId: row.resourceId,
      progress: 100,
      lastPosition: row.lastPosition || 0,
      status: 'completed'
    })
    if (res.code === 200) {
      ElMessage.success('已标记完成')
      loadRecords()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

/**
 * 删除学习记录
 */
async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该学习记录？', '提示', { type: 'warning' })
    const res: any = await deleteLearningRecord(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRecords()
      loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

onMounted(() => {
  loadRecords()
  loadStats()
})
</script>

<style scoped lang="scss">
.learning-center-page {
  padding: 20px;

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

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;
      margin-bottom: 20px;

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-label {
        color: #606266;
        font-size: 14px;
      }
    }
  }

  .record-card {
    .el-tabs {
      margin-bottom: 16px;
    }
  }
}
</style>
