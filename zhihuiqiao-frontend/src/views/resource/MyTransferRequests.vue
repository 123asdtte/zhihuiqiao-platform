<template>
  <div class="my-transfer-requests-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的转让意向</h2>
      <p>查看您发起的资源转让意向及收到的买家意向</p>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 我发出的意向 -->
        <el-tab-pane label="我发出的" name="sent">
          <div v-if="sentList.length === 0" class="empty-state">
            <el-empty description="暂无发出的转让意向" />
          </div>
          <el-table v-else :data="sentList" style="width: 100%">
            <el-table-column prop="resourceName" label="资源名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="sellerName" label="卖家" width="120" />
            <el-table-column prop="message" label="我的留言" min-width="200" show-overflow-tooltip />
            <el-table-column prop="contactInfo" label="联系方式" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleCancel(row.id)"
                >
                  取消
                </el-button>
                <el-button
                  v-if="row.status === 'accepted' && !row.buyerReviewed"
                  type="primary"
                  size="small"
                  @click="openReviewDialog(row, 'seller')"
                >
                  评价卖家
                </el-button>
                <el-tag v-else-if="row.status === 'accepted' && row.buyerReviewed" type="info" size="small">
                  已评价
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我收到的意向 -->
        <el-tab-pane label="我收到的" name="received">
          <div v-if="receivedList.length === 0" class="empty-state">
            <el-empty description="暂无收到的转让意向" />
          </div>
          <el-table v-else :data="receivedList" style="width: 100%">
            <el-table-column prop="resourceName" label="资源名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="buyerName" label="买家" width="120" />
            <el-table-column prop="message" label="买家留言" min-width="200" show-overflow-tooltip />
            <el-table-column prop="contactInfo" label="买家联系方式" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'pending'"
                  type="success"
                  size="small"
                  @click="handleAccept(row.id)"
                >
                  确认成交
                </el-button>
                <el-button
                  v-if="row.status === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleReject(row.id)"
                >
                  拒绝
                </el-button>
                <el-button
                  v-if="row.status === 'accepted' && !row.sellerReviewed"
                  type="primary"
                  size="small"
                  @click="openReviewDialog(row, 'buyer')"
                >
                  评价买家
                </el-button>
                <el-tag v-else-if="row.status === 'accepted' && row.sellerReviewed" type="info" size="small">
                  已评价
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="交易评价" width="450px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分" required>
          <el-rate v-model="reviewForm.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请对交易进行评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReviewSubmit">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSentTransferRequests,
  getReceivedTransferRequests,
  acceptTransferRequest,
  rejectTransferRequest,
  cancelTransferRequest,
  reviewTransferRequest
} from '@/api/resource'

// 当前标签页
const activeTab = ref<'sent' | 'received'>('sent')

// 列表数据
const sentList = ref<any[]>([])
const receivedList = ref<any[]>([])
const loading = ref(false)

// 评价弹窗
const reviewDialogVisible = ref(false)
const reviewForm = reactive({
  requestId: '',
  targetUserId: '',
  rating: 5,
  comment: ''
})

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    pending: '待处理',
    accepted: '已成交',
    rejected: '已拒绝',
    cancelled: '已取消'
  }
  return map[status] || status
}

/**
 * 状态标签类型
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    accepted: 'success',
    rejected: 'danger',
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
 * 加载我发出的意向
 */
async function loadSentList() {
  loading.value = true
  try {
    const res: any = await getSentTransferRequests()
    sentList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载发出的意向失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载我收到的意向
 */
async function loadReceivedList() {
  loading.value = true
  try {
    const res: any = await getReceivedTransferRequests()
    receivedList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载收到的意向失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 标签切换
 */
function handleTabChange(tab: any) {
  if (tab === 'sent') {
    loadSentList()
  } else {
    loadReceivedList()
  }
}

/**
 * 确认成交
 */
async function handleAccept(id: string | number) {
  try {
    await ElMessageBox.confirm('确认与该买家成交吗？成交后资源状态将变为已转让。', '确认成交', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await acceptTransferRequest(id)
    ElMessage.success('已确认成交')
    await loadReceivedList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 拒绝意向
 */
async function handleReject(id: string | number) {
  try {
    await ElMessageBox.confirm('确定要拒绝该意向吗？', '拒绝意向', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await rejectTransferRequest(id)
    ElMessage.success('已拒绝')
    await loadReceivedList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 取消意向
 */
async function handleCancel(id: string | number) {
  try {
    await ElMessageBox.confirm('确定要取消该意向吗？', '取消意向', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelTransferRequest(id)
    ElMessage.success('已取消')
    await loadSentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 打开评价弹窗
 */
function openReviewDialog(row: any, target: 'seller' | 'buyer') {
  reviewForm.requestId = row.id
  reviewForm.targetUserId = target === 'seller' ? row.sellerId : row.buyerId
  reviewForm.rating = 5
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

/**
 * 提交评价
 */
async function handleReviewSubmit() {
  if (!reviewForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  try {
    await reviewTransferRequest(reviewForm.requestId, {
      targetUserId: reviewForm.targetUserId,
      rating: reviewForm.rating,
      comment: reviewForm.comment
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    if (activeTab.value === 'sent') {
      await loadSentList()
    } else {
      await loadReceivedList()
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadSentList()
})
</script>

<style scoped lang="scss">
.my-transfer-requests-page {
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

  .empty-state {
    padding: 40px 0;
  }
}
</style>
