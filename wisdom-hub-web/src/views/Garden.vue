<template>
  <div class="garden-container">
    <!-- 顶部看板 -->
    <div class="profile-header">
      <div class="header-content">
        <el-avatar :size="100" :src="userInfo.avatarUrl" class="user-avatar" />
        <div class="user-info-text">
          <div class="name-row">
            <h2 class="username">{{ userInfo.username }}</h2>
            <el-tag size="small" effect="plain" class="id-tag">ID: {{ userInfo.accountId }}</el-tag>
          </div>
          <p class="bio">{{ userInfo.motto || '种一棵树最好的时间是十年前，其次是现在。' }}</p>
          
          <div class="stat-row">
            <div class="stat-item"><strong>{{ stats.thoughts }}</strong> <span>碎碎念</span></div>
            <div class="stat-item"><strong>{{ stats.articles }}</strong> <span>长文</span></div>
            <div class="stat-item"><strong>{{ stats.totalLikes }}</strong> <span>获赞</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 动态列表区 -->
    <div class="garden-content">
      <el-tabs v-model="activeTab" class="custom-tabs">
        
        <!-- ================= Tab 1: 全部动态 ================= -->
        <el-tab-pane label="全部动态" name="all">
          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else-if="postList.length > 0">
            <!-- 渲染所有帖子 -->
            <div 
              v-for="post in postList" 
              :key="post.id" 
              class="post-card"
              @click="goToDetail(post.id)"
            >
              <div class="post-header">
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
                <div class="header-tags">
                  <el-tag :type="post.type === 0 ? 'success' : 'warning'" size="small" style="margin-right: 8px;">
                    {{ post.type === 0 ? '长文' : '碎碎念' }}
                  </el-tag>
                  <el-tag :type="post.status === 1 ? 'info' : 'success'" size="small">
                    {{ post.status === 1 ? '私密' : '公开' }}
                  </el-tag>
                </div>
              </div>
              <h3 class="post-title" v-if="post.type === 0">{{ post.title }}</h3>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="post-tag"><el-icon><Star /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="花园里还没有播种，快去发布动态吧" />
        </el-tab-pane>

        <!-- ================= Tab 2: 仅长文 ================= -->
        <el-tab-pane label="长文" name="articles">
          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else-if="articlesList.length > 0">
            <!-- 仅渲染长文 -->
            <div 
              v-for="post in articlesList" 
              :key="'article-'+post.id" 
              class="post-card"
              @click="goToDetail(post.id)"
            >
              <div class="post-header">
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
                <div class="header-tags">
                  <el-tag type="success" size="small" style="margin-right: 8px;">长文</el-tag>
                  <el-tag :type="post.status === 1 ? 'info' : 'success'" size="small">
                    {{ post.status === 1 ? '私密' : '公开' }}
                  </el-tag>
                </div>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="post-tag"><el-icon><Star /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="还没有发布过长文专栏哦" />
        </el-tab-pane>

        <!-- ================= Tab 3: 仅碎碎念 ================= -->
        <el-tab-pane label="碎碎念" name="thoughts">
          <el-skeleton :rows="5" animated v-if="loading" />
          <div v-else-if="thoughtsList.length > 0">
            <!-- 仅渲染碎碎念 -->
            <div 
              v-for="post in thoughtsList" 
              :key="'thought-'+post.id" 
              class="post-card"
              @click="goToDetail(post.id)"
            >
              <div class="post-header">
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
                <div class="header-tags">
                  <el-tag type="warning" size="small" style="margin-right: 8px;">碎碎念</el-tag>
                  <el-tag :type="post.status === 1 ? 'info' : 'success'" size="small">
                    {{ post.status === 1 ? '私密' : '公开' }}
                  </el-tag>
                </div>
              </div>
              <!-- 碎碎念没有标题，直接渲染内容 -->
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-footer">
                <span class="post-tag"><el-icon><Star /></el-icon> {{ post.likeCount || 0 }} 赞</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="没有任何碎碎念吐槽哦" />
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
// 🌟 注意这里导入了 computed
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Star } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const activeTab = ref('all')
const loading = ref(false)
const postList = ref([]) // 存放后端原始的所有数据

