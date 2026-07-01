<template>
  <div class="my-resources-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的资源中心</h2>
      <p>管理您发布的闲置资源，查看预约记录和借用申请</p>
    </div>

    <el-card shadow="never">
      <!-- 视角切换标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 我的资源 -->
        <el-tab-pane label="我的资源" name="resources">
          <template #label>
            <span class="tab-label">
              <el-icon><Box /></el-icon>
              我的资源
            </span>
          </template>
          
          <!-- 顶部筛选：按状态筛选 -->
          <div class="filter-bar">
            <el-select
              v-model="resourceQuery.status"
              placeholder="全部状态"
              clearable
              style="width: 160px"
              @change="handleResourceStatusChange"
            >
              <el-option label="待审核" value="pending_audit" />
              <el-option label="可预约" value="available" />
              <el-option label="已租出" value="rented" />
              <el-option label="已转让" value="transferred" />
              <el-option label="已下架" value="unavailable" />
            </el-select>
            <el-button type="primary" @click="handlePublish">
              <el-icon><Plus /></el-icon>
              发布新资源
            </el-button>
          </div>

          <!-- 我的资源列表 -->
          <el-table v-loading="resourceLoading" :data="resourceList" style="width: 100%">
            <el-table-column prop="resourceName" label="资源名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column label="交易模式" width="100">
              <template #default="{ row }">
                <el-tag :type="row.tradeMode === 'transfer' ? 'warning' : 'primary'" size="small">
                  {{ row.tradeMode === 'transfer' ? '转让' : '借用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getResourceStatusType(row.status)" size="small">
                  {{ getResourceStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="120">
              <template #default="{ row }">
                <span v-if="row.tradeMode === 'transfer'">
                  {{ row.expectPrice === 0 ? '免费' : `¥${row.expectPrice}` }}
                </span>
                <span v-else>
                  {{ row.rentalPrice === 0 ? '免费' : `¥${row.rentalPrice}/天` }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="存放位置" min-width="140" show-overflow-tooltip />
            <el-table-column prop="createTime" label="发布时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="goDetail(row.id)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="resourceQuery.pageNum"
              v-model:page-size="resourceQuery.pageSize"
              :total="resourceTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadMyResources"
              @current-change="loadMyResources"
            />
          </div>
        </el-tab-pane>

        <!-- 我预约的 -->
        <el-tab-pane label="我预约的" name="borrower">
          <template #label>
            <span class="tab-label">
              <el-icon><Calendar /></el-icon>
              我预约的
              <el-badge v-if="borrowerBookings.length > 0" :value="borrowerBookings.length" :max="99" class="tab-badge" />
            </span>
          </template>
          
          <booking-table
            :data="borrowerBookings"
            :loading="borrowerLoading"
            view-mode="borrower"
            @refresh="loadBorrowerBookings"
          />
        </el-tab-pane>

        <!-- 我出借的 -->
        <el-tab-pane label="我出借的" name="owner">
          <template #label>
            <span class="tab-label">
              <el-icon><Share /></el-icon>
              我出借的
              <el-badge v-if="ownerBookings.length > 0" :value="ownerBookings.length" :max="99" class="tab-badge" />
            </span>
          </template>
          
          <booking-table
            :data="ownerBookings"
            :loading="ownerLoading"
            view-mode="owner"
            @refresh="loadOwnerBookings"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box, Plus, Calendar, Share } from '@element-plus/icons-vue'
import { getMyResources, getMyBookings, getOwnerBookings } from '@/api/resource'
import { useUserStore } from '@/stores/user'
import BookingTable from './components/BookingTable.vue'

const router = useRouter()
const userStore = useUserStore()

// 当前标签页
const activeTab = ref<'resources' | 'borrower' | 'owner'>('resources')

// ==================== 我的资源 ====================
const resourceLoading = ref(false)
const resourceList = ref<any[]>([])
const resourceTotal = ref(0)

const resourceQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

// ==================== 我预约的 ====================
const borrowerBookings = ref<any[]>([])
const borrowerLoading = ref(false)

// ==================== 我出借的 ====================
const ownerBookings = ref<any[]>([])
const ownerLoading = ref(false)

/**
 * 资源状态标签类型映射
 */
function getResourceStatusType(status: string): any {
  const map: Record<string, string> = {
    pending_audit: 'info',
    available: 'success',
    rented: 'warning',
    transferred: '',
    unavailable: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 资源状态中文映射
 */
function getResourceStatusText(status: string) {
  const map: Record<string, string> = {
    pending_audit: '待审核',
    available: '可预约',
    rented: '已租出',
    transferred: '已转让',
    unavailable: '已下架'
  }
  return map[status] || status
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTime: string) {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const pad = (n: number) => (n < 10 ? `0${n}` : n)
  return `${date.getFullYear()}/${pad(date.getMonth() + 1)}/${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// ==================== 数据加载 ====================

/**
 * 加载我的资源列表
 */
async function loadMyResources() {
  resourceLoading.value = true
  try {
    const res: any = await getMyResources({
      pageNum: resourceQuery.pageNum,
      pageSize: resourceQuery.pageSize,
      status: resourceQuery.status
    })
    if (res.code === 200) {
      resourceList.value = res.data?.records || []
      resourceTotal.value = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载我的资源失败')
    console.error(error)
  } finally {
    resourceLoading.value = false
  }
}

/**
 * 加载我预约的列表
 */
async function loadBorrowerBookings() {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }
  borrowerLoading.value = true
  try {
    const res: any = await getMyBookings(userStore.userInfo.id)
    borrowerBookings.value = res.data || []
  } catch (error) {
    ElMessage.error('加载预约记录失败')
    console.error(error)
  } finally {
    borrowerLoading.value = false
  }
}

/**
 * 加载我出借的列表
 */
async function loadOwnerBookings() {
  ownerLoading.value = true
  try {
    const res: any = await getOwnerBookings()
    ownerBookings.value = res.data || []
  } catch (error) {
    ElMessage.error('加载出借记录失败')
    console.error(error)
  } finally {
    ownerLoading.value = false
  }
}

/**
 * 加载当前标签页的数据
 */
function loadTabData() {
  switch (activeTab.value) {
    case 'resources':
      loadMyResources()
      break
    case 'borrower':
      loadBorrowerBookings()
      break
    case 'owner':
      loadOwnerBookings()
      break
  }
}

// ==================== 事件处理 ====================

/**
 * 标签页切换
 */
function handleTabChange(tab: any) {
  loadTabData()
}

/**
 * 资源状态筛选变化
 */
function handleResourceStatusChange() {
  resourceQuery.pageNum = 1
  loadMyResources()
}

/**
 * 跳转发布资源页
 */
function handlePublish() {
  router.push('/app/resource/publish')
}

/**
 * 跳转资源详情页
 */
function goDetail(id: number | string) {
  router.push(`/app/resource/${id}`)
}

onMounted(() => {
  loadMyResources()
})
</script>

<style scoped lang="scss">
.my-resources-page {
  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      font-family: var(--zh-font-display);
      font-size: 24px;
      font-weight: 700;
      color: var(--zh-primary);
    }

    p {
      margin: 0;
      color: var(--zh-text-secondary);
      font-size: 14px;
    }
  }

  // 标签页样式
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }

  :deep(.el-tabs__item) {
    height: 48px;
    line-height: 48px;
    font-size: 14px;
    font-weight: 500;
  }

  .tab-label {
    display: inline-flex;
    align-items: center;
    gap: 6px;

    .el-icon {
      font-size: 16px;
    }

    .tab-badge {
      margin-left: 4px;
    }
  }

  // 筛选栏
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: var(--zh-bg-soft);
    border-radius: var(--zh-radius);
  }

  // 分页
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--zh-border-light);
  }
}
</style>
