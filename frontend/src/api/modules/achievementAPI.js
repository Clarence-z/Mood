import api from '../axios'

export const achievementAPI = {
  // 获取成就列表
  getAchievements: () => api.get('/achievements'),
}

export default achievementAPI