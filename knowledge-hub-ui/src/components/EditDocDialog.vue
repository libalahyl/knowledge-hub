<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑文档"
    width="500px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="摘要" prop="summary">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入摘要（可选）" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="选择分类（可选）" clearable style="width: 100%;">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { updateDoc } from '@/api/doc'
import { getVisibleCategories } from '@/api/category'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  doc: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const form = reactive({
  title: '',
  summary: '',
  categoryId: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 打开时回填数据
watch(() => props.modelValue, (val) => {
  if (val && props.doc) {
    form.title = props.doc.title || ''
    form.summary = props.doc.summary || ''
    form.categoryId = props.doc.categoryId || null
  }
})

async function handleSubmit() {
  if (!props.doc) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return // 校验失败
  }
  submitting.value = true
  try {
    await updateDoc(props.doc.id, {
      title: form.title,
      summary: form.summary,
      categoryId: form.categoryId
    })
    ElMessage.success('保存成功')
    emit('success')
    emit('update:modelValue', false)
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  getVisibleCategories().then((list) => { categories.value = list || [] }).catch(() => {})
})
</script>
