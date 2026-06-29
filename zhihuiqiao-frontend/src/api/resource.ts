import request from '@/utils/request'

/**
 * 资源流转模块 API 封装
 * 提供闲置资源、资源预约相关接口调用
 */

/**
 * 查询资源列表
 * @param params 查询参数，支持交易模式与价格区间筛选
 */
export function getResourceList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  resourceType?: string
  status?: string
  tradeMode?: string
  minPrice?: number
  maxPrice?: number
}) {
  return request({
    // 后端 ResourceController 前缀为 /api/resource
    url: '/api/resource/list',
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
    url: `/api/resource/${id}`,
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
    url: '/api/resource/booking',
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
    url: `/api/resource/${resourceId}/bookings`,
    method: 'get'
  })
}

/**
 * 查询我的预约列表
 * @param borrowerId 借用人ID
 */
export function getMyBookings(borrowerId: number) {
  return request({
    url: '/api/resource/booking/my',
    method: 'get',
    params: { borrowerId }
  })
}

/**
 * 查询我收到的预约列表（资源所有者视角）
 * @param resourceId 资源ID（可选）
 */
export function getOwnerBookings(resourceId?: number | string) {
  return request({
    url: '/api/resource/booking/owner',
    method: 'get',
    params: resourceId != null ? { resourceId } : undefined
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
    url: `/api/resource/booking/${id}/audit`,
    method: 'put',
    params: { status, replyMessage }
  })
}

/**
 * 取消资源预约
 * @param id 预约ID
 */
export function cancelBooking(id: number) {
  return request({
    url: `/api/resource/booking/${id}/cancel`,
    method: 'put'
  })
}

/**
 * 借用方申请归还资源
 * @param id 预约ID
 */
export function requestReturn(id: number | string) {
  return request({
    url: `/api/resource/booking/${id}/return-request`,
    method: 'put'
  })
}

/**
 * 资源所有者确认归还资源
 * @param id 预约ID
 */
export function confirmReturn(id: number | string) {
  return request({
    url: `/api/resource/booking/${id}/return-confirm`,
    method: 'put'
  })
}

/**
 * 上报资源损坏赔偿
 * @param id 预约ID
 * @param data 损坏赔偿信息
 */
export function reportDamage(id: number | string, data: Record<string, any>) {
  return request({
    url: `/api/resource/booking/${id}/damage`,
    method: 'post',
    data
  })
}

/**
 * 处理资源损坏赔偿
 * @param id 损坏记录ID
 * @param resolveRemark 处理备注
 */
export function resolveDamage(id: number | string, resolveRemark?: string) {
  return request({
    url: `/api/resource/damage/${id}/resolve`,
    method: 'put',
    params: resolveRemark ? { resolveRemark } : undefined
  })
}

/**
 * 查询损坏赔偿记录列表
 * @param params 查询参数
 */
export function listDamageRecords(params: {
  resourceId?: number | string
  bookingId?: number | string
}) {
  return request({
    url: '/api/resource/damage-records',
    method: 'get',
    params
  })
}

/**
 * 查询资源预约日历
 * @param resourceId 资源ID
 * @param startDate 开始日期 YYYY-MM-DD
 * @param endDate 结束日期 YYYY-MM-DD
 */
export function getResourceCalendar(
  resourceId: number | string,
  startDate: string,
  endDate: string
) {
  return request({
    url: `/api/resource/${resourceId}/calendar`,
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 发布闲置资源
 * @param data 资源信息
 */
export function publishResource(data: Record<string, any>) {
  return request({
    url: '/api/resource/publish',
    method: 'post',
    data
  })
}

/**
 * 提交资源转让意向
 * @param data 意向信息
 */
export function submitTransferRequest(data: {
  resourceId: number | string
  message?: string
  contactInfo?: string
}) {
  return request({
    url: '/api/resource/transfer/request',
    method: 'post',
    data
  })
}

/**
 * 查询我发出的转让意向
 */
export function getSentTransferRequests() {
  return request({
    url: '/api/resource/transfer/requests/sent',
    method: 'get'
  })
}

/**
 * 查询我收到的转让意向
 */
export function getReceivedTransferRequests() {
  return request({
    url: '/api/resource/transfer/requests/received',
    method: 'get'
  })
}

/**
 * 卖家确认转让成交
 * @param id 意向ID
 */
export function acceptTransferRequest(id: number | string) {
  return request({
    url: `/api/resource/transfer/request/${id}/accept`,
    method: 'put'
  })
}

/**
 * 卖家拒绝转让意向
 * @param id 意向ID
 */
export function rejectTransferRequest(id: number | string) {
  return request({
    url: `/api/resource/transfer/request/${id}/reject`,
    method: 'put'
  })
}

/**
 * 买家取消转让意向
 * @param id 意向ID
 */
export function cancelTransferRequest(id: number | string) {
  return request({
    url: `/api/resource/transfer/request/${id}/cancel`,
    method: 'put'
  })
}

/**
 * 交易评价
 * @param id 意向ID
 * @param data 评价信息
 */
export function reviewTransferRequest(
  id: number | string,
  data: {
    targetUserId: number | string
    rating: number
    comment?: string
  }
) {
  return request({
    url: `/api/resource/transfer/request/${id}/review`,
    method: 'post',
    data
  })
}
