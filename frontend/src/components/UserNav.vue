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

    <!-- 已登录状态 -->
    <div v-else class="user-info">
      <!-- 导航图标按钮（使用 router-link 包裹） -->
      <div class="nav-icons">
        <router-link to="/" class="nav-link">
          <el-tooltip content="主页" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <HomeFilled />
            </el-icon>
          </el-tooltip>
        </router-link>

        <router-link to="/diary" class="nav-link">
          <el-tooltip content="心情日记" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <Notebook />
            </el-icon>
          </el-tooltip>
        </router-link>

        <router-link to="/todo" class="nav-link">
          <el-tooltip content="ToDo" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <List />
            </el-icon>
          </el-tooltip>
        </router-link>

        <router-link to="/new-ai-chat" class="nav-link">
          <el-tooltip content="AI疗愈" placement="bottom" effect="light">
            <el-icon :size="27" class="nav-icon">
              <ChatLineRound />
            </el-icon>
          </el-tooltip>
        </router-link>
      </div>

      <!-- 用户头像和下拉菜单（仍使用命令处理，右键不支持，但头像本身不常用右键） -->
      <el-dropdown trigger="hover" placement="bottom-end" @command="handleCommand">
        <div class="user-avatar-wrapper">
          <el-avatar :size="36" :src="user.avatar || defaultAvatar" class="user-avatar">
            {{ user.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ user.nickname || user.username }}</span>
          <el-icon style="color: var(--text-secondary)"><ArrowDown /></el-icon>
        </div>

        <template #dropdown>
          <el-dropdown-menu>
            <!-- 将个人主页改为链接，使用 router-link 模拟菜单项样式 -->
            <router-link to="/profile" class="dropdown-item-link">
              <el-dropdown-item>
                <el-icon><User /></el-icon>
                个人主页
              </el-dropdown-item>
            </router-link>
            <!-- 退出登录仍使用命令 -->
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import defaultAvatar from '@/assets/image/default-avatar.png'

import {
  HomeFilled,
  Notebook,
  List,
  ChatLineRound,
  User,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const user = computed(() => userStore.user || {})

// 下拉菜单命令处理（仅保留退出登录）
const handleCommand = async (command) => {
  if (command === 'logout') {
    await handleLogout()
  }
}

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

/* 登录/注册按钮样式（通过 router-link 包裹，实际按钮仍为 el-button） */
/* .nav-button-login,
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
} */

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

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
}

.user-avatar {
  background-color: transparent;
  color: white;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 下拉菜单中的链接项 */
.dropdown-item-link {
  text-decoration: none;
  color: inherit;
  display: block;
}
.dropdown-item-link:hover .el-dropdown-menu__item {
  background-color: var(--accent-hover) !important;
  color: white !important;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: var(--accent-hover) !important;
  color: white !important;
}

@media (max-width: 768px) {
  .user-name {
    display: none;
  }
  .nav-icons {
    gap: 12px;
    padding-right: 12px;
  }
}
</style>