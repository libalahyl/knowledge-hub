import request from '@/request/request'

export function addFavorite(docId) {
  return request.post(`/favorite/${docId}`)
}

export function removeFavorite(docId) {
  return request.delete(`/favorite/${docId}`)
}

export function checkFavorite(docId) {
  return request.get(`/favorite/check/${docId}`)
}

export function getMyFavorites(params) {
  return request.get('/favorite/my', { params })
}
