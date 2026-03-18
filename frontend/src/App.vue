<script setup>
// import { RouterLink, RouterView } from 'vue-router'
// import MoodDiary from './views/MoodDiaryView.vue'
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
onMounted(async () => {
  // 如果已有 token 但用户信息为空，则自动加载
  if (userStore.token && !userStore.user) {
    try {
      await userStore.fetchProfile()
    } catch (error) {
      console.error('自动加载用户信息失败', error)
    }
  }
})
</script>

<template>
  <!-- <Home /> -->
  <router-view />
  <!-- <Chat /> -->
</template>

<style scoped>

</style>
