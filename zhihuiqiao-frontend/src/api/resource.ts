import request from '@/utils/request'

/**
 * 资源流转模块 API 封装
 * 提供闲置资源、资源预约相关接口调用
 */

/**
 * 查询资源列表
 * @param params 查询参数
 */
export function getResourceList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  resourceType?: string
  status?: string
}) {
  return request({
    url: '/resource/list',
    method: 'get',
    params
  })
}

/**
 * 查询资源详情
 * @param id 资源ID
 */
export function getResourceDetail(id: number | string) {
  return request({
    url: `/resource/${id}`,
    method: 'get'
  })
}

/**
 * 提交资源预约申请
 * @param data 预约信息
 */
export function submitBooking(data: {
  resourceId: number
  borrowerId: number
  startTime: string
  endTime: string
  purpose: string
}) {
  return request({
    url: '/resource/booking',
    method: 'post',
    data
  })
}

/**
 * 查询资源的预约列表
 * @param resourceId 资源ID
 */
export function getResourceBookings(resourceId: number | string) {
  return request({
    url: `/resource/${resourceId}/bookings`,
    method: 'get'
  })
}

/**
 * 查询我的预约列表
 * @param borrowerId 借用人ID
 */
export function getMyBookings(borrowerId: number) {
  return request({
    url: '/resource/booking/my',
    method: 'get',
    params: { borrowerId }
  })
}

/**
 * 审批资源预约
 * @param id 预约ID
 * @param status 审批状态
 * @param replyMessage 回复消息
 */
export function auditBooking(id: number, status: string, replyMessage?: string) {
  return request({
    url: `/resource/booking/${id}/audit`,
    method: 'put',
    params: { status, replyMessage }
  })
}

/**
 * 归还资源
 * @param id 预约ID
 */
export function returnResource(id: number) {
  return request({
    url: `/resource/booking/${id}/return`,
    method: 'put'
  })
}

/**
 * 发布闲置资源
 * @param data 资源信息
 */
export function publishResource(data: Record<string, any>) {
  return request({
    url: '/resource/publish',
    method: 'post',
    data
  })
}
