<template>
  <div>
    <el-table v-loading="loading" :data="data" style="width: 100%" stripe>
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
      <el-table-column prop="purpose" label="用途说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="预约状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="超期状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.overdueStatus === 'overdue'" type="danger" size="small">已超期</el-tag>
          <el-tag v-else-if="row.overdueStatus === 'resolved'" type="success" size="small">已处理</el-tag>
          <span v-else class="no-action">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="replyMessage" label="审批回复" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.replyMessage || '暂无回复' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <!-- 借用方操作 -->
          <template v-if="viewMode === 'borrower'">
            <el-button
              v-if="row.status === 'approved' || row.status === 'ongoing'"
              type="primary"
              size="small"
              @click="handleRequestReturn(row.id)"
            >
              申请归还
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

          <!-- 所有者操作 -->
          <template v-if="viewMode === 'owner'">
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
              驳回
            </el-button>
            <el-button
              v-if="row.status === 'return_request'"
              type="primary"
              size="small"
              @click="handleConfirmReturn(row.id)"
            >
              确认归还
            </el-button>
            <el-button
              v-if="['ongoing', 'return_request', 'return_confirmed'].includes(row.status)"
              type="warning"
              size="small"
              @click="openDamageDialog(row)"
            >
              损坏上报
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && data.length === 0" description="暂无记录" />

    <!-- 损坏赔偿上报弹窗 -->
    <el-dialog v-model="damageDialogVisible" title="上报资源损坏赔偿" width="500px">
      <el-form :model="damageForm" label-position="top">
        <el-form-item label="损坏描述">
          <el-input
            v-model="damageForm.damageDescription"
            type="textarea"
            :rows="3"
            placeholder="请描述资源损坏情况"
          />
        </el-form-item>
        <el-form-item label="损坏照片URL（多个以逗号分隔）">
          <el-input
            v-model="damageForm.damageImages"
            placeholder="例如：https://example.com/img1.jpg,https://example.com/img2.jpg"
          />
        </el-form-item>
        <el-form-item label="赔偿金额（元）">
          <el-input-number v-model="damageForm.compensationAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="damageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReportDamage">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  requestReturn,
  confirmReturn,
  cancelBooking,
  auditBooking,
  reportDamage
} from '@/api/resource'

const props = defineProps<{
  data: any[]
  loading: boolean
  viewMode: 'borrower' | 'owner'
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const router = useRouter()

// 损坏赔偿弹窗
const damageDialogVisible = ref(false)
const submitting = ref(false)
const damageForm = ref({
  damageDescription: '',
  damageImages: '',
  compensationAmount: 0,
  currentBookingId: null as number | string | null
})

/**
 * 状态文本映射
 */
function statusText(status: string) {
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
 * 状态标签类型映射
 */
function statusType(status: string) {
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
 * 跳转资源详情页
 */
function goToResource(resourceId: number) {
  router.push(`/app/resource/${resourceId}`)
}

/**
 * 借用方申请归还
 */
async function handleRequestReturn(id: number) {
  try {
    await ElMessageBox.confirm('确定要申请归还该资源吗？', '申请归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await requestReturn(id)
    if (res.code === 200) {
      ElMessage.success('已申请归还，等待所有者确认')
      emit('refresh')
    } else {
      ElMessage.error(res.message || '申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 所有者确认归还
 */
async function handleConfirmReturn(id: number) {
  try {
    await ElMessageBox.confirm('确定已收到归还的资源且状态正常吗？', '确认归还', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await confirmReturn(id)
    if (res.code === 200) {
      ElMessage.success('归还确认成功')
      emit('refresh')
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 借用方取消预约
 */
async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该预约申请吗？', '取消预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await cancelBooking(id)
    if (res.code === 200) {
      ElMessage.success('预约已取消')
      emit('refresh')
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 所有者审批预约
 */
async function handleAudit(id: number, status: string) {
  try {
    const title = status === 'approved' ? '通过预约' : '驳回预约'
    const confirmText = status === 'approved' ? '确定要通过该预约吗？' : '确定要驳回该预约吗？'
    await ElMessageBox.confirm(confirmText, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 'approved' ? 'success' : 'warning'
    })
    const res: any = await auditBooking(id, status)
    if (res.code === 200) {
      ElMessage.success(status === 'approved' ? '已通过' : '已驳回')
      emit('refresh')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

/**
 * 打开损坏赔偿上报弹窗
 */
function openDamageDialog(row: any) {
  damageForm.value = {
    damageDescription: '',
    damageImages: '',
    compensationAmount: 0,
    currentBookingId: row.id
  }
  damageDialogVisible.value = true
}

/**
 * 上报损坏赔偿
 */
async function handleReportDamage() {
  if (!damageForm.value.damageDescription) {
    ElMessage.warning('请输入损坏描述')
    return
  }
  if (!damageForm.value.currentBookingId) {
    return
  }

  submitting.value = true
  try {
    const res: any = await reportDamage(damageForm.value.currentBookingId, {
      damageDescription: damageForm.value.damageDescription,
      damageImages: damageForm.value.damageImages,
      compensationAmount: damageForm.value.compensationAmount
    })
    if (res.code === 200) {
      ElMessage.success('损坏赔偿上报成功')
      damageDialogVisible.value = false
      emit('refresh')
    } else {
      ElMessage.error(res.message || '上报失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.no-action {
  color: #909399;
  font-size: 13px;
}
</style>
