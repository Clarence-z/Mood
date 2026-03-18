<template>
  <div class="mood-diary-view">
    <div class="view-wrapper">
      <!-- 页面头部 -->
      <header class="mood-header">
        <div class="header-content">
          <h1>心情日记 🌼</h1>
          <p class="subtitle">今天发生了什么有趣的事？</p>
        </div>
        <div class="header-actions">
          <user-nav />
        </div>
      </header>

      <!-- 主要内容区域 -->
      <main class="mood-main">
        <!-- 左侧栏：日记列表 -->
        <section class="mood-left-panel">
          <!-- 列表顶部：分页信息（筛选器暂时移除） -->
          <!-- <div class="list-header">
            <div v-if="!loading.list && moodStore.records.length > 0" class="pagination-info">
              <span>共 {{ moodStore.pagination.total }} 条记录</span>
              <span>第 {{ moodStore.pagination.pageNum }} 页 / 共 {{ moodStore.pagination.pages }} 页</span>
            </div>
          </div> -->

          <!-- 日记列表区域 -->
          <div class="list-section">
            <mood-list
              @edit="handleEdit"
              @delete="handleDeleteRecord"
              @view="handleViewDetail"
            />
          </div>
        </section>

        <!-- 右侧栏：表单 + 统计 -->
        <aside class="mood-right-panel">
          <!-- 上半部分：记录表单 -->
          <div class="form-section">
            <mood-form
              ref="moodFormRef"
              :mode="formMode"
              :record-id="editingId"
              :initial-data="editingData"
              @success="handleFormSuccess"
            />
          </div>

          <!-- 下半部分：统计图表 -->
          <div class="stats-section">
            <mood-stats ref="moodStatsRef" />
          </div>
        </aside>
      </main>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑心情日记"
      width="500px"
      :before-close="handleEditDialogClose"
    >
      <mood-form
        v-if="showEditDialog"
        :record-id="editingId"
        mode="edit"
        :initial-data="editingData"
        @success="handleEditSuccess"
        @cancel="showEditDialog = false"
      />
    </el-dialog>

    <!-- 详情抽屉（从左侧出现） -->
    <el-drawer
      v-model="showDetailDrawer"
      size="40%"
      direction="rtl"
      :with-header="false"
      destroy-on-close
    >
      <mood-detail
        v-if="selectedRecordId"
        :record-id="selectedRecordId"
        @close="showDetailDrawer = false"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useMoodStore } from '@/stores/mood'

// 组件导入
import UserNav from '@/components/UserNav.vue'
import MoodForm from '@/components/Mood/MoodForm.vue'
import MoodList from '@/components/Mood/MoodList.vue'
import MoodStats from '@/components/Mood/MoodStats.vue'
import MoodDetail from '@/components/Mood/MoodDetail.vue'

const moodStore = useMoodStore()
const { records, pagination, loading } = storeToRefs(moodStore)

// Refs
const moodStatsRef = ref(null)

// 页面状态
const showEditDialog = ref(false)
const showDetailDrawer = ref(false)
const editingId = ref(null)
const selectedRecordId = ref(null)
const editingData = ref({})

// 计算属性
const formMode = computed(() => {
  return editingId.value ? 'edit' : 'add'
})

// 生命周期
onMounted(async () => {
  try {
    await moodStore.fetchMoodList()
  } catch (error) {
    ElMessage.error('加载心情记录失败')
  }
})

// 事件处理函数
const handleEdit = async (id) => {
  try {
    editingId.value = String(id)
    const record = moodStore.getMoodByIdLocal(id)

    if (record) {
      editingData.value = {
        value: record.value,
        description: record.description
      }
      showEditDialog.value = true
    } else {
      const remoteRecord = await moodStore.getMoodById(id)
      if (remoteRecord) {
        editingData.value = {
          value: remoteRecord.value,
          description: remoteRecord.description
        }
        showEditDialog.value = true
      } else {
        ElMessage.error('未找到要编辑的记录')
      }
    }
  } catch (error) {
    console.error('准备编辑失败:', error)
    ElMessage.error('编辑失败')
  }
}

