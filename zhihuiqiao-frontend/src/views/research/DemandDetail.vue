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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Calendar, ArrowLeft } from '@element-plus/icons-vue'
import { getDemandDetail } from '@/api/research'

const route = useRoute()
const router = useRouter()

// 当前需求ID
const demandId = route.params.id as string
// 需求详情数据
const demand = ref<any>(null)
// 页面加载状态
const loading = ref(false)

/**
 * 状态文本映射
 * open-进行中 closed-已关闭 completed-已完成
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    open: '进行中',
    closed: '已关闭',
    completed: '已完成'
  }
  return map[status] || status
}

/**
 * 加载企业需求详情
 */
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

/**
 * 返回企业需求列表页
 */
function goBack() {
  router.push('/app/research/demands')
}

onMounted(() => {
  loadDemandDetail()
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
