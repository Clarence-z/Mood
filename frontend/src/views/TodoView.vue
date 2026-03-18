<template>
  <div class="todo-view-container">
    <div class="view-wrapper">
      <!-- 页面头部 -->
      <header class="todo-header">
        <div class="header-content">
          <h1>待办清单 📋</h1>
          <p class="subtitle">做点什么好呢？</p>
        </div>
        <div class="header-actions">
          <user-nav />
        </div>
      </header>

      <!-- 主要内容区域（双栏） -->
      <main class="todo-main">
        <!-- 左侧：列表区域 (70%)，内部再分左右 -->
        <aside class="todo-list-sidebar">
          <!-- 顶部视图切换（跨列） -->
          <div class="list-header">
            <div class="view-tabs">
              <el-button
                v-for="view in viewOptions"
                :key="view.value"
                :class="{ active: activeView === view.value }"
                @click="switchView(view.value)"
                class="view-btn"
              >
                {{ view.label }}
              </el-button>
            </div>
          </div>

          <!-- 左右内容 -->
          <div class="list-content">
            <!-- 左侧：任务列表列 -->
            <div class="task-list-column">
              <div class="task-list-container">
                <!-- <div v-if="loading.list" class="loading-state">
                  <el-skeleton :rows="5" animated />
                </div>
                <div v-else-if="displayedTodos.length === 0" class="empty-state">
                  <p>暂无任务</p>
                </div>
                <div v-else class="task-list">
                  <todo-item
                    v-for="todo in displayedTodos"
                    :key="todo.id"
                    :todo="todo"
                    @toggle-pin="handleTogglePin"
                    @toggle-status="handleToggleStatus"
                    @edit="handleEdit"
                    @delete="handleDelete"
                    @select="handleSelect"
                  />
                </div> -->
                <todo-list
                  @toggle-pin="handleTogglePin"
                  @toggle-status="handleToggleStatus"
                  @edit="handleEdit"
                  @delete="handleDelete"
                  @select="handleSelect"
                />
              </div>
            </div>

            <!-- 右侧：统计列 -->
            <div class="stats-column">
              <todo-stats :view="activeView" :loading="loading.stats" />
            </div>
          </div>
        </aside>

        <!-- 右侧：日历 + 筛选器 (30%) -->
        <section class="todo-right-panel">
          <!-- 上半部分：日历 + 新建 -->
          <div class="calendar-form-section">
            <todo-calendar v-model="selectedDate" @date-select="handleDateSelect" />
            <el-button 
              type="primary" 
              @click="handleCreateNew"
              :loading="loading.action"
              class="create-btn"
            >
              <el-icon><Plus /></el-icon> 新建任务
            </el-button>
          </div>

          <!-- 下半部分：筛选器 -->
          <div class="filter-section">
            <todo-filter
              :filters="filters"
              @filter-change="handleFilterChange"
            />
          </div>
        </section>
      </main>
    </div>

    <!-- 任务详情抽屉 -->
    <el-drawer
      v-model="showDetailDrawer"
      size="40%"
      :with-header="false"
      destroy-on-close
    >
      <todo-detail
        v-if="selectedTodoId"
        :todo-id="selectedTodoId"
        @updated="handleDetailUpdated"
      />
    </el-drawer>

    <!-- 编辑对话框（使用 TodoForm 但放在 dialog 中） -->
    <el-dialog
      v-model="showEditDialog"
      width="500px"
      destroy-on-close
    >
      <todo-form
        v-if="showEditDialog"
        :todo-id="editingTodoId"
        :mode="formMode"
        :initial-date="selectedDate"
        @success="handleEditSuccess"
        @cancel="showEditDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useTodoStore } from '@/stores/todo'

// 导入组件
import UserNav from '@/components/UserNav.vue'
import TodoStats from '@/components/Todo/TodoStats.vue'
import TodoList from '@/components/Todo/TodoList.vue'
import TodoCalendar from '@/components/Todo/TodoCalendar.vue'
import TodoForm from '@/components/Todo/TodoForm.vue'
import TodoFilter from '@/components/Todo/TodoFilter.vue'
import TodoDetail from '@/components/Todo/TodoDetail.vue'

const todoStore = useTodoStore()
const { todoList, loading, filters } = storeToRefs(todoStore)

// 视图状态
const viewOptions = [
  { value: 'today', label: '今日' },
  { value: 'week', label: '本周' },
  { value: 'all', label: '全部' }
]
const activeView = ref('all') // 默认全部

// 日历选中的日期
const selectedDate = ref(null)

// 抽屉/对话框控制
const showDetailDrawer = ref(false)
const showEditDialog = ref(false)
const selectedTodoId = ref(null)
const editingTodoId = ref(null)
const formMode = ref('create')

// 计算当前视图显示的任务列表
const displayedTodos = computed(() => {
  switch (activeView.value) {
    case 'today':
      return todoStore.todayTodos
    case 'week':
      return todoStore.weekTodos
    case 'all':
    default:
      return todoList.value
  }
})

