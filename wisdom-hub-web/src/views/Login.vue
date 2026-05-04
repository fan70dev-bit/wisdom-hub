<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-illustration">
        <img src="@/assets/Login.png" alt="Wisdom Hub Illustration" />
      </div>

      <div class="login-form-section">
        <div class="form-header">
          <h1 class="brand-title">Wisdom Hub</h1>
          <p class="brand-subtitle">欢迎来到你的智慧花园枢纽</p>
        </div>

        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
          <el-form-item label="电子邮箱" prop="email">
            <el-input 
              v-model="loginForm.email" 
              placeholder="请输入你的邮箱" 
              prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-input-group">
              <el-input 
                v-model="loginForm.code" 
                placeholder="6位验证码" 
                prefix-icon="Lock"
                maxlength="6"
              />
              <el-button 
                :disabled="counting" 
                @click="sendCode" 
                class="send-btn"
              >
                {{ counting ? `${counter}s 后获取` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">
            开启探索
          </el-button>
        </el-form>

        <div class="form-footer">
          <span class="privacy-tip">登录即代表你同意我们的《隐私政策》</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const counting = ref(false)
const counter = ref(60)

const loginForm = reactive({
  email: '',
  code: ''
})

const loginRules = {
  email: [
    { required: true, message: '邮箱不能缺席哦', trigger: 'blur' },
    { type: 'email', message: '邮箱格式好像不太对劲', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码是6位数字', trigger: 'blur' }
  ]
}

// 发送验证码逻辑
const sendCode = async () => {
  if (!loginForm.email) return ElMessage.warning('先填一下邮箱吧')
  try {
    // 将参数放在对象里作为第二个参数，Axios 会自动将其转为 Request Body
    await request.post('/auth/send-code', { 
      email: loginForm.email 
    })
    ElMessage.success('验证码已飞往你的邮箱')
    startCountdown()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '发送失败')
  }
}

const startCountdown = () => {
  counting.value = true
  const timer = setInterval(() => {
    counter.value--
    if (counter.value <= 0) {
      clearInterval(timer)
      counting.value = false
      counter.value = 60
    }
  }, 1000)
}

// 登录逻辑重构：保存 accountId 和 avatarUrl
const handleLogin = async () => {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm)
    const { token, email, username, accountId, avatarUrl } = res.data.data
    
    // 存储重构后的关键字段
    localStorage.setItem('token', token)
    localStorage.setItem('userEmail', email)
    localStorage.setItem('username', username)
    localStorage.setItem('accountId', accountId)
    localStorage.setItem('avatarUrl', avatarUrl)
    
    ElMessage.success(`欢迎回来，${username}！`)
    router.push('/garden')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f2f7e8; /* 提取自图片的奶油绿背景 */
}

.login-box {
  width: 900px;
  height: 550px;
  background: white;
  display: flex;
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(140, 176, 107, 0.2);
  overflow: hidden;
}

.login-illustration {
  flex: 1.2;
  background: #fdfdfd;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.login-illustration img {
  max-width: 100%;
  height: auto;
}

.login-form-section {
  flex: 1;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-title {
  font-size: 32px;
  color: #4a5d23;
  margin-bottom: 8px;
}

.brand-subtitle {
  color: #8cb06b;
  margin-bottom: 40px;
  font-size: 14px;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.send-btn {
  background-color: #f2f7e8 !important;
  color: #8cb06b !important;
  border-color: #8cb06b !important;
}

.submit-btn {
  width: 100%;
  height: 45px;
  background-color: #8cb06b !important;
  border-color: #8cb06b !important;
  font-size: 16px;
  margin-top: 20px;
  border-radius: 8px;
}

.submit-btn:hover {
  background-color: #7a9a5d !important;
}

.privacy-tip {
  font-size: 12px;
  color: #999;
  display: block;
  text-align: center;
  margin-top: 24px;
}
</style>