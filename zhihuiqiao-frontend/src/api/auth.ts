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
