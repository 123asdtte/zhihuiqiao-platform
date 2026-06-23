import request from '@/utils/request'

/**
 * 教学辅助模块 API 封装
 * 提供知识点管理、学习路径推荐、学习记录等接口调用
 */

// ==================== 知识点管理（算法：知识图谱） ====================

/**
 * 创建知识点
 */
export function createKnowledgePoint(data: Record<string, any>) {
  return request({
    url: '/learning/knowledge-point',
    method: 'post',
    data
  })
}

/**
 * 更新知识点
 */
export function updateKnowledgePoint(id: number | string, data: Record<string, any>) {
  return request({
    url: `/learning/knowledge-point/${id}`,
    method: 'put',
    data
  })
}

/**
 * 查询知识点列表
 */
export function getKnowledgePointList(courseName?: string) {
  return request({
    url: '/learning/knowledge-point/list',
    method: 'get',
    params: courseName ? { courseName } : {}
  })
}

/**
 * 查询知识点详情
 */
export function getKnowledgePointDetail(id: number | string) {
  return request({
    url: `/learning/knowledge-point/${id}`,
    method: 'get'
  })
}

/**
 * 删除知识点
 */
export function deleteKnowledgePoint(id: number | string) {
  return request({
    url: `/learning/knowledge-point/${id}`,
    method: 'delete'
  })
}

// ==================== 学习路径推荐（算法：拓扑排序） ====================

/**
 * 【算法】生成个性化学习路径
 * 基于知识图谱拓扑排序
 */
export function recommendLearningPath(userId: number, courseName: string) {
  return request({
    url: '/learning/path/recommend',
    method: 'get',
    params: { userId, courseName }
  })
}

/**
 * 【算法】获取下一个推荐学习的知识点
 */
export function getNextRecommended(userId: number, courseName: string) {
  return request({
    url: '/learning/path/next',
    method: 'get',
    params: { userId, courseName }
  })
}

/**
 * 【算法】获取按难度分层的推荐路径
 */
export function getLeveledPath(userId: number, courseName: string) {
  return request({
    url: '/learning/path/leveled',
    method: 'get',
    params: { userId, courseName }
  })
}

/**
 * 【算法】获取课程学习进度百分比
 */
export function getLearningProgress(userId: number, courseName: string) {
  return request({
    url: '/learning/progress',
    method: 'get',
    params: { userId, courseName }
  })
}

// ==================== 学习资源 ====================

/**
 * 分页查询学习资源
 */
export function getLearningResourceList(params: {
  pageNum?: number
  pageSize?: number
  subject?: string
  resourceType?: string
}) {
  return request({
    url: '/learning/resource/list',
    method: 'get',
    params
  })
}

// ==================== 学习记录 ====================

/**
 * 创建或更新学习记录
 */
export function saveLearningRecord(data: {
  userId: number
  resourceId: number
  progress?: number
  status?: string
  lastPosition?: number
}) {
  return request({
    url: '/learning/record',
    method: 'post',
    data
  })
}

/**
 * 查询用户的学习记录列表
 */
export function getMyLearningRecords(userId: number) {
  return request({
    url: '/learning/record/my',
    method: 'get',
    params: { userId }
  })
}