// 初始加载
onMounted(async () => {
  try {
    await Promise.all([
      todoStore.fetchTodoList(),
      todoStore.fetchTodoListByPage(),
      todoStore.fetchStatistics()
    ])
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
})

const formatLocalDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 视图切换
function switchView(view) {
  activeView.value = view

  let newDateFilter = { startDate: null, endDate: null }
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()) // 当天 0 点

  switch (view) {
    case 'today':
      newDateFilter.startDate = formatLocalDateTime(today)
      const todayEnd = new Date(today)
      todayEnd.setDate(todayEnd.getDate() + 1)
      todayEnd.setMilliseconds(todayEnd.getMilliseconds() - 1) // 23:59:59.999
      newDateFilter.endDate = formatLocalDateTime(todayEnd)
      break
    case 'week':
      const dayOfWeek = today.getDay() // 0 = 周日
      const monday = new Date(today)
      monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
      newDateFilter.startDate = formatLocalDateTime(monday)
      const sunday = new Date(monday)
      sunday.setDate(monday.getDate() + 6)
      sunday.setHours(23, 59, 59, 999)
      newDateFilter.endDate = formatLocalDateTime(sunday)
      break
    case 'all':
      // 保持 null
      break
  }

  handleFilterChange(newDateFilter)
}

// 处理日历选择日期
function handleDateSelect(date) {
  selectedDate.value = date
}

// 处理筛选变化
async function handleFilterChange(newFilters) {
  await todoStore.updateFilters(newFilters)
}

// 处理任务操作
async function handleTogglePin(todoId) {
  try {
    await todoStore.togglePin(todoId)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleToggleStatus(todoId) {
  try {
    const todo = todoStore.getTodoByIdLocal(todoId)
    if (!todo) return
    if (todo.status === 1) {
      await todoStore.markAsTodo(todoId)
    } else if (todo.status === 0) {
      await todoStore.markAsDone(todoId)
    }
    // 刷新列表和统计
    await Promise.all([
      todoStore.fetchTodoList(),
      todoStore.fetchStatistics()
    ])
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

function handleEdit(todoId) {
  editingTodoId.value = todoId
  formMode.value = 'edit'
  showEditDialog.value = true
}

async function handleDelete(todoId) {
  try {
    await todoStore.deleteTodo(todoId)
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
}

function handleSelect(todoId) {
  selectedTodoId.value = todoId
  showDetailDrawer.value = true
}

// 编辑成功
async function handleEditSuccess() {
  showEditDialog.value = false
  await Promise.all([
    todoStore.fetchTodoList(),
    todoStore.fetchStatistics()
  ])
  ElMessage.success('更新成功')
}

// 详情更新
async function handleDetailUpdated() {
  await todoStore.fetchTodoList()
  await todoStore.fetchStatistics()
}

// 新建按钮（打开对话框）
function handleCreateNew() {
  editingTodoId.value = null
  formMode.value = 'create'
  showEditDialog.value = true
}
</script>

<style scoped>
.todo-view-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffd6d6 0%, #ebffe6 100%);
  padding: 20px;
  height: 100vh;
}

.view-wrapper {
  background: rgba(255, 255, 255, 0.312);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  backdrop-filter: blur(10px);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 头部样式（原样保留） */
.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  color: rgb(169, 146, 146);
  background: rgba(255, 255, 255, 0.85);
  flex-shrink: 0;
}

.header-content h1 {
  font-size: 2.8rem;
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}


/* 主要内容区域（双栏） */
.todo-main {
  display: flex;
  gap: 20px;
  padding: 16px 20px;
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

/* 左侧列表区域 (70%) */
.todo-list-sidebar {
  flex: 7;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.list-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.view-tabs {
  display: flex;
  gap: 8px;
  background: #fff4f4;
  padding: 4px;
  border-radius: 8px;
  width: fit-content;
}

.view-tabs .el-button {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 6px;
  transition: all 0.2s;
}
.view-tabs .el-button.active {
  background: white;
  color: var(--accent-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 左右内容布局 */
.list-content {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0; /* 重要：让内部可滚动 */
}

.task-list-column {
  flex: 6; /* 60% */
  min-width: 0; /* 防止溢出 */
  display: flex;
  flex-direction: column;
}

.stats-column {
  flex: 4; /* 40% */
  min-width: 0;
}

.task-list-container {
  flex: 1;
  overflow: hidden;
  /* max-height: calc(100vh - 280px); */
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.loading-state,
.empty-state {
  padding: 30px 0;
  text-align: center;
  color: #909399;
}

.todo-right-panel {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  scrollbar-width: none;             /* Firefox */
  -ms-overflow-style: none;          /* IE/Edge */
}

.todo-right-panel::-webkit-scrollbar {
  display: none;                     /* Chrome/Safari/Opera */
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .todo-main {
    flex-direction: column;
  }
  .todo-list-sidebar,
  .todo-right-panel {
    flex: none;
    width: 100%;
  }
  .list-content {
    flex-direction: column;
  }
  .task-list-column,
  .stats-column {
    flex: none;
    width: 100%;
  }
}

.calendar-form-section,
.filter-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  align-items: center;
}

.calendar-form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.create-btn {
  background: var(--primary-bg);
  border: none;
  border-radius: 7px;
  color: var(--text-primary);
  height: 10%;
  width: 50%;
  padding: 12px 24px;
  transition: all 0.3s ease;
}
.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.filter-section {
  flex: 1 1 auto;
}

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__body) {
  padding: 7px;
}

/* 自定义关闭按钮悬停颜色 */
:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--accent-hover) !important;  /* 这里替换为你想要的颜色 */
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .todo-main {
    flex-direction: column;
  }
  .todo-list-sidebar,
  .todo-right-panel {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .todo-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>