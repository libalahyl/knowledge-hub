<template>
  <div class="doc-detail" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="detail-toolbar">
      <el-button link @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon>返回知识库
      </el-button>
      <div class="actions" v-if="doc">
        <el-button @click="handleDownload">
          <el-icon><Download /></el-icon>下载
        </el-button>
        <el-button @click="toggleFavorite">
          <el-icon :color="doc.favorited ? '#f59e0b' : ''">
            <StarFilled v-if="doc.favorited" />
            <Star v-else />
          </el-icon>
          {{ doc.favorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="openReport">
          <el-icon><Warning /></el-icon>举报
        </el-button>
        <el-button v-if="canEdit" @click="openEdit">
          <el-icon><Edit /></el-icon>编辑
        </el-button>
        <el-button v-if="canEdit" type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>删除
        </el-button>
      </div>
    </div>

    <template v-if="doc">
      <!-- 文档头部 -->
      <h1 class="doc-title">{{ doc.title }}</h1>
      <div class="doc-meta">
        <el-tag size="small" type="success" effect="plain" v-if="doc.categoryName">
          {{ doc.categoryName }}
        </el-tag>
        <span v-if="doc.categoryName" class="from-folder">
          📁 来自：{{ doc.categoryName }}
        </span>
        <span>{{ doc.uploaderName }}</span>
        <span><el-icon><View /></el-icon>{{ doc.viewCount }}</span>
        <span><el-icon><Star /></el-icon>{{ doc.favoriteCount }}</span>
        <span>{{ doc.createTime }}</span>
      </div>

      <!-- 正文 + 目录 -->
      <div class="doc-body">
        <aside class="doc-toc" v-if="tocList.length">
          <h4>目录</h4>
          <ul>
            <li
              v-for="item in tocList"
              :key="item.id"
              :class="{ 'toc-h3': item.level === 3 }"
              @click="scrollTo(item.id)"
            >
              {{ item.text }}
            </li>
          </ul>
        </aside>

        <div class="markdown-body" v-html="renderedContent"></div>
      </div>

      <CommentSection v-if="doc" :doc-id="doc.id" />
    </template>

    <EditDocDialog v-model="editVisible" :doc="doc" @success="handleEditSuccess" />
    <ReportDialog v-model="reportVisible" :doc="doc" @success="handleReportSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocDetail, updateDoc, deleteDoc } from '@/api/doc'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import EditDocDialog from '@/components/EditDocDialog.vue'
import ReportDialog from '@/components/ReportDialog.vue'
import CommentSection from '@/components/CommentSection.vue'
import { marked } from 'marked'

// 极简渲染器：只保留标题（TOC 用）+ 纯文本，砍掉高亮/表格/引用等增强渲染
const tocList = ref([])
marked.use({
  renderer: {
    // 标题：保留（TOC 依赖）
    heading({ tokens, depth }) {
      const text = this.parser.parseInline(tokens)
      const raw = text.replace(/<[^>]+>/g, '')
      if (depth === 2 || depth === 3) {
        const id = 'heading-' + tocList.value.length
        tocList.value.push({ id, text: raw, level: depth })
        return `<h${depth} id="${id}">${text}</h${depth}>`
      }
      return `<h${depth}>${text}</h${depth}>`
    },
    // 代码块：纯 <pre>，不高亮
    code({ text }) {
      const escaped = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      return `<pre>${escaped}</pre>`
    },
    // 行内代码：纯 <code>
    codespan({ text }) {
      const escaped = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      return `<code>${escaped}</code>`
    },
    // 表格：渲染成纯文本，不渲染 <table>（省 DOM）
    table(token) {
      let result = ''
      token.header.forEach((cell) => {
        result += this.parser.parseInline(cell.tokens) + ' | '
      })
      result += '\n'
      token.rows.forEach((row) => {
        row.forEach((cell) => {
          result += this.parser.parseInline(cell.tokens) + ' | '
        })
        result += '\n'
      })
      return `<pre class="plain-table">${result}</pre>`
    },
    // 引用：纯文本
    blockquote({ tokens }) {
      const text = this.parser.parse(tokens)
      return `<div class="plain-quote">${text}</div>`
    }
  }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const doc = ref(null)
const loading = ref(false)
const editVisible = ref(false)
const reportVisible = ref(false)

const canEdit = computed(() => {
  if (!doc.value || !userStore.user) return false
  return (
    doc.value.uploaderId === userStore.user.id ||
    userStore.user.role === 'ADMIN'
  )
})

const renderedContent = ref('')

// doc 变化时重新渲染正文并重新生成目录。
// 注意：不能在 computed 里改 tocList（会死循环），必须用 watch
watch(() => doc.value, (newDoc) => {
  if (!newDoc?.content) {
    renderedContent.value = ''
    tocList.value = []
    return
  }
  // 清空旧目录，marked.parse 时 heading 渲染器会重新收集
  tocList.value = []
  renderedContent.value = marked.parse(newDoc.content)
  // 超大文档（>100KB）不显示目录，纯正文保证流畅
  if (newDoc.content.length > 100000) {
    tocList.value = []
  }
}, { immediate: true })

async function loadDoc() {
  loading.value = true
  try {
    doc.value = await getDocDetail(route.params.id)
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

async function toggleFavorite() {
  if (!doc.value) return
  try {
    if (doc.value.favorited) {
      await removeFavorite(doc.value.id)
      doc.value.favorited = false
      doc.value.favoriteCount = Math.max(0, doc.value.favoriteCount - 1)
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(doc.value.id)
      doc.value.favorited = true
      doc.value.favoriteCount++
      ElMessage.success('已收藏')
    }
  } catch (e) {
    // 拦截器已提示
  }
}

function handleDownload() {
  window.open(`/api/doc/${doc.value.id}/download`, '_blank')
}

function openReport() {
  reportVisible.value = true
}

function handleReportSuccess() {
  // 举报成功后什么都不用做，弹窗组件已提示
}

function openEdit() {
  editVisible.value = true
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除这篇文档吗？删除后不可恢复。', '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteDoc(doc.value.id)
    ElMessage.success('已删除')
    router.push('/')
  } catch (e) {
    // 用户取消或接口失败
  }
}

function handleEditSuccess() {
  ElMessage.success('修改成功')
  loadDoc()
}

onMounted(loadDoc)
watch(() => route.params.id, loadDoc)
</script>

<style scoped>
.doc-detail {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 8px;
}

.doc-title {
  font-size: 26px;
  margin: 16px 0;
  color: #1f2937;
}

.doc-meta {
  display: flex;
  gap: 16px;
  color: #9ca3af;
  font-size: 13px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.doc-meta span {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

/* 正文 + 目录 布局 */
.doc-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 目录 */
.doc-toc {
  width: 200px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-size: 13px;
}

.doc-toc h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #374151;
  font-weight: 600;
}

.doc-toc ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.doc-toc li {
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 4px;
  color: #6b7280;
  line-height: 1.4;
  transition: all 0.2s;
  word-break: break-word;
}

.doc-toc li:hover {
  background: #e5e7eb;
  color: #10b981;
}

.doc-toc li.toc-h3 {
  padding-left: 24px;
  font-size: 12px;
}

/* 正文 */
.markdown-body {
  flex: 1;
  min-width: 0;
  line-height: 1.8;
  font-size: 15px;
  color: #374151;
  contain: content;
}

/* 正文内样式（极简：标题 + 纯文本） */
:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
  color: #10b981;
  margin: 24px 0 12px;
  font-weight: 600;
  scroll-margin-top: 80px;
}

:deep(.markdown-body h1) { font-size: 28px; }
:deep(.markdown-body h2) { font-size: 22px; }
:deep(.markdown-body h3) { font-size: 18px; }

:deep(.markdown-body p) {
  margin: 12px 0;
}

:deep(.markdown-body pre) {
  background: #f6f8fa;
  padding: 12px;
  overflow-x: auto;
  font-size: 13px;
  margin: 12px 0;
}

:deep(.markdown-body code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
</style>