const handleDeleteRecord = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条心情记录吗？此操作不可撤销。', '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button-yes',
      cancelButtonClass: 'el-button-no'
    })

    await moodStore.deleteMoodRecord(id)
    ElMessage.success('删除成功')

    if (selectedRecordId.value === id) {
      showDetailDrawer.value = false
      selectedRecordId.value = null
    }

    if (moodStatsRef.value) {
      moodStatsRef.value.fetchChartData()
    }

    // 如果当前页无数据且不是第一页，跳转到上一页
    if (records.value.length === 0 && pagination.value.pageNum > 1) {
      await moodStore.changePage(pagination.value.pageNum - 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleViewDetail = (id) => {
  selectedRecordId.value = String(id)
  showDetailDrawer.value = true
}

const handleFormSuccess = async () => {
  // 表单提交成功后，刷新列表和统计
  try {
    await moodStore.fetchMoodList()
    if (moodStatsRef.value) {
      moodStatsRef.value.fetchChartData()
    }
  } catch (error) {
    console.error('刷新数据失败:', error)
  }
}

const handleEditSuccess = async () => {
  showEditDialog.value = false
  editingId.value = null
  editingData.value = {}

  try {
    await moodStore.fetchMoodList()
    if (moodStatsRef.value) {
      moodStatsRef.value.fetchChartData()
    }
  } catch (error) {
    console.error('刷新数据失败:', error)
  }
}

const handleEditDialogClose = (done) => {
  editingId.value = null
  editingData.value = {}
  done()
}
</script>

<style scoped>
.mood-diary-view {
  min-height: 100vh;
  background: var(--primary-bg);
  padding: 20px;
}

.view-wrapper {
  background: rgba(255, 255, 255, 0.312);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  backdrop-filter: blur(10px);
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

/* 头部样式 */
.mood-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.85);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.header-content h1 {
  font-size: 2.8rem;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--accent-color);
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.8;
  color: var(--text-secondary);
}

/* 主要内容区域 */
.mood-main {
  display: flex;
  gap: 20px;
  padding: 20px;
  flex: 1;
  min-height: 0; /* 重要：允许内部滚动 */
  overflow: hidden;
}

/* 左侧面板 - 日记列表 */
.mood-left-panel {
  flex: 5;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.list-header {
  padding: 20px 20px 0 20px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.9);
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 13px;
  color: var(--text-secondary);
  border-top: 1px solid var(--border-light);
  margin-top: 12px;
}

.list-section {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  min-height: 0;
}

/* 右侧面板 - 表单 + 统计 */
.mood-right-panel {
  flex: 5;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.form-section,
.stats-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.form-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.stats-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* 确保子组件填满容器 */
:deep(.mood-form),
:deep(.mood-stats) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.mood-form .el-form) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.mood-form .el-form .el-form-item:last-child) {
  margin-top: auto;
  margin-bottom: 0;
}

/* 滚动条样式 */
.list-section::-webkit-scrollbar,
:deep(.mood-list-container) .mood-items::-webkit-scrollbar {
  width: 6px;
}

.list-section::-webkit-scrollbar-track,
:deep(.mood-list-container) .mood-items::-webkit-scrollbar-track {
  background: var(--bg-secondary, #f8f8f8);
  border-radius: 3px;
}

.list-section::-webkit-scrollbar-thumb,
:deep(.mood-list-container) .mood-items::-webkit-scrollbar-thumb {
  background: var(--border-color, #e0e0e0);
  border-radius: 3px;
}

.list-section::-webkit-scrollbar-thumb:hover,
:deep(.mood-list-container) .mood-items::-webkit-scrollbar-thumb:hover {
  background: var(--accent-color, #a99292);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .mood-main {
    gap: 15px;
    padding: 15px;
  }

  .mood-header {
    padding: 20px 30px;
  }

  .header-content h1 {
    font-size: 2.2rem;
  }
}

@media (max-width: 1024px) {
  .mood-main {
    flex-direction: column;
  }

  .mood-left-panel,
  .mood-right-panel {
    flex: none;
    width: 100%;
  }

  .mood-right-panel {
    max-height: 50vh;
  }

  .view-wrapper {
    height: auto;
    min-height: calc(100vh - 40px);
  }
}

@media (max-width: 768px) {
  .mood-diary-view {
    padding: 10px;
  }

  .view-wrapper {
    border-radius: 12px;
  }

  .mood-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
    text-align: center;
  }

  .header-actions {
    width: 100%;
  }

  .mood-main {
    padding: 10px;
  }

  .header-content h1 {
    font-size: 1.8rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .list-header,
  .list-section {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .mood-header {
    padding: 15px;
  }

  .header-content h1 {
    font-size: 1.5rem;
  }

  .pagination-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  background: white;
  backdrop-filter: blur(10px);
}

:deep(.el-dialog__header) {
  padding: 10px 20px 10px;
  border-bottom: 1px solid var(--border-color, #e0e0e0);
  margin-right: 0;
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
  font-weight: 500;
}

:deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--accent-hover);
}

/* 抽屉样式优化（左侧出现） */
:deep(.el-drawer.ltr) {
  border-radius: 0 16px 16px 0;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 20px;
  border-bottom: 1px solid var(--border-color, #e0e0e0);
}

:deep(.el-drawer__body) {
  padding: 0;
}

/* 加载状态优化 */
:deep(.el-skeleton) {
  padding: 20px;
}

:deep(.el-skeleton__item) {
  background: linear-gradient(90deg, var(--bg-secondary, #f8f8f8) 25%, var(--bg-primary, #fff) 37%, var(--bg-secondary, #f8f8f8) 63%);
  background-size: 400% 100%;
  animation: el-skeleton-loading 1.4s ease infinite;
}

@keyframes el-skeleton-loading {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>