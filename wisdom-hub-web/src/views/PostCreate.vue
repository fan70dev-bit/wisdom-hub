<template>
  <div class="post-create-wrap">
    <el-card class="post-card" shadow="soft">
      <!-- 顶部：类型切换 -->
      <div class="section">
        <div class="section-title">发布类型</div>
        <el-radio-group v-model="form.type" @change="onTypeChange">
          <el-radio :label="0">长文专栏</el-radio>
          <el-radio :label="1">碎碎念</el-radio>
        </el-radio-group>
      </div>

      <!-- 长文模式：Type = 0 -->
      <template v-if="form.type === 0">
        <!-- 标题（必填，沉浸式大字号） -->
        <div class="section">
          <div class="section-title">标题</div>
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入标题（必填）"
                size="large"
                class="title-input"
                :autosize="{ minRows: 1, maxRows: 2 }"
                type="textarea"
                resize="none"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 工具栏：插入图片 -->
        <div class="section toolbar">
          <div class="section-title">编辑工具</div>
          <el-upload
            class="upload-inline"
            action="/api/file/upload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :on-success="onImageUploadSuccess"
            :on-error="onImageUploadError"
            accept="image/*"
          >
            <el-button type="primary" plain>
              <el-icon><Picture /></el-icon>
              插入图片
            </el-button>
          </el-upload>
          <span class="hint">上传成功后会自动追加到 Markdown 编辑区：<code>![插图](url)</code></span>
        </div>

        <!-- Markdown 双栏编辑器 -->
        <div class="section">
          <div class="section-title">内容（Markdown）</div>
          <el-row :gutter="16" class="editor-row">
            <el-col :span="12">
              <el-input
                v-model="contentLong"
                type="textarea"
                placeholder="在这里输入 Markdown 源码..."
                :autosize="{ minRows: 14, maxRows: 30 }"
                class="editor-textarea"
              />
            </el-col>
            <el-col :span="12">
              <div class="preview" v-html="markdownPreview"></div>
            </el-col>
          </el-row>
        </div>

        <!-- 视频 BV 号（可选） -->
        <div class="section">
          <div class="section-title">视频（可选，B站 BV 号）</div>
          <el-input
            v-model="form.videoUrl"
            placeholder="例如：BV1xx411c7mD"
            clearable
          />
        </div>
      </template>

      <!-- 碎碎念模式：Type = 1 -->
      <template v-else>
        <!-- 编辑器左右分栏：文字 + 九宫格图片 -->
        <div class="section">
          <div class="section-title">碎碎念内容</div>
          <el-row :gutter="16" class="editor-row">
            <el-col :span="12">
              <el-input
                v-model="contentText"
                type="textarea"
                placeholder="写点什么吧..."
                :autosize="{ minRows: 14, maxRows: 30 }"
                class="editor-textarea"
              />
            </el-col>
            <el-col :span="12">
              <div class="image-grid">
                <el-upload
                  action="/api/file/upload"
                  list-type="picture-card"
                  :file-list="imageFileList"
                  :before-upload="beforeImageUpload"
                  :on-success="onMomoImageUploadSuccess"
                  :on-remove="onMomoImageRemove"
                  :limit="4"
                  accept="image/*"
                >
                  <el-icon><Plus /></el-icon>
                  <template #tip>
                    <div class="upload-tip">最多上传 4 张图片</div>
                  </template>
                </el-upload>
              </div>
            </el-col>
          </el-row>
        </div>
      </template>

      <!-- 通用设置 -->
      <div class="section">
        <div class="section-title">可见性</div>
        <el-switch
          v-model="visibilitySwitch"
          inline-prompt
          active-text="公开"
          inactive-text="私密"
        />
        <span class="hint">公开 = 广场可见，私密 = 仅自己可见</span>
      </div>

      <!-- 发布按钮 -->
      <div class="actions">
        <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">
          <el-icon><Position /></el-icon>
          发布
        </el-button>
        <el-button size="large" @click="onReset">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Plus, Position } from '@element-plus/icons-vue'
import { marked } from 'marked'
import request from '@/utils/request'

// 路由
const router = useRouter()

// 表单状态
const formRef = ref(null)
const form = reactive({
  type: 0,           // 0 长文，1 碎碎念
  title: '',         // 长文标题
  videoUrl: '',      // 可选 BV 号
  status: 0          // 0 公开，1 私密
})

