<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <h2>智汇桥</h2>
          <p>产学研用一体化智慧协同系统</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
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
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>测试账号：admin / 123456</p>
        <p>没有账号？<el-button link @click="goToRegister">立即注册</el-button></p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

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
        // 保存 token 和用户信息到 Pinia
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data.userInfo)
        ElMessage.success('登录成功')
        // 跳转到首页
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
 * 跳转到注册页
 */
function goToRegister() {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);

  .login-card {
    width: 420px;
    border-radius: 12px;

    .login-header {
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

    .login-button {
      width: 100%;
      margin-top: 10px;
    }

    .login-tips {
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
