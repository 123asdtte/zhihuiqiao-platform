import request from '@/utils/request'

/**
 * 数据看板 API 封装
 */

/**
 * 获取全平台统计数据（后台管理专用）
 */
export function getDashboardStats() {
  return request({
    url: '/api/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取公开统计数据（首页展示，所有登录用户均可查看）
 */
export function getPublicDashboardStats() {
  return request({
    url: '/api/dashboard/public-stats',
    method: 'get'
  })
}
