import request from '@/utils/request'

/**
 * 操作日志模块 API 封装
 * 提供管理员查询操作日志的接口
 */

/**
 * 分页查询操作日志
 * @param params 查询参数
 */
export function getOperationLogList(params: {
  pageNum?: number
  pageSize?: number
  module?: string
  username?: string
  status?: number
  startTime?: string
  endTime?: string
}) {
  return request({
    url: '/api/admin/logs',
    method: 'get',
    params
  })
}
