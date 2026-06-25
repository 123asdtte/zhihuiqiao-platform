<template>
  <div class="login-view">
    <div class="form-header">
      <h2 class="form-title">欢迎回来</h2>
      <p class="form-subtitle">登录您的智汇桥账户，开启学术协同之旅</p>
    </div>

    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      label-position="top"
      size="large"
      class="login-form"
      @keyup.enter="handleLogin"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
          clearable
          class="auth-input"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          show-password
          clearable
          class="auth-input"
        />
      </el-form-item>

      <div class="form-options">
        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        <el-button link type="primary" class="forgot-link">忘记密码？</el-button>
      </div>

      <el-form-item>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form-item>
    </el-form>

    <div class="auth-divider">
      <span>测试账号</span>
    </div>

    <div class="test-accounts">
      <div class="account-chip" @click="fillAccount('admin', '123456')">
        <el-icon><UserFilled /></el-icon>
        <span>admin</span>
      </div>
      <div class="account-chip" @click="fillAccount('student01', '123456')">
        <el-icon><UserFilled /></el-icon>
        <span>student01</span>
      </div>
      <div class="account-chip" @click="fillAccount('teacher01', '123456')">
        <el-icon><UserFilled /></el-icon>
        <span>teacher01</span>
      </div>
    </div>

    <div class="form-footer">
      <p>还没有账号？<el-button link type="primary" @click="goToRegister">立即注册</el-button></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

const rememberMe = ref(false)

// 表单校验规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loginFormRef = ref<any>(null)
const loading = ref(false)

/**
 * 处理登录
 */
async function handleLogin() {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      const res: any = await login({
        username: loginForm.username,
        password: loginForm.password
      })

      if (res.code === 200) {
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data.userInfo)
        ElMessage.success('登录成功')
        router.push('/app/home')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error('登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

/**
 * 填充测试账号
 */
function fillAccount(username: string, password: string) {
  loginForm.username = username
  loginForm.password = password
}

/**
 * 跳转到注册页
 */
function goToRegister() {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.login-view {
  width: 100%;
}

.form-header {
  margin-bottom: var(--zh-space-8);

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

.login-form {
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: var(--zh-space-2) 0 var(--zh-space-6) 0;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: var(--zh-text-secondary);
  }

  .forgot-link {
    font-size: 13px;
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
  transition: all var(--zh-transition-fast);

  &:hover {
    background: var(--zh-secondary);
    transform: translateY(-1px);
    box-shadow: var(--zh-shadow-md);
  }
}

.auth-divider {
  display: flex;
  align-items: center;
  margin: var(--zh-space-6) 0;
  color: var(--zh-text-tertiary);
  font-size: 13px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--zh-border-light);
  }

  span {
    padding: 0 var(--zh-space-3);
  }
}

.test-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: var(--zh-space-3);
  justify-content: center;
  margin-bottom: var(--zh-space-6);

  .account-chip {
    display: inline-flex;
    align-items: center;
    gap: var(--zh-space-2);
    padding: var(--zh-space-2) var(--zh-space-4);
    border-radius: 100px;
    font-size: 13px;
    color: var(--zh-text-secondary);
    background: var(--zh-bg-warm);
    border: 1px solid var(--zh-border-light);
    cursor: pointer;
    transition: all var(--zh-transition-fast);

    &:hover {
      color: var(--zh-primary);
      border-color: var(--zh-primary);
      background: var(--zh-primary-soft);
    }
  }
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--zh-text-secondary);

  p {
    margin: 0;
  }
}
</style>
