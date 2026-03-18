import api from "../axios"

// 用户相关API
export const userAPI = {
  login: (data) => api.post('/user/login', data),
  register: (data) => api.post('/user/register', data),
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  updateAvatar: (data) => api.post('/user/avatar', data),
  changePassword: (data) => api.put('/user/password', data)
}

export default userAPI