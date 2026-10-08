import request from '@/request/request'

export function getReportPage(params) {
  return request.get('/report/page', { params })
}

export function handleReport(id, data) {
  return request.put(`/report/${id}/handle`, data)
}

export function submitReport(data) {
  return request.post('/report', data)
}
