<template>
  <div class="login-view">
    <!-- 背景装饰 -->
    <!-- <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div> -->

    <!-- 主容器 -->
    <div class="login-container">
      <!-- 左侧品牌信息 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">心语空间</h1>
          <p class="brand-subtitle">记录心情 · 关怀自我 · 发现美好</p>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">📝</span>
              <span>心情日记记录</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">✅</span>
              <span>个人任务管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <span>AI智能陪伴</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <!-- 选项卡切换 -->
          <div class="tab-switcher">
            <button 
              class="tab-button" 
              :class="{ active: activeTab === 'login' }"
              @click="activeTab = 'login'"
            >
              登录
            </button>
            <button 
              class="tab-button" 
              :class="{ active: activeTab === 'register' }"
              @click="activeTab = 'register'"
            >
              注册
            </button>
          </div>

          <!-- 表单内容 -->
          <div class="form-content">
            <LoginForm 
              v-if="activeTab === 'login'" 
              @switch-to-register="activeTab = 'register'"
            />
            <RegisterForm 
              v-else 
              @switch-to-login="activeTab = 'login'"
            />
          </div>

          <!-- 第三方登录（可选） -->
          <!-- <div class="social-login">
            <div class="divider">
              <span>或使用以下方式登录</span>
            </div>
            <div class="social-buttons">
              <button class="social-button wechat">
                <span class="social-icon">💬</span>
                微信
              </button>
              <button class="social-button qq">
                <span class="social-icon">🐧</span>
                QQ
              </button>
            </div>
          </div> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginForm from '@/components/LoginForm.vue'
import RegisterForm from '@/components/RegisterForm.vue'

const activeTab = ref('login')
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  background: var(--primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: var(--radius-round);
  opacity: 0.1;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: var(--accent-color);
  top: 10%;
  left: 5%;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: var(--text-primary);
  bottom: 15%;
  right: 10%;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: var(--accent-color);
  top: 60%;
  left: 80%;
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.login-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1000px;
  width: 100%;
  background: var(--container-bg);
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-container);
  overflow: hidden;
  min-height: 600px;
  position: relative;
  z-index: 1;
}

.brand-section {
  background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-hover) 100%);
  padding: var(--space-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-content {
  text-align: center;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: var(--space-sm);
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.brand-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: var(--space-xl);
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 1rem;
  opacity: 0.9;
}

.feature-icon {
  font-size: 1.2rem;
}

.form-section {
  padding: var(--space-xl);
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.tab-switcher {
  display: flex;
  background: var(--section-bg);
  border-radius: var(--radius-medium);
  padding: var(--space-xs);
  margin-bottom: var(--space-xl);
}

.tab-button {
  flex: 1;
  padding: var(--space-md) var(--space-lg);
  border: none;
  background: transparent;
  border-radius: var(--radius-small);
  color: var(--text-secondary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-button.active {
  background: white;
  color: var(--text-primary);
  box-shadow: var(--shadow-small);
}

.form-content {
  margin-bottom: var(--space-xl);
}

.social-login {
  text-align: center;
}

.divider {
  position: relative;
  margin: var(--space-lg) 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-color);
  z-index: -1;
}

.divider span {
  background: var(--container-bg);
  padding: 0 var(--space-md);
}

.social-buttons {
  display: flex;
  gap: var(--space-md);
  justify-content: center;
}

.social-button {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-medium);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.social-button.wechat:hover {
  border-color: #07C160;
  color: #07C160;
}

.social-button.qq:hover {
  border-color: #12B7F5;
  color: #12B7F5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  
  .brand-section {
    padding: var(--space-lg);
    min-height: 200px;
  }
  
  .brand-title {
    font-size: 2rem;
  }
  
  .social-buttons {
    flex-direction: column;
  }
  
  .feature-list {
    display: none;
  }
}
</style>