// 代理字段
const contentLong = ref('')               // 长文 Markdown
const contentText = ref('')               // 碎碎念文字
const contentImagesUrls = ref([])        // 碎碎念图片 URL 列表
const imageFileList = ref([])            // Element Plus 上传列表

// 提交状态
const submitting = ref(false)

// 可见性开关
const visibilitySwitch = ref(true) // true 公开(0)，false 私密(1)
const syncStatusFromSwitch = () => {
  form.status = visibilitySwitch.value ? 0 : 1
}
onMounted(() => {
  syncStatusFromSwitch()
})

// 类型切换时清理
const onTypeChange = () => {
  // 清理交叉字段，避免脏数据
  form.title = ''
  form.videoUrl = ''
  contentLong.value = ''
  contentText.value = ''
  contentImagesUrls.value = []
  imageFileList.value = []
}

// 表单校验规则（按类型动态）
const rules = computed(() => {
  if (form.type === 0) {
    return {
      title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 2, max: 100, message: '标题长度 2-100 个字符', trigger: 'blur' }
      ]
    }
  }
  return {}
})

// Markdown 实时预览
const markdownPreview = computed(() => {
  if (!contentLong.value) return '<div class="empty-preview">预览区</div>'
  try {
    return marked.parse(contentLong.value)
  } catch (e) {
    return '<div class="empty-preview">Markdown 解析失败</div>'
  }
})

// 图片上传校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

