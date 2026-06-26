<template>
  <div class="home-page zh-page">
    <!-- Hero 区域：非对称布局 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          AI 驱动的产学研协同平台
        </div>
        <h1 class="hero-title">
          连接知识、资源与机遇
          <span class="hero-accent">智汇桥</span>
        </h1>
        <p class="hero-desc">
          为高校师生、科研机构与企业搭建高效协同桥梁，整合科研撮合、资源流转与教学辅助，
          让每一次合作都始于精准的连接。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="hero-btn-primary" @click="router.push('/app/research/projects')">
            浏览科研项目
          </el-button>
          <el-button size="large" class="hero-btn-secondary" @click="router.push('/app/resource/list')">
            探索闲置资源
          </el-button>
        </div>
      </div>
      <div class="hero-visual">
        <div class="hero-card hero-card-primary">
          <el-icon :size="32"><Search /></el-icon>
          <div class="hero-card-value">{{ stats.projectCount }}+</div>
          <div class="hero-card-label">科研项目</div>
        </div>
        <div class="hero-card hero-card-secondary">
          <el-icon :size="32"><OfficeBuilding /></el-icon>
          <div class="hero-card-value">{{ stats.demandCount }}+</div>
          <div class="hero-card-label">企业需求</div>
        </div>
        <div class="hero-card hero-card-tertiary">
          <el-icon :size="32"><Box /></el-icon>
          <div class="hero-card-value">{{ stats.resourceCount }}+</div>
          <div class="hero-card-label">闲置资源</div>
        </div>
      </div>
    </section>

    <!-- 统计指标 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-item" v-for="(item, index) in statItems" :key="index">
          <div class="stat-icon-wrap" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-access-section">
      <h2 class="zh-section-title">
        <span class="section-icon"><Grid /></span>
        快捷入口
      </h2>
      <div class="quick-grid">
        <div
          class="quick-card"
          v-for="item in quickEntries"
          :key="item.path"
          @click="router.push(item.path)"
        >
          <div class="quick-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="26"><component :is="item.icon" /></el-icon>
          </div>
          <div class="quick-content">
            <div class="quick-title">{{ item.title }}</div>
            <div class="quick-desc">{{ item.desc }}</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 公告与推荐 -->
    <section class="bottom-section">
      <div class="notice-panel">
        <h2 class="zh-section-title">
          <span class="section-icon"><Bell /></span>
          系统公告
        </h2>
        <ul class="notice-list">
          <li v-for="(notice, index) in notices" :key="index">
            <span class="notice-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="notice-text">{{ notice }}</span>
          </li>
        </ul>
      </div>

      <div class="tip-panel">
        <h2 class="zh-section-title">
          <span class="section-icon"><InfoFilled /></span>
          使用小贴士
        </h2>
        <div class="tip-list">
          <div class="tip-item" v-for="(tip, index) in tips" :key="index">
            <div class="tip-marker" :style="{ background: tip.color }"></div>
            <div class="tip-content">
              <div class="tip-title">{{ tip.title }}</div>
              <div class="tip-desc">{{ tip.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  OfficeBuilding,
  Box,
  Reading,
  DocumentChecked,
  User,
  Collection,
  DataLine,
  ArrowRight,
  Bell,
  Grid,
  InfoFilled
} from '@element-plus/icons-vue'
import { getPublicDashboardStats } from '@/api/dashboard'

const router = useRouter()

// 统计数据
const stats = reactive({
  userCount: 0,
  projectCount: 0,
  demandCount: 0,
  resourceCount: 0,
  learningResourceCount: 0,
  bookingCount: 0
})

// 统计项配置
const statItems = computed(() => [
  { label: '平台用户', value: stats.userCount, icon: User, color: '#1a365d', bg: 'rgba(26, 54, 93, 0.08)' },
  { label: '科研项目', value: stats.projectCount, icon: Search, color: '#3b5998', bg: 'rgba(59, 89, 152, 0.08)' },
  { label: '企业需求', value: stats.demandCount, icon: OfficeBuilding, color: '#c77a24', bg: 'rgba(199, 122, 36, 0.08)' },
  { label: '闲置资源', value: stats.resourceCount, icon: Box, color: '#2d7d46', bg: 'rgba(45, 125, 70, 0.08)' },
  { label: '学习资源', value: stats.learningResourceCount, icon: Reading, color: '#b5423b', bg: 'rgba(181, 66, 59, 0.08)' },
  { label: '资源预约', value: stats.bookingCount, icon: DocumentChecked, color: '#3b82a4', bg: 'rgba(59, 130, 164, 0.08)' }
])

// 快捷入口
const quickEntries = [
  {
    title: '科研项目',
    desc: '浏览前沿课题，申请加入研究团队',
    path: '/app/research/projects',
    icon: Search,
    color: '#1a365d',
    bg: 'rgba(26, 54, 93, 0.08)'
  },
  {
    title: '企业需求',
    desc: '发现校企合作项目与技术攻关机会',
    path: '/app/research/demands',
    icon: OfficeBuilding,
    color: '#c77a24',
    bg: 'rgba(199, 122, 36, 0.08)'
  },
  {
    title: '闲置资源',
    desc: '预约借用实验室设备与闲置物资',
    path: '/app/resource/list',
    icon: Box,
    color: '#2d7d46',
    bg: 'rgba(45, 125, 70, 0.08)'
  },
  {
    title: '学习资源',
    desc: '探索课程、论文与学术资料',
    path: '/app/learning/resources',
    icon: Collection,
    color: '#b5423b',
    bg: 'rgba(181, 66, 59, 0.08)'
  },
  {
    title: '学习中心',
    desc: '管理个人学习进度与收藏',
    path: '/app/learning/center',
    icon: Reading,
    color: '#3b5998',
    bg: 'rgba(59, 89, 152, 0.08)'
  },
  {
    title: '数据看板',
    desc: '查看平台运营与统计数据',
    path: '/app/admin/dashboard',
    icon: DataLine,
    color: '#3b82a4',
    bg: 'rgba(59, 130, 164, 0.08)'
  }
]

// 系统公告
const notices = [
  '科研撮合模块已上线，欢迎发布项目与申请加入。',
  '闲置资源流转功能已启用，请在资源列表预约借用。',
  '教学辅助模块新增学习资源与学习中心。',
  '请及时完善个人科研画像，提升匹配精准度。'
]

// 使用小贴士
const tips = [
  { title: '完善科研画像', desc: '填写研究方向与技能标签，获取更精准的项目推荐。', color: '#1a365d' },
  { title: '主动申请项目', desc: '在项目详情页提交申请理由，提高通过概率。', color: '#c9a227' },
  { title: '及时查看通知', desc: '系统将通过实时推送告知申请审核与预约结果。', color: '#2d7d46' }
]

/**
 * 加载统计数据
 */
async function loadStats() {
  try {
    // 首页使用公开统计接口，所有登录用户均可查看平台运营数据
    const res: any = await getPublicDashboardStats()
    if (res.code === 200 && res.data) {
      Object.assign(stats, res.data)
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error(error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.hero-section {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: var(--zh-space-10);
  align-items: center;
  background: linear-gradient(135deg, var(--zh-primary) 0%, #234876 50%, #3b5998 100%);
  border-radius: var(--zh-radius-xl);
  padding: var(--zh-space-12) var(--zh-space-10);
  margin-bottom: var(--zh-space-8);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(201, 162, 39, 0.2) 0%, transparent 70%);
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.hero-content {
  position: relative;
  z-index: 1;
  color: #fff;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--zh-space-2);
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 100px;
  font-size: 13px;
  margin-bottom: var(--zh-space-5);
  backdrop-filter: blur(8px);

  .badge-dot {
    width: 8px;
    height: 8px;
    background: var(--zh-accent);
    border-radius: 50%;
    animation: pulse 2s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-family: var(--zh-font-display);
  font-size: 40px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: var(--zh-space-4);

  .hero-accent {
    display: block;
    color: var(--zh-accent-light);
    margin-top: var(--zh-space-2);
  }
}

.hero-desc {
  font-size: 16px;
  line-height: 1.7;
  opacity: 0.85;
  margin-bottom: var(--zh-space-6);
  max-width: 520px;
}

.hero-actions {
  display: flex;
  gap: var(--zh-space-4);

  .hero-btn-primary {
    background: var(--zh-accent) !important;
    border-color: var(--zh-accent) !important;
    color: #fff !important;
    font-weight: 600;
    padding: 0 28px;

    &:hover {
      background: var(--zh-accent-light) !important;
      border-color: var(--zh-accent-light) !important;
    }
  }

  .hero-btn-secondary {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.3) !important;
    color: #fff !important;
    font-weight: 500;
    padding: 0 28px;

    &:hover {
      background: rgba(255, 255, 255, 0.2) !important;
    }
  }
}

.hero-visual {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--zh-space-4);
  padding: var(--zh-space-4);
}

.hero-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  color: #fff;
  transition: transform var(--zh-transition), background var(--zh-transition);

  &:hover {
    transform: translateY(-4px);
    background: rgba(255, 255, 255, 0.15);
  }

  .el-icon {
    margin-bottom: var(--zh-space-3);
    color: var(--zh-accent-light);
  }

  .hero-card-value {
    font-family: var(--zh-font-display);
    font-size: 32px;
    font-weight: 700;
    margin-bottom: var(--zh-space-1);
  }

  .hero-card-label {
    font-size: 13px;
    opacity: 0.8;
  }
}

.hero-card-primary {
  grid-column: span 2;
}

.hero-card-tertiary {
  grid-column: span 2;
}

// ==================== 统计区 ====================
.stats-section {
  margin-bottom: var(--zh-space-10);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--zh-space-5);
}

.stat-item {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
  box-shadow: var(--zh-shadow);
  transition: transform var(--zh-transition), box-shadow var(--zh-transition);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--zh-shadow-md);
  }

  .stat-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: var(--zh-radius);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-value {
    font-family: var(--zh-font-display);
    font-size: 24px;
    font-weight: 700;
    color: var(--zh-primary);
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: var(--zh-text-tertiary);
  }
}

// ==================== 快捷入口 ====================
.quick-access-section {
  margin-bottom: var(--zh-space-10);
}

.section-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--zh-primary-soft);
  color: var(--zh-primary);
  border-radius: var(--zh-radius-sm);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--zh-space-5);
}

