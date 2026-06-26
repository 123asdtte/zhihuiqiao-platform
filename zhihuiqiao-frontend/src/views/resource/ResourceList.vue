<template>
  <div class="resource-list-page zh-page">
    <!-- 页面标题区 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">闲置资源共享</h1>
        <p class="zh-page-subtitle">浏览校园闲置设备、图书、场地等资源，按需预约借用</p>
      </div>
    </div>

    <!-- 搜索筛选：横向排列，新增资源按钮放在搜索框右侧 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-select v-model="searchForm.resourceType" placeholder="全部类型" clearable style="width: 140px" @change="handleSearch">
          <el-option label="实验设备" value="实验设备" />
          <el-option label="图书资料" value="图书资料" />
          <el-option label="办公用品" value="办公用品" />
          <el-option label="电子数码" value="电子数码" />
          <el-option label="场地空间" value="场地空间" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="可借用" value="available" />
          <el-option label="已借出" value="rented" />
          <el-option label="不可用" value="unavailable" />
        </el-select>
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索资源名称/描述"
          clearable
          style="width: 260px"
          @input="handleKeywordInput"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <!-- 发布资源按钮：仅学生、教师、管理员可见，企业账号无发布权限 -->
        <el-button v-if="!userStore.isEnterprise" type="primary" class="publish-btn" @click="router.push('/app/resource/publish')">
          <el-icon><Plus /></el-icon>
          发布资源
        </el-button>
      </div>
      <el-button text @click="handleReset">重置筛选</el-button>
    </div>

    <!-- 资源卡片列表 -->
    <div v-loading="loading" class="resource-grid">
      <div
        class="resource-card"
        v-for="item in resourceList"
        :key="item.id"
        @click="goToDetail(item.id)"
      >
        <div class="resource-image">
          <el-image
            :src="item.images ? item.images.split(',')[0] : defaultImage"
            fit="cover"
            lazy
            style="width: 100%; height: 200px"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="resource-status" :class="item.status">{{ statusText(item.status) }}</div>
        </div>
        <div class="resource-info">
          <div class="resource-meta-top">
            <span class="zh-tag">{{ item.resourceType }}</span>
            <span class="resource-views">
              <el-icon><View /></el-icon>
              {{ item.views }}
            </span>
          </div>
          <h3 class="resource-name">{{ item.resourceName }}</h3>
          <p class="resource-desc">{{ item.description || '暂无描述' }}</p>
          <div class="resource-location">
            <el-icon><Location /></el-icon>
            <span>{{ item.location || '暂无位置' }}</span>
          </div>
          <div class="resource-footer">
            <span class="resource-price">
              <span v-if="item.rentalPrice > 0" class="paid">¥{{ item.rentalPrice }}<small>/天</small></span>
              <span v-else class="free">免费借用</span>
            </span>
            <span class="book-hint">查看详情</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="resourceList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无相关资源" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
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
import { Picture, Location, View, Search, Plus } from '@element-plus/icons-vue'
import { getResourceList } from '@/api/resource'

const router = useRouter()
const userStore = useUserStore()

// 默认占位图片
const defaultImage = 'https://placehold.co/300x160/e4e7ed/909399?text=暂无图片'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  resourceType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 资源列表
const resourceList = ref<any[]>([])
const loading = ref(false)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    available: '可借用',
    rented: '已借出',
    unavailable: '不可用'
  }
  return map[status] || status
}

/**
 * 加载资源列表数据
 */
async function loadResourceList() {
  loading.value = true
  try {
    const res: any = await getResourceList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      resourceType: searchForm.resourceType,
      status: searchForm.status
    })
    resourceList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载资源列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 关键词输入防抖搜索
 */
const handleKeywordInput = debounce(() => {
  handleSearch()
}, 400)

/**
 * 搜索
 */
function handleSearch() {
  pagination.pageNum = 1
  loadResourceList()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  searchForm.keyword = ''
  searchForm.resourceType = ''
  searchForm.status = ''
  handleSearch()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadResourceList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadResourceList()
}

/**
 * 跳转资源详情页
 */
function goToDetail(id: number) {
  router.push(`/app/resource/${id}`)
}

onMounted(() => {
  loadResourceList()
})
</script>

<style scoped lang="scss">
.resource-list-page {
  padding-bottom: var(--zh-space-8);
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--zh-space-5);
  margin-bottom: var(--zh-space-8);
}

.resource-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--zh-shadow);
  transition: all var(--zh-transition);

  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--zh-shadow-md);

    .resource-image img,
    .resource-image .el-image {
      transform: scale(1.05);
    }
  }
}

.resource-image {
  position: relative;
  overflow: hidden;

  .el-image,
  img {
    transition: transform var(--zh-transition-slow);
  }

  .image-placeholder {
    width: 100%;
    height: 200px;
    background: var(--zh-bg-warm);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--zh-text-tertiary);
    font-size: 40px;
  }

  .resource-status {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 5px 12px;
    border-radius: 100px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    background-color: var(--zh-text-tertiary);
    box-shadow: var(--zh-shadow-sm);

    &.available { background-color: var(--zh-success); }
    &.rented { background-color: var(--zh-danger); }
    &.unavailable { background-color: var(--zh-text-tertiary); }
  }
}

.resource-info {
  padding: var(--zh-space-5);

  .resource-meta-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--zh-space-3);

    .resource-views {
      display: flex;
      align-items: center;
      gap: var(--zh-space-1);
      font-size: 12px;
      color: var(--zh-text-tertiary);
    }
  }

  .resource-name {
    font-family: var(--zh-font-display);
    font-size: 17px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-2) 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 48px;
  }

  .resource-desc {
    font-size: 13px;
    color: var(--zh-text-secondary);
    line-height: 1.6;
    margin: 0 0 var(--zh-space-3) 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 42px;
  }

  .resource-location {
    display: flex;
    align-items: center;
    gap: var(--zh-space-1);
    font-size: 12px;
    color: var(--zh-text-tertiary);
    margin-bottom: var(--zh-space-4);

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .resource-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: var(--zh-space-3);
    border-top: 1px solid var(--zh-border-light);

    .resource-price {
      .paid {
        font-family: var(--zh-font-display);
        font-size: 20px;
        font-weight: 700;
        color: var(--zh-danger);

        small {
          font-size: 12px;
          font-weight: 500;
          color: var(--zh-text-tertiary);
        }
      }

      .free {
        font-size: 14px;
        font-weight: 600;
        color: var(--zh-success);
      }
    }

    .book-hint {
      font-size: 12px;
      color: var(--zh-primary);
      font-weight: 500;
    }
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-4) var(--zh-space-5);
  margin-bottom: var(--zh-space-6);
  box-shadow: var(--zh-shadow-sm);

  .filter-group {
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);
    flex-wrap: wrap;

    > * {
      flex-shrink: 0;
    }
  }
}

.empty-state {
  padding: var(--zh-space-16) 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

@media (max-width: 1400px) {
  .resource-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .resource-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }
}
</style>
