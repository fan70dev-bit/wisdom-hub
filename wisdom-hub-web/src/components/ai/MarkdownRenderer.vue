<template>
  <div class="markdown-body ai-markdown" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'github-markdown-css/github-markdown-light.css'
import 'highlight.js/styles/github.css'

const props = defineProps<{
  content: string
}>()

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(code: string, language: string): string {
    if (language && hljs.getLanguage(language)) {
      return hljs.highlight(code, { language }).value
    }
    return hljs.highlightAuto(code).value
  }
})

const renderedHtml = computed(() => markdown.render(props.content || ''))
</script>

<style scoped>
.ai-markdown {
  background: transparent;
  color: inherit;
  font-size: 14px;
  line-height: 1.7;
}

.ai-markdown :deep(p) {
  margin: 0 0 10px;
}

.ai-markdown :deep(p:last-child) {
  margin-bottom: 0;
}

.ai-markdown :deep(pre) {
  border-radius: 8px;
  border: 1px solid #e5eadf;
  background: #f8faf6;
  font-size: 13px;
  overflow-x: auto;
}

.ai-markdown :deep(code) {
  border-radius: 4px;
}

.ai-markdown :deep(a) {
  color: #6f9957;
}
</style>
