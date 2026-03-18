<template>
  <div class="todo-calendar">
    <div class="calendar-header">
      <el-button link @click="prevMonth">〈</el-button>
      <span class="current-month">{{ currentMonthText }}</span>
      <el-button link @click="nextMonth">〉</el-button>
    </div>
    <div class="calendar-grid">
      <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
      <div
        v-for="day in days"
        :key="day.date"
        class="calendar-day"
        :class="{
          'selected': day.date === selectedDate,
          'other-month': !day.isCurrentMonth,
          'today': isToday(day.date)
        }"
        @click="selectDate(day.date)"
      >
        <span class="day-number">{{ day.dayOfMonth }}</span>
        <div class="task-dots-container">
          <!-- 任务标记点 -->
          <span
            v-for="(dot, index) in day.dots"
            :key="index"
            class="task-dot"
            :class="{
              'pending': !dot.completed,
              'completed': dot.completed
            }"
            :style="{
              backgroundColor: dot.completed ? dot.color : 'white',
              border: `1.5px solid ${dot.color}`
            }"
            :title="dot.title"
          ></span>
          <!-- 超出数量提示 -->
          <span v-if="day.extraCount > 0" class="extra-dots" :title="`还有${day.extraCount}个任务`">
            +{{ day.extraCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useTodoStore } from '@/stores/todo'

const props = defineProps({
  modelValue: {
    type: String,
    default: null
  }
})
const emit = defineEmits(['update:modelValue', 'date-select'])

const todoStore = useTodoStore()

// 当前显示的月份 (0-11)
const currentMonth = ref(new Date().getMonth())
const currentYear = ref(new Date().getFullYear())

// 选中的日期 (YYYY-MM-DD)
const selectedDate = ref(props.modelValue)

// 星期几标题
const weekdays = ['一', '二', '三', '四', '五', '六', '日']

// 最大显示点数
const MAX_DOTS = 3

// 优先级颜色映射
const priorityColors = {
  0: '#c2ecb8',
  1: '#fff495',
  2: '#ffd08a',
  3: '#ff8181'
}

// 判断某天是否是今天
const isToday = (dateStr) => {
  const today = new Date()
  const todayStr = formatDate(today)
  return dateStr === todayStr
}

// 获取当月所有日期信息
const days = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()

  let firstWeekday = firstDay.getDay()
  firstWeekday = firstWeekday === 0 ? 6 : firstWeekday - 1

  const daysArray = []

  // 上月末尾
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = firstWeekday - 1; i >= 0; i--) {
    const date = new Date(year, month - 1, prevMonthLastDay - i)
    daysArray.push(buildDayInfo(date, false))
  }

  // 当月
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d)
    daysArray.push(buildDayInfo(date, true))
  }

  // 下月开头
  const remaining = 42 - daysArray.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    daysArray.push(buildDayInfo(date, false))
  }

  return daysArray
})

// 构建单日信息（包括任务标记点）
function buildDayInfo(date, isCurrentMonth) {
  const dateStr = formatDate(date)
  // 获取该日期的所有任务
  const tasks = todoStore.todoList.filter(todo => todo.dueDate && todo.dueDate.substring(0, 10) === dateStr)

  // 排序规则：待办优先，同状态按优先级降序
  const sorted = [...tasks].sort((a, b) => {
    // 待办（status === 0）排在已完成前面
    const aTodo = a.status === 0
    const bTodo = b.status === 0
    if (aTodo !== bTodo) {
      return aTodo ? -1 : 1
    }
    // 同状态，优先级高的在前（数值大的优先）
    return (b.priority || 0) - (a.priority || 0)
  })

  // 生成标记点（最多 MAX_DOTS 个）
  const dots = []
  const extraCount = Math.max(0, sorted.length - MAX_DOTS)
  const showDots = sorted.slice(0, MAX_DOTS)

  for (const task of showDots) {
    dots.push({
      color: priorityColors[task.priority] || priorityColors[1],
      completed: task.status === 1,
      title: task.title
    })
  }

  return {
    date: dateStr,
    dayOfMonth: date.getDate(),
    isCurrentMonth,
    dots,
    extraCount
  }
}

const currentMonthText = computed(() => {
  const date = new Date(currentYear.value, currentMonth.value)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
})

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value -= 1
  } else {
    currentMonth.value -= 1
  }
}

function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value += 1
  } else {
    currentMonth.value += 1
  }
}

function selectDate(date) {
  selectedDate.value = date
  emit('update:modelValue', date)
  emit('date-select', date)
}

watch(() => props.modelValue, (val) => {
  selectedDate.value = val
})
</script>

<style scoped>
.todo-calendar {
  width: 100%;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  color: #606266;
}

.current-month {
  font-size: 16px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  color: #909399;
  padding: 4px 0;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 14px;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  color: #303133;
  background: white;
  box-sizing: border-box;
}

/* 鼠标悬停 */
.calendar-day:hover {
  background: #f7f7f7;
  transform: scale(1.02);
}

/* 当天日期：下划线标识 */
.calendar-day.today .day-number {
  text-decoration: underline;
  text-underline-offset: 2px;
  text-decoration-color: var(--accent-hover);
  text-decoration-thickness: 2px;
}

/* 选中日期：背景+边框 */
.calendar-day.selected {
  border: 2px solid var(--border-light);
  font-weight: 600;
}

/* 其他月份日期 */
.calendar-day.other-month {
  color: #c0c4cc;
  background: transparent;
}

.day-number {
  margin-bottom: 2px;
}

.task-dots-container {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  justify-content: center;
  align-items: center;
  max-width: 100%;
  height: 12px;
  overflow: hidden;
  /* padding: 0 2px; */
  /* margin-top: 7px; */
}

.task-dot {
  border-radius: 50%;
  display: inline-block;
  box-sizing: border-box;
  transition: transform 0.1s;
  flex-shrink: 0;              /* 防止被压缩 */
}

.task-dot:hover {
  transform: scale(1.1);
  z-index: 1;
}

/* 待办：空心 */
.task-dot.pending {
  width: 8px;
  height: 8px;
}

/* 已完成：实心 */
.task-dot.completed {
  width: 7.2px;
  height: 7.2px;
}

.extra-dots {
  font-size: 8px;
  color: #909399;
  background: #f0f0f0;
  border-radius: 8px;
  padding: 0 3px;
  line-height: 12px;
  height: 12px;
  display: inline-flex;
  align-items: center;
  cursor: default;
  white-space: nowrap;
}
</style>