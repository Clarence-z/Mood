import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import MoodDiaryView from '../views/MoodDiaryView.vue'
import NewAIChatView from '../views/NewAIChatView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import TodoView from '@/views/TodoView.vue'
import ProfileView from '@/views/ProfileView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/diary',
      name: 'diary',
      component: MoodDiaryView
    },
    {
      path: '/new-ai-chat',
      name: 'new-ai-chat',
      component: NewAIChatView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        title: '登录',
        requiresGuest: true
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        title: '注册',
        requiresGuest: true
      }
    },
    {
      path: '/todo',
      name: 'todo',
      component: TodoView
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: {
        requiresAuth: true
      }
    }
  ]
})

export default router
