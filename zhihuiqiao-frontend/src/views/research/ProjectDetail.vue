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
              v-if="canManageProject && project.status === 'recruiting'"
              size="large"
              type="success"
              @click="handleStartProject"
            >
              开始项目
            </el-button>
            <el-button
              v-if="canManageProject && project.status === 'in_progress'"
              size="large"
              type="warning"
              @click="handleCompleteProject"
            >
              结项
            </el-button>
            <el-button
              v-if="canViewApplications"
              size="large"
              @click="showApplicationsDrawer"
            >
              查看申请
            </el-button>
            <el-button
              size="large"
              @click="showMembersDrawer"
            >
              项目成员
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

          <!-- 项目工作台：动态/任务/成果 -->
          <div class="detail-section workspace-section">
            <div class="section-header-with-action">
              <h3 class="section-title">
                <span class="section-marker"></span>
                项目工作台
              </h3>
            </div>
            <el-tabs v-model="activeProcessTab" v-loading="processLoading">
              <!-- 项目动态 -->
              <el-tab-pane label="项目动态" name="dynamic">
                <div v-if="canManageProject" class="workspace-toolbar">
                  <el-button type="primary" size="small" @click="dynamicFormVisible = true">
                    发布动态
                  </el-button>
                </div>
                <el-empty v-if="dynamics.length === 0" description="暂无项目动态" />
                <div v-else class="dynamic-list">
                  <div v-for="item in dynamics" :key="item.id" class="dynamic-item">
                    <div class="dynamic-header">
                      <span class="dynamic-publisher">{{ item.publisherName || '未知用户' }}</span>
                      <el-tag size="small" :type="item.dynamicType === 'announcement' ? 'primary' : item.dynamicType === 'progress' ? 'success' : 'info'">
                        {{ item.dynamicType === 'announcement' ? '公告' : item.dynamicType === 'progress' ? '进展' : '讨论' }}
                      </el-tag>
                      <span class="dynamic-time">{{ item.createTime }}</span>
                    </div>
                    <div class="dynamic-content">{{ item.content }}</div>
                    <div v-if="canManageProject || item.publisherId === userStore.userInfo?.id" class="dynamic-actions">
                      <el-button link type="danger" size="small" @click="handleDeleteDynamic(item.id)">删除</el-button>
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 项目任务 -->
              <el-tab-pane label="项目任务" name="task">
                <div v-if="canManageProject" class="workspace-toolbar">
                  <el-button type="primary" size="small" @click="taskDialogVisible = true">
                    创建任务
                  </el-button>
                </div>
                <el-empty v-if="tasks.length === 0" description="暂无项目任务" />
                <div v-else class="task-list">
                  <el-card v-for="task in tasks" :key="task.id" class="task-card" shadow="never">
                    <div class="task-header">
                      <span class="task-name">{{ task.taskName }}</span>
                      <el-tag size="small" :type="taskStatusType(task.status)">{{ taskStatusText(task.status) }}</el-tag>
                    </div>
                    <p class="task-desc">{{ task.description || '暂无描述' }}</p>
                    <p class="task-meta">负责人：{{ task.assigneeName || '未分配' }}</p>
                    <p class="task-meta">截止：{{ task.deadline || '未设置' }}</p>
                    <div class="task-actions">
                      <el-button v-if="task.status !== 'completed'" link type="success" size="small" @click="handleUpdateTaskStatus(task, 'completed')">完成</el-button>
                      <el-button v-if="task.status === 'pending'" link type="primary" size="small" @click="handleUpdateTaskStatus(task, 'in_progress')">开始</el-button>
                      <el-button v-if="canManageProject" link type="danger" size="small" @click="handleDeleteTask(task.id)">删除</el-button>
                    </div>
                  </el-card>
                </div>
              </el-tab-pane>

              <!-- 项目成果 -->
              <el-tab-pane label="项目成果" name="outcome">
                <div v-if="canManageProject" class="workspace-toolbar">
                  <el-button type="primary" size="small" @click="outcomeDialogVisible = true">
                    上传成果
                  </el-button>
                </div>
                <el-empty v-if="outcomes.length === 0" description="暂无项目成果" />
                <div v-else class="outcome-list">
                  <el-card v-for="outcome in outcomes" :key="outcome.id" class="outcome-card" shadow="never">
                    <div class="outcome-header">
                      <span class="outcome-name">{{ outcome.outcomeName }}</span>
                      <el-tag size="small" type="info">{{ outcomeTypeText(outcome.outcomeType) }}</el-tag>
                    </div>
                    <p class="outcome-desc">{{ outcome.description || '暂无描述' }}</p>
                    <p v-if="outcome.fileUrl" class="outcome-meta">附件：<a :href="outcome.fileUrl" target="_blank">查看</a></p>
                    <div v-if="canManageProject" class="outcome-actions">
                      <el-button link type="danger" size="small" @click="handleDeleteOutcome(outcome.id)">删除</el-button>
                    </div>
                  </el-card>
                </div>
              </el-tab-pane>
            </el-tabs>
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <!-- 发布者/管理员操作 -->
            <template v-if="canViewApplications">
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
                @click="handleAudit(row.id, 'approved')"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 'pending' || row.status === 'interview'"
                link
                type="danger"
                size="small"
                @click="handleAudit(row.id, 'rejected')"
              >
                拒绝
              </el-button>
            </template>
            <!-- 申请人操作 -->
            <template v-if="row.applicantId === userStore.userInfo?.id">
              <el-button
                v-if="row.status === 'pending'"
                link
                type="warning"
                size="small"
                @click="handleWithdraw(row.id)"
              >
                撤回
              </el-button>
              <el-button
                v-if="row.status === 'approved'"
                link
                type="success"
                size="small"
                @click="handleConfirm(row.id)"
              >
                确认入组
              </el-button>
            </template>
            <span v-if="['confirmed', 'rejected', 'withdrawn'].includes(row.status)">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 项目成员抽屉 -->
    <el-drawer
      v-model="membersDrawerVisible"
      title="项目成员"
      size="500px"
      destroy-on-close
    >
      <el-table v-loading="membersLoading" :data="members" style="width: 100%">
        <el-table-column prop="userName" label="成员" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'leader' ? 'primary' : 'info'">
              {{ row.role === 'leader' ? '负责人' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" min-width="160" />
        <el-table-column
          v-if="canViewApplications"
          label="操作"
          width="180"
          fixed="right"
        >
          <template #default="{ row }">
            <el-select
              v-if="row.userId !== project?.publisherId"
              :model-value="row.role"
              placeholder="设置角色"
              size="small"
              style="width: 90px; margin-right: 8px"
              @change="(val: any) => handleUpdateMemberRole(row.userId, val)"
            >
              <el-option label="负责人" value="leader" />
              <el-option label="成员" value="member" />
            </el-select>
            <el-button
              v-if="row.userId !== project?.publisherId"
              link
              type="danger"
              size="small"
              @click="handleRemoveMember(row.userId, row.userName)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!membersLoading && members.length === 0" description="暂无成员" />
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

    <!-- 发布动态弹窗 -->
    <el-dialog v-model="dynamicFormVisible" title="发布项目动态" width="560px">
      <el-form :model="dynamicForm" label-position="top">
        <el-form-item label="动态类型">
          <el-radio-group v-model="dynamicForm.dynamicType">
            <el-radio value="announcement">公告</el-radio>
            <el-radio value="progress">进展</el-radio>
            <el-radio value="discussion">讨论</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="动态内容">
          <el-input v-model="dynamicForm.content" type="textarea" :rows="4" placeholder="请输入动态内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dynamicFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateDynamic">发布</el-button>
      </template>
    </el-dialog>

    <!-- 创建任务弹窗 -->
    <el-dialog v-model="taskDialogVisible" title="创建项目任务" width="560px">
      <el-form :model="taskForm" label-position="top">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.assigneeId" placeholder="选择负责人" clearable style="width: 100%">
            <el-option v-for="m in members" :key="m.userId" :label="m.userName" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="taskForm.deadline" type="date" placeholder="选择截止日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTask">创建</el-button>
      </template>
    </el-dialog>

    <!-- 上传成果弹窗 -->
    <el-dialog v-model="outcomeDialogVisible" title="上传项目成果" width="560px">
      <el-form :model="outcomeForm" label-position="top">
        <el-form-item label="成果名称">
          <el-input v-model="outcomeForm.outcomeName" placeholder="请输入成果名称" />
        </el-form-item>
        <el-form-item label="成果类型">
          <el-select v-model="outcomeForm.outcomeType" placeholder="选择类型" style="width: 100%">
            <el-option label="论文" value="paper" />
            <el-option label="专利" value="patent" />
            <el-option label="代码" value="code" />
            <el-option label="视频" value="video" />
            <el-option label="报告" value="report" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="成果描述">
          <el-input v-model="outcomeForm.description" type="textarea" :rows="3" placeholder="请输入成果描述" />
        </el-form-item>
        <el-form-item label="成果附件URL">
          <el-input v-model="outcomeForm.fileUrl" placeholder="可填写附件链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outcomeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateOutcome">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Calendar, ArrowLeft } from '@element-plus/icons-vue'
import {
  getProjectDetail,
  applyProject,
  getProjectApplications,
  getMyApplications,
  auditApplication,
  deleteProject,
  withdrawApplication,
  confirmAdmission,
  interviewApplication,
  getProjectMembers,
  removeProjectMember,
  updateProjectMemberRole,
  getProjectDynamics,
  createProjectDynamic,
  deleteProjectDynamic,
  getProjectTasks,
  createProjectTask,
  updateProjectTaskStatus,
  deleteProjectTask,
  getProjectOutcomes,
  createProjectOutcome,
  deleteProjectOutcome,
  startProject,
  completeProject
} from '@/api/research'
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

// 当前用户对该项目的申请记录（用于判断是否可以再次申请）
const myApplication = ref<any>(null)

// 项目成员
const members = ref<any[]>([])
const membersLoading = ref(false)
const membersDrawerVisible = ref(false)

// 项目过程管理
const activeProcessTab = ref('dynamic')
const dynamics = ref<any[]>([])
const tasks = ref<any[]>([])
const outcomes = ref<any[]>([])
const processLoading = ref(false)
const dynamicFormVisible = ref(false)
const dynamicForm = reactive({
  content: '',
  dynamicType: 'announcement'
})
const taskDialogVisible = ref(false)
const taskForm = reactive({
  taskName: '',
  description: '',
  assigneeId: null as any,
  deadline: '',
  status: 'pending'
})
const outcomeDialogVisible = ref(false)
const outcomeForm = reactive({
  outcomeName: '',
  outcomeType: 'other',
  description: '',
  fileUrl: ''
})

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
    in_progress: '进行中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭',
    pending_audit: '待审核'
  }
  return map[status] || status
}

