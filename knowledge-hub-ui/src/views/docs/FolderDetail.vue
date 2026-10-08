<template>
  <div class="folder-detail">
    <el-button link @click="router.push('/')">
      <el-icon><ArrowLeft /></el-icon>返回知识库
    </el-button>

    <template v-if="folder">
      <div class="folder-header">
        <div class="folder-header-left">
          <h1>📁 {{ folder.name }}</h1>
          <div class="folder-meta">
            <el-tag :type="folder.isPublic === 1 ? 'success' : 'info'" size="small">
              {{ folder.isPublic === 1 ? '公开' : '私有' }}
            </el-tag>
            <span>共 {{ total }} 篇文档</span>
          </div>
        </div>
        <div class="folder-header-right" v-if="canEdit">
          <el-button @click="openEdit">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>

      <div class="doc-grid" v-loading="loading">
        <el-card
          v-for="doc in docs"
          :key="doc.id"
          class="doc-card"
          shadow="hover"
          @click="router.push(`/doc/${doc.id}`)"
        >
          <h3>{{ doc.title }}</h3>
          <p>{{ doc.summary || '（暂无摘要）' }}</p>
          <div class="doc-footer">
            <span>{{ doc.uploaderName }}</span>
            <span><el-icon><View /></el-icon>{{ doc.viewCount }}</span>
            <span><el-icon><Star /></el-icon>{{ doc.favoriteCount }}</span>
          </div>
        </el-card>
      </div>
      <el-empty v-if="!loading && docs.length === 0" description="这个文件夹还没有文档" />

      <div class="pagination" v-if="total > 0">
        <el-pagination
          :current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </template>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑文件夹" width="420px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="文件夹名">
          <el-input v-model="editForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="是否公开">
          <el-radio-group v-model="editForm.isPublic">
            <el-radio :value="1">公开</el-radio>
            <el-radio :value="0">私有</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPublicDocPage } from '@/api/doc'
import { getCategoryList, deleteMyFolder, updateMyFolder } from '@/api/category'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const folder = ref(null)
const docs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)
const editVisible = ref(false)
const editForm = ref({ name: '', isPublic: 1 })

const canEdit = computed(() => {
  if (!folder.value || !userStore.user) return false
  return folder.value.ownerId === userStore.user.id || userStore.user.role === 'ADMIN'
})

async function loadFolder() {
  // 没有单独的"查分类详情"接口，从列表里查
  const all = (await getCategoryList()) || []
  folder.value = all.find((c) => c.id === Number(route.params.id)) || null
  if (!folder.value) {
    ElMessage.error('文件夹不存在')
    router.push('/')
  }
}

async function loadDocs() {
  loading.value = true
  try {
    const data = await getPublicDocPage({
      page: page.value,
      size: size.value,
      categoryId: Number(route.params.id)
    })
    docs.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function handleDelete() {
  ElMessageBox.confirm(`确定删除文件夹"${folder.value.name}"吗？文件夹下必须为空。`, '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteMyFolder(folder.value.id)
      ElMessage.success('已删除')
      router.push('/')
    } catch (e) {
      // 拦截器已提示
    }
  }).catch(() => {})
}

function openEdit() {
  editForm.value = { name: folder.value.name, isPublic: folder.value.isPublic }
  editVisible.value = true
}

async function handleEditSave() {
  try {
    await updateMyFolder(folder.value.id, editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    loadFolder()
  } catch (e) {
    // 拦截器已提示
  }
}

function handlePageChange(p) {
  page.value = p
  loadDocs()
}

onMounted(() => {
  loadFolder()
  loadDocs()
})

watch(() => route.params.id, () => {
  page.value = 1
  loadFolder()
  loadDocs()
})
</script>

<style scoped>
.folder-detail {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.folder-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin: 20px 0 24px;
}

.folder-header h1 {
  font-size: 24px;
  margin: 0 0 8px;
  color: #1f2937;
}

.folder-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #9ca3af;
  font-size: 13px;
}

.folder-header-right {
  display: flex;
  gap: 8px;
}

.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.doc-card {
  cursor: pointer;
}

.doc-card h3 {
  font-size: 15px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-card p {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.doc-footer {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
  align-items: center;
  flex-wrap: wrap;
}

.doc-footer span {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>
