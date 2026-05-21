<template>
  <div class="wisdom-master-container">
    <div class="editor-section">
      <header class="editor-header">
        <div class="meta-inputs">
          <input v-model="form.title" class="title-input" placeholder="文章标题 (留空则默认为碎碎念) / Title" />
          <div class="bilibili-wrapper">
            <el-icon><VideoCamera /></el-icon>
            <input v-model="form.videoUrl" class="bilibili-input" placeholder="贴入 B站链接 或 BV号 (可选)" />
          </div>
        </div>
        <div class="header-actions">
          <el-checkbox v-model="form.isPrivate" class="private-check">花园私密</el-checkbox>
          <el-button 
            :type="editingId ? 'warning' : 'success'" 
            :loading="publishLoading" 
            @click="handlePublishOrUpdate"
          >
            {{ editingId ? '保存修改 (Update)' : '发布动态 (Publish)' }}
          </el-button>
          <el-button v-if="editingId" @click="resetForm">取消编辑</el-button>
        </div>
      </header>

      <div class="toolbar">
        <span class="toolbar-label">
          {{ editingId ? '📝 编辑模式' : (form.title ? '✍️ 长文模式' : '💬 碎碎念') }}
        </span>
        <div class="toolbar-buttons">
          <el-upload
            class="inline-upload"
            action="/api/file/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :headers="{ Authorization: 'Bearer ' + getToken() }"
            name="file"
          >
            <el-button link :icon="Picture">上传图片/封面</el-button>
          </el-upload>

          <el-popover placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button link :icon="Cherry">表情</el-button>
            </template>
            <div class="emoji-picker">
              <span v-for="e in emojis" :key="e" @click="insertEmoji(e)">{{ e }}</span>
            </div>
          </el-popover>
        </div>
      </div>

      <div class="editor-main">
        <div class="edit-pane">
          <textarea 
            ref="editorRef" 
            v-model="form.content" 
            class="raw-textarea" 
            placeholder="支持 Markdown 语法..."
          ></textarea>
        </div>
        <div class="preview-pane markdown-body">
          <div v-html="previewHtml"></div>
        </div>
      </div>
    </div>

    <div class="management-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span class="header-title"><el-icon><Reading /></el-icon> 枢纽文章管理</span>
            <el-button type="primary" :loading="syncLoading" @click="loadLocalList" icon="Refresh">刷新数据</el-button>
          </div>
        </template>

        <el-table :data="localArticles" stripe style="width: 100%" max-height="400">
          <el-table-column label="封面" width="80">
            <template #default="scope">
              <el-image 
                v-if="scope.row.coverImage"
                style="width: 48px; height: 48px; border-radius: 4px"
                :src="scope.row.coverImage" 
                :preview-src-list="[scope.row.coverImage]"
                fit="cover"
                preview-teleported
              />
              <div v-else class="empty-cover-placeholder"><el-icon><Picture /></el-icon></div>
            </template>
          </el-table-column>

          <el-table-column label="内容摘要" min-width="250">
            <template #default="scope">
              <div class="table-title">{{ scope.row.title || '碎碎念动态' }}</div>
              <div class="table-content-text">{{ scope.row.content.substring(0, 40) }}...</div>
              <div class="tags-row">
                <el-tag v-if="scope.row.visibility === 1" size="small" type="info">私密</el-tag>
                <el-tag v-if="scope.row.videoUrl" size="small" color="#fb7299" style="color:white; border:none">B站视频</el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="发布时间" width="160">
            <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-popconfirm 
                title="确定要删除并清理云端图片吗？" 
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDelete(scope.row.id)"
              >
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Picture, Cherry, Refresh, Reading } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'github-markdown-css/github-markdown.css'
import 'highlight.js/styles/github.css'

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre><code class="hljs">' + hljs.highlight(str, { language: lang, ignoreIllegals: true }).value + '</code></pre>';
      } catch (__) {}
    }
    return '<pre><code class="hljs">' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

const editorRef = ref(null)
const publishLoading = ref(false)
const syncLoading = ref(false)
const localArticles = ref([])
const editingId = ref(null) 
const emojis = ['👍', '🙌', '🔥', '🚀', '💻', '✨', '✅', '👀', '🎉']

const form = reactive({ 
  title: '', 
  videoUrl: '', 
  content: '', 
  coverImage: '',
  isPrivate: false 
})

const getToken = () => localStorage.getItem('token')

const handleImageSuccess = (response) => {
  if (response.code === 200) {
    const imageUrl = response.data.url
    if (!form.coverImage) form.coverImage = imageUrl
    insertAtCursor(`\n![图片描述](${imageUrl})\n`)
    ElMessage.success('上传成功')
  }
}

