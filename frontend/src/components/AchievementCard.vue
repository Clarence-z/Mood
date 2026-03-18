<template>
  <div class="achievement-card" :class="{ locked: !unlocked }">
    <!-- 图标区域：显示图片 -->
    <div class="achievement-icon" :class="{ 'grayscale-icon': !unlocked }">
      <img :src="iconUrl" :alt="achievement.name" @error="handleImageError" />
    </div>
    <div class="achievement-content">
      <h4 class="achievement-name">{{ achievement.name }}</h4>
      <p class="achievement-desc">{{ achievement.description }}</p>
      <div class="achievement-progress" v-if="threshold >= 1 && !unlocked">
        <el-progress
          :percentage="progressPercentage"
          :stroke-width="6"
          :show-text="false"
        />
        <span class="progress-text">
          {{ progress }} / {{ threshold }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  achievement: {
    type: Object,
    required: true
  }
})

const unlocked = computed(() => props.achievement.isAchieved)

const progress = computed(() => props.achievement.currentProgress ?? 0)

const threshold = computed(() => props.achievement.threshold ?? 1)

// 进度百分比
const progressPercentage = computed(() => {
  if (threshold.value <= 0) return 100
  return Math.min(100, (progress.value / threshold.value) * 100)
})

const iconUrl = computed(() => {
  if (!props.achievement.icon) {
    return '/achievements/default_achievement.png'
  }
  return props.achievement.icon
})

// 图标加载失败时的后备处理
const handleImageError = (e) => {
  e.target.src = '/achievements/default_achievement.png' // 默认图片
}
</script>

<style scoped>
.achievement-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(193, 183, 183, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
  overflow: hidden; /* 防止图片溢出 */
}

.achievement-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 保持图标比例 */
}

.grayscale-icon {
  filter: grayscale(1);
  opacity: 0.7;
}

.achievement-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
}

.achievement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.achievement-card.locked {
  opacity: 0.7;
  filter: grayscale(0.3);
}

.achievement-content {
  flex: 1;
  min-width: 0;
}

.achievement-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.achievement-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

.achievement-progress {
  margin-top: 12px;
}

.progress-text {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

:deep(.el-progress .el-progress-bar__inner) {
  background-color: #ffcccc !important;
}

.achievement-status {
  position: absolute;
  top: 12px;
  right: 12px;
}
</style>