import request from '@/request/request'

// 非流式全局问答
export function chat(data) {
  return request.post('/ai/chat', data)
}

// 非流式文档问答
export function chatWithDoc(data) {
  return request.post('/ai/chat/doc', data)
}

// 获取 token（给流式 fetch 用）
export function getToken() {
  return localStorage.getItem('token') || ''
}

export function getConversationList() {
  return request.get('/ai/conversation/list')
}

export function getConversationMessages(id) {
  return request.get(`/ai/conversation/${id}/messages`)
}

export function deleteConversation(id) {
  return request.delete(`/ai/conversation/${id}`)
}
