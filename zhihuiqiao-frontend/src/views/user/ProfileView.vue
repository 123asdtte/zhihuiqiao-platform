<template>
  <div class="profile-page zh-page">
    <!-- 页面标题区：非对称布局，左侧标题右侧元信息 -->
    <div class="page-header-section">
      <div class="page-header-content">
        <h1 class="zh-page-title">个人中心</h1>
        <p class="zh-page-subtitle">管理您的学术身份、联系方式与账户偏好</p>
      </div>
      <div class="page-header-meta">
        <span class="meta-pill">
          <el-icon><Clock /></el-icon>
          最后更新：{{ lastUpdateText }}
        </span>
      </div>
    </div>

    <!-- 顶部横幅：渐变背景 + 头像信息 -->
    <div class="profile-hero">
      <div class="hero-backdrop"></div>
      <div class="hero-content">
        <div class="hero-avatar">
          <el-avatar :size="96" :src="userInfo.avatar || ''" class="avatar-ring">
            <el-icon :size="44"><UserFilled /></el-icon>
          </el-avatar>
          <div class="avatar-badge" :class="userInfo.roleType">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="hero-text">
          <h2 class="hero-name">{{ userInfo.realName || userInfo.username || '用户' }}</h2>
          <p class="hero-id">@{{ userInfo.username || '-' }} · {{ roleText(userInfo.roleType) }}</p>
        </div>
      </div>
      <div class="hero-decoration">
        <span class="deco-line"></span>
        <span class="deco-dot"></span>
      </div>
    </div>

    <!-- 主内容区：左侧信息卡 + 右侧编辑表单 -->
    <div class="profile-layout">
      <!-- 左侧边栏 -->
      <aside class="profile-sidebar">
        <div class="sidebar-card info-card">
          <h3 class="sidebar-title">账户概览</h3>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo.username || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">真实姓名</span>
              <span class="info-value">{{ userInfo.realName || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userInfo.email || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">所属部门</span>
              <span class="info-value">{{ userInfo.department || '未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">专业</span>
              <span class="info-value">{{ userInfo.major || '未填写' }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card stat-card">
          <h3 class="sidebar-title">学术足迹</h3>
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.roleType === 'student' ? '3' : '5' }}</div>
              <div class="stat-label">参与项目</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.roleType === 'student' ? '12' : '8' }}</div>
              <div class="stat-label">学习资源</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">2</div>
              <div class="stat-label">资源借用</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">98%</div>
              <div class="stat-label">资料完整度</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧表单 -->
      <main class="profile-main">
        <div class="main-card">
          <div class="card-tabs">
            <div
              v-for="tab in tabs"
              :key="tab.key"
              class="card-tab"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              <el-icon :size="18"><component :is="tab.icon" /></el-icon>
              <span>{{ tab.label }}</span>
            </div>
          </div>

          <!-- 基础资料 -->
          <div v-show="activeTab === 'basic'" class="tab-panel" v-loading="loading">
            <div class="panel-header">
              <h3 class="panel-title">编辑基础资料</h3>
              <p class="panel-desc">完善个人信息，让合作方更了解您的学术背景</p>
            </div>
            <el-form :model="profileForm" label-width="120px" class="profile-form">
              <el-form-item label="头像">
                <ImageUpload v-model="profileForm.avatar" class="avatar-upload" />
                <div class="form-tip">支持 jpg、png、gif、webp 格式，建议上传正方形图片</div>
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="所属部门/学院">
                <el-input v-model="profileForm.department" placeholder="请输入所属部门或学院" />
              </el-form-item>
              <el-form-item label="专业">
                <el-input v-model="profileForm.major" placeholder="请输入专业方向" />
              </el-form-item>

              <!-- 角色专属字段 -->
              <el-form-item v-if="userInfo.roleType === 'student'" label="年级">
                <el-input v-model="profileForm.grade" placeholder="如：2024级" />
              </el-form-item>
              <el-form-item v-if="userInfo.roleType === 'teacher'" label="职称">
                <el-input v-model="profileForm.title" placeholder="如：副教授" />
              </el-form-item>
              <el-form-item v-if="userInfo.roleType === 'enterprise'" label="企业名称">
                <el-input v-model="profileForm.companyName" placeholder="请输入企业名称" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" size="large" class="save-btn" @click="handleSave">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-show="activeTab === 'security'" class="tab-panel">
            <div class="panel-header">
              <h3 class="panel-title">账户安全</h3>
              <p class="panel-desc">管理密码与登录安全选项</p>
            </div>
            <div class="security-list">
              <div class="security-item">
                <div class="security-icon">
                  <el-icon :size="22"><Lock /></el-icon>
                </div>
                <div class="security-content">
                  <div class="security-title">登录密码</div>
                  <div class="security-desc">定期修改密码可以更好地保护您的账户安全</div>
                </div>
                <el-button type="primary" plain @click="handleChangePassword">修改密码</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">
                  <el-icon :size="22"><Message /></el-icon>
                </div>
                <div class="security-content">
                  <div class="security-title">邮箱绑定</div>
                  <div class="security-desc">当前邮箱：{{ userInfo.email || '未绑定' }}</div>
                </div>
                <el-button text type="primary">更换邮箱</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">
                  <el-icon :size="22"><Iphone /></el-icon>
                </div>
                <div class="security-content">
                  <div class="security-title">手机绑定</div>
                  <div class="security-desc">当前手机：{{ userInfo.phone || '未绑定' }}</div>
                </div>
                <el-button text type="primary">更换手机</el-button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  Check,
  Clock,
  Lock,
  Message,
  Iphone,
  User,
  Setting
} from '@element-plus/icons-vue'
import { getCurrentUser, updateCurrentUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

// ==================== Store 与状态 ====================
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('basic')

const userInfo = reactive<any>({})

const profileForm = reactive({
  avatar: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  major: '',
  grade: '',
  title: '',
  companyName: ''
})

const tabs = [
  { key: 'basic', label: '基础资料', icon: 'User' },
  { key: 'security', label: '安全设置', icon: 'Setting' }
]

const lastUpdateText = computed(() => {
  return new Date().toLocaleDateString('zh-CN')
})

// ==================== 辅助函数 ====================
function roleText(role: string) {
  const map: Record<string, string> = {
    student: '学生用户',
    teacher: '教师用户',
    enterprise: '企业用户',
    admin: '系统管理员'
  }
  return map[role] || role
}

// ==================== 数据加载 ====================
async function loadUserInfo() {
  loading.value = true
  try {
    const res: any = await getCurrentUser()
    if (res.code === 200 && res.data) {
      userStore.setUserInfo(res.data)
      Object.assign(userInfo, res.data)
      Object.assign(profileForm, {
        avatar: res.data.avatar || '',
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
async function handleSave() {
  const submitData: Record<string, any> = {
    avatar: profileForm.avatar,
    realName: profileForm.realName,
    email: profileForm.email,
    phone: profileForm.phone,
    department: profileForm.department,
    major: profileForm.major
  }

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
      await loadUserInfo()
    }
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleChangePassword() {
  ElMessage.info('修改密码功能正在开发中，敬请期待')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<script lang="ts">
export default {
  components: {
    User,
    Setting
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  padding-bottom: var(--zh-space-12);
}

// ==================== 页面标题区 ====================
.page-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--zh-space-6);
  gap: var(--zh-space-4);
}

.page-header-meta {
  .meta-pill {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-2);
    padding: var(--zh-space-2) var(--zh-space-4);
    border-radius: 100px;
    font-size: 13px;
    color: var(--zh-text-secondary);
    background: var(--zh-bg-elevated);
    border: 1px solid var(--zh-border-light);
    box-shadow: var(--zh-shadow-sm);
  }
}

// ==================== Hero 横幅 ====================
.profile-hero {
  position: relative;
  border-radius: var(--zh-radius-lg);
  overflow: hidden;
  margin-bottom: var(--zh-space-6);
  background: linear-gradient(135deg, var(--zh-primary) 0%, var(--zh-secondary) 100%);
  box-shadow: var(--zh-shadow-md);
  padding: var(--zh-space-8);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .hero-backdrop {
    position: absolute;
    inset: 0;
    opacity: 0.15;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }

  .hero-content {
    position: relative;
    display: flex;
    align-items: center;
    gap: var(--zh-space-6);
  }

  .hero-avatar {
    position: relative;

    .avatar-ring {
      border: 4px solid rgba(255, 255, 255, 0.9);
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
      box-shadow: var(--zh-shadow-lg);
    }

    .avatar-badge {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--zh-success);
      border: 2px solid #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;

      &.admin { background: var(--zh-danger); }
      &.teacher { background: var(--zh-accent); }
      &.enterprise { background: var(--zh-info); }
    }
  }

  .hero-text {
    color: #fff;

    .hero-name {
      font-family: var(--zh-font-display);
      font-size: 28px;
      font-weight: 700;
      margin: 0 0 var(--zh-space-2) 0;
      letter-spacing: -0.02em;
    }

    .hero-id {
      margin: 0;
      font-size: 15px;
      opacity: 0.85;
    }
  }

  .hero-decoration {
    position: relative;
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);

    .deco-line {
      width: 80px;
      height: 2px;
      background: rgba(255, 255, 255, 0.4);
    }

    .deco-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: var(--zh-accent);
      box-shadow: 0 0 0 4px rgba(201, 162, 39, 0.3);
    }
  }
}

// ==================== 主内容布局 ====================
.profile-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: var(--zh-space-6);
  align-items: start;
}

.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-5);
  position: sticky;
  top: var(--zh-space-6);
}

