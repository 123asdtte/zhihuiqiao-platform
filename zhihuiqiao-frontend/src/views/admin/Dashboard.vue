<template>
  <div class="admin-dashboard-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据看板</h2>
      <p>全平台运营数据实时监控与统计分析</p>
    </div>

    <!-- 核心指标 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#409eff"><User /></el-icon>
            <span class="stat-label">注册用户</span>
          </div>
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-detail">
            学生 {{ stats.studentCount }} / 教师 {{ stats.teacherCount }} / 企业 {{ stats.enterpriseCount }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#67c23a"><Search /></el-icon>
            <span class="stat-label">科研项目</span>
          </div>
          <div class="stat-value">{{ stats.projectCount }}</div>
          <div class="stat-detail">累计发布项目数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#e6a23c"><OfficeBuilding /></el-icon>
            <span class="stat-label">企业需求</span>
          </div>
          <div class="stat-value">{{ stats.demandCount }}</div>
          <div class="stat-detail">累计需求发布数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#f56c6c"><Box /></el-icon>
            <span class="stat-label">闲置资源</span>
          </div>
          <div class="stat-value">{{ stats.resourceCount }}</div>
          <div class="stat-detail">累计资源预约 {{ stats.bookingCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 二级指标 -->
    <el-row :gutter="20" class="sub-stat-row">
      <el-col :xs="24" :md="8">
        <el-card class="sub-stat-card" shadow="never">
          <div class="sub-stat-value">{{ stats.applicationCount }}</div>
          <div class="sub-stat-label">项目申请数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="sub-stat-card" shadow="never">
          <div class="sub-stat-value">{{ stats.learningResourceCount }}</div>
          <div class="sub-stat-label">学习资源数</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="sub-stat-card" shadow="never">
          <div class="sub-stat-value">{{ stats.bookingCount }}</div>
          <div class="sub-stat-label">资源预约数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷管理 -->
    <el-card class="manage-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>快捷管理</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="8" :md="4" v-for="item in manageEntries" :key="item.path">
          <div class="manage-entry" @click="router.push(item.path)">
            <el-icon class="entry-icon" :size="28" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <div class="entry-title">{{ item.title }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
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
  UserFilled,
  Management,
  Collection
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'

const router = useRouter()

// 统计数据
const stats = reactive({
  userCount: 0,
  studentCount: 0,
  teacherCount: 0,
  enterpriseCount: 0,
  projectCount: 0,
  demandCount: 0,
  applicationCount: 0,
  resourceCount: 0,
  bookingCount: 0,
  learningResourceCount: 0
})

// 快捷管理入口
const manageEntries = [
  {
    title: '用户管理',
    path: '/app/admin/users',
    icon: UserFilled,
    color: '#409eff'
  },
  {
    title: '内容审核',
    path: '/app/admin/audit',
    icon: Management,
    color: '#e6a23c'
  },
  {
    title: '科研项目',
    path: '/app/research/projects',
    icon: Search,
    color: '#67c23a'
  },
  {
    title: '企业需求',
    path: '/app/research/demands',
    icon: OfficeBuilding,
    color: '#f56c6c'
  },
  {
    title: '闲置资源',
    path: '/app/resource/list',
    icon: Box,
    color: '#909399'
  },
  {
    title: '学习资源',
    path: '/app/learning/resources',
    icon: Collection,
    color: '#409eff'
  },
  {
    title: '教学资源',
    path: '/app/learning/resources',
    icon: Reading,
    color: '#f56c6c'
  },
  {
    title: '资源预约',
    path: '/app/resource/bookings',
    icon: DocumentChecked,
    color: '#67c23a'
  }
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
.admin-dashboard-page {
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

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      margin-bottom: 20px;
      padding: 12px;

      .stat-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .stat-icon {
          font-size: 24px;
        }

        .stat-label {
          color: #909399;
          font-size: 14px;
        }
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
        margin-bottom: 8px;
      }

      .stat-detail {
        color: #909399;
        font-size: 12px;
      }
    }
  }

  .sub-stat-row {
    margin-bottom: 20px;

    .sub-stat-card {
      text-align: center;
      margin-bottom: 20px;

      .sub-stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 4px;
      }

      .sub-stat-label {
        color: #606266;
        font-size: 13px;
      }
    }
  }

  .manage-card {
    .card-header {
      font-weight: 700;
      font-size: 16px;
    }

    .manage-entry {
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
        font-size: 14px;
        color: #303133;
      }
    }
  }
}
</style>
