import request from '@/utils/request'

/**
 * 通用文件上传
 * @param file 上传的文件对象
 * @param type 文件类型：image 图片、file 通用文件
 * @returns 上传结果，包含 url、filename、size
 */
export function uploadFile(file: File, type: 'image' | 'file' = 'file') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return request({
    url: '/api/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
