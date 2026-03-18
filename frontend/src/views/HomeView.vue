<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="home-header">
      <div class="header-left">
        <span class="mini-brand">Mood·心绪</span>
      </div>
      <div class="header-right">
        <UserNav />
      </div>
    </header>

    <!-- 品牌区 -->
    <div class="brand-section">
      <h1 class="brand-name">
        Mood<span class="brand-dot">·</span>
        <span class="brand-sub">心绪</span>
      </h1>
      <p class="slogan">记录情绪，陪伴成长</p>
      <div class="brand-glow"></div>
    </div>

    <!-- 欢迎区（根据登录状态） -->
    <div class="welcome-section" v-if="userStore.isLoggedIn">
      <div class="welcome-card">
        <h2 class="welcome-message">
          欢迎回来，
          <span class="user-nickname">{{ userStore.user?.nickname || userStore.user?.username }}</span>
        </h2>
        <div class="quote-container">
          <span class="quote-icon left">“</span>
          <p class="daily-quote">{{ randomQuote }}</p>
          <span class="quote-icon right">”</span>
        </div>
      </div>
    </div>

    <div class="welcome-section" v-else>
      <div class="welcome-card">
        <h2 class="welcome-message">欢迎来到Mood!</h2>
        <div class="quote-container">
          <span class="quote-icon left">“</span>
          <p class="daily-quote">{{ randomQuote }}</p>
          <span class="quote-icon right">”</span>
        </div>
      </div>
    </div>

    <!-- 功能卡片区 -->
    <div class="feature-grid">
      <router-link to="/diary" class="feature-card">
        <div class="card-icon">📔</div>
        <h3>心情日记</h3>
        <p>记录每日心情，捕捉情绪波动</p>
        <div class="card-shine"></div>
      </router-link>

      <router-link to="/todo" class="feature-card">
        <div class="card-icon">✅</div>
        <h3>待办清单</h3>
        <p>规划任务，有序生活</p>
        <div class="card-shine"></div>
      </router-link>

      <router-link to="/new-ai-chat" class="feature-card">
        <div class="card-icon">💬</div>
        <h3>与小慕聊天</h3>
        <p>和你的 AI 助手聊聊天</p>
        <div class="card-shine"></div>
      </router-link>

      <router-link to="/profile" class="feature-card">
        <div class="card-icon">👤</div>
        <h3>个人主页</h3>
        <p>查看你的成就与数据</p>
        <div class="card-shine"></div>
      </router-link>
    </div>

    <!-- 今日灵感区域 -->
    <div class="inspiration-section">
      <h3 class="section-title">✨ 今日灵感</h3>
      <div class="inspiration-grid">
        <div class="inspiration-card">
          <span class="inspiration-icon">🌿</span>
          <p class="inspiration-text">每天记录一件小确幸</p>
        </div>
        <div class="inspiration-card">
          <span class="inspiration-icon">🧘</span>
          <p class="inspiration-text">深呼吸，放空五分钟</p>
        </div>
        <div class="inspiration-card">
          <span class="inspiration-icon">📝</span>
          <p class="inspiration-text">写下明天的三个目标</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import UserNav from '@/components/UserNav.vue'

const router = useRouter()
const userStore = useUserStore()

const randomQuote = computed(() => {
  const quotes = [
    '今天也要好好照顾自己 🌱',
    '每一个情绪都值得被看见',
    '你的内心，比想象中更强大',
    '小慕随时在这里陪你',
    '记录此刻，珍藏未来',
    '完成一件小事，也是进步',
    '慢慢来，反而快',
    '你比你想象中更接近目标'
  ]
  return quotes[Math.floor(Math.random() * quotes.length)]
})

const goToLogin = () => router.push('/login')
const goToRegister = () => router.push('/register')
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffd6d6 0%, #ebffe6 100%);
  padding: 0 3% 3%;
}

/* 顶部导航栏 */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px 0;
  z-index: 10;
}

