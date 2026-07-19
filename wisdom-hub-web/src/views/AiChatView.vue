<template>
  <div class="ai-chat-page">
    <div class="page-header">
      <div>
        <h1>Wisdom AI</h1>
        <p>基于站内知识与会话记忆的智能助手</p>
      </div>
      <el-tag effect="light" class="status-tag">
        普通对话
      </el-tag>
    </div>

    <ChatWindow
      class="chat-panel"
      :messages="messages"
      :loading="loading"
      @send="handleSend"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import ChatWindow from '@/components/ai/ChatWindow.vue'
import type { AiChatMessage } from '@/components/ai/types'
import { sendAgentMessage } from '@/services/agent'

const messages = ref<AiChatMessage[]>([])
const loading = ref(false)

const createId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`

const handleSend = async (content: string) => {
  if (loading.value) {
    return
  }

  messages.value.push({
    id: createId(),
    role: 'user',
    content
  })

  loading.value = true
  try {
    const response = await sendAgentMessage({ message: content })
    messages.value.push({
      id: createId(),
      role: 'assistant',
      content: response.answer || '没有收到有效回复。',
      traceId: response.traceId
    })
  } catch (error) {
    console.error('AI 请求失败:', error)
    ElMessage.error('AI 暂时没有回应，请稍后再试')
    messages.value.push({
      id: createId(),
      role: 'assistant',
      content: '抱歉，我刚刚没有连接上服务。请稍后再试。'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-chat-page {
  height: calc(100vh - 64px);
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 24px 30px;
  background: #fff;
}

.page-header {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-header h1 {
  margin: 0;
  color: #2f3e2f;
  font-size: 26px;
  font-weight: 800;
}

.page-header p {
  margin: 6px 0 0;
  color: #8b9a78;
  font-size: 14px;
}

.status-tag {
  border-color: #dce8d2;
  color: #6f9957;
  background: #f2f7e8;
}

.chat-panel {
  flex: 1;
}

@media (max-width: 640px) {
  .ai-chat-page {
    padding: 18px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
