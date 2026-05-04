<template>
  <div class="bookmarks-container">
    <div class="page-header">
      <h2 class="title">个人收藏 (My Bookmarks)</h2>
      <div v-if="favoriteList.length > 0" style="font-size: 10px; color: #ccc; margin-bottom: 10px;">
        已同步数据 ID: {{ favoriteList.map(i => i.id).join(',') }}
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="favoriteList && favoriteList.length > 0" class="bookmarks-grid">
      <el-card 
        v-for="(post, index) in favoriteList" 
        :key="post.id || index" 
        class="post-card" 
        shadow="hover"
      >
        <div class="post-content" @click="goToDetail(post.id)">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-excerpt">{{ filterMarkdown(post.content) || '暂无内容详情' }}</p>
        </div>

        <div class="post-footer">
          <div class="post-stats">
            <span class="stat-item"><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
            <span class="stat-item"><el-icon><CollectionTag /></el-icon> {{ post.favoriteCount || 0 }}</span>
          </div>
          <div class="actions">
            <el-button type="danger" icon="Delete" circle plain size="small" @click.stop="handleUnfavorite(post.id)" />
          </div>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-state">
      <el-empty :description="loading ? '加载中...' : '还没找到收藏的痕迹呢'">
        <el-button type="primary" @click="fetchFavorites">点此刷新数据</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, CollectionTag, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const favoriteList = ref([]) // 确保 ref([]) 已经初始化

// 替换 Bookmarks.vue 里的这个函数
const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/favorites')
    console.log('1. Axios 拿到的原始信封:', res)
    
    // 🚀 【核心修复：无敌脱壳法】
    let realData = []
    
    if (res.data && res.data.code === 200) {
      // 情况 A：原始 Axios 响应（大概率是你的情况！）
      realData = res.data.data
    } else if (res.code === 200) {
      // 情况 B：被 request.js 拦截器剥了一层
      realData = res.data
    } else if (Array.isArray(res)) {
      // 情况 C：被 request.js 拦截器剥到了极致，直接返回了数组
      realData = res
    }

    // 赋值给响应式变量
    favoriteList.value = realData || []
    
    console.log('2. 成功扒出来的数组:', favoriteList.value)
    
  } catch (err) {
    console.error('获取收藏失败:', err)
  } finally {
    loading.value = false
  }
}

const handleUnfavorite = (postId) => {
  ElMessageBox.confirm('移出收藏？', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/post/${postId}/favorite`)
    favoriteList.value = favoriteList.value.filter(item => item.id !== postId)
    ElMessage.success('已移除')
  }).catch(() => {})
}

const goToDetail = (id) => router.push(`/post/${id}`)

const filterMarkdown = (text) => {
  if (!text) return ''
  return text.replace(/!\[.*\]\(.*\)/g, '[图片]').substring(0, 80)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
/* 增加布局强制可见性 */
.bookmarks-container {
  padding: 40px;
  background-color: #F9FBF6;
  min-height: 100vh;
}
.bookmarks-grid {
  display: flex; /* 改用 flex 试试，防止 grid 在某些容器下失效 */
  flex-wrap: wrap;
  gap: 20px;
}
.post-card {
  width: 300px;
  border-radius: 15px;
}
.post-title { font-weight: bold; margin-bottom: 10px; color: #333; }
.post-excerpt { color: #666; font-size: 14px; }
.post-footer { margin-top: 15px; border-top: 1px solid #eee; padding-top: 10px; display: flex; justify-content: space-between; }
</style>