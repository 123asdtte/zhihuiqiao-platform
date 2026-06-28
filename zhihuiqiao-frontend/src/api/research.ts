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

/**
 * 删除科研项目
 * @param id 项目ID
 */
export function deleteProject(id: number | string) {
  return request({
    url: `/api/research/project/${id}`,
    method: 'delete'
  })
}

/**
 * 查询我发布的科研项目
 * @param params 查询参数
 */
export function getMyProjects(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/research/project/my',
    method: 'get',
    params
  })
}

/**
 * 查询我加入的科研项目
 * @param params 查询参数
 */
export function getJoinedProjects(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return request({
    url: '/api/research/project/joined',
    method: 'get',
    params
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
 * 根据申请ID查询项目申请详情
 * @param id 申请ID
 */
export function getApplicationById(id: number | string) {
  return request({
    url: `/api/research/application/${id}`,
    method: 'get'
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
 * 学生角色必须传入 applicantId（通常为当前用户ID）；管理员可不传，返回全部申请
 * @param applicantId 申请人ID（可选）
 */
export function getMyApplications(applicantId?: number) {
  return request({
    url: '/api/research/application/my',
    method: 'get',
    params: applicantId != null ? { applicantId } : undefined
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

/**
 * 申请人撤回项目申请
 * @param id 申请ID
 */
export function withdrawApplication(id: number | string) {
  return request({
    url: `/api/research/application/${id}/withdraw`,
    method: 'put'
  })
}

/**
 * 发布人将申请标记为待沟通
 * @param id 申请ID
 * @param replyMessage 回复消息
 */
export function interviewApplication(id: number | string, replyMessage?: string) {
  return request({
    url: `/api/research/application/${id}/interview`,
    method: 'put',
    params: replyMessage ? { replyMessage } : undefined
  })
}

/**
 * 申请人确认入组
 * @param id 申请ID
 */
export function confirmAdmission(id: number | string) {
  return request({
    url: `/api/research/application/${id}/confirm`,
    method: 'put'
  })
}

/**
 * 查询项目成员列表
 * @param projectId 项目ID
 */
export function getProjectMembers(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/members`,
    method: 'get'
  })
}

/**
 * 移除项目成员
 * @param projectId 项目ID
 * @param userId 用户ID
 */
export function removeProjectMember(projectId: number | string, userId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/member/${userId}`,
    method: 'delete'
  })
}

/**
 * 更新项目成员角色
 * @param projectId 项目ID
 * @param userId 用户ID
 * @param role 角色
 */
export function updateProjectMemberRole(
  projectId: number | string,
  userId: number | string,
  role: string
) {
  return request({
    url: `/api/research/project/${projectId}/member/${userId}/role`,
    method: 'put',
    params: { role }
  })
 }

// ==================== 项目过程管理 ====================

/**
 * 查询项目动态列表
 * @param projectId 项目ID
 */
export function getProjectDynamics(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/dynamics`,
    method: 'get'
  })
}

/**
 * 发布项目动态
 * @param projectId 项目ID
 * @param data 动态数据
 */
export function createProjectDynamic(projectId: number | string, data: Record<string, any>) {
  return request({
    url: `/api/research/project/${projectId}/dynamic`,
    method: 'post',
    data
  })
}

/**
 * 删除项目动态
 * @param dynamicId 动态ID
 */
export function deleteProjectDynamic(dynamicId: number | string) {
  return request({
    url: `/api/research/project/dynamic/${dynamicId}`,
    method: 'delete'
  })
}

/**
 * 查询项目任务列表
 * @param projectId 项目ID
 */
export function getProjectTasks(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/tasks`,
    method: 'get'
  })
}

/**
 * 创建项目任务
 * @param projectId 项目ID
 * @param data 任务数据
 */
export function createProjectTask(projectId: number | string, data: Record<string, any>) {
  return request({
    url: `/api/research/project/${projectId}/task`,
    method: 'post',
    data
  })
}

/**
 * 更新任务状态
 * @param taskId 任务ID
 * @param status 状态
 */
export function updateProjectTaskStatus(taskId: number | string, status: string) {
  return request({
    url: `/api/research/project/task/${taskId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除项目任务
 * @param taskId 任务ID
 */
export function deleteProjectTask(taskId: number | string) {
  return request({
    url: `/api/research/project/task/${taskId}`,
    method: 'delete'
  })
}

/**
 * 查询项目成果列表
 * @param projectId 项目ID
 */
export function getProjectOutcomes(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/outcomes`,
    method: 'get'
  })
}

/**
 * 上传项目成果
 * @param projectId 项目ID
 * @param data 成果数据
 */
export function createProjectOutcome(projectId: number | string, data: Record<string, any>) {
  return request({
    url: `/api/research/project/${projectId}/outcome`,
    method: 'post',
    data
  })
}

/**
 * 删除项目成果
 * @param outcomeId 成果ID
 */
export function deleteProjectOutcome(outcomeId: number | string) {
  return request({
    url: `/api/research/project/outcome/${outcomeId}`,
    method: 'delete'
  })
}

/**
 * 开始项目
 * @param projectId 项目ID
 */
export function startProject(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/start`,
    method: 'put'
  })
}

/**
 * 结项项目
 * @param projectId 项目ID
 */
export function completeProject(projectId: number | string) {
  return request({
    url: `/api/research/project/${projectId}/complete`,
    method: 'put'
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

// ==================== 需求撮合相关接口 ====================

/**
 * 提交需求承接方案
 * @param data 承接方案数据
 */
export function submitDemandBid(data: any) {
  return request({
    url: '/api/matching/bid',
    method: 'post',
    data
  })
}

/**
 * 查询需求下的所有承接方案
 * @param demandId 需求ID
 */
export function listDemandBids(demandId: number | string) {
  return request({
    url: `/api/matching/bid/list/${demandId}`,
    method: 'get'
  })
}

/**
 * 企业审核通过承接方案
 * @param bidId 承接方案ID
 * @param remark 备注
 */
export function approveDemandBid(bidId: number | string, remark?: string) {
  return request({
    url: `/api/matching/bid/${bidId}/approve`,
    method: 'post',
    params: { remark }
  })
}

/**
 * 企业驳回承接方案
 * @param bidId 承接方案ID
 * @param remark 备注
 */
export function rejectDemandBid(bidId: number | string, remark?: string) {
  return request({
    url: `/api/matching/bid/${bidId}/reject`,
    method: 'post',
    params: { remark }
  })
}

/**
 * 创建合作合同
 * @param data 合同数据
 */
export function createContract(data: any) {
  return request({
    url: '/api/matching/contract',
    method: 'post',
    data
  })
}

/**
 * 根据需求ID查询合同
 * @param demandId 需求ID
 */
export function getContractByDemand(demandId: number | string) {
  return request({
    url: `/api/matching/contract/demand/${demandId}`,
    method: 'get'
  })
}

/**
 * 签订合同
 * @param contractId 合同ID
 */
export function signContract(contractId: number | string) {
  return request({
    url: `/api/matching/contract/${contractId}/sign`,
    method: 'post'
  })
}

/**
 * 完成合同
 * @param contractId 合同ID
 */
export function completeContract(contractId: number | string) {
  return request({
    url: `/api/matching/contract/${contractId}/complete`,
    method: 'post'
  })
}

/**
 * 创建里程碑
 * @param data 里程碑数据
 */
export function createMilestone(data: any) {
  return request({
    url: '/api/matching/milestone',
    method: 'post',
    data
  })
}

/**
 * 查询合同下的全部里程碑
 * @param contractId 合同ID
 */
export function listMilestones(contractId: number | string) {
  return request({
    url: `/api/matching/milestone/list/${contractId}`,
    method: 'get'
  })
}

/**
 * 提交交付物
 * @param milestoneId 里程碑ID
 * @param url 交付物URL
 */
export function submitDeliverable(milestoneId: number | string, url: string) {
  return request({
    url: `/api/matching/milestone/${milestoneId}/submit`,
    method: 'post',
    params: { url }
  })
}

/**
 * 企业验收通过里程碑
 * @param milestoneId 里程碑ID
 * @param remark 备注
 */
export function approveDeliverable(milestoneId: number | string, remark?: string) {
  return request({
    url: `/api/matching/milestone/${milestoneId}/approve`,
    method: 'post',
    params: { remark }
  })
}

/**
 * 企业验收驳回里程碑
 * @param milestoneId 里程碑ID
 * @param remark 备注
 */
export function rejectDeliverable(milestoneId: number | string, remark?: string) {
  return request({
    url: `/api/matching/milestone/${milestoneId}/reject`,
    method: 'post',
    params: { remark }
  })
}
