<template>
  <div class="my-favorites">
    <h2 class="page-title">我的收藏</h2>

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
        <el-button
          class="remove-btn"
          size="small"
          type="danger"
          link
          @click.stop="handleRemoveFavorite(doc.id)"
        >
          <el-icon><Delete /></el-icon>取消收藏
        </el-button>
      </el-card>
    </div>

    <el-empty v-if="!loading && docs.length === 0" description="还没有收藏任何文档" />

    <div class="pagination" v-if="total > 0">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyFavorites, removeFavorite } from '@/api/favorite'

const router = useRouter()
const loading = ref(false)
const docs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)

async function loadDocs() {
  loading.value = true
  try {
    const data = await getMyFavorites({ page: page.value, size: size.value })
    docs.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/doc/${id}`)
}

async function handleRemoveFavorite(docId) {
  try {
    await removeFavorite(docId)
    ElMessage.success('已取消收藏')
    loadDocs()
  } catch (e) {
    // 拦截器已提示
  }
}

function handlePageChange(p) {
  page.value = p
  loadDocs()
}

onMounted(loadDocs)
</script>

<style scoped>
.page-title {
  font-size: 22px;
  margin-bottom: 16px;
}
.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 200px;
}
.doc-card {
  cursor: pointer;
  position: relative;
}
.doc-card h3 {
  font-size: 16px;
  margin: 0 0 8px;
  padding-right: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.doc-card p {
  color: #666;
  font-size: 13px;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.doc-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-top: 12px;
}
.doc-footer span {
  display: flex;
  align-items: center;
  gap: 2px;
}
.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
}
.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>
