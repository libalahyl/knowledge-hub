<template>
  <div class="ai-chat-panel">
    <div class="ai-header">
      <div class="ai-title">
        <el-icon color="#10b981"><ChatDotRound /></el-icon>
        <span>AI 助手</span>
        <el-tag size="small" :type="docId ? 'success' : 'info'" effect="plain">
          {{ docId ? '文档问答' : '全局问答' }}
        </el-tag>
      </div>
      <el-button link @click="handleClear">
        <el-icon><Delete /></el-icon>
      </el-button>
    </div>

    <div class="ai-messages" ref="messagesRef">
      <div v-for="(msg, i) in messages" :key="i" :class="['msg', msg.role]">
        <div class="msg-bubble">{{ msg.content }}<span v-if="msg.streaming" class="cursor">|</span></div>
        <div class="msg-time">{{ msg.time }}</div>
      </div>
    </div>

    <div class="ai-input">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        resize="none"
        placeholder="输入问题... (Enter 发送，Shift+Enter 换行)"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button type="primary" :disabled="!input.trim() || loading" @click="handleSend">
        <el-icon><Promotion /></el-icon>
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { getToken } from '@/api/ai'

const props = defineProps({
  docId: { type: Number, default: null }
})

const messages = ref([
  { role: 'assistant', content: '你好！我是知识库助手，有什么可以帮你的？', time: formatTime() }
])
const input = ref('')
const loading = ref(false)
const isStreaming = ref(false)
const messagesRef = ref(null)
const conversationId = ref(null)

function formatTime() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

async function handleSend() {
  const text = input.value.trim()
  if (!text || loading.value) return

  // 1. 追加用户消息
  messages.value.push({ role: 'user', content: text, time: formatTime() })
  input.value = ''
  scrollToBottom()

  // 2. 追加空的 AI 消息占位
  messages.value.push({ role: 'assistant', content: '', time: formatTime(), streaming: true })
  const aiIndex = messages.value.length - 1

  loading.value = true
  isStreaming.value = true

  try {
    // 3. fetch 流式请求
    const url = props.docId ? '/api/ai/chat/doc/stream' : '/api/ai/chat/stream'
    const body = props.docId
      ? { docId: props.docId, message: text, conversationId: conversationId.value }
      : { message: text, conversationId: conversationId.value }

    const resp = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
      },
      body: JSON.stringify(body)
    })

    // 从响应头读 conversationId，实现多轮上下文
    const cid = resp.headers.get('X-Conversation-Id')
    if (cid) {
      conversationId.value = Number(cid)
    }

    if (!resp.ok || !resp.body) {
      throw new Error('HTTP ' + resp.status)
    }

    // 4. 读取流并解析 data:xxx
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let fullContent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const t = line.replace(/\r$/, '')
        if (t.startsWith('data:')) {
          const data = t.substring(5)
          if (data === '[DONE]') continue
          fullContent += data
          messages.value[aiIndex].content = fullContent
          scrollToBottom()
        }
      }
    }

    // 5. 流结束，去掉光标
    messages.value[aiIndex].streaming = false
    if (!fullContent) {
      messages.value[aiIndex].content = '（无回答）'
    }
  } catch (e) {
    console.error('AI 请求失败:', e)
    messages.value[aiIndex].content = '抱歉，AI 服务暂时不可用，请稍后重试。'
    messages.value[aiIndex].streaming = false
  } finally {
    loading.value = false
    isStreaming.value = false
    scrollToBottom()
  }
}

function handleClear() {
  messages.value = [
    { role: 'assistant', content: '对话已清空，请问有什么可以帮你的？', time: formatTime() }
  ]
  conversationId.value = null
}

// 切换文档时重置对话
watch(() => props.docId, () => {
  messages.value = [
    { role: 'assistant', content: '你好！我是知识库助手，有什么可以帮你的？', time: formatTime() }
  ]
  conversationId.value = null
})
</script>

<style scoped>
.ai-chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.ai-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.msg {
  margin-bottom: 12px;
}

.msg.user {
  text-align: right;
}

.msg-bubble {
  display: inline-block;
  max-width: 85%;
  padding: 8px 12px;
  border-radius: 8px;
  text-align: left;
  word-break: break-word;
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.6;
}

.msg.user .msg-bubble {
  background: #10b981;
  color: #fff;
}

.msg.assistant .msg-bubble {
  background: #f5f7fa;
  color: #303133;
  border: 1px solid #e4e7ed;
}

.msg-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.cursor {
  display: inline-block;
  color: #10b981;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.ai-input {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
