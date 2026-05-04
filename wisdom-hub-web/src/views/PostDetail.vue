<template>
  <div class="post-detail-container">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span class="detail-title"> 动态详情 </span>
      </template>
    </el-page-header>

    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="10" animated />
    </div>

    <el-card v-else-if="post" class="detail-card">
      <h1 class="post-title">{{ post.title || '无标题' }}</h1>
      
      <div class="post-meta">
        <el-tag size="small" type="success" effect="light" class="author-tag">
          作者 ID: {{ post.authorId || '未知' }}
        </el-tag>
        <span class="post-time">{{ formatTime(post.createTime) }}</span>
      </div>
      
      <el-divider border-style="dashed" />
      
      <div class="post-body">
        <div class="content-text" v-html="formatContent(post.content)"></div>
      </div>

      <!-- ✨ 新增：底部互动动作区 -->
      <div class="post-actions">
        <!-- 点赞按钮 -->
        <el-button 
          :class="['action-btn', isLiked ? 'is-liked' : '']" 
          round 
          @click="toggleLike"
        >
          <el-icon><CaretTop /></el-icon>
          {{ isLiked ? '已赞' : '点赞' }}
          <span class="count-num" v-if="post.likeCount">{{ post.likeCount + (isLiked ? 1 : 0) }}</span>
        </el-button>

        <!-- 收藏按钮 -->
        <el-button 
          :class="['action-btn', isFavorited ? 'is-favorited' : '']" 
          round 
          @click="toggleFavorite"
        >
          <el-icon><Star /></el-icon>
          {{ isFavorited ? '已收藏' : '收藏' }}
        </el-button>

        <!-- 不喜欢按钮 -->
        <el-button 
          :class="['action-btn', isDisliked ? 'is-disliked' : '']" 
          round 
          @click="toggleDislike"
        >
          <el-icon><CaretBottom /></el-icon>
          不喜欢
        </el-button>
      </div>

    </el-card>

    <div v-else class="empty-state">
      <el-empty description="哎呀，内容走丢了，或者解析失败了" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
// ✨ 引入所需的图标
import { Star, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const post = ref(null)
const loading = ref(false)

// 互动状态
const isLiked = ref(false)
const isFavorited = ref(false)
const isDisliked = ref(false)

const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/post/${route.params.id}`)
    
    // 无敌脱壳法
    let realData = null
    if (res.data && res.data.code === 200) {
      realData = res.data.data
    } else if (res.code === 200) {
      realData = res.data
    } else if (res.post) {
      realData = res
    } else {
      realData = res // 兜底
    }

    if (realData) {
      post.value = realData.post || realData 
      isLiked.value = realData.isLiked || false
      isFavorited.value = realData.isFavorited || false
      // 如果后端有不喜欢字段，也可在此读取
    }
  } catch (err) {
    console.error('获取详情失败', err)
  } finally {
    loading.value = false
  }
}

// ======== ✨ 真实互动逻辑 ========

// ======== ✨ 真实互动逻辑 (防套娃升级版) ========

// 点赞
const toggleLike = async () => {
  try {
    const res = await request.post(`/post/${route.params.id}/like`)
    
    // 🚀 兼容 Axios 未脱壳的情况
    if (res.code === 200 || (res.data && res.data.code === 200)) {
      isLiked.value = !isLiked.value
      if (isLiked.value) {
        isDisliked.value = false // 互斥
        if(post.value.likeCount !== undefined) post.value.likeCount++ 
        ElMessage.success('点赞成功！作者会很开心的~')
      } else {
        if(post.value.likeCount !== undefined) post.value.likeCount--
        ElMessage.info('已取消点赞')
      }
    }
  } catch (err) {
    ElMessage.error('操作失败，请检查网络')
  }
}

// 收藏
const toggleFavorite = async () => {
  try {
    const res = await request.post(`/post/${route.params.id}/favorite`)
    
    // 🚀 兼容 Axios 未脱壳的情况
    if (res.code === 200 || (res.data && res.data.code === 200)) {
      isFavorited.value = !isFavorited.value
      if (isFavorited.value) {
        ElMessage.success('已加入你的个人收藏夹！')
      } else {
        ElMessage.info('已取消收藏')
      }
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

// 不喜欢 (前端暂存逻辑)
const toggleDislike = () => {
  isDisliked.value = !isDisliked.value
  if (isDisliked.value) {
    // 如果点过赞，现在点不喜欢，需要向后端发请求取消点赞
    if (isLiked.value) {
       toggleLike() // 触发取消点赞的真实请求
    }
    ElMessage.warning('将减少此类内容的推荐')
  } else {
    ElMessage.info('已取消不喜欢')
  }
}
// ======== 工具方法 ========

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 简单的换行和图片处理
const formatContent = (text) => {
  if (!text) return '暂无内容'
  let html = text.replace(/\n/g, '<br/>')
  html = html.replace(/!\[.*?\]\((.*?)\)/g, '<br/><img src="$1" style="max-width: 100%; border-radius: 8px; margin: 10px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.05);" /><br/>')
  return html
}

onMounted(() => {
  fetchPostDetail()
})
</script>

<style scoped>
.post-detail-container {
  padding: 30px;
  background-color: #F9FBF6;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center; /* 居中整个页面容器 */
}

/* 限制详情页最大宽度，阅读体验更好 */
.detail-card, .loading-box, .empty-state, .el-page-header {
  width: 100%;
  max-width: 800px; 
}

.detail-card {
  margin-top: 25px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 30px rgba(140, 176, 107, 0.08);
  padding: 10px;
}

.post-title {
  font-size: 26px;
  color: #2F3E2F;
  margin-bottom: 20px;
  font-weight: bold;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #99A97E;
  font-size: 13px;
}

.author-tag {
  color: #8CB06B;
  border-color: #DBE7CF;
  background-color: #F2F7EA;
}

.post-body {
  line-height: 1.8;
  color: #4A5D23;
  font-size: 16px;
  padding: 10px 0 30px 0;
}

/* ======== ✨ 互动操作区样式 ======== */
.post-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding-top: 30px;
  border-top: 1px dashed #EDF2E9;
}

.action-btn {
  padding: 10px 24px;
  font-size: 15px;
  border: 1px solid #EDF2E9;
  background-color: #FAFCF7;
  color: #66784D;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background-color: #F2F7EA;
  color: #8CB06B;
  border-color: #8CB06B;
  transform: translateY(-2px);
}

.action-btn .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.count-num {
  margin-left: 6px;
  font-size: 14px;
}

/* 激活状态的主题色定制 */
.is-liked {
  background-color: #8CB06B !important;
  border-color: #8CB06B !important;
  color: #FFF !important;
}

.is-favorited {
  background-color: #F39C12 !important; /* 温暖的橘黄色 */
  border-color: #F39C12 !important;
  color: #FFF !important;
}

.is-disliked {
  background-color: #E0E0E0 !important;
  border-color: #E0E0E0 !important;
  color: #666 !important;
}

.loading-box, .empty-state {
  margin-top: 40px;
  background: white;
  padding: 40px;
  border-radius: 16px;
}
</style>