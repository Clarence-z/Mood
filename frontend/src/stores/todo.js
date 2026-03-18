import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { todoAPI } from '@/api'

export const useTodoStore = defineStore('todo', () => {
  // 状态
  const todoList = ref([])
  
  const records = ref([])   // 分页查询的当前页数据
  const pagination = ref({
    pageNum: 1,
    pageSize: 9,           // 默认9条，与组件一致
    total: 0,
    pages: 0
  })

  const filters = ref({
    status: undefined,
    priority: undefined,
    tags: [],
    keyword: '',
    startDate: null,
    endDate: null,
  })
  
  const statistics = ref(null)
  
  const loading = ref({
    list: false,
    detail: false,
    action: false,
    stats: false,
    pageList: false,
  })
  
  const currentEditId = ref(null)

  // 计算属性
  const todoByStatus = computed(() => {
    return (status) => todoList.value.filter(item => item.status === status)
  })
  
  const pinnedTodos = computed(() => 
    todoList.value.filter(item => item.isPinned)
  )
  
  const unpinnedTodos = computed(() => 
    todoList.value.filter(item => !item.isPinned)
  )
  
  const overdueTodos = computed(() => 
    todoList.value.filter(item => {
      if (!item.dueDate) return false
      const now = new Date()
      const due = new Date(item.dueDate)
      return due < now && item.status !== 1
    })
  )
  
  const todayTodos = computed(() =>
    todoList.value.filter(item => {
      if (!item.dueDate) return false
      const today = new Date().toISOString().split('T')[0]
      return item.dueDate.startsWith(today)
    })
  )

  const weekTodos = computed(() => {
    const now = new Date()
    const dayOfWeek = now.getDay() // 0 = 周日
    // 计算本周一的日期
    const monday = new Date(now)
    monday.setDate(now.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
    monday.setHours(0, 0, 0, 0)
    // 计算本周日的日期
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    sunday.setHours(23, 59, 59, 999)

    return todoList.value.filter(item => {
      if (!item.dueDate) return false
      const due = new Date(item.dueDate)
      return due >= monday && due <= sunday
    })
  })
  
  const localStatistics = computed(() => {
    const total = todoList.value.length
    const done = todoList.value.filter(item => item.status === 1).length
    const todo = todoList.value.filter(item => item.status === 0).length
    
    return {
      total,
      done,
      todo,
      completionRate: total > 0 ? Math.round((done / total) * 100) : 0,
    }
  })

  // 动作
  // 获取任务列表
  const fetchTodoList = async (extraParams = {}) => {
    loading.value.list = true
    try {
      const params = { ...filters.value, ...extraParams }
      const response = await todoAPI.getList(params)   // response是AxiosResponse
      todoList.value = response.data.data || []        // 提取实际数据
      // return response.data
    } catch (error) {
      console.error('获取任务列表失败:', error)
      throw error
    } finally {
      loading.value.list = false
    }
  }
  
  // 分页查询
  const fetchTodoListByPage = async (extraParams = {}) => {
    loading.value.pageList = true
    try {
      const params = { 
        page: pagination.value.pageNum,
        size: pagination.value.pageSize,
        ...filters.value, 
        ...extraParams 
      }
      
      const response = await todoAPI.getListByPage(params)
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
      console.error('获取分页任务列表失败:', error)
      throw error
    } finally {
      loading.value.pageList = false
    }
  }

  const changePage = async (pageNum) => {
    pagination.value.pageNum = pageNum
    await fetchTodoListByPage()
  }

  const changePageSize = async (pageSize) => {
    pagination.value.pageSize = pageSize
    pagination.value.pageNum = 1
    await fetchTodoListByPage()
  }

  // 更新筛选条件
  const updateFilters = async (newFilters) => {
    filters.value = { ...filters.value, ...newFilters }
    // await fetchTodoList()
    await Promise.all([
      fetchTodoList(),
      fetchTodoListByPage()
    ])
  }
  
  // 重置筛选条件
  const resetFilters = async () => {
    filters.value = {
      status: undefined,
      priority: undefined,
      tags: [],
      keyword: '',
      startDate: null,
      endDate: null,
    }
    pagination.value.pageNum = 1
    // 同时更新普通查询和分页查询
    await Promise.all([
      fetchTodoList(),
      fetchTodoListByPage()
    ])
  }
  
  // 获取单个任务详情
  const fetchTodoById = async (id) => {
    loading.value.detail = true
    try {
      const response = await todoAPI.getById(id)
      const todo = response.data.data                   // 提取实际数据
      const index = todoList.value.findIndex(item => item.id === todo.id)
      if (index > -1) {
        todoList.value[index] = todo
      } else {
        todoList.value.unshift(todo)
      }
      return todo
    } catch (error) {
      console.error('获取任务详情失败:', error)
      throw error
    } finally {
      loading.value.detail = false
    }
  }
  
  // 创建新任务
  const createTodo = async (todoData) => {
    loading.value.action = true
    try {
      const response = await todoAPI.create(todoData)
      const newTodo = response.data.data
      // todoList.value.unshift(newTodo)

      // 同时更新两种列表
      todoList.value.unshift(newTodo)
      // 调整分页总数
      pagination.value.total += 1
      await fetchTodoListByPage()

      await fetchStatistics()
      return newTodo
    } catch (error) {
      console.error('创建任务失败:', error)
      throw error
    } finally {
      loading.value.action = false
    }
  }
  
  // 更新任务
  const updateTodo = async (id, updateData) => {
    loading.value.action = true
    try {
      const response = await todoAPI.update(id, updateData)
      const updatedTodo = response.data.data
      const index = todoList.value.findIndex(item => item.id === id)
      if (index > -1) {
        todoList.value[index] = updatedTodo
      }
    
      // 重新获取分页数据（保持与后端一致）
      await fetchTodoListByPage()

      return updatedTodo
    } catch (error) {
      console.error('更新任务失败:', error)
      throw error
    } finally {
      loading.value.action = false
    }
  }
  
  // 删除任务
  const deleteTodo = async (id) => {
    loading.value.action = true
    try {
      await todoAPI.delete(id)
      // todoList.value = todoList.value.filter(item => item.id !== id)
      // 同时从两个列表中移除
      todoList.value = todoList.value.filter(item => item.id !== id)
      
      // 调整分页总数
      pagination.value.total = Math.max(pagination.value.total - 1, 0)
      
      // 如果当前页为空且不是第一页，跳转到上一页
      const currentPage = pagination.value.pageNum
      const isCurrentPageNowEmpty = records.value.length === 1 // 删除前当前页只有这1条
      if (isCurrentPageNowEmpty && currentPage > 1) {
        pagination.value.pageNum = currentPage - 1
      }
      await fetchTodoListByPage()

      await fetchStatistics()
      return true
    } catch (error) {
      console.error('删除任务失败:', error)
      throw error
    } finally {
      loading.value.action = false
    }
  }
  
  // 快捷状态操作
  const _updateStatus = async (id, action) => {
    try {
      const apiMethod = todoAPI[action]
      if (!apiMethod) throw new Error(`未知的操作: ${action}`)
      
      const response = await apiMethod(id)
      const updatedTodo = response.data.data
      
      const index = todoList.value.findIndex(item => item.id === id)
      if (index > -1) {
        todoList.value[index] = updatedTodo
      }

      // 更新分页列表
      await fetchTodoListByPage()
      
      await fetchStatistics()
      return updatedTodo
    } catch (error) {
      console.log('?')
      console.error('状态更新失败:', error)
      throw error
    }
  }
  
  const markAsTodo = (id) => _updateStatus(id, 'markAsTodo')
  const markAsDone = (id) => _updateStatus(id, 'markAsDone')
  
  // 切换置顶
  const togglePin = async (id) => {
    try {
      const response = await todoAPI.togglePin(id)
      const updatedTodo = response.data.data
      
      const index = todoList.value.findIndex(item => item.id === id)
      if (index > -1) {
        todoList.value[index] = updatedTodo
      }
      
      // 置顶排序
      todoList.value.sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return 0
      })

      // 跳转到第一页
      pagination.value.pageNum = 1
      await fetchTodoListByPage()
      
      // 置顶排序（只对分页列表排序）
      records.value.sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return 0
      })
      
      return updatedTodo
    } catch (error) {
      console.error('切换置顶失败:', error)
      throw error
    }
  }
  
  // 获取统计信息
  const fetchStatistics = async () => {
    loading.value.stats = true
    try {
      const response = await todoAPI.getStatistics()
      statistics.value = response.data.data
      return response.data.data
    } catch (error) {
      console.error('获取统计失败:', error)
      throw error
    } finally {
      loading.value.stats = false
    }
  }
  
  // 辅助方法
  const clearCurrentEdit = () => {
    currentEditId.value = null
  }
  
  const setCurrentEdit = (id) => {
    currentEditId.value = id
  }
  
  const getTodoByIdLocal = (id) => {
    return todoList.value.find(item => item.id === id) || null
  }

  return {
    // 状态
    todoList,
    records,         // 新增：分页查询列表
    pagination,      // 新增：分页信息
    filters,
    statistics,
    loading,
    currentEditId,
    
    // 计算属性
    todoByStatus,
    pinnedTodos,
    unpinnedTodos,
    overdueTodos,
    todayTodos,
    weekTodos,
    localStatistics,
    
    // 动作
    fetchTodoList,
    fetchTodoListByPage,   // 新增：分页查询
    updateFilters,
    resetFilters,
    fetchTodoById,
    createTodo,
    updateTodo,
    deleteTodo,
    markAsTodo,
    markAsDone,
    togglePin,
    fetchStatistics,
    clearCurrentEdit,
    setCurrentEdit,
    getTodoByIdLocal,
    changePage,           // 新增：分页跳转
    changePageSize,       // 新增：修改每页条数
  }
})

// 如果需要持久化，可以在外部配置
// 在组件中使用时：
// import { useTodoStore } from '@/stores/todo-composition'
// import { storeToRefs } from 'pinia'
//
// const todoStore = useTodoStore()
// const { todoList, loading } = storeToRefs(todoStore)
// const { fetchTodoList, createTodo } = todoStore