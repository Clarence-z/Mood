import api from '../axios'

// 待办事项相关API
export const todoAPI = {
  // 获取列表 (带筛选参数)
  getList: (params) => api.get('/todos', { params }), // Axios会自动将params对象转换为query string
  getListByPage: (params) => api.get('/todos/page', { params }), // 分页接口
  // 获取单条详情
  getById: (id) => api.get(`/todos/${id}`),
  // 创建
  create: (data) => api.post('/todos', data),
  // 更新
  update: (id, data) => api.put(`/todos/${id}`, data),
  // 删除
  delete: (id) => api.delete(`/todos/${id}`),
  // 状态操作
  markAsTodo: (id) => api.patch(`/todos/${id}/todo`),
  markAsDone: (id) => api.patch(`/todos/${id}/done`),
  togglePin: (id) => api.patch(`/todos/${id}/pin`),
  // 统计
  getStatistics: () => api.get('/todos/statistics'),
};

export default todoAPI;