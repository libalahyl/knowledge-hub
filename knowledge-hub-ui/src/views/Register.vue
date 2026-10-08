<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1>知识库</h1>
        <p>创建你的账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large" :prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large"
                    :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading"
                   @click="handleRegister">注册</el-button>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-button link type="primary" @click="$router.push('/login')">去登录</el-button>
      </div>
      <p class="register-tip">注册后为普通用户，可使用知识库浏览、上传、评论、AI 问答等功能。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Lock } from '@element-plus/icons-vue'
import request from '@/request/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度 6-100', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', {
      username: form.username,
      password: form.password,
      nickname: form.nickname
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.register-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.register-header {
  text-align: center;
  margin-bottom: 30px;
}
.register-header h1 {
  color: #10b981;
  font-size: 28px;
  margin: 0 0 8px;
}
.register-header p {
  color: #999;
  margin: 0;
  font-size: 14px;
}
.register-footer {
  text-align: center;
  margin-top: 16px;
  color: #9ca3af;
  font-size: 13px;
}
.register-tip {
  text-align: center;
  color: #bbb;
  font-size: 12px;
  margin-top: 12px;
}
</style>
