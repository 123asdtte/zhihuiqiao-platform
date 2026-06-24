<template>
  <div class="project-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>科研项目</h2>
      <p>浏览师生发布的科研项目，寻找感兴趣的科研机会</p>
    </div>

    <!-- 搜索筛选区域 -->
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
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索项目名称/描述"
            clearable
            style="width: 240px"
            @input="handleKeywordInput"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 项目列表 -->
    <el-row v-loading="loading" :gutter="20" class="project-list">
      <el-col v-for="item in projectList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="project-card" shadow="hover" @click="goToDetail(item.id)">
          <div class="project-header">
            <div class="project-status" :class="item.status">
              {{ statusText(item.status) }}
            </div>
            <div class="project-views">
              <el-icon><View /></el-icon>
              {{ item.views }}
            </div>
          </div>
          <h3 class="project-name">{{ item.projectName }}</h3>
          <div class="project-meta">
            <el-tag size="small" type="info">{{ item.projectType }}</el-tag>
            <span class="project-field">{{ item.researchFields || '暂无领域' }}</span>
          </div>
          <p class="project-desc">{{ item.projectDescription }}</p>
          <div class="project-footer">
            <span class="project-members">
              <el-icon><User /></el-icon>
              {{ item.currentMembers || 0 }} / {{ item.maxMembers || 0 }} 人
            </span>
            <span class="project-date">{{ formatDate(item.startDate) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="projectList.length === 0 && !loading" description="暂无相关项目" />

    <!-- 分页 -->
    <div class="pagination-wrapper">
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
import { debounce } from '@/utils/debounce'
import { View, User } from '@element-plus/icons-vue'
import { getProjectList } from '@/api/research'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  projectType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 项目列表
const projectList = ref<any[]>([])
const loading = ref(false)

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
 * 格式化日期
 */
function formatDate(date: string) {
  if (!date) return '暂无开始日期'
  return date
}

/**
 * 加载项目列表数据
 */
async function loadProjectList() {
  loading.value = true
  try {
    const res: any = await getProjectList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      projectType: searchForm.projectType,
      status: searchForm.status
    })
    // 后端返回 data.records 和 data.total
    projectList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载项目列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 关键词输入防抖搜索，减少频繁请求
 */
const handleKeywordInput = debounce(() => {
  handleSearch()
}, 400)

/**
 * 搜索
 */
function handleSearch() {
  pagination.pageNum = 1
  loadProjectList()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  searchForm.keyword = ''
  searchForm.projectType = ''
  searchForm.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadProjectList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadProjectList()
}

/**
 * 跳转项目详情页
 */
function goToDetail(id: number) {
  router.push(`/app/research/projects/${id}`)
}

onMounted(() => {
  loadProjectList()
})
</script>

<style scoped lang="scss">
.project-list-page {
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

  .filter-card {
    margin-bottom: 20px;
  }

  .project-list {
    margin-bottom: 20px;

    .project-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }

      .project-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .project-status {
          padding: 4px 10px;
          border-radius: 12px;
          font-size: 12px;
          color: #fff;
          background-color: #909399;

          &.recruiting {
            background-color: #67c23a;
          }

          &.ongoing {
            background-color: #409eff;
          }

          &.completed {
            background-color: #909399;
          }

          &.closed {
            background-color: #f56c6c;
          }
        }

        .project-views {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #909399;
        }
      }

      .project-name {
        margin: 0 0 10px 0;
        font-size: 16px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .project-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;

        .project-field {
          font-size: 13px;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .project-desc {
        margin: 0 0 12px 0;
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        height: 40px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .project-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 13px;
        color: #909399;

        .project-members {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
