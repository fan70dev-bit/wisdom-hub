<template>
  <div class="search-container">
    <!-- 顶部搜索区：未搜索时居中，搜索后自动上移 -->
    <div class="search-header" :class="{ 'is-active': hasSearched }">
      <h1 class="page-title" v-if="!hasSearched">🔍 寻找答案</h1>
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="输入关键字搜索动态、长文或碎碎念..."
          class="massive-search-input"
          clearable
          @clear="onClear"
          @keyup.enter="onSearch"
        >
          <template #append>
            <el-button type="primary" @click="onSearch" :loading="loading && pageNum === 1">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 搜索结果区 -->
    <div class="search-content" v-if="hasSearched">
      <div class="result-stats" v-if="!loading || pageNum > 1">
        找到与 <span class="highlight">"{{ currentKeyword }}"</span> 相关的结果 <strong>{{ total }}</strong> 个
      </div>

      <!-- 骨架屏加载 -->
      <div v-if="loading && pageNum === 1" class="list-wrapper">
        <el-card class="post-card" v-for="i in 3" :key="i">
          <el-skeleton animated :rows="3" />
        </el-card>
      </div>

      <!-- 真实结果列表 -->
      <div v-else-if="postList.length > 0" class="list-wrapper">
        <div 
          v-for="post in postList" 
          :key="post.id" 
          class="post-card"
          @click="goToDetail(post.id)"
        >
          <div class="post-header">
            <div class="author-info">
              <el-avatar :size="36" :src="post.authorAvatar" class="author-avatar">
                {{ post.authorAvatar ? '' : (post.authorName || 'U')[0] }}
              </el-avatar>
              <div class="author-text">
                <span class="author-name">{{ post.authorName || '热心网友' }}</span>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <el-tag :type="post.type === 0 ? 'success' : 'warning'" size="small" effect="light">
              {{ post.type === 0 ? '长文' : '碎碎念' }}
            </el-tag>
          </div>

          <!-- 标题（支持高亮） -->
          <h3 class="post-title" v-if="post.type === 0" v-html="highlightText(post.title)"></h3>
          
          <!-- 摘要（支持高亮） -->
          <p class="post-excerpt" v-html="getExcerpt(post.content)"></p>

          <div class="post-footer">
            <span class="stat-item"><el-icon><Star /></el-icon> {{ post.favoriteCount || 0 }} 收藏</span>
            <span class="stat-item"><el-icon><CaretTop /></el-icon> {{ post.likeCount || 0 }} 赞</span>
          </div>
        </div>

        <!-- 加载更多 -->
        <div class="load-more-wrap">
          <el-button 
            v-if="postList.length < total" 
            :loading="loadingMore" 
            @click="loadNextPage"
            round
            class="load-more-btn"
          >
            加载更多...
          </el-button>
          <div v-else class="bottom-tip">- 已经到底啦 -</div>
        </div>
      </div>

      <!-- 查无结果 -->
      <div v-else class="empty-state">
        <el-empty description="没有找到相关内容，换个词试试看吧" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Star, CaretTop } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 搜索状态
const keyword = ref('')
const currentKeyword = ref('') // 锁定的搜索词
const hasSearched = ref(false) // 是否已经执行过搜索

// 列表与分页
const postList = ref([])
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)

// 触发搜索
const onSearch = () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键字')
    return
  }
  currentKeyword.value = keyword.value.trim()
  hasSearched.value = true
  pageNum.value = 1
  fetchResults(false)
}

// 清空输入框还原初始状态
const onClear = () => {
  hasSearched.value = false
  postList.value = []
  total.value = 0
}

