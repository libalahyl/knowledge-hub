import request from '@/request/request'

export function getAnnotationList(docId) {
  return request.get(`/annotation/list/${docId}`)
}

export function getAnnotationTree(docId) {
  return request.get(`/annotation/tree/${docId}`)
}

export function addAnnotation(data) {
  return request.post('/annotation', data)
}

export function deleteAnnotation(id) {
  return request.delete(`/annotation/${id}`)
}

export function likeAnnotation(id) {
  return request.post(`/annotation/${id}/like`)
}
