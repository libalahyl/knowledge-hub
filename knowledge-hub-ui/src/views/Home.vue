<template>
  <el-container class="home">
    <!-- 顶栏 -->
    <el-header class="home-header">
      <div class="logo" @click="router.push('/')">
        <el-icon :size="24" color="#10b981"><Reading /></el-icon>
        <span>知识库</span>
      </div>
      <div class="user-area">
        <el-button link @click="aiCollapsed = !aiCollapsed" style="margin-right: 16px;">
          <el-icon :size="20"><Fold v-if="!aiCollapsed" /><Expand v-else /></el-icon>
        </el-button>
        <NotificationBell v-if="userStore.user" />
        <el-dropdown @command="handleCommand">
          <span class="user-name">
            {{ userStore.user?.nickname || userStore.user?.username || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="home-body">
      <!-- 左侧菜单 -->
      <el-aside width="200px" class="home-aside">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/">
            <el-icon><Document /></el-icon>
            <span>知识库</span>
          </el-menu-item>
          <el-menu-item index="/my-folders">
            <el-icon><FolderOpened /></el-icon>
            <span>我的文件夹</span>
          </el-menu-item>
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/uploads">
            <el-icon><Upload /></el-icon>
            <span>我的上传</span>
          </el-menu-item>
          <el-menu-item index="/conversations">
            <el-icon><ChatDotRound /></el-icon>
            <span>我的问答</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories" v-if="isAdmin">
            <el-icon><Setting /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports" v-if="isAdmin">
            <el-icon><Warning /></el-icon>
            <span>举报处理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 中间内容 + 右侧 AI -->
      <el-container>
        <el-main class="home-main">
          <router-view />
        </el-main>
        <el-aside :width="aiCollapsed ? '0' : '380px'" class="home-ai">
          <AiChatPanel v-if="!aiCollapsed" :doc-id="currentDocId" />
        </el-aside>
      </el-container>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import AiChatPanel from '@/components/AiChatPanel.vue'
import NotificationBell from '@/components/NotificationBell.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const aiCollapsed = ref(false)
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const activeMenu = computed(() => route.path)
const currentDocId = computed(() => (route.params.id ? Number(route.params.id) : null))

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await logout()
    } catch (e) {
      // 登出接口失败也继续清理本地
    }
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.home {
  height: 100vh;
}
.home-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  cursor: pointer;
}
.user-area {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.user-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #303133;
}
.home-body {
  flex: 1;
  overflow: hidden;
}
.home-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}
.home-main {
  padding: 20px;
  overflow: auto;
  background: #f5f7fa;
}
.home-ai {
  background: #fff;
  border-left: 1px solid #e4e7ed;
  transition: width 0.3s;
  overflow: hidden;
}
</style>
