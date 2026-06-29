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
              <div class="stat-value">{{ dashboard.joinedProjects }}</div>
              <div class="stat-label">参与项目</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dashboard.learningResources + dashboard.completedLearning }}</div>
              <div class="stat-label">学习资源</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ dashboard.totalBookings }}</div>
              <div class="stat-label">资源借用</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ profileCompletion }}%</div>
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

          <!-- 我的主页：聚合看板 -->
          <div v-show="activeTab === 'dashboard'" class="tab-panel" v-loading="dashboardLoading">
            <div class="panel-header">
              <h3 class="panel-title">我的主页</h3>
              <p class="panel-desc">聚合您的科研项目、资源预约、学习进度与消息动态</p>
            </div>

            <!-- 数据概览卡片 -->
            <div class="dashboard-stats">
              <div class="dashboard-stat-card" @click="router.push('/app/research/my-projects')">
                <div class="stat-icon published"><el-icon :size="22"><Opportunity /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.publishedProjects }}</div>
                  <div class="stat-label">我发布的项目</div>
                </div>
              </div>
              <div class="dashboard-stat-card" @click="router.push('/app/research/my-projects')">
                <div class="stat-icon joined"><el-icon :size="22"><User /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.joinedProjects }}</div>
                  <div class="stat-label">我加入的项目</div>
                </div>
              </div>
              <div class="dashboard-stat-card" @click="router.push('/app/research/applications')">
                <div class="stat-icon application"><el-icon :size="22"><CollectionTag /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.pendingApplications }}</div>
                  <div class="stat-label">待处理申请</div>
                </div>
              </div>
              <div class="dashboard-stat-card" @click="router.push('/app/resource/bookings')">
                <div class="stat-icon booking"><el-icon :size="22"><Calendar /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.pendingBookings }}</div>
                  <div class="stat-label">待处理预约</div>
                </div>
              </div>
              <div class="dashboard-stat-card" @click="router.push('/app/learning/center')">
                <div class="stat-icon learning"><el-icon :size="22"><Star /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.learningResources }}</div>
                  <div class="stat-label">学习中资源</div>
                </div>
              </div>
              <div class="dashboard-stat-card" @click="router.push('/app/notifications')">
                <div class="stat-icon notification"><el-icon :size="22"><Bell /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ dashboard.unreadNotifications }}</div>
                  <div class="stat-label">未读消息</div>
                </div>
              </div>
            </div>

            <!-- 信用分与资料完整度 -->
            <div class="dashboard-extra">
              <div class="extra-card credit-card">
                <div class="extra-title">信用分</div>
                <div class="credit-score" :class="creditScoreClass">{{ dashboard.creditScore }}</div>
                <div class="extra-desc">按时归还资源、遵守约定可提升信用分</div>
              </div>
              <div class="extra-card completion-card">
                <div class="extra-title">资料完整度</div>
                <el-progress :percentage="profileCompletion" :stroke-width="12" :status="profileCompletion >= 80 ? 'success' : ''" />
                <div class="extra-desc">完善资料可获得更精准的推荐</div>
              </div>
            </div>

            <!-- 快捷入口 -->
            <div class="dashboard-links">
              <div class="links-title">快捷入口</div>
              <div class="links-list">
                <el-button plain @click="router.push('/app/research/my-projects')">我的项目</el-button>
                <el-button plain @click="router.push('/app/research/applications')">我的申请</el-button>
                <el-button plain @click="router.push('/app/resource/bookings')">我的预约</el-button>
                <el-button plain @click="router.push('/app/learning/center')">学习中心</el-button>
                <el-button plain @click="router.push('/app/notifications')">消息通知</el-button>
                <el-button plain @click="activeTab = 'researchProfile'">科研画像</el-button>
              </div>
            </div>
          </div>

          <!-- 基础资料 -->
          <div v-show="activeTab === 'basic'" class="tab-panel" v-loading="loading">
            <div class="panel-header">
              <h3 class="panel-title">编辑基础资料</h3>
              <p class="panel-desc">完善个人信息，让合作方更了解您的学术背景</p>
            </div>
            <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="120px" class="profile-form">
              <el-form-item label="头像" prop="avatar">
                <ImageUpload v-model="profileForm.avatar" class="avatar-upload" />
                <div class="form-tip">支持 jpg、png、gif、webp 格式，建议上传正方形图片</div>
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" maxlength="50" show-word-limit />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" maxlength="100" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="20" />
              </el-form-item>
              <el-form-item label="所属部门/学院" prop="department">
                <el-input v-model="profileForm.department" placeholder="请输入所属部门或学院" maxlength="100" />
              </el-form-item>
              <el-form-item label="专业" prop="major">
                <el-input v-model="profileForm.major" placeholder="请输入专业方向" maxlength="100" />
              </el-form-item>

              <!-- 角色专属字段 -->
              <el-form-item v-if="userInfo.roleType === 'student'" label="年级" prop="grade">
                <el-input v-model="profileForm.grade" placeholder="如：2024级" maxlength="50" />
              </el-form-item>
              <el-form-item v-if="userInfo.roleType === 'teacher'" label="职称" prop="title">
                <el-input v-model="profileForm.title" placeholder="如：副教授" maxlength="50" />
              </el-form-item>
              <el-form-item v-if="userInfo.roleType === 'enterprise'" label="企业名称" prop="companyName">
                <el-input v-model="profileForm.companyName" placeholder="请输入企业名称" maxlength="100" />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" size="large" class="save-btn" @click="handleSave">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 科研画像 -->
          <div v-show="activeTab === 'researchProfile'" class="tab-panel" v-loading="loading">
            <div class="panel-header">
              <h3 class="panel-title">编辑科研画像</h3>
              <p class="panel-desc">完善科研背景、技能与兴趣，让 AI 推荐更精准地匹配科研项目</p>
            </div>
            <el-form
              ref="researchFormRef"
              :model="researchForm"
              :rules="researchRules"
              label-position="top"
              class="profile-form"
            >
              <el-form-item label="研究方向" prop="researchDirections">
                <el-input
                  v-model="researchForm.researchDirections"
                  type="textarea"
                  :rows="3"
                  placeholder="请填写您的研究方向，如：深度学习、自然语言处理、计算机视觉等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="专业技能" prop="skills">
                <el-input
                  v-model="researchForm.skills"
                  type="textarea"
                  :rows="3"
                  placeholder="请填写您掌握的专业技能，如：Python、PyTorch、Java、数据分析等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="科研成果" prop="publications">
                <el-input
                  v-model="researchForm.publications"
                  type="textarea"
                  :rows="3"
                  placeholder="请填写您的论文、专利、获奖等科研成果"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="项目经历" prop="projectExperience">
                <el-input
                  v-model="researchForm.projectExperience"
                  type="textarea"
                  :rows="3"
                  placeholder="请填写您参与过的科研项目或工程实践经历"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="研究兴趣" prop="researchInterests">
                <el-input
                  v-model="researchForm.researchInterests"
                  type="textarea"
                  :rows="3"
                  placeholder="请填写您感兴趣的研究课题或合作方向"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="可投入时间" prop="availability">
                    <el-select v-model="researchForm.availability" placeholder="请选择可投入时间" style="width: 100%">
                      <el-option label="每周 1-5 小时" value="每周 1-5 小时" />
                      <el-option label="每周 5-10 小时" value="每周 5-10 小时" />
                      <el-option label="每周 10-20 小时" value="每周 10-20 小时" />
                      <el-option label="每周 20 小时以上" value="每周 20 小时以上" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="合作意向" prop="cooperationIntention">
                    <el-select v-model="researchForm.cooperationIntention" placeholder="请选择合作意向" style="width: 100%">
                      <el-option label="仅校内合作" value="仅校内合作" />
                      <el-option label="校企联合" value="校企联合" />
                      <el-option label="开放合作" value="开放合作" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-button type="primary" size="large" class="save-btn" :loading="researchSubmitting" @click="handleSaveResearchProfile">
                  <el-icon><Check /></el-icon>
                  保存画像
                </el-button>
                <el-button size="large" @click="handleResetResearchProfile">重置</el-button>
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
                <el-button type="primary" plain @click="openSecurityDialog('password')">修改密码</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">
                  <el-icon :size="22"><Message /></el-icon>
                </div>
                <div class="security-content">
                  <div class="security-title">邮箱绑定</div>
                  <div class="security-desc">当前邮箱：{{ userInfo.email || '未绑定' }}</div>
                </div>
                <el-button text type="primary" @click="openSecurityDialog('email')">更换邮箱</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">
                  <el-icon :size="22"><Iphone /></el-icon>
                </div>
                <div class="security-content">
                  <div class="security-title">手机绑定</div>
                  <div class="security-desc">当前手机：{{ userInfo.phone || '未绑定' }}</div>
                </div>
                <el-button text type="primary" @click="openSecurityDialog('phone')">更换手机</el-button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 安全设置弹窗：修改密码 / 更换邮箱 / 更换手机 -->
    <el-dialog
      v-model="securityDialogVisible"
      :title="securityDialogTitle"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="resetSecurityForm"
    >
      <el-form
        ref="securityFormRef"
        :model="securityForm"
        :rules="securityRules"
        label-width="110px"
        class="security-form"
      >
        <!-- 修改密码表单 -->
        <template v-if="securityDialogType === 'password'">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="securityForm.oldPassword"
              type="password"
              placeholder="请输入当前登录密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="securityForm.newPassword"
              type="password"
              placeholder="请输入 6-32 位新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="securityForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </template>

        <!-- 更换邮箱表单 -->
        <template v-if="securityDialogType === 'email'">
          <el-form-item label="登录密码" prop="password">
            <el-input
              v-model="securityForm.password"
              type="password"
              placeholder="请输入当前登录密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新邮箱" prop="email">
            <el-input
              v-model="securityForm.email"
              placeholder="请输入新邮箱地址"
              maxlength="100"
            />
          </el-form-item>
        </template>

        <!-- 更换手机表单 -->
        <template v-if="securityDialogType === 'phone'">
          <el-form-item label="登录密码" prop="password">
            <el-input
              v-model="securityForm.password"
              type="password"
              placeholder="请输入当前登录密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新手机号" prop="phone">
            <el-input
              v-model="securityForm.phone"
              placeholder="请输入新手机号"
              maxlength="11"
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="securityDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="securitySubmitting" @click="handleSecuritySubmit">
            确认{{ securityDialogTitle }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  Check,
  Clock,
  Lock,
  Message,
  Iphone,
  User,
  Setting,
  Collection,
  HomeFilled,
  Opportunity,
  Calendar,
  CollectionTag,
  Bell,
  Star
} from '@element-plus/icons-vue'
import { getCurrentUser, updateCurrentUser, changePassword, changeEmail, changePhone } from '@/api/auth'
import { getResearcherProfile, saveResearcherProfile, getMyProjects, getJoinedProjects, getMyApplications } from '@/api/research'
import { getMyBookings } from '@/api/resource'
import { getMyLearningRecords } from '@/api/learning'
import { getUnreadCount } from '@/api/notification'
import { useUserStore } from '@/stores/user'

