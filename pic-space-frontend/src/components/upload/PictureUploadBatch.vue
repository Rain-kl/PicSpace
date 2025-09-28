<template>
  <div class="picture-upload-card">
    <a-upload-dragger
      v-model:fileList="fileList"
      name="file"
      :multiple="props.multiple"
      :accept="props.accept"
      :before-upload="beforeUpload"
      :custom-request="handleUpload"
      :disabled="props.disabled || uploading"
      @change="handleChange"
      @drop="handleDrop"
      @remove="handleRemove"
      class="upload-dragger"
    >
      <div class="upload-content">
        <p class="upload-icon">
          <inbox-outlined v-if="!uploading" />
          <loading-outlined v-else spin />
        </p>
        <p class="upload-text">
          {{ uploading ? '上传中...' : props.uploadText || '点击或拖拽文件到此区域上传' }}
        </p>
        <p class="upload-hint">
          {{ props.hintText || '支持单个或批量上传，请勿上传敏感信息文件' }}
        </p>

        <!-- 上传进度显示 -->
        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
          <a-progress :percent="uploadProgress" size="small" />
        </div>

        <!-- 约束信息提示 -->
        <div v-if="showConstraints" class="upload-constraints">
          <p v-if="props.maxSize" class="constraint-item">
            最大文件大小: {{ formatFileSize(props.maxSize) }}
          </p>
          <p v-if="props.maxCount && props.maxCount > 1" class="constraint-item">
            最多上传: {{ props.maxCount }} 个文件
          </p>
          <p v-if="props.accept" class="constraint-item">
            支持格式: {{ formatAcceptTypes(props.accept) }}
          </p>
        </div>
      </div>
    </a-upload-dragger>

    <!-- 上传成功的文件列表 -->
    <div v-if="successList.length > 0" class="success-list">
      <h4 class="success-title">上传成功的图片:</h4>
      <div class="success-grid">
        <div v-for="(item, index) in successList" :key="item.id || index" class="success-item">
          <div class="success-preview">
            <img :src="item.url" :alt="item.name" />
            <div class="success-overlay">
              <eye-outlined @click="handlePreview(item)" />
              <delete-outlined @click="handleDelete(item, index)" />
            </div>
          </div>
          <p class="success-name">{{ item.name }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup >
import { ref, computed, h } from 'vue'
import { InboxOutlined, LoadingOutlined, EyeOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import type { UploadChangeParam, UploadFile, UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { usePictureStore } from '@/stores/usePictureStore.ts'

// 定义组件属性接口
interface Props {
  /** 空间ID */
  spaceId?: number
  /** 是否允许多选 */
  multiple?: boolean
  /** 接受的文件类型 */
  accept?: string
  /** 最大文件大小(字节) */
  maxSize?: number
  /** 最大文件数量 */
  maxCount?: number
  /** 是否禁用 */
  disabled?: boolean
  /** 上传按钮文本 */
  uploadText?: string
  /** 提示文本 */
  hintText?: string
  /** 是否显示约束信息 */
  showConstraints?: boolean
  /** 上传成功回调 */
  onSuccess?: (pictures: API.PictureVO[]) => void
  /** 上传失败回调 */
  onError?: (error: Error, file: UploadFile) => void
  /** 文件移除回调 */
  onRemove?: (file: UploadFile) => void
}

const props = withDefaults(defineProps<Props>(), {
  multiple: true,
  accept: 'image/*',
  maxSize: 100 * 1024 * 1024, // 100MB
  maxCount: 10,
  disabled: false,
  showConstraints: true,
})

// 响应式状态
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)
const uploadProgress = ref(0)
const successList = ref<API.PictureVO[]>([])

// 计算属性
const uploadCount = computed(
  () =>
    fileList.value.filter((file) => file.status === 'uploading' || file.status === 'done').length,
)

// 工具函数
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

const formatAcceptTypes = (accept: string): string => {
  return accept.replace(/image\/\*/g, '图片').replace(/,/g, '、')
}

// 文件上传前的验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 检查文件类型
  if (props.accept && !isFileTypeValid(file, props.accept)) {
    message.error(`不支持的文件类型: ${file.type || '未知'}`)
    return false
  }

  // 检查文件大小
  if (props.maxSize && file.size && file.size > props.maxSize) {
    message.error(`文件大小超过限制: ${formatFileSize(props.maxSize)}`)
    return false
  }

  // 检查文件数量
  if (props.maxCount && uploadCount.value >= props.maxCount) {
    message.error(`最多只能上传 ${props.maxCount} 个文件`)
    return false
  }

  return true
}

// 验证文件类型
const isFileTypeValid = (file: UploadFile, accept: string): boolean => {
  if (!accept || accept === '*') return true

  const acceptTypes = accept.split(',').map((type) => type.trim())
  return acceptTypes.some((type) => {
    if (type === 'image/*') {
      return file.type?.startsWith('image/')
    }
    return file.type === type
  })
}

// 上传重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000, // 1秒
}

