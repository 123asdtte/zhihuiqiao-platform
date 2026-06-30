<template>
  <div class="my-resources-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的资源</h2>
      <p>查看您发布的所有闲置资源，包括待审核、可借用/转让、已租出等状态</p>
    </div>

    <el-card shadow="never">
      <!-- 顶部筛选：按状态筛选 -->
      <div class="filter-bar">
        <el-select
          v-model="query.status"
          placeholder="全部状态"
          clearable
          style="width: 160px"
          @change="handleStatusChange"
        >
          <el-option label="待审核" value="pending_audit" />
          <el-option label="可预约" value="available" />
          <el-option label="已租出" value="rented" />
          <el-option label="已转让" value="transferred" />
          <el-option label="已下架" value="unavailable" />
        </el-select>
        <el-button type="primary" @click="handlePublish">发布新资源</el-button>
      </div>

      <!-- 我的资源列表 -->
      <el-table v-loading="loading" :data="resourceList" style="width: 100%">
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
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
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
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadMyResources"
          @current-change="loadMyResources"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyResources } from '@/api/resource'

const router = useRouter()

// 列表加载状态
const loading = ref(false)
// 资源列表数据
const resourceList = ref<any[]>([])
// 总条数
const total = ref(0)

// 查询参数
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

/**
 * 状态标签类型映射
 */
function getStatusType(status: string): any {
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
 * 状态中文映射
 */
function getStatusText(status: string) {
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

/**
 * 加载我的资源列表
 */
async function loadMyResources() {
  loading.value = true
  try {
    const res: any = await getMyResources({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status
    })
    if (res.code === 200) {
      resourceList.value = res.data?.records || []
      total.value = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载我的资源失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 状态筛选变化
 */
function handleStatusChange() {
  query.pageNum = 1
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

  // 筛选栏
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  // 分页
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