.sidebar-card {
  background: var(--zh-bg-elevated);
  border-radius: var(--zh-radius);
  border: 1px solid var(--zh-border-light);
  box-shadow: var(--zh-shadow);
  padding: var(--zh-space-5);

  .sidebar-title {
    font-family: var(--zh-font-display);
    font-size: 16px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-4) 0;
    padding-bottom: var(--zh-space-3);
    border-bottom: 1px solid var(--zh-border-light);
  }
}

.info-card {
  .info-list {
    display: flex;
    flex-direction: column;
    gap: var(--zh-space-3);
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;

    .info-label {
      color: var(--zh-text-tertiary);
      flex-shrink: 0;
    }

    .info-value {
      color: var(--zh-text-primary);
      font-weight: 500;
      text-align: right;
      word-break: break-all;
      margin-left: var(--zh-space-3);
    }
  }
}

.stat-card {
  .stat-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--zh-space-3);
  }

  .stat-item {
    background: var(--zh-bg-warm);
    border-radius: var(--zh-radius-sm);
    padding: var(--zh-space-4);
    text-align: center;
    border: 1px solid var(--zh-border-light);
    transition: transform var(--zh-transition-fast);

    &:hover {
      transform: translateY(-2px);
    }

    .stat-value {
      font-family: var(--zh-font-display);
      font-size: 22px;
      font-weight: 700;
      color: var(--zh-primary);
      margin-bottom: var(--zh-space-1);
    }

    .stat-label {
      font-size: 12px;
      color: var(--zh-text-secondary);
    }
  }
}