const previewHtml = computed(() => {
  let html = md.render(form.content || '*预览区域*')
  const bvRegex = /(BV[a-zA-Z0-9]{10})/g
  return html.replace(bvRegex, (match) => `
    <div class="bili-card-box">
      <div class="bili-tag">📺 Bilibili</div>
      <div class="bili-detail"><strong>视频解析</strong><br/><small>${match}</small></div>
    </div>`)
})

const loadLocalList = async () => {
  syncLoading.value = true
  try {
    const res = await axios.get('/api/post/garden', {
      headers: { Authorization: `Bearer ${getToken()}` }
    })
    if (res.data.code === 200) localArticles.value = res.data.data
  } finally { syncLoading.value = false }
}

// 核心修改：支持修改和新增切换
const handlePublishOrUpdate = async () => {
  if (!form.content) return ElMessage.warning('内容不能为空')
  publishLoading.value = true
  
  try {
    // 根据是否有 editingId 决定调用哪个接口
    const isEdit = !!editingId.value
    const url = isEdit ? '/api/post/update' : '/api/post/create'
    const method = isEdit ? 'put' : 'post'
    
    const payload = {
      ...form,
      id: editingId.value, // 修改时必须带上 ID
      type: form.title ? 0 : 1,
      visibility: form.isPrivate ? 1 : 0
    }

    const res = await axios[method](url, payload, {
      headers: { Authorization: `Bearer ${getToken()}` }
    })

    if (res.data.code === 200) {
      ElMessage.success(isEdit ? '修改成功' : '发布成功')
      resetForm()
      loadLocalList()
    }
  } catch (err) {
    ElMessage.error('操作失败')
  } finally {
    publishLoading.value = false
  }
}

// 新增：删除逻辑
const handleDelete = async (id) => {
  try {
    const res = await axios.delete(`/api/post/${id}`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    })
    if (res.data.code === 200) {
      ElMessage.success('删除成功，云端资源已同步清理')
      loadLocalList()
    }
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

const handleEdit = (row) => {
  editingId.value = row.id 
  Object.assign(form, {
    title: row.title || '',
    content: row.content || '',
    videoUrl: row.videoUrl || '',
    coverImage: row.coverImage || '',
    isPrivate: row.visibility === 1
  })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, { title: '', videoUrl: '', content: '', coverImage: '', isPrivate: false })
}

const insertEmoji = (e) => insertAtCursor(e)
const insertAtCursor = (text) => {
  const el = editorRef.value
  const start = el.selectionStart
  const end = el.selectionEnd
  form.content = form.content.substring(0, start) + text + form.content.substring(end)
}

const formatDate = (d) => d ? new Date(d).toLocaleString() : '-'

onMounted(loadLocalList)
</script>

<style scoped>
/* 保持原有样式，仅微调 */
.wisdom-master-container { height: 100vh; overflow-y: auto; background: #f8f9fa; }
.editor-section { height: 80vh; display: flex; flex-direction: column; background: #fff; border-bottom: 2px solid #eee; }
.editor-header { padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.meta-inputs { flex: 1; }
.title-input { width: 100%; border: none; font-size: 24px; font-weight: bold; outline: none; margin-bottom: 5px; }
.bilibili-wrapper { display: flex; align-items: center; color: #fb7299; font-size: 14px; gap: 5px; }
.bilibili-input { border: none; outline: none; width: 300px; }

.toolbar { padding: 10px 30px; background: #fafafa; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.editor-main { flex: 1; display: flex; overflow: hidden; }
.edit-pane { flex: 1; border-right: 1px solid #eee; padding: 15px; }
.preview-pane { flex: 1; padding: 25px; overflow-y: auto; }
.raw-textarea { width: 100%; height: 100%; border: none; resize: none; outline: none; font-family: 'Fira Code', monospace; font-size: 15px; }

.management-section { padding: 30px; }
.header-title { font-weight: bold; display: flex; align-items: center; gap: 8px; }
.empty-cover-placeholder { width: 48px; height: 48px; background: #f0f0f0; display: flex; align-items: center; justify-content: center; color: #ccc; border-radius: 4px; }
.table-title { font-weight: bold; color: #333; }
.table-content-text { font-size: 13px; color: #666; margin: 4px 0; }
.tags-row { display: flex; gap: 8px; }

:deep(.markdown-body pre) { background-color: #f6f8fa !important; padding: 16px !important; border-radius: 8px !important; }

:deep(.bili-card-box) {
  background: #f1f2f3; border: 1px solid #e3e5e7; padding: 10px; border-radius: 6px; display: flex; gap: 15px; margin: 10px 0;
}
:deep(.bili-tag) { background: #fb7299; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; height: fit-content; }
</style>