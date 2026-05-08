<template>
  <div class="space-upload-modal">
    <!-- 主模态框 -->
    <Dialog :open="open" @update:open="$emit('update:open', $event)">
      <DialogContent class="space-upload-content" :class="modalClasses">
        <div class="upload-modal-container" :class="containerClasses">
          <!-- 头部 -->
          <div class="upload-header">
            <h2 class="upload-title" :class="titleClasses">
              <ImagePlus :class="titleIconClasses" />
              添加图片
            </h2>
          </div>

          <!-- 内容区域 -->
          <div class="upload-content">
            <a-tabs v-model:activeKey="activeTab" :class="tabsClasses">
              <!-- 单张上传 -->
              <a-tab-pane key="single" tab="上传图片">
                <div class="upload-single-container">
                  <div class="upload-layout">
                    <!-- 左侧：上传区域 -->
                    <div class="upload-section">
                      <PictureUpload
                        :space-id="spaceId"
                        :picture="currentPicture"
                        :onSuccess="handleUploadSuccess"
                      />

                      <!-- 从网址收藏按钮 -->
                      <Button
                        variant="outline"
                        size="lg"
                        :class="urlButtonClasses"
                        @click="showUrlUpload = true"
                      >
                        <Link :class="buttonIconClasses" />
                        从网址收藏
                      </Button>
                    </div>

                    <!-- 右侧：信息输入表单 -->
                    <div class="form-section" :class="formSectionClasses">
                      <PictureInfoForm
                        :picture-form-data="currentPictureForm"
                        :disabled="!currentPicture"
                        :success-callback="handleFormSuccess"
                      />
                    </div>
                  </div>
                </div>
              </a-tab-pane>

              <!-- 批量上传 -->
              <a-tab-pane key="batch" tab="批量上传">
                <div class="upload-batch-container">
                  <PictureUploadBatch :space-id="spaceId" />
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </div>
      </DialogContent>
    </Dialog>

    <!-- URL 上传弹窗 -->
    <Dialog :open="showUrlUpload" @update:open="showUrlUpload = $event">
      <DialogContent class="url-upload-content" :class="urlModalClasses">
        <div class="url-upload-container" :class="urlContainerClasses">
          <div class="url-upload-header">
            <h3 class="url-upload-title" :class="urlTitleClasses">
              <Link :class="titleIconClasses" />
              从网址添加图片
            </h3>
          </div>

          <div class="url-upload-form">
            <PictureUploadUrl
              :space-id="spaceId"
              :picture="currentPicture"
              :onSuccess="handleUrlUploadSuccess"
            />
          </div>

          <div class="url-upload-actions">
            <Button variant="outline" @click="showUrlUpload = false" :class="cancelButtonClasses">
              取消
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ImagePlus, Link } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore'
import { Dialog, DialogContent } from '@/components/ui/dialog'
import { Button } from '@/components/primary-ui'
import PictureUpload from '@/components/upload/PictureUpload.vue'
import PictureUploadBatch from '@/components/upload/PictureUploadBatch.vue'
import PictureUploadUrl from '@/components/upload/PictureUploadUrl.vue'
import PictureInfoForm from '@/components/PictureInfoForm.vue'
import { message } from 'ant-design-vue'

export interface SpaceUploadModalProps {
  open: boolean
  spaceId: string
  variant?: 'default' | 'glass'
}

