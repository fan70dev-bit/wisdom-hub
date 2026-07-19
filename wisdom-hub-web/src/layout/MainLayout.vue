<template>
  <el-container class="main-layout">
    <el-aside width="260px" class="aside-menu">
      <div class="brand-section">
        <img src="@/assets/Login.png" class="brand-logo" alt="logo" />
        <span class="brand-name">Wisdom Hub</span>
      </div>

      <div class="menu-wrapper">
        <el-menu
          :default-active="$route.path"
          router
          class="custom-menu"
        >
          <el-menu-item index="/garden">
            <el-icon><House /></el-icon>
            <template #title>首页 (Garden)</template>
          </el-menu-item>

          <el-menu-item index="/explore">
            <el-icon><Compass /></el-icon>
            <template #title>探索 (Explore)</template>
          </el-menu-item>

          <el-menu-item index="/search">
            <el-icon><Search /></el-icon>
            <template #title>搜索 (Search)</template>
          </el-menu-item>

          <el-menu-item index="/ai">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI Assistant</template>
          </el-menu-item>

          <el-menu-item index="/bookmarks">
            <el-icon><CollectionTag /></el-icon>
            <template #title>书签 (Bookmarks)</template>
          </el-menu-item>

          <div class="post-btn-wrapper">
            <el-button type="primary" class="post-action-btn" @click="$router.push('/post/create')">
              <el-icon><Plus /></el-icon>
              <span>发布新动态</span>
            </el-button>
          </div>
        </el-menu>
      </div>

      <div class="bottom-settings">
        <el-dropdown trigger="click" placement="top">
          <div class="setting-trigger">
            <el-icon><Setting /></el-icon>
            <span>设置 & 账号</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="setting-dropdown">
              <el-dropdown-item icon="User" @click="$router.push('/profile')">账号信息修改</el-dropdown-item>
              <el-dropdown-item icon="SwitchButton" @click="handleLogout">退出登录</el-dropdown-item>
              <el-dropdown-item divided icon="Warning" style="color: #F56C6C" @click="handleDeactivate">
                注销账号
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <el-main class="content-main">
      <div class="top-info-bar">
        <div class="page-title">{{ currentPathName }}</div>
        <div class="user-info">
          <span class="user-id">ID: {{ userAccountId }}</span>
          <el-avatar :size="32" :src="userAvatar" />
        </div>
      </div>
      
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '@/utils/request'
import { 
  House, Compass, Search, CollectionTag, 
  Plus, Setting, User, Refresh, Warning, ChatDotRound 
} from '@element-plus/icons-vue'
import { SwitchButton } from '@element-plus/icons-vue' // 记得导入图标

const route = useRoute()
const router = useRouter()

// 响应式数据
const userAccountId = ref(localStorage.getItem('accountId') || '')
const userAvatar = ref(localStorage.getItem('avatarUrl') || '')

// 页面标题映射
const currentPathName = computed(() => {
  const titles = {
    '/garden': '我的花园',
    '/explore': '探索广场',
    '/search': '全局搜索',
    '/ai': 'AI Assistant',
    '/bookmarks': '个人收藏',
    '/profile': '账号设置',
    '/post/create': '发布新动态'
  }
  return titles[route.path] || 'Wisdom Hub'
})


const handleLogout = () => {
  // 1. 清空本地存储的所有用户信息
  localStorage.clear()
  
  // 2. 提示退出成功
  ElMessage.success('已安全退出智慧花园')
  
  // 3. 强制跳转回登录页
  router.push('/login')
}

// --- 新增业务逻辑 ---

// 1. 注销账号逻辑 (对接后端逻辑注销)
const handleDeactivate = () => {
  ElMessageBox.confirm(
    '注销后，您的账号将进行匿名化处理，此操作不可撤销。确定要离开 Wisdom Hub 吗？',
    '安全确认',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '我再想想',
      type: 'warning',
      buttonSize: 'default',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      // 调用后端注销接口 (status -> 2, email -> anonymized)
      await request.post('/user/deactivate')
      
      // 清理前端缓存
      localStorage.clear()
      
      ElMessage({
        type: 'success',
        message: '账号已注销，期待在未来的花园里与你重逢。',
        duration: 3000
      })
      
      // 跳回登录页
      router.push('/login')
    } catch (err) {
      console.error('注销请求失败:', err)
    }
  }).catch(() => {
    // 用户取消操作，无需处理
  })
}

// 2. 语言切换提示
const handleLangChange = () => {
  ElMessage.info({
    message: '多语言模式 (中/日/英) 正在紧急灌溉中，敬请期待！',
    showClose: true
  })
}

// 挂载时再次确认数据（防止存储延迟）
onMounted(() => {
  userAccountId.value = localStorage.getItem('accountId') || 'Unknown'
  userAvatar.value = localStorage.getItem('avatarUrl') || ''
})
</script>

<style scoped>
/* 核心配色方案 */
:root {
  --hub-green: #8CB06B;
  --hub-light-green: #F2F7E8;
  --hub-dark-green: #4A5D23;
}

.main-layout {
  height: 100vh;
  background-color: #F9FBF6; /* 极淡的底色 */
}

.aside-menu {
  background-color: #fff;
  border-right: 1px solid #EDF2E9;
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}

.brand-section {
  padding: 0 25px 30px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
}

.brand-name {
  font-size: 20px;
  font-weight: 800;
  color: #4A5D23;
  letter-spacing: -0.5px;
}

.custom-menu {
  border-right: none;
  flex: 1;
}

/* 重点标注的发帖按钮 */
.post-btn-wrapper {
  padding: 20px;
}

.post-action-btn {
  width: 100%;
  height: 48px;
  background-color: #8CB06B !important;
  border: none !important;
  border-radius: 12px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(140, 176, 107, 0.3);
  transition: all 0.3s;
}

.post-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(140, 176, 107, 0.4);
}

/* 侧边栏菜单项样式 */
:deep(.el-menu-item) {
  height: 54px;
  margin: 4px 15px;
  border-radius: 10px;
  color: #66784d;
}

:deep(.el-menu-item.is-active) {
  background-color: #F2F7E8 !important;
  color: #8CB06B !important;
  font-weight: bold;
}

.bottom-settings {
  padding: 20px;
  border-top: 1px solid #EDF2E9;
}

.setting-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #66784d;
  padding: 10px;
  border-radius: 8px;
  transition: background 0.2s;
}

.setting-trigger:hover {
  background-color: #F2F7E8;
}

.content-main {
  padding: 0;
  background-color: #fff;
}

.top-info-bar {
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 1px solid #F2F7E8;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #4A5D23;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-id {
  font-size: 12px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
