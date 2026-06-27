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

/**
 * AI 通用对话
 * @param question 用户问题
 */
export function chatWithAi(question: string) {
  return request({
    url: '/api/ai/chat',
    method: 'post',
    data: { question }
  })
}
