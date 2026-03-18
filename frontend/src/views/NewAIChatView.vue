<template>
  <div class="ai-chat-container" ref="chatContainer">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': !isSidebarVisible }">
      <div class="sidebar-header">
        <div class="sidebar-title"><h3>历史对话</h3></div>
        <div class="actions-container">
          <Plus @click="createNewConversation" class="new-chat-icon"/>
          <ArrowLeft v-if="isSidebarVisible" @click="toggleSidebar" class="close-icon"/>
        </div>
      </div>

      <div class="conversation-list">
        <div 
          v-for="conv in sortedConversations" 
          :key="conv.id"
          class="conversation-item"
          :class="{ active: currentConversationId === conv.id }"
          @click="conversationStore.selectConversation(conv.id)"
        >
          <div class="conv-content">
            <div class="conv-container">
              <span class="conv-title">{{ conv.title || '未命名对话' }}</span>
              <span class="conv-actions" v-if="currentConversationId === conv.id">
              <el-dropdown 
                trigger="click" 
                placement="bottom-end"
                @command="handleConvAction"
              >
                <el-icon class="more-icon">
                  <MoreFilled />
                </el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', conversation: conv }">
                      <el-icon><Edit /></el-icon>
                      <span>编辑名称</span>
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{ action: 'delete', conversation: conv }"
                      class="delete-action"
                    >
                      <el-icon><Delete /></el-icon>
                      <span>删除对话</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              </span>
            </div>
            <div class="conv-meta">
              <span>{{ conv.messageCount }} 条消息</span>
              <span>{{ formatDate(conv.updatedAt) }}</span>
               <!-- <span class="time">{{ conv.formattedUpdatedAt }}</span> -->
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <div class="main-wrapper">
    <main class="chat-main" :class="{ 'sidebar-expanded': isSidebarVisible }">
      <!-- 标题栏 -->
      <!-- <header class="chat-header">
        <div class="header-content">
          <div class="title-grid" v-if="currentConversation">
            <div class="arrow-container">
              <ArrowRight v-if="!isSidebarVisible" @click="toggleSidebar" class="open-icon"/>
              <ArrowRight v-else @click="toggleSidebar" class="open-icon-trans"/>
            </div>
            <div class="title">
              <h2>{{ currentConversation.title || '新对话' }}
                <Edit 
                class="edit-title-icon" 
                @click="showEditTitleDialog(currentConversation)"
              />
              </h2>
            </div>
            <div class="user-nav">
              <UserNav />
            </div>
          </div>
          <div class="title-grid" v-else>
            <div class="arrow">
              <ArrowRight v-if="!isSidebarVisible" @click="toggleSidebar" class="open-icon"/>
              <ArrowRight v-else @click="toggleSidebar" class="open-icon-trans"/>
            </div>
            <div class="title"><h2>AI心理助手 💭</h2></div>
            <div class="user-nav">
              <UserNav />
            </div>
          </div>
        </div>
      </header> -->

      <header class="chat-header">
        <div class="header-content">
          <div class="title-grid">
            <!-- 左侧：侧边栏切换按钮，始终显示 -->
            <div class="sidebar-toggle">
              <el-icon :size="22" class="toggle-icon" @click="toggleSidebar">
                <!-- <component :is="isSidebarVisible ? ArrowLeft : ArrowRight" /> -->
                <ArrowRight v-if="!isSidebarVisible" class="open-icon"/>
              </el-icon>
            </div>

            <!-- 中间：标题（自适应居中，过长省略） -->
            <div class="title">
              <h2 v-if="currentConversation">
                {{ currentConversation.title || '新对话' }}
                <Edit class="edit-title-icon" @click="showEditTitleDialog(currentConversation)" />
              </h2>
              <h2 v-else>AI心理助手 💭</h2>
            </div>

            <!-- 右侧：用户导航，固定宽度由内容决定 -->
            <div class="user-nav">
              <UserNav />
            </div>
          </div>
        </div>
      </header>

      <div class="messages-container" ref="messagesContainer">
        <!-- 欢迎消息（如果没有选择对话） -->
        <div v-if="!currentConversationId" class="welcome-message">
          <div class="welcome-content">
            <h3>欢迎使用AI心灵伙伴！</h3>
            <p>请从左侧选择对话或创建新对话开始聊天。</p>
            <p>你也可以使用下面的输入框直接开始新的对话。</p>
          </div>
        </div>

        <!-- 对话消息 -->
        <div 
          v-else
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message', message.role]"
        >
          <!-- <div class="message-avatar">
            <div class="message-avatar">
              <img v-if="message.role === 'user'" :src="userAvatar" alt="用户头像" class="avatar-img" />
              <img v-else :src="aiAvatar" alt="AI头像" class="avatar-img" />
            </div>
          </div> -->
          <div class="message-content">
            <div v-html="formatMessage(message.content)"></div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="message assistant thinking">
          <!-- <div class="message-avatar">🤖</div> -->
          <div class="message-content">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 固定的输入区域 -->
      <footer class="input-area">
        <div class="input-container">
          <div class="input-wrapper">
            <textarea
              v-model="userInput"
              @input="handleInput"
              @focus="updateTextareaScrollbar"
              @blur="shouldShowScrollbar = false"
              @keydown.enter.exact.prevent="sendMessage"
              ref="textAreaRef"
              class="message-input"
              :class="{ 'show-scrollbar': shouldShowScrollbar }"
              placeholder="告诉我你今天的心情如何？或者有什么想聊的吗？"
              rows="1"
            ></textarea>
            <button 
              @click="sendMessage" 
              :disabled="!userInput.trim() || loading"
              class="send-button"
            >
              <span v-if="!loading">发送</span>
              <span v-else>发送中...</span>
            </button>
          </div>
          <div class="input-hints">
            <span class="hint">试试问：</span>
            <button 
              v-for="hint in quickHints" 
              :key="hint"
              @click="userInput = hint"
              class="hint-button"
            >
              {{ hint }}
            </button>
          </div>
        </div>
      </footer>
    </main>

    <!-- 编辑标题对话框 -->
    <el-dialog 
      v-model="editTitleDialogVisible" 
      title="修改对话标题" 
      width="400px"
    >
    <div class="edit-title-dialog">
      <el-input 
        class="el-input-self"
        v-model="editingTitle" 
        placeholder="请输入对话标题"
        maxlength="50"
        @keyup.enter="updateConversationTitle"
      />
      <div class="dialog-actions">
        <el-button class="el-button-no" @click="editTitleDialogVisible = false">取消</el-button>
        <el-button class="el-button-yes" type="primary" @click="updateConversationTitle">确定</el-button>
      </div>
    </div>
    </el-dialog>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useConversationStore } from '@/stores/conversation'
