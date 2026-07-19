import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import Garden from '@/views/Garden.vue' 

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/garden', 
    children: [
      {
        path: 'garden',
        name: 'Garden',
        component: Garden,
        meta: { title: '我的花园' }
      },
      {
        path: 'bookmarks',
        name: 'Bookmarks',
        component: () => import('@/views/Bookmarks.vue'),
        meta: { title: '个人收藏' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '账号设置' }
      },
      {
        path: 'explore',
        name: 'Explore',
        component: () => import('@/views/Explore.vue'),
        meta: { title: '探索广场' }
      },
      // ✨ 关键新增：发布动态页
      {
        path: 'post/create', 
        name: 'PostCreate',
        component: () => import('@/views/PostCreate.vue'), 
        meta: { title: '发布新动态' }
      },
      // ✨ 详情页（动态路由）
      {
        path: 'post/:id', 
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'), 
        meta: { title: '动态详情' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('@/views/AiChatView.vue'),
        meta: { title: 'AI Assistant' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
