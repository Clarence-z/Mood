import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { achievementAPI } from '@/api/index'

export const useAchievementStore = defineStore('achievement', () => {
  // 状态
  const achievements = ref([])        // 所有成就列表
  const loading = ref(false)          // 加载状态
  const error = ref(null)             // 错误信息

  // 计算属性
  const earnedAchievements = computed(() => 
    achievements.value.filter(a => a.isAchieved)
  )
  const unearnedAchievements = computed(() => 
    achievements.value.filter(a => !a.isAchieved)
  )
  const totalEarned = computed(() => earnedAchievements.value.length)
  const totalCount = computed(() => achievements.value.length)

  // 动作：获取成就
  const fetchAchievements = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await achievementAPI.getAchievements()
      // 后端返回的数据结构为Result<DTO>
      achievements.value = response.data.data || []
    } catch (err) {
      console.error('获取成就失败:', err)
      error.value = err.message || '获取成就失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 获取进度文本
  const getProgressText = (achievement) => {
    if (achievement.isAchieved) return '已达成'
    if (achievement.currentProgress != null && achievement.threshold != null) {
      return `${achievement.currentProgress}/${achievement.threshold}`
    }
    return '未开始'
  }

  return {
    // 状态
    achievements,
    loading,
    error,
    // 计算属性
    earnedAchievements,
    unearnedAchievements,
    totalEarned,
    totalCount,
    // 动作
    fetchAchievements,
    getProgressText
  }
})