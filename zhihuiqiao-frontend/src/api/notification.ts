import request from '@/utils/request'

/**
 * 消息通知模块 API 封装
 */

/**
 * 分页查询当前用户的通知列表
 * @param params 查询参数
 */
export function getNotificationList(params: {
  pageNum?: number
  pageSize?: number
  onlyUnread?: boolean
  type?: string
}) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get'
  })
}

/**
 * 标记指定通知为已读
 * @param id 通知ID
 */
export function markNotificationAsRead(id: number | string) {
  return request({
    url: `/api/notifications/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记当前用户所有通知为已读
 */
export function markAllNotificationsAsRead() {
  return request({
    url: '/api/notifications/read-all',
    method: 'put'
  })
}

/**
 * 通知设置项类型
 */
export interface NotificationSettingItem {
  id?: number
  userId?: number
  type: string
  enabled: number
}

/**
 * 获取当前用户的通知设置
 */
export function getNotificationSettings() {
  return request({
    url: '/api/notifications/settings',
    method: 'get'
  })
}

/**
 * 更新当前用户的通知设置
 * @param settings 设置列表
 */
export function updateNotificationSettings(settings: NotificationSettingItem[]) {
  return request({
    url: '/api/notifications/settings',
    method: 'put',
    data: settings
  })
}
