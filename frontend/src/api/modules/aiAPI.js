import api from '../axios'

// AI对话相关API
export const aiAPI = {  
  // 1. 聊天（新增信息/创建对话）
  async chat(messageData) {
    console.log('发送消息:', messageData)
    const response = await api.post('/ai/chat', {
      message: messageData.message,
      conversationId: messageData.conversationId || null  // 可选参数
    })
    return response
  },

  // 2. 获取对话列表
  async getConversations() {
    const response = await api.get('/ai/conversations')
    return response
  },

  // 3. 获取特定对话的历史消息
  async getConversationMessages(conversationId) {
    const response = await api.get(`/ai/conversations/${conversationId}/messages`)
    return response
  },

  // 4. 更新对话标题
  async updateConversationTitle(conversationId, title) {
    const response = await api.put(`/ai/conversations/${conversationId}`, { title })
    return response
  },

  // 5. 删除对话
  async deleteConversation(conversationId) {
    const response = await api.delete(`/ai/conversations/${conversationId}`)
    return response
  },

  // 6. 创建空对话
  async createConversation(title) {
    const response = await api.post('/ai/conversations', { title })
    return response
  }
}

export default aiAPI