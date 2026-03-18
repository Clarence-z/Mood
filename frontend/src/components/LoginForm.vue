<template>
  <form class="login-form" @submit.prevent="handleLogin">
    <div class="form-group">
      <label class="form-label">用户名</label>
      <input 
        v-model="formData.username"
        type="text" 
        class="form-input"
        placeholder="请输入用户名"
        required
      >
    </div>

    <div class="form-group">
      <label class="form-label">密码</label>
      <input 
        v-model="formData.password"
        type="password" 
        class="form-input"
        placeholder="请输入密码"
        required
      >
    </div>

    <div class="form-options">
      <a href="#" class="forgot-password">忘记密码？</a>
    </div>

    <button 
      type="submit" 
      class="submit-button"
      :disabled="loading"
    >
      <span v-if="loading" class="loading-spinner"></span>
      {{ loading ? '登录中...' : '登录' }}
    </button>

    <div class="switch-prompt">
      还没有账号？
      <a href="#" @click.prevent="$emit('switch-to-register')">立即注册</a>
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const emit = defineEmits(['switch-to-register'])

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const rememberMe = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!formData.value.username || !formData.value.password) {
    ElMessage.error('请填写完整的登录信息')
    return
  }

  loading.value = true

  try {
    await userStore.login(formData.value)
    ElMessage.success('登录成功！')
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: var(--space-lg);
}

.form-label {
  display: block;
  margin-bottom: var(--space-sm);
  color: var(--text-primary);
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: var(--space-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-small);
  background: var(--section-bg);
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(var(--accent-color-rgb), 0.1);
}

.form-input::placeholder {
  color: var(--text-secondary);
  opacity: 0.7;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  color: var(--text-secondary);
  cursor: pointer;
}

.remember-me input {
  margin: 0;
}

.forgot-password {
  color: var(--accent-color);
  text-decoration: none;
}

.forgot-password:hover {
  text-decoration: underline;
}

.submit-button {
  width: 100%;
  padding: var(--space-md);
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: var(--radius-medium);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: var(--space-lg);
  position: relative;
}

.submit-button:hover:not(:disabled) {
  background: var(--accent-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: var(--radius-round);
  animation: spin 1s linear infinite;
  margin-right: var(--space-sm);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.switch-prompt {
  text-align: center;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.switch-prompt a {
  color: var(--accent-color);
  text-decoration: none;
  font-weight: 500;
}

.switch-prompt a:hover {
  text-decoration: underline;
}
</style>