<template>
  <div class="todo-stats">
    <div v-if="loading" class="stats-loading">
      <el-skeleton :rows="6" animated />
    </div>
    <div v-else class="stats-content">
      <!-- 环形进度图 -->
      <div class="chart-container">
        <el-progress
          type="circle"
          :percentage="completionRate"
          :stroke-width="5"
          color="rgb(243, 203, 203)"
          class="completion-chart"
        >
          <span class="percentage-value">{{ completionRate }}%</span>
        </el-progress>
        <div class="chart-label">完成率</div>
      </div>

      <!-- 关键指标网格 -->
      <div class="stats-grid">
        <div class="grid-item total">
          <div class="item-label">总数</div>
          <div class="item-value">{{ stats.total }}</div>
        </div>
        <div class="grid-item completed">
          <div class="item-label">已完成</div>
          <div class="item-value">{{ stats.completed }}</div>
        </div>
        <div class="grid-item remaining">
          <div class="item-label">剩余</div>
          <div class="item-value">{{ stats.remaining }}</div>
        </div>
        <div class="grid-item overdue">
          <div class="item-label">逾期</div>
          <div class="item-value">{{ stats.overdue }}</div>
        </div>
      </div>

      <!-- 优先级分布（已完成） -->
      <div class="priority-distribution" v-if="stats.total > 0">
        <div class="dist-title">优先级分布</div>
        <div class="priority-bars">
          <div v-for="p in [3,2,1,0]" :key="p" class="priority-row">
            <span class="priority-label" :style="{ color: priorityMap[p].textColor }">
              {{ priorityMap[p].text }}
            </span>
            <div class="bar-container">
              <div 
                class="bar-total"
                :style="{
                    width: `${getPriorityPercent(p)}%`
                  }"
              >
                <div
                  class="bar-completed"
                  :style="{
                    width: `${getPriorityCompletion(p, true)}%`,
                    backgroundColor: priorityMap[p].color
                  }"
                ></div>
              </div>
            </div>
            <span class="priority-count">
              {{ getPriorityCount(p, true) }}/{{ getPriorityCount(p, false) + getPriorityCount(p, true) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTodoStore } from '@/stores/todo'

const props = defineProps({
  view: { type: String, required: true }, // 'today', 'week', 'all'
  loading: { type: Boolean, default: false }
})

const todoStore = useTodoStore()

const priorityMap = [
  { text: '低', color: '#c2ecb8', textColor: '#9ae17b' },
  { text: '中', color: '#fff495', textColor: '#ffcb3c' },
  { text: '高', color: '#ffd08a', textColor: '#f08c00' },
  { text: '紧急', color: '#ff8181', textColor: '#ff8181' }
]

const stats = computed(() => {
  let todos = []
  switch (props.view) {
    case 'today':
      todos = todoStore.todayTodos
      break
    case 'week':
      todos = todoStore.weekTodos
      break
    default:
      todos = todoStore.todoList
  }
  const total = todos.length
  const completed = todos.filter(t => t.status === 1).length
  const remaining = total - completed
  const overdue = todoStore.overdueTodos.filter(t => todos.includes(t)).length
  const mostPriority = Math.max(...todos.map(t => t.priority), 0)
  return { total, completed, remaining, overdue, mostPriority }
})

const completionRate = computed(() => {
  const { total, completed } = stats.value
  return total > 0 ? Math.round((completed / total) * 100) : 0
})


function getPriorityPercent(priority) {
  const total = stats.value.total
  const count = getPriorityCount(priority, true) + getPriorityCount(priority, false)
  return total > 0 ? Math.round((count / total) * 100) : 0
}

function getPriorityCount(priority, completed) {
  let todos = []
  switch (props.view) {
    case 'today':
      todos = todoStore.todayTodos
      break
    case 'week':
      todos = todoStore.weekTodos
      break
    default:
      todos = todoStore.todoList
  }
  return todos.filter(t => t.priority === priority && (completed ? t.status === 1 : t.status !== 1)).length
}

function getPriorityCompletion(priority, completed) {
  const total = getPriorityCount(priority, true) + getPriorityCount(priority, false)
  if (total === 0) return 0
  const count = getPriorityCount(priority, completed)
  return (count / total) * 100
}
</script>

<style scoped>
.todo-stats {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-loading {
  padding: 20px 0;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-container {
  text-align: center;
}

.chart-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.completion-chart {
  width: 70%;
  margin: 0 auto;
  align-items: center;
}

:deep(.el-progress-circle) {
  margin: 0 auto !important;
}

.percentage-value {
  font-size: 14px;
  font-weight: 600;
  color: #6a5d5d;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.grid-item {
  background: #f7f9fa;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
}

.grid-item .item-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.grid-item .item-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.grid-item.total .item-value { color: var(--accent-hover); }
.grid-item.completed .item-value { color: #b3e2a6; }
.grid-item.remaining .item-value { color: #ffc268; }
.grid-item.overdue .item-value { color: #f56c6c; }

.priority-distribution {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.dist-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.priority-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.priority-label {
  width: 18%;
  font-size: 12px;
  font-weight: 500;
  /* text-shadow: #b6b6b6 0.4px 0.4px; */
  text-align: center;
}

.bar-container {
  flex: 1;
}

.bar-total {
  height: 8px;
  background: #efefef;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
}

.bar-completed {
  height: 100%;
  transition: width 0.3s;
}

/* .bar-pending {
  height: 100%;
  transition: width 0.3s;
} */

.priority-count {
  width: 12%;
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
}
</style>