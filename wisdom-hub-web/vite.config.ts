import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 1. 引入 path 模块

export default defineConfig({
  plugins: [vue()],
  resolve: {
    // 2. 添加别名配置
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果后端接口本身就带 /api 前缀，这里不需要 rewrite
        // 如果后端接口不带 /api，请取消下面那一行的注释
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})