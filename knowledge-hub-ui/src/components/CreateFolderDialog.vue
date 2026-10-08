<template>
  <el-dialog
    :model-value="modelValue"
    title="新建文件夹"
    width="420px"
    :close-on-click-modal="false"
    @update:model-value="(v) => emit('update:modelValue', v)"
    @closed="handleClosed"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
      <el-form-item label="文件夹名" prop="name">
        <el-input
          v-model="form.name"
          placeholder="如：Java 学习笔记"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="是否公开">
        <el-radio-group v-model="form.isPublic">
          <el-radio :value="1">公开（所有人可见）</el-radio>
          <el-radio :value="0">私有（仅自己可见）</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createMyFolder } from '@/api/category'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ name: '', isPublic: 1 })

const rules = {
  name: [
    { required: true, message: '请输入文件夹名', trigger: 'blur' },
    { max: 50, message: '文件夹名不能超过 50 字', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (v) => {
  if (v) {
    form.name = ''
    form.isPublic = 1
    nextTick(() => formRef.value?.clearValidate())
  }
})

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await createMyFolder({ name: form.name, isPublic: form.isPublic })
    ElMessage.success('文件夹创建成功')
    emit('success')
    emit('update:modelValue', false)
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

function handleClosed() {
  form.name = ''
  form.isPublic = 1
}
</script>
