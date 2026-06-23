<template>
  <div class="page-container">
    <div class="page-header">
      <h2>科研项目</h2>
      <p>浏览科研项目，发现感兴趣的方向，加入研究团队</p>
    </div>

    <!-- 智能推荐区（仅学生） -->
    <el-card v-if="userStore.isStudent" class="recommend-section" shadow="never">
      <template #header>
        <div class="section-header">
          <span class="section-title">
            <el-icon><Search /></el-icon>
            智能推荐
          </span>
          <el-tag type="success" effect="plain" size="small">基于你的科研画像</el-tag>
        </div>
      </template>
      <el-skeleton :loading="loading.recommend" animated :count="3">
        <template v-if="recommendedList.length > 0">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" v-for="item in recommendedList" :key="item.project?.id" class="mb-16">
              <el-card class="recommend-card" shadow="hover" @click="goToDetail(item.project?.id)">
                <div class="recommend-header">
                  <span class="recommend-name">{{ item.project?.projectName }}</span>
                  <el-tag :type="scoreTag(item.score)" size="small" effect="dark">
                    {{ (item.score * 100).toFixed(0) }}%
                  </el-tag>
                </div>
                <div class="recommend-tags">
                  <el-tag size="small" type="info" v-if="item.project?.researchFields">
                    {{ item.project.researchFields }}
                  </el-tag>
                </div>
                <p class="recommend-reason">{{ item.reason }}</p>
              </el-card>
            </el-col>
          </el-row>
        </template>
        <el-empty v-else description="暂无推荐（请先完善科研画像）" :image-size="80" />
      </el-skeleton>
    </el-card>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="项目类型">
          <el-select v-model="searchForm.projectType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option label="基础研究" value="基础研究" />
            <el-option label="应用研究" value="应用研究" />
            <el-option label="技术开发" value="技术开发" />
            <el-option label="创新创业" value="创新创业" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="招募中" value="recruiting" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="项目名称/描述" clearable style="width: 220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 项目列表 -->
    <el-skeleton :loading="loading.list" animated :count="4">
      <template v-if="projectList.length > 0">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in projectList" :key="item.id">
            <el-card class="project-card" shadow="hover" @click="goToDetail(item.id)">
              <div class="card-header">
                <el-tag :type="statusType(item.status)" size="small" effect="dark">
                  {{ statusText(item.status) }}
                </el-tag>
                <el-tag size="small" type="info" v-if="item.projectType">{{ item.projectType }}</el-tag>
              </div>
              <h3 class="project-name">{{ item.projectName }}</h3>
              <div class="project-tags">
                <el-tag size="small" type="info" v-if="item.researchFields">{{ item.researchFields }}</el-tag>
              </div>
              <p class="project-desc">{{ item.projectDescription }}</p>
              <div class="card-footer">
                <span class="footer-item">
                  <el-icon><User /></el-icon>
                  {{ item.currentMembers }}/{{ item.maxMembers }}
                </span>
                <span class="footer-item">
                  <el-icon><View /></el-icon>
                  {{ item.views }}
                </span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
      <el-empty v-else description="暂无项目数据" :image-size="100" />
    </el-skeleton>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[8, 12, 16, 20]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, User, View } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProjectList, recommendProjects } from '@/api/research'

const router = useRouter()
const userStore = useUserStore()

const loading = reactive({ recommend: true, list: true })
const recommendedList = ref<any[]>([])
const projectList = ref<any[]>([])

const searchForm = reactive({
  keyword: '',
  projectType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

function statusText(status: string) {
  const map: Record<string, string> = {
    recruiting: '招募中',
    ongoing: '进行中',
    completed: '已完成',
    closed: '已关闭'
  }
  return map[status] || status
}

function statusType(status: string) {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    recruiting: 'success',
    ongoing: 'warning',
    completed: 'info',
    closed: 'danger'
  }
  return map[status] || 'info'
}

function scoreTag(score: number): 'success' | 'warning' | 'info' {
  if (score >= 0.6) return 'success'
  if (score >= 0.3) return 'warning'
  return 'info'
}

function goToDetail(id: number) {
  if (id) router.push(`/app/research/projects/${id}`)
}

async function loadProjectList() {
  loading.list = true
  try {
    const res: any = await getProjectList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      projectType: searchForm.projectType,
      status: searchForm.status
    })
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.list = false
  }
}

async function loadRecommendations() {
  if (!userStore.isStudent) {
    loading.recommend = false
    return
  }
  loading.recommend = true
  try {
    const res: any = await recommendProjects(userStore.userInfo!.id, 6)
    recommendedList.value = res?.data || []
  } catch (e: any) {
    // silently fail for recommendations
  } finally {
    loading.recommend = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadProjectList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.projectType = ''
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

onMounted(() => {
  loadProjectList()
  loadRecommendations()
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

.recommend-section {
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
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.mb-16 {
  margin-bottom: 16px;
}

.recommend-card {
  cursor: pointer;
  transition: transform 0.2s;

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
    margin: 0;
    font-size: 12px;
    color: #909399;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.project-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
  }

  .card-header {
    display: flex;
    gap: 6px;
    margin-bottom: 12px;
  }

  .project-name {
    margin: 0 0 8px 0;
    font-size: 16px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .project-tags {
    display: flex;
    gap: 6px;
    margin-bottom: 10px;
    flex-wrap: wrap;
  }

  .project-desc {
    margin: 0 0 12px 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: #909399;

    .footer-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
