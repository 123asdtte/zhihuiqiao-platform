<template>
  <div class="demand-detail-page zh-page">
    <!-- 返回导航 -->
    <div class="back-nav">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回需求列表
      </el-button>
    </div>

    <div v-if="demand" class="detail-layout">
      <!-- 左侧主要内容 -->
      <div class="detail-main">
        <div class="detail-hero">
          <div class="detail-header">
            <div class="header-tags">
              <span class="demand-status" :class="demand.status">{{ statusText(demand.status) }}</span>
              <span class="zh-tag">{{ demand.demandType }}</span>
            </div>
            <h1 class="detail-title">{{ demand.demandTitle }}</h1>
            <div class="detail-meta">
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ demand.views }} 次浏览
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                发布于 {{ demand.createTime || '未知' }}
              </span>
            </div>
          </div>
        </div>

        <div class="detail-sections">
          <!-- 需求描述 -->
          <div class="detail-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              需求描述
            </h3>
            <div class="section-content">{{ demand.demandDescription || '暂无需求描述' }}</div>
          </div>

          <!-- 技术要求 -->
          <div class="detail-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              技术要求
            </h3>
            <div class="section-content">{{ demand.technicalRequirements || '暂无技术要求' }}</div>
          </div>

          <!-- 撮合流程：仅登录用户可见 -->
          <div class="detail-section matching-section">
            <h3 class="section-title">
              <span class="section-marker"></span>
              需求撮合
            </h3>

            <!-- 步骤条 -->
            <el-steps :active="matchingStep" finish-status="success" simple class="matching-steps">
              <el-step title="提交方案" />
              <el-step title="方案审核" />
              <el-step title="签订合同" />
              <el-step title="阶段交付" />
              <el-step title="验收完成" />
            </el-steps>

            <!-- 学生/教师：提交承接方案 -->
            <div v-if="canBid" class="bid-action">
              <el-button type="primary" @click="openBidDialog">提交承接方案</el-button>
            </div>

            <!-- 企业：查看承接方案并审核 -->
            <div v-if="userStore.isEnterprise || userStore.isAdmin" class="bid-list">
              <h4 class="sub-title">承接方案</h4>
              <el-empty v-if="bidList.length === 0" description="暂无承接方案" />
              <el-card v-for="bid in bidList" :key="bid.id" class="bid-card" shadow="never">
                <div class="bid-header">
                  <div>
                    <span class="bidder-name">{{ bid.bidderName || '未知用户' }}</span>
                    <el-tag size="small" :type="bidStatusType(bid.status)">{{ bidStatusText(bid.status) }}</el-tag>
                  </div>
                  <span class="bid-time">{{ bid.createTime }}</span>
                </div>
                <div class="bid-content">
                  <p><strong>承接方案：</strong>{{ bid.bidContent }}</p>
                  <p><strong>预计周期：</strong>{{ bid.expectedDuration || '未填写' }}</p>
                  <p><strong>预计报价：</strong>{{ bid.expectedBudget || '未填写' }}</p>
                </div>
                <div v-if="demand.status === 'open' && bid.status === 'pending'" class="bid-actions">
                  <el-button type="primary" size="small" @click="handleApproveBid(bid.id)">通过</el-button>
                  <el-button size="small" @click="handleRejectBid(bid.id)">驳回</el-button>
                </div>
              </el-card>
            </div>

            <!-- 合同信息 -->
            <div v-if="contract" class="contract-section">
              <h4 class="sub-title">合作合同</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="合同标题">{{ contract.contractTitle }}</el-descriptions-item>
                <el-descriptions-item label="合同金额">{{ contract.amount || '面议' }}</el-descriptions-item>
                <el-descriptions-item label="甲方">{{ contract.partyAName || '企业' }}</el-descriptions-item>
                <el-descriptions-item label="乙方">{{ contract.partyBName || '承接方' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="contractStatusType(contract.status)">{{ contractStatusText(contract.status) }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <div v-if="contract.status === 'unsigned'" class="contract-actions">
                <el-button type="primary" @click="handleSignContract">签订合同</el-button>
              </div>

              <!-- 里程碑 -->
              <div class="milestone-section">
                <div class="milestone-header">
                  <h4 class="sub-title">交付里程碑</h4>
                  <el-button v-if="canManageMilestone" type="primary" size="small" @click="openMilestoneDialog">新增里程碑</el-button>
                </div>
                <el-empty v-if="milestoneList.length === 0" description="暂无里程碑" />
                <el-timeline v-else>
                  <el-timeline-item v-for="m in milestoneList" :key="m.id" :type="milestoneItemType(m.status)" :hollow="m.status !== 'approved'">
                    <div class="milestone-card">
                      <div class="milestone-title">
                        <span>{{ m.milestoneName }}</span>
                        <el-tag size="small" :type="milestoneTagType(m.status)">{{ milestoneStatusText(m.status) }}</el-tag>
                      </div>
                      <p class="milestone-desc">{{ m.description || '暂无描述' }}</p>
                      <p class="milestone-meta">计划截止：{{ m.dueDate || '未设置' }}</p>
                      <p v-if="m.deliverableUrl" class="milestone-meta">交付物：<a :href="m.deliverableUrl" target="_blank">查看</a></p>
                      <div class="milestone-actions">
                        <el-button v-if="canSubmitMilestone(m)" type="primary" size="small" @click="handleSubmitMilestone(m.id)">提交交付物</el-button>
                        <el-button v-if="canApproveMilestone(m)" type="success" size="small" @click="handleApproveMilestone(m.id)">验收通过</el-button>
                        <el-button v-if="canApproveMilestone(m)" size="small" @click="handleRejectMilestone(m.id)">驳回</el-button>
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息栏 -->
      <aside class="detail-sidebar">
        <div class="sidebar-card info-card">
          <h4 class="sidebar-title">需求信息</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">行业领域</span>
              <span class="info-value">{{ demand.industryField || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预算范围</span>
              <span class="info-value">{{ demand.budgetRange || '面议' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">合作方式</span>
              <span class="info-value">{{ demand.cooperationMode || '待定' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系方式</span>
              <span class="info-value">{{ demand.contactInfo || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布企业</span>
              <span class="info-value">{{ demand.publisherName || '未知' }}</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 加载占位 -->
    <el-skeleton v-else :rows="10" animated />

    <!-- 提交承接方案弹窗 -->
    <el-dialog v-model="bidDialogVisible" title="提交承接方案" width="600px">
      <el-form :model="bidForm" label-position="top">
        <el-form-item label="承接方案">
          <el-input v-model="bidForm.bidContent" type="textarea" :rows="4" placeholder="请描述你的承接方案" />
        </el-form-item>
        <el-form-item label="预计周期">
          <el-input v-model="bidForm.expectedDuration" placeholder="例如：3个月" />
        </el-form-item>
        <el-form-item label="预计报价">
          <el-input v-model="bidForm.expectedBudget" placeholder="例如：50000元" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bidDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitBid">提交</el-button>
      </template>
    </el-dialog>

    <!-- 创建里程碑弹窗 -->
    <el-dialog v-model="milestoneDialogVisible" title="新增里程碑" width="600px">
      <el-form :model="milestoneForm" label-position="top">
        <el-form-item label="里程碑名称">
          <el-input v-model="milestoneForm.milestoneName" placeholder="例如：需求分析完成" />
        </el-form-item>
        <el-form-item label="交付内容">
          <el-input v-model="milestoneForm.description" type="textarea" :rows="3" placeholder="请描述该里程碑需要交付的内容" />
        </el-form-item>
        <el-form-item label="计划截止日期">
          <el-date-picker v-model="milestoneForm.dueDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="milestoneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreateMilestone">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Calendar, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getDemandDetail,
  submitDemandBid,
  listDemandBids,
  approveDemandBid,
  rejectDemandBid,
  createContract,
  getContractByDemand,
  signContract,
  createMilestone,
  listMilestones,
  submitDeliverable,
  approveDeliverable,
  rejectDeliverable
} from '@/api/research'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前需求ID
const demandId = route.params.id as string
// 需求详情数据
const demand = ref<any>(null)
// 页面加载状态
const loading = ref(false)

// 承接方案
const bidList = ref<any[]>([])
const bidDialogVisible = ref(false)
const bidForm = reactive({
  bidContent: '',
  expectedDuration: '',
  expectedBudget: ''
})

// 合同
const contract = ref<any>(null)

// 里程碑
const milestoneList = ref<any[]>([])
const milestoneDialogVisible = ref(false)
const milestoneForm = reactive({
  milestoneName: '',
  description: '',
  dueDate: ''
})

const submitting = ref(false)

// 能否提交承接方案：学生/教师且需求进行中
const canBid = computed(() => {
  return (userStore.isStudent || userStore.isTeacher) && demand.value?.status === 'open'
})

// 能否管理里程碑：企业方且合同已签订
const canManageMilestone = computed(() => {
  return (userStore.isEnterprise || userStore.isAdmin) && contract.value?.status === 'signed'
})

// 当前撮合步骤
const matchingStep = computed(() => {
  if (!contract) return 0
  if (contract.value?.status === 'completed') return 5
  if (milestoneList.value.some(m => m.status === 'approved')) return 4
  if (milestoneList.value.length > 0) return 3
  if (contract.value?.status === 'signed') return 3
  if (contract.value?.status === 'unsigned') return 2
  if (bidList.value.some(b => b.status === 'approved')) return 2
  if (bidList.value.length > 0) return 1
  return 0
})

function statusText(status: string) {
  const map: Record<string, string> = {
    open: '进行中',
    closed: '已关闭',
    completed: '已完成',
    ongoing: '撮合中'
  }
  return map[status] || status
}

function bidStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消'
  }
  return map[status] || status
}

function bidStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

function contractStatusText(status: string) {
  const map: Record<string, string> = {
    unsigned: '未签订',
    signed: '已签订',
    completed: '已完成',
    terminated: '已终止'
  }
  return map[status] || status
}

function contractStatusType(status: string) {
  const map: Record<string, any> = {
    unsigned: 'warning',
    signed: 'success',
    completed: 'success',
    terminated: 'danger'
  }
  return map[status] || 'info'
}

function milestoneStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待提交',
    submitted: '待验收',
    approved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || status
}

function milestoneTagType(status: string) {
  const map: Record<string, any> = {
    pending: 'info',
    submitted: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

function milestoneItemType(status: string) {
  const map: Record<string, any> = {
    pending: 'primary',
    submitted: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'primary'
}

function canSubmitMilestone(m: any) {
  return (userStore.isStudent || userStore.isTeacher || userStore.isAdmin) && m.status === 'pending'
}

function canApproveMilestone(m: any) {
  return (userStore.isEnterprise || userStore.isAdmin) && m.status === 'submitted'
}

async function loadDemandDetail() {
  loading.value = true
  try {
    const res: any = await getDemandDetail(demandId)
    demand.value = res.data
  } catch (error) {
    ElMessage.error('加载企业需求详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function loadBids() {
  try {
    const res: any = await listDemandBids(demandId)
    bidList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

async function loadContract() {
  try {
    const res: any = await getContractByDemand(demandId)
    contract.value = res.data
    if (contract.value) {
      loadMilestones(contract.value.id)
    }
  } catch (error) {
    console.error(error)
  }
}

async function loadMilestones(contractId: number | string) {
  try {
    const res: any = await listMilestones(contractId)
    milestoneList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

function openBidDialog() {
  bidDialogVisible.value = true
}

async function handleSubmitBid() {
  if (!bidForm.bidContent) {
    ElMessage.warning('请填写承接方案')
    return
  }
  submitting.value = true
  try {
    const res: any = await submitDemandBid({
      demandId: Number(demandId),
      bidContent: bidForm.bidContent,
      expectedDuration: bidForm.expectedDuration,
      expectedBudget: bidForm.expectedBudget
    })
    if (res.code === 200) {
      ElMessage.success('提交成功')
      bidDialogVisible.value = false
      bidForm.bidContent = ''
      bidForm.expectedDuration = ''
      bidForm.expectedBudget = ''
      loadBids()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

async function handleApproveBid(bidId: number | string) {
  try {
    const res: any = await approveDemandBid(bidId)
    if (res.code === 200) {
      ElMessage.success('已通过')
      // 自动创建合同
      const approvedBid = bidList.value.find(b => b.id === bidId)
      await createContractForBid(approvedBid)
      loadBids()
      loadContract()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

async function createContractForBid(bid: any) {
  try {
    await createContract({
      demandId: Number(demandId),
      bidId: bid.id,
      contractTitle: `${demand.value?.demandTitle || '需求'} 合作合同`,
      contractContent: '本合同由甲乙双方就本需求达成合作意向后签订，具体内容双方另行约定。',
      amount: bid.expectedBudget ? parseFloat(bid.expectedBudget.replace(/[^0-9.]/g, '')) : null
    })
  } catch (error) {
    console.error(error)
  }
}

async function handleRejectBid(bidId: number | string) {
  try {
    const res: any = await rejectDemandBid(bidId)
    if (res.code === 200) {
      ElMessage.success('已驳回')
      loadBids()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

async function handleSignContract() {
  if (!contract.value) return
  try {
    const res: any = await signContract(contract.value.id)
    if (res.code === 200) {
      ElMessage.success('合同签订成功')
      loadContract()
    } else {
      ElMessage.error(res.message || '签订失败')
    }
  } catch (error) {
    ElMessage.error('签订失败')
    console.error(error)
  }
}

function openMilestoneDialog() {
  milestoneDialogVisible.value = true
}

async function handleCreateMilestone() {
  if (!milestoneForm.milestoneName) {
    ElMessage.warning('请填写里程碑名称')
    return
  }
  if (!contract.value) return
  submitting.value = true
  try {
    const res: any = await createMilestone({
      contractId: contract.value.id,
      demandId: Number(demandId),
      milestoneName: milestoneForm.milestoneName,
      description: milestoneForm.description,
      dueDate: milestoneForm.dueDate,
      sortOrder: milestoneList.value.length
    })
    if (res.code === 200) {
      ElMessage.success('创建成功')
      milestoneDialogVisible.value = false
      milestoneForm.milestoneName = ''
      milestoneForm.description = ''
      milestoneForm.dueDate = ''
      loadMilestones(contract.value.id)
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

async function handleSubmitMilestone(milestoneId: number | string) {
  try {
    const { value } = await ElMessageBox.prompt('请输入交付物链接（URL）', '提交交付物', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: 'https://example.com/file'
    })
    if (!value) return
    const res: any = await submitDeliverable(milestoneId, value)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      if (contract.value) loadMilestones(contract.value.id)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    // 取消时不处理
  }
}

async function handleApproveMilestone(milestoneId: number | string) {
  try {
    const res: any = await approveDeliverable(milestoneId)
    if (res.code === 200) {
      ElMessage.success('验收通过')
      if (contract.value) loadMilestones(contract.value.id)
      loadDemandDetail()
      loadContract()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

async function handleRejectMilestone(milestoneId: number | string) {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回交付物', {
      confirmButtonText: '驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入驳回原因'
    })
    const res: any = await rejectDeliverable(milestoneId, value || '')
    if (res.code === 200) {
      ElMessage.success('已驳回')
      if (contract.value) loadMilestones(contract.value.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

function goBack() {
  router.push('/app/research/demands')
}

onMounted(() => {
  loadDemandDetail()
  loadBids()
  loadContract()
})
</script>

<style scoped lang="scss">
.back-nav {
  margin-bottom: var(--zh-space-4);

  .el-button {
    color: var(--zh-text-secondary);
    font-weight: 500;

    &:hover {
      color: var(--zh-primary);
      background: var(--zh-primary-soft);
    }
  }
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--zh-space-6);
  align-items: start;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-6);
}

.detail-hero {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius-lg);
  padding: var(--zh-space-8);
  box-shadow: var(--zh-shadow);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 5px;
    background: linear-gradient(90deg, var(--zh-primary) 0%, var(--zh-secondary) 50%, var(--zh-accent) 100%);
  }
}

.detail-header {
  margin-bottom: var(--zh-space-6);

  .header-tags {
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);
    margin-bottom: var(--zh-space-4);
    flex-wrap: wrap;

    .demand-status {
      padding: 5px 12px;
      border-radius: 100px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      background-color: var(--zh-text-tertiary);

      &.open { background-color: var(--zh-success); }
      &.completed { background-color: var(--zh-secondary); }
      &.closed { background-color: var(--zh-danger); }
    }
  }

  .detail-title {
    font-family: var(--zh-font-display);
    font-size: 32px;
    font-weight: 700;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-4) 0;
    line-height: 1.3;
  }

  .detail-meta {
    display: flex;
    gap: var(--zh-space-5);

    .meta-item {
      display: flex;
      align-items: center;
      gap: var(--zh-space-2);
      font-size: 13px;
      color: var(--zh-text-secondary);
    }
  }
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-5);
}

.detail-section {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-6);
  box-shadow: var(--zh-shadow);

  .section-title {
    font-family: var(--zh-font-display);
    font-size: 18px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-4) 0;
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);

    .section-marker {
      width: 4px;
      height: 22px;
      background: var(--zh-accent);
      border-radius: 2px;
    }
  }

  .section-content {
    font-size: 15px;
    line-height: 1.8;
    color: var(--zh-text-secondary);
    white-space: pre-wrap;
  }
}

.matching-section {
  .matching-steps {
    margin-bottom: var(--zh-space-5);
  }

  .bid-action {
    margin-bottom: var(--zh-space-4);
  }

  .sub-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: var(--zh-space-4) 0 var(--zh-space-3) 0;
  }

  .bid-list {
    .bid-card {
      margin-bottom: var(--zh-space-3);
      .bid-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: var(--zh-space-2);
        .bidder-name {
          font-weight: 600;
          margin-right: var(--zh-space-2);
        }
        .bid-time {
          font-size: 12px;
          color: var(--zh-text-tertiary);
        }
      }
      .bid-content {
        font-size: 14px;
        color: var(--zh-text-secondary);
        line-height: 1.6;
        p {
          margin: var(--zh-space-1) 0;
        }
      }
      .bid-actions {
        margin-top: var(--zh-space-2);
      }
    }
  }

  .contract-section {
    margin-top: var(--zh-space-4);
    .contract-actions {
      margin-top: var(--zh-space-3);
    }
  }

  .milestone-section {
    margin-top: var(--zh-space-4);
    .milestone-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--zh-space-3);
    }
    .milestone-card {
      .milestone-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
        margin-bottom: var(--zh-space-2);
      }
      .milestone-desc {
        font-size: 14px;
        color: var(--zh-text-secondary);
        margin-bottom: var(--zh-space-1);
      }
      .milestone-meta {
        font-size: 12px;
        color: var(--zh-text-tertiary);
        margin-bottom: var(--zh-space-1);
      }
      .milestone-actions {
        margin-top: var(--zh-space-2);
      }
    }
  }
}

.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-5);
  position: sticky;
  top: 88px;
}

.sidebar-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-6);
  box-shadow: var(--zh-shadow);
}

.sidebar-title {
  font-family: var(--zh-font-display);
  font-size: 17px;
  font-weight: 600;
  color: var(--zh-primary);
  margin: 0 0 var(--zh-space-5) 0;
  padding-bottom: var(--zh-space-3);
  border-bottom: 1px solid var(--zh-border-light);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-1);

  .info-label {
    font-size: 12px;
    color: var(--zh-text-tertiary);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .info-value {
    font-size: 14px;
    color: var(--zh-text-primary);
    font-weight: 500;
    line-height: 1.5;
  }
}

@media (max-width: 1200px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .detail-hero {
    padding: var(--zh-space-5);
  }

  .detail-header .detail-title {
    font-size: 24px;
  }
}
</style>
