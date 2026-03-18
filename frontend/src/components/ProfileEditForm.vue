<template>
  <el-form :model="form" label-width="80px" ref="formRef">
    <el-form-item label="昵称" prop="nickname">
      <el-input v-model="form.nickname" placeholder="请输入您的昵称" />
    </el-form-item>
    <el-form-item label="个人简介" prop="bio">
      <el-input
        v-model="form.bio"
        type="textarea"
        :rows="3"
        placeholder="介绍一下自己吧"
        maxlength="200"
        show-word-limit
      />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email" placeholder="请输入邮箱地址" />
    </el-form-item>
  </el-form>
  <div class="dialog-footer">
    <el-button class="el-button-no" @click="emit('cancel')">取消</el-button>
    <el-button class="el-button-yes" @click="handleSubmit" :loading="submitting">
      保存
    </el-button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['success', 'cancel'])

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const form = ref({
  nickname: '',
  bio: '',
  email: ''
})

// 当传入的用户数据变化时，更新表单
watch(() => props.user, (newUser) => {
  if (newUser) {
    form.value = {
      nickname: newUser.nickname || '',
      bio: newUser.bio || '',
      email: newUser.email || ''
    }
  }
}, { immediate: true })

const handleSubmit = async () => {
  submitting.value = true
  try {
    // 调用 store 中的更新资料方法
    await userStore.updateProfile(form.value)
    ElMessage.success('资料更新成功')
    emit('success')
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>