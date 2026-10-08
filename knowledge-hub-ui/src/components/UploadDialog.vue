<template>
  <el-dialog
    :model-value="modelValue"
    title="上传文档"
    width="500px"
    @update:model-value="(v) => emit('update:modelValue', v)"
    @closed="resetForm"
  >
    <el-form label-width="80px">
      <el-form-item label="文件" required>
        <el-upload
          v-model:file-list="fileList"
          :auto-upload="false"
          multiple
          :limit="20"
          accept=".md"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>选择 .md 文件（可多选）
          </el-button>
        </el-upload>
        <div v-if="fileList.length" class="file-list">
          <div v-for="(f, i) in fileList" :key="i" class="file-item">
            <span>{{ f.name }}</span>
            <span class="file-size">{{ formatSize(f.size) }}</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="选择文件夹">
        <el-select v-model="form.categoryId" placeholder="不选择文件夹" clearable style="width: 100%;">
          <el-option v-for="f in myFolders" :key="f.id" :label="f.name" :value="f.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">批量上传</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadDocsBatch } from '@/api/doc'
import { getMyFolders } from '@/api/category'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'success'])

const submitting = ref(false)
const myFolders = ref([])
const fileList = ref([])
const form = reactive({ categoryId: null })

watch(() => props.modelValue, (v) => {
  if (v) {
    loadMyFolders()
  }
})

async function loadMyFolders() {
  try {
    myFolders.value = (await getMyFolders()) || []
  } catch (e) {
    // 拦截器已提示
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function resetForm() {
  form.categoryId = null
  fileList.value = []
}

async function handleSubmit() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  submitting.value = true
  try {
    const files = fileList.value.map((f) => f.raw)
    const res = await uploadDocsBatch(files, form.categoryId)
    if (res.failCount > 0) {
      ElMessage.warning(`成功 ${res.successCount} 个，失败 ${res.failCount} 个`)
      console.warn('失败清单：', res.failList)
    } else {
      ElMessage.success(`成功上传 ${res.successCount} 个文件`)
    }
    emit('success')
    emit('update:modelValue', false)
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.file-list {
  margin-top: 12px;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 12px;
}
.file-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
  color: #374151;
  border-bottom: 1px solid #f3f4f6;
}
.file-item:last-child {
  border-bottom: none;
}
.file-size {
  color: #9ca3af;
  font-size: 12px;
}
</style>
