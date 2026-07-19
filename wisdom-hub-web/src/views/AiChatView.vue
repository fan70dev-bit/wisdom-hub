<template>
  <div class="ai-chat-page">
    <div class="page-header">
      <div>
        <h1>Wisdom AI</h1>
        <p>基于站内知识与会话记忆的智能助手</p>
      </div>
      <el-tag effect="light" class="status-tag">
        流式对话
      </el-tag>
    </div>

    <ChatWindow
      class="chat-panel"
      :messages="messages"
      :loading="loading"
      :responding="responding"
      @send="handleSend"
      @stop="handleStop"
    />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ChatWindow from '@/components/ai/ChatWindow.vue'
import type { AiChatMessage } from '@/components/ai/types'
import { streamAgentMessage } from '@/services/agent'

const messages = ref<AiChatMessage[]>([])
const loading = ref(false)
const responding = ref(false)
const abortController = ref<AbortController | null>(null)
let pendingText = ''
let typingTimer: number | null = null

const createId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`

const handleSend = async (content: string) => {
  if (responding.value) {
    return
  }

  const assistantMessage: AiChatMessage = {
    id: createId(),
    role: 'assistant',
    content: ''
  }
  const assistantMessageId = assistantMessage.id
  const controller = new AbortController()

  messages.value.push({
    id: createId(),
    role: 'user',
    content
  })
  messages.value.push(assistantMessage)

  loading.value = true
  responding.value = true
  abortController.value = controller

  try {
    await streamAgentMessage({ message: content }, {
      signal: controller.signal,
      onToken: token => {
        if (loading.value) {
          loading.value = false
        }
        enqueueToken(assistantMessageId, token)
      }
    })
  } catch (error) {
    if (isAbortError(error)) {
      if (!findMessage(assistantMessageId)?.content) {
        updateAssistantMessage(assistantMessageId, '已停止，未收到新的内容。')
      }
      return
    }

    console.error('AI 请求失败:', error)
    ElMessage.error('AI 暂时没有回应，请稍后再试')
    if (!findMessage(assistantMessageId)?.content) {
      updateAssistantMessage(assistantMessageId, '抱歉，我刚刚没有连接上服务。请稍后再试。')
    }
  } finally {
    loading.value = false
    responding.value = false
    abortController.value = null
  }
}

const handleStop = () => {
  abortController.value?.abort()
}

const enqueueToken = (messageId: string, token: string) => {
  console.debug('[Agent Stream] AiChatView enqueueToken', token)
  pendingText += token
  drainTokenQueue(messageId)
}

const drainTokenQueue = (messageId: string) => {
  if (typingTimer !== null) {
    return
  }

  const tick = () => {
    if (!pendingText) {
      typingTimer = null
      return
    }

    const nextChar = pendingText.slice(0, 1)
    appendAssistantMessage(messageId, nextChar)
    pendingText = pendingText.slice(1)
    typingTimer = window.setTimeout(tick, 12)
  }

  tick()
}

const findMessage = (messageId: string) => messages.value.find(message => message.id === messageId)

const appendAssistantMessage = (messageId: string, chunk: string) => {
  const index = messages.value.findIndex(message => message.id === messageId)
  if (index === -1) {
    return
  }
  const current = messages.value[index]
  messages.value[index] = {
    ...current,
    content: current.content + chunk
  }
  console.debug('[Agent Stream] AiChatView append', chunk, messages.value[index].content)
}

const updateAssistantMessage = (messageId: string, content: string) => {
  const index = messages.value.findIndex(message => message.id === messageId)
  if (index === -1) {
    return
  }
  messages.value[index] = {
    ...messages.value[index],
    content
  }
}

const isAbortError = (error: unknown) => error instanceof DOMException && error.name === 'AbortError'

onBeforeUnmount(() => {
  abortController.value?.abort()
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
  }
})
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
