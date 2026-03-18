import api from "../axios"

// 心情日记相关API
export const moodAPI = {
  // 获取所有记录
  getMoods: (params) => api.get('/diary', { params }),
  // 获取单条记录
  getMoodById: (id) => api.get(`/diary/${id}`),
  // 添加记录
  addMood: (data) => api.post('/diary', data),
  // 删除记录
  deleteMood: (id) => api.delete(`diary/${id}`),
  // 更新记录
  updateMood: (id, data) => api.put(`/diary/${id}`, data),
}

export default moodAPI