.header-left .mini-brand {
  font-size: 1.4rem;
  font-weight: 300;
  color: var(--text-secondary);
  letter-spacing: 1px;
  background: rgba(255, 255, 255, 0.4);
  padding: 4px 16px;
  border-radius: 30px;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 品牌区域 */
.brand-section {
  text-align: center;
  margin-bottom: 10px;
  position: relative;
  z-index: 1;
}

.brand-name {
  font-size: 4rem;
  font-weight: 300;
  color: var(--text-secondary);
  margin-bottom: 8px;
  letter-spacing: 2px;
  position: relative;
  display: inline-block;
}

.brand-dot {
  color: var(--accent-hover);
  font-weight: bold;
  margin: 0 4px;
}

.brand-sub {
  font-size: 3rem;
  font-weight: 300;
  color: var(--text-secondary);
}

.slogan {
  font-size: 1.3rem;
  color: var(--text-secondary);
  opacity: 0.8;
  margin-bottom: 20px;
}

/* 欢迎区域 */
.welcome-section {
  position: relative;
  z-index: 1;
  margin-bottom: 60px;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: 48px;
  padding: 30px 32px;
  margin-top: 40px;
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-message {
  font-size: 2.4rem;
  font-weight: 400;
  color: var(--text-secondary);
  margin-bottom: 16px;
  line-height: 1.3;
}

.user-nickname {
  font-weight: 600;
  color: var(--accent-hover);
  padding-bottom: 2px;
}

.quote-container {
  position: relative;
  max-width: 600px;
  margin: 0 auto;
  padding: 16px 24px;
}

.quote-icon {
  position: absolute;
  font-size: 4rem;
  color: rgba(255, 159, 159, 0.3);
  font-family: serif;
  line-height: 1;
  transition: color 0.3s;
}

.quote-icon.left {
  left: -10px;
  top: -20px;
}

.quote-icon.right {
  right: -10px;
  bottom: -30px;
}

.daily-quote {
  font-size: 1.4rem;
  color: var(--text-secondary);
  font-style: italic;
  margin: 0;
  line-height: 1.6;
  border-radius: 40px;
}

.auth-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 32px;
}

.auth-btn {
  min-width: 140px;
  height: 48px;
  font-size: 1.1rem;
  font-weight: 500;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
}

.auth-btn.login {
  background: #ff9f9f;
  color: white;
}

.auth-btn.login:hover {
  background: #ff8a8a;
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(255, 159, 159, 0.3);
}

.auth-btn.register {
  background: white;
  color: #ff9f9f;
  border: 1px solid #ffc5c5;
}

.auth-btn.register:hover {
  background: #fff5f5;
  transform: translateY(-2px);
  border-color: #ff9f9f;
  box-shadow: 0 12px 24px rgba(255, 159, 159, 0.2);
}

/* 功能卡片 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  margin: 10px 0 50px;
  position: relative;
  z-index: 1;
}

.feature-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 40px;
  padding: 40px 24px 36px;
  text-align: center;
  text-decoration: none;
  color: var(--text-secondary, #4a4a4a);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid rgba(255, 255, 255, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.feature-card:hover::before {
  left: 100%;
}

.feature-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 30px 50px rgba(255, 159, 159, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

.card-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  background: transparent;
  width: 110px;
  height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.4s;
  border: 2px solid transparent;
}

.feature-card:hover .card-icon {
  transform: rotate(8deg) scale(1.05);
  background: #fff3f3;
  border-color: #ffc5c5;
}

.feature-card h3 {
  font-size: 1.7rem;
  font-weight: 500;
  margin: 0 0 12px;
  color: var(--text-secondary);
}

.feature-card p {
  font-size: 1.05rem;
  color: var(--text-secondary);
  opacity: 0.8;
  line-height: 1.6;
  max-width: 220px;
  margin: 0 auto;
}

/* 今日灵感区域 */
.inspiration-section {
  margin: 20px 0 0;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 1.6rem;
  font-weight: 500;
  color: var(--text-secondary, #4a4a4a);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.inspiration-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.inspiration-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(6px);
  border-radius: 30px;
  padding: 28px 20px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease, background 0.3s;
}

.inspiration-card:hover {
  transform: translateY(-6px);
  background: rgba(255, 255, 255, 0.8);
}

.inspiration-icon {
  font-size: 2.8rem;
  margin-bottom: 14px;
  display: block;
}

.inspiration-text {
  font-size: 1.1rem;
  color: var(--text-secondary, #5a5a5a);
  margin: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .home-container {
    padding-left: 4%;
    padding-right: 4%;
  }
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
  .inspiration-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-container {
    padding-left: 20px;
    padding-right: 20px;
  }
  .home-header {
    padding: 4px 0;
  }
  .header-left .mini-brand {
    font-size: 1.2rem;
  }
  .brand-name {
    font-size: 3.2rem;
  }
  .brand-sub {
    font-size: 1.6rem;
  }
  .welcome-message {
    font-size: 2rem;
  }
  .daily-quote {
    font-size: 1.2rem;
  }
  .feature-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .inspiration-grid {
    grid-template-columns: 1fr;
  }
  .auth-buttons {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  .auth-btn {
    width: 100%;
    max-width: 240px;
  }
  .quote-icon {
    font-size: 3rem;
  }
  .floating-dot {
    opacity: 0.3;
  }
}

@media (max-width: 480px) {
  .brand-name {
    font-size: 2.6rem;
  }
  .brand-sub {
    font-size: 1.4rem;
  }
  .slogan {
    font-size: 1rem;
  }
  .welcome-message {
    font-size: 1.6rem;
  }
  .welcome-card {
    padding: 30px 16px;
  }
  .card-icon {
    width: 90px;
    height: 90px;
    font-size: 3rem;
  }
  .feature-card h3 {
    font-size: 1.4rem;
  }
  .feature-card p {
    font-size: 0.95rem;
  }
}
</style>