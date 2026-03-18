// stores/mood.js - 重构后
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { moodAPI } from '@/api'

export const useMoodStore = defineStore('mood', () => {
  // 状态
  const records = ref([])
  const pagination = ref({
    pageNum: 1,
    pageSize: 7,
    total: 0,
    pages: 0
  })
  
  const filters = ref({
    startDate: null,
    endDate: null,
    keyword: '',
    minValue: null,
    maxValue: null
  })
  
  const loading = ref({
    list: false,
    action: false,
    stats: false
  })
  
  const currentEditId = ref(null)

  // 计算属性
  const averageMood = computed(() => {
    if (records.value.length === 0) return '0.0'
    const sum = records.value.reduce((total, record) => total + record.value, 0)
    return (sum / records.value.length).toFixed(1)
  })
  
  const chartData = computed(() => {
    const maxValue = Math.max(...records.value.map(r => r.value), 1)
    return records.value.slice(0, 7).map(record => ({
      date: new Date(record.createTime).getDate() + '日',
      height: (record.value / maxValue) * 150,
      value: record.value
    }))
  })

  // 动作
  const fetchMoodList = async (extraParams = {}) => {
    loading.value.list = true
    try {
      const params = { 
        page: pagination.value.pageNum,
        size: pagination.value.pageSize,
        ...filters.value, 
        ...extraParams 
      }
      
      const response = await moodAPI.getMoods(params)
      const result = response.data
      
      if (result.code === 200) {
        records.value = result.data.records || []
        pagination.value = {
          pageNum: result.data.current,
          pageSize: result.data.size,
          total: result.data.total,
          pages: result.data.pages
        }
      }
      return result.data
    } catch (error) {
      console.error('获取心情记录失败:', error)
      throw error
    } finally {
      loading.value.list = false
    }
  }

  const fetchTrendData = async (days) => {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - days + 1)

    const params = {
      page: 1,
      size: 15,
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0]
    }

    let allRecords = []
    let page = 1
    let totalPages = 1

    try {
      while (page <= totalPages) {
        params.page = page
        const response = await moodAPI.getMoods(params)
        const result = response.data

        if (result.code === 200) {
          const records = result.data.records || []
          allRecords = allRecords.concat(records)

          totalPages = result.data.pages
          page++

          if (records.length < params.size) {
            break
          }
        } else {
          break
        }
      }

      // 按日期聚合：值数组和计数
      const dateMap = new Map() // key: YYYY-MM-DD, value: { values: [], count: number }
      allRecords.forEach(r => {
        const date = new Date(r.createTime).toISOString().split('T')[0]
        if (!dateMap.has(date)) {
          dateMap.set(date, { values: [], count: 0 })
        }
        const entry = dateMap.get(date)
        entry.values.push(r.value)
        entry.count++
      })

      // 生成趋势数组
      const trend = []
      for (let i = 0; i < days; i++) {
        const currentDate = new Date(startDate)
        currentDate.setDate(startDate.getDate() + i)
        const dateStr = currentDate.toISOString().split('T')[0]
        const entry = dateMap.get(dateStr)

        if (entry) {
          const avg = entry.values.reduce((a, b) => a + b, 0) / entry.count
          trend.push({
            date: dateStr,
            value: Math.round(avg * 10) / 10, // 保留一位小数
            count: entry.count
          })
        } else {
          trend.push({
            date: dateStr,
            value: null,
            count: 0
          })
        }
      }
      return trend
    } catch (error) {
      console.error('获取趋势数据失败:', error)
      throw error
    }
  }
  
  const updateFilters = async (newFilters) => {
    filters.value = { ...filters.value, ...newFilters }
    pagination.value.pageNum = 1 // 重置到第一页
    await fetchMoodList()
  }
  
  const resetFilters = () => {
    filters.value = {
      startDate: null,
      endDate: null,
      keyword: '',
      minValue: null,
      maxValue: null
    }
  }
  
  const changePage = async (pageNum) => {
    pagination.value.pageNum = pageNum
    await fetchMoodList()
  }
  
  // 要不还是定死吧？按照组件对齐
  const changePageSize = async (pageSize) => {
    pagination.value.pageSize = pageSize
    pagination.value.pageNum = 1
    await fetchMoodList()
  }
  
  const addMoodRecord = async (recordData) => {
    loading.value.action = true
    try {
      const response = await moodAPI.addMood(recordData)
      const result = response.data
      
      if (result.code === 200) {
        // 添加后重新加载第一页
        await fetchMoodList()
        return result.data
      }
    } catch (error) {
      console.error('添加心情记录失败:', error)
      throw error
    } finally {
      loading.value.action = false
    }
  }
  
  const deleteMoodRecord = async (id) => {
    try {
      const response = await moodAPI.deleteMood(id)
      const result = response.data
      
      if (result.code === 200) {
        // 删除后重新加载当前页
        await fetchMoodList()
        return true
      }
    } catch (error) {
      console.error('删除心情记录失败:', error)
      throw error
    }
  }
  
  const updateMoodRecord = async (id, updateData) => {
    loading.value.action = true
    try {
      const response = await moodAPI.updateMood(id, updateData)
      const result = response.data
      
      if (result.code === 200) {
        // 更新本地数据
        const index = records.value.findIndex(item => item.id === id)
        if (index > -1) {
          records.value[index] = { ...records.value[index], ...result.data }
        }
        return result.data
      }
    } catch (error) {
      console.error('更新心情记录失败:', error)
      throw error
    } finally {
      loading.value.action = false
    }
  }
  
  const getMoodById = async (id) => {
    try {
      const response = await moodAPI.getMoodById(id)
      const result = response.data
      return result.code === 200 ? result.data : null
    } catch (error) {
      console.error('获取心情详情失败:', error)
      throw error
    }
  }
  
  // 辅助方法
  const setCurrentEdit = (id) => {
    currentEditId.value = id
  }
  
  const clearCurrentEdit = () => {
    currentEditId.value = null
  }
  
  const getMoodByIdLocal = (id) => {
    return records.value.find(item => item.id === id) || null
  }

  return {
    // 状态
    records,
    pagination,
    filters,
    loading,
    currentEditId,
    
    // 计算属性
    averageMood,
    chartData,
    
    // 动作
    fetchMoodList,
    fetchTrendData,
    updateFilters,
    resetFilters,
    changePage,
    changePageSize,
    addMoodRecord,
    deleteMoodRecord,
    updateMoodRecord,
    getMoodById,
    setCurrentEdit,
    clearCurrentEdit,
    getMoodByIdLocal
  }
})