import { aiAPI } from '@/api/index'
import { formatMessage } from '@/utils/markdown'
import { Edit, Plus, ArrowLeft, ArrowRight, MoreFilled, Delete } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import UserNav from '@/components/UserNav.vue'
import defaultAvatar from '@/assets/image/default-avatar.png'

const userStore = useUserStore() // 如果已有则无需重复
const userAvatar = computed(() => userStore.user?.avatar || defaultAvatar)
const aiAvatar = defaultAvatar // 可以替换为AI专用头像

const conversationStore = useConversationStore()
const messages = ref([])
const loading = ref(false)
const userInput = ref('')
const messagesContainer = ref(null)
const chatContainer = ref(null)
const textAreaRef = ref(null)
const shouldShowScrollbar = ref(false)

// 编辑标题相关
const editTitleDialogVisible = ref(false)
const editingConversationId = ref(null)
const editingTitle = ref('')

// 使用store中的计算属性
const isSidebarVisible = computed(() => conversationStore.isSidebarVisible)
const currentConversationId = computed(() => conversationStore.currentConversationId)
const currentConversation = computed(() => conversationStore.currentConversation)
const sortedConversations = computed(() => conversationStore.sortedConversations)

const quickHints = ref([
  '我今天心情不太好，能给我一些建议吗？',
  '如何缓解焦虑情绪？',
  '推荐一些自我关怀的方法',
  '我最近压力很大，怎么办？',
])

// 侧边栏操作
const toggleSidebar = () => {
  conversationStore.toggleSidebar()
}
// 监听对话选择变化
watch(currentConversationId, async (newId) => {
  if (newId) {
    await loadConversationHistory(newId)
  } else {
    messages.value = [] // 清空消息
  }
})

