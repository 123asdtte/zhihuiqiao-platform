<template>
  <div class="my-applications-page">
    <div class="page-header">
      <h2>我的申请</h2>
      <p>查看您申请加入的科研项目状态</p>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="applications" style="width: 100%">
        <el-table-column prop="projectName" label="项目名称" min-width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="goToProject(row.projectId)">
              {{ row.projectName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="applyReason" label="申请理由" min-width="240" show-overflow-tooltip />
        <el-table-column prop="status" label="申请状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replyMessage" label="审核回复" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.replyMessage || '暂无回复' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
      </el-table>

      <el-empty v-if="!loading && applications.length === 0" description="暂无申请记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyApplications } from '@/api/research'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const applications = ref<any[]>([])
const loading = ref(false)

/**
 * 状态文本映射
 */
function statusText(status: string) {
  const map: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return map[status] || status
}

/**
 * 状态标签类型
 */
function statusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

/**
 * 加载我的申请列表
 */
async function loadMyApplications() {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const res: any = await getMyApplications(userStore.userInfo.id)
    applications.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 跳转项目详情
 */
function goToProject(projectId: number) {
  router.push(`/app/research/projects/${projectId}`)
}

onMounted(() => {
  loadMyApplications()
})
</script>

<style scoped lang="scss">
.my-applications-page {
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
