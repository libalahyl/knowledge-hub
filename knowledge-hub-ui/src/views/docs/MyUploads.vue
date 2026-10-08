<template>
  <div class="my-uploads">
    <h2 class="page-title">我的上传</h2>

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
          <span class="create-time">{{ formatTime(doc.createTime) }}</span>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && docs.length === 0" description="还没有上传任何文档" />

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
import { useUserStore } from '@/stores/user'
import { getDocPage } from '@/api/doc'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const docs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)

async function loadDocs() {
  loading.value = true
  try {
    const data = await getDocPage({
      page: page.value,
      size: size.value,
      uploaderId: userStore.user?.id
    })
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

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
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
}
.doc-card h3 {
  font-size: 16px;
  margin: 0 0 8px;
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
  flex-wrap: wrap;
}
.doc-footer span {
  display: flex;
  align-items: center;
  gap: 2px;
}
.create-time {
  margin-left: auto;
}
.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>
