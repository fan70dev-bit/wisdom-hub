<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>Wisdom Hub 登录</h2>
      <el-form :model="loginForm">
        <el-form-item>
          <el-input v-model="loginForm.email" placeholder="请输入邮箱">
            <template #append>
              <el-button @click="handleSendCode" :disabled="!!timer">
                {{ timer ? `${countdown}s 后重发` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.code" placeholder="6位验证码" maxlength="6"></el-input>
        </el-form-item>
        <el-button type="primary" @click="handleLogin" style="width: 100%">登录 / 注册</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router' // 1. 引入路由工具

const router = useRouter() // 2. 初始化路由实例

const loginForm = reactive({
  email: '',
  code: ''
})

const countdown = ref(60)
const timer = ref<any>(null)

// 发送验证码
const handleSendCode = async () => {
  if (!loginForm.email) return ElMessage.warning('请先输入邮箱')
  try {
    await request.post('/auth/send-code', { email: loginForm.email })
    ElMessage.success('验证码已发送')
    
    timer.value = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer.value)
        timer.value = null
        countdown.value = 60
      }
    }, 1000)
  } catch (error) {
    // 错误处理已在 request.ts 拦截器中完成
  }
}

// 登录
const handleLogin = async () => {
  if (!loginForm.email || !loginForm.code) {
    return ElMessage.warning('请填写完整信息')
  }

  try {
    const res: any = await request.post('/auth/login', loginForm)
    // 注意：这里要根据你后端返回的实际结构判断，通常是 res.success 或 res.code === 200
    if (res.success || res.data) {
      ElMessage.success('登录成功')
      
      // 3. 存储 Token
      const token = res.data.token || res.data
      localStorage.setItem('token', token)
      
      // 4. 【关键步骤】执行页面跳转，进入主页
      router.push('/') 
    }
  } catch (error) {
    console.error('登录失败', error)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.login-card { width: 400px; }
</style>