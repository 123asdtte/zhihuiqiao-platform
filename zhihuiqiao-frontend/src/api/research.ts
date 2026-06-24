import request from '@/utils/request'

/**
 * 科研撮合模块 API 封装
 * 提供科研画像、科研项目、项目申请、企业需求相关接口
 */

// ==================== 科研画像 ====================

/**
 * 创建或更新科研画像
 * @param data 科研画像数据
 */
export function saveResearcherProfile(data: {
  userId: number
  researchDirections?: string
  skills?: string
  publications?: string
  projectExperience?: string
  researchInterests?: string
  availability?: string
  cooperationIntention?: string
}) {
  return request({
    url: '/api/research/profile',
    method: 'post',
    data
  })
}

/**
 * 根据用户ID查询科研画像
 * @param userId 用户ID
 */
export function getResearcherProfile(userId: number) {
  return request({
    url: `/api/research/profile/${userId}`,
    method: 'get'
  })
}

// ==================== 科研项目 ====================

/**
 * 发布科研项目
 * @param data 科研项目数据
 */
export function publishProject(data: Record<string, any>) {
  return request({
    url: '/api/research/project',
    method: 'post',
    data
  })
}

/**
 * 分页查询科研项目列表
 * @param params 查询参数
 */
export function getProjectList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  projectType?: string
  status?: string
}) {
  return request({
    url: '/api/research/project/list',
    method: 'get',
    params
  })
}

/**
 * 查询项目详情
 * @param id 项目ID
 */
export function getProjectDetail(id: number | string) {
  return request({
    url: `/api/research/project/${id}`,
    method: 'get'
  })
}

/**
 * 更新项目状态
 * @param id 项目ID
 * @param status 状态
 */
export function updateProjectStatus(id: number | string, status: string) {
  return request({
    url: `/api/research/project/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// ==================== 项目申请 ====================

/**
 * 提交项目加入申请
 * @param data 申请数据
 */
export function applyProject(data: {
  projectId: number
  applicantId: number
  applyReason: string
}) {
  return request({
    url: '/api/research/application',
    method: 'post',
    data
  })
}

/**
 * 查询项目的申请列表
 * @param projectId 项目ID
 */
export function getProjectApplications(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/applications`,
    method: 'get'
  })
}

/**
 * 查询我的申请列表
 * @param applicantId 申请人ID
 */
export function getMyApplications(applicantId: number) {
  return request({
    url: '/api/research/application/my',
    method: 'get',
    params: { applicantId }
  })
}

/**
 * 审核项目申请
 * @param id 申请ID
 * @param status 审核状态
 * @param replyMessage 回复消息
 */
export function auditApplication(
  id: number | string,
  status: string,
  replyMessage?: string
) {
  return request({
    url: `/api/research/application/${id}/audit`,
    method: 'put',
    params: { status, replyMessage }
  })
}

// ==================== 企业需求 ====================

/**
 * 发布企业需求
 * @param data 企业需求数据
 */
export function publishDemand(data: Record<string, any>) {
  return request({
    url: '/api/research/demand',
    method: 'post',
    data
  })
}

/**
 * 分页查询企业需求列表
 * @param params 查询参数
 */
export function getDemandList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  demandType?: string
  status?: string
}) {
  return request({
    url: '/api/research/demand/list',
    method: 'get',
    params
  })
}

/**
 * 查询企业需求详情
 * @param id 需求ID
 */
export function getDemandDetail(id: number | string) {
  return request({
    url: `/api/research/demand/${id}`,
    method: 'get'
  })
}