// 核心请求：获取搜索结果
const fetchResults = async (isAppend = false) => {
  if (isAppend) loadingMore.value = true
  else loading.value = true

  try {
    // 💡 这里调用了 Claude 刚刚给你写的搜索接口
    const res = await request.get('/post/search', {
      params: { keyword: currentKeyword.value, pageNum: pageNum.value, pageSize: pageSize.value }
    })
    
    // 终极拆盒法
    let pageData = null
    if (res.data && res.data.code === 200 && res.data.data) pageData = res.data.data
    else if (res.code === 200 && res.data) pageData = res.data
    else if (res.list) pageData = res

    if (pageData) {
      const newList = pageData.list || []
      total.value = pageData.total || 0
      
      if (isAppend) postList.value.push(...newList)
      else postList.value = newList
    }
  } catch (err) {
    ElMessage.error('搜索失败，请检查网络或后端接口')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载下一页
const loadNextPage = () => {
  pageNum.value++
  fetchResults(true)
}

const goToDetail = (id) => router.push(`/post/${id}`)
const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''

// 🚀 高亮匹配词的方法
const highlightText = (text) => {
  if (!text) return ''
  if (!currentKeyword.value) return text
  // 忽略大小写全局匹配
  const reg = new RegExp(`(${currentKeyword.value})`, 'gi')
  // 替换为带有高亮样式的 span
  return text.replace(reg, '<span style="color: #F39C12; font-weight: bold; padding: 0 2px;">$1</span>')
}

// 提取摘要并高亮
const getExcerpt = (content) => {
  if (!content) return ''
  let text = content.replace(/!\[.*?\]\(.*?\)/g, '[图片]').replace(/[#*`~>-]/g, '')
  text = text.length > 100 ? text.slice(0, 100) + '...' : text
  return highlightText(text) // 加上高亮
}
</script>

<style scoped>
.search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  min-height: calc(100vh - 80px);
}

/* 顶部搜索区动画过渡 */
.search-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 15vh; /* 初始状态在页面中上部 */
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 搜索后自动滑到顶部 */
.search-header.is-active {
  margin-top: 0;
  margin-bottom: 30px;
}

.page-title {
  font-size: 32px;
  color: #2F3E2F;
  margin-bottom: 30px;
  font-weight: bold;
}

.search-box {
  width: 100%;
  max-width: 600px;
  box-shadow: 0 8px 25px rgba(140, 176, 107, 0.15);
  border-radius: 8px;
}

/* 定制搜索框外观 */
.massive-search-input :deep(.el-input__wrapper) {
  padding: 8px 16px;
  font-size: 16px;
}

.massive-search-input :deep(.el-input-group__append) {
  background-color: #8CB06B;
  color: white;
  border: 1px solid #8CB06B;
  font-size: 16px;
  padding: 0 24px;
}

.massive-search-input :deep(.el-input-group__append:hover) {
  background-color: #7BA75F;
}

.result-stats {
  margin-bottom: 20px;
  font-size: 14px;
  color: #66784D;
  background: #F2F7EA;
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 4px solid #8CB06B;
}

.highlight {
  color: #F39C12;
  font-weight: bold;
}

/* 复用卡片样式 */
.list-wrapper { display: flex; flex-direction: column; gap: 20px; }
.post-card { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #EDF2E9; transition: all 0.3s ease; cursor: pointer; }
.post-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(140, 176, 107, 0.12); border-color: #8CB06B; }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.author-info { display: flex; align-items: center; gap: 12px; }
.author-avatar { background-color: #8CB06B; color: white; font-weight: bold; }
.author-text { display: flex; flex-direction: column; }
.author-name { font-size: 15px; color: #2F3E2F; font-weight: 600; }
.post-time { font-size: 12px; color: #99A97E; margin-top: 2px; }
.post-title { font-size: 18px; color: #333; margin: 0 0 10px; font-weight: bold; }
.post-excerpt { color: #66784D; line-height: 1.6; font-size: 14px; margin-bottom: 0; }
.post-footer { margin-top: 16px; display: flex; gap: 16px; border-top: 1px dashed #F2F7EA; padding-top: 16px; }
.stat-item { color: #8CB06B; font-size: 13px; display: flex; align-items: center; gap: 4px; }

.load-more-wrap { text-align: center; margin-top: 25px; padding-bottom: 30px; }
.load-more-btn { color: #8CB06B; border-color: #8CB06B; width: 150px; }
.load-more-btn:hover { background-color: #F2F7EA; }
.bottom-tip { color: #99A97E; font-size: 13px; }
</style>