.quick-card {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
  cursor: pointer;
  box-shadow: var(--zh-shadow);
  transition: all var(--zh-transition);

  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--zh-shadow-md);
    border-color: var(--zh-primary-soft);

    .quick-arrow {
      transform: translateX(4px);
      color: var(--zh-primary);
    }
  }

  .quick-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--zh-radius);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .quick-content {
    flex: 1;
    min-width: 0;

    .quick-title {
      font-family: var(--zh-font-display);
      font-size: 17px;
      font-weight: 600;
      color: var(--zh-primary);
      margin-bottom: var(--zh-space-1);
    }

    .quick-desc {
      font-size: 13px;
      color: var(--zh-text-secondary);
      line-height: 1.5;
    }
  }

  .quick-arrow {
    color: var(--zh-text-tertiary);
    transition: all var(--zh-transition-fast);
  }
}

// ==================== 底部区域 ====================
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--zh-space-5);
}

.notice-panel,
.tip-panel {
  background: var(--zh-bg-elevated);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  box-shadow: var(--zh-shadow);
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: flex-start;
    gap: var(--zh-space-3);
    padding: var(--zh-space-3) 0;
    border-bottom: 1px solid var(--zh-border-light);

    &:last-child {
      border-bottom: none;
    }

    .notice-index {
      font-family: var(--zh-font-mono);
      font-size: 12px;
      font-weight: 600;
      color: var(--zh-accent);
      padding-top: 2px;
    }

    .notice-text {
      font-size: 14px;
      color: var(--zh-text-secondary);
      line-height: 1.6;
    }
  }
}

.tip-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-4);
}

.tip-item {
  display: flex;
  gap: var(--zh-space-4);
  align-items: flex-start;

  .tip-marker {
    width: 4px;
    height: 40px;
    border-radius: 2px;
    flex-shrink: 0;
  }

  .tip-content {
    .tip-title {
      font-family: var(--zh-font-display);
      font-size: 15px;
      font-weight: 600;
      color: var(--zh-primary);
      margin-bottom: var(--zh-space-1);
    }

    .tip-desc {
      font-size: 13px;
      color: var(--zh-text-secondary);
      line-height: 1.6;
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .hero-section {
    grid-template-columns: 1fr;
    gap: var(--zh-space-8);
  }

  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: var(--zh-space-6);
  }

  .hero-title {
    font-size: 28px;
  }

  .stats-grid,
  .quick-grid,
  .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>