const handleConvAction = (command) => {
  const { action, conversation } = command
  
  switch (action) {
    case 'edit':
      showEditTitleDialog(conversation)
      break
    case 'delete':
      deleteConversation(conversation.id)
      break
  }
}
// 编辑标题相关方法
const showEditTitleDialog = (conversation) => {
  editingConversationId.value = conversation.id
  editingTitle.value = conversation.title || ''
  editTitleDialogVisible.value = true
  // 下一个tick后聚焦输入框
  nextTick(() => {
    const input = document.querySelector('.edit-title-dialog .el-input__inner')
    if (input) {
      input.focus()
      input.select()
    }
  })
}
const updateConversationTitle = async () => {
  if (!editingTitle.value.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }

  try {
    await conversationStore.updateConversationTitle(
      editingConversationId.value, 
      editingTitle.value.trim()
    )
    editTitleDialogVisible.value = false
    ElMessage.success('标题更新成功')
  } catch (error) {
    ElMessage.error('更新标题失败')
  }
}
// 删除对话方法
const deleteConversation = async (conversationId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个对话吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'delete-msg-box', // 添加自定义类名
        confirmButtonClass: 'el-button-yes', // 确认按钮类名
        cancelButtonClass: 'el-button-no' // 取消按钮类名
      }
    )
    
    await conversationStore.deleteConversation(conversationId)
    ElMessage.success('对话已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除对话失败:', error)
      ElMessage.error('删除对话失败')
    }
  }
}

// 加载对话历史
const loadConversationHistory = async (conversationId) => {
  try {
    loading.value = true
    const response = await aiAPI.getConversationMessages(conversationId)
    messages.value = response.data || []
    scrollToBottom()
  } catch (error) {
    console.error('加载对话历史失败:', error)
    console.error('加载对话历史失败')
  } finally {
    loading.value = false
  }
}

