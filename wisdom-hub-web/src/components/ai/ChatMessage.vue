<template>
  <div class="message-row" :class="message.role">
    <el-avatar :size="34" class="message-avatar">
      {{ avatarText }}
    </el-avatar>
    <div class="message-content">
      <div class="message-meta">
        <span>{{ senderName }}</span>
        <span v-if="message.traceId" class="trace-id">Trace {{ message.traceId.slice(0, 8) }}</span>
      </div>
      <div class="message-bubble">
        <MarkdownRenderer v-if="message.role === 'assistant'" :content="message.content" />
        <p v-else class="plain-text">{{ message.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AiChatMessage } from './types'
import MarkdownRenderer from './MarkdownRenderer.vue'

const props = defineProps<{
  message: AiChatMessage
}>()

const senderName = computed(() => props.message.role === 'user' ? '你' : 'Wisdom AI')
const avatarText = computed(() => props.message.role === 'user' ? '我' : 'AI')
</script>

<style scoped>
.message-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex: 0 0 auto;
  background: #8cb06b;
  color: #fff;
  font-weight: 700;
}

.message-row.assistant .message-avatar {
  background: #4a5d23;
}

.message-content {
  max-width: min(720px, calc(100% - 52px));
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-row.user .message-content {
  align-items: flex-end;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #8b9a78;
  font-size: 12px;
}

.trace-id {
  color: #b4bda8;
}

.message-bubble {
  border: 1px solid #edf2e9;
  border-radius: 8px;
  background: #fff;
  padding: 13px 15px;
  color: #2f3e2f;
  box-shadow: 0 6px 18px rgba(74, 93, 35, 0.05);
}

.message-row.user .message-bubble {
  background: #8cb06b;
  border-color: #8cb06b;
  color: #fff;
}

.plain-text {
  margin: 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.7;
  font-size: 14px;
}
</style>
