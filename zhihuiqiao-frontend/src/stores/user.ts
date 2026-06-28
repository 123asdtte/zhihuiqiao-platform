import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  roleType: string
  department: string
  major: string
  grade: string
  title: string
  companyName: string
  creditScore?: number
}

// localStorage 中 userInfo 的 key
const USER_INFO_KEY = 'userInfo'

/**
 * 从 localStorage 中读取用户信息
 */
function loadUserInfoFromStorage(): UserInfo | null {
  const stored = localStorage.getItem(USER_INFO_KEY)
  if (!stored) return null
  try {
    return JSON.parse(stored) as UserInfo
  } catch (e) {
    console.error('解析 localStorage 中用户信息失败', e)
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  // 初始化时从 localStorage 恢复用户信息，避免刷新后角色状态丢失
  const userInfo = ref<UserInfo | null>(loadUserInfoFromStorage())

  const isLoggedIn = computed(() => !!token.value)
  const roleType = computed(() => userInfo.value?.roleType || '')
  const isAdmin = computed(() => roleType.value === 'admin')
  const isTeacher = computed(() => roleType.value === 'teacher')
  const isStudent = computed(() => roleType.value === 'student')
  const isEnterprise = computed(() => roleType.value === 'enterprise')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    // 同步持久化到 localStorage，确保刷新后仍可恢复角色信息
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem(USER_INFO_KEY)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roleType,
    isAdmin,
    isTeacher,
    isStudent,
    isEnterprise,
    setToken,
    setUserInfo,
    logout
  }
})
