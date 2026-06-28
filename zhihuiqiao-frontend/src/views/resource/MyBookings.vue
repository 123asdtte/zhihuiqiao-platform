<template>
  <div class="my-bookings-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的预约</h2>
      <p>查看您提交的资源预约记录及您收到的资源借用申请</p>
    </div>

    <el-card shadow="never">
      <!-- 视角切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我预约的" name="borrower">
          <booking-table
            :data="borrowerBookings"
            :loading="borrowerLoading"
            view-mode="borrower"
            @refresh="loadBorrowerBookings"
          />
        </el-tab-pane>
        <el-tab-pane label="我出借的" name="owner">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyBookings, getOwnerBookings } from '@/api/resource'
import { useUserStore } from '@/stores/user'
import BookingTable from './components/BookingTable.vue'

const userStore = useUserStore()

// 当前标签页
const activeTab = ref<'borrower' | 'owner'>('borrower')

// 我预约的
const borrowerBookings = ref<any[]>([])
const borrowerLoading = ref(false)

// 我出借的
const ownerBookings = ref<any[]>([])
const ownerLoading = ref(false)

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
 * 标签切换
 */
function handleTabChange(tab: any) {
  if (tab === 'borrower') {
    loadBorrowerBookings()
  } else {
    loadOwnerBookings()
  }
}

onMounted(() => {
  loadBorrowerBookings()
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
}
</style>
