<template>
  <div class="chat-input">
    <el-input
      v-model="draft"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 6 }"
      :disabled="loading"
      resize="none"
      placeholder="问问 Wisdom AI..."
      @keydown.enter.exact.prevent="submit"
    />
    <el-button
      type="primary"
      class="send-button"
      :loading="loading"
      :disabled="!draft.trim()"
      @click="submit"
    >
      <el-icon><Promotion /></el-icon>
      <span>发送</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Promotion } from '@element-plus/icons-vue'

defineProps<{
  loading: boolean
}>()

const emit = defineEmits<{
  send: [message: string]
}>()

const draft = ref('')

const submit = () => {
  const message = draft.value.trim()
  if (!message) {
    return
  }
  emit('send', message)
  draft.value = ''
}
</script>

<style scoped>
.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  padding: 16px;
  border-top: 1px solid #edf2e9;
  background: #fff;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #dfe8d7;
  box-shadow: none;
  color: #2f3e2f;
  line-height: 1.6;
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: #8cb06b;
}

.send-button {
  min-width: 104px;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: #8cb06b;
  font-weight: 700;
}

.send-button:hover,
.send-button:focus {
  background: #7ba75f;
}

@media (max-width: 640px) {
  .chat-input {
    grid-template-columns: 1fr;
  }

  .send-button {
    width: 100%;
  }
}
</style>
