<template>
  <div class="mood-stats">
    <div class="stats-header">
      <h3>心情趋势</h3>
      <div class="view-switcher">
        <el-button-group class="stats-switcher">
          <el-button
            :class="{ active: currentView === '7days' }"
            size="small"
            @click="switchView('7days')"
          >
            近7天
          </el-button>
          <el-button
            :class="{ active: currentView === '15days' }"
            size="small"
            @click="switchView('15days')"
          >
            近15天
          </el-button>
        </el-button-group>
      </div>
    </div>

    <div v-loading="loading" class="chart-container">
      <div ref="chartRef" class="chart" :style="{ height: chartHeight }"></div>

      <div v-if="!hasData" class="empty-chart">
        <el-empty description="暂无数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { useMoodStore } from '@/stores/mood'

const moodStore = useMoodStore()

const chartRef = ref(null)
let chartInstance = null
const currentView = ref('7days')
const loading = ref(false)
const chartData = ref([])
const statistics = ref(null)
const chartHeight = '300px'

const hasData = computed(() => {
  return chartData.value.some(item => item.value !== null)
})

const formatDateLabel = (date) => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const dateStr = date.toISOString().split('T')[0]
  const todayStr = today.toISOString().split('T')[0]
  const yesterdayStr = yesterday.toISOString().split('T')[0]

  if (dateStr === todayStr) return '今天'
  if (dateStr === yesterdayStr) return '昨天'
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const switchView = (view) => {
  if (currentView.value !== view) {
    currentView.value = view
    fetchChartData()
  }
}

const fetchChartData = async () => {
  try {
    loading.value = true
    const days = currentView.value === '7days' ? 7 : 15
    const data = await moodStore.fetchTrendData(days)

    chartData.value = data.map(item => ({
      ...item,
      label: formatDateLabel(new Date(item.date))
    }))

    calculateStatistics(data)
    renderChart()
  } catch (error) {
    console.error('获取图表数据失败:', error)
    chartData.value = []
    statistics.value = null
  } finally {
    loading.value = false
  }
}

const calculateStatistics = (data) => {
  const values = data.map(item => item.value).filter(v => v !== null)

  if (values.length === 0) {
    statistics.value = null
    return
  }

  const sum = values.reduce((a, b) => a + b, 0)
  const daysWithRecords = values.length

  statistics.value = {
    average: sum / daysWithRecords,
    max: Math.max(...values),
    min: Math.min(...values),
    daysWithRecords: daysWithRecords,
    totalDays: data.length
  }
}

const renderChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  // 获取 CSS 变量实际值
  const style = getComputedStyle(document.documentElement)
  const accentColor = style.getPropertyValue('--accent-color').trim()
  const textSecondary = style.getPropertyValue('--text-secondary').trim()

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0]
        const rawData = chartData.value[data.dataIndex]
        if (data.value == null || data.value === 0) {
          return `${data.name}<br/>暂无记录`
        }
        const countText = rawData.count > 1 ? ` (共${rawData.count}条记录)` : ''
        return `${data.name}<br/>心情值: ${data.value}分${countText}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '6%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: chartData.value.map(item => item.label),
      axisLine: {
        show: true,
        lineStyle: {
          color: textSecondary,
          width: 1
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: textSecondary,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 5.5,
      interval: 1,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: textSecondary,
        fontSize: 12,
        formatter: (value) => {
          const labels = ['', '很差', '较差', '一般', '较好', '很好']
          return labels[value] || ''
        }
      },
      splitLine: {
        show: false
      }
    },
    series: [
      {
        name: '心情值',
        type: 'line',
        data: chartData.value.map(item => item.value),
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: accentColor,
        },
        lineStyle: {
          color: accentColor,
          width: 1.5
        },
        connectNulls: false,
        smooth: false,
        showSymbol: true,
        markLine: {
          symbol: 'none',
          lineStyle: {
            color: accentColor,
            type: 'dashed',
            width: 1
          },
          label: {
            formatter: '平均: {c}',
            position: 'middle',
            color: textSecondary
          },
          data: statistics.value ? [
            {
              yAxis: statistics.value.average,
              name: '平均值'
            }
          ] : []
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

onMounted(() => {
  fetchChartData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => currentView.value, () => {
  fetchChartData()
})

// 暴露方法给父组件
defineExpose({
  fetchChartData
})
</script>

<style scoped>
.mood-stats {
  border-radius: 12px;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stats-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.stats-switcher .el-button {
  font-size: 12px;
  padding: 4px 12px;
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  transition: all 0.3s;
}

.stats-switcher .el-button:hover {
  color: var(--accent-color);
  border-color: var(--accent-color);
  background-color: rgba(var(--accent-rgb), 0.05);
}

.stats-switcher .el-button.active {
  background-color: var(--accent-color);
  border-color: var(--accent-color);
  color: white;
}

.stats-switcher .el-button:first-child {
  border-radius: 4px 0 0 4px;
}

.stats-switcher .el-button:last-child {
  border-radius: 0 4px 4px 0;
}

.chart-container {
  flex: 1;
  position: relative;
  min-height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

.empty-chart {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}
</style>