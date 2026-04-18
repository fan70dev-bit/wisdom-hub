<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">Wisdom Hub</div>
      <el-menu router :default-active="$route.path" background-color="#304156" text-color="#fff">
        <el-menu-item index="/home">首页看板</el-menu-item>
        <el-menu-item index="/ai">AI 助手 (待开发)</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>智慧枢纽管理后台</span>
        <el-button type="text" @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.el-aside { background-color: #304156; }
.logo { color: white; text-align: center; padding: 20px; font-weight: bold; }
.el-header { border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
</style>