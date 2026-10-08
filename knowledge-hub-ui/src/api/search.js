import request from '@/request/request'

export function searchDocs(params) {
  return request.get('/search', { params })
}
