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
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

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
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
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
