<template>
  <div class="my-bookings-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的预约</h2>
      <p>查看您提交的所有资源预约记录及审批状态</p>
    </div>

    <!-- 预约记录表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="bookingList" style="width: 100%" stripe>
        <el-table-column prop="resourceName" label="资源名称" min-width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="goToResource(row.resourceId)">
              {{ row.resourceName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="220">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}<br />
            至 {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="预约状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replyMessage" label="审批回复" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.replyMessage || '暂无回复' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'approved' || row.status === 'ongoing'"
              type="primary"
              size="small"
              @click="handleReturn(row.id)"
            >
              归还
            </el-button>
            <el-button
              v-else-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
            <span v-else class="no-action">--</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && bookingList.length === 0" description="暂无预约记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, returnResource, cancelBooking } from '@/api/resource'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 预约列表
const bookingList = ref<any[]>([])
const loading = ref(false)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝',
    ongoing: '使用中',
    returned: '已归还',
    cancelled: '已取消'
  }
  return map[status] || status
}

/**
 * 状态标签类型映射
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    ongoing: 'primary',
    returned: 'info',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTime: string) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

/**
 * 加载我的预约列表
 */
async function loadMyBookings() {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }
  loading.value = true
  try {
    const res: any = await getMyBookings(userStore.userInfo.id)
    bookingList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载预约记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转资源详情页
 */
function goToResource(resourceId: number) {
  router.push(`/app/resource/${resourceId}`)
}

/**
 * 归还资源
 */
async function handleReturn(id: number) {
  try {
    await ElMessageBox.confirm('确定该资源已归还吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await returnResource(id)
    if (res.code === 200) {
      ElMessage.success('归还成功')
      loadMyBookings()
    } else {
      ElMessage.error(res.message || '归还失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 取消预约申请
 */
async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该预约申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await cancelBooking(id)
    if (res.code === 200) {
      ElMessage.success('预约已取消')
      loadMyBookings()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadMyBookings()
})
</script>

<style scoped lang="scss">
.my-bookings-page {
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

  .no-action {
    color: #909399;
    font-size: 13px;
  }
}
</style>