// 创建新对话
const createNewConversation = async () => {
  try {
    const newConversation = await conversationStore.createNewConversation()
    messages.value = [] // 清空消息区域
    userInput.value = '' // 清空输入框
    ElMessage.success('已创建新对话')
  } catch (error) {
    ElMessage.error('创建对话失败', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || loading.value) return

  const userMessage = userInput.value.trim()
  userInput.value = ''
  
  // 如果没有当前对话，先创建一个
  let conversationId = currentConversationId.value
  if (!conversationId) {
    try {
      const newConv = await conversationStore.createNewConversation()
      conversationId = newConv.id
      messages.value = [] // 清空可能存在的欢迎消息
    } catch (error) {
      console.error('创建对话失败，无法发送消息')
      return
    }
  }

  // 添加用户消息到本地
  const newUserMessage = {
    role: 'user',
    content: userMessage,
    createdAt: new Date().toISOString()
  }
  messages.value.push(newUserMessage)
  scrollToBottom()

  loading.value = true

  try {
    // 调用后端API
    const response = await aiAPI.chat({
      message: userMessage,
      conversationId: conversationId
    })
    
    const data = response.data
    console.log('收到AI响应:', data)

    // 添加AI回复到本地
    const newAssistantMessage = {
      role: 'assistant',
      content: data.answer,
      createdAt: new Date().toISOString()
    }
    
    messages.value.push(newAssistantMessage)
    
    // 刷新对话列表（更新消息计数和时间）
    await conversationStore.loadConversations()
    
  } catch (error) {
    console.error('AI对话失败:', error)
    
    // 添加错误消息
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我暂时无法回应。请检查网络连接或稍后再试。',
      createdAt: new Date().toISOString()
    })
    console.error('发送消息失败')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 工具函数
const formatDate = (dateStr) => {
  // 如果是UTC时间字符串（包含+00:00或Z），转换为本地时间
  let date
  if (typeof dateStr === 'string') {
    if (dateStr.includes('+00:00') || dateStr.endsWith('Z')) {
      // UTC时间，需要加上8小时（如果是中国时区）
      const utcDate = new Date(dateStr)
      // 转换为本地时间（会自动根据浏览器时区转换）
      date = new Date(utcDate.getTime())
    } else {
      date = new Date(dateStr)
    }
  } else if (typeof dateStr === 'number') {
    // 如果是时间戳
    date = new Date(dateStr)
  } else {
    date = new Date(dateStr)
  }
  
  const now = new Date()
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

const formatTime = (dateStr) => {
  return new Date(dateStr).toLocaleTimeString([], { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 检查输入框是否需要滚动条
const checkTextareaScrollable = () => {
  if (textAreaRef.value) {
    const textarea = textAreaRef.value
    return textarea.scrollHeight > textarea.clientHeight
  }
  return false
}
// 显示/隐藏输入框滚动条
const updateTextareaScrollbar = async () => {
  await nextTick()
  shouldShowScrollbar.value = checkTextareaScrollable()
}
// 自动调整文本域高度
const adjustTextareaHeight = () => {
  if (textAreaRef.value) {
    textAreaRef.value.style.height = 'auto'
    const newHeight = Math.min(textAreaRef.value.scrollHeight, 120)
    textAreaRef.value.style.height = newHeight + 'px'
    
    // 调整高度后检查滚动条
    updateTextareaScrollbar()
  }
}
// 输入处理
const handleInput = () => {
  adjustTextareaHeight()
}

const scrollToBottom = async () => {
  try {
    await nextTick()
    
    // 使用整个页面容器进行滚动
    if (chatContainer.value) {
      console.log('滚动整个页面容器到最底部')
      
      // 使用scrollTo方法确保滚动到底部
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  } catch (error) {
    console.error('滚动失败:', error)
  }
}

// 监听输入变化
watch(userInput, () => {
  nextTick(() => {
    adjustTextareaHeight()
  })
})

// 初始化
onMounted(async () => {
  try {
    await conversationStore.init()
    // 如果已有对话，默认选择最新的一个
    if (sortedConversations.value.length > 0) {
      await conversationStore.selectConversation(sortedConversations.value[0].id)
      await loadConversationHistory(sortedConversations.value[0].id)
    }
  } catch (error) {
    console.error('初始化失败:', error)
  }
})
</script>

<style scoped>
.ai-chat-container {
  min-height: 100vh;
  position: relative;
  /* overflow-y: auto !important; */
}
/* 添加侧边栏样式 */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  background: rgba(255, 255, 255, 0.771);
  transition: transform 0.3s ease;
  z-index: 1001;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: thin;
}
.sidebar-collapsed {
  transform: translateX(-100%);
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* 左右两端对齐 */
  position: relative; /* 为绝对定位的标题提供参考 */
  padding: 24px 16px;
  border-bottom: 1px solid var(--border-light);
}
.sidebar-title {
  position: absolute; /* 绝对定位实现真正居中 */
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}
.sidebar-title h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--text-primary);
}
.actions-container {
  display: flex;
  align-items: center;
  gap: 8px; /* 图标之间的间距 */
  margin-left: 200px;
  margin-top: 2px;
}
.new-chat-icon, .close-icon {
  width: 22px;
  height: 22px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0.7;
}
.new-chat-icon:hover {
  color: var(--accent-color);
  transform: scale(1.1);
  opacity: 1;
}
.close-icon:hover {
  color: var(--accent-color);
  transform: scale(1.1);
  opacity: 1;
}

/* 对话项样式调整 */
.conversation-item {
  position: relative;
  padding: 12px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.2s;
}
.conversation-item.active {
  background: var(--accent-color);
  color: white;
}
.conv-content {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}
.conv-container{
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  padding: 2px 0;
  justify-content: space-between; /* 关键：左右两端对齐 */
  width: 100%; /* 确保容器占满父元素宽度 */
}
.conv-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  /* width: 78%; */
  flex: 1; /* 标题占据剩余空间 */
  margin-right: 8px; /* 添加右边距，与图标保持间距 */
}
/* 确保操作容器不会换行 */
.conv-actions {
  flex-shrink: 0;
  margin-left: 0 !important;
}
/* 详情图标样式 */
.more-icon {
  width: 22px;
  height: 22px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  /* border-radius: 4px; */
  transition: all 0.3s ease;
  opacity: 0;
  flex-shrink: 0; /* 防止图标被压缩 */
}
.conversation-item:hover .more-icon,
.conversation-item.active .more-icon {
  opacity: 1;
  color: white;
}
.more-icon:hover {
  color: var(--accent-color);
  background: rgba(var(--accent-rgb), 0.1);
}

:deep(.el-dropdown-menu) {
  min-width: 120px;
}
.el-dropdown-menu {
  padding: 0;
  /* box-shadow: none !important; */
}
.el-dropdown-menu__item {
  display: flex;
  align-items: center;
}
/* 修改菜单项悬停样式 */
:deep(.el-dropdown-menu__item) {
  font-size: 14px;
  padding: 10px 16px;
  transition: all 0.3s ease;
  background-color: white !important; /* 确保基础背景色 */
}
:deep(.el-dropdown-menu__item:not(.is-disabled):hover),
:deep(.el-dropdown-menu__item:focus),
:deep(.el-dropdown-menu__item.is-focus) {
  background-color: var(--accent-hover) !important;
  color: white !important;
}
/* 移动端适配 */
@media (max-width: 768px) {
  .more-icon {
    opacity: 1; /* 移动端始终显示 */
  }
}

.conv-meta {
  font-size: 0.8rem;
  opacity: 0.7;
  display: flex;
  justify-content: space-between;
}

/* 固定标题栏 */
.user-nav {
  margin: auto 10px auto auto;
}

/* .chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: var(--container-bg);
  backdrop-filter: blur(10px);
}
.header-content {
  margin: 0 auto;
  padding: 10px 20px 10px 20px;
  text-align: center;
}
.title-grid {
  display: grid;
  grid-template-columns: 15% 70% 15%;
}
.arrow-container {
  display: flex;
  align-items: center;
  height: 100%;
  width: 20%;
  margin: auto auto auto 0;
  align-items: center;
  justify-content: center;
}
.title {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
}
.title h2 {
  margin: 5px auto;
  font-size: 1.5rem;
  color: var(--text-primary);
}
.open-icon {
  width: 22px;
  height: 22px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0.7;
}
.open-icon:hover {
  color: var(--accent-color);
  transform: scale(1.1);
  opacity: 1;
}
.open-icon-trans {
  width: 22px;
  height: 22px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0;
}
.edit-title-icon {
  width: 18px;
  height: 18px;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0.7;
  margin-top: 4px;
}
.edit-title-icon:hover {
  color: var(--accent-hover);
  transform: scale(1.1);
  opacity: 1;
} */

.chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: var(--container-bg);
  backdrop-filter: blur(10px);
}

.header-content {
  margin: 0 auto;
  padding: 10px 20px;
}

.title-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

/* 左侧切换按钮容器 */
.sidebar-toggle {
  flex-shrink: 0;
  width: 40px;               /* 固定宽度，保持对称 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-icon {
  color: var(--text-primary);
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.toggle-icon:hover {
  color: var(--accent-color);
  transform: scale(1.1);
  opacity: 1;
}

/* 中间标题区域 */
.title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  max-width: 40%;
  text-align: center;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title h2 {
  display: inline-block;     /* 使编辑图标与标题同行 */
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 编辑标题图标 */
.edit-title-icon {
  margin-left: 8px;
  width: 18px;
  height: 18px;
  cursor: pointer;
  color: var(--text-primary);
  opacity: 0.7;
  transition: all 0.3s ease;
  vertical-align: middle;
}

.edit-title-icon:hover {
  color: var(--accent-hover);
  transform: scale(1.1);
}

.user-nav {
  flex-shrink: 0;
}

/* 响应式调整 */
@media (max-width: 900px) {
  .sidebar {
    position: absolute;
    width: 100%;
    max-width: 300px;
    z-index: 2000;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  }
  
  /* .chat-main.sidebar-expanded {
    margin-left: 0;
  } */
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}
.main-wrapper {
  display: flex;
  justify-content: center;
  min-height: 100vh; /* 确保至少占满整个视口 */
}
.chat-main {
  flex: 1;
  padding: 78px var(--space-lg) 180px;
  margin: 0 270px;
  width: 100%;
  box-sizing: border-box;
  z-index: 0;
  display: flex;
  justify-content: center;
  min-height: 0;
}

.messages-container {
  padding: var(--space-xl);
  /* min-height: 100%; */
  flex: 1 0 auto;
  overflow-y: hidden;
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
  width: 100%; /* 占满父容器宽度 */
  max-width: 100%; /* 确保不超出 */
}

/* 消息样式 */
.message {
  display: flex;
  gap: var(--space-md);
  animation: fadeInUp 0.4s ease-out;
}

.message.user {
  flex-direction: row-reverse;
}

/* .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.message-avatar {
  width: 45px;
  height: 45px;
  border-radius: var(--radius-round);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
  background: var(--accent-color);
  box-shadow: var(--shadow-card);
} 

.user .message-avatar {
  background: var(--accent-color);
}

.assistant .message-avatar {
  background: var(--text-primary);
} */

.message-content {
  background: #ffffff;
  padding: 10px 14px;
  border-radius: var(--radius-medium);
  box-shadow: var(--shadow-card);
  max-width: 70%;
  position: relative;
  transition: transform 0.2s ease;
}

.user .message-content {
  background: var(--accent-color);
  color: white;
}

.assistant .message-content {
  background: var(--section-bg);
  color: var(--text-primary);
}

/* 固定底部输入区域 */
.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: var(--space-lg);
  background: linear-gradient(transparent, var(--primary-bg) 30%);
}

.input-container {
  margin: 0 300px;
  background: var(--container-bg);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-large);
  padding: var(--space-lg);
  box-shadow: 
    0 -4px 20px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.input-wrapper {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
  /* align-items: flex-end; */
  align-items: center;
}

.message-input {
  flex: 1;
  padding: 9px 15px 11px;
  border: 2px solid var(--border-light);
  border-radius: 10px;
  display: flex;
  align-items: center;  /* 垂直居中 */
  resize: none;
  font-size: 1rem;
  line-height: 1.5;
  max-height: 120px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  overflow-y: hidden;
  scrollbar-width: none;
}

.message-input::-webkit-scrollbar {
  display: none;
}

.message-input.show-scrollbar {
  overflow-y: auto;
  scrollbar-width: thin;
  padding-right: 15px;
}

.message-input.show-scrollbar::-webkit-scrollbar {
  display: block;
  width: 6px;
}

.message-input.show-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  margin: 8px 0;
  border-radius: 3px;
}

.message-input.show-scrollbar::-webkit-scrollbar-thumb {
  background: var(--accent-color);
  border-radius: 3px;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.message-input.show-scrollbar:hover::-webkit-scrollbar-thumb {
  opacity: 0.8;
}

.message-input:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(230, 193, 193, 0.2);
  background: white;
}

.send-button {
  background: var(--accent-color);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 100px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.send-button:hover:not(:disabled) {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 快速提示 */
.input-hints {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-sm);
}

.hint {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 3px;
}

.hint-button {
  padding: 6px 14px;
  background: var(--section-bg);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-primary);
}

.hint-button:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  color: white;
  border-color: var(--accent-hover);
}

