<template>
  <div class="explore-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">🌍 探索广场</h1>
      <p class="page-subtitle">在这里，发现同样正在努力向前的同路人</p>
    </div>

    <!-- 极简 Tabs 分类切换 -->
    <div class="explore-content">
      <el-tabs v-model="activeTab" class="custom-tabs">
        
        <!-- ================= Tab 1: 全部动态 ================= -->
        <el-tab-pane label="全部动态" name="all">
          <div v-if="loading && pageNum === 1" class="list-wrapper">
            <el-card class="post-card" v-for="i in 3" :key="i"><el-skeleton animated :rows="3" /></el-card>
          </div>
          <div v-else-if="postList.length > 0" class="list-wrapper">
            <div v-for="post in postList" :key="post.id" class="post-card" @click="goToDetail(post.id)">
              <div class="post-header">
                <div class="author-info">
                  <el-avatar :size="36" :src="post.authorAvatar" class="author-avatar">{{ post.authorAvatar ? '' : (post.authorName || 'U')[0] }}</el-avatar>
                  <div class="author-text">
                    <span class="author-name">{{ post.authorName || '热心网友' }}</span>
                    <span class="post-time">{{ formatTime(post.createTime) }}</span>
                  </div>
                </div>
                <el-tag :type="post.type === 0 ? 'success' : 'warning'" size="small" effect="light">{{ post.type === 0 ? '长文' : '碎碎念' }}</el-tag>
              </div>
              <h3 class="post-title" v-if="post.type === 0">{{ post.title }}</h3>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="stat-item"><el-icon><Star /></el-icon> {{ post.favoriteCount || 0 }} 收藏</span>
                <span class="stat-item"><el-icon><CaretTop /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="广场上空空如也" />
        </el-tab-pane>

        <!-- ================= Tab 2: 仅长文 ================= -->
        <el-tab-pane label="长文" name="articles">
          <div v-if="loading && pageNum === 1" class="list-wrapper">
            <el-card class="post-card" v-for="i in 3" :key="i"><el-skeleton animated :rows="3" /></el-card>
          </div>
          <div v-else-if="articlesList.length > 0" class="list-wrapper">
            <div v-for="post in articlesList" :key="'art-'+post.id" class="post-card" @click="goToDetail(post.id)">
              <div class="post-header">
                <div class="author-info">
                  <el-avatar :size="36" :src="post.authorAvatar" class="author-avatar">{{ post.authorAvatar ? '' : (post.authorName || 'U')[0] }}</el-avatar>
                  <div class="author-text">
                    <span class="author-name">{{ post.authorName || '热心网友' }}</span>
                    <span class="post-time">{{ formatTime(post.createTime) }}</span>
                  </div>
                </div>
                <el-tag type="success" size="small" effect="light">长文</el-tag>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="stat-item"><el-icon><Star /></el-icon> {{ post.favoriteCount || 0 }} 收藏</span>
                <span class="stat-item"><el-icon><CaretTop /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂时还没有人发布长文哦" />
        </el-tab-pane>

        <!-- ================= Tab 3: 仅碎碎念 ================= -->
        <el-tab-pane label="碎碎念" name="thoughts">
          <div v-if="loading && pageNum === 1" class="list-wrapper">
            <el-card class="post-card" v-for="i in 3" :key="i"><el-skeleton animated :rows="3" /></el-card>
          </div>
          <div v-else-if="thoughtsList.length > 0" class="list-wrapper">
            <div v-for="post in thoughtsList" :key="'tht-'+post.id" class="post-card" @click="goToDetail(post.id)">
              <div class="post-header">
                <div class="author-info">
                  <el-avatar :size="36" :src="post.authorAvatar" class="author-avatar">{{ post.authorAvatar ? '' : (post.authorName || 'U')[0] }}</el-avatar>
                  <div class="author-text">
                    <span class="author-name">{{ post.authorName || '热心网友' }}</span>
                    <span class="post-time">{{ formatTime(post.createTime) }}</span>
                  </div>
                </div>
                <el-tag type="warning" size="small" effect="light">碎碎念</el-tag>
              </div>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="stat-item"><el-icon><Star /></el-icon> {{ post.favoriteCount || 0 }} 收藏</span>
                <span class="stat-item"><el-icon><CaretTop /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂时还没有人发布碎碎念哦" />
        </el-tab-pane>

      </el-tabs>

      <!-- 统一的加载更多按钮 -->
      <div class="load-more-wrap" v-if="postList.length > 0">
        <el-button v-if="postList.length < total" :loading="loadingMore" @click="loadNextPage" round class="load-more-btn">
          加载更多...
        </el-button>
        <div v-else class="bottom-tip">- 已经到底啦 -</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star, CaretTop } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('all')

// 列表数据
const postList = ref([])
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)

// 🌟 核心：极简智能计算属性
const articlesList = computed(() => postList.value.filter(post => post.type === 0))
const thoughtsList = computed(() => postList.value.filter(post => post.type === 1))

// 仅保留获取广场数据的极简逻辑
const fetchPosts = async (isAppend = false) => {
  if (isAppend) loadingMore.value = true
  else loading.value = true

  try {
    const res = await request.get('/post/explore', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
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
    ElMessage.error('获取广场数据失败，请检查网络')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadNextPage = () => {
  pageNum.value++
  fetchPosts(true)
}

const goToDetail = (id) => router.push(`/post/${id}`)
const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
const getExcerpt = (content) => {
  if (!content) return ''
  let text = content.replace(/!\[.*?\]\(.*?\)/g, '[图片]').replace(/[#*`~>-]/g, '')
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

onMounted(() => fetchPosts())
</script>

<style scoped>
.explore-container { max-width: 800px; margin: 0 auto; padding: 30px; }
.page-header { margin-bottom: 25px; text-align: center; }
.page-title { font-size: 28px; color: #2F3E2F; margin-bottom: 8px; font-weight: bold; }
.page-subtitle { color: #8CB06B; font-size: 15px; }

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

/* Tabs 样式定制 */
:deep(.el-tabs__item.is-active) { color: #8CB06B !important; }
:deep(.el-tabs__active-bar) { background-color: #8CB06B !important; }
</style>