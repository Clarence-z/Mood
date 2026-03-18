<template>
  <div class="todo-detail">
    <div v-if="loading" class="detail-loading">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="todo" class="detail-content">
      <!-- 标题 -->
      <h2 class="detail-title">{{ todo.title }}</h2>

      <!-- 状态和优先级行 -->
      <div class="detail-row status-row">
        <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
        <el-tag :color="priorityColor" size="large" effect="plain" class="priority-tag" :style="{ color: priorityTextColor }">
          {{ priorityText }}
        </el-tag>
        <el-tag v-if="todo.isPinned" type="warning" size="large" effect="light" class="pinned-tag">
          置顶
        </el-tag>
      </div>

      <!-- 标签 -->
      <div v-if="todo.tags && todo.tags.length" class="detail-row">
        <div class="tag-list">
          <span
            v-for="tag in todo.tags"
            :key="tag"
            class="custom-tag"
          >
            ·  {{ tag }}
          </span>
        </div>
      </div>

      <!-- 截止日期 -->
      <div class="detail-row" v-if="todo.dueDate">
        <span class="label">截止日期：</span>
        <span :class="{ 'overdue': isOverdue }">
          {{ formatDate(todo.dueDate) }}
          <span v-if="isOverdue" class="overdue-warning">（已逾期）</span>
        </span>
      </div>

      <!-- 描述 -->
      <div class="detail-row description" v-if="todo.description">
        <span class="label">描述：</span>
        <div class="description-text">{{ todo.description }}</div>
      </div>

      <!-- 创建/更新时间 + 完成时间（如果已完成） -->
      <div class="detail-footer">
        <div v-if="todo.createdAt" class="meta">创建于：{{ formatDateTime(todo.createdAt) }}</div>
        <div v-if="todo.updatedAt && todo.updatedAt !== todo.createdAt" class="meta">
          更新于：{{ formatDateTime(todo.updatedAt) }}
        </div>
        <!-- 完成时间：仅当状态为已完成时显示 -->
        <div v-if="todo.status === 1" class="meta completed-time">
          完成于：{{ formatDateTime(todo.completedAt || todo.updatedAt) }}
        </div>
      </div>
    </div>
    <div v-else class="detail-error">任务不存在或已被删除</div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTodoStore } from '@/stores/todo'

const props = defineProps({
  todoId: {
    type: [String, Number],
    required: true
  }
})
const emit = defineEmits(['close', 'edit', 'updated'])

const todoStore = useTodoStore()
const loading = ref(false)
const todo = ref(null)

// 状态映射
const statusMap = {
  0: { text: '待办', type: 'warning' },
  1: { text: '已完成', type: 'success' }
}
const statusText = computed(() => statusMap[todo.value?.status]?.text || '未知')
const statusType = computed(() => statusMap[todo.value?.status]?.type || 'info')

// 优先级映射
const priorityMap = {
  0: { text: '低', color: '#c2ecb8', textColor: '#6daf5d' },
  1: { text: '中', color: '#fff495', textColor: '#d5af00' },
  2: { text: '高', color: '#ffd08a', textColor: '#da8300' },
  3: { text: '紧急', color: '#ff8181', textColor: '#b14646' }
}
const priorityText = computed(() => priorityMap[todo.value?.priority]?.text || '未知')
const priorityColor = computed(() => priorityMap[todo.value?.priority]?.color || '#909399')
const priorityTextColor = computed(() => priorityMap[todo.value?.priority]?.textColor || '#c8c8c8')

// 是否逾期
const isOverdue = computed(() => {
  if (!todo.value?.dueDate || todo.value.status === 1) return false
  return new Date(todo.value.dueDate) < new Date()
})

// 加载详情
async function loadDetail() {
  if (!props.todoId) return
  loading.value = true
  try {
    const data = await todoStore.fetchTodoById(props.todoId)
    todo.value = data
  } catch (error) {
    ElMessage.error('加载详情失败')
    todo.value = null
  } finally {
    loading.value = false
  }
}

defineExpose({ refresh: loadDetail })

watch(() => props.todoId, () => {
  loadDetail()
}, { immediate: true })  // immediate 确保初次加载

// 格式化日期
function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
function formatDateTime(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.todo-detail {
  padding: 16px 16px 0;
  backdrop-filter: blur(10px);
  height: 100%;
  overflow-y: auto;
}
.detail-loading,
.detail-error {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.detail-title {
  font-size: 27px;
  color: #303133;
}
.detail-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  /* gap: 16px; */
  line-height: 1.6;
}
.status-row {
  gap: 10px;
}
:deep(.el-tag) {
  font-size: 16px;       /* 调整字体大小 */
  height: 40px;
  padding: 0 16px;       /* 调整内边距（可选） */
  line-height: 38px;     /* 配合高度调整行高（可选） */
}
.priority-tag {
  border: none;
}
.pinned-tag {
  margin-left: auto;
}
.label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
  min-width: 70px;
}
.description {
  flex-direction: column;
  align-items: flex-start;
}
.description-text {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  width: 100%;
  white-space: pre-wrap;
  word-break: break-word;
  color: #303133;
  max-height: 200px;
  overflow-y: auto;
}

/* 标签列表样式（参考 hint-button） */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #606266;
}

.overdue {
  color: #f56c6c;
  font-weight: 500;
}
.overdue-warning {
  color: #f56c6c;
}
.detail-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #909399;
}
.meta {
  margin-bottom: 4px;
}
.completed-time {
  color: #67C23A;
  font-weight: 500;
}
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  margin-bottom: 16px;
}
</style>