const props = withDefaults(defineProps<SpaceUploadModalProps>(), {
  variant: 'glass',
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  uploadSuccess: []
}>()

const themeStore = useThemeStore()

// 内部状态
const activeTab = ref('single')
const showUrlUpload = ref(false)
const currentPicture = ref<API.PictureVO>()
const currentPictureForm = reactive<API.PictureUpdateRequest>({})

// 响应式主题类
const themeClasses = computed(() => ({
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
  bg: {
    glass: themeStore.isDark
      ? 'bg-gray-900/80 backdrop-blur-xl border-gray-700/50'
      : 'bg-white/80 backdrop-blur-xl border-gray-200/50',
    default: themeStore.isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200',
  },
}))

// 模态框样式
const modalClasses = computed(() => [
  'w-[95vw] max-w-[1200px] max-h-[90vh] overflow-hidden',
  'border rounded-3xl shadow-2xl',
  props.variant === 'glass' ? 'backdrop-blur-xl' : '',
])

// 容器样式
const containerClasses = computed(() => [
  'p-6 lg:p-8 space-y-6',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
])

// 标题样式
const titleClasses = computed(() => [
  'text-3xl font-bold flex items-center gap-3 transition-colors duration-200',
  themeClasses.value.text.primary,
])

const titleIconClasses = computed(() => [
  'w-8 h-8 transition-colors duration-200',
  themeClasses.value.text.secondary,
])

// Tabs样式
const tabsClasses = computed(() => ['upload-tabs'])

// URL按钮样式
const urlButtonClasses = computed(() => [
  'w-full transition-all duration-200 rounded-xl',
  'hover:scale-[1.02] active:scale-[0.98]',
])

const buttonIconClasses = computed(() => ['w-5 h-5 mr-2'])

// 表单区域样式
const formSectionClasses = computed(() => [
  'rounded-2xl border p-6 shadow-sm transition-all duration-200',
  props.variant === 'glass'
    ? themeStore.isDark
      ? 'bg-gray-800/40 border-gray-700/50'
      : 'bg-white/40 border-gray-200/50'
    : themeStore.isDark
      ? 'bg-gray-700/50 border-gray-600'
      : 'bg-gray-50 border-gray-200',
])

// URL模态框样式
const urlModalClasses = computed(() => [
  'w-[90vw] max-w-lg border rounded-2xl',
  props.variant === 'glass' ? 'backdrop-blur-xl' : '',
])

const urlContainerClasses = computed(() => [
  'p-6 space-y-4',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
])

const urlTitleClasses = computed(() => [
  'text-xl font-semibold flex items-center gap-2',
  themeClasses.value.text.primary,
])

const cancelButtonClasses = computed(() => ['w-full rounded-xl'])

// 事件处理
const handleUploadSuccess = (newPicture: API.PictureVO) => {
  currentPicture.value = newPicture
  // 设置完整的表单数据
  currentPictureForm.id = newPicture.id
  currentPictureForm.name = newPicture.name
  currentPictureForm.introduction = newPicture.introduction
  currentPictureForm.category = newPicture.category
  currentPictureForm.tags = newPicture.tags
}

const handleUrlUploadSuccess = (newPicture: API.PictureVO) => {
  handleUploadSuccess(newPicture)
  showUrlUpload.value = false
}

const handleFormSuccess = () => {
  // 重置表单状态
  currentPicture.value = undefined
  Object.keys(currentPictureForm).forEach((key) => {
    delete currentPictureForm[key as keyof API.PictureUpdateRequest]
  })

  message.success('图片保存成功')
  emit('uploadSuccess')
}
</script>

<style scoped>
.space-upload-content {
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}

.upload-modal-container {
  min-width: 0; /* 防止 flex 子项溢出 */
}

.upload-content {
  overflow-x: auto; /* 在小屏幕上允许水平滚动 */
}

.upload-layout {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 2.5rem;
  padding: 1rem 0;
  min-height: 500px;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  min-width: 300px;
}

.form-section {
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  min-width: 350px;
}

.url-upload-content {
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

/* 中等屏幕适配 */
@media (max-width: 1200px) {
  .upload-layout {
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }
}

/* 平板适配 */
@media (max-width: 900px) {
  .upload-layout {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    min-height: auto;
  }

  .form-section,
  .upload-section {
    min-width: auto;
  }
}

@media (max-width: 640px) {
  .space-upload-content {
    margin: 0.5rem;
    width: calc(100vw - 1rem);
    max-width: none;
  }

  .upload-modal-container {
    padding: 1.5rem;
  }

  .upload-layout {
    gap: 1rem;
    padding: 0.5rem 0;
  }
}

/* Ant Design Tabs 自定义样式 */
:deep(.upload-tabs .ant-tabs-nav) {
  background: transparent;
  border-bottom: 1px solid rgba(229, 231, 235, 0.3);
  margin-bottom: 1.5rem;
}

:deep(.upload-tabs .ant-tabs-tab) {
  padding: 12px 24px;
  border-radius: 12px;
  margin: 0 4px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  background: transparent;
}

:deep(.upload-tabs .ant-tabs-tab:hover) {
  background: rgba(0, 0, 0, 0.05);
}

:deep(.dark .upload-tabs .ant-tabs-tab:hover) {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.upload-tabs .ant-tabs-tab-active) {
  background: rgba(59, 130, 246, 0.1);
  color: rgb(59, 130, 246);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.upload-tabs .ant-tabs-ink-bar) {
  display: none;
}

.upload-batch-container {
  min-height: 400px;
  padding: 1rem;
}

/* 平滑过渡 */
.upload-single-container,
.upload-batch-container {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
