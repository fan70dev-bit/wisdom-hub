import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    {
      path: '/',
      component: () => import('../layout/MainLayout.vue'),
      redirect: '/home',
      children: [
        { path: 'home', component: () => import('../views/Home.vue') },
        // 👇 这里是新加的路由映射 👇
        { path: 'post', component: () => import('../views/PostEditor.vue') }
      ]
    }
  ]
})

// 路由守卫：没 Token 不准进主页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login' // 直接返回路径，不用调用 next()
  }
  // 如果不写 return，默认就是放行
})

export default router