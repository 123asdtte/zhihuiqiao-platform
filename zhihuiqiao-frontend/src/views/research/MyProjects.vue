<template>
  <div class="my-projects-page zh-page">
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">我的项目</h1>
        <p class="zh-page-subtitle">管理您发布、加入或申请的科研项目</p>
      </div>
      <el-button type="primary" class="publish-btn" @click="router.push('/app/research/project/publish')">
        <el-icon><Plus /></el-icon>
        发布项目
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="project-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我发布的" name="published" />
      <el-tab-pane label="我加入的" name="joined" />
      <el-tab-pane label="我申请的" name="applied" />
    </el-tabs>

    <!-- 项目类标签：我发布的 / 我加入的 -->
    <template v-if="activeTab !== 'applied'">
      <div class="filter-bar">
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="pending_audit" />
          <el-option label="招募中" value="recruiting" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已完成" value="completed" />
          <el-option label="已关闭" value="closed" />
        </el-select>
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索项目名称"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button text @click="handleReset">重置筛选</el-button>
      </div>

      <div v-loading="loading" class="project-table-wrapper">
        <el-table :data="projectList" style="width: 100%" border>
          <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="projectType" label="项目类型" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="views" label="浏览量" width="90" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goToDetail(row.id)">查看</el-button>
              <el-button link type="danger" :disabled="!canDelete(row.status)" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <div v-if="projectList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无项目" />
      </div>
    </template>

    <!-- 我申请的：申请记录列表 -->
    <template v-else>
      <div class="filter-bar">
        <el-select v-model="applicationSearchForm.status" placeholder="全部申请状态" clearable @change="handleApplicationSearch">
          <el-option label="待审核" value="pending" />
          <el-option label="待沟通" value="interview" />
          <el-option label="已通过" value="approved" />
          <el-option label="已入组" value="confirmed" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="已撤回" value="withdrawn" />
        </el-select>
        <el-input
          v-model="applicationSearchForm.keyword"
          placeholder="搜索项目名称"
          clearable
          style="width: 260px"
          @keyup.enter="handleApplicationSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button text @click="handleApplicationReset">重置筛选</el-button>
      </div>

      <div v-loading="applicationLoading" class="project-table-wrapper">
        <el-table :data="filteredApplications" style="width: 100%" border>
          <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <el-button link type="primary" @click="goToDetail(row.projectId)">
                {{ row.projectName || '未知项目' }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="applyReason" label="申请理由" min-width="240" show-overflow-tooltip />
          <el-table-column label="申请状态" width="110">
            <template #default="{ row }">
              <el-tag :type="applicationStatusType(row.status)">{{ applicationStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="replyMessage" label="审核回复" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="reply-text">{{ row.replyMessage || '暂无回复' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="170" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="goToDetail(row.projectId)">查看项目</el-button>
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
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="filteredApplications.length === 0 && !applicationLoading" class="empty-state">
        <el-empty description="暂无申请记录" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getMyProjects,
  getJoinedProjects,
  deleteProject,
  getMyApplications,
  withdrawApplication,
  confirmAdmission
} from '@/api/research'

const router = useRouter()

const loading = ref(false)
const projectList = ref<any[]>([])
const activeTab = ref('published')
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 我申请的列表相关状态
const applicationLoading = ref(false)
const applications = ref<any[]>([])
const applicationSearchForm = reactive({
  keyword: '',
  status: ''
})

const deletableStatuses = ['draft', 'pending_audit', 'recruiting']

function statusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    pending_audit: '待审核',
    recruiting: '招募中',
    in_progress: '进行中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

function statusType(status: string) {
  const map: Record<string, any> = {
    draft: 'info',
    pending_audit: 'warning',
    recruiting: 'success',
    in_progress: 'primary',
    ongoing: 'primary',
    completed: '',
    closed: 'danger'
  }
  return map[status] || 'info'
}

function canDelete(status: string) {
  return deletableStatuses.includes(status)
}

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
 * 申请状态标签类型映射
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
 * 加载我的申请列表
 * 后端会根据当前登录用户返回自己的申请，管理员可查看全部
 */
async function loadApplications() {
  applicationLoading.value = true
  try {
    const res: any = await getMyApplications()
    applications.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请记录失败')
    console.error(error)
  } finally {
    applicationLoading.value = false
  }
}

/**
 * 根据搜索条件过滤后的申请记录
 */
const filteredApplications = computed(() => {
  return applications.value.filter((item) => {
    const matchStatus = !applicationSearchForm.status || item.status === applicationSearchForm.status
    const keyword = applicationSearchForm.keyword.trim()
    const matchKeyword = !keyword || (item.projectName || '').toLowerCase().includes(keyword.toLowerCase())
    return matchStatus && matchKeyword
  })
})

async function loadProjectList() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    }
    const api = activeTab.value === 'joined' ? getJoinedProjects : getMyProjects
    const res: any = await api(params)
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pagination.pageNum = 1
  if (activeTab.value === 'applied') {
    loadApplications()
  } else {
    loadProjectList()
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadProjectList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadProjectList()
}

function handlePageChange(page: number) {
  pagination.pageNum = page
  loadProjectList()
}

function goToDetail(id: number) {
  router.push(`/app/research/projects/${id}`)
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目「${row.projectName}」吗？删除后将无法恢复，且相关申请记录也会被清除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res: any = await deleteProject(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadProjectList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '删除失败')
  }
}

/**
 * 我申请的：按条件搜索（本地过滤）
 */
function handleApplicationSearch() {
  // 过滤逻辑由 computed 属性自动完成，此处触发即可
}

/**
 * 我申请的：重置筛选条件
 */
function handleApplicationReset() {
  applicationSearchForm.keyword = ''
  applicationSearchForm.status = ''
}

/**
 * 申请人撤回申请
 */
async function handleWithdraw(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要撤回对项目「${row.projectName || '未知项目'}」的申请吗？`,
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
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '撤回失败')
  }
}

/**
 * 申请人确认入组
 */
async function handleConfirm(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要加入项目「${row.projectName || '未知项目'}」吗？`,
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
  } catch (error: any) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error?.response?.data?.message || '确认失败')
  }
}

onMounted(() => {
  loadProjectList()
})
</script>

<style scoped lang="scss">
.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--zh-space-6);
}

.publish-btn {
  background: var(--zh-accent) !important;
  border-color: var(--zh-accent) !important;
  color: #fff !important;
  font-weight: 600;

  &:hover {
    background: var(--zh-accent-light) !important;
    border-color: var(--zh-accent-light) !important;
  }
}

.project-tabs {
  margin-bottom: var(--zh-space-4);
}

.filter-bar {
  display: flex;
  gap: var(--zh-space-3);
  align-items: center;
  margin-bottom: var(--zh-space-6);
}

.project-table-wrapper {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  box-shadow: var(--zh-shadow);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--zh-space-6);
}

.empty-state {
  padding: var(--zh-space-16) 0;
}

.reply-text {
  color: var(--zh-text-secondary);
}
</style>
