<template>
  <div class="profile-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2>个人中心</h2>
      <p>查看并编辑您的个人资料，保持信息最新</p>
    </div>

    <!-- 用户信息卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card class="info-card" shadow="never">
          <div class="user-avatar">
            <!-- 头像占位：若用户有头像则显示，否则显示默认占位图标 -->
            <el-avatar :size="80" :src="userInfo.avatar || ''">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <div class="user-name">{{ userInfo.username || '-' }}</div>
          <div class="user-role">
            <el-tag :type="roleTagType(userInfo.roleType)" size="small">
              {{ roleText(userInfo.roleType) }}
            </el-tag>
          </div>
          <el-divider />
          <div class="user-meta">
            <div class="meta-item">
              <span class="meta-label">姓名</span>
              <span class="meta-value">{{ userInfo.realName || '未填写' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">邮箱</span>
              <span class="meta-value">{{ userInfo.email || '未填写' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">手机号</span>
              <span class="meta-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 编辑表单卡片 -->
      <el-col :xs="24" :md="16">
        <el-card class="form-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>编辑资料</span>
            </div>
          </template>
          <el-form :model="profileForm" label-width="120px">
            <!-- 姓名 -->
            <el-form-item label="姓名">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <!-- 邮箱 -->
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <!-- 手机号 -->
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <!-- 所属部门/学院 -->
            <el-form-item label="所属部门/学院">
              <el-input v-model="profileForm.department" placeholder="请输入所属部门或学院" />
            </el-form-item>
            <!-- 专业：通用字段 -->
            <el-form-item label="专业">
              <el-input v-model="profileForm.major" placeholder="请输入专业" />
            </el-form-item>

            <!-- 动态字段：根据当前用户角色显示不同表单项 -->
            <!-- 学生角色显示年级 -->
            <el-form-item v-if="userInfo.roleType === 'student'" label="年级">
              <el-input v-model="profileForm.grade" placeholder="请输入年级，如 2024级" />
            </el-form-item>
            <!-- 教师角色显示职称 -->
            <el-form-item v-if="userInfo.roleType === 'teacher'" label="职称">
              <el-input v-model="profileForm.title" placeholder="请输入职称，如 副教授" />
            </el-form-item>
            <!-- 企业角色显示企业名称 -->
            <el-form-item v-if="userInfo.roleType === 'enterprise'" label="企业名称">
              <el-input v-model="profileForm.companyName" placeholder="请输入企业名称" />
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item>
              <el-button type="primary" @click="handleSave">保存修改</el-button>
              <el-button @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getCurrentUser, updateCurrentUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

// ==================== Store 与状态 ====================

/**
 * 用户信息 Store，用于全局状态同步
 */
const userStore = useUserStore()

/**
 * 页面加载状态
 */
const loading = ref(false)

/**
 * 当前展示的用户信息（从 Store 或接口同步）
 */
const userInfo = reactive<any>({})

/**
 * 可编辑的表单数据
 */
const profileForm = reactive({
  realName: '',
  email: '',
  phone: '',
  department: '',
  major: '',
  grade: '',
  title: '',
  companyName: ''
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
 * 角色对应的 tag 类型
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
 * 加载当前登录用户信息
 * 1. 优先从后端接口获取最新数据
 * 2. 更新全局 userStore
 * 3. 同步到页面展示与表单
 */
async function loadUserInfo() {
  loading.value = true
  try {
    const res: any = await getCurrentUser()
    if (res.code === 200 && res.data) {
      // 更新全局 Store，保证导航栏等其它组件同步
      userStore.setUserInfo(res.data)
      // 同步到本地展示对象
      Object.assign(userInfo, res.data)
      // 回填编辑表单
      Object.assign(profileForm, {
        realName: res.data.realName || '',
        email: res.data.email || '',
        phone: res.data.phone || '',
        department: res.data.department || '',
        major: res.data.major || '',
        grade: res.data.grade || '',
        title: res.data.title || '',
        companyName: res.data.companyName || ''
      })
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

/**
 * 保存修改：提交当前表单数据到后端
 */
async function handleSave() {
  // 构建提交数据，仅包含后端允许的字段
  const submitData: Record<string, any> = {
    realName: profileForm.realName,
    email: profileForm.email,
    phone: profileForm.phone,
    department: profileForm.department,
    major: profileForm.major
  }

  // 根据角色补充动态字段
  if (userInfo.roleType === 'student') {
    submitData.grade = profileForm.grade
  } else if (userInfo.roleType === 'teacher') {
    submitData.title = profileForm.title
  } else if (userInfo.roleType === 'enterprise') {
    submitData.companyName = profileForm.companyName
  }

  loading.value = true
  try {
    const res: any = await updateCurrentUser(submitData)
    if (res.code === 200) {
      ElMessage.success('个人信息保存成功')
      // 重新拉取并刷新 Store 与页面数据
      await loadUserInfo()
    }
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

/**
 * 修改密码入口：当前仅做模拟提示
 */
function handleChangePassword() {
  ElMessage.info('修改密码功能正在开发中，敬请期待')
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-page {
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

  .info-card {
    margin-bottom: 20px;

    .user-avatar {
      display: flex;
      justify-content: center;
      margin-bottom: 16px;
    }

    .user-name {
      text-align: center;
      font-size: 20px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 8px;
    }

    .user-role {
      text-align: center;
      margin-bottom: 8px;
    }

    .user-meta {
      .meta-item {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid #ebeef5;
        font-size: 14px;

        &:last-child {
          border-bottom: none;
        }

        .meta-label {
          color: #909399;
        }

        .meta-value {
          color: #303133;
        }
      }
    }
  }

  .form-card {
    margin-bottom: 20px;

    .card-header {
      font-weight: 700;
      font-size: 16px;
    }
  }
}
</style>
