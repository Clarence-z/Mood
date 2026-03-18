<template>
  <div class="avatar-upload-container">
    <!-- 当前头像展示，支持键盘操作 -->
    <div
      class="current-avatar"
      @click="triggerFileInput"
      @keydown.enter.prevent="triggerFileInput"
      role="button"
      tabindex="0"
    >
      <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
      <img v-else :src="defaultAvatar" alt="默认头像" class="default-avatar-img" />
      <div class="edit-overlay">
        <el-icon><Camera /></el-icon>
      </div>
    </div>

    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileSelect"
    />

    <!-- 裁切弹窗 -->
    <el-dialog
      v-model="showCropDialog"
      title="裁切头像"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <div class="cropper-wrapper">
        <vue-cropper
          v-if="cropImageSrc"
          ref="cropperRef"
          :img="cropImageSrc"
          :autoCrop="true"
          :fixed="true"
          :fixedNumber="[1, 1]"
          :centerBox="true"
          :info="true"
          :full="true"
          :canScale="true"
          :canMove="true"
          :canMoveBox="true"
          :original="false"
          @realTime="handleRealTime"
        />
        <div v-else class="loading">加载中...</div>
      </div>

      <template #footer>
          <!-- 底部按钮（使用全局样式） -->
          <div class="footer-buttons">
            <el-button class="el-button-no" @click="showCropDialog = false">取消</el-button>
            <el-button class="el-button-yes" type="primary" @click="confirmCrop" :loading="uploading">
              {{ uploading ? '上传中...' : '确定' }}
            </el-button>
          </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import defaultAvatar from '@/assets/image/default-avatar.png'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

const props = defineProps({
  userId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:avatar'])

const userStore = useUserStore()

// 响应式数据
const fileInput = ref(null)
const showCropDialog = ref(false)
const cropImageSrc = ref('')
const uploading = ref(false)
const zoomLevel = ref(1)

// vue-cropper 实例引用
const cropperRef = ref(null)

// 当前头像URL
const avatarUrl = computed(() => userStore.user?.avatar || '')

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件
  if (!validateImageFile(file)) {
    resetFileInput()
    return
  }

  // 读取文件为DataURL
  const reader = new FileReader()
  reader.onload = (e) => {
    cropImageSrc.value = e.target.result
    showCropDialog.value = true
  }
  reader.readAsDataURL(file)
}

// 验证图片文件
const validateImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请上传 JPG、PNG、GIF 或 WebP 格式的图片')
    return false
  }

  const maxSize = 2 * 1024 * 1024 // 2MB
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  return true
}

// 旋转方法
const rotateLeft = () => {
  if (cropperRef.value) {
    cropperRef.value.rotateLeft()
  }
}

const rotateRight = () => {
  if (cropperRef.value) {
    cropperRef.value.rotateRight()
  }
}

// 实时预览（可选，用于同步缩放滑块）
const handleRealTime = (data) => {
  // 可以在这里更新缩放滑块，但 vue-cropper 没有直接提供缩放比例获取
  // 我们通过 watch 来控制缩放，这里不需要操作
}

// 监听缩放滑块，控制图片缩放
watch(zoomLevel, (newVal) => {
  if (cropperRef.value) {
    // changeScale 接收一个数字，表示缩放倍数
    cropperRef.value.changeScale(newVal)
  }
})

// 确认裁切并上传
const confirmCrop = async () => {
  if (!cropperRef.value) return

  const loading = ElLoading.service({
    lock: true,
    text: '正在上传头像...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    uploading.value = true

    // 获取裁切后的图片（Base64）
    cropperRef.value.getCropData((base64Image) => {
      // 这里得到的是裁切后的 base64 图片
      uploadImage(base64Image)
    })
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败，请重试')
    loading.close()
    uploading.value = false
  }
}

// 实际上传方法
const uploadImage = async (base64Image) => {
  try {
    // 可选：检查 Base64 长度是否超过 MEDIUMTEXT 限制（16MB ≈ 16,777,216 字符）
    if (base64Image.length > 16 * 1024 * 1024) {
      throw new Error('图片数据过大，请缩小尺寸后重试')
    }

    // 调用上传接口
    await userStore.updateAvatar(base64Image)

    // 更新本地显示
    avatarUrl.value = base64Image
    emit('update:avatar', base64Image)

    // 关闭弹窗
    showCropDialog.value = false
    resetFileInput()
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    uploading.value = false
    ElLoading.service().close()
  }
}

// 重置文件输入框
const resetFileInput = () => {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 弹窗关闭时的清理工作
const handleDialogClose = () => {
  resetFileInput()
  cropImageSrc.value = ''
  zoomLevel.value = 1
}

// 组件卸载时清理（vue-cropper 自身会清理）
onUnmounted(() => {
  // 无需额外操作
})
</script>

<style scoped>
.avatar-upload-container {
  display: inline-block;
  position: relative;
}

.current-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 4px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  outline: none;
}

.current-avatar:hover,
.current-avatar:focus-visible {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.current-avatar:hover .edit-overlay,
.current-avatar:focus-visible .edit-overlay {
  opacity: 1;
}

.current-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 确保图片覆盖整个圆形区域 */
}

.edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  font-size: 24px;
}

.cropper-wrapper {
  width: 100%;
  height: 400px;
  overflow: hidden;
}

.cropper-wrapper > div {
  width: 100%;
  height: 100%;
}

:deep(.vue-cropper .cropper-crop-box .cropper-view-box) {
  outline-color: var(--accent-color) !important;
}

:deep(.vue-cropper .cropper-crop-box .crop-point) {
  background-color: var(--accent-color) !important;
}

.dialog-footer {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.crop-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.footer-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 右上角关闭按钮悬停颜色 */
:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--accent-hover) !important;
}
</style>