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

      <!-- 底部互动动作区 -->
      <div class="post-actions">
        <el-button :class="['action-btn', isLiked ? 'is-liked' : '']" round @click="toggleLike">
          <el-icon><CaretTop /></el-icon>
          {{ isLiked ? '已赞' : '点赞' }}
          <span class="count-num" v-if="post.likeCount">{{ post.likeCount + (isLiked ? 1 : 0) }}</span>
        </el-button>
        <el-button :class="['action-btn', isFavorited ? 'is-favorited' : '']" round @click="toggleFavorite">
          <el-icon><Star /></el-icon>
          {{ isFavorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button :class="['action-btn', isDisliked ? 'is-disliked' : '']" round @click="toggleDislike">
          <el-icon><CaretBottom /></el-icon>
          不喜欢
        </el-button>
      </div>

      <el-divider />

      <!-- ======== ✨ 新增：评论区 ======== -->
      <div class="comments-section">
        <h3 class="comments-title">全部评论 ({{ totalCommentsCount }})</h3>

        <!-- 顶部发表主评论区 -->
        <div class="comment-input-box" v-if="!activeReplyId">
          <el-input 
            type="textarea" 
            v-model="commentContent" 
            placeholder="写下你的评论，友善交流哦..." 
            :rows="3" 
            maxlength="500"
            show-word-limit
          />
          <div class="input-actions">
            <el-button type="primary" :loading="submitting" @click="submitComment" round>发表评论</el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <!-- 顶层评论头像 -->
            <el-avatar :size="40" :src="comment.authorAvatar" class="c-avatar">
              {{ comment.authorAvatar ? '' : (comment.authorName || 'U')[0] }}
            </el-avatar>

            <div class="c-main">
              <!-- 顶层评论信息 -->
              <div class="c-header">
                <span class="c-name">{{ comment.authorName }}</span>
                <span class="c-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="c-content">{{ comment.content }}</div>
              <div class="c-actions">
                <span class="action-text" @click="openReply(comment, false, comment.id)">回复</span>
                <span class="action-text delete" v-if="comment.userId === currentUserId" @click="handleDelete(comment.id)">删除</span>
              </div>

              <!-- 顶层评论的直接回复框 -->
              <div class="inline-reply" v-if="activeReplyId === comment.id">
                <el-input type="textarea" v-model="commentContent" :placeholder="`回复 @${replyParams.replyToUserName} :`" :rows="2" />
                <div class="input-actions">
                  <el-button size="small" @click="cancelReply" round>取消</el-button>
                  <el-button type="primary" size="small" :loading="submitting" @click="submitComment" round>回复</el-button>
                </div>
              </div>

              <!-- 楼中楼：子孙评论 (扁平化展示无限极) -->
              <div class="sub-comments" v-if="comment.children && comment.children.length > 0">
                <div class="sub-comment-item" v-for="sub in flattenReplies(comment.children)" :key="sub.id">
                  <div class="sub-c-header">
                    <span class="c-name">{{ sub.authorName }}</span>
                    <!-- 如果不是直接回复顶层楼主，则显示 回复@某人 -->
                    <span v-if="sub.replyToUserId && sub.replyToUserId !== comment.userId" class="reply-to">
                       回复 <span class="c-name">@{{ sub.replyToUserName }}</span>
                    </span>
                    <span class="c-content-inline"> : {{ sub.content }}</span>
                  </div>
                  <div class="c-actions sub-actions">
                    <span class="c-time">{{ formatTime(sub.createTime) }}</span>
                    <span class="action-text" @click="openReply(sub, true, comment.id)">回复</span>
                    <span class="action-text delete" v-if="sub.userId === currentUserId" @click="handleDelete(sub.id)">删除</span>
                  </div>

                  <!-- 楼中楼的回复框 -->
                  <div class="inline-reply" v-if="activeReplyId === sub.id">
                    <el-input type="textarea" v-model="commentContent" :placeholder="`回复 @${replyParams.replyToUserName} :`" :rows="2" />
                    <div class="input-actions">
                      <el-button size="small" @click="cancelReply" round>取消</el-button>
                      <el-button type="primary" size="small" :loading="submitting" @click="submitComment" round>回复</el-button>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
        
        <el-empty v-else description="还没有人评论，快来抢沙发吧！" :image-size="100" />
      </div>

    </el-card>

    <div v-else class="empty-state">
      <el-empty description="哎呀，内容走丢了，或者解析失败了" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const post = ref(null)
const loading = ref(false)

// 当前登录用户ID (用于判断是否显示删除按钮)
const currentUserId = ref(localStorage.getItem('accountId') || '')

// 原有互动状态
const isLiked = ref(false)
const isFavorited = ref(false)
const isDisliked = ref(false)

// ======== ✨ 评论区状态 ========
const comments = ref([])
const commentContent = ref('')
const submitting = ref(false)

// 回复控制状态
const activeReplyId = ref(null) // 当前打开回复框的评论ID
const replyParams = ref({
  parentId: 0,
  replyToUserId: null,
  replyToUserName: ''
})

// 计算评论总数 (包含所有子孙)
const totalCommentsCount = computed(() => {
  let count = 0
  const countNodes = (nodes) => {
    nodes.forEach(n => { 
      count++ 
      if (n.children && n.children.length) countNodes(n.children) 
    })
  }
  countNodes(comments.value)
  return count
})

// 无限级子孙评论扁平化方法 (B站楼中楼模式)
const flattenReplies = (children) => {
  let result = []
  children.forEach(child => {
    result.push(child)
    if (child.children && child.children.length > 0) {
      result = result.concat(flattenReplies(child.children))
    }
  })
  return result
}

// 1. 获取帖子详情 (包含原有的获取逻辑)
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/post/${route.params.id}`)
    let realData = null
    if (res.data && res.data.code === 200) realData = res.data.data
    else if (res.code === 200) realData = res.data
    else if (res.post) realData = res
    else realData = res 

    if (realData) {
      post.value = realData.post || realData 
      isLiked.value = realData.isLiked || false
      isFavorited.value = realData.isFavorited || false
    }
  } catch (err) {
    console.error('获取详情失败', err)
  } finally {
    loading.value = false
  }
}

// 2. 获取评论树
const fetchComments = async () => {
  try {
    const res = await request.get(`/comment/post/${route.params.id}`)
    let realData = null
    if (res.data && res.data.code === 200) realData = res.data.data
    else if (res.code === 200) realData = res.data
    else if (Array.isArray(res)) realData = res

    if (Array.isArray(realData)) {
      comments.value = realData
    }
  } catch (e) {
    console.error('获取评论失败', e)
  }
}

// 3. 打开回复框
// isSubReply: 是否在回复楼中楼的评论; topLevelId: 顶层楼主的ID
const openReply = (comment, isSubReply = false, topLevelId = null) => {
  activeReplyId.value = comment.id
  replyParams.value = {
    // 关键逻辑：无论回复谁，parentId永远挂在顶层楼主下，保证楼中楼扁平化
    parentId: isSubReply ? topLevelId : comment.id, 
    replyToUserId: comment.userId,
    replyToUserName: comment.authorName
  }
  commentContent.value = ''
}

// 4. 取消回复
const cancelReply = () => {
  activeReplyId.value = null
  replyParams.value = { parentId: 0, replyToUserId: null, replyToUserName: '' }
  commentContent.value = ''
}

// 5. 提交评论/回复
const submitComment = async () => {
  if (!commentContent.value.trim()) return ElMessage.warning('请输入评论内容')
  submitting.value = true
  try {
    const res = await request.post('/comment', {
      postId: route.params.id,
      content: commentContent.value,
      parentId: replyParams.value.parentId,
      replyToUserId: replyParams.value.replyToUserId
    })
    
    if (res.code === 200 || (res.data && res.data.code === 200)) {
      ElMessage.success('评论成功')
      cancelReply()
      fetchComments() // 重新拉取评论树
    }
  } catch (e) {
    ElMessage.error('评论失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 6. 删除评论
const handleDelete = (commentId) => {
  ElMessageBox.confirm('确定要删除这条评论吗？包含的回复也会被一并删除哦！', '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/comment/${commentId}`)
      if (res.code === 200 || (res.data && res.data.code === 200)) {
        ElMessage.success('已删除')
        fetchComments() // 重新拉取评论树
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// ======== 工具方法 ========
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const formatContent = (text) => {
  if (!text) return '暂无内容'
  let html = text.replace(/\n/g, '<br/>')
  html = html.replace(/!\[.*?\]\((.*?)\)/g, '<br/><img src="$1" style="max-width: 100%; border-radius: 8px; margin: 10px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.05);" /><br/>')
  return html
}

// ======== 互动三连逻辑 ========
const toggleLike = async () => {
  try {
    const res = await request.post(`/post/${route.params.id}/like`)
    if (res.code === 200 || (res.data && res.data.code === 200)) {
      isLiked.value = !isLiked.value
      if (isLiked.value) {
        isDisliked.value = false
        if (post.value.likeCount !== undefined) post.value.likeCount++ 
        ElMessage.success('点赞成功！')
      } else {
        if (post.value.likeCount !== undefined) post.value.likeCount--
        ElMessage.info('已取消点赞')
      }
    }
  } catch (err) {}
}

const toggleFavorite = async () => {
  try {
    const res = await request.post(`/post/${route.params.id}/favorite`)
    if (res.code === 200 || (res.data && res.data.code === 200)) {
      isFavorited.value = !isFavorited.value
      if (isFavorited.value) ElMessage.success('已加入你的个人收藏夹！')
      else ElMessage.info('已取消收藏')
    }
  } catch (err) {}
}

const toggleDislike = () => {
  isDisliked.value = !isDisliked.value
  if (isDisliked.value) {
    if (isLiked.value) toggleLike()
    ElMessage.warning('将减少此类内容的推荐')
  } else {
    ElMessage.info('已取消不喜欢')
  }
}

onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<style scoped>
.post-detail-container {
  padding: 30px;
  background-color: #F9FBF6;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center;
}

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

.post-title { font-size: 26px; color: #2F3E2F; margin-bottom: 20px; font-weight: bold; }
.post-meta { display: flex; align-items: center; gap: 15px; color: #99A97E; font-size: 13px; }
.author-tag { color: #8CB06B; border-color: #DBE7CF; background-color: #F2F7EA; }
.post-body { line-height: 1.8; color: #4A5D23; font-size: 16px; padding: 10px 0 30px 0; }

.post-actions {
  display: flex; justify-content: center; align-items: center; gap: 20px;
  margin-top: 20px; padding-top: 30px; border-top: 1px dashed #EDF2E9;
}
.action-btn {
  padding: 10px 24px; font-size: 15px; border: 1px solid #EDF2E9;
  background-color: #FAFCF7; color: #66784D; transition: all 0.3s ease;
}
.action-btn:hover { background-color: #F2F7EA; color: #8CB06B; border-color: #8CB06B; transform: translateY(-2px); }
.is-liked { background-color: #8CB06B !important; border-color: #8CB06B !important; color: #FFF !important; }
.is-favorited { background-color: #F39C12 !important; border-color: #F39C12 !important; color: #FFF !important; }
.is-disliked { background-color: #E0E0E0 !important; border-color: #E0E0E0 !important; color: #666 !important; }

/* ======== ✨ 评论区样式 ======== */
.comments-section {
  margin-top: 20px;
}
.comments-title {
  font-size: 18px; color: #2F3E2F; margin-bottom: 20px;
}

.comment-input-box {
  background: #FAFCF7; padding: 16px; border-radius: 12px; margin-bottom: 30px; border: 1px solid #EDF2E9;
}
.input-actions {
  display: flex; justify-content: flex-end; margin-top: 12px; gap: 10px;
}

.comment-list {
  display: flex; flex-direction: column; gap: 24px;
}
.comment-item {
  display: flex; gap: 16px;
}
.c-avatar {
  background-color: #8CB06B; color: white; flex-shrink: 0;
}
.c-main {
  flex: 1; border-bottom: 1px solid #EDF2E9; padding-bottom: 20px;
}
.comment-item:last-child .c-main { border-bottom: none; padding-bottom: 0; }

.c-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.c-name { font-size: 14px; font-weight: bold; color: #4A5D23; }
.c-time { font-size: 12px; color: #99A97E; }
.c-content { font-size: 15px; color: #333; line-height: 1.6; margin-bottom: 10px; white-space: pre-wrap; }

.c-actions { display: flex; gap: 16px; align-items: center; }
.action-text { font-size: 13px; color: #8CB06B; cursor: pointer; transition: color 0.2s; }
.action-text:hover { color: #7BA75F; }
.action-text.delete { color: #F56C6C; }

/* 楼中楼样式 */
.sub-comments {
  margin-top: 15px; background: #FAFCF7; padding: 16px; border-radius: 8px;
  display: flex; flex-direction: column; gap: 16px; border: 1px solid #EDF2E9;
}
.sub-c-header { font-size: 14px; line-height: 1.6; color: #333; }
.reply-to { color: #99A97E; margin: 0 4px; }
.c-content-inline { color: #444; }
.sub-actions { margin-top: 6px; }

/* 行内回复框 */
.inline-reply {
  margin-top: 15px; background: #fff; padding: 12px; border-radius: 8px; border: 1px solid #EDF2E9;
}
</style>