// 延时函数
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

// 自定义上传处理（带重试机制）
const handleUpload = async ({ file, onProgress, onSuccess, onError }: any) => {
  if (!file) return

  uploading.value = true
  uploadProgress.value = 0

  let retryCount = 0
  let progressInterval: number | null = null

  // 上传执行函数
  const executeUpload = async (): Promise<void> => {
    try {
      // 准备上传参数
      const params: API.uploadPictureUsingPOSTParams = {
        spaceId: props.spaceId,
        picName: file.name.split('.')[0], // 去掉文件扩展名
      }

      // 启动进度模拟
      let currentProgress = uploadProgress.value
      progressInterval = setInterval(() => {
        if (currentProgress < 90) {
          currentProgress += Math.random() * 15
          uploadProgress.value = currentProgress
          onProgress?.({ percent: currentProgress })
        }
      }, 300)

      // 执行上传API
      const res = await uploadPictureUsingPost(params, {}, file)

      // 清除进度定时器
      if (progressInterval) {
        clearInterval(progressInterval)
        progressInterval = null
      }

      // 检查上传结果
      const response = res.data as API.BaseResponsePictureVO_
      if (response.code === 0 && response.data) {
        // 上传成功
        uploadProgress.value = 100
        onProgress?.({ percent: 100 })
        onSuccess?.(response.data)

        // 添加到成功列表
        successList.value.push(response.data)

        message.success(`${file.name} 上传成功`)

        // 调用成功回调
        props.onSuccess?.(successList.value)
      } else {
        throw new Error(response.message || '服务器返回错误')
      }
    } catch (error: any) {
      // 清除进度定时器
      if (progressInterval) {
        clearInterval(progressInterval)
        progressInterval = null
      }

      // 检查是否需要重试
      if (retryCount < RETRY_CONFIG.maxRetries && isRetryableError(error)) {
        retryCount++
        console.warn(`上传失败，正在重试 ${retryCount}/${RETRY_CONFIG.maxRetries}:`, error.message)

        message.warning(
          `${file.name} 上传失败，正在重试 (${retryCount}/${RETRY_CONFIG.maxRetries})`,
        )

        // 重置进度
        uploadProgress.value = 0
        onProgress?.({ percent: 0 })

        // 延时后重试
        await delay(RETRY_CONFIG.retryDelay * retryCount)
        return await executeUpload()
      } else {
        // 重试次数耗尽或不可重试的错误
        throw error
      }
    }
  }

  try {
    await executeUpload()
  } catch (error: any) {
    console.error('图片上传最终失败', error)

    // 构造友好的错误信息
    let errorMsg = '上传失败'
    if (error?.response?.status === 413) {
      errorMsg = '文件过大'
    } else if (error?.response?.status === 415) {
      errorMsg = '文件类型不支持'
    } else if (error?.code === 'NETWORK_ERROR' || error?.message?.includes('network')) {
      errorMsg = '网络连接失败'
    } else if (error?.message) {
      errorMsg = error.message
    }

    message.error(`${file.name} ${errorMsg}${retryCount > 0 ? `（已重试${retryCount}次）` : ''}`)

    onError?.(error)
    props.onError?.(error, file)
  } finally {
    // 清理资源
    if (progressInterval) {
      clearInterval(progressInterval)
    }
    uploading.value = false
    uploadProgress.value = 0
  }
  usePictureStore().triggerRefresh()
}

