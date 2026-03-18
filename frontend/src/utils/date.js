// src/utils/date.js
import { ref } from 'vue'

// 时区配置
const TIMEZONE_OFFSET = 8 // 东八区（北京时间）
const isDevelopment = import.meta.env.MODE === 'development'

// 调试模式
const debugMode = ref(false)

/**
 * 开启/关闭调试模式
 */
export const setDateDebug = (enabled) => {
  debugMode.value = enabled
}

/**
 * 将UTC时间转换为东八区时间
 */
export const utcToBeijingTime = (utcString) => {
  if (!utcString) return null
  
  try {
    const date = new Date(utcString)
    // UTC时间转东八区时间（加8小时）
    const beijingTime = new Date(date.getTime() + TIMEZONE_OFFSET * 60 * 60 * 1000)
    
    if (debugMode.value) {
      console.log('时间转换调试:', {
        原始UTC: utcString,
        转换后: beijingTime.toISOString(),
        本地显示: beijingTime.toLocaleString('zh-CN')
      })
    }
    
    return beijingTime
  } catch (error) {
    console.error('时间转换错误:', error)
    return null
  }
}

/**
 * 格式化日期显示（相对时间）
 */
export const formatRelativeTime = (utcString) => {
  if (!utcString) return ''
  
  const beijingTime = utcToBeijingTime(utcString)
  if (!beijingTime) return utcString
  
  const now = new Date()
  const diffMs = now.getTime() - beijingTime.getTime()
  const diffMins = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`
  
  return formatDate(utcString, 'short')
}

/**
 * 格式化日期（多种格式可选）
 */
export const formatDate = (utcString, format = 'short') => {
  if (!utcString) return ''
  
  const beijingTime = utcToBeijingTime(utcString)
  if (!beijingTime) return utcString
  
  const formats = {
    // 简短格式：12-06
    short: beijingTime.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    }),
    
    // 标准格式：2025-12-06
    standard: beijingTime.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-'),
    
    // 完整格式：2025年12月06日
    full: beijingTime.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: '2-digit'
    }),
    
    // 日期时间：12-06 20:57
    datetime: beijingTime.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(/\//g, '-'),
    
    // 聊天时间格式
    chat: (() => {
      const now = new Date()
      const isToday = now.toDateString() === beijingTime.toDateString()
      const isThisYear = now.getFullYear() === beijingTime.getFullYear()
      
      if (isToday) {
        return beijingTime.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        })
      } else if (isThisYear) {
        return beijingTime.toLocaleDateString('zh-CN', {
          month: '2-digit',
          day: '2-digit'
        })
      } else {
        return beijingTime.toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        }).replace(/\//g, '-')
      }
    })()
  }
  
  return formats[format] || formats.standard
}

/**
 * 格式化时间（仅时间部分）
 */
export const formatTime = (utcString) => {
  if (!utcString) return ''
  
  const beijingTime = utcToBeijingTime(utcString)
  if (!beijingTime) return utcString
  
  return beijingTime.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

/**
 * 获取消息时间显示（智能格式）
 */
export const getMessageTime = (utcString) => {
  if (!utcString) return ''
  
  const beijingTime = utcToBeijingTime(utcString)
  if (!beijingTime) return utcString
  
  const now = new Date()
  const diffMs = now.getTime() - beijingTime.getTime()
  const diffMins = Math.floor(diffMs / (1000 * 60))
  
  // 5分钟内显示"刚刚"，今天内显示时间，其他显示日期
  if (diffMins < 5) return '刚刚'
  
  const isToday = now.toDateString() === beijingTime.toDateString()
  if (isToday) {
    return formatTime(utcString)
  }
  
  return formatDate(utcString, 'short')
}

/**
 * 调试函数：显示时间转换详情
 */
export const debugTimeConversion = (utcString, label = '时间') => {
  if (!debugMode.value) return
  
  const beijingTime = utcToBeijingTime(utcString)
  const localTime = new Date(utcString)
  
  console.group(`🕒 ${label} - 时间转换调试`)
  console.log('原始UTC字符串:', utcString)
  console.log('转换为本地时间:', localTime.toString())
  console.log('转换为北京时间:', beijingTime?.toString())
  console.log('相对时间显示:', formatRelativeTime(utcString))
  console.log('标准格式显示:', formatDate(utcString, 'standard'))
  console.log('聊天格式显示:', formatDate(utcString, 'chat'))
  console.groupEnd()
}

// 开发环境默认开启调试
if (isDevelopment) {
  setDateDebug(true)
}

export default {
  utcToBeijingTime,
  formatRelativeTime,
  formatDate,
  formatTime,
  getMessageTime,
  debugTimeConversion,
  setDateDebug
}