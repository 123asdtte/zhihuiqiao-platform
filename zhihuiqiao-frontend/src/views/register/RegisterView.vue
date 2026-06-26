<template>
  <div class="register-view">
    <div class="form-header">
      <h2 class="form-title">创建账户</h2>
      <p class="form-subtitle">加入智汇桥，连接产学研用无限可能</p>
    </div>

    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      label-position="top"
      size="large"
      class="register-form"
      @keyup.enter="handleRegister"
    >
      <!-- 账户信息 -->
      <div class="form-section">
        <h3 class="section-title">
          <span class="section-marker"></span>
          账户信息
        </h3>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
      </div>

      <!-- 角色选择 -->
      <div class="form-section">
        <h3 class="section-title">
          <span class="section-marker"></span>
          选择身份
        </h3>

        <el-form-item prop="roleType">
          <div class="role-cards">
            <div
              v-for="role in roles"
              :key="role.value"
              class="role-card"
              :class="{ active: registerForm.roleType === role.value }"
              @click="registerForm.roleType = role.value"
            >
              <el-icon :size="24"><component :is="role.icon" /></el-icon>
              <span class="role-name">{{ role.label }}</span>
            </div>
          </div>
        </el-form-item>

        <!-- 学生特有字段 -->
        <template v-if="registerForm.roleType === 'student'">
          <el-form-item label="院系" prop="department">
            <el-input v-model="registerForm.department" placeholder="请输入院系" clearable />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="registerForm.major" placeholder="请输入专业" clearable />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-input v-model="registerForm.grade" placeholder="例如：2022级" clearable />
          </el-form-item>
        </template>

        <!-- 教师特有字段 -->
        <template v-if="registerForm.roleType === 'teacher'">
          <el-form-item label="院系" prop="department">
            <el-input v-model="registerForm.department" placeholder="请输入院系" clearable />
          </el-form-item>
          <el-form-item label="职称" prop="title">
            <el-input v-model="registerForm.title" placeholder="例如：副教授" clearable />
          </el-form-item>
        </template>

        <!-- 企业特有字段 -->
        <template v-if="registerForm.roleType === 'enterprise'">
          <el-form-item label="企业名称" prop="companyName">
            <el-input v-model="registerForm.companyName" placeholder="请输入企业名称" clearable />
          </el-form-item>
        </template>
      </div>

      <el-form-item>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="handleRegister"
        >
          注 册
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-footer">
      <p>已有账号？<el-button link type="primary" @click="goToLogin">立即登录</el-button></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Reading,
  User as TeacherIcon,
  OfficeBuilding
} from '@element-plus/icons-vue'
import { register } from '@/api/auth'

const router = useRouter()

// 注册表单（个人资料中的真实姓名、邮箱、手机号改为非必填，注册时无需填写）
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  roleType: '',
  department: '',
  major: '',
  grade: '',
  title: '',
  companyName: ''
})

const roles = [
  { label: '学生', value: 'student', icon: 'Reading' },
  { label: '教师', value: 'teacher', icon: 'User' },
  { label: '企业', value: 'enterprise', icon: 'OfficeBuilding' }
]

// 自定义校验：确认密码是否与密码一致
function validateConfirmPassword(_rule: any, value: string, callback: Function) {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单校验规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  department: [{ required: true, message: '请输入院系', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

const registerFormRef = ref<any>(null)
const loading = ref(false)

// 角色切换时清空动态字段
watch(
  () => registerForm.roleType,
  () => {
    registerForm.department = ''
    registerForm.major = ''
    registerForm.grade = ''
    registerForm.title = ''
    registerForm.companyName = ''
  }
)

/**
 * 处理注册
 */
async function handleRegister() {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      // 组装提交数据，移除确认密码字段与个人资料字段（注册时无需填写）
      const submitData: any = {
        username: registerForm.username,
        password: registerForm.password,
        roleType: registerForm.roleType
      }

      // 根据角色补充对应字段
      if (registerForm.roleType === 'student') {
        submitData.department = registerForm.department
        submitData.major = registerForm.major
        submitData.grade = registerForm.grade
      } else if (registerForm.roleType === 'teacher') {
        submitData.department = registerForm.department
        submitData.title = registerForm.title
      } else if (registerForm.roleType === 'enterprise') {
        submitData.companyName = registerForm.companyName
      }

      const res: any = await register(submitData)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message || '注册失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('注册失败，请检查输入信息')
    } finally {
      loading.value = false
    }
  })
}

/**
 * 跳转到登录页
 */
function goToLogin() {
  router.push('/login')
}
</script>

<script lang="ts">
export default {
  components: {
    Reading,
    TeacherIcon,
    OfficeBuilding
  }
}
</script>

<style scoped lang="scss">
.register-view {
  width: 100%;
  padding-bottom: var(--zh-space-4);
}

.form-header {
  margin-bottom: var(--zh-space-6);

  .form-title {
    font-family: var(--zh-font-display);
    font-size: 32px;
    font-weight: 700;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-3) 0;
  }

  .form-subtitle {
    margin: 0;
    font-size: 15px;
    color: var(--zh-text-secondary);
    line-height: 1.6;
  }
}

.register-form {
  :deep(.el-form-item__label) {
    font-size: 14px;
    font-weight: 500;
    color: var(--zh-text-primary);
    padding-bottom: var(--zh-space-1);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--zh-radius);
    box-shadow: 0 0 0 1px var(--zh-border) inset;
    padding: 4px 16px;
    transition: all var(--zh-transition-fast);

    &:hover {
      box-shadow: 0 0 0 1px var(--zh-primary) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px var(--zh-primary) inset, 0 0 0 3px var(--zh-primary-soft);
    }
  }

  :deep(.el-input__inner) {
    height: 44px;
  }
}

.form-section {
  background: var(--zh-bg-warm);
  border: 1px solid var(--zh-border-light);
  border-radius: var(--zh-radius);
  padding: var(--zh-space-5);
  margin-bottom: var(--zh-space-5);

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--zh-space-3);
    font-family: var(--zh-font-display);
    font-size: 16px;
    font-weight: 600;
    color: var(--zh-primary);
    margin: 0 0 var(--zh-space-5) 0;
    padding-bottom: var(--zh-space-3);
    border-bottom: 1px solid var(--zh-border-light);

    .section-marker {
      width: 4px;
      height: 18px;
      border-radius: 2px;
      background: var(--zh-accent);
    }
  }
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--zh-space-3);
}

.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--zh-space-2);
  padding: var(--zh-space-4) var(--zh-space-2);
  border-radius: var(--zh-radius);
  border: 1px solid var(--zh-border);
  background: var(--zh-bg-elevated);
  color: var(--zh-text-secondary);
  cursor: pointer;
  transition: all var(--zh-transition-fast);

  &:hover {
    border-color: var(--zh-primary);
    color: var(--zh-primary);
  }

  &.active {
    border-color: var(--zh-primary);
    background: var(--zh-primary-soft);
    color: var(--zh-primary);
    box-shadow: var(--zh-shadow-sm);
  }

  .role-name {
    font-size: 14px;
    font-weight: 500;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--zh-radius);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
  background: var(--zh-primary);
  border: none;
  margin-top: var(--zh-space-2);
  transition: all var(--zh-transition-fast);

  &:hover {
    background: var(--zh-secondary);
    transform: translateY(-1px);
    box-shadow: var(--zh-shadow-md);
  }
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--zh-text-secondary);
  margin-top: var(--zh-space-4);

  p {
    margin: 0;
  }
}

@media (max-width: 576px) {
  .role-cards {
    grid-template-columns: 1fr;
  }
}
</style>
