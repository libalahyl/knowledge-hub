<template>
  <div class="doc-list">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="query.title"
          placeholder="搜索文档..."
          clearable
          style="width: 260px;"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-select
          v-model="query.categoryId"
          placeholder="全部分类"
          clearable
          style="width: 160px;"
          @change="handleSearch"
        >
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button @click="folderVisible = true">
          <el-icon><FolderAdd /></el-icon>新建文件夹
        </el-button>
        <el-button type="primary" @click="uploadVisible = true">
          <el-icon><Upload /></el-icon>上传文档
        </el-button>
      </div>
    </div>

    <!-- 文件夹区 -->
    <section class="section" v-if="folders.length > 0">
      <h3 class="section-title">📁 文件夹</h3>
      <div class="folder-grid">
        <div
          v-for="f in folders"
          :key="f.id"
          class="folder-card"
          @click="goFolder(f.id)"
        >
          <el-icon class="folder-icon" :size="28" color="#10b981">
            <Folder />
          </el-icon>
          <div class="folder-info">
            <div class="folder-name">{{ f.name }}</div>
            <div class="folder-meta">
              <el-tag v-if="f.isPublic === 0" size="small" type="info">私有</el-tag>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 文档区 -->
    <section class="section">
      <h3 class="section-title">📄 文档</h3>
      <div class="doc-grid" v-loading="loading">
        <el-card
          v-for="doc in docs"
          :key="doc.id"
          class="doc-card"
          shadow="hover"
          @click="goDetail(doc.id)"
        >
          <h3>{{ doc.title }}</h3>
          <p>{{ doc.summary || '（暂无摘要）' }}</p>
          <div class="doc-footer">
            <el-tag size="small" type="success" effect="plain" v-if="doc.categoryName">
              {{ doc.categoryName }}
            </el-tag>
            <span>{{ doc.uploaderName }}</span>
            <span><el-icon><View /></el-icon>{{ doc.viewCount }}</span>
            <span><el-icon><Star /></el-icon>{{ doc.favoriteCount }}</span>
          </div>
        </el-card>
      </div>
      <el-empty
        v-if="!loading && docs.length === 0"
        description="没有找到文档"
      />
    </section>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="query.page"
        :page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 弹窗 -->
    <CreateFolderDialog v-model="folderVisible" @success="loadFolders" />
    <UploadDialog v-model="uploadVisible" @success="loadDocs" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicDocPage } from '@/api/doc'
import { getVisibleCategories, getPublicFolders } from '@/api/category'
import CreateFolderDialog from '@/components/CreateFolderDialog.vue'
import UploadDialog from '@/components/UploadDialog.vue'

const router = useRouter()
const loading = ref(false)
const docs = ref([])
const folders = ref([])
const total = ref(0)
const categories = ref([])
const folderVisible = ref(false)
const uploadVisible = ref(false)

const query = reactive({
  page: 1,
  size: 12,
  title: '',
  categoryId: null
})

async function loadDocs() {
  loading.value = true
  try {
    const data = await getPublicDocPage({
      page: query.page,
      size: query.size,
      title: query.title,
      categoryId: query.categoryId
    })
    docs.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function loadFolders() {
  try {
    folders.value = (await getPublicFolders()) || []
  } catch (e) {
    // 拦截器已提示
  }
}

async function loadCategories() {
  try {
    categories.value = (await getVisibleCategories()) || []
  } catch (e) {
    // 拦截器已提示
  }
}

function handleSearch() {
  query.page = 1
  loadDocs()
}

function handlePageChange(p) {
  query.page = p
  loadDocs()
}

function goDetail(id) {
  router.push(`/doc/${id}`)
}

function goFolder(id) {
  router.push(`/folder/${id}`)
}

onMounted(() => {
  loadCategories()
  loadFolders()
  loadDocs()
})
</script>

<style scoped>
.doc-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.section {
  margin-bottom: 28px;
}

.section-title {
  font-size: 16px;
  color: #374151;
  margin: 0 0 12px;
  font-weight: 600;
}

/* 文件夹网格 */
.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.folder-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.folder-card:hover {
  background: #ecfdf5;
  border-color: #10b981;
  transform: translateY(-2px);
}

.folder-icon {
  flex-shrink: 0;
}

.folder-info {
  flex: 1;
  min-width: 0;
}

.folder-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-meta {
  margin-top: 4px;
}

/* 文档网格 */
.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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
