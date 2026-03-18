<template>
  <div class="user-nav">
    <!-- 未登录状态：登录/注册按钮（改为链接） -->
    <div v-if="!isAuthenticated" class="guest-nav">
      <router-link to="/login" custom v-slot="{ navigate }">
        <el-button class="el-button-no" @click="navigate">登录</el-button>
      </router-link>
      <router-link to="/register" custom v-slot="{ navigate }">
        <el-button class="el-button-yes" @click="navigate">注册</el-button>
      </router-link>
    </div>

    <!-- 已登录状态：导航图标 + 退出按钮 -->
    <div v-else class="user-info">
      <div class="nav-icons">
        <!-- 主页 -->
        <router-link to="/" class="nav-link">
          <el-tooltip content="主页" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <HomeFilled />
            </el-icon>
          </el-tooltip>
        </router-link>

        <!-- 心情日记 -->
        <router-link to="/diary" class="nav-link">
          <el-tooltip content="心情日记" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <Notebook />
            </el-icon>
          </el-tooltip>
        </router-link>

        <!-- ToDo -->
        <router-link to="/todo" class="nav-link">
          <el-tooltip content="ToDo" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <List />
            </el-icon>
          </el-tooltip>
        </router-link>

        <!-- AI疗愈 -->
        <router-link to="/new-ai-chat" class="nav-link">
          <el-tooltip content="AI疗愈" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <ChatLineRound />
            </el-icon>
          </el-tooltip>
        </router-link>

        <!-- 退出登录（仍为点击事件，不需要右键） -->
        <el-tooltip content="退出登录" placement="bottom" effect="light">
          <el-icon :size="27" class="nav-icon" @click="handleLogout">
            <SwitchButton />
          </el-icon>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

import {
  HomeFilled,
  Notebook,
  List,
  ChatLineRound,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isAuthenticated = computed(() => userStore.isAuthenticated)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button-yes',
      cancelButtonClass: 'el-button-no',
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {}
}
</script>

<style scoped>
.user-nav {
  display: flex;
  align-items: center;
  height: 100%;
}

.guest-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 登录/注册按钮样式 */
.nav-button-login,
.nav-button-register {
  display: inline-block;
}
.nav-button-login .el-button,
.nav-button-register .el-button {
  width: 60px;
  border-radius: 6px;
  font-weight: 500;
}
.nav-button-login .el-button {
  color: var(--text-primary);
  background-color: white;
}
.nav-button-login .el-button:hover {
  color: var(--accent-hover);
  background-color: white;
}
.nav-button-register .el-button {
  color: white;
  background-color: var(--accent-color);
  border-color: transparent;
}
.nav-button-register .el-button:hover {
  background-color: var(--accent-hover);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-icons {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 导航链接样式 */
.nav-link {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.nav-link:hover .nav-icon {
  color: var(--accent-hover) !important;
}

.nav-icon {
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.1s;
  padding: 4px;
  border-radius: 4px;
}

.nav-icon:hover {
  color: var(--accent-hover) !important;
}

@media (max-width: 768px) {
  .nav-icons {
    gap: 12px;
    padding-right: 12px;
  }
}
</style>