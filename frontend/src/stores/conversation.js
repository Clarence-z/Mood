import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
// import { aiAPI } from '@/services/api'
import { aiAPI } from '@/api/index'

export const useConversationStore = defineStore('conversation', () => {
  // 状态
  const conversations = ref([])  // 所有对话
  const currentConversationId = ref(null)  // 当前选中的对话ID
  const isSidebarVisible = ref(true)  // 侧边栏是否可见
  const isLoading = ref(false)

  // 计算属性
  const currentConversation = computed(() => {
    console.log(currentConversation)
    return conversations.value.find(c => c.id === currentConversationId.value)
  })

  const sortedConversations = computed(() => {
    return [...conversations.value].sort((a, b) => 
      new Date(b.updatedAt) - new Date(a.updatedAt)
    )
  })

  // const formattedConversations = computed(() => {
  //   return conversations.value.map(conversation => ({
  //     ...conversation,
  //     // 添加格式化后的时间字段，不影响原始数据
  //     formattedUpdatedAt: formatRelativeTime(conversation.updatedAt),
  //     // 如果需要其他格式，也可以添加
  //     // standardDate: formatDate(conversation.updatedAt, 'standard'),
  //     // shortDate: formatDate(conversation.updatedAt, 'short')
  //   }))
  // })

  // const sortedConversations = computed(() => {
  //   return [...formattedConversations.value].sort((a, b) => 
  //     new Date(b.formattedUpdatedAt) - new Date(a.formattedUpdatedAt)
  //   )
  // })

  // 动作
  const loadConversations = async () => {
    try {
      isLoading.value = true
      const response = await aiAPI.getConversations()
      console.log('加载对话列表:', response.data)
      conversations.value = response.data || []
    } catch (error) {
      console.error('加载对话列表失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const selectConversation = async (conversationId) => {
    currentConversationId.value = conversationId
    console.log('选中对话ID:', conversationId)
    // 加载该对话的历史消息
  }

  const createNewConversation = async () => {
    try {
      const response = await aiAPI.createConversation('新对话')
      const newConversation = response.data
      console.log(response)
      conversations.value.unshift(newConversation)
      await selectConversation(newConversation.id)
      return newConversation
    } catch (error) {
      console.error('创建对话失败:', error)
      throw error
    }
  }

  const updateConversationTitle = async (conversationId, newTitle) => {
    try {
      await aiAPI.updateConversationTitle(conversationId, newTitle)
      const conversation = conversations.value.find(c => c.id === conversationId)
      if (conversation) {
        conversation.title = newTitle
        conversation.updatedAt = new Date().toISOString()
      }
    } catch (error) {
      console.error('更新对话标题失败:', error)
      throw error
    }
  }

  const deleteConversation = async (conversationId) => {
    try {
      await aiAPI.deleteConversation(conversationId)
      conversations.value = conversations.value.filter(c => c.id !== conversationId)
      if (currentConversationId.value === conversationId) {
        currentConversationId.value = null
      }
    } catch (error) {
      console.error('删除对话失败:', error)
      throw error
    }
  }

  const toggleSidebar = () => {
    isSidebarVisible.value = !isSidebarVisible.value
  }

  // 初始化时加载对话列表
  const init = async () => {
    await loadConversations()
  }

  return {
    // 状态
    conversations,
    currentConversationId,
    currentConversation,
    isSidebarVisible,
    isLoading,
    // formattedConversations,
    sortedConversations,

    // 动作
    loadConversations,
    selectConversation,
    createNewConversation,
    updateConversationTitle,
    deleteConversation,
    toggleSidebar,
    init
  }
})