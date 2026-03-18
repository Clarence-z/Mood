<template>
  <div class="todo-filter">
    <h3>筛选任务</h3>

    <!-- 关键词搜索 -->
    <div class="filter-group">
      <label>搜索</label>
      <el-input
        v-model="localFilters.keyword"
        placeholder="搜索任务标题或描述..."
        clearable
        @change="emitFilters"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-group">
      <label>状态</label>
      <div class="status-buttons">
        <button
          v-for="status in statusOptions"
          :key="status.value"
          :class="['status-btn', { active: localFilters.status === status.value }]"
          @click="toggleStatus(status.value)"
        >
         {{ status.label }}
        </button>
      </div>
    </div>

    <!-- 优先级筛选 -->
    <div class="filter-group">
      <label>优先级</label>
      <div class="priority-buttons">
        <button
          v-for="priority in priorityOptions"
          :key="priority.value"
          :class="['priority-btn', `priority-${priority.value}`, { active: localFilters.priority === priority.value }]"
          @click="togglePriority(priority.value)"
        >
          {{ priority.label }}
        </button>
      </div>
    </div>

    <!-- 标签筛选 -->
    <div class="filter-group">
      <label>标签</label>
      <el-select
        v-model="localFilters.tags"
        multiple
        filterable
        allow-create
        default-first-option
        placeholder="选择或输入标签"
        clearable
        @change="emitFilters"
        class="custom-select"
        popper-class="custom-select-popper"
      >
        <el-option
          v-for="tag in tagOptions"
          :key="tag"
          :label="tag"
          :value="tag"
        />
      </el-select>
    </div>

    <!-- 日期范围筛选 -->
    <div class="filter-group">
      <label>截止日期范围</label>
      <div class="date-range-wrapper">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
          class="date-picker"
          popper-class="custom-date-popper"
        />
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="filter-actions">
      <el-button class="el-button-yes" @click="emitFilters">
        应用筛选
      </el-button>
      <el-button class="el-button-no" @click="resetFilters">
        重置
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  filters: { type: Object, required: true }
})
const emit = defineEmits(['filter-change'])

// 本地筛选条件（初始化时合并 props，确保 tags 为数组）
const localFilters = ref({
  status: undefined,
  priority: undefined,
  keyword: '',
  tags: [],
  startDate: null,
  endDate: null,
  ...props.filters,
  tags: Array.isArray(props.filters?.tags) ? props.filters.tags : []
})

const dateRange = ref([])

// 状态选项
const statusOptions = [
  { value: 0, label: '待办'},
  { value: 1, label: '已完成'}
]

// 优先级选项
const priorityOptions = [
  { value: 0, label: '低'},
  { value: 1, label: '中'},
  { value: 2, label: '高'},
  { value: 3, label: '紧急'}
]

// 常用标签选项
const tagOptions = ['工作', '学习', '个人', '锻炼']

// 监听 props 变化，更新本地 filters
watch(() => props.filters, (newFilters) => {
  localFilters.value = {
    ...newFilters,
    tags: Array.isArray(newFilters?.tags) ? newFilters.tags : []
  }
  updateDateRange()
}, { deep: true })

// 更新日期范围显示
const updateDateRange = () => {
  const { startDate, endDate } = localFilters.value
  if (startDate && endDate) {
    const start = startDate.split(' ')[0]
    const end = endDate.split(' ')[0]
    dateRange.value = [start, end]
  } else {
    dateRange.value = []
  }
}

// 切换状态
const toggleStatus = (status) => {
  localFilters.value.status = localFilters.value.status === status ? undefined : status
  emitFilters()
}

// 切换优先级
const togglePriority = (priority) => {
  localFilters.value.priority = localFilters.value.priority === priority ? undefined : priority
  emitFilters()
}

// 日期变化处理
const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    // 构造带时间的字符串：开始日期 00:00:00，结束日期 23:59:59
    localFilters.value.startDate = `${dates[0]}T00:00:00`;
    localFilters.value.endDate = `${dates[1]}T23:59:59`;
  } else {
    localFilters.value.startDate = null;
    localFilters.value.endDate = null;
  }
  emitFilters()
}

// 发射筛选条件
const emitFilters = () => {
  emit('filter-change', { ...localFilters.value })
}

// 重置所有筛选
const resetFilters = () => {
  localFilters.value = {
    status: undefined,
    priority: undefined,
    keyword: '',
    tags: [],
    startDate: null,
    endDate: null,
  }
  dateRange.value = []
  emitFilters()
}

// 初始化
updateDateRange()
</script>

<style scoped>
.todo-filter {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.todo-filter h3 {
  margin: 0 0 20px 0;
  color: #606266;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.status-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.status-btn {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
}

.status-btn:hover {
  border-color: var(--accent-color);
  background: #faf5f5;
}

.status-btn.active {
  border-color: var(--accent-color);
  color: var(--text-primary);
}

.priority-buttons {
  display: flex;
  gap: 8px;
}

.priority-btn {
  width: 20%;
  height: 40px;
  border: 2px solid transparent;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.priority-btn:hover {
  transform: scale(1.1);
  border-color: currentColor;
}

.priority-btn.active {
  border-color: currentColor;
  box-shadow: 0 0 0 2px rgba(var(--accent-color-rgb), 0.2);
}

/* 优先级颜色映射（使用给定的 priorityMap） */
.priority-0 {
  color: #6daf5d; /* textColor for low */
  --priority-color: #c2ecb8;
}
.priority-1 {
  color: #d5af00; /* textColor for medium */
  --priority-color: #fff495;
}
.priority-2 {
  color: #da8300; /* textColor for high */
  --priority-color: #ffd08a;
}
.priority-3 {
  color: #b14646; /* textColor for urgent */
  --priority-color: #ff8181;
}

.priority-0:hover,
.priority-0.active {
  border-color: var(--priority-color);
}
.priority-1:hover,
.priority-1.active {
  border-color: var(--priority-color);
}
.priority-2:hover,
.priority-2.active {
  border-color: var(--priority-color);
}
.priority-3:hover,
.priority-3.active {
  border-color: var(--priority-color);
}

/* 标签选择器 */
.tag-select {
  width: 100%;
}

/* 日期范围包装器，与 TodoForm 的 datetime-wrapper 风格一致 */
.date-range-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}

.date-range-wrapper .el-date-editor {
  flex: 1;
}

.date-picker {
  width: 100%;
}

:deep(.el-range-editor.is-active) {
  border-color: var(--accent-color) !important;
  box-shadow: 0 0 0 1px var(--accent-color) !important;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

/* 按钮样式复用 TodoForm 的类 */
.filter-actions .el-button-yes {
  flex: 1;
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s;
}

.filter-actions .el-button-no {
  flex: 1;
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s;
}
</style>