<template>
  <el-popover
    ref="popoverRef"
    placement="bottom-end"
    :width="360"
    trigger="click"
    @show="loadNotifications"
  >
    <template #reference>
      <div class="bell">
        <el-icon :size="20"><Bell /></el-icon>
        <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>
    </template>

    <div class="panel">
      <div class="panel-header">
        <span class="panel-title">通知</span>
        <el-button v-if="unreadCount > 0" link size="small" @click="handleMarkAll">全部已读</el-button>
      </div>
      <div class="panel-body">
        <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" :image-size="60" />
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notify-item"
          :class="{ unread: n.isRead === 0 }"
          @click="handleClick(n)"
        >
          <span v-if="n.isRead === 0" class="unread-dot"></span>
          <el-icon class="type-icon" :color="n.type === 'DOC_COMMENT' ? '#10b981' : '#f59e0b'">
            <ChatDotRound v-if="n.type === 'DOC_COMMENT'" />
            <ChatLineRound v-else />
          </el-icon>
          <div class="notify-main">
            <div class="notify-text">
              <strong class="from">{{ n.fromUserName || '匿名' }}</strong>
              <span class="content">{{ n.content }}</span>
            </div>
            <div class="notify-time">{{ formatTime(n.createTime) }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUnreadCount, getNotificationPage, markRead, markAllRead } from '@/api/notification'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const popoverRef = ref(null)
const unreadCount = ref(0)
const notifications = ref([])
const loading = ref(false)
let timer = null

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.count || 0
  } catch (e) {
    // 拦截器已提示
  }
}

async function loadNotifications() {
  loading.value = true
  try {
    const data = await getNotificationPage({ page: 1, size: 20 })
    notifications.value = data.records || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function handleClick(n) {
  if (n.isRead === 0) {
    try {
      await markRead(n.id)
      n.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      // 拦截器已提示
    }
  }
  popoverRef.value?.hide()
  if (n.docId) {
    router.push(`/doc/${n.docId}`)
  }
}

async function handleMarkAll() {
  try {
    await markAllRead()
    unreadCount.value = 0
    notifications.value.forEach((n) => { n.isRead = 1 })
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    // 拦截器已提示
  }
}

function startPolling() {
  if (timer) clearInterval(timer)
  timer = setInterval(fetchUnreadCount, 30000)
}

function onVisibilityChange() {
  if (document.hidden) {
    // 页面隐藏：暂停轮询，避免后台重复请求
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  } else {
    // 页面可见：立即刷新一次 + 恢复轮询
    fetchUnreadCount()
    startPolling()
  }
}

function formatTime(str) {
  if (!str) return ''
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  if (userStore.token) {
    fetchUnreadCount()
    startPolling()
    document.addEventListener('visibilitychange', onVisibilityChange)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})
</script>

<style scoped>
.bell {
  position: relative;
  display: flex;
  align-items: center;
  margin-right: 16px;
  padding: 4px;
  cursor: pointer;
  color: #374151;
}

.badge {
  position: absolute;
  top: -2px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: #10b981;
  color: #fff;
  font-size: 11px;
  line-height: 16px;
  text-align: center;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.panel-title {
  font-weight: 600;
  color: #1f2937;
}

.panel-body {
  max-height: 420px;
  overflow-y: auto;
}

.notify-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 4px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
}

.notify-item:hover {
  background: #f9fafb;
}

.notify-item.unread {
  background: #ecfdf5;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  flex-shrink: 0;
  margin-top: 6px;
}

.type-icon {
  margin-top: 3px;
  flex-shrink: 0;
}

.notify-main {
  flex: 1;
  min-width: 0;
}

.notify-text {
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
  word-break: break-word;
}

.notify-text .from {
  color: #1f2937;
}

.notify-text .content {
  color: #4b5563;
}

.notify-time {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}
</style>