// ==================== Store 与状态 ====================
const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('dashboard')

const userInfo = reactive<any>({})

// 我的主页聚合看板数据
const dashboardLoading = ref(false)
const dashboard = reactive({
  publishedProjects: 0,
  joinedProjects: 0,
  pendingApplications: 0,
  totalApplications: 0,
  pendingBookings: 0,
  totalBookings: 0,
  learningResources: 0,
  completedLearning: 0,
  unreadNotifications: 0,
  creditScore: 100
})

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

const profileFormRef = ref<any>(null)

// 基础资料表单校验规则
const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  department: [{ required: true, message: '请输入所属部门或学院', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业方向', trigger: 'blur' }]
}

// 科研画像表单
// 安全设置弹窗状态
const securityDialogVisible = ref(false)
const securityDialogType = ref<'password' | 'email' | 'phone'>('password')
const securityDialogTitle = computed(() => {
  const titles = {
    password: '修改密码',
    email: '更换邮箱',
    phone: '更换手机'
  }
  return titles[securityDialogType.value]
})
const securitySubmitting = ref(false)
const securityFormRef = ref<any>(null)
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  password: '',
  email: '',
  phone: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== securityForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度应在 6-32 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const emailRules = {
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const phoneRules = {
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const securityRules = computed(() => {
  if (securityDialogType.value === 'password') return passwordRules
  if (securityDialogType.value === 'email') return emailRules
  return phoneRules
})

const researchFormRef = ref<any>(null)
const researchSubmitting = ref(false)
const researchForm = reactive({
  userId: 0,
  researchDirections: '',
  skills: '',
  publications: '',
  projectExperience: '',
  researchInterests: '',
  availability: '',
  cooperationIntention: ''
})

const researchRules = {
  researchDirections: [{ required: true, message: '请填写研究方向', trigger: 'blur' }],
  skills: [{ required: true, message: '请填写专业技能', trigger: 'blur' }]
}

const tabs = [
  { key: 'dashboard', label: '我的主页', icon: 'HomeFilled' },
  { key: 'basic', label: '基础资料', icon: 'User' },
  { key: 'researchProfile', label: '科研画像', icon: 'Collection' },
  { key: 'security', label: '安全设置', icon: 'Setting' }
]

const lastUpdateText = computed(() => {
  return new Date().toLocaleDateString('zh-CN')
})

/**
 * 资料完整度：基于基础资料关键字段计算百分比
 */
const profileCompletion = computed(() => {
  const fields = ['avatar', 'realName', 'email', 'phone', 'department', 'major']
  const filled = fields.filter((field) => {
    const value = profileForm[field as keyof typeof profileForm]
    return value && String(value).trim() !== ''
  }).length
  return Math.round((filled / fields.length) * 100)
})

/**
 * 信用分样式：根据分数区间返回不同颜色类
 */
const creditScoreClass = computed(() => {
  const score = dashboard.creditScore
  if (score >= 90) return 'excellent'
  if (score >= 70) return 'good'
  if (score >= 60) return 'normal'
  return 'poor'
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
  if (!profileFormRef.value) return

  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

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

function openSecurityDialog(type: 'password' | 'email' | 'phone') {
  securityDialogType.value = type
  securityDialogVisible.value = true
  resetSecurityForm()
  nextTick(() => {
    securityFormRef.value?.clearValidate?.()
  })
}

function resetSecurityForm() {
  securityForm.oldPassword = ''
  securityForm.newPassword = ''
  securityForm.confirmPassword = ''
  securityForm.password = ''
  securityForm.email = ''
  securityForm.phone = ''
}

async function handleSecuritySubmit() {
  if (!securityFormRef.value) return

  const valid = await securityFormRef.value.validate().catch(() => false)
  if (!valid) return

  securitySubmitting.value = true
  try {
    let res: any
    if (securityDialogType.value === 'password') {
      res = await changePassword({
        oldPassword: securityForm.oldPassword,
        newPassword: securityForm.newPassword,
        confirmPassword: securityForm.confirmPassword
      })
    } else if (securityDialogType.value === 'email') {
      res = await changeEmail({
        password: securityForm.password,
        email: securityForm.email
      })
    } else {
      res = await changePhone({
        password: securityForm.password,
        phone: securityForm.phone
      })
    }

    if (res.code === 200) {
      ElMessage.success(`${securityDialogTitle.value}成功`)
      securityDialogVisible.value = false
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || `${securityDialogTitle.value}失败`)
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || `${securityDialogTitle.value}失败`)
    console.error(error)
  } finally {
    securitySubmitting.value = false
  }
}

// ==================== 科研画像加载与保存 ====================
async function loadResearchProfile() {
  const userId = userStore.userInfo?.id
  if (!userId) return

  try {
    const res: any = await getResearcherProfile(userId)
    if (res.data) {
      Object.assign(researchForm, res.data)
    } else {
      researchForm.userId = userId
    }
  } catch (error) {
    ElMessage.error('加载科研画像失败')
    console.error(error)
  }
}

async function handleSaveResearchProfile() {
  if (!researchFormRef.value) return

  await researchFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    researchSubmitting.value = true
    try {
      const submitData = {
        ...researchForm,
        userId: userStore.userInfo?.id || 0
      }
      const res: any = await saveResearcherProfile(submitData)
      if (res.code === 200) {
        ElMessage.success('科研画像保存成功')
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      ElMessage.error('保存失败，请稍后重试')
      console.error(error)
    } finally {
      researchSubmitting.value = false
    }
  })
}

function handleResetResearchProfile() {
  loadResearchProfile()
}

/**
 * 加载聚合看板数据
 * 并发请求各模块接口，失败时回退为 0，避免单个接口异常影响整个看板
 */
async function loadDashboard() {
  if (!userStore.userInfo?.id) return
  dashboardLoading.value = true

  try {
    const [
      publishedRes,
      joinedRes,
      applicationsRes,
      bookingsRes,
      learningRes,
      unreadRes
    ] = await Promise.allSettled([
      getMyProjects({ pageNum: 1, pageSize: 1 }),
      getJoinedProjects({ pageNum: 1, pageSize: 1 }),
      getMyApplications(),
      getMyBookings(userStore.userInfo.id),
      getMyLearningRecords(),
      getUnreadCount()
    ])

    dashboard.publishedProjects = extractTotal(publishedRes)
    dashboard.joinedProjects = extractTotal(joinedRes)

    const applications = extractList(applicationsRes)
    dashboard.totalApplications = applications.length
    dashboard.pendingApplications = applications.filter((item: any) => item.status === 'pending' || item.status === 'interview').length

    const bookings = extractList(bookingsRes)
    dashboard.totalBookings = bookings.length
    dashboard.pendingBookings = bookings.filter((item: any) => item.status === 'pending').length

    const learningRecords = extractList(learningRes)
    dashboard.learningResources = learningRecords.filter((item: any) => item.status === 'learning').length
    dashboard.completedLearning = learningRecords.filter((item: any) => item.status === 'completed').length

    dashboard.unreadNotifications = extractValue(unreadRes, 0)

    // 信用分优先使用后端返回的 creditScore，否则默认 100
    dashboard.creditScore = userStore.userInfo?.creditScore ?? userInfo.creditScore ?? 100
  } catch (error) {
    console.error('加载聚合看板失败', error)
  } finally {
    dashboardLoading.value = false
  }
}

/**
 * 从 Promise.allSettled 结果中提取分页 total
 */
function extractTotal(result: PromiseSettledResult<any>) {
  if (result.status === 'fulfilled' && result.value?.code === 200) {
    return result.value.data?.total ?? 0
  }
  return 0
}

/**
 * 从 Promise.allSettled 结果中提取列表数据
 */
function extractList(result: PromiseSettledResult<any>) {
  if (result.status === 'fulfilled' && result.value?.code === 200) {
    return Array.isArray(result.value.data) ? result.value.data : (result.value.data?.records || [])
  }
  return []
}

/**
 * 从 Promise.allSettled 结果中提取单个数值
 */
function extractValue(result: PromiseSettledResult<any>, defaultValue: any) {
  if (result.status === 'fulfilled' && result.value?.code === 200) {
    return result.value.data ?? defaultValue
  }
  return defaultValue
}

onMounted(() => {
  loadUserInfo().then(() => {
    loadResearchProfile()
    loadDashboard()
  })
})
</script>

<script lang="ts">
export default {
  components: {
    User,
    Setting,
    Collection,
    HomeFilled,
    Opportunity,
    Calendar,
    CollectionTag,
    Bell,
    Star
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

// ==================== 我的主页聚合看板 ====================
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--zh-space-4);
  margin-bottom: var(--zh-space-6);
}

.dashboard-stat-card {
  display: flex;
  align-items: center;
  gap: var(--zh-space-4);
  padding: var(--zh-space-5);
  background: var(--zh-bg-warm);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    border-color: var(--zh-border);
    box-shadow: var(--zh-shadow-sm);
    transform: translateY(-2px);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--zh-radius-sm);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;

    &.published { background: var(--zh-primary); }
    &.joined { background: var(--zh-accent); }
    &.application { background: var(--zh-success); }
    &.booking { background: var(--zh-info); }
    &.learning { background: var(--zh-warning); }
    &.notification { background: var(--zh-danger); }
  }

  .stat-info {
    .stat-value {
      font-family: var(--zh-font-display);
      font-size: 24px;
      font-weight: 700;
      color: var(--zh-text-primary);
      line-height: 1.2;
    }

    .stat-label {
      font-size: 13px;
      color: var(--zh-text-secondary);
      margin-top: 2px;
    }
  }
}

.dashboard-extra {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--zh-space-4);
  margin-bottom: var(--zh-space-6);
}

.extra-card {
  padding: var(--zh-space-5);
  background: var(--zh-bg-warm);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);

  .extra-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--zh-text-secondary);
    margin-bottom: var(--zh-space-3);
  }

  .credit-score {
    font-family: var(--zh-font-display);
    font-size: 36px;
    font-weight: 700;
    margin-bottom: var(--zh-space-2);

    &.excellent { color: var(--zh-success); }
    &.good { color: var(--zh-primary); }
    &.normal { color: var(--zh-warning); }
    &.poor { color: var(--zh-danger); }
  }

  .extra-desc {
    font-size: 13px;
    color: var(--zh-text-tertiary);
  }
}

.dashboard-links {
  padding: var(--zh-space-5);
  background: var(--zh-bg-warm);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);

  .links-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--zh-text-secondary);
    margin-bottom: var(--zh-space-3);
  }

  .links-list {
    display: flex;
    flex-wrap: wrap;
    gap: var(--zh-space-3);
  }
}

@media (max-width: 992px) {
  .dashboard-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-extra {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .dashboard-stats {
    grid-template-columns: 1fr;
  }
}
</style>
