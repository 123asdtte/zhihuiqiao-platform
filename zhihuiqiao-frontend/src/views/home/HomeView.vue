<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>欢迎使用智汇桥</h1>
        <p>AI 驱动的产学研用一体化智慧协同平台，连接教师、学生、实验室与校企资源。</p>
      </div>
    </div>

    <!-- 统计指标 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">平台用户</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#67c23a"><Search /></el-icon>
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-label">科研项目</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#e6a23c"><OfficeBuilding /></el-icon>
          <div class="stat-value">{{ stats.demandCount }}</div>
          <div class="stat-label">企业需求</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#909399"><Box /></el-icon>
          <div class="stat-value">{{ stats.resourceCount }}</div>
          <div class="stat-label">闲置资源</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#f56c6c"><Reading /></el-icon>
          <div class="stat-value">{{ stats.learningResourceCount }}</div>
          <div class="stat-label">学习资源</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="4">
        <el-card class="stat-card" shadow="hover">
          <el-icon class="stat-icon" color="#409eff"><DocumentChecked /></el-icon>
          <div class="stat-value">{{ stats.bookingCount }}</div>
          <div class="stat-label">资源预约</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card class="feature-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" v-for="item in quickEntries" :key="item.path">
              <div class="quick-entry" @click="router.push(item.path)">
                <el-icon class="entry-icon" :size="28" :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
                <div class="entry-title">{{ item.title }}</div>
                <div class="entry-desc">{{ item.desc }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="notice-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <ul class="notice-list">
            <li v-for="(notice, index) in notices" :key="index">
              <el-icon><Bell /></el-icon>
              <span>{{ notice }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Search,
  OfficeBuilding,
  Box,
  Reading,
  DocumentChecked,
  Bell,
  Collection,
  DataLine
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'

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

// 快捷入口
const quickEntries = [
  {
    title: '科研项目',
    desc: '浏览与申请科研项目',
    path: '/app/research/projects',
    icon: Search,
    color: '#409eff'
  },
  {
    title: '企业需求',
    desc: '查看企业发布的需求',
    path: '/app/research/demands',
    icon: OfficeBuilding,
    color: '#e6a23c'
  },
  {
    title: '闲置资源',
    desc: '预约借用校园资源',
    path: '/app/resource/list',
    icon: Box,
    color: '#67c23a'
  },
  {
    title: '学习资源',
    desc: '探索课程与资料',
    path: '/app/learning/resources',
    icon: Collection,
    color: '#f56c6c'
  },
  {
    title: '学习中心',
    desc: '管理学习进度',
    path: '/app/learning/center',
    icon: Reading,
    color: '#909399'
  },
  {
    title: '数据看板',
    desc: '查看平台统计数据',
    path: '/app/admin/dashboard',
    icon: DataLine,
    color: '#409eff'
  }
]

// 系统公告
const notices = [
  '科研撮合模块已上线，欢迎发布项目与申请加入。',
  '闲置资源流转功能已启用，请在资源列表预约借用。',
  '教学辅助模块新增学习资源与学习中心。',
  '请及时完善个人科研画像，提升匹配精准度。'
]

/**
 * 加载统计数据
 */
async function loadStats() {
  try {
    const res: any = await getDashboardStats()
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
.home-page {
  padding: 20px;

  .welcome-banner {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 40px;
    margin-bottom: 24px;
    color: #fff;

    .welcome-content {
      h1 {
        margin: 0 0 12px 0;
        font-size: 28px;
      }

      p {
        margin: 0;
        font-size: 15px;
        opacity: 0.9;
      }
    }
  }

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;
      margin-bottom: 20px;
      padding: 16px;

      .stat-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
        margin-bottom: 4px;
      }

      .stat-label {
        color: #909399;
        font-size: 13px;
      }
    }
  }

  .feature-card,
  .notice-card {
    margin-bottom: 20px;

    .card-header {
      font-weight: 700;
      font-size: 16px;
    }
  }

  .quick-entry {
    text-align: center;
    padding: 20px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;
    margin-bottom: 12px;

    &:hover {
      background: #f5f7fa;
    }

    .entry-icon {
      margin-bottom: 8px;
    }

    .entry-title {
      font-size: 15px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 4px;
    }

    .entry-desc {
      font-size: 12px;
      color: #909399;
    }
  }

  .notice-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 10px 0;
      border-bottom: 1px solid #ebeef5;
      color: #606266;
      font-size: 14px;

      &:last-child {
        border-bottom: none;
      }

      .el-icon {
        color: #409eff;
        margin-top: 2px;
      }
    }
  }
}
</style>
