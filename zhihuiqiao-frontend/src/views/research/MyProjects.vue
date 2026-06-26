<template>
  <div class="my-projects-page zh-page">
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">我的项目</h1>
        <p class="zh-page-subtitle">管理您发布的科研项目</p>
      </div>
      <el-button type="primary" class="publish-btn" @click="router.push('/app/research/project/publish')">
        <el-icon><Plus /></el-icon>
        发布项目
      </el-button>
    </div>

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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getMyProjects, deleteProject } from '@/api/research'

const router = useRouter()

const loading = ref(false)
const projectList = ref<any[]>([])
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const deletableStatuses = ['draft', 'pending_audit', 'recruiting']

function statusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    pending_audit: '待审核',
    recruiting: '招募中',
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
    ongoing: 'primary',
    completed: '',
    closed: 'danger'
  }
  return map[status] || 'info'
}

function canDelete(status: string) {
  return deletableStatuses.includes(status)
}

async function loadProjectList() {
  loading.value = true
  try {
    const res: any = await getMyProjects({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
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
</style>
