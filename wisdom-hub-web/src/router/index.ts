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
        { path: 'home', component: () => import('../views/Home.vue') }
      ]
    }
  ]
})

// 路由守卫：没 Token 不准进主页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router