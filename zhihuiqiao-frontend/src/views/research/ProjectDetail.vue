<template>
  <div class="project-detail-page zh-page">
    <div class="back-nav">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回项目列表
      </el-button>
    </div>

    <div v-if="project" class="detail-layout">
      <!-- 左侧主要内容 -->
      <div class="detail-main">
        <div class="detail-hero">
          <div class="detail-header">
            <div class="header-tags">
              <span class="project-status" :class="project.status">{{ statusText(project.status) }}</span>
              <span class="zh-tag">{{ project.projectType }}</span>
              <span class="project-code">编号：{{ project.projectCode || '-' }}</span>
            </div>
            <h1 class="detail-title">{{ project.projectName }}</h1>
            <div class="detail-meta">
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ project.views }} 次浏览
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                发布于 {{ project.createTime || '未知' }}
              </span>
            </div>
          </div>

          <div class="detail-actions-bar">
            <el-button
              v-if="canDeleteProject"
              size="large"
              type="danger"
              plain
              @click="handleDeleteProject"
            >
              删除项目
            </el-button>
            <el-button
              v-if="canViewApplications"
              size="large"
              @click="showApplicationsDrawer"
            >
              查看申请
            </el-button>
            <el-button
              type="primary"
              size="large"
              :disabled="!canApply"
              @click="showApplyDialog"
              class="apply-btn"
            >
              {{ applyButtonText }}
            </el-button>
          </div>
        </div>

        <div class="detail-sections">
          <div class="detail-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              项目简介
            </h3>
            <div class="section-content">{{ project.projectDescription || '暂无项目简介' }}</div>
          </div>

          <div class="detail-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              成员要求
            </h3>
            <div class="section-content">{{ project.requirements || '暂无成员要求' }}</div>
          </div>

          <div class="detail-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              预期成果
            </h3>
            <div class="section-content">{{ project.expectedOutcomes || '暂无预期成果' }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧信息栏 -->
      <aside class="detail-sidebar">
        <div class="sidebar-card info-card">
          <h4 class="sidebar-title">项目信息</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">研究领域</span>
              <span class="info-value">{{ project.researchFields || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">项目周期</span>
              <span class="info-value">{{ project.startDate || '待定' }} 至 {{ project.endDate || '待定' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">团队规模</span>
              <span class="info-value">{{ project.currentMembers || 0 }} / {{ project.maxMembers || 0 }} 人</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布人</span>
              <span class="info-value">{{ project.publisherName || '未知' }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card progress-card">
          <h4 class="sidebar-title">招募进度</h4>
          <div class="progress-ring-wrap">
            <el-progress
              type="dashboard"
              :percentage="memberProgress"
              :color="progressColor"
              :stroke-width="10"
              :width="140"
            />
            <div class="progress-text">已招募 {{ project.currentMembers || 0 }} 人</div>
          </div>
        </div>
      </aside>
    </div>

    <el-skeleton v-else :rows="10" animated />

    <!-- 申请列表抽屉 -->
    <el-drawer
      v-model="applicationsDrawerVisible"
      title="项目申请管理"
      size="600px"
      destroy-on-close
    >
      <el-table v-loading="applicationsLoading" :data="applications" style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="applyReason" label="申请理由" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="applicationStatusType(row.status)">
              {{ applicationStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              link
              type="success"
              size="small"
              @click="handleAudit(row.id, 'approved')"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              link
              type="danger"
              size="small"
              @click="handleAudit(row.id, 'rejected')"
            >
              拒绝
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 申请弹窗 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请加入项目"
      width="520px"
      destroy-on-close
      class="apply-dialog"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-position="top"
      >
        <el-form-item label="申请理由" prop="applyReason">
          <el-input
            v-model="applyForm.applyReason"
            type="textarea"
            :rows="5"
            placeholder="请简要说明您为什么想加入该项目，以及您能贡献什么"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleApply" class="apply-btn">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Calendar, ArrowLeft } from '@element-plus/icons-vue'
import { getProjectDetail, applyProject, getProjectApplications, auditApplication, deleteProject } from '@/api/research'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const projectId = route.params.id as string
const project = ref<any>(null)
const loading = ref(false)

// 申请弹窗
const applyDialogVisible = ref(false)
const applyLoading = ref(false)
const applyForm = reactive({
  applyReason: ''
})
const applyFormRef = ref<any>(null)

// 申请列表抽屉
const applicationsDrawerVisible = ref(false)
const applicationsLoading = ref(false)
const applications = ref<any[]>([])

const applyRules = {
  applyReason: [
    { required: true, message: '请输入申请理由', trigger: 'blur' },
    { min: 10, max: 500, message: '申请理由长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

/**
 * 是否可申请加入
 */
const canApply = computed(() => {
  if (!project.value) return false
  if (project.value.status !== 'recruiting') return false
  return userStore.isStudent
})

/**
 * 申请按钮文本
 */
const applyButtonText = computed(() => {
  if (!project.value) return '申请加入'
  if (project.value.status !== 'recruiting') return '当前项目不招募成员'
  if (!userStore.isStudent) return '仅学生可申请加入'
  return '申请加入'
})

/**
 * 是否可以查看项目申请（仅教师/管理员身份，且为项目发布者或管理员可查看）
 */
const canViewApplications = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
  if (!userStore.isTeacher) return false
  return project.value.publisherId === userStore.userInfo?.id
})

/**
 * 是否可删除项目（仅教师/管理员身份，且为项目发布者或管理员可删除）
 */
const canDeleteProject = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
  if (!userStore.isTeacher) return false
  return project.value.publisherId === userStore.userInfo?.id
})

/**
 * 招募进度百分比
 */
const memberProgress = computed(() => {
  if (!project.value || !project.value.maxMembers) return 0
  return Math.min(Math.round(((project.value.currentMembers || 0) / project.value.maxMembers) * 100), 100)
})

const progressColor = computed(() => {
  if (memberProgress.value >= 80) return '#b5423b'
  if (memberProgress.value >= 50) return '#c9a227'
  return '#1a365d'
})

/**
 * 申请状态文本映射
 */
function applicationStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

/**
 * 申请状态标签类型
 */
function applicationStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 加载项目详情
 */
async function loadProjectDetail() {
  loading.value = true
  try {
    const res: any = await getProjectDetail(projectId)
    project.value = res.data
  } catch (error) {
    ElMessage.error('加载项目详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 返回列表
 */
function goBack() {
  router.push('/app/research/projects')
}

/**
 * 显示申请弹窗
 */
function showApplyDialog() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  applyForm.applyReason = ''
  applyDialogVisible.value = true
}

/**
 * 提交申请
 */
async function handleApply() {
  if (!applyFormRef.value) return

  await applyFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    applyLoading.value = true
    try {
      const res: any = await applyProject({
        projectId: Number(projectId),
        applicantId: userStore.userInfo?.id || 0,
        applyReason: applyForm.applyReason
      })

      if (res.code === 200) {
        ElMessage.success('申请提交成功，请等待审核')
        applyDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '申请提交失败')
      }
    } catch (error: any) {
      console.error(error)
      ElMessage.error(error?.response?.data?.message || '申请提交失败，请稍后重试')
    } finally {
      applyLoading.value = false
    }
  })
}

/**
 * 显示申请列表抽屉
 */
function showApplicationsDrawer() {
  applicationsDrawerVisible.value = true
  loadApplications()
}

/**
 * 加载项目申请列表
 */
async function loadApplications() {
  applicationsLoading.value = true
  try {
    const res: any = await getProjectApplications(projectId)
    applications.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请列表失败')
    console.error(error)
  } finally {
    applicationsLoading.value = false
  }
}

/**
 * 审核申请
 */
async function handleAudit(id: number, status: string) {
  try {
    const res: any = await auditApplication(id, status)
    if (res.code === 200) {
      ElMessage.success(status === 'approved' ? '已通过申请' : '已拒绝申请')
      loadApplications()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败')
    console.error(error)
  }
}

/**
 * 删除项目
 */
async function handleDeleteProject() {
  if (!project.value) return
  try {
    await ElMessageBox.confirm(
      `确定要删除项目「${project.value.projectName}」吗？删除后将无法恢复，且相关申请记录也会被清除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await deleteProject(project.value.id)
    if (res.code === 200) {
      ElMessage.success('项目删除成功')
      router.push('/app/research/projects')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadProjectDetail()
})
</script>

<style scoped lang="scss">
.back-nav {
  margin-bottom: var(--zh-space-4);

  .el-button {
    color: var(--zh-text-secondary);
    font-weight: 500;

    &:hover {
      color: var(--zh-primary);
      background: var(--zh-primary-soft);
    }
  }
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--zh-space-6);
  align-items: start;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-6);
}

.detail-hero {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius-lg);
  padding: var(--zh-space-8);
  box-shadow: var(--zh-shadow);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 5px;
    background: linear-gradient(90deg, var(--zh-primary) 0%, var(--zh-secondary) 50%, var(--zh-accent) 100%);
  }
}

.detail-header {
  margin-bottom: var(--zh-space-6);

  .header-tags {
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);
    margin-bottom: var(--zh-space-4);
    flex-wrap: wrap;

    .project-status {
      padding: 5px 12px;
      border-radius: 100px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      background-color: var(--zh-text-tertiary);

      &.recruiting { background-color: var(--zh-success); }
      &.ongoing { background-color: var(--zh-secondary); }
      &.completed { background-color: var(--zh-text-tertiary); }
      &.closed { background-color: var(--zh-danger); }
    }

    .project-code {
      font-size: 13px;
      color: var(--zh-text-tertiary);
      font-family: var(--zh-font-mono);
    }
  }

  .detail-title {
    font-family: var(--zh-font-display);
    font-size: 32px;
    font-weight: 700;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-4) 0;
    line-height: 1.3;
  }

  .detail-meta {
    display: flex;
    gap: var(--zh-space-5);

    .meta-item {
      display: flex;
      align-items: center;
      gap: var(--zh-space-2);
      font-size: 13px;
      color: var(--zh-text-secondary);
    }
  }
}

.detail-actions-bar {
  display: flex;
  gap: var(--zh-space-3);

  .apply-btn {
    background: var(--zh-accent) !important;
    border-color: var(--zh-accent) !important;
    color: #fff !important;
    font-weight: 600;

    &:hover:not(:disabled) {
      background: var(--zh-accent-light) !important;
      border-color: var(--zh-accent-light) !important;
    }

    &:disabled {
      background: var(--zh-border) !important;
      border-color: var(--zh-border) !important;
      color: var(--zh-text-tertiary) !important;
    }
  }
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-5);
}

.detail-section {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-6);
  box-shadow: var(--zh-shadow);

  .section-title {
    font-family: var(--zh-font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-4) 0;
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);

    .section-marker {
      width: 4px;
      height: 22px;
      background: var(--zh-accent);
      border-radius: 2px;
    }
  }

  .section-content {
    font-size: 15px;
    line-height: 1.8;
    color: var(--zh-text-secondary);
    white-space: pre-wrap;
  }
}

// 侧边栏
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-5);
  position: sticky;
  top: 88px;
}

.sidebar-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-6);
  box-shadow: var(--zh-shadow);
}

.sidebar-title {
  font-family: var(--zh-font-display);
  font-size: 17px;
  font-weight: 600;
  color: var(--zh-primary);
  margin: 0 0 var(--zh-space-5) 0;
  padding-bottom: var(--zh-space-3);
  border-bottom: 1px solid var(--zh-border-light);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-1);

  .info-label {
    font-size: 12px;
    color: var(--zh-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .info-value {
    font-size: 14px;
    color: var(--zh-text-primary);
    font-weight: 500;
    line-height: 1.5;
  }
}

.progress-ring-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--zh-space-3);

  .progress-text {
    font-size: 14px;
    color: var(--zh-text-secondary);
  }
}

@media (max-width: 1200px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-sidebar {
    position: static;
    flex-direction: row;

    .info-card,
    .progress-card {
      flex: 1;
    }
  }
}

@media (max-width: 768px) {
  .detail-hero {
    padding: var(--zh-space-5);
  }

  .detail-header .detail-title {
    font-size: 24px;
  }

  .detail-sidebar {
    flex-direction: column;
  }
}
</style>
