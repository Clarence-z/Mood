<template>
  <div class="theme-switcher">
    <button @click="toggleTheme" class="theme-toggle-btn" :title="`当前主题: ${currentThemeName} (点击切换)`">
      <span class="theme-emoji">{{ currentThemeEmoji }}</span>
      <span class="theme-name">{{ currentThemeName }}</span>
    </button>
    
    <div v-if="showThemePicker" class="theme-picker">
      <div class="theme-picker-header">
        <h3>选择主题</h3>
        <button @click="showThemePicker = false" class="close-btn">×</button>
      </div>
      <div class="theme-options">
        <button
          v-for="theme in availableThemes"
          :key="theme.id"
          @click="selectTheme(theme.id)"
          :class="['theme-option', { active: currentTheme === theme.id }]"
          :style="getThemeStyle(theme.id)"
        >
          <span class="theme-emoji">{{ theme.emoji }}</span>
          <span class="theme-label">{{ theme.name }}</span>
        </button>
      </div>
    </div>
    
    <!-- 遮罩层 -->
    <div v-if="showThemePicker" class="theme-picker-overlay" @click="showThemePicker = false"></div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const showThemePicker = ref(false)

const { currentTheme, availableThemes, setTheme, toggleTheme } = themeStore

const currentThemeName = computed(() => {
  return availableThemes.find(t => t.id === currentTheme)?.name || '默认'
})

const currentThemeEmoji = computed(() => {
  return availableThemes.find(t => t.id === currentTheme)?.emoji || '🌸'
})

const selectTheme = (themeId) => {
  setTheme(themeId)
  showThemePicker.value = false
}

const getThemeStyle = (themeId) => {
  // 这里可以根据主题ID返回不同的预览样式
  const styles = {
    default: { background: 'linear-gradient(135deg, #ffd6d6, #ebffe6)' },
    dark: { background: 'linear-gradient(135deg, #2d3748, #4a5568)' },
    blue: { background: 'linear-gradient(135deg, #bee3f8, #ebf8ff)' },
    purple: { background: 'linear-gradient(135deg, #e9d8fd, #faf5ff)' },
    pink: { background: 'linear-gradient(135deg, #fed7e2, #fff5f7)' }
  }
  return styles[themeId] || styles.default
}
</script>

<style scoped>
.theme-switcher {
  position: relative;
  z-index: 1000;
}

.theme-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--section-bg);
  border: 1px solid var(--border-light);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-primary);
}

.theme-toggle-btn:hover {
  background: var(--accent-hover);
  color: white;
  transform: translateY(-1px);
}

.theme-emoji {
  font-size: 1.2rem;
}

.theme-name {
  font-size: 0.9rem;
  font-weight: 500;
}

.theme-picker {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background: var(--container-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-medium);
  padding: 15px;
  min-width: 200px;
  box-shadow: var(--shadow-container);
  z-index: 1001;
}

.theme-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.theme-picker-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--text-secondary);
}

.theme-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 2px solid transparent;
  border-radius: var(--radius-small);
  background: var(--section-bg);
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-primary);
}

.theme-option:hover {
  border-color: var(--accent-color);
  transform: translateX(5px);
}

.theme-option.active {
  border-color: var(--accent-color);
  background: var(--accent-color);
  color: white;
}

.theme-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}
</style>