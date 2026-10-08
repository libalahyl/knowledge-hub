import request from '@/request/request'

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function getNotificationPage(params) {
  return request.get('/notification/page', { params })
}

export function markRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllRead() {
  return request.put('/notification/read-all')
}
