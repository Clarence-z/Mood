<template>
  <div class="mood-form">
    <div class="form-header">
      <h3 v-if="mode === 'add'">记录此刻心情</h3>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="0"
      @submit.prevent="handleSubmit"
    >
      <!-- 顶部：emoji 选择器 -->
      <div class="emoji-selector">
        <div
          v-for="option in moodOptions"
          :key="option.value"
          class="emoji-option"
          :class="{ selected: form.value === option.value }"
          @click="selectValue(option.value)"
        >
          <span class="emoji">{{ option.emoji }}</span>
        </div>
      </div>

      <!-- 中间：描述输入框 -->
      <el-form-item prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="记录一下此刻的心情吧..."
          maxlength="500"
          show-word-limit
          resize="none"
          class="form-input"
        />
      </el-form-item>

      <!-- 底部：按钮区域（靠右） -->
      <div 
        class="form-actions"
        :class="{ 'mode-add': mode === 'add' }"
      >
        <el-button
          v-if="mode === 'edit'"
          class="el-button-no"
          @click="$emit('cancel')"
        >
          取消
        </el-button>
        <el-button
          type="primary"
          class="el-button-yes"
          :class="{ 'add-mode-btn': mode === 'add' }"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ mode === 'add' ? '记录心情' : '保存修改' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useMoodStore } from '@/stores/mood'

const props = defineProps({
  recordId: {
    type: String,
    default: null
  },
  mode: {
    type: String,
    default: 'add', // 'add' 或 'edit'
    validator: (value) => ['add', 'edit'].includes(value)
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['success', 'cancel'])

const moodStore = useMoodStore()
const formRef = ref(null)
const loading = ref(false)

// 心情选项
const moodOptions = [
  { value: 1, emoji: '😭' },
  { value: 2, emoji: '😟' },
  { value: 3, emoji: '😐' },
  { value: 4, emoji: '🙂' },
  { value: 5, emoji: '😄' }
]

// 表单数据
const form = reactive({
  value: null,
  description: ''
})

// 表单验证规则
const rules = {
  value: [{ required: true, message: '请选择心情', trigger: 'change' }],
  description: [
    { required: false },
    { min: 1, max: 500, message: '描述长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 选择心情
const selectValue = (value) => {
  form.value = value
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    if (props.mode === 'add') {
      await moodStore.addMoodRecord({
        value: form.value,
        description: form.description
      })
      ElMessage.success('记录心情成功')
      resetForm()
    } else {
      await moodStore.updateMoodRecord(props.recordId, {
        value: form.value,
        description: form.description
      })
      ElMessage.success('修改成功')
    }

    // 通知父组件刷新数据
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
    if (error.code !== 400) {
      ElMessage.error(props.mode === 'add' ? '记录失败' : '修改失败')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.value = null
  form.description = ''
  formRef.value?.clearValidate()
}

// 加载编辑数据
const loadEditData = () => {
  if (props.initialData && Object.keys(props.initialData).length > 0) {
    form.value = props.initialData.value
    form.description = props.initialData.description
  }
}

onMounted(() => {
  if (props.mode === 'edit') {
    loadEditData()
  }
})

// 监听初始数据变化
watch(
  () => props.initialData,
  (newData) => {
    if (props.mode === 'edit' && newData) {
      loadEditData()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.mood-form {
  border-radius: 12px;
  padding: 14px 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.form-header {
  margin-bottom: 20px;
}

.form-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.emoji-selector {
  display: flex;
  width: 90%;
  gap: 20px;
  justify-content: space-between;
  margin: 0 auto 10px;
}

.emoji-option {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.emoji-option:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 0 2px var(--accent-color);
}

.emoji-option.selected {
  box-shadow: 0 0 0 2px var(--accent-color);
}

.emoji {
  font-size: 28px;
}

.mood-form :deep(.el-form) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0; /* 防止 flex 溢出 */
}

.mood-form :deep(.el-form-item) {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  min-height: 0;
}

.mood-form :deep(.el-textarea) {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.mood-form :deep(.el-textarea textarea) {
  flex: 1;
  height: 100%;
  resize: none;
  font-family: inherit;
  line-height: 1.5;
  overflow-y: auto; /* 内容超出时显示滚动条 */
}

/* 输入框宽度 */
.form-input {
  width: 100%;
  margin: 0 auto;
}

/* ---------- 滚动条样式（参考要求） ---------- */
.mood-form :deep(.el-textarea textarea)::-webkit-scrollbar {
  width: 6px;
}

.mood-form :deep(.el-textarea textarea)::-webkit-scrollbar-track {
  background: var(--bg-secondary, #f8f8f8);
  border-radius: 3px;
}

.mood-form :deep(.el-textarea textarea)::-webkit-scrollbar-thumb {
  background: var(--border-color, #e0e0e0);
  border-radius: 3px;
}

.mood-form :deep(.el-textarea textarea)::-webkit-scrollbar-thumb:hover {
  background: var(--accent-color, #a99292);
}

.emoji-option.selected .label {
  color: var(--accent-color);
  font-weight: 500;
}

/* 表单项 */
:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
  line-height: 1.5;
  resize: none;
}

/* 底部按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.form-actions.mode-add {
  justify-content: center;
}

.add-mode-btn {
  background: var(--primary-bg) !important;
  border: none !important;
  border-radius: 7px !important;
  color: var(--text-primary) !important;
  width: 30%;
  padding: 12px 0 !important;
  transition: all 0.3s ease !important;
}

.add-mode-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15) !important;
}
</style>