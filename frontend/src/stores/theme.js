import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('theme') || 'default')
  
  // 可用主题列表
  const availableThemes = [
    { id: 'default', name: '默认', emoji: '🌸' },
    { id: 'dark', name: '深色', emoji: '🌙' },
    { id: 'blue', name: '蓝色', emoji: '💙' },
    { id: 'purple', name: '紫色', emoji: '💜' },
    { id: 'pink', name: '粉色', emoji: '💖' }
  ]
  
  // 设置主题
  const setTheme = (themeId) => {
    currentTheme.value = themeId
    localStorage.setItem('theme', themeId)
    applyTheme(themeId)
  }
  
  // 应用主题到DOM
  const applyTheme = (themeId) => {
    const html = document.documentElement
    if (themeId === 'default') {
      html.removeAttribute('data-theme')
    } else {
      html.setAttribute('data-theme', themeId)
    }
  }
  
  // 切换主题（循环切换）
  const toggleTheme = () => {
    const currentIndex = availableThemes.findIndex(t => t.id === currentTheme.value)
    const nextIndex = (currentIndex + 1) % availableThemes.length
    setTheme(availableThemes[nextIndex].id)
  }
  
  // 初始化时应用主题
  applyTheme(currentTheme.value)
  
  // 监听主题变化，保存到localStorage
  watch(currentTheme, (newTheme) => {
    localStorage.setItem('theme', newTheme)
  })
  
  return {
    currentTheme,
    availableThemes,
    setTheme,
    toggleTheme
  }
})