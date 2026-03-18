<template>
  <div class="todo-list-container">
    <!-- 加载状态 -->
    <div v-if="loading.list" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="records.length === 0" class="empty-state">
      <p>暂无任务</p>
    </div>

    <!-- 列表内容 -->
    <div v-else class="todo-list-wrapper">
      <div class="todo-items">
        <todo-item
          v-for="todo in records"
          :key="todo.id"
          :todo="todo"
          @toggle-pin="handleTogglePin"
          @toggle-status="handleToggleStatus"
          @edit="handleEdit"
          @delete="handleDelete"
          @select="handleSelect"
        />
      </div>

      <!-- 分页组件（固定每页9条） -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          :page-size="8"
          :total="pagination.total"
          layout="total, prev, pager, next, jumper"
          class="custom-pager"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useTodoStore } from '@/stores/todo'
import TodoItem from './TodoItem.vue'

const emit = defineEmits(['toggle-pin', 'toggle-status', 'edit', 'delete', 'select'])

const todoStore = useTodoStore()
// 使用分页专用的 records 和 pagination
const { records, pagination, loading } = storeToRefs(todoStore)

// 每页条数为7
onMounted(() => {
  if (todoStore.pagination.pageSize !== 8) {
    todoStore.changePageSize(8)
  }
})

// 监听分页变化
const handleCurrentChange = async (page) => {
  await todoStore.changePage(page)
}

// 任务操作事件透传
const handleTogglePin = (id) => {
  emit('toggle-pin', id)
}

const handleToggleStatus = (id) => {
  emit('toggle-status', id)
}

const handleEdit = (id) => {
  emit('edit', id)
}

const handleDelete = (id) => {
  emit('delete', id)
}

const handleSelect = (id) => {
  emit('select', id)
}

// 监听 store 中的 filters 变化，自动重新加载分页数据
watch(() => todoStore.filters, async () => {
  await todoStore.fetchTodoListByPage()
}, { deep: true })
</script>

<style scoped>
.todo-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.todo-list-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.todo-items {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  margin-bottom: 8px;
}

.pagination-wrapper {
  padding: 12px 0;
  border-top: 1px solid var(--border-color, #e0e0e0);
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.loading-state,
.empty-state {
  padding: 40px 0;
  text-align: center;
  color: var(--text-secondary);
}

.empty-state p {
  font-size: 14px;
  opacity: 0.8;
}

/* 分页组件样式定制 */
.custom-pager {
  --el-pagination-button-disabled-bg-color: transparent;
  --el-pagination-bg-color: transparent;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-hover-color: var(--accent-color);
}

.custom-pager .el-pager li {
  border-radius: 4px;
  transition: all 0.2s;
}

.custom-pager .el-pager li.is-active {
  background-color: var(--accent-color);
  color: white;
}

.custom-pager .el-pager li:not(.is-active):hover {
  color: var(--accent-color);
}
</style>