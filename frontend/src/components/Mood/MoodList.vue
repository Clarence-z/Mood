<template>
  <div class="mood-list-container">
    <!-- 加载状态 -->
    <div v-if="loading.list" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="records.length === 0" class="empty-state">
      <p>暂无心情记录，开始记录你的第一份心情吧！</p>
    </div>

    <!-- 列表内容 -->
    <div v-else class="mood-list-wrapper">
      <div class="mood-items">
        <mood-item
          v-for="record in records"
          :key="record.id"
          :record="record"
          @edit="handleEdit"
          @delete="handleDelete"
          @view="handleViewDetail"
        />
      </div>

      <!-- 分页组件（固定每页7条） -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          :page-size="7"
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
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useMoodStore } from '@/stores/mood'
import MoodItem from './MoodItem.vue'

const emit = defineEmits(['edit', 'delete', 'view'])

const moodStore = useMoodStore()
const { records, pagination, loading } = storeToRefs(moodStore)

// 每页条数为7
onMounted(() => {
  if (moodStore.pagination.pageSize !== 7) {
    moodStore.changePageSize(7)
  }
})

const handleCurrentChange = async (page) => {
  await moodStore.changePage(page)
}

const handleEdit = (id) => {
  emit('edit', id)
}

const handleDelete = (id) => {
  emit('delete', id)
}

const handleViewDetail = (id) => {
  emit('view', id)
}
</script>

<style scoped>
.mood-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mood-list-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mood-items {
  flex: 1; /* 占满剩余空间，由内部 item 撑开高度 */
}

.pagination-wrapper {
  padding: 12px 0;
  border-top: 1px solid var(--border-color);
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
</style>