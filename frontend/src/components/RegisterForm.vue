<template>
  <form class="register-form" @submit.prevent="handleRegister">
    <div class="form-group">
      <label class="form-label">用户名</label>
      <input 
        v-model="formData.username"
        type="text" 
        class="form-input"
        placeholder="3-50个字符"
        required
        @blur="validateUsername"
      >
      <div v-if="usernameError" class="error-message">{{ usernameError }}</div>
    </div>

    <div class="form-group">
      <label class="form-label">密码</label>
      <input 
        v-model="formData.password"
        type="password" 
        class="form-input"
        placeholder="至少6位字符"
        required
        @blur="validatePassword"
      >
      <div v-if="passwordError" class="error-message">{{ passwordError }}</div>
    </div>

    <div class="form-group">
      <label class="form-label">确认密码</label>
      <input 
        v-model="confirmPassword"
        type="password" 
        class="form-input"
        placeholder="请再次输入密码"
        required
        @blur="validateConfirmPassword"
      >
      <div v-if="confirmPasswordError" class="error-message">{{ confirmPasswordError }}</div>
    </div>

    <div class="form-group">
      <label class="form-label">昵称</label>
      <input 
        v-model="formData.nickname"
        type="text" 
        class="form-input"
        placeholder="最多50个字符"
        required
      >
    </div>

    <div class="form-group">
      <label class="form-label">邮箱</label>
      <input 
        v-model="formData.email"
        type="email" 
        class="form-input"
        placeholder="请输入邮箱地址"
        required
        @blur="validateEmail"
      >
      <div v-if="emailError" class="error-message">{{ emailError }}</div>
    </div>

    <div class="terms-agreement">
      <label class="terms-label">
        <input type="checkbox" v-model="agreedToTerms" required>
        <span>我已阅读并同意</span>
      </label>
      <a href="#" class="terms-link">《用户协议》</a>
      <span>和</span>
      <a href="#" class="terms-link">《隐私政策》</a>
    </div>

    <button 
      type="submit" 
      class="submit-button"
      :disabled="loading || !agreedToTerms"
    >
      <span v-if="loading" class="loading-spinner"></span>
      {{ loading ? '注册中...' : '注册' }}
    </button>

    <div class="switch-prompt">
      已有账号？
      <a href="#" @click.prevent="$emit('switch-to-login')">立即登录</a>
    </div>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const emit = defineEmits(['switch-to-login'])

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const agreedToTerms = ref(false)
const confirmPassword = ref('')

// 表单数据
const formData = ref({
  username: '',
  password: '',
  nickname: '',
  email: ''
})

// 验证错误信息
const usernameError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const emailError = ref('')

// 验证用户名
const validateUsername = () => {
  const username = formData.value.username
  if (username.length < 3) {
    usernameError.value = '用户名至少需要3个字符'
  } else if (username.length > 50) {
    usernameError.value = '用户名不能超过50个字符'
  } else {
    usernameError.value = ''
  }
}

// 验证密码
const validatePassword = () => {
  const password = formData.value.password
  if (password.length < 6) {
    passwordError.value = '密码至少需要6个字符'
  } else {
    passwordError.value = ''
  }
}

// 验证确认密码
const validateConfirmPassword = () => {
  if (confirmPassword.value !== formData.value.password) {
    confirmPasswordError.value = '两次输入的密码不一致'
  } else {
    confirmPasswordError.value = ''
  }
}

// 验证邮箱
const validateEmail = () => {
  const email = formData.value.email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  
  if (!emailRegex.test(email)) {
    emailError.value = '请输入有效的邮箱地址'
  } else {
    emailError.value = ''
  }
}

// 检查表单是否有效
const isFormValid = computed(() => {
  return formData.value.username && 
         formData.value.password && 
         formData.value.nickname && 
         formData.value.email &&
         !usernameError.value &&
         !passwordError.value &&
         !confirmPasswordError.value &&
         !emailError.value &&
         agreedToTerms.value
})

const handleRegister = async () => {
  // 重新验证所有字段
  validateUsername()
  validatePassword()
  validateConfirmPassword()
  validateEmail()

  if (!isFormValid.value) {
    ElMessage.error('请检查表单信息是否正确')
    return
  }

  loading.value = true

  try {
    await userStore.register(formData.value)
    ElMessage.success('注册成功！')
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-form {
  width: 100%;
}

.form-group {
  margin-bottom: var(--space-lg);
  position: relative;
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

.error-message {
  color: #f56565;
  font-size: 0.8rem;
  margin-top: var(--space-xs);
}

.terms-agreement {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  margin-bottom: var(--space-xl);
  font-size: 0.9rem;
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.terms-label {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  cursor: pointer;
}

.terms-link {
  color: var(--accent-color);
  text-decoration: none;
}

.terms-link:hover {
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

/* 响应式调整 */
@media (max-width: 480px) {
  .terms-agreement {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>