/* 加载状态 */
.thinking {
  opacity: 0.8;
}

.thinking-dots {
  display: flex;
  gap: 4px;
  padding: var(--space-sm) 0;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-round);
  background: var(--text-secondary);
  animation: bounce 1.4s infinite ease-in-out;
}

.thinking-dots span:nth-child(1) { animation-delay: -0.32s; }
.thinking-dots span:nth-child(2) { animation-delay: -0.16s; }

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: var(--space-md);
  }
  
  .header-content {
    padding: var(--space-md);
  }
  
  .chat-header h1 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 0.9rem;
  }
  
  .chat-main {
    padding: 100px var(--space-md) 160px;
  }
  
  .messages-container {
    padding: var(--space-lg);
    border-radius: var(--radius-medium);
  }
  
  .input-area {
    padding: var(--space-md);
  }
  
  .input-container {
    padding: var(--space-md);
    border-radius: var(--radius-medium);
  }
  
  .input-wrapper {
    flex-direction: column;
    gap: var(--space-sm);
  }
  
  .send-button {
    width: 100%;
    border-radius: 20px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .message-avatar {
    width: 40px;
    height: 40px;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .chat-main {
    padding: 90px var(--space-sm) 150px;
  }
  
  .messages-container {
    padding: var(--space-md);
    gap: var(--space-md);
  }
  
  .message-content {
    max-width: 90%;
    padding: var(--space-md);
  }
  
  .input-hints {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-xs);
  }
  
  .hint {
    margin-bottom: var(--space-xs);
  }
}
</style>