/**
 * 是否可申请加入
 */
const canApply = computed(() => {
  if (!project.value) return false
  if (project.value.status !== 'recruiting') return false
  if (!userStore.isStudent) return false
  // 已存在非拒绝/非撤回状态的申请时，禁止再次申请
  if (myApplication.value && !['rejected', 'withdrawn'].includes(myApplication.value.status)) return false
  return true
})

/**
 * 当前用户是否已入组
 */
const isJoined = computed(() => {
  return members.value.some((m) => m.userId === userStore.userInfo?.id)
})

/**
 * 申请按钮文本
 */
const applyButtonText = computed(() => {
  if (!project.value) return '申请加入'
  if (isJoined.value) return '已是项目成员'
  if (project.value.status !== 'recruiting') return '当前项目不招募成员'
  if (!userStore.isStudent) return '仅学生可申请加入'
  if (myApplication.value) {
    if (myApplication.value.status === 'pending') return '申请审核中'
    if (myApplication.value.status === 'interview') return '待沟通'
    if (myApplication.value.status === 'approved') return '待确认入组'
    if (myApplication.value.status === 'confirmed') return '已加入该项目'
    if (myApplication.value.status === 'rejected') return '申请已被拒绝'
    if (myApplication.value.status === 'withdrawn') return '申请已撤回'
  }
  return '申请加入'
})

