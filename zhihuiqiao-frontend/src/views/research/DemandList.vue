<template>
  <div class="demand-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>企业需求</h2>
        <p>浏览企业发布的技术需求、合作意向，寻找产学研合作机会</p>
      </div>
      <!-- 发布企业需求按钮：仅企业和管理员可见 -->
      <el-button v-if="userStore.isEnterprise || userStore.isAdmin" type="primary" @click="router.push('/app/research/demand/publish')">
        <el-icon><Plus /></el-icon>
        发布企业需求
      </el-button>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="需求类型">
          <el-select v-model="searchForm.demandType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option label="技术攻关" value="技术攻关" />
            <el-option label="成果转化" value="成果转化" />
            <el-option label="人才招聘" value="人才招聘" />
            <el-option label="联合研发" value="联合研发" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="进行中" value="open" />
            <el-option label="已关闭" value="closed" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索需求标题/描述"
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

    <!-- 需求列表 -->
    <el-row v-loading="loading" :gutter="20" class="demand-list">
      <el-col v-for="item in demandList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="demand-card" shadow="hover" @click="goToDetail(item.id)">
          <div class="demand-header">
            <div class="demand-status" :class="item.status">
              {{ statusText(item.status) }}
            </div>
            <div class="demand-views">
              <el-icon><View /></el-icon>
              {{ item.views }}
            </div>
          </div>
          <h3 class="demand-title">{{ item.demandTitle }}</h3>
          <div class="demand-meta">
            <el-tag size="small" type="info">{{ item.demandType }}</el-tag>
            <span class="demand-field">{{ item.industryField || '暂无行业' }}</span>
          </div>
          <p class="demand-desc">{{ item.demandDescription }}</p>
          <div class="demand-footer">
            <span class="demand-budget">
              <el-icon><Money /></el-icon>
              {{ item.budgetRange || '面议' }}
            </span>
            <span class="demand-mode">{{ item.cooperationMode || '合作方式待定' }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="demandList.length === 0 && !loading" description="暂无相关需求" />

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
import { useUserStore } from '@/stores/user'
import { View, Money, Plus } from '@element-plus/icons-vue'
import { getDemandList } from '@/api/research'

const router = useRouter()
const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  demandType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 需求列表
const demandList = ref<any[]>([])
const loading = ref(false)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    open: '进行中',
    closed: '已关闭',
    completed: '已完成'
  }
  return map[status] || status
}

/**
 * 加载需求列表数据
 */
async function loadDemandList() {
  loading.value = true
  try {
    const res: any = await getDemandList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      demandType: searchForm.demandType,
      status: searchForm.status
    })
    // 后端返回 data.records 和 data.total
    demandList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载企业需求列表失败')
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
  loadDemandList()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  searchForm.keyword = ''
  searchForm.demandType = ''
  searchForm.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadDemandList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadDemandList()
}

/**
 * 跳转需求详情页（预留）
 */
function goToDetail(id: number) {
  // 当前仅做提示，如需详情页可后续扩展
  ElMessage.info(`查看需求详情 ID: ${id}`)
}

onMounted(() => {
  loadDemandList()
})
</script>

<style scoped lang="scss">
.demand-list-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

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

  .demand-list {
    margin-bottom: 20px;

    .demand-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }

      .demand-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .demand-status {
          padding: 4px 10px;
          border-radius: 12px;
          font-size: 12px;
          color: #fff;
          background-color: #909399;

          &.open {
            background-color: #67c23a;
          }

          &.completed {
            background-color: #409eff;
          }

          &.closed {
            background-color: #909399;
          }
        }

        .demand-views {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #909399;
        }
      }

      .demand-title {
        margin: 0 0 10px 0;
        font-size: 16px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .demand-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;

        .demand-field {
          font-size: 13px;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .demand-desc {
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

      .demand-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 13px;
        color: #909399;

        .demand-budget {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #f56c6c;
          font-weight: 500;
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
