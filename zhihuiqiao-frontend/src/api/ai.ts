import request from '@/utils/request'

/**
 * AI 智能推荐模块 API 封装
 */

/**
 * 获取科研项目智能推荐列表
 */
export function getRecommendedProjects() {
  return request({
    url: '/api/ai/recommend/projects',
    method: 'get'
  })
}
