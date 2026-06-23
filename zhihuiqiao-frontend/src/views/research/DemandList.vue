<template>
  <div class="page-container">
    <div class="page-header">
      <h2>企业需求</h2>
      <p>浏览企业发布的技术需求与合作机会，促进产学研对接</p>
    </div>

    <!-- 发布需求按钮（企业用户） -->
    <div class="action-row" v-if="userStore.isEnterprise">
      <el-button type="primary" @click="showPublish = true">
        <el-icon><Edit /></el-icon> 发布需求
      </el-button>
    </div>

    <!-- 智能推荐区（教师/企业） -->
    <el-card v-if="userStore.isTeacher || userStore.isEnterprise" class="recommend-section" shadow="never">
      <template #header>
        <div class="section-header">
          <span class="section-title">
            <el-icon><Search /></el-icon>
            智能推荐
          </span>
          <el-tag type="success" effect="plain" size="small">基于你的科研能力</el-tag>
        </div>
      </template>
      <el-skeleton :loading="loading.recommend" animated :count="3">
        <template v-if="recommendedList.length > 0">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" v-for="item in recommendedList" :key="item.demand?.id" class="mb-16">
              <el-card class="recommend-card" shadow="hover">
                <div class="recommend-header">
                  <span class="recommend-name">{{ item.demand?.demandTitle }}</span>
                  <el-tag :type="scoreTag(item.score)" size="small" effect="dark">
                    {{ (item.score * 100).toFixed(0) }}%
                  </el-tag>
                </div>
                <div class="recommend-tags">
                  <el-tag size="small" type="info" v-if="item.demand?.industryField">
                    {{ item.demand.industryField }}
                  </el-tag>
                </div>
                <p class="recommend-reason">{{ item.reason }}</p>
              </el-card>
            </el-col>
          </el-row>
        </template>
        <el-empty v-else description="暂无推荐" :image-size="80" />
      </el-skeleton>
    </el-card>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="需求类型">
          <el-select v-model="searchForm.demandType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option label="技术攻关" value="技术攻关" />
            <el-option label="成果转化" value="成果转化" />
            <el-option label="人才招聘" value="人才招聘" />
            <el-option label="联合研发" value="联合研发" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable @change="handleSearch">
            <el-option label="开放中" value="open" />
            <el-option label="已关闭" value="closed" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="需求标题/描述" clearable style="width: 220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 需求列表 -->
    <el-skeleton :loading="loading.list" animated :count="4">
      <template v-if="demandList.length > 0">
        <el-card
          v-for="item in demandList"
          :key="item.id"
          class="demand-card"
          shadow="hover"
          @click="toggleExpand(item.id)"
        >
          <div class="demand-header">
            <div class="demand-title-row">
              <h3 class="demand-title">{{ item.demandTitle }}</h3>
              <el-tag :type="demandStatusType(item.status)" size="small" effect="dark">
                {{ demandStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="demand-tags">
              <el-tag size="small" v-if="item.demandType">{{ item.demandType }}</el-tag>
              <el-tag size="small" type="info" v-if="item.industryField">{{ item.industryField }}</el-tag>
            </div>
          </div>
          <p class="demand-desc">{{ item.demandDescription }}</p>
          <div class="demand-footer">
            <span class="footer-item" v-if="item.budgetRange">
              <el-icon><Coin /></el-icon> {{ item.budgetRange }}
            </span>
            <span class="footer-item">
              <el-icon><View /></el-icon> {{ item.views }}
            </span>
            <span class="expand-hint">
              <el-icon><ArrowDown v-if="expandedId !== item.id" /><ArrowUp v-else /></el-icon>
              {{ expandedId === item.id ? '收起' : '展开详情' }}
            </span>
          </div>

          <!-- 展开详情 -->
          <el-collapse-transition>
            <div v-show="expandedId === item.id" class="demand-expand">
              <el-divider />
              <div class="expand-section">
                <h4>技术要求</h4>
                <p>{{ item.technicalRequirements || '暂无' }}</p>
              </div>
              <div class="expand-section">
                <h4>合作方式</h4>
                <p>{{ item.cooperationMode || '暂无' }}</p>
              </div>
              <div class="expand-section">
                <h4>联系方式</h4>
                <p>{{ item.contactInfo || '暂无' }}</p>
              </div>
            </div>
          </el-collapse-transition>
        </el-card>
      </template>
      <el-empty v-else description="暂无需求数据" :image-size="100" />
    </el-skeleton>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-wrapper">
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

    <!-- 发布需求对话框 -->
    <el-dialog v-model="showPublish" title="发布企业需求" width="640px" :close-on-click-modal="false">
      <el-form :model="publishForm" label-width="100px" :rules="publishRules" ref="publishFormRef">
        <el-form-item label="需求标题" prop="demandTitle">
          <el-input v-model="publishForm.demandTitle" placeholder="请输入需求标题" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="需求类型" prop="demandType">
              <el-select v-model="publishForm.demandType" placeholder="请选择" style="width: 100%">
                <el-option label="技术攻关" value="技术攻关" />
                <el-option label="成果转化" value="成果转化" />
                <el-option label="人才招聘" value="人才招聘" />
                <el-option label="联合研发" value="联合研发" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行业领域" prop="industryField">
              <el-input v-model="publishForm.industryField" placeholder="如：人工智能" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="需求描述" prop="demandDescription">
          <el-input v-model="publishForm.demandDescription" type="textarea" :rows="3" placeholder="请详细描述需求内容..." />
        </el-form-item>
        <el-form-item label="技术要求">
          <el-input v-model="publishForm.technicalRequirements" type="textarea" :rows="2" placeholder="技术要求（可选）" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算范围">
              <el-input v-model="publishForm.budgetRange" placeholder="如：10-50万" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作方式">
              <el-select v-model="publishForm.cooperationMode" placeholder="请选择" style="width: 100%">
                <el-option label="委托开发" value="委托开发" />
                <el-option label="联合研发" value="联合研发" />
                <el-option label="技术转让" value="技术转让" />
                <el-option label="人才合作" value="人才合作" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系方式">
          <el-input v-model="publishForm.contactInfo" placeholder="电话/邮箱（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Edit, View, Coin, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDemandList, recommendDemands, publishDemand } from '@/api/research'

const userStore = useUserStore()

const loading = reactive({ recommend: true, list: true })
const recommendedList = ref<any[]>([])
const demandList = ref<any[]>([])
const expandedId = ref<number | null>(null)

const searchForm = reactive({
  keyword: '',
  demandType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 发布需求
const showPublish = ref(false)
const publishing = ref(false)
const publishFormRef = ref<any>(null)
const publishForm = reactive({
  demandTitle: '',
  demandType: '',
  industryField: '',
  demandDescription: '',
  technicalRequirements: '',
  budgetRange: '',
  cooperationMode: '',
  contactInfo: ''
})
const publishRules = {
  demandTitle: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  demandType: [{ required: true, message: '请选择需求类型', trigger: 'change' }],
  demandDescription: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
}

function demandStatusText(status: string) {
  const map: Record<string, string> = { open: '开放中', closed: '已关闭', completed: '已完成' }
  return map[status] || status
}

function demandStatusType(status: string): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = { open: 'success', closed: 'info', completed: 'info' }
  return map[status] || 'info'
}

function scoreTag(score: number): 'success' | 'warning' | 'info' {
  if (score >= 0.6) return 'success'
  if (score >= 0.3) return 'warning'
  return 'info'
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

async function loadDemandList() {
  loading.list = true
  try {
    const res: any = await getDemandList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      demandType: searchForm.demandType,
      status: searchForm.status
    })
    demandList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error('加载需求列表失败')
  } finally {
    loading.list = false
  }
}

async function loadRecommendations() {
  if (!userStore.isTeacher && !userStore.isEnterprise) {
    loading.recommend = false
    return
  }
  loading.recommend = true
  try {
    const res: any = await recommendDemands(userStore.userInfo!.id, 6)
    recommendedList.value = res?.data || []
  } catch {
    // silently fail
  } finally {
    loading.recommend = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadDemandList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.demandType = ''
  searchForm.status = ''
  handleSearch()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadDemandList()
}

function handlePageChange(page: number) {
  pagination.pageNum = page
  loadDemandList()
}

async function handlePublish() {
  const valid = await publishFormRef.value?.validate().catch(() => false)
  if (!valid) return
  publishing.value = true
  try {
    await publishDemand({ ...publishForm, enterpriseId: userStore.userInfo!.id })
    ElMessage.success('需求发布成功')
    showPublish.value = false
    Object.assign(publishForm, {
      demandTitle: '', demandType: '', industryField: '', demandDescription: '',
      technicalRequirements: '', budgetRange: '', cooperationMode: '', contactInfo: ''
    })
    loadDemandList()
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  loadDemandList()
  loadRecommendations()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

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

.action-row {
  margin-bottom: 16px;
}

.recommend-section {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.mb-16 {
  margin-bottom: 16px;
}

.recommend-card {
  cursor: default;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .recommend-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .recommend-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  .recommend-tags {
    display: flex;
    gap: 6px;
    margin-bottom: 8px;
    flex-wrap: wrap;
  }

  .recommend-reason {
    margin: 0;
    font-size: 12px;
    color: #909399;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.demand-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .demand-header {
    margin-bottom: 12px;
  }

  .demand-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .demand-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .demand-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  .demand-desc {
    margin: 0 0 12px 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .demand-footer {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 13px;
    color: #909399;

    .footer-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .expand-hint {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 4px;
      color: #409eff;
      font-size: 12px;
    }
  }

  .demand-expand {
    padding-top: 8px;

    .expand-section {
      margin-bottom: 12px;

      h4 {
        margin: 0 0 6px 0;
        font-size: 13px;
        color: #606266;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #303133;
        line-height: 1.6;
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
