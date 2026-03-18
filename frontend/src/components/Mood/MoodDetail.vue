<template>
  <div class="mood-detail">
    <div class="detail-header">
      <h3>心情详情</h3>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="record" class="detail-content">
      <!-- 第一行：心情分数 + 日期时间 -->
      <div class="info-row">
        <div class="value-circle" :class="`mood-${record.value}`">
          <span class="value-number">{{ record.value }}</span>
          <span class="value-label">分</span>
        </div>
        <div class="datetime">
          <div class="time-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatFullDate(record.createTime) }}</span>
          </div>
          <div class="time-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatFullTime(record.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 描述部分（占据剩余空间） -->
      <div class="description-section">
        <h4>心情描述 >></h4>
        <div class="description-content">
          {{ record.description }}
        </div>
      </div>
    </div>

    <div v-else class="empty-container">
      <el-empty description="未找到记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar, Clock } from '@element-plus/icons-vue'
import { useMoodStore } from '@/stores/mood'

const props = defineProps({
  recordId: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['delete', 'close', 'updated'])

const moodStore = useMoodStore()
const record = ref(null)
const loading = ref(true)

// 格式化完整日期
const formatFullDate = (dateString) => {
  const date = new Date(dateString)
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 星期${weekdays[date.getDay()]}`
}

// 格式化完整时间
const formatFullTime = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 加载记录详情
const loadRecord = async () => {
  try {
    loading.value = true
    const data = await moodStore.getMoodById(props.recordId)
    if (data) {
      record.value = data
    } else {
      record.value = null
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
    record.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecord()
})

// 监听recordId变化
watch(() => props.recordId, () => {
  if (props.recordId) {
    loadRecord()
  }
})
</script>

<style scoped>
.mood-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0 20px;
  background: var(--card-bg);
}

.detail-header {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
  flex-shrink: 0;
}

.detail-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.loading-container,
.empty-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  display: flex;
  flex-direction: column;
}

/* 第一行：分数和日期时间 */
.info-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 30px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 12px;
  flex-shrink: 0;
}

.value-circle {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.value-number {
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
}

.value-label {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 2px;
}

.datetime {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.time-item .el-icon {
  color: var(--accent-color);
  font-size: 16px;
}

/* 描述部分 */
.description-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.description-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  flex-shrink: 0;
}

.description-content {
  flex: 1;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  text-indent: 2em;
  line-height: 1.8;
  font-size: 15px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  overflow-y: auto;
  min-height: 0;
}

/* 心情颜色 */
.mood-1 { background: linear-gradient(135deg, #ff6b6b, #ff8e8e); }
.mood-2 { background: linear-gradient(135deg, #ffa726, #ffb74d); }
.mood-3 { background: linear-gradient(135deg, #ffd54f, #ffdf80); }
.mood-4 { background: linear-gradient(135deg, #81c784, #a5d6a7); }
.mood-5 { background: linear-gradient(135deg, #4caf50, #66bb6a); }

/* 滚动条样式 */
.detail-content::-webkit-scrollbar,
.description-content::-webkit-scrollbar {
  width: 6px;
}

.detail-content::-webkit-scrollbar-track,
.description-content::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb,
.description-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb:hover,
.description-content::-webkit-scrollbar-thumb:hover {
  background: var(--accent-color);
}
</style>