<template>
  <div class="mood-item">
    <!-- 一行三列布局：左emoji，中内容（上下两行），右操作按钮 -->
    <div class="mood-row">
      <!-- 左侧：emoji -->
      <span class="mood-emoji">{{ moodEmoji }}</span>

      <!-- 中间：上下两行 -->
      <div class="mood-content">
        <div class="content-top">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(record.createTime) }}</span>
          <el-icon><Clock /></el-icon>
          <span>{{ formatTime(record.createTime) }}</span>
        </div>
        <div class="content-bottom" :title="record.description">
          {{ record.description }}
        </div>
      </div>

      <!-- 右侧：操作按钮 -->
      <div class="actions">
        <el-button type="text" size="small" @click="$emit('edit', record.id)">
          <el-icon><Edit /></el-icon>
        </el-button>
        <el-button type="text" size="small" @click="$emit('delete', record.id)">
          <el-icon><Delete /></el-icon>
        </el-button>
        <el-button type="text" size="small" @click="$emit('view', record.id)">
          查看详情 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Calendar, Clock, Edit, Delete, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

defineEmits(['edit', 'delete', 'view'])

// 根据心情值返回对应 emoji
const moodEmoji = computed(() => {
  const map = {
    1: '😭', // 很差
    2: '😟', // 较差
    3: '😐', // 一般
    4: '🙂', // 较好
    5: '😄'  // 很好
  }
  return map[props.record.value] || '😐'
})

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const recordDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (recordDate.getTime() === today.getTime()) {
    return '今天'
  } else if (recordDate.getTime() === yesterday.getTime()) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 格式化时间
const formatTime = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}
</script>

<style scoped>
.mood-item {
  background: white;
  border-radius: 12px;
  padding: 0 12px 0 18px;
  margin-bottom: 10px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
  height: 74px; /* 固定高度，配合分页填满页面 */
  display: flex;
  align-items: center; /* 垂直居中 */
}

.mood-item:hover {
  border-color: var(--accent-color);
  transform: translateY(-1px);
}

/* 主行：三列布局 */
.mood-row {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 12px;
}

/* 左侧 emoji */
.mood-emoji {
  font-size: 28px;
  line-height: 1;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 中间内容区（上下两行） */
.mood-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  min-width: 0; /* 防止文本溢出 */
}

.content-top {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.content-top .el-icon {
  font-size: 12px;
}

.content-bottom {
  font-size: 16px;
  color: var(--text-primary);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧操作按钮 */
.actions {
  display: flex;
  gap: 3px;
  margin-left: 0;
  align-items: center;
  flex-shrink: 0;
}

.actions .el-button {
  color: var(--text-secondary);
  font-size: 14px;
  padding: 4px;
  margin-left: 0 !important;
  height: auto;
}

.actions .el-button:hover {
  color: var(--accent-color);
}

</style>