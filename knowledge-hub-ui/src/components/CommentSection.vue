<template>
  <div class="comment-section">
    <h3 class="comment-title">评论 ({{ commentCount }})</h3>

    <!-- 发表评论 -->
    <div class="comment-form" v-if="userStore.user">
      <el-radio-group v-model="form.type" size="small" class="type-group">
        <el-radio-button value="SUPPLEMENT">补充</el-radio-button>
        <el-radio-button value="CORRECTION">纠错</el-radio-button>
        <el-radio-button value="QUESTION">提问</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="form.content"
        type="textarea"
        :rows="3"
        maxlength="1000"
        show-word-limit
        placeholder="写下你的补充、纠错或提问..."
      />
      <div class="form-footer">
        <el-button type="primary" :loading="submitting" :disabled="!form.content.trim()"
                   @click="handleSubmit">发表评论</el-button>
      </div>
    </div>
    <el-empty v-else description="登录后即可发表评论" :image-size="60" />

    <!-- 评论列表（树形：顶层 + 回复） -->
    <div class="comment-list" v-loading="loading">
      <div v-for="c in comments" :key="c.id" class="comment-item top">
        <div class="avatar">{{ (c.userName || '?').charAt(0) }}</div>
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-user">{{ c.userName || '匿名' }}</span>
            <el-tag :type="typeTagColor(c.type)" size="small" effect="plain">
              {{ typeText(c.type) }}
            </el-tag>
            <span class="comment-time">{{ formatTime(c.createTime) }}</span>
            <el-button
              v-if="canDelete(c)"
              class="comment-delete"
              link
              size="small"
              type="danger"
              @click="handleDelete(c)"
            >删除</el-button>
          </div>
          <div class="comment-content">{{ c.content }}</div>

          <div class="comment-actions">
            <el-button link size="small" class="like-btn" :class="{ liked: c.liked }"
                       :disabled="!userStore.user" @click="handleLike(c)">
              👍 {{ c.likeCount || 0 }}
            </el-button>
            <el-button link size="small" @click="openReply(c.id, c.id, c.userName)">回复</el-button>
            <el-button v-if="userStore.user" link size="small" type="danger" @click="openReport(c)">举报</el-button>
          </div>

          <!-- 回复列表 -->
          <div class="reply-list" v-if="c.replies && c.replies.length">
            <div v-for="r in c.replies" :key="r.id" class="reply-item">
              <div class="avatar small">{{ (r.userName || '?').charAt(0) }}</div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-user">{{ r.userName || '匿名' }}</span>
                  <span v-if="r.replyToUserName" class="reply-to">回复 @{{ r.replyToUserName }}</span>
                  <span class="comment-time">{{ formatTime(r.createTime) }}</span>
                  <el-button
                    v-if="canDelete(r)"
                    class="comment-delete"
                    link
                    size="small"
                    type="danger"
                    @click="handleDelete(r)"
                  >删除</el-button>
                </div>
                <div class="comment-content">{{ r.content }}</div>
                <div class="comment-actions">
                  <el-button link size="small" class="like-btn" :class="{ liked: r.liked }"
                             :disabled="!userStore.user" @click="handleLike(r)">
                    👍 {{ r.likeCount || 0 }}
                  </el-button>
                  <el-button link size="small" @click="openReply(r.id, c.id, r.userName)">回复</el-button>
                  <el-button v-if="userStore.user" link size="small" type="danger" @click="openReport(r)">举报</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTarget && replyTarget.topCommentId === c.id" class="reply-form">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              maxlength="1000"
              placeholder="回复 @{{ replyTarget.replyToUserName || '匿名' }}..."
            />
            <div class="reply-form-footer">
              <el-button size="small" @click="cancelReply">取消</el-button>
              <el-button size="small" type="primary" :loading="replySubmitting"
                         :disabled="!replyContent.trim()" @click="submitReply">回复</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && comments.length === 0" description="还没有评论，来发表第一条吧" />
    </div>

    <!-- 举报评论弹窗 -->
    <el-dialog v-model="reportVisible" title="举报评论" width="480px" :close-on-click-modal="false">
      <div v-if="reportTarget" class="report-comment-preview">
        <strong>{{ reportTarget.userName || '匿名' }}：</strong>{{ reportTarget.content }}
      </div>
      <el-input
        v-model="reportReason"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="请详细描述举报原因..."
      />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="reportSubmitting"
                   :disabled="!reportReason.trim()" @click="submitReportComment">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnotationTree, addAnnotation, deleteAnnotation, likeAnnotation } from '@/api/annotation'
