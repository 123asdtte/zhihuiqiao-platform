<template>
  <div class="operation-log-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>操作日志</h2>
      <p>查看系统用户的操作记录，支持按模块、用户名、状态和时间筛选</p>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="操作模块">
          <el-input
            v-model="queryForm.module"
            placeholder="请输入操作模块"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select
            v-model="queryForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table :data="logList" stripe border>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="roleType" label="角色" width="100" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operation" label="操作描述" min-width="150" />
        <el-table-column prop="method" label="请求方法" width="90" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="executionTime" label="耗时(ms)" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">成功</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLogList } from '@/api/operationLog'

// 查询表单
const queryForm = reactive({
  module: '',
  username: '',
  status: undefined as number | undefined,
  dateRange: [] as string[]
})

// 日志列表
const logList = ref<any[]>([])
const loading = ref(false)

// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/**
 * 加载操作日志列表
 */
async function loadLogs() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      module: queryForm.module || undefined,
      username: queryForm.username || undefined,
      status: queryForm.status
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startTime = queryForm.dateRange[0]
      params.endTime = queryForm.dateRange[1]
    }

    const res: any = await getOperationLogList(params)
    if (res.code === 200 && res.data) {
      logList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载操作日志失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 查询按钮
 */
function handleSearch() {
  pagination.pageNum = 1
  loadLogs()
}

/**
 * 重置按钮
 */
function handleReset() {
  queryForm.module = ''
  queryForm.username = ''
  queryForm.status = undefined
  queryForm.dateRange = []
  pagination.pageNum = 1
  loadLogs()
}

/**
 * 分页大小变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadLogs()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
.operation-log-page {
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

  .filter-card {
    margin-bottom: 20px;
  }

  .table-card {
    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style>
