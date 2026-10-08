<template>
  <div class="my-conversations">
    <h2 class="page-title">我的问答</h2>

    <div class="conversation-layout">
      <!-- 左侧：会话列表 -->
      <aside class="conv-list">
        <div
          v-for="c in conversations"
          :key="c.id"
          :class="['conv-item', { active: currentId === c.id }]"
          @click="selectConversation(c.id)"
        >
          <div class="conv-title">{{ c.title || '新对话' }}</div>
          <div class="conv-time">{{ formatTime(c.createTime) }}</div>
          <el-button
            class="conv-delete"
            link
            size="small"
            type="danger"
            @click.stop="handleDelete(c)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-empty
          v-if="!loadingList && conversations.length === 0"
          description="还没有问答记录"
          :image-size="80"
        />
      </aside>

      <!-- 右侧：消息详情 -->
      <main class="conv-detail" v-loading="loadingMessages">
        <div v-if="!currentId" class="placeholder">
          <el-empty description="请从左侧选择一个会话" />
        </div>
        <template v-else>
          <div class="conv-header">
            <h3>{{ currentConversation?.title || '新对话' }}</h3>
          </div>
          <div class="msg-list" ref="messagesRef">
            <div v-for="m in messages" :key="m.id" :class="['msg', m.role]">
              <div class="msg-bubble">{{ m.content }}</div>
              <div class="msg-time">{{ formatTime(m.createTime, 'HH:mm') }}</div>
            </div>
            <el-empty
              v-if="messages.length === 0"
              description="这个会话还没有消息"
              :image-size="80"
            />
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConversationList,
  getConversationMessages,
  deleteConversation
} from '@/api/ai'

const conversations = ref([])
const messages = ref([])
const currentId = ref(null)
const loadingList = ref(false)
const loadingMessages = ref(false)
const messagesRef = ref(null)

const currentConversation = computed(() =>
  conversations.value.find((c) => c.id === currentId.value)
)

function formatTime(str, pattern = 'MM-DD HH:mm') {
  if (!str) return ''
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hours = pad(d.getHours())
  const minutes = pad(d.getMinutes())
  if (pattern === 'HH:mm') return `${hours}:${minutes}`
  return `${month}-${day} ${hours}:${minutes}`
}

async function loadConversations() {
  loadingList.value = true
  try {
    conversations.value = (await getConversationList()) || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loadingList.value = false
  }
}

async function selectConversation(id) {
  currentId.value = id
  loadingMessages.value = true
  try {
    messages.value = (await getConversationMessages(id)) || []
    nextTick(() => {
      if (messagesRef.value) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    })
  } catch (e) {
    // 拦截器已提示
  } finally {
    loadingMessages.value = false
  }
}

async function handleDelete(c) {
  try {
    await ElMessageBox.confirm(
      `确定删除会话"${c.title || '新对话'}"吗？删除后不可恢复。`,
      '提示',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await deleteConversation(c.id)
    ElMessage.success('已删除')
    if (currentId.value === c.id) {
      currentId.value = null
      messages.value = []
    }
    loadConversations()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(loadConversations)
</script>

<style scoped>
.my-conversations {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  margin-bottom: 16px;
  color: #1f2937;
}

.conversation-layout {
  display: flex;
  height: calc(100vh - 180px);
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

/* 左侧列表 */
.conv-list {
  width: 260px;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
  padding: 8px 0;
}

.conv-item {
  position: relative;
  padding: 12px 40px 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.2s;
}

.conv-item:hover {
  background: #f9fafb;
}

.conv-item.active {
  background: #ecfdf5;
  border-left: 3px solid #10b981;
  padding-left: 13px;
}

.conv-title {
  font-size: 14px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.conv-time {
  font-size: 12px;
  color: #9ca3af;
}

.conv-delete {
  position: absolute;
  top: 12px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.conv-item:hover .conv-delete {
  opacity: 1;
}

/* 右侧详情 */
.conv-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.conv-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.conv-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.msg {
  margin-bottom: 16px;
}

.msg.user {
  text-align: right;
}

.msg-bubble {
  display: inline-block;
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 10px;
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
  color: #9ca3af;
  margin-top: 4px;
}
</style>