// 判断错误是否可重试
const isRetryableError = (error: any): boolean => {
  // 网络错误、超时错误、服务器5xx错误可以重试
  if (
    error?.code === 'NETWORK_ERROR' ||
    error?.code === 'TIMEOUT' ||
    error?.message?.includes('timeout') ||
    error?.message?.includes('network')
  ) {
    return true
  }

  // HTTP状态码5xx可以重试
  if (error?.response?.status >= 500 && error?.response?.status < 600) {
    return true
  }

  // 客户端错误（4xx）通常不重试
  return false
}

// 处理文件变化
const handleChange = (info: UploadChangeParam) => {
  fileList.value = info.fileList

  const { status } = info.file
  if (status === 'done') {
    console.log('文件上传完成:', info.file.name)
  } else if (status === 'error') {
    console.error('文件上传失败:', info.file.name)
  }
}

// 处理拖拽放置
const handleDrop = (e: DragEvent) => {
  console.log('文件拖拽放置:', e)
  // 可以在这里添加自定义的拖拽处理逻辑
}

// 处理文件移除
const handleRemove = (file: UploadFile) => {
  const index = fileList.value.findIndex((item) => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  props.onRemove?.(file)
  return true
}

// 处理图片预览
const handlePreview = (picture: API.PictureVO) => {
  if (!picture.url) return

  // 使用Modal显示大图
  Modal.info({
    title: picture.name || '图片预览',
    width: 800,
    content: h('div', { class: 'text-center' }, [
      h('img', {
        src: picture.url,
        alt: picture.name,
        style: {
          maxWidth: '100%',
          maxHeight: '60vh',
          objectFit: 'contain',
        },
      }),
    ]),
    footer: null,
  })
}

// 处理删除成功的图片
const handleDelete = (picture: API.PictureVO, index: number) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除图片 "${picture.name}" 吗？`,
    onOk: () => {
      successList.value.splice(index, 1)
      message.success('删除成功')
      // 更新回调
      props.onSuccess?.(successList.value)
    },
  })
}
</script>

<style scoped>
.picture-upload-card {
  width: 100%;
}

.upload-dragger {
  border: 2px dashed #d1d5db;
  transition: border-color 0.2s ease;
}

.upload-dragger:hover {
  border-color: #60a5fa;
}

.upload-content {
  padding: 2rem;
  text-align: center;
}

.upload-icon {
  font-size: 2.25rem;
  color: #9ca3af;
  margin-bottom: 1rem;
}

.upload-text {
  font-size: 1.125rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.upload-hint {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

.upload-progress {
  margin-top: 1rem;
  max-width: 20rem;
  margin-left: auto;
  margin-right: auto;
}

.upload-constraints {
  margin-top: 1rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

.upload-constraints > * + * {
  margin-top: 0.25rem;
}

.constraint-item {
  margin: 0;
}

.success-list {
  margin-top: 1.5rem;
}

.success-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.success-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

@media (min-width: 640px) {
  .success-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 768px) {
  .success-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 1024px) {
  .success-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

.success-item {
  position: relative;
}

.success-preview {
  position: relative;
  overflow: hidden;
  border-radius: 0.5rem;
  background-color: #f3f4f6;
  aspect-ratio: 1;
  transition: box-shadow 0.2s ease;
}

.success-preview:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.success-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.success-overlay {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.success-preview:hover .success-overlay {
  opacity: 1;
}

.success-overlay .anticon {
  color: white;
  font-size: 1.125rem;
  cursor: pointer;
  transition: color 0.2s ease;
}

.success-overlay .anticon:hover {
  color: #60a5fa;
}

.success-name {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #4b5563;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 640px) {
  .upload-content {
    padding: 1.5rem;
  }

  .upload-icon {
    font-size: 1.875rem;
  }

  .upload-text {
    font-size: 1rem;
  }

  .success-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