// ========== 🌟 核心：智能计算属性 ==========
// 当 postList 有数据时，自动过滤出 type === 0 的长文数组
const articlesList = computed(() => {
  return postList.value.filter(post => post.type === 0)
})

// 当 postList 有数据时，自动过滤出 type === 1 的碎碎念数组
const thoughtsList = computed(() => {
  return postList.value.filter(post => post.type === 1)
})
// ============================================

const userInfo = reactive({
  username: localStorage.getItem('username') || 'Wisdom 用户',
  accountId: localStorage.getItem('accountId') || '未登录',
  avatarUrl: localStorage.getItem('avatarUrl') || '',
  motto: localStorage.getItem('motto') || '' 
})

const stats = reactive({
  thoughts: 0,
  articles: 0,
  totalLikes: 0
})

const fetchGardenData = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/garden')
    
    // 终极拆盒法
    let realData = []
    if (res.data && res.data.code === 200) {
      realData = res.data.data
    } else if (res.code === 200) {
      realData = res.data
    } else if (Array.isArray(res)) {
      realData = res
    } else if (Array.isArray(res.data)) {
      realData = res.data
    }

    postList.value = realData || []

    let thoughtsCount = 0
    let articlesCount = 0
    let likesCount = 0

    postList.value.forEach(post => {
      if (post.type === 0) articlesCount++
      if (post.type === 1) thoughtsCount++
      likesCount += (post.likeCount || 0)
    })

    stats.thoughts = thoughtsCount
    stats.articles = articlesCount
    stats.totalLikes = likesCount

  } catch (error) {
    console.error('获取帖子失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/post/${id}`)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const getExcerpt = (content) => {
  if (!content) return ''
  let text = content.replace(/!\[.*?\]\(.*?\)/g, '[图片]')
  text = text.replace(/[#*`~>-]/g, '')
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

onMounted(() => {
  fetchGardenData()
})
</script>

<style scoped>
.garden-container { max-width: 900px; margin: 0 auto; padding: 30px; }

/* 顶部看板样式 */
.profile-header { background: linear-gradient(135deg, #F2F7E8 0%, #ffffff 100%); border-radius: 24px; padding: 40px; margin-bottom: 30px; border: 1px solid #eef3e6; }
.header-content { display: flex; align-items: center; gap: 30px; }
.user-avatar { border: 4px solid #fff; box-shadow: 0 8px 20px rgba(140, 176, 107, 0.2); }
.username { font-size: 28px; color: #4A5D23; margin: 0; }
.name-row { display: flex; align-items: center; gap: 12px; }
.id-tag { color: #8CB06B; border-color: #8CB06B; }
.bio { color: #66784d; margin: 12px 0 20px; font-size: 15px; }
.stat-row { display: flex; gap: 25px; }
.stat-item { color: #99a97e; font-size: 14px; }
.stat-item strong { color: #4A5D23; font-size: 18px; margin-right: 4px; }

/* 动态卡片样式 */
.post-card { 
  background: #fff; 
  border-radius: 16px; 
  padding: 24px; 
  margin-bottom: 20px; 
  border: 1px solid #f0f0f0; 
  transition: all 0.3s;
  cursor: pointer; 
}
.post-card:hover { transform: translateY(-3px); box-shadow: 0 10px 25px rgba(0,0,0,0.05); border-color: #8CB06B; }
.post-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.post-time { font-size: 13px; color: #999; }
.post-title { font-size: 18px; color: #333; margin: 0 0 10px; font-weight: bold; }
.post-excerpt { color: #666; line-height: 1.6; font-size: 14px; margin-bottom: 0; }
.post-footer { margin-top: 15px; display: flex; gap: 8px; }
.post-tag { background: #f2f7e8; color: #8CB06B; padding: 4px 10px; border-radius: 6px; font-size: 12px; display: flex; align-items: center; gap: 4px; }

/* Tabs 样式定制 */
:deep(.el-tabs__item.is-active) { color: #8CB06B !important; }
:deep(.el-tabs__active-bar) { background-color: #8CB06B !important; }
</style>