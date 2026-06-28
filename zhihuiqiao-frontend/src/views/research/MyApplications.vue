<template>
  <div class="my-applications-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-main">
        <h2 class="zh-page-title">{{ pageTitle }}</h2>
        <p class="zh-page-subtitle">{{ pageSubtitle }}</p>
      </div>
    </div>

    <el-card shadow="never" class="zh-card">
      <el-table
        v-loading="loading"
        :data="applications"
        style="width: 100%"
        class="zh-table"
      >
        <!-- 项目名称列 -->
        <el-table-column prop="projectName" label="项目名称" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" class="project-link" @click="goToProject(row.projectId)">
              {{ row.projectName }}
            </el-button>
          </template>
        </el-table-column>

        <!-- 管理员专属：申请人列 -->
        <el-table-column
          v-if="userStore.isAdmin"
          prop="applicantName"
          label="申请人"
          width="140"
        >
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">
              {{ row.applicantName || '未知用户' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 申请理由列 -->
        <el-table-column prop="applyReason" label="申请理由" min-width="240" show-overflow-tooltip />

        <!-- 申请状态列 -->
        <el-table-column prop="status" label="申请状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 审核回复列 -->
        <el-table-column prop="replyMessage" label="审核回复" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="reply-text">{{ row.replyMessage || '暂无回复' }}</span>
          </template>
        </el-table-column>

        <!-- 申请时间列 -->
        <el-table-column prop="createTime" label="申请时间" width="180" />

        <!-- 操作列 -->
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <!-- 申请人操作 -->
            <template v-if="isApplicant(row)">
              <el-button
                v-if="row.status === 'pending'"
                link
                type="warning"
                size="small"
                @click="handleWithdraw(row)"
              >
                撤回
              </el-button>
              <el-button
                v-if="row.status === 'approved'"
                link
                type="success"
                size="small"
                @click="handleConfirm(row)"
              >
                确认入组
              </el-button>
              <el-button
                v-if="row.status === 'rejected' || row.status === 'withdrawn'"
                link
                type="primary"
                size="small"
                @click="goToProject(row.projectId)"
              >
                重新申请
              </el-button>
            </template>
            <!-- 管理员/发布者操作 -->
            <template v-if="canManageApplication(row)">
              <el-button
                v-if="row.status === 'pending'"
                link
                type="primary"
                size="small"
                @click="handleInterview(row)"
              >
                待沟通
              </el-button>
              <el-button
                v-if="row.status === 'pending' || row.status === 'interview'"
                link
                type="success"
                size="small"
                @click="handleAudit(row, 'approved')"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 'pending' || row.status === 'interview'"
                link
                type="danger"
                size="small"
                @click="handleAudit(row, 'rejected')"
              >
                拒绝
              </el-button>
            </template>
            <span v-if="isDoneStatus(row.status)" class="audit-done">已结束</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && applications.length === 0" description="暂无申请记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, auditApplication, withdrawApplication, confirmAdmission, interviewApplication } from '@/api/research'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const applications = ref<any[]>([])
const loading = ref(false)

/**
 * 页面标题：学生显示"我的申请"，管理员显示"项目申请管理"
 */
const pageTitle = computed(() => (userStore.isAdmin ? '项目申请管理' : '我的申请'))

/**
 * 页面副标题
 */
const pageSubtitle = computed(() =>
  userStore.isAdmin
    ? '查看并管理平台内所有科研项目加入申请'
    : '查看您申请加入的科研项目状态'
)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审核',
    interview: '待沟通',
    approved: '已通过',
    confirmed: '已入组',
    rejected: '已拒绝',
    withdrawn: '已撤回'
  }
  return map[status] || status
}

/**
 * 状态标签类型
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    interview: 'primary',
    approved: 'success',
    confirmed: 'success',
    rejected: 'danger',
    withdrawn: 'info'
  }
  return map[status] || 'info'
}

/**
 * 加载申请列表
 * 学生：只查询自己的申请
 * 管理员：查询全部申请
 */
async function loadApplications() {
  if (!userStore.isAdmin && !userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    // 管理员不传 applicantId，后端返回全部申请；学生传入当前用户ID
    const applicantId = userStore.isAdmin ? undefined : userStore.userInfo?.id
    const res: any = await getMyApplications(applicantId)
    applications.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转项目详情
 */
function goToProject(projectId: number) {
  router.push(`/app/research/projects/${projectId}`)
}

/**
 * 当前用户是否为该申请的申请人
 */
function isApplicant(row: any) {
  return row.applicantId === userStore.userInfo?.id
}

/**
 * 当前用户是否可以管理该申请（管理员或项目发布者）
 */
function canManageApplication(row: any) {
  return userStore.isAdmin || row.publisherId === userStore.userInfo?.id
}

/**
 * 申请是否已结束
 */
function isDoneStatus(status: string) {
  return ['confirmed', 'rejected', 'withdrawn'].includes(status)
}

/**
 * 管理员/发布者快速审核申请
 */
async function handleAudit(row: any, status: string) {
  const actionText = status === 'approved' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}「${row.applicantName || '该用户'}」对项目「${row.projectName}」的申请吗？`,
      '审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: status === 'approved' ? 'success' : 'warning'
      }
    )
    const res: any = await auditApplication(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`已${actionText}该申请`)
      loadApplications()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    // 用户取消时不做任何处理
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 申请人撤回申请
 */
async function handleWithdraw(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要撤回对项目「${row.projectName}」的申请吗？`,
      '撤回确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await withdrawApplication(row.id)
    if (res.code === 200) {
      ElMessage.success('申请已撤回')
      loadApplications()
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

/**
 * 发布者将申请标记为待沟通
 */
async function handleInterview(row: any) {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入沟通安排或需要补充了解的信息',
      '标记为待沟通',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：请准备作品集，下周三进行线上交流'
      }
    )
    const res: any = await interviewApplication(row.id, value)
    if (res.code === 200) {
      ElMessage.success('已标记为待沟通')
      loadApplications()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 取消时不处理
  }
}

/**
 * 申请人确认入组
 */
async function handleConfirm(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要加入项目「${row.projectName}」吗？`,
      '确认入组',
      {
        confirmButtonText: '确定加入',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    const res: any = await confirmAdmission(row.id)
    if (res.code === 200) {
      ElMessage.success('已确认入组')
      loadApplications()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped lang="scss">
.my-applications-page {
  padding: var(--zh-space-6);

  .page-header {
    margin-bottom: var(--zh-space-6);

    .zh-page-title {
      margin: 0 0 var(--zh-space-2);
      color: var(--zh-primary);
    }

    .zh-page-subtitle {
      margin: 0;
      color: var(--zh-text-secondary);
      font-size: 14px;
    }
  }

  .project-link {
    font-weight: 600;
  }

  .reply-text {
    color: var(--zh-text-secondary);
  }

  .audit-done {
    font-size: 13px;
    color: var(--zh-text-tertiary);
  }
}
</style>
