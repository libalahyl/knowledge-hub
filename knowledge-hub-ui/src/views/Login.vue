<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>知识库</h1>
        <p>AI 个人知识库</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading"
                   @click="handleLogin">登录</el-button>
      </el-form>
      <p class="tip">默认账号：admin / 123456</p>
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="$router.push('/register')">去注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return // 校验失败，不提交
  }
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    userStore.setLogin(data.token, data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // request.js 拦截器已提示错误信息，这里忽略
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h1 {
  color: #10b981;
  font-size: 28px;
  margin: 0 0 8px;
}
.login-header p {
  color: #999;
  margin: 0;
  font-size: 14px;
}
.tip {
  text-align: center;
  color: #bbb;
  font-size: 12px;
  margin-top: 16px;
}
.login-footer {
  text-align: center;
  margin-top: 8px;
  color: #9ca3af;
  font-size: 13px;
}
</style>
