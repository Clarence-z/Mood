import axios from 'axios'
// import router from '@/router'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/mood',
  timeout: 1000000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器，自动添加token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  
  // 如果是OPTIONS请求，不添加认证头
  if (config.method === 'options') {
    delete config.headers.Authorization
  }

  return config
})

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // router.push('/login')
          // 可以跳转到登录页
          // 动态导入 router，避免循环依赖
          import('@/router').then(module => {
            module.default.push('/login')
          })
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default api