import request from '@/request/request'

export function getCategoryList() {
  return request.get('/category/list')
}

export function createCategory(data) {
  return request.post('/category', data)
}

export function updateCategory(id, data) {
  return request.put(`/category/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}

export function getPublicFolders() {
  return request.get('/category/public')
}

export function getVisibleCategories() {
  return request.get('/category/visible')
}

export function getMyFolders() {
  return request.get('/category/mine')
}

export function createMyFolder(data) {
  return request.post('/category/mine', data)
}

export function deleteMyFolder(id) {
  return request.delete(`/category/mine/${id}`)
}

export function updateMyFolder(id, data) {
  return request.put(`/category/mine/${id}`, data)
}
