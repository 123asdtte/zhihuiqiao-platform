import request from '@/utils/request'

/**
 * 系统管理模块 API 封装
 * 提供用户管理、内容审核等后台接口
 */

// ==================== 用户管理 ====================

/**
 * 分页查询用户列表
 * @param params 查询参数
 */
export function getUserList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  roleType?: string
}) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户信息
 * @param id 用户ID
 * @param data 用户数据
 */
export function updateUser(id: number | string, data: Record<string, any>) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function deleteUser(id: number | string) {
  return request({
    url: `/api/admin/users/${id}`,
    method: 'delete'
  })
}

/**
 * 重置用户密码
 * @param id 用户ID
 * @param newPassword 新密码
 */
export function resetUserPassword(id: number | string, newPassword: string) {
  return request({
    url: `/api/admin/users/${id}/reset-password`,
    method: 'put',
    params: { newPassword }
  })
}

// ==================== 内容审核 ====================

/**
 * 查询内容统计
 */
export function getAuditStats() {
  return request({
    url: '/api/admin/audit/stats',
    method: 'get'
  })
}

/**
 * 分页查询科研项目列表
 */
export function getAuditProjects(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/admin/audit/projects',
    method: 'get',
    params
  })
}

/**
 * 分页查询企业需求列表
 */
export function getAuditDemands(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/admin/audit/demands',
    method: 'get',
    params
  })
}

/**
 * 分页查询闲置资源列表
 */
export function getAuditResources(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/admin/audit/resources',
    method: 'get',
    params
  })
}

/**
 * 分页查询学习资源列表
 */
export function getAuditLearningResources(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}) {
  return request({
    url: '/api/admin/audit/learning-resources',
    method: 'get',
    params
  })
}

/**
 * 更新科研项目状态
 */
export function updateAuditProjectStatus(
  id: number | string,
  status: string
) {
  return request({
    url: `/api/admin/audit/project/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新企业需求状态
 */
export function updateAuditDemandStatus(
  id: number | string,
  status: string
) {
  return request({
    url: `/api/admin/audit/demand/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新闲置资源状态
 */
export function updateAuditResourceStatus(
  id: number | string,
  status: string
) {
  return request({
    url: `/api/admin/audit/resource/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新学习资源状态
 */
export function updateAuditLearningResourceStatus(
  id: number | string,
  status: number
) {
  return request({
    url: `/api/admin/audit/learning-resource/${id}/status`,
    method: 'put',
    params: { status }
  })
}
