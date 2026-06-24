import request from '@/utils/request'

/**
 * 数据看板 API 封装
 */

/**
 * 获取全平台统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/api/dashboard/stats',
    method: 'get'
  })
}
