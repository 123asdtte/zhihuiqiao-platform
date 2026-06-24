import request from '@/utils/request'

/**
 * 教学辅助模块 API 封装
 * 提供学习资源、学习记录、收藏相关接口
 */

/**
 * 分页查询学习资源列表
 * @param params 查询参数
 */
export function getLearningResourceList(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  resourceType?: string
  subject?: string
  difficultyLevel?: string
}) {
  return request({
    url: '/api/learning/resource/list',
    method: 'get',
    params
  })
}

/**
 * 查询学习资源详情
 * @param id 资源ID
 */
export function getLearningResourceDetail(id: number | string) {
  return request({
    url: `/api/learning/resource/${id}`,
    method: 'get'
  })
}

/**
 * 发布学习资源
 * @param data 学习资源数据
 */
export function publishLearningResource(data: Record<string, any>) {
  return request({
    url: '/api/learning/resource',
    method: 'post',
    data
  })
}

/**
 * 删除学习资源
 * @param id 资源ID
 */
export function deleteLearningResource(id: number | string) {
  return request({
    url: `/api/learning/resource/${id}`,
    method: 'delete'
  })
}

/**
 * 保存或更新学习记录（开始学习、更新进度）
 * @param data 学习记录数据
 */
export function saveLearningRecord(data: Record<string, any>) {
  return request({
    url: '/api/learning/record',
    method: 'post',
    data
  })
}

/**
 * 查询我的学习记录
 * @param status 状态过滤：learning/completed/favorite
 */
export function getMyLearningRecords(status?: string) {
  return request({
    url: '/api/learning/record/my',
    method: 'get',
    params: status ? { status } : {}
  })
}

/**
 * 收藏或取消收藏学习资源
 * @param resourceId 资源ID
 */
export function toggleFavoriteResource(resourceId: number | string) {
  return request({
    url: `/api/learning/record/favorite/${resourceId}`,
    method: 'post'
  })
}

/**
 * 删除学习记录
 * @param id 记录ID
 */
export function deleteLearningRecord(id: number | string) {
  return request({
    url: `/api/learning/record/${id}`,
    method: 'delete'
  })
}
