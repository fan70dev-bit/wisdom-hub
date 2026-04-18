import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的后端地址
        changeOrigin: true,
        // 如果后端接口没有 /api 前缀，可以开启下面的重写
        // rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})