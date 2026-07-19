<template>
  <section class="chat-window">
    <div ref="scrollArea" class="message-list">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <h2>Wisdom AI</h2>
        <p>可以直接提问，也可以让它帮你检索站内知识。</p>
      </div>

      <ChatMessage
        v-for="message in messages"
        :key="message.id"
        :message="message"
      />

      <div v-if="loading" class="typing-row">
        <el-avatar :size="34" class="typing-avatar">AI</el-avatar>
        <div class="typing-bubble">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <ChatInput
      :loading="loading"
      :responding="responding"
      @send="message => emit('send', message)"
      @stop="emit('stop')"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import ChatInput from './ChatInput.vue'
import ChatMessage from './ChatMessage.vue'
import type { AiChatMessage } from './types'

const props = defineProps<{
  messages: AiChatMessage[]
  loading: boolean
  responding: boolean
}>()

const emit = defineEmits<{
  send: [message: string]
  stop: []
}>()

const scrollArea = ref<HTMLElement | null>(null)
const messageContent = computed(() => props.messages.map(message => message.content).join('\n'))

const scrollToBottom = async () => {
  await nextTick()
  if (scrollArea.value) {
    scrollArea.value.scrollTop = scrollArea.value.scrollHeight
  }
}

watch(() => props.messages.length, scrollToBottom)
watch(messageContent, scrollToBottom, { flush: 'post' })
watch(() => props.loading, scrollToBottom)
</script>

<style scoped>
.chat-window {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #edf2e9;
  border-radius: 8px;
  background: #fbfdf8;
  overflow: hidden;
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.empty-state {
  margin: auto;
  max-width: 360px;
  text-align: center;
  color: #66784d;
}

.empty-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 16px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #f2f7e8;
  color: #8cb06b;
  font-size: 30px;
}

.empty-state h2 {
  margin: 0 0 8px;
  color: #4a5d23;
  font-size: 24px;
}

.empty-state p {
  margin: 0;
  line-height: 1.7;
  font-size: 14px;
}

.typing-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.typing-avatar {
  background: #4a5d23;
  color: #fff;
  font-weight: 700;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 5px;
  width: 70px;
  height: 38px;
  padding: 0 14px;
  border: 1px solid #edf2e9;
  border-radius: 8px;
  background: #fff;
}

.typing-bubble span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #8cb06b;
  animation: typing 1s infinite ease-in-out;
}

.typing-bubble span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-bubble span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }

  40% {
    opacity: 1;
    transform: translateY(-4px);
  }
}
</style>
