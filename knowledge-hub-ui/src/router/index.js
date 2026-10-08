import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/views/Home.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/docs/DocList.vue'), meta: { title: '知识库' } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/docs/MyFavorites.vue'), meta: { title: '我的收藏' } },
      { path: 'uploads', name: 'Uploads', component: () => import('@/views/docs/MyUploads.vue'), meta: { title: '我的上传' } },
      { path: 'conversations', name: 'Conversations', component: () => import('@/views/docs/MyConversations.vue'), meta: { title: '我的问答' } },
      { path: 'doc/:id', name: 'DocDetail', component: () => import('@/views/docs/DocDetail.vue'), meta: { title: '文档详情' } },
      { path: 'folder/:id', name: 'FolderDetail', component: () => import('@/views/docs/FolderDetail.vue'), meta: { title: '文件夹' } },
      { path: 'my-folders', name: 'MyFolders', component: () => import('@/views/docs/MyFolders.vue'), meta: { title: '我的文件夹' } },
      { path: 'admin/categories', name: 'AdminCategories', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '分类管理', requiresAdmin: true } },
      { path: 'admin/reports', name: 'AdminReports', component: () => import('@/views/admin/ReportManage.vue'), meta: { title: '举报处理', requiresAdmin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} · 知识库` : '知识库'

  if (to.path === '/login' || to.path === '/register') {
    if (userStore.token) return next('/')
    return next()
  }
  if (!userStore.token) {
    return next('/login')
  }
  // 管理员权限
  if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    ElMessage.warning('无权限')
    return next('/')
  }
  next()
})

export default router
