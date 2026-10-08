<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>新增分类
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <el-table-column label="创建时间" width="180" align="center">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="还没有分类" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如 Java 基础" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  sort: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名', trigger: 'blur' },
    { max: 50, message: '分类名不能超过 50 字', trigger: 'blur' }
  ]
}

function formatTime(str) {
  if (!str) return ''
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadList() {
  loading.value = true
  try {
    list.value = (await getCategoryList()) || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogTitle.value = '新增分类'
  Object.assign(form, { id: null, name: '', sort: 0 })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row) {
  dialogTitle.value = '编辑分类'
  Object.assign(form, { id: row.id, name: row.name, sort: row.sort || 0 })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  try {
    if (form.id) {
      await updateCategory(form.id, { name: form.name, sort: form.sort })
    } else {
      await createCategory({ name: form.name, sort: form.sort })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类"${row.name}"吗？该分类下有文档时无法删除。`,
      '提示',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await deleteCategory(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(loadList)
</script>

<style scoped>
.category-manage {
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

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #1f2937;
}
</style>
