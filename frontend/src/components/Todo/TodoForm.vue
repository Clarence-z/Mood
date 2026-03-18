<template>
  <div class="todo-form">
    <h3>{{ isEdit ? '编辑任务' : '新建任务' }}</h3>
    <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
      <!-- 标题 -->
      <el-form-item label="标题" required>
        <el-input v-model="form.title" placeholder="任务标题" clearable />
      </el-form-item>

      <!-- 描述（textarea） -->
      <el-form-item label="描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="描述（可选）"
        />
      </el-form-item>

      <!-- 截止日期 + 优先级 -->
      <div class="form-row">
        <el-form-item label="截止日期">
          <div class="datetime-wrapper">
            <el-date-picker
              v-model="form.dueDate"
              type="datetime"
              placeholder="选择日期时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              :clearable="true"
              popper-class="custom-date-popper"
            />
            <el-checkbox v-model="isAllDay" label="全天" />
          </div>
        </el-form-item>

        <el-form-item label="优先级">
          <el-select 
            v-model="form.priority" 
            placeholder="优先级" 
            class="custom-select" 
            popper-class="custom-select-popper"
          >
            <el-option label="低" :value="0" />
            <el-option label="中" :value="1" />
            <el-option label="高" :value="2" />
            <el-option label="紧急" :value="3" />
          </el-select>
        </el-form-item>
      </div>

      <!-- 预计耗时 + 标签 -->
      <div class="form-row">
        <el-form-item label="预计耗时(分钟)">
          <el-input-number
            class="estimate-minutes"
            v-model="form.estimatedMinutes"
            :min="0"
            :step="1"
            :precision="0"
            placeholder="请输入整数"
          />
        </el-form-item>

        <el-form-item label="标签" class="tag-form-item">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入标签后回车"
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
        </el-form-item>
      </div>

      <!-- 按钮组 -->
      <div class="form-actions">
        <el-button class="el-button-no" @click="handleCancel">取消</el-button>
        <el-button class="el-button-yes" native-type="submit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTodoStore } from '@/stores/todo'

const props = defineProps({
  todoId: { type: [String, Number], default: null },
  mode: { type: String, default: 'create' },
  initialDate: { type: String, default: null }
})
const emit = defineEmits(['success', 'cancel'])

const todoStore = useTodoStore()
const submitting = ref(false)

// 表单数据（使用 T 格式）
const form = reactive({
  title: '',
  description: '',
  dueDate: props.initialDate ? `${props.initialDate}T00:00:00` : null,
  priority: 1,
  estimatedMinutes: null,
  tags: []
})
const isAllDay = ref(true) // 默认全天

// 全天切换：强制时间部分为 00:00:00
watch(isAllDay, (newVal) => {
  if (newVal && form.dueDate) {
    const datePart = form.dueDate.split('T')[0]
    form.dueDate = `${datePart}T00:00:00`
  }
})

// 监听日期变化：若时间非 00:00:00，则自动取消全天
watch(() => form.dueDate, (newVal) => {
  if (newVal) {
    const timePart = newVal.split('T')[1]
    if (timePart !== '00:00:00') {
      isAllDay.value = false
    }
  }
})

const tagOptions = ['工作', '学习', '个人', '锻炼']
const isEdit = computed(() => props.mode === 'edit' && props.todoId)

// 编辑模式加载数据
watch(
  () => props.todoId,
  async (newId) => {
    if (newId && props.mode === 'edit') {
      const todo = todoStore.getTodoByIdLocal(newId)
      if (todo) {
        Object.assign(form, {
          title: todo.title,
          description: todo.description || '',
          dueDate: todo.dueDate,
          priority: todo.priority,
          estimatedMinutes: todo.estimatedMinutes || null,
          tags: todo.tags || []
        })
      } else {
        try {
          const todo = await todoStore.fetchTodoById(newId)
          Object.assign(form, {
            title: todo.title,
            description: todo.description || '',
            dueDate: todo.dueDate,
            priority: todo.priority,
            estimatedMinutes: todo.estimatedMinutes || null,
            tags: todo.tags || []
          })
        } catch {
          ElMessage.error('加载任务详情失败')
        }
      }
    }
  },
  { immediate: true }
)

// 创建模式：initialDate 变化时更新 dueDate
watch(() => props.initialDate, (newDate) => {
  if (props.mode === 'create') {
    form.dueDate = newDate ? `${newDate}T00:00:00` : null
  }
})

// 提交表单
async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  submitting.value = true
  try {
    // 确保全天任务时间正确（已在 watch 中处理，但以防万一）
    if (isAllDay.value && form.dueDate) {
      const datePart = form.dueDate.split('T')[0]
      form.dueDate = `${datePart}T00:00:00`
    }

    if (isEdit.value) {
      await todoStore.updateTodo(props.todoId, form)
    } else {
      await todoStore.createTodo(form)
    }
    emit('success')
    // 清空表单（创建模式）
    if (props.mode === 'create') {
      Object.assign(form, {
        title: '',
        description: '',
        dueDate: props.initialDate ? `${props.initialDate}T00:00:00` : null,
        priority: 1,
        estimatedMinutes: null,
        tags: []
      })
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.todo-form {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 0 14px;
}

.todo-form h3 {
  margin: 0 0 20px 0;
  color: #606266;
  font-size: 18px;
  font-weight: 600;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.form-row .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.datetime-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.datetime-wrapper .el-date-editor {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* 自定义样式覆盖 Element Plus 默认主题 */
:deep(.el-checkbox__inner:hover) {
  border-width: 1px;
}
:deep(.el-checkbox__inner:hover) {
  border: 1px solid var(--accent-color);
}
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--accent-color);
  border-color: var(--accent-color);
}
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: var(--text-secondary);
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background-color: transparent !important;
}
:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: var(--accent-color) !important;
}
:deep(.estimate-minutes .el-input__inner::placeholder) {
  font-size: 12px;
}

</style>