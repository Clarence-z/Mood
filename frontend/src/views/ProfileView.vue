<template>
  <div class="profile-view-container">
    <div class="view-wrapper">
      <!-- 页面头部 -->
      <header class="profile-header">
        <!-- 左侧：用户信息 -->
        <div class="user-info-section">
          <div class="avatar-section">
            <user-avatar 
              v-if="userStore.user"
              :user-id="userStore.user?.id" 
              class="profile-avatar"
            />
          </div>
          
          <div class="user-details">
            <div class="user-nickname-wrapper">
              <h1 class="user-nickname">{{ userStore.user?.nickname || userStore.user?.username || '未设置昵称' }}</h1>
              <el-button 
                type="text" 
                @click="handleEditProfile"
                class="edit-icon-btn"
                :icon="Edit"
              />
            </div>
            
            <p class="user-bio">{{ userBio }}</p>
            
            <div class="join-date">
              <el-icon><Calendar /></el-icon>
              加入时间: {{ formatJoinDate }}
            </div>
          </div>
        </div>

        <!-- 右侧：导航 -->
        <div class="header-nav-section">
          <user-nav />
        </div>
      </header>

      <!-- 主要内容区域 -->
      <main class="profile-main">
        <!-- 成就系统区域（已移除统计卡片） -->
        <div class="achievements-section">
          <div class="section-header">
            <h3 class="section-title">🎖️ 我的成就</h3>
          </div>
          
          <!-- 成就展示区 -->
          <div class="achievements-container">
            <!-- 加载状态 -->
            <div v-if="achievementStore.loading" class="loading-indicator">
              <el-skeleton :rows="3" animated />
            </div>
            
            <!-- 错误提示 -->
            <div v-else-if="achievementStore.error" class="error-message">
              <el-alert :title="achievementStore.error" type="error" show-icon />
            </div>
            
            <!-- 正常展示 -->
            <template v-else>
              <!-- 已获得的成就 -->
              <div v-if="earnedAchievements.length > 0" class="earned-achievements">
                <h4 class="achievements-subtitle">已获得 ({{ earnedCount }}/{{ totalAchievements }})</h4>
                <div class="achievements-grid">
                  <achievement-card
                    v-for="achievement in earnedAchievements"
                    :key="achievement.id"
                    :achievement="achievement"
                  />
                </div>
              </div>
              
              <!-- 未获得的成就 -->
              <div v-if="unearnedAchievements.length > 0" class="unearned-achievements">
                <h4 class="achievements-subtitle">待解锁</h4>
                <div class="achievements-grid">
                  <achievement-card
                    v-for="achievement in unearnedAchievements"
                    :key="achievement.id"
                    :achievement="achievement"
                  />
                </div>
              </div>
            </template>
          </div>
        </div>
      </main>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <profile-edit-form
        v-if="showEditDialog"
        :user="userStore.user"
        @success="handleEditSuccess"
        @cancel="showEditDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAchievementStore } from '@/stores/achievement'
import { ElMessage } from 'element-plus'
import { Calendar, Edit, Lock } from '@element-plus/icons-vue'

import UserAvatar from '@/components/UserAvatar.vue'
import UserNav from '@/components/UserNavOnlyFuncs.vue'
import AchievementCard from '@/components/AchievementCard.vue'
import ProfileEditForm from '@/components/ProfileEditForm.vue'

const userStore = useUserStore()
const achievementStore = useAchievementStore()

// 状态
const showEditDialog = ref(false)

// 计算属性
const userBio = computed(() => userStore.user?.bio || '没有写个人简介哦')

const formatJoinDate = computed(() => {
  if (!userStore.user?.createdAt) return '未知'
  const date = new Date(userStore.user.createdAt)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
})

// 成就相关计算属性（直接从 store 获取）
const earnedAchievements = computed(() => achievementStore.earnedAchievements)
const unearnedAchievements = computed(() => achievementStore.unearnedAchievements)
const earnedCount = computed(() => achievementStore.totalEarned)
const totalAchievements = computed(() => achievementStore.totalCount)

// 生命周期
onMounted(async () => {
  try {
    // 确保用户信息已加载
    if (!userStore.user) {
      await userStore.fetchProfile()
    }
    
    // 加载成就数据
    await achievementStore.fetchAchievements()
  } catch (error) {
    console.error('加载个人主页数据失败:', error)
    ElMessage.error('加载数据失败，请刷新重试')
  }
})

// 方法
const handleEditProfile = () => {
  showEditDialog.value = true
}

const handleEditSuccess = async () => {
  showEditDialog.value = false
  await userStore.fetchProfile()
  // ElMessage.success('个人资料更新成功')
}
</script>

<style scoped>
.profile-view-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffd6d6 0%, #ebffe6 100%);
  padding: 20px;
  height: 100vh;
}

.view-wrapper {
  background: rgba(255, 255, 255, 0.312);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  backdrop-filter: blur(10px);
  height: 100%;
}

/* 头部样式 */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px 40px;
  color: rgb(169, 146, 146);
  background: rgba(255, 255, 255, 0.85);
  border-bottom: 1px solid rgba(169, 146, 146, 0.1);
}

/* 左侧用户信息区域 */
.user-info-section {
  display: flex;
  align-items: flex-start;
  gap: 30px;
  flex: 1;
  min-width: 0;
}

.avatar-section {
  flex-shrink: 0;
}

.profile-avatar {
  width: 120px;
  height: 120px;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-nickname-wrapper {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 12px;
}

.user-nickname {
  font-size: 2.5rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.2;
}

.edit-icon-btn {
  margin: 16px 0 0 0;
  font-size: 1.5rem;
  padding: 4px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.edit-icon-btn:hover {
  color: var(--accent-hover);
  background: transparent;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--accent-hover)
}

.user-bio {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--text-primary);
  margin: 0 0 10px 3px;
  max-width: 85%;
}

.join-date {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.95rem;
  color: var(--text-secondary);
  border-radius: 8px;
  margin-left: 3px;
}

.join-date .el-icon {
  font-size: 1rem;
}

.header-nav-section {
  flex-shrink: 0;
}

.profile-main {
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  height: 78%;
}

/* 成就区域 */
.achievements-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  height: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.achievements-subtitle {
  font-size: 1.2rem;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.placeholder-area {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  border: 2px dashed #dee2e6;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.placeholder-icon {
  font-size: 3rem;
  color: #adb5bd;
}

.placeholder-text {
  font-size: 1.2rem;
  font-weight: 600;
  color: #6c757d;
  margin: 0;
}

.loading-indicator {
  padding: 40px;
}

.error-message {
  margin: 20px 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .achievements-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    gap: 24px;
    padding: 20px;
    align-items: stretch;
  }
  
  .user-info-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .user-nickname-wrapper {
    justify-content: center;
  }
  
  .user-details {
    width: 100%;
  }
  
  .user-nickname {
    font-size: 2rem;
  }
  
  .join-date {
    justify-content: center;
    width: fit-content;
    margin: 0 auto;
  }
  
  .header-nav-section {
    width: 100%;
    display: flex;
    justify-content: center;
  }
  
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>