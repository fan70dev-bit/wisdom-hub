<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="title">个人资料 (Profile)</span>
          <el-tag type="success" effect="light">UID: {{ userInfo.accountId }}</el-tag>
        </div>
      </template>

      <div class="profile-section">
        <div class="avatar-uploader-wrapper">
          <el-upload
            class="avatar-uploader"
            action="/api/file/upload" 
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            name="file"
          >
            <el-avatar v-if="editForm.avatarUrl" :size="120" :src="editForm.avatarUrl" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            <div class="upload-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          <p class="avatar-tip">JPG/PNG, 小于 2MB</p>
        </div>

        <el-form :model="editForm" label-position="top" class="edit-form">
          <el-form-item label="电子邮箱 (不可修改)">
            <el-input v-model="userInfo.email" disabled prefix-icon="Message" />
          </el-form-item>

          <el-form-item label="显示名称 (Username)">
            <el-input v-model="editForm.username" placeholder="请输入新昵称" prefix-icon="User" />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" class="save-btn" @click="handleUpdate" :loading="loading">
              保存资料
            </el-button>
          </div>
        </el-form>
      </div>

    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Camera, Message, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)

// 基础信息展示 (从缓存初始化)
const userInfo = reactive({
  accountId: localStorage.getItem('accountId'),
  email: localStorage.getItem('userEmail'), // 后端接口未返回 email，先从缓存拿
  username: localStorage.getItem('username'),
  avatarUrl: localStorage.getItem('avatarUrl')
})

// 修改表单 (绑定到预览)
const editForm = reactive({
  username: userInfo.username,
  avatarUrl: userInfo.avatarUrl // 这里用于展示上传后的临时链接或原链接
})

// ========== 头像上传逻辑 ==========

// 上传前校验
const beforeAvatarUpload = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleAvatarSuccess = (response) => {
  // 假设后端上传接口返回 JSON: { code: 200, data: { url: 'http://.../new_avatar.jpg' } }
  if (response.code === 200) {
    // 仅仅更新预览，不立刻保存到数据库
    editForm.avatarUrl = response.data.url 
    ElMessage.success('头像上传成功，点击“保存资料”生效')
  } else {
    ElMessage.error('上传失败: ' + (response.message || '网络错误'))
  }
}

// ========== 资料更新逻辑 ==========

const handleUpdate = async () => {
  if (!editForm.username) return ElMessage.warning('昵称是必填项')
  
  loading.value = true
  try {
    // 这里调用的依然是原来的 PUT /user/profile 接口
    // 只是现在的参数是从 upload 拿到的临时 URL 和新昵称
    await request.put('/user/profile', editForm)
    
    // 更新成功后同步本地缓存和显示
    localStorage.setItem('username', editForm.username)
    localStorage.setItem('avatarUrl', editForm.avatarUrl)
    
    userInfo.username = editForm.username
    userInfo.avatarUrl = editForm.avatarUrl
    
    ElMessage.success('资料已同步至你的智慧花园！')
    
    // 强制刷新以更新 Layout 顶栏头像（简单粗暴但有效）
    setTimeout(() => {
      window.location.reload()
    }, 1000)
    
  } catch (err) {
    // 12小时冷却业务异常会自动由 GlobalExceptionHandler 处理并抛出 ElMessage
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  padding: 40px;
  display: flex;
  justify-content: center;
  background-color: #F9FBF6;
  min-height: calc(100vh - 64px);
}

.profile-card {
  width: 100%;
  max-width: 650px;
  border-radius: 20px;
  border: none;
  box-shadow: 0 10px 30px rgba(140, 176, 107, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #4A5D23;
}

.profile-section {
  display: flex;
  gap: 50px;
  padding: 30px 0 10px;
  align-items: flex-start;
}

/* 上传头像样式 */
.avatar-uploader-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-uploader {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 4px solid #fff;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.avatar-uploader:hover {
  transform: scale(1.03);
  box-shadow: 0 8px 20px rgba(140, 176, 107, 0.2);
}

.avatar-preview {
  width: 100%;
  height: 100%;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  background-color: #f0f2f5;
}

/* 悬停遮罩 */
.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 12px;
}

.avatar-uploader:hover .upload-mask {
  opacity: 1;
}

.avatar-tip {
  font-size: 11px;
  color: #999;
}

.edit-form {
  flex: 1;
}

.form-actions {
  margin-top: 25px;
  text-align: right;
}

.save-btn {
  background-color: #8CB06B !important;
  border: none !important;
  width: 100%;
  height: 40px;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #fdfdfd;
  color: #aaa;
}
</style>