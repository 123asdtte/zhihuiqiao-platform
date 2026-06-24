<template>
  <div class="register-page">
    <el-card class="register-card" shadow="hover">
      <template #header>
        <div class="register-header">
          <h2>智汇桥</h2>
          <p>注册新账号</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        size="large"
        @keyup.enter="handleRegister"
      >
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

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            :prefix-icon="UserFilled"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色类型" prop="roleType">
          <el-select
            v-model="registerForm.roleType"
            placeholder="请选择角色类型"
            style="width: 100%"
            @change="handleRoleChange"
          >
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="企业" value="enterprise" />
          </el-select>
        </el-form-item>

        <!-- 学生特有字段 -->
        <template v-if="registerForm.roleType === 'student'">
          <el-form-item label="院系" prop="department">
            <el-input
              v-model="registerForm.department"
              placeholder="请输入院系"
              clearable
            />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input
              v-model="registerForm.major"
              placeholder="请输入专业"
              clearable
            />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-input
              v-model="registerForm.grade"
              placeholder="例如：2022级"
              clearable
            />
          </el-form-item>
        </template>

        <!-- 教师特有字段 -->
        <template v-if="registerForm.roleType === 'teacher'">
          <el-form-item label="院系" prop="department">
            <el-input
              v-model="registerForm.department"
              placeholder="请输入院系"
              clearable
            />
          </el-form-item>
          <el-form-item label="职称" prop="title">
            <el-input
              v-model="registerForm.title"
              placeholder="例如：副教授"
              clearable
            />
          </el-form-item>
        </template>

        <!-- 企业特有字段 -->
        <template v-if="registerForm.roleType === 'enterprise'">
          <el-form-item label="企业名称" prop="companyName">
            <el-input
              v-model="registerForm.companyName"
              placeholder="请输入企业名称"
              clearable
            />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-tips">
        <p>已有账号？<el-button link @click="goToLogin">立即登录</el-button></p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Message, Phone } from '@element-plus/icons-vue'
import { register } from '@/api/auth'

const router = useRouter()

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  roleType: '',
  department: '',
  major: '',
  grade: '',
  title: '',
  companyName: ''
})

// 自定义校验：确认密码是否与密码一致
function validateConfirmPassword(_rule: any, value: string, callback: Function) {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 自定义校验：手机号格式
function validatePhone(_rule: any, value: string, callback: Function) {
  if (!value) {
    callback()
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号'))
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
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
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

/**
 * 角色切换时清空动态字段
 */
function handleRoleChange() {
  registerForm.department = ''
  registerForm.major = ''
  registerForm.grade = ''
  registerForm.title = ''
  registerForm.companyName = ''
}

/**
 * 处理注册
 */
async function handleRegister() {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      // 组装提交数据，移除确认密码字段
      const submitData: any = {
        username: registerForm.username,
        password: registerForm.password,
        roleType: registerForm.roleType,
        realName: registerForm.realName,
        email: registerForm.email,
        phone: registerForm.phone
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

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);

  .register-card {
    width: 480px;
    border-radius: 12px;

    .register-header {
      text-align: center;

      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .register-button {
      width: 100%;
      margin-top: 10px;
    }

    .register-tips {
      margin-top: 20px;
      text-align: center;
      color: #909399;
      font-size: 13px;

      p {
        margin: 6px 0;
      }
    }
  }
}
</style>
