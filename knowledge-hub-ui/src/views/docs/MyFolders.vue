<template>
  <div class="my-folders">
    <div class="page-header">
      <h2 class="page-title">我的文件夹</h2>
      <el-button type="primary" @click="folderVisible = true">
        <el-icon><FolderAdd /></el-icon>新建文件夹
      </el-button>
    </div>

    <div class="folder-grid" v-loading="loading">
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
            <el-tag :type="f.isPublic === 1 ? 'success' : 'info'" size="small">
              {{ f.isPublic === 1 ? '公开' : '私有' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && folders.length === 0" description="还没有创建文件夹" />

    <CreateFolderDialog v-model="folderVisible" @success="loadFolders" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyFolders } from '@/api/category'
import CreateFolderDialog from '@/components/CreateFolderDialog.vue'

const router = useRouter()
const folders = ref([])
const loading = ref(false)
const folderVisible = ref(false)

async function loadFolders() {
  loading.value = true
  try {
    folders.value = (await getMyFolders()) || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function goFolder(id) {
  router.push(`/folder/${id}`)
}

onMounted(loadFolders)
</script>

<style scoped>
.my-folders {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  margin: 0;
  color: #1f2937;
}

.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  min-height: 100px;
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
</style>
