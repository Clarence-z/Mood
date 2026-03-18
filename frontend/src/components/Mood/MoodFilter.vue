<template>
  <div class="mood-filter">
    <el-form :model="form" label-width="80px">
      <el-form-item label="关键词">
        <el-input v-model="form.keyword" placeholder="搜索描述内容" clearable />
      </el-form-item>
      
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
      </el-form-item>
      
      <el-form-item label="心情范围">
        <div class="value-range">
          <el-input-number 
            v-model="form.minValue" 
            :min="1" 
            :max="5" 
            placeholder="最小值"
            controls-position="right"
          />
          <span class="range-separator">-</span>
          <el-input-number 
            v-model="form.maxValue" 
            :min="1" 
            :max="5" 
            placeholder="最大值"
            controls-position="right"
          />
        </div>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useMoodStore } from '@/stores/mood'

const moodStore = useMoodStore()
const { filters } = storeToRefs(moodStore)

const form = ref({ ...filters.value })
const dateRange = ref([])

const handleDateChange = (range) => {
  if (range && range.length === 2) {
    form.value.startDate = range[0]
    form.value.endDate = range[1]
  } else {
    form.value.startDate = null
    form.value.endDate = null
  }
}

const handleSearch = async () => {
  await moodStore.updateFilters(form.value)
}

const handleReset = async () => {
  form.value = {
    startDate: null,
    endDate: null,
    keyword: '',
    minValue: null,
    maxValue: null
  }
  dateRange.value = []
  await moodStore.resetFilters()
}

// 监听store的filters变化同步到表单
watch(filters, (newFilters) => {
  form.value = { ...newFilters }
  if (newFilters.startDate && newFilters.endDate) {
    dateRange.value = [newFilters.startDate, newFilters.endDate]
  }
}, { deep: true })
</script>

<style scoped>
.mood-filter {
  padding: 16px;
  background: var(--card-bg);
  border-radius: 8px;
}

.value-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-separator {
  margin: 0 8px;
  color: var(--text-secondary);
}
</style>