<template>
  <div class="user-manage-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <p>管理平台所有注册用户，支持筛选、编辑、删除与密码重置</p>
    </div>

    <!-- 顶部筛选区：使用 el-card 包裹，保持与项目其他页面风格一致 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="searchForm" inline>
        <!-- 角色类型筛选 -->
        <el-form-item label="角色类型">
          <el-select
            v-model="searchForm.roleType"
            placeholder="全部角色"
            clearable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="企业" value="enterprise" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <!-- 关键词搜索：支持用户名、姓名、邮箱 -->
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名/姓名/邮箱"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表表格 -->
    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table :data="userList" stripe border style="width: 100%">
        <!-- 用户名列 -->
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <!-- 姓名列 -->
        <el-table-column prop="realName" label="姓名" min-width="120" show-overflow-tooltip />
        <!-- 角色列：使用 tag 显示不同颜色 -->
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.roleType)" size="small">
              {{ roleText(row.roleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 邮箱列 -->
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <!-- 手机号列 -->
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <!-- 状态列 -->
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 创建时间列 -->
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <!-- 操作列 -->
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            <el-button type="warning" link size="small" @click="openResetPwdDialog(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="520px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleType" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="企业" value="enterprise" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="420px" destroy-on-close>
      <el-form :model="resetPwdForm" label-width="90px">
        <el-form-item label="新密码">
          <el-input
            v-model="resetPwdForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwdSubmit">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUser, deleteUser, resetUserPassword } from '@/api/admin'

// ==================== 搜索与筛选 ====================

/**
 * 搜索表单：roleType 为角色类型，keyword 为关键词
 */
const searchForm = reactive({
  roleType: '',
  keyword: ''
})

// ==================== 分页与列表 ====================

/**
 * 分页信息：pageNum 当前页，pageSize 每页条数，total 总条数
 */
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

/**
 * 用户列表数据与加载状态
 */
const userList = ref<any[]>([])
const loading = ref(false)

// ==================== 弹窗与表单 ====================

/**
 * 编辑弹窗显示状态及表单数据
 */
const editDialogVisible = ref(false)
const editForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  status: 1,
  roleType: ''
})

/**
 * 重置密码弹窗显示状态及表单数据
 */
const resetPwdDialogVisible = ref(false)
const resetPwdForm = reactive({
  id: '',
  newPassword: ''
})

// ==================== 辅助函数 ====================

/**
 * 角色文本映射
 * @param role 角色类型
 */
function roleText(role: string) {
  const map: Record<string, string> = {
    student: '学生',
    teacher: '教师',
    enterprise: '企业',
    admin: '管理员'
  }
  return map[role] || role
}

/**
 * 角色对应的 Element Plus tag 类型，用于显示不同颜色
 * @param role 角色类型
 */
function roleTagType(role: string) {
  const map: Record<string, any> = {
    student: 'primary',
    teacher: 'success',
    enterprise: 'warning',
    admin: 'danger'
  }
  return map[role] || 'info'
}

// ==================== 数据加载 ====================

/**
 * 加载用户列表数据
 */
async function loadUserList() {
  loading.value = true
  try {
    const res: any = await getUserList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      roleType: searchForm.roleType
    })
    // 后端返回 data.records 为列表，data.total 为总数
    userList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// ==================== 搜索与分页事件 ====================

/**
 * 搜索按钮：重置到第一页后加载数据
 */
function handleSearch() {
  pagination.pageNum = 1
  loadUserList()
}

/**
 * 重置按钮：清空搜索条件并重新加载
 */
function handleReset() {
  searchForm.roleType = ''
  searchForm.keyword = ''
  handleSearch()
}

/**
 * 每页条数变化
 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadUserList()
}

/**
 * 页码变化
 */
function handlePageChange(page: number) {
  pagination.pageNum = page
  loadUserList()
}

// ==================== 操作事件 ====================

/**
 * 打开编辑弹窗，并回填当前行数据
 * @param row 当前用户行数据
 */
function openEditDialog(row: any) {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status === 1 ? 1 : 0,
    roleType: row.roleType
  })
  editDialogVisible.value = true
}

/**
 * 提交编辑表单，更新用户信息
 */
async function handleEditSubmit() {
  try {
    await updateUser(editForm.id, {
      realName: editForm.realName,
      email: editForm.email,
      phone: editForm.phone,
      status: editForm.status,
      roleType: editForm.roleType
    })
    ElMessage.success('用户信息更新成功')
    editDialogVisible.value = false
    loadUserList()
  } catch (error) {
    ElMessage.error('更新用户信息失败')
    console.error(error)
  }
}

/**
 * 删除用户：二次确认后调用删除接口
 * @param row 当前用户行数据
 */
function handleDelete(row: any) {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await deleteUser(row.id)
        ElMessage.success('删除成功')
        loadUserList()
      } catch (error) {
        ElMessage.error('删除失败')
        console.error(error)
      }
    })
    .catch(() => {
      // 用户取消删除，无需提示
    })
}

/**
 * 打开重置密码弹窗
 * @param row 当前用户行数据
 */
function openResetPwdDialog(row: any) {
  resetPwdForm.id = row.id
  resetPwdForm.newPassword = ''
  resetPwdDialogVisible.value = true
}

/**
 * 提交重置密码表单
 */
async function handleResetPwdSubmit() {
  if (!resetPwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    await resetUserPassword(resetPwdForm.id, resetPwdForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPwdDialogVisible.value = false
    resetPwdForm.newPassword = ''
  } catch (error) {
    ElMessage.error('密码重置失败')
    console.error(error)
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-manage-page {
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
