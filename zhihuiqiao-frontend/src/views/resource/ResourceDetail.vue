<template>
  <div class="resource-detail-page">
    <!-- 返回按钮 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回资源列表
      </el-button>
    </div>

    <div v-if="resource" class="detail-content">
      <el-row :gutter="24">
        <!-- 左侧：资源图片与基本信息 -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="never">
            <div class="resource-gallery">
              <el-image
                :src="currentImage || defaultImage"
                fit="cover"
                style="width: 100%; height: 360px; border-radius: 8px"
                :preview-src-list="imageList"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-if="imageList.length > 1" class="image-thumbnails">
                <img
                  v-for="(img, index) in imageList"
                  :key="index"
                  :src="img"
                  :class="{ active: currentImage === img }"
                  @click="currentImage = img"
                />
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：资源信息与预约 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="info-card">
            <h2 class="resource-name">{{ resource.resourceName }}</h2>
            <div class="resource-meta">
              <el-tag type="info">{{ resource.resourceType }}</el-tag>
              <el-tag :type="statusTagType(resource.status)">{{ statusText(resource.status) }}</el-tag>
            </div>

            <div class="info-section">
              <div class="info-item">
                <span class="label">存放位置：</span>
                <span class="value">{{ resource.location || '暂无' }}</span>
              </div>
              <div class="info-item">
                <span class="label">租赁价格：</span>
                <span class="value price">
                  <span v-if="resource.rentalPrice > 0">¥{{ resource.rentalPrice }}/天</span>
                  <span v-else class="free">免费</span>
                </span>
              </div>
              <div class="info-item">
                <span class="label">原价：</span>
                <span class="value">¥{{ resource.originalPrice || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="label">浏览量：</span>
                <span class="value">{{ resource.views }}</span>
              </div>
            </div>

            <div class="info-section">
              <h4>资源描述</h4>
              <p class="description">{{ resource.description }}</p>
            </div>

            <div class="info-section">
              <h4>借用规则</h4>
              <p class="rules">{{ resource.borrowRules || '暂无借用规则' }}</p>
            </div>

            <div v-if="!userStore.isEnterprise" class="action-buttons">
              <el-button
                type="primary"
                size="large"
                :disabled="resource.status !== 'available'"
                @click="openBookingDialog"
              >
                立即预约
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 预约记录（仅资源所有者可见审批入口） -->
      <el-card v-if="isOwner || bookingList.length > 0" class="booking-card" shadow="never">
        <template #header>
          <span>预约记录</span>
        </template>
        <el-table :data="bookingList" style="width: 100%">
          <el-table-column prop="borrowerId" label="借用人ID" width="100" />
          <el-table-column label="预约时间" min-width="200">
            <template #default="{ row }">
              {{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="purpose" label="用途说明" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="bookingStatusType(row.status)">{{ bookingStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="isOwner" label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'pending'"
                type="success"
                size="small"
                @click="handleAudit(row.id, 'approved')"
              >
                通过
              </el-button>
              <el-button
                v-if="row.status === 'pending'"
                type="danger"
                size="small"
                @click="handleAudit(row.id, 'rejected')"
              >
                拒绝
              </el-button>
              <el-button
                v-if="row.status === 'approved' || row.status === 'ongoing'"
                type="primary"
                size="small"
                @click="handleReturn(row.id)"
              >
                申请归还
              </el-button>
              <el-button
                v-if="row.status === 'return_request' && isOwner"
                type="primary"
                size="small"
                @click="handleConfirmReturn(row.id)"
              >
                确认归还
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 资源日历入口 -->
      <el-card class="calendar-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>资源可预约日历</span>
            <el-button type="primary" size="small" @click="openCalendarDialog">查看日历</el-button>
          </div>
        </template>
        <p class="calendar-hint">绿色表示可预约，红色表示已被占用。点击右上角按钮查看完整日历。</p>
      </el-card>
    </div>

    <el-empty v-else description="资源不存在或已删除" />

    <!-- 资源日历弹窗 -->
    <el-dialog v-model="calendarDialogVisible" title="资源可预约日历" width="700px">
      <el-calendar v-model="calendarMonth" @input="handleCalendarMonthChange">
        <template #date-cell="{ data }">
          <div class="calendar-cell" :class="{ available: isDateAvailable(data.date), unavailable: !isDateAvailable(data.date) }">
            <span>{{ data.day.split('-').pop() }}</span>
            <span v-if="isDateAvailable(data.date)" class="status-text">可约</span>
            <span v-else class="status-text">已约</span>
          </div>
        </template>
      </el-calendar>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog v-model="bookingDialogVisible" title="资源预约" width="500px">
      <el-form :model="bookingForm" label-width="100px">
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="bookingForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="bookingForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="用途说明" required>
          <el-input
            v-model="bookingForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请说明借用用途"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBookingSubmit">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import {
  getResourceDetail,
  getResourceBookings,
  submitBooking,
  auditBooking,
  requestReturn,
  confirmReturn,
  getResourceCalendar
} from '@/api/resource'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 默认占位图片
const defaultImage = 'https://placehold.co/800x400/e4e7ed/909399?text=暂无图片'

// 资源详情
const resource = ref<any>(null)
const bookingList = ref<any[]>([])
const loading = ref(false)

// 图片列表
const imageList = computed(() => {
  if (resource.value?.images) {
    return resource.value.images.split(',').filter((url: string) => url.trim())
  }
  return []
})
const currentImage = ref('')

// 是否为资源所有者
const isOwner = computed(() => {
  return userStore.userInfo?.id && resource.value?.ownerId === userStore.userInfo.id
})

// 资源日历
const calendarDialogVisible = ref(false)
const calendarDates = ref<any[]>([])
const calendarMonth = ref(new Date())

// 预约弹窗
const bookingDialogVisible = ref(false)
const bookingForm = reactive({
  resourceId: 0,
  borrowerId: 0,
  startTime: '',
  endTime: '',
  purpose: ''
})

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
 * 状态标签类型
 */
function statusTagType(status: string) {
  const map: Record<string, any> = {
    available: 'success',
    rented: 'danger',
    unavailable: 'info'
  }
  return map[status] || 'info'
}

/**
 * 预约状态文本映射
 */
function bookingStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝',
    ongoing: '使用中',
    return_request: '归还待确认',
    return_confirmed: '已归还',
    cancelled: '已取消'
  }
  return map[status] || status
}

/**
 * 预约状态标签类型
 */
function bookingStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    ongoing: 'primary',
    return_request: 'warning',
    return_confirmed: 'info',
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
 * 加载资源详情
 */
async function loadResourceDetail() {
  const id = route.params.id as string
  loading.value = true
  try {
    const res: any = await getResourceDetail(id)
    resource.value = res.data
    if (imageList.value.length > 0) {
      currentImage.value = imageList.value[0]
    }
    await loadBookings()
  } catch (error) {
    ElMessage.error('加载资源详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载预约记录
 */
async function loadBookings() {
  if (!resource.value) return
  try {
    const res: any = await getResourceBookings(resource.value.id)
    bookingList.value = res.data || []
  } catch (error) {
    console.error('加载预约记录失败', error)
  }
}

/**
 * 加载资源预约日历
 */
async function loadCalendar() {
  if (!resource.value) return
  const year = calendarMonth.value.getFullYear()
  const month = calendarMonth.value.getMonth()
  const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`
  const lastDay = new Date(year, month + 1, 0).getDate()
  const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${lastDay}`

  try {
    const res: any = await getResourceCalendar(resource.value.id, startDate, endDate)
    calendarDates.value = res.data || []
  } catch (error) {
    console.error('加载资源日历失败', error)
  }
}

/**
 * 打开日历弹窗
 */
function openCalendarDialog() {
  calendarMonth.value = new Date()
  loadCalendar()
  calendarDialogVisible.value = true
}

/**
 * 切换日历月份
 */
function handleCalendarMonthChange(date: Date) {
  calendarMonth.value = date
  loadCalendar()
}

/**
 * 判断某日是否可预约
 */
function isDateAvailable(date: Date) {
  const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  const item = calendarDates.value.find((d: any) => d.date === dateStr)
  return item ? item.available : true
}

/**
 * 打开预约弹窗
 */
function openBookingDialog() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!userStore.userInfo?.id) {
    ElMessage.warning('用户信息不完整，请重新登录')
    return
  }
  bookingForm.resourceId = resource.value.id
  bookingForm.borrowerId = userStore.userInfo.id
  bookingForm.startTime = ''
  bookingForm.endTime = ''
  bookingForm.purpose = ''
  bookingDialogVisible.value = true
}

/**
 * 提交预约申请
 */
async function handleBookingSubmit() {
  if (!bookingForm.startTime || !bookingForm.endTime) {
    ElMessage.warning('请选择预约时间')
    return
  }
  if (!bookingForm.purpose.trim()) {
    ElMessage.warning('请填写用途说明')
    return
  }

  try {
    await submitBooking(bookingForm)
    ElMessage.success('预约申请已提交，等待审批')
    bookingDialogVisible.value = false
    await loadBookings()
  } catch (error) {
    console.error(error)
  }
}

/**
 * 审批预约
 */
async function handleAudit(id: number, status: string) {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 'approved' ? '通过' : '拒绝'}该预约申请吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await auditBooking(id, status, status === 'approved' ? '审批通过' : '审批拒绝')
    ElMessage.success('操作成功')
    await loadBookings()
    await loadResourceDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 借用方申请归还资源
 */
async function handleReturn(id: number) {
  try {
    await ElMessageBox.confirm('确定要申请归还该资源吗？', '申请归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await requestReturn(id)
    ElMessage.success('已申请归还，等待所有者确认')
    await loadBookings()
    await loadResourceDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 所有者确认归还资源
 */
async function handleConfirmReturn(id: number) {
  try {
    await ElMessageBox.confirm('确定已收到归还的资源且状态正常吗？', '确认归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await confirmReturn(id)
    ElMessage.success('归还确认成功')
    await loadBookings()
    await loadResourceDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 返回列表页
 */
function goBack() {
  router.push('/app/resource/list')
}

onMounted(() => {
  loadResourceDetail()
})
</script>

<style scoped lang="scss">
.resource-detail-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
  }

  .detail-content {
    .resource-gallery {
      .image-placeholder {
        width: 100%;
        height: 360px;
        background-color: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #909399;
        font-size: 60px;
        border-radius: 8px;
      }

      .image-thumbnails {
        display: flex;
        gap: 10px;
        margin-top: 12px;
        overflow-x: auto;

        img {
          width: 80px;
          height: 60px;
          object-fit: cover;
          border-radius: 4px;
          cursor: pointer;
          border: 2px solid transparent;

          &.active {
            border-color: #409eff;
          }
        }
      }
    }

    .info-card {
      .resource-name {
        margin: 0 0 16px 0;
        font-size: 22px;
        color: #303133;
      }

      .resource-meta {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;
      }

      .info-section {
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #ebeef5;

        &:last-of-type {
          border-bottom: none;
        }

        h4 {
          margin: 0 0 10px 0;
          color: #303133;
        }

        .info-item {
          display: flex;
          margin-bottom: 10px;
          font-size: 14px;

          .label {
            color: #909399;
            width: 80px;
            flex-shrink: 0;
          }

          .value {
            color: #606266;

            &.price {
              color: #f56c6c;
              font-size: 18px;
              font-weight: 500;

              .free {
                color: #67c23a;
              }
            }
          }
        }

        .description,
        .rules {
          margin: 0;
          color: #606266;
          line-height: 1.6;
          font-size: 14px;
        }
      }

      .action-buttons {
        margin-top: 20px;

        .el-button {
          width: 100%;
        }
      }
    }

    .booking-card {
      margin-top: 24px;
    }

    .calendar-card {
      margin-top: 24px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .calendar-hint {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

.calendar-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 12px;

  .status-text {
    font-size: 10px;
    margin-top: 2px;
  }

  &.available {
    color: #67c23a;
    background-color: #f0f9eb;
  }

  &.unavailable {
    color: #f56c6c;
    background-color: #fef0f0;
  }
}
</style>
