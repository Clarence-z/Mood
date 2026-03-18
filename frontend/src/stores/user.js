import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userAPI } from '@/api/index'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const isAuthenticated = computed(() => !!token.value)
  
  // 计算属性
  const userInfo = computed(() => user.value)
  const isLoggedIn = computed(() => isAuthenticated.value)

  // 动作
  const login = async (credentials) => {
    try {
        const response = await userAPI.login(credentials)
        token.value = response.data.token
        localStorage.setItem('token', token.value)
        
        // 添加延迟，确保token已保存
        await new Promise(resolve => setTimeout(resolve, 100))
        
        await fetchProfile()
        return response.data
    } catch (error) {
        console.error('登录失败:', error)
        // 登录失败时清除token
        localStorage.removeItem('token')
        token.value = null
        throw error
    }
  }

  const register = async (userData) => {
    try {
      const response = await userAPI.register(userData)
      return response.data
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }

  const fetchProfile = async () => {
    try {
      const response = await userAPI.getProfile()
      user.value = response.data
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  const updateProfile = async (profileData) => {
    try {
      const response = await userAPI.updateProfile(profileData)
      user.value = { ...user.value, ...response.data }
      return response.data
    } catch (error) {
      console.error('更新用户信息失败:', error)
      throw error
    }
  }

  // 更新头像
  const updateAvatar = async (avatarData) => {
    try {
      const response = await userAPI.updateAvatar({ avatarData })
      // 更新本地用户信息
      user.value = response.data
      return response.data
    } catch (error) {
      console.error('更新头像失败:', error)
      throw error
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  return {
    // 状态
    user,
    token,
    
    // 计算属性
    isAuthenticated,
    userInfo,
    isLoggedIn,
    
    // 动作
    login,
    register,
    fetchProfile,
    updateProfile,
    updateAvatar,
    logout
  }
})