import request from '@/request/request'

export function getDocPage(params) {
  return request.get('/doc/page', { params })
}

export function getPublicDocPage(params) {
  return request.get('/doc/page/public', { params })
}

export function getDocDetail(id) {
  return request.get(`/doc/${id}`)
}

// 传 FormData 时不手动设 Content-Type，让浏览器/axios 自动带 boundary
export function uploadDoc(formData) {
  return request.post('/doc/upload', formData)
}

export function uploadDocsBatch(files, categoryId) {
  const formData = new FormData()
  files.forEach((f) => formData.append('files', f))
  if (categoryId) formData.append('categoryId', categoryId)
  return request.post('/doc/upload/batch', formData)
}

export function updateDoc(id, data) {
  return request.put(`/doc/${id}`, data)
}

export function deleteDoc(id) {
  return request.delete(`/doc/${id}`)
}

export function downloadDocUrl(id) {
  return `/api/doc/${id}/download`
}