/**
 * 是否可以查看项目申请（管理员可查看所有，发布者可查看自己项目的申请）
 */
const canViewApplications = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
  return project.value.publisherId === userStore.userInfo?.id
})

/**
 * 是否可删除项目（管理员可删除所有，发布者可删除自己的项目）
 */
const canDeleteProject = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
  return project.value.publisherId === userStore.userInfo?.id
})

/**
 * 是否可管理项目过程（管理员或项目负责人）
 */
const canManageProject = computed(() => {
  if (!project.value) return false
  if (userStore.isAdmin) return true
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
    interview: '待沟通',
    approved: '已通过',
    confirmed: '已入组',
    rejected: '已拒绝',
    withdrawn: '已撤回'
  }
  return map[status] || status
}

/**
 * 申请状态标签类型
 */
function applicationStatusType(status: string) {
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
 * 加载项目详情
 */
async function loadProjectDetail() {
  loading.value = true
  try {
    const res: any = await getProjectDetail(projectId)
    project.value = res.data
    // 项目加载完成后，查询成员、过程数据与当前用户申请记录
    await Promise.all([loadMembers(), loadProcessData()])
    if (userStore.isStudent && userStore.isLoggedIn) {
      await loadMyApplication()
    }
  } catch (error) {
    ElMessage.error('加载项目详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载当前用户对当前项目的申请记录
 */
async function loadMyApplication() {
  try {
    const res: any = await getMyApplications()
    if (res.code === 200 && res.data) {
      myApplication.value = res.data.find((item: any) => String(item.projectId) === projectId)
    }
  } catch (error) {
    console.error('加载我的申请记录失败', error)
  }
}

/**
 * 加载项目成员列表
 */
async function loadMembers() {
  membersLoading.value = true
  try {
    const res: any = await getProjectMembers(projectId)
    members.value = res.data || []
  } catch (error) {
    console.error('加载项目成员失败', error)
  } finally {
    membersLoading.value = false
  }
}

/**
 * 加载项目过程数据（动态/任务/成果）
 */
async function loadProcessData() {
  processLoading.value = true
  try {
    const [dRes, tRes, oRes] = await Promise.all([
      getProjectDynamics(projectId),
      getProjectTasks(projectId),
      getProjectOutcomes(projectId)
    ])
    dynamics.value = dRes.data || []
    tasks.value = tRes.data || []
    outcomes.value = oRes.data || []
  } catch (error) {
    console.error('加载项目过程数据失败', error)
  } finally {
    processLoading.value = false
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
  membersDrawerVisible.value = false
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
 * 将申请标记为待沟通
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
async function handleConfirm(id: number) {
  try {
    await ElMessageBox.confirm('确定要加入该项目吗？', '确认入组', {
      confirmButtonText: '确定加入',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res: any = await confirmAdmission(id)
    if (res.code === 200) {
      ElMessage.success('已确认入组')
      loadApplications()
      loadProjectDetail()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

/**
 * 申请人撤回申请
 */
async function handleWithdraw(id: number) {
  try {
    await ElMessageBox.confirm('确定要撤回该申请吗？', '撤回确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await withdrawApplication(id)
    if (res.code === 200) {
      ElMessage.success('申请已撤回')
      loadApplications()
      loadMyApplication()
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

/**
 * 显示项目成员抽屉
 */
function showMembersDrawer() {
  membersDrawerVisible.value = true
  applicationsDrawerVisible.value = false
  loadMembers()
}

/**
 * 移除项目成员
 */
async function handleRemoveMember(userId: number, userName: string) {
  try {
    await ElMessageBox.confirm(
      `确定要将「${userName || '该成员'}」移出项目吗？`,
      '移除成员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await removeProjectMember(projectId, userId)
    if (res.code === 200) {
      ElMessage.success('成员已移除')
      loadMembers()
      loadProjectDetail()
    } else {
      ElMessage.error(res.message || '移除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

/**
 * 更新成员角色
 */
async function handleUpdateMemberRole(userId: number, role: string) {
  try {
    const res: any = await updateProjectMemberRole(projectId, userId, role)
    if (res.code === 200) {
      ElMessage.success('角色已更新')
      loadMembers()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
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

// ==================== 项目过程管理操作 ====================

async function handleStartProject() {
  try {
    await ElMessageBox.confirm('确定要开始该项目吗？开始后项目将进入“进行中”状态。', '开始项目', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res: any = await startProject(projectId)
    // 确保确认弹窗关闭，避免页面刷新后 DOM 残留
    ElMessageBox.close()
    if (res.code === 200) {
      ElMessage.success('项目已开始')
      loadProjectDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

async function handleCompleteProject() {
  try {
    await ElMessageBox.confirm('确定要结项吗？结项后项目将标记为“已完成”。', '结项确认', {
      confirmButtonText: '确定结项',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await completeProject(projectId)
    // 确保确认弹窗关闭，避免页面刷新后 DOM 残留
    ElMessageBox.close()
    if (res.code === 200) {
      ElMessage.success('项目已结项')
      loadProjectDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

async function handleCreateDynamic() {
  if (!dynamicForm.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  try {
    const res: any = await createProjectDynamic(projectId, { ...dynamicForm })
    if (res.code === 200) {
      ElMessage.success('动态发布成功')
      dynamicForm.content = ''
      dynamicFormVisible.value = false
      loadProcessData()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error(error)
  }
}

async function handleDeleteDynamic(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除该动态吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteProjectDynamic(id)
    if (res.code === 200) {
      ElMessage.success('动态已删除')
      loadProcessData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

async function handleCreateTask() {
  if (!taskForm.taskName.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }
  try {
    const res: any = await createProjectTask(projectId, { ...taskForm })
    if (res.code === 200) {
      ElMessage.success('任务创建成功')
      taskForm.taskName = ''
      taskForm.description = ''
      taskForm.assigneeId = null
      taskForm.deadline = ''
      taskDialogVisible.value = false
      loadProcessData()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error(error)
  }
}

async function handleUpdateTaskStatus(task: any, status: string) {
  try {
    const res: any = await updateProjectTaskStatus(task.id, status)
    if (res.code === 200) {
      ElMessage.success('任务状态已更新')
      loadProcessData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error(error)
  }
}

async function handleDeleteTask(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteProjectTask(id)
    if (res.code === 200) {
      ElMessage.success('任务已删除')
      loadProcessData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

async function handleCreateOutcome() {
  if (!outcomeForm.outcomeName.trim()) {
    ElMessage.warning('请输入成果名称')
    return
  }
  try {
    const res: any = await createProjectOutcome(projectId, { ...outcomeForm })
    if (res.code === 200) {
      ElMessage.success('成果上传成功')
      outcomeForm.outcomeName = ''
      outcomeForm.description = ''
      outcomeForm.fileUrl = ''
      outcomeDialogVisible.value = false
      loadProcessData()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    console.error(error)
  }
}

async function handleDeleteOutcome(id: number | string) {
  try {
    await ElMessageBox.confirm('确定要删除该成果吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteProjectOutcome(id)
    if (res.code === 200) {
      ElMessage.success('成果已删除')
      loadProcessData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error(error)
  }
}

function taskStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待处理',
    in_progress: '进行中',
    completed: '已完成'
  }
  return map[status] || status
}

function taskStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'info',
    in_progress: 'warning',
    completed: 'success'
  }
  return map[status] || 'info'
}

function outcomeTypeText(type: string) {
  const map: Record<string, string> = {
    paper: '论文',
    patent: '专利',
    code: '代码',
    video: '视频',
    report: '报告',
    other: '其他'
  }
  return map[type] || '其他'
}

onMounted(() => {
  loadProjectDetail().then(() => {
    // 若从通知中心点击项目申请通知跳转过来，自动打开申请管理抽屉
    if (route.query.openApplications === '1' && canViewApplications.value) {
      showApplicationsDrawer()
    }
  })
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

// 项目工作台
.workspace-section {
  .workspace-toolbar {
    margin-bottom: var(--zh-space-4);
    display: flex;
    justify-content: flex-end;
  }

  .dynamic-list {
    display: flex;
    flex-direction: column;
    gap: var(--zh-space-4);
  }

  .dynamic-item {
    padding: var(--zh-space-4);
    background: var(--zh-bg-soft);
    border-radius: var(--zh-radius);
    border: 1px solid var(--zh-border-light);

    .dynamic-header {
      display: flex;
      align-items: center;
      gap: var(--zh-space-2);
      margin-bottom: var(--zh-space-2);

      .dynamic-publisher {
        font-weight: 600;
        color: var(--zh-text-primary);
      }

      .dynamic-time {
        margin-left: auto;
        font-size: 13px;
        color: var(--zh-text-tertiary);
      }
    }

    .dynamic-content {
      color: var(--zh-text-secondary);
      line-height: 1.6;
      white-space: pre-wrap;
    }

    .dynamic-actions {
      margin-top: var(--zh-space-2);
    }
  }

  .task-list,
  .outcome-list {
    display: flex;
    flex-direction: column;
    gap: var(--zh-space-3);
  }

  .task-card,
  .outcome-card {
    .task-header,
    .outcome-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--zh-space-2);

      .task-name,
      .outcome-name {
        font-weight: 600;
        color: var(--zh-text-primary);
      }
    }

    .task-desc,
    .outcome-desc {
      color: var(--zh-text-secondary);
      margin: var(--zh-space-2) 0;
    }

    .task-meta,
    .outcome-meta {
      font-size: 13px;
      color: var(--zh-text-tertiary);
      margin: var(--zh-space-1) 0;
    }

    .task-actions,
    .outcome-actions {
      margin-top: var(--zh-space-2);
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
