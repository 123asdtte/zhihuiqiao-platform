<template>
  <div class="resource-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>闲置资源共享</h2>
      <p>浏览校园闲置设备、图书、场地等资源，按需预约借用</p>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.resourceType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option label="实验设备" value="实验设备" />
            <el-option label="图书资料" value="图书资料" />
            <el-option label="办公用品" value="办公用品" />
            <el-option label="电子数码" value="电子数码" />
            <el-option label="场地空间" value="场地空间" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="可借用" value="available" />
            <el-option label="已借出" value="rented" />
            <el-option label="不可用" value="unavailable" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索资源名称/描述"
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

    <!-- 资源列表 -->
    <el-row v-loading="loading" :gutter="20" class="resource-list">
      <el-col v-for="item in resourceList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="resource-card" shadow="hover" @click="goToDetail(item.id)">
          <div class="resource-image">
            <el-image
              :src="item.images ? item.images.split(',')[0] : defaultImage"
              fit="cover"
              lazy
              style="width: 100%; height: 160px"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="resource-status" :class="item.status">
              {{ statusText(item.status) }}
            </div>
          </div>
          <div class="resource-info">
            <h3 class="resource-name">{{ item.resourceName }}</h3>
            <div class="resource-meta">
              <el-tag size="small" type="info">{{ item.resourceType }}</el-tag>
              <span class="resource-location">
                <el-icon><Location /></el-icon>
                {{ item.location || '暂无位置' }}
              </span>
            </div>
            <p class="resource-desc">{{ item.description }}</p>
            <div class="resource-footer">
              <span class="resource-price">
                <span v-if="item.rentalPrice > 0">¥{{ item.rentalPrice }}/天</span>
                <span v-else class="free">免费</span>
              </span>
              <span class="resource-views">
                <el-icon><View /></el-icon>
                {{ item.views }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="resourceList.length === 0 && !loading" description="暂无相关资源" />

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
import { Picture, Location, View } from '@element-plus/icons-vue'
import { getResourceList } from '@/api/resource'

const router = useRouter()

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
    // 后端返回 data.records 和 data.total
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

  .resource-list {
    margin-bottom: 20px;

    .resource-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }

      .resource-image {
        position: relative;
        margin: -20px -20px 16px -20px;

        .image-placeholder {
          width: 100%;
          height: 160px;
          background-color: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #909399;
          font-size: 40px;
        }

        .resource-status {
          position: absolute;
          top: 12px;
          right: 12px;
          padding: 4px 10px;
          border-radius: 12px;
          font-size: 12px;
          color: #fff;
          background-color: #909399;

          &.available {
            background-color: #67c23a;
          }

          &.rented {
            background-color: #f56c6c;
          }

          &.unavailable {
            background-color: #909399;
          }
        }
      }

      .resource-info {
        .resource-name {
          margin: 0 0 10px 0;
          font-size: 16px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .resource-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 10px;

          .resource-location {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #606266;
          }
        }

        .resource-desc {
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

        .resource-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .resource-price {
            font-size: 16px;
            color: #f56c6c;
            font-weight: 500;

            .free {
              color: #67c23a;
            }
          }

          .resource-views {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #909399;
          }
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
