<template>
  <div class="page-container">
    <!-- 返回按钮 + 标题 -->
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><Back /></el-icon>
        返回
      </el-button>
      <h2>{{ project?.projectName || '项目详情' }}</h2>
    </div>

    <!-- 加载中 -->
    <el-skeleton :loading="loading.detail" animated :count="6" v-if="loading.detail" />

    <!-- 项目不存在 -->
    <el-empty v-else-if="!project" description="项目不存在" :image-size="120" />

    <!-- 项目内容 -->
    <template v-else>
      <!-- 基本信息 -->
      <el-card class="detail-card" shadow="never">
        <div class="project-title-row">
          <h3>{{ project.projectName }}</h3>
          <el-tag :type="statusType(project.status)" effect="dark" size="large">
            {{ statusText(project.status) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border class="project-info">
          <el-descriptions-item label="项目编号">{{ project.projectCode || '--' }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">
            <el-tag size="small">{{ project.projectType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="研究领域">
            <el-tag size="small" type="info" v-if="project.researchFields">{{ project.researchFields }}</el-tag>
            <span v-else>--</span>
          </el-descriptions-item>
          <el-descriptions-item label="发布方类型">
            {{ publisherTypeText(project.publisherType) }}
          </el-descriptions-item>
          <el-descriptions-item label="成员">
            <el-icon><User /></el-icon> {{ project.currentMembers }}/{{ project.maxMembers }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">
            <el-icon><View /></el-icon> {{ project.views }}
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ project.startDate || '--' }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ project.endDate || '--' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ project.createTime }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 项目描述 -->
      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">项目简介</span></template>
        <p class="desc-text">{{ project.projectDescription || '暂无描述' }}</p>
      </el-card>

      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">成员要求</span></template>
        <p class="desc-text">{{ project.requirements || '暂无要求' }}</p>
      </el-card>

      <el-card class="detail-card" shadow="never">
        <template #header><span class="card-title">预期成果</span></template>
        <p class="desc-text">{{ project.expectedOutcomes || '暂无说明' }}</p>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <!-- 学生：申请加入 -->
        <el-button v-if="userStore.isStudent && project.status === 'recruiting'" type="primary" size="large" @click="showApply = true">
          <el-icon><Document /></el-icon> 申请加入
        </el-button>

        <!-- 发布人：状态管理 -->
        <template v-if="isPublisher">
          <el-button type="success" @click="handleUpdateStatus('ongoing')" v-if="project.status === 'recruiting'">
            开始进行
          </el-button>
          <el-button type="warning" @click="handleUpdateStatus('completed')" v-if="project.status === 'ongoing'">
            标记完成
          </el-button>
          <el-button @click="handleUpdateStatus('closed')" v-if="project.status !== 'closed' && project.status !== 'completed'">
            关闭项目
          </el-button>
        </template>
      </div>

      <!-- 申请对话框 -->
      <el-dialog v-model="showApply" title="申请加入项目" width="500px">
        <el-form :model="applyForm" label-width="80px">
          <el-form-item label="申请理由">
            <el-input v-model="applyForm.message" type="textarea" :rows="4" placeholder="请简要说明你的研究方向、技能和加入理由..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showApply = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleApply">提交申请</el-button>
        </template>
      </el-dialog>

      <!-- 申请列表（发布人可见） -->
      <el-card v-if="isPublisher && applications.length > 0" class="detail-card" shadow="never">
        <template #header><span class="card-title">申请列表 ({{ applications.length }})</span></template>
        <el-table :data="applications" stripe>
          <el-table-column prop="applicantId" label="申请人ID" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="appStatusType(row.status)" size="small">
                {{ row.status === 'pending' ? '待审核' : row.status === 'approved' ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="replyMessage" label="申请理由" min-width="150" />
          <el-table-column prop="createTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="160" v-if="hasPending">
            <template #default="{ row }">
              <template v-if="row.status === 'pending'">
                <el-button size="small" type="success" @click="handleAudit(row.id, 'approved')">通过</el-button>
                <el-button size="small" type="danger" @click="handleAudit(row.id, 'rejected')">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, User, View, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProjectDetail, applyProject, getProjectApplications, auditApplication, updateProjectStatus } from '@/api/research'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const project = ref<any>(null)
const applications = ref<any[]>([])
const loading = reactive({ detail: true })
const showApply = ref(false)
const submitting = ref(false)

const applyForm = reactive({ message: '' })

const isPublisher = computed(() =>
  project.value && userStore.userInfo?.id === project.value.publisherId
)

const hasPending = computed(() =>
  applications.value.some((a: any) => a.status === 'pending')
)

function statusText(status: string) {
  const map: Record<string, string> = {
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

function statusType(status: string): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    recruiting: 'success',
    ongoing: 'warning',
    completed: 'info',
    closed: 'danger'
  }
  return map[status] || 'info'
}

function appStatusType(status: string): 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

function publisherTypeText(type: string) {
  const map: Record<string, string> = {
    teacher: '教师',
    enterprise: '企业',
    student: '学生',
    admin: '管理员'
  }
  return map[type] || type
}

function goBack() {
  router.push('/app/research/projects')
}

async function loadDetail() {
  loading.detail = true
  try {
    const id = route.params.id as string
    const [detailRes, appRes] = await Promise.all([
      getProjectDetail(id),
      getProjectApplications(id).catch(() => ({ data: [] }))
    ])
    project.value = detailRes?.data || null
    applications.value = appRes?.data || []
  } catch (e: any) {
    ElMessage.error('加载项目详情失败')
  } finally {
    loading.detail = false
  }
}

async function handleApply() {
  submitting.value = true
  try {
    await applyProject({
      projectId: project.value.id,
      applicantId: userStore.userInfo!.id,
      replyMessage: applyForm.message,
      status: 'pending'
    })
    ElMessage.success('申请已提交，等待发布人审核')
    showApply.value = false
    applyForm.message = ''
  } catch (e: any) {
    ElMessage.error(e?.message || '提交申请失败')
  } finally {
    submitting.value = false
  }
}

async function handleAudit(id: number, status: string) {
  try {
    await auditApplication(id, status)
    ElMessage.success(status === 'approved' ? '已通过该申请' : '已拒绝该申请')
    // 重新加载
    const appRes = await getProjectApplications(route.params.id as string).catch(() => ({ data: [] }))
    applications.value = appRes?.data || []
    // 如果通过，重新加载详情获取最新成员数
    const detailRes = await getProjectDetail(route.params.id as string)
    project.value = detailRes?.data || null
  } catch (e: any) {
    ElMessage.error('操作失败')
  }
}

async function handleUpdateStatus(status: string) {
  const statusNames: Record<string, string> = {
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  try {
    await ElMessageBox.confirm(`确定将项目状态更新为「${statusNames[status] || status}」？`, '确认操作')
    await updateProjectStatus(route.params.id as string, status)
    ElMessage.success('状态已更新')
    project.value = { ...project.value, status }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
  }
}

.detail-card {
  margin-bottom: 20px;

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.project-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 20px;
    color: #303133;
  }
}

.project-info {
  :deep(.el-descriptions__label) {
    width: 100px;
    color: #909399;
  }
}

.desc-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
</style>
