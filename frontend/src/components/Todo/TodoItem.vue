<template>
  <div 
    :class="[
      'todo-item',
      `priority-${todo.priority}`,
      { 'is-pinned': todo.isPinned, 'is-overdue': isOverdue }
    ]"
    @click="emit('select', todo.id)"
  >
    <!-- 左侧：复选框 -->
    <div class="todo-left">
      <div 
        class="status-checkbox"
        @click.stop="toggleStatus"
        :title="statusConfig.title"
      >
        <span v-if="todo.status === 1" class="checked">✓</span>
        <span v-else class="unchecked"></span>
      </div>
    </div>
    
    <!-- 中间：任务内容 -->
    <div class="todo-content">
      <div class="todo-header">
        <h4 :class="{ 'completed': todo.status === 1 }" class="todo-title">
          {{ todo.title }}
        </h4>
      </div>
      
      <div class="todo-meta">
        <span v-if="todo.dueDate" class="due-date" :class="{ overdue: isOverdue }">
          {{ formatShortDueDate(todo.dueDate) }}
        </span>
      </div>
    </div>
    
    <!-- 右侧：操作按钮 -->
    <div class="todo-actions">
      <button 
        @click.stop="togglePin"
        class="icon-btn"
        :title="todo.isPinned ? '取消置顶' : '置顶'"
      >
        <el-icon>
          <component :is="todo.isPinned ? StarFilled : Star" />
        </el-icon>
      </button>
      <button 
        @click.stop="emit('edit', todo.id)"
        class="icon-btn"
        title="编辑"
      >
        <el-icon><Edit /></el-icon>
      </button>
      <button 
        @click.stop="handleDelete"
        class="icon-btn"
        title="删除"
      >
        <el-icon><Delete /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Star, StarFilled, Edit, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  todo: { type: Object, required: true }
})
const emit = defineEmits(['toggle-pin', 'toggle-status', 'edit', 'delete', 'select'])

const isOverdue = computed(() => {
  if (!props.todo.dueDate || props.todo.status === 1) return false
  return new Date(props.todo.dueDate) < new Date()
})

const statusConfig = computed(() => {
  const configs = {
    0: { title: '标记为Todo' },
    1: { title: '标记为完成' },
  }
  return configs[props.todo.status] || configs[0]
})

function togglePin() {
  emit('toggle-pin', props.todo.id)
}
function toggleStatus() {
  emit('toggle-status', props.todo.id)
}
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除此任务？', '提示', { 
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button-yes',
      cancelButtonClass: 'el-button-no', 
      type: 'warning' })
    emit('delete', props.todo.id)
  } catch {}
}
function formatShortDueDate(dateString) {
  const date = new Date(dateString)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const due = new Date(date)
  due.setHours(0, 0, 0, 0)
  const diffDays = Math.floor((due - today) / (86400000))
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '明天'
  if (diffDays === -1) return '昨天'
  if (diffDays < 0) return `${-diffDays}天前`
  if (diffDays < 7) return `${diffDays}天后`
  return `${date.getMonth()+1}/${date.getDate()}`
}
</script>

<style scoped>
.todo-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  background: white;
  border-radius: 6px;
  margin-bottom: 4px;
  border-left: 4px solid #e6e6e6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.15s;
}
.todo-item:hover {
  background: #f9f9f9;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.priority-0 { border-left-color: #c2ecb8; }
.priority-1 { border-left-color: #fff495; }
.priority-2 { border-left-color: #ffd08a; }
.priority-3 { border-left-color: #ff8181; }

.todo-left {
  margin-right: 8px;
}
.status-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid #c8c8c8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.checked {
  font-size: 12px;
  font-weight: bold;
  color: #c8c8c8;
}

.todo-content {
  flex: 1;
  min-width: 0; /* 防止溢出 */
}
.todo-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}
.todo-title {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: #4f474a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.todo-title.completed {
  text-decoration: line-through;
  color: #909399;
}
.pin-badge {
  font-size: 11px;
  flex-shrink: 0;
}
.todo-meta {
  font-size: 13px;
  color: #909399;
}
.due-date.overdue {
  color: #f56c6c;
  font-weight: 500;
}

.todo-actions {
  display: flex;
  gap: 2px;
  margin-left: 4px;
  flex-shrink: 0;
}
.icon-btn {
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.1s;

  width: 30px;
  height: 30px;
  padding: 10%;
  border: none;
  background: transparent;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.icon-btn .el-icon {
  font-size: 18px;
}
.icon-btn:hover {
  color: var(--accent-hover) !important;
}

</style>