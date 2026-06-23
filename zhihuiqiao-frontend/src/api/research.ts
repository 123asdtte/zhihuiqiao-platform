import request from '@/utils/request'

/**
 * 科研撮合模块 API 封装
 * 提供科研画像、科研项目、企业需求、智能推荐等接口调用
 */

// ==================== 科研画像 ====================

/**
 * 创建或更新科研画像
 */
export function saveProfile(data: Record<string, any>) {
  return request({
    url: '/research/profile',
    method: 'post',
    data
  })
}

/**
 * 根据用户ID查询科研画像
 */
export function getProfileByUserId(userId: number) {
  return request({
    url: `/research/profile/${userId}`,
    method: 'get'
  })
}

// ==================== 科研项目 ====================

/**
 * 发布科研项目
 */
export function publishProject(data: Record<string, any>) {
  return request({
    url: '/research/project',
    method: 'post',
    data
  })
}

/**
 * 分页查询科研项目列表
 */
export function getProjectList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  projectType?: string
  status?: string
}) {
  return request({
    url: '/research/project/list',
    method: 'get',
    params
  })
}

/**
 * 查询项目详情
 */
export function getProjectDetail(id: number | string) {
  return request({
    url: `/research/project/${id}`,
    method: 'get'
  })
}

/**
 * 更新项目状态
 */
export function updateProjectStatus(id: number | string, status: string) {
  return request({
    url: `/research/project/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// ==================== 项目申请 ====================

/**
 * 提交项目加入申请
 */
export function applyProject(data: Record<string, any>) {
  return request({
    url: '/research/application',
    method: 'post',
    data
  })
}

/**
 * 查询项目的申请列表
 */
export function getProjectApplications(projectId: number | string) {
  return request({
    url: `/research/project/${projectId}/applications`,
    method: 'get'
  })
}

/**
 * 查询我的申请列表
 */
export function getMyApplications(applicantId: number) {
  return request({
    url: '/research/application/my',
    method: 'get',
    params: { applicantId }
  })
}

/**
 * 审核项目申请
 */
export function auditApplication(id: number | string, status: string, replyMessage?: string) {
  return request({
    url: `/research/application/${id}/audit`,
    method: 'put',
    params: { status, replyMessage }
  })
}

// ==================== 企业需求 ====================

/**
 * 发布企业需求
 */
export function publishDemand(data: Record<string, any>) {
  return request({
    url: '/research/demand',
    method: 'post',
    data
  })
}

/**
 * 分页查询企业需求列表
 */
export function getDemandList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  demandType?: string
  status?: string
}) {
  return request({
    url: '/research/demand/list',
    method: 'get',
    params
  })
}

/**
 * 查询企业需求详情
 */
export function getDemandDetail(id: number | string) {
  return request({
    url: `/research/demand/${id}`,
    method: 'get'
  })
}

// ==================== 智能推荐（算法：TF-IDF + 余弦相似度） ====================

/**
 * 【算法】为学生推荐最匹配的科研项目
 * 基于 TF-IDF + Cosine Similarity
 */
export function recommendProjects(userId: number, topN: number = 6) {
  return request({
    url: '/research/project/recommend',
    method: 'get',
    params: { userId, topN }
  })
}

/**
 * 【算法】为教师/企业推荐匹配的企业需求
 * 基于科研能力画像匹配
 */
export function recommendDemands(userId: number, topN: number = 6) {
  return request({
    url: '/research/demand/recommend',
    method: 'get',
    params: { userId, topN }
  })
}