// ==================== 右侧表单卡片 ====================
.profile-main {
  min-width: 0;
}

.main-card {
  background: var(--zh-bg-elevated);
  border-radius: var(--zh-radius);
  border: 1px solid var(--zh-border-light);
  box-shadow: var(--zh-shadow);
  overflow: hidden;
}

.card-tabs {
  display: flex;
  border-bottom: 1px solid var(--zh-border-light);
  background: var(--zh-bg-warm);
  padding: var(--zh-space-2);
  gap: var(--zh-space-1);
}

.card-tab {
  display: flex;
  align-items: center;
  gap: var(--zh-space-2);
  padding: var(--zh-space-3) var(--zh-space-5);
  border-radius: var(--zh-radius-sm);
  font-size: 15px;
  font-weight: 500;
  color: var(--zh-text-secondary);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    color: var(--zh-primary);
    background: var(--zh-primary-soft);
  }

  &.active {
    color: var(--zh-primary);
    background: var(--zh-bg-elevated);
    box-shadow: var(--zh-shadow-sm);
    font-weight: 600;
  }
}

.tab-panel {
  padding: var(--zh-space-6);
  animation: fadeIn 0.3s ease;
}

.panel-header {
  margin-bottom: var(--zh-space-6);

  .panel-title {
    font-family: var(--zh-font-display);
    font-size: 20px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-2) 0;
  }

  .panel-desc {
    margin: 0;
    color: var(--zh-text-secondary);
    font-size: 14px;
  }
}

.profile-form {
  max-width: 560px;

  .save-btn {
    min-width: 140px;
  }

  .avatar-upload {
    :deep(.el-upload) {
      border-radius: 50%;
      border-style: solid;
    }

    :deep(.uploader-trigger),
    :deep(.image-preview) {
      width: 96px;
      height: 96px;
      border-radius: 50%;
    }

    :deep(.preview-img) {
      border-radius: 50%;
    }
  }

  .form-tip {
    font-size: 12px;
    color: var(--zh-text-tertiary);
    margin-top: var(--zh-space-1);
  }
}

// ==================== 安全设置 ====================
.security-list {
  display: flex;
  flex-direction: column;
  gap: var(--zh-space-4);
  max-width: 720px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
  padding: var(--zh-space-5);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  background: var(--zh-bg-warm);
  transition: all var(--zh-transition-fast);

  &:hover {
    border-color: var(--zh-border);
    box-shadow: var(--zh-shadow-sm);
  }

  .security-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--zh-radius-sm);
    background: var(--zh-primary-soft);
    color: var(--zh-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .security-content {
    flex: 1;
    min-width: 0;

    .security-title {
      font-size: 15px;
      font-weight: 600;
      color: var(--zh-text-primary);
      margin-bottom: var(--zh-space-1);
    }

    .security-desc {
      font-size: 13px;
      color: var(--zh-text-secondary);
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .profile-layout {
    grid-template-columns: 280px 1fr;
  }
}

@media (max-width: 992px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .page-header-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--zh-space-4);

    .hero-decoration {
      display: none;
    }
  }

  .profile-sidebar {
    grid-template-columns: 1fr;
  }

  .card-tabs {
    overflow-x: auto;
  }
}
</style>
