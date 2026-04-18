import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/api', // 对应上面的代理
  timeout: 5000
})

// 简单处理响应
instance.interceptors.response.use(
  res => res.data,
  err => {
    ElMessage.error(err.response?.data?.message || '网络连接失败')
    return Promise.reject(err)
  }
)

export default instance