// 长文：插入图片到 Markdown
const onImageUploadSuccess = (response) => {
  if (response && response.code === 200 && response.data && response.data.url) {
    const url = response.data.url
    const markdownImg = `\n![插图](${url})\n`
    contentLong.value = contentLong.value + markdownImg
    ElMessage.success('图片已插入到编辑区')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 长文：上传失败
const onImageUploadError = () => {
  ElMessage.error('图片上传失败，请稍后重试')
}

// 碎碎念：九宫格图片上传成功
const onMomoImageUploadSuccess = (response, file, fileList) => {
  imageFileList.value = fileList
  // 收集 URL 列表
  contentImagesUrls.value = fileList
    .map(item => item.response?.data?.url || item.url)
    .filter(Boolean)
  ElMessage.success('图片上传成功')
}

// 碎碎念：移除图片
const onMomoImageRemove = (file, fileList) => {
  imageFileList.value = fileList
  contentImagesUrls.value = fileList
    .map(item => item.response?.data?.url || item.url)
    .filter(Boolean)
}

// 重置
const onReset = () => {
  formRef.value?.resetFields?.()
  form.type = 0
  form.title = ''
  form.videoUrl = ''
  form.status = 0
  visibilitySwitch.value = true
  contentLong.value = ''
  contentText.value = ''
  contentImagesUrls.value = []
  imageFileList.value = []
  ElMessage.info('已重置')
}

// 发布提交
const onSubmit = async () => {
  // 同步状态
  syncStatusFromSwitch()

  // 校验
  if (form.type === 0) {
    try {
      await formRef.value?.validate()
    } catch {
      return
    }
    if (!contentLong.value.trim()) {
      ElMessage.warning('请输入内容')
      return
    }
  } else {
    // 碎碎念：内容或图片至少要有一个
    const hasText = contentText.value.trim().length > 0
    const hasImages = contentImagesUrls.value.length > 0
    if (!hasText && !hasImages) {
      ElMessage.warning('请填写文字或上传至少一张图片')
      return
    }
  }

  // 组装最终 JSON
  let finalContent = ''
  if (form.type === 0) {
    // 长文
    finalContent = contentLong.value
  } else {
    // 碎碎念：文字 + 两个换行符 + 图片 Markdown
    const textPart = contentText.value.trim()
    const imgParts = contentImagesUrls.value.map(url => `![配图](${url})`).join('\n')
    if (textPart && imgParts) {
      finalContent = textPart + '\n\n' + imgParts
    } else if (textPart) {
      finalContent = textPart
    } else {
      finalContent = imgParts
    }
  }

  const payload = {
    type: form.type,
    title: form.type === 0 ? form.title.trim() : null,
    content: finalContent,
    videoUrl: form.type === 0 ? (form.videoUrl.trim() || null) : null,
    isPrivate: form.status === 1
  }

  // 提交
  submitting.value = true
  try {
    const res = await request.post('/post/create', payload, {
      headers: {
        'Content-Type': 'application/json',
        // 如果你用 JWT，请在这里自动加 Authorization 头（推荐在 axios 拦截器里统一处理）
        // 'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      withCredentials: true
    })

    // 兼容后端 Result 格式：{ success: true, data: 1 }
    if (res.data && (res.data.success === true || res.data.code === 200)) {
      ElMessage.success('发布成功')
      // 跳转到“我的花园”
      router.push('/garden')
    } else {
      ElMessage.error(res.data?.message || '发布失败')
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 奶油绿治愈系主题色 */
:root {
  --cream-green: #8cb06b;
  --deep-green: #4a5d23;
  --cream-bg: #f9fbf6;
  --soft-shadow: 0 6px 20px rgba(74, 93, 35, 0.12);
  --card-radius: 12px;
}

.post-create-wrap {
  /* 定义变量 */
  --cream-green: #8cb06b;
  --deep-green: #4a5d23;
  --cream-bg: #f9fbf6;
  --soft-shadow: 0 6px 20px rgba(74, 93, 35, 0.12);
  --card-radius: 12px;

  /* 使用变量 */
  background: var(--cream-bg);
  min-height: calc(100vh - 80px);
  padding: 24px;
  display: flex;
  justify-content: center;
}

.post-card {
  width: 100%;
  max-width: 1200px;
  border: none;
  border-radius: var(--card-radius);
  box-shadow: var(--soft-shadow);
  padding: 16px 20px;
}

.section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--deep-green);
  margin-bottom: 10px;
}

.hint {
  margin-left: 12px;
  color: #6b7a5a;
  font-size: 12px;
}

/* 标题输入：沉浸式大字号 */
.title-input :deep(.el-textarea__inner) {
  font-size: 28px;
  line-height: 1.35;
  border: none;
  box-shadow: none;
  padding: 10px 0;
  color: var(--deep-green);
  background: transparent;
  transition: background 0.2s ease;
}
.title-input :deep(.el-textarea__inner):focus {
  background: rgba(140, 176, 107, 0.08);
  border-radius: 8px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.upload-inline {
  display: inline-block;
}

/* 编辑器左右分栏 */
.editor-row {
  min-height: 360px;
}

/* 文本编辑区 */
.editor-textarea :deep(.el-textarea__inner) {
  height: 100%;
  font-size: 14px;
  line-height: 1.7;
  color: #2f3e2f;
  border: 1px solid #dbe7cf;
  border-radius: 10px;
  padding: 12px;
  background: #fbfdf8;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.editor-textarea :deep(.el-textarea__inner):focus {
  border-color: var(--cream-green);
  box-shadow: 0 0 0 3px rgba(140, 176, 107, 0.25);
}

/* 预览区 */
.preview {
  height: 100%;
  overflow: auto;
  padding: 12px 14px;
  border: 1px solid #dbe7cf;
  border-radius: 10px;
  background: #fbfdf8;
  color: #2f3e2f;
  line-height: 1.8;
}

/* 预览区滚动条美观 */
.preview::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
.preview::-webkit-scrollbar-thumb {
  background: #cfe1b9;
  border-radius: 6px;
}
.preview::-webkit-scrollbar-track {
  background: #f2f7ea;
  border-radius: 6px;
}

.empty-preview {
  color: #8aa07a;
  text-align: center;
  padding: 60px 0;
}

/* 碎碎念九宫格图片上传 */
.image-grid :deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border: 1px dashed #cfe1b9;
  background: #fbfdf8;
  border-radius: 10px;
  transition: border-color 0.2s, background 0.2s;
}
.image-grid :deep(.el-upload--picture-card:hover) {
  border-color: var(--cream-green);
  background: #f5faee;
}
.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7a5a;
}

/* 操作按钮区 */
.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 10px;
}

/* Element Plus 按钮主题微调 */
:deep(.el-button--primary) {
  background-color: var(--cream-green);
  border-color: var(--cream-green);
}
:deep(.el-button--primary:hover) {
  background-color: #7ba75f;
  border-color: #7ba75f;
}
:deep(.el-button--primary.is-plain) {
  color: var(--cream-green);
  background: #f2f7ea;
  border-color: #dbe7cf;
}
:deep(.el-button--primary.is-plain:hover) {
  color: #fff;
  background: var(--cream-green);
  border-color: var(--cream-green);
}
</style>