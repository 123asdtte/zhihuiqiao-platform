<template>
  <div class="project-detail-page">
    <el-page-header title="返回列表" @back="goBack" />

    <el-card v-if="project" class="detail-card" shadow="never">
      <div class="detail-header">
        <div class="header-left">
          <h2>{{ project.projectName }}</h2>
          <div class="header-meta">
            <el-tag :type="statusType(project.status)">{{ statusText(project.status) }}</el-tag>
            <el-tag type="info">{{ project.projectType }}</el-tag>
            <span class="project-code">编号：{{ project.projectCode || '-' }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="view-count">
            <el-icon><View /></el-icon>
            {{ project.views }} 次浏览
          </div>
        </div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="研究领域">{{ project.researchFields || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="项目周期">
          {{ project.startDate || '待定' }} 至 {{ project.endDate || '待定' }}
        </el-descriptions-item>
        <el-descriptions-item label="团队成员">
          {{ project.currentMembers || 0 }} / {{ project.maxMembers || 0 }} 人
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ project.createTime || '暂无' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <h3>项目简介</h3>
        <p>{{ project.projectDescription || '暂无简介' }}</p>
      </div>

      <div class="detail-section">
        <h3>成员要求</h3>
        <p>{{ project.requirements || '暂无要求' }}</p>
      </div>

      <div class="detail-section">
        <h3>预期成果</h3>
        <p>{{ project.expectedOutcomes || '暂无预期成果' }}</p>
      </div>

      <div class="detail-actions">
        <el-button
          v-if="canViewApplications"
          type="default"
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
        >
          {{ applyButtonText }}
        </el-button>
      </div>
    </el-card>

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
      width="500px"
      destroy-on-close
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
            :rows="4"
            placeholder="请简要说明您为什么想加入该项目，以及您能贡献什么"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { getProjectDetail, applyProject, getProjectApplications, auditApplication } from '@/api/research'
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
 * 状态标签类型
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    recruiting: 'success',
    ongoing: 'primary',
    completed: 'info',
    closed: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 是否可申请加入
 */
const canApply = computed(() => {
  if (!project.value) return false
  // 只有招募中的项目可以申请
  if (project.value.status !== 'recruiting') return false
  // 学生角色可以申请加入
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
 * 是否可以查看项目申请（教师/管理员/企业可查看）
 */
const canViewApplications = computed(() => {
  // 当前实现：教师、管理员、企业角色可查看项目申请
  // 后续可扩展为仅项目负责人可见
  return userStore.isTeacher || userStore.isAdmin || userStore.isEnterprise
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

onMounted(() => {
  loadProjectDetail()
})
</script>

<style scoped lang="scss">
.project-detail-page {
  padding: 20px;

  .detail-card {
    margin-top: 20px;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;

      .header-left {
        h2 {
          margin: 0 0 12px 0;
          color: #303133;
          font-size: 22px;
        }

        .header-meta {
          display: flex;
          align-items: center;
          gap: 12px;

          .project-code {
            font-size: 13px;
            color: #909399;
          }
        }
      }

      .header-right {
        .view-count {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 14px;
          color: #909399;
        }
      }
    }

    .detail-section {
      margin-top: 24px;

      h3 {
        margin: 0 0 12px 0;
        font-size: 16px;
        color: #303133;
        border-left: 4px solid #409eff;
        padding-left: 10px;
      }

      p {
        margin: 0;
        color: #606266;
        line-height: 1.8;
        font-size: 14px;
      }
    }

    .detail-actions {
      margin-top: 32px;
      display: flex;
      justify-content: center;
      gap: 16px;
    }
  }
}
</style>
