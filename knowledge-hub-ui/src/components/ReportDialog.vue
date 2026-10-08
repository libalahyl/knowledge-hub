<template>
  <el-dialog
    :model-value="modelValue"
    title="举报文档"
    width="480px"
    :close-on-click-modal="false"
    @update:model-value="(v) => emit('update:modelValue', v)"
    @closed="handleClosed"
  >
    <div class="report-doc-title" v-if="doc">
      <strong>文档：</strong>{{ doc.title }}
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="举报原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="请详细描述举报原因..."
        />
      </el-form-item>
      <el-form-item label="快捷选择">
        <el-tag
          v-for="t in quickReasons"
          :key="t"
          class="quick-tag"
          @click="form.reason = t"
        >{{ t }}</el-tag>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { submitReport } from '@/api/report'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  doc: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const form = reactive({ reason: '' })
const quickReasons = ['内容有误', '涉嫌抄袭', '广告/垃圾内容', '排版混乱', '其他']

const rules = {
  reason: [
    { required: true, message: '请输入举报原因', trigger: 'blur' },
    { min: 5, message: '举报原因至少 5 个字', trigger: 'blur' }
  ]
}

// 打开时重置表单
watch(() => props.modelValue, (v) => {
  if (v) {
    form.reason = ''
    nextTick(() => formRef.value?.clearValidate())
  }
})

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  if (!props.doc) return
  submitting.value = true
  try {
    await submitReport({ docId: props.doc.id, reason: form.reason })
    ElMessage.success('举报成功，管理员会尽快处理')
    emit('success')
    emit('update:modelValue', false)
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

function handleClosed() {
  form.reason = ''
}
</script>

<style scoped>
.report-doc-title {
  margin-bottom: 16px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 14px;
  color: #374151;
}

.quick-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}
</style>
