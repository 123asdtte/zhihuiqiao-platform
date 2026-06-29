import request from '@/utils/request'

/**
 * 认证相关 API 封装
 * 提供登录、注册、获取当前用户信息等接口
 */

/**
 * 用户登录
 * @param data 登录参数
 */
export function login(data: { username: string; password: string }) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param data 注册参数
 */
export function register(data: Record<string, any>) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return request({
    // 与后端 AuthController 的 /auth/info 接口对齐
    url: '/auth/info',
    method: 'get'
  })
}

/**
 * 更新当前登录用户信息
 * @param data 用户信息
 */
export function updateCurrentUser(data: Record<string, any>) {
  return request({
    url: '/auth/profile',
    method: 'put',
    data
  })
}

/**
 * 修改当前登录用户密码
 * @param data 密码参数
 */
export function changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

/**
 * 更换当前登录用户邮箱
 * @param data 邮箱参数
 */
export function changeEmail(data: { password: string; email: string }) {
  return request({
    url: '/auth/change-email',
    method: 'post',
    data
  })
}

/**
 * 更换当前登录用户手机号
 * @param data 手机号参数
 */
export function changePhone(data: { password: string; phone: string }) {
  return request({
    url: '/auth/change-phone',
    method: 'post',
    data
  })
}