import { submitReport } from '@/api/report'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  docId: { type: Number, required: true }
})

const userStore = useUserStore()
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = reactive({ content: '', type: 'SUPPLEMENT' })

// 回复状态：{ replyToId, topCommentId, replyToUserName }
const replyTarget = ref(null)
const replyContent = ref('')
const replySubmitting = ref(false)

// 举报状态
const reportVisible = ref(false)
const reportTarget = ref(null)
const reportReason = ref('')
const reportSubmitting = ref(false)

const commentCount = computed(() => {
  let n = 0
  for (const c of comments.value) {
    n += 1 + (c.replies ? c.replies.length : 0)
  }
  return n
})

async function loadComments() {
  loading.value = true
  try {
    comments.value = (await getAnnotationTree(props.docId)) || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const content = form.content.trim()
  if (!content) return
  submitting.value = true
  try {
    await addAnnotation({ docId: props.docId, content, type: form.type })
    ElMessage.success('评论成功')
    form.content = ''
    form.type = 'SUPPLEMENT'
    loadComments()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

function openReply(replyToId, topCommentId, replyToUserName) {
  replyTarget.value = { replyToId, topCommentId, replyToUserName }
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

async function submitReply() {
  const content = replyContent.value.trim()
  if (!content || !replyTarget.value) return
  replySubmitting.value = true
  try {
    await addAnnotation({
      docId: props.docId,
      content,
      type: 'SUPPLEMENT',
      replyToId: replyTarget.value.replyToId
    })
    ElMessage.success('回复成功')
    cancelReply()
    loadComments()
  } catch (e) {
    // 拦截器已提示
  } finally {
    replySubmitting.value = false
  }
}

async function handleLike(comment) {
  if (!userStore.user) return
  try {
    const res = await likeAnnotation(comment.id)
    comment.liked = res.liked
    comment.likeCount = res.likeCount
  } catch (e) {
    // 拦截器已提示
  }
}

function openReport(comment) {
  reportTarget.value = comment
  reportReason.value = ''
  reportVisible.value = true
}

async function submitReportComment() {
  const reason = reportReason.value.trim()
  if (!reason || !reportTarget.value) return
  reportSubmitting.value = true
  try {
    await submitReport({ commentId: reportTarget.value.id, reason })
    ElMessage.success('举报成功，管理员会尽快处理')
    reportVisible.value = false
  } catch (e) {
    // 拦截器已提示
  } finally {
    reportSubmitting.value = false
  }
}

function canDelete(c) {
  if (!userStore.user) return false
  return c.userId === userStore.user.id || userStore.user.role === 'ADMIN'
}

function handleDelete(c) {
  ElMessageBox.confirm('确定删除这条评论吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteAnnotation(c.id)
      ElMessage.success('已删除')
      loadComments()
    } catch (e) {
      // 拦截器已提示
    }
  }).catch(() => {})
}

function typeText(t) {
  return { SUPPLEMENT: '补充', CORRECTION: '纠错', QUESTION: '提问' }[t] || t
}

function typeTagColor(t) {
  return { SUPPLEMENT: 'success', CORRECTION: 'danger', QUESTION: 'primary' }[t] || 'info'
}

function formatTime(str) {
  if (!str) return ''
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(loadComments)
watch(() => props.docId, () => {
  form.content = ''
  form.type = 'SUPPLEMENT'
  cancelReply()
  loadComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 32px;
}

.comment-title {
  font-size: 18px;
  color: #1f2937;
  margin: 0 0 16px;
}

.comment-form {
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.type-group {
  margin-bottom: 12px;
}

.form-footer {
  margin-top: 12px;
  text-align: right;
}

.comment-list {
  min-height: 100px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #10b981;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 500;
  flex-shrink: 0;
}

.avatar.small {
  width: 32px;
  height: 32px;
  font-size: 14px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.comment-user {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.reply-to {
  color: #10b981;
  font-size: 13px;
}

.comment-time {
  color: #9ca3af;
  font-size: 12px;
}

.comment-delete {
  margin-left: auto;
}

.comment-content {
  color: #374151;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.comment-actions {
  margin-top: 6px;
}

.like-btn.liked {
  color: #f59e0b;
}

.reply-list {
  margin-top: 12px;
  padding-left: 52px;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
}

.reply-item + .reply-item {
  border-top: 1px dashed #f3f4f6;
}

.reply-form {
  margin-top: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.reply-form-footer {
  margin-top: 8px;
  text-align: right;
}

.report-comment-preview {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 13px;
  color: #6b7280;
  word-break: break-word;
}
</style>
