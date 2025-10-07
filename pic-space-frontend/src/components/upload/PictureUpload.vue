<template>
  <div class="picture-upload-container">
    <a-upload
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
      class="upload-area"
    >
      <!-- 上传成功后显示图片 -->
      <div v-if="picture?.url" class="uploaded-image-container">
        <img :src="picture?.url" alt="uploaded" class="uploaded-image" />
        <div class="upload-overlay">
          <div class="upload-icon">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M16 16L12 12M12 12L8 16M12 12V21"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M20.39 18.39A5 5 0 0 0 18 9H15.5A8.5 8.5 0 1 0 3.5 18.39"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
          <p class="upload-text">点击重新上传</p>
        </div>
      </div>

      <!-- 未上传时的上传区域 -->
      <div v-else class="upload-placeholder">
        <div class="upload-icon-large">
          <loading-outlined v-if="loading" class="loading-icon"></loading-outlined>
          <svg
            v-else
            width="48"
            height="48"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M16 16L12 12M12 12L8 16M12 12V21"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M20.39 18.39A5 5 0 0 0 18 9H15.5A8.5 8.5 0 1 0 3.5 18.39"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
        <p class="upload-main-text">选择一个文件或拖放文件到此处</p>
        <p class="upload-sub-text">
          我们建议使用小于 20 MB 的高画质 .jpg 文件小于 200 MB 的高画质 .mp4 文件。
        </p>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { LoadingOutlined } from '@ant-design/icons-vue'

import { message } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { usePictureStore } from '@/stores/usePictureStore.ts'

interface Props {
  picture?: API.PictureVO
  spaceId?: string
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params = props.picture
      ? { id: props.picture.id, ...(props.spaceId ? { spaceId: props.spaceId } : {}) }
      : { ...(props.spaceId ? { spaceId: props.spaceId } : {}) }
    const res = await uploadPictureUsingPost(params, {}, file)
    if ((res.data as any).code === 0 && (res.data as any).data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.((res.data as any).data)
    } else {
      message.error('图片上传失败，' + (res.data as any).message)
    }
  } catch (error: any) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + (error?.message || '未知错误'))
  }
  loading.value = false
  usePictureStore().triggerRefresh()
}

const loading = ref<boolean>(false)

/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file: any) => {
  // 校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  // 校验图片大小
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    message.error('不能上传超过 100M 的图片')
  }
  return isJpgOrPng && isLt100M
}
</script>
<style scoped>
.picture-upload-container {
  width: 100%;
  height: 400px;
}

.upload-area :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  border: 2px dashed #d1d5db !important;
  border-radius: 12px !important;
  background: #f9fafb !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s ease !important;
}

.upload-area :deep(.ant-upload:hover) {
  border-color: #6b7280 !important;
  background: #f3f4f6 !important;
}

.uploaded-image-container {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.uploaded-image-container:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .upload-icon {
  color: white;
  margin-bottom: 8px;
}

.upload-overlay .upload-text {
  color: white;
  font-size: 14px;
  font-weight: 500;
  margin: 0;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
}

.upload-icon-large {
  color: #9ca3af;
  margin-bottom: 16px;
}

.loading-icon {
  font-size: 48px !important;
  color: #6b7280 !important;
}

.upload-main-text {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin: 0 0 8px 0;
}

.upload-sub-text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  max-width: 280px;
  line-height: 1.4;
}
</style>
