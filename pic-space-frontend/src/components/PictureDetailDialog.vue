<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogScrollContent
      class="fullscreen-dialog max-w-screen max-h-screen w-screen h-screen p-0 bg-black border-0 rounded-none"
      style="
        margin: 0 !important;
        padding: 0 !important;
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        right: 0 !important;
        bottom: 0 !important;
        width: 100vw !important;
        height: 100vh !important;
      "
    >
      <!-- 主容器 - 全屏图片查看器 -->
      <div
        class="relative bg-black flex flex-col h-screen m-10 p-0"
        @dblclick="handleBackgroundDoubleClick"
        ref="dialogContainer"
        style="margin: 0 !important; padding: 0 !important"
      >
        <!-- 顶部工具栏 -->
        <div
          class="relative z-10 flex items-center justify-between px-6 py-2 bg-black/80 backdrop-blur-sm"
          style="margin-top: 0 !important"
        >
          <!-- 左侧 - 缩放控制器 -->
          <div class="flex items-center space-x-4">
            <!-- 缩放控制器 -->
            <div class="flex items-center space-x-2 bg-white/10 rounded-lg px-3 py-1">
              <button
                @click="zoomOut"
                class="zoom-control-button"
                :disabled="scale <= 1"
                title="缩小"
              >
                <MinusOutlined style="color: #ffffff !important" />
              </button>
              <span
                class="text-white text-sm min-w-12 text-center"
                style="color: #ffffff !important"
              >
                {{ Math.round(scale * 100) }}%
              </span>
              <button
                @click="zoomIn"
                class="zoom-control-button"
                :disabled="scale >= 5"
                title="放大"
              >
                <PlusOutlined style="color: #ffffff !important" />
              </button>
              <div class="w-px h-4 bg-gray-400 mx-1"></div>
              <button @click="resetZoom" class="zoom-control-button text-xs" title="重置缩放">
                重置
              </button>
            </div>
          </div>

          <!-- 中间 - 图片信息摘要 -->
          <div class="flex-1 flex items-center justify-center text-white">
            <div class="text-center max-w-md">
              <h1 class="text-lg font-medium text-center truncate">
                {{ picture.name || '未命名图片' }}
              </h1>
              <p class="text-sm text-gray-300 text-center truncate">
                {{ picture.picWidth }}x{{ picture.picHeight }} · {{ formatSize(picture.picSize) }}
              </p>
            </div>
          </div>

          <!-- 右侧 - 功能按钮 -->
          <div class="flex items-center space-x-2">
            <!-- 下载按钮 -->
            <button @click="doDownload" class="white-text-button" title="下载图片">
              <DownloadOutlined class="text-xl" style="color: #ffffff !important" />
            </button>

            <!-- 信息按钮 -->
            <button
              @click="toggleInfoPanel"
              class="white-text-button"
              :class="{ 'bg-white/20': showInfoPanel }"
              title="图片信息"
            >
              <InfoCircleOutlined class="text-xl" style="color: #ffffff !important" />
            </button>

            <!-- 编辑按钮 (仅管理员) -->
            <button v-if="canEdit" @click="doEdit" class="white-text-button" title="编辑图片">
              <EditOutlined class="text-xl" style="color: #ffffff !important" />
            </button>

            <!-- 删除按钮 (仅管理员) -->
            <button
              v-if="canEdit"
              @click="doDelete"
              class="text-red-400 hover:text-red-300 transition-colors p-2 rounded-lg hover:bg-red-500/20"
              title="删除图片"
            >
              <DeleteOutlined class="text-xl" style="color: #ffffff !important" />
            </button>
          </div>
        </div>

        <!-- 图片展示区域 -->
        <div class="flex-1 relative overflow-hidden">
          <!-- 图片容器 -->
          <div
            ref="imageContainer"
            class="absolute inset-0 flex items-center justify-center cursor-move"
            @wheel.prevent="handleWheel"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseUp"
          >
            <img
              v-if="picture.url"
              :src="picture.url"
              :alt="picture.name"
              class="max-w-none select-none mx-auto"
              :style="{
                transform: `scale(${scale}) translate(${translateX}px, ${translateY}px)`,
                transition: isDragging ? 'none' : 'transform 0.1s ease-out',
              }"
              @load="handleImageLoad"
              @dblclick.stop
              draggable="false"
            />
          </div>
        </div>

        <!-- 右侧信息面板 -->
        <div
          v-if="showInfoPanel"
          class="absolute top-0 right-0 h-full w-80 z-20 bg-black/90 backdrop-blur-sm border-l border-gray-700 transform transition-transform duration-300 ease-in-out"
          :class="showInfoPanel ? 'translate-x-0' : 'translate-x-full'"
        >
          <div class="p-6 h-full overflow-y-auto">
            <!-- 关闭按钮 -->
            <div class="flex justify-between items-center mb-6">
              <h2 class="info-content text-lg font-semibold">图片信息</h2>
              <button
                @click="toggleInfoPanel"
                class="text-gray-400 hover:text-white transition-colors p-1"
              >
                <CloseOutlined />
              </button>
            </div>

            <!-- 图片信息 -->
            <div class="space-y-4 text-sm">
              <!-- 作者信息 -->
              <div class="border-b border-gray-700 pb-4">
                <div class="flex items-center space-x-3 mb-2">
                  <img
                    :src="picture.userVO?.userAvatar"
                    :alt="picture.userVO?.userName"
                    class="w-10 h-10 rounded-full bg-gray-600"
                  />
                  <div>
                    <p class="info-content font-medium">{{ picture.userVO?.userName }}</p>
                    <p class="text-gray-400 text-xs">作者</p>
                  </div>
                </div>
              </div>

              <!-- 基本信息 -->
              <div class="info-spacing">
                <div>
                  <label class="info-label">名称</label>
                  <p class="info-content">{{ picture.name || '未命名' }}</p>
                </div>

                <div v-if="picture.introduction">
                  <label class="info-label">简介</label>
                  <p class="info-content">{{ picture.introduction }}</p>
                </div>

                <div>
                  <label class="info-label">分类</label>
                  <p class="info-content">{{ picture.category || '默认' }}</p>
                </div>

                <div v-if="picture.tags && picture.tags.length">
                  <label class="info-label">标签</label>
                  <div class="flex flex-wrap gap-1">
                    <span v-for="tag in picture.tags" :key="tag" class="tag-style">
                      {{ tag }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 技术信息 -->
              <div class="border-divider info-spacing">
                <h3 class="info-content font-medium">技术信息</h3>

                <div class="tech-info-grid">
                  <div>
                    <label class="info-label">格式</label>
                    <p class="info-content">{{ picture.picFormat || '-' }}</p>
                  </div>

                  <div>
                    <label class="info-label">大小</label>
                    <p class="info-content">{{ formatSize(picture.picSize) }}</p>
                  </div>

                  <div>
                    <label class="info-label">宽度</label>
                    <p class="info-content">{{ picture.picWidth || '-' }}</p>
                  </div>

                  <div>
                    <label class="info-label">高度</label>
                    <p class="info-content">{{ picture.picHeight || '-' }}</p>
                  </div>

                  <div class="col-span-2">
                    <label class="info-label">宽高比</label>
                    <p class="info-content">{{ picture.picScale || '-' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DialogScrollContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  CloseOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  InfoCircleOutlined,
  MinusOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { usePictureStore } from '@/stores/usePictureStore.ts'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize } from '@/utils'
import { Dialog, DialogScrollContent } from '@/components/ui/dialog'

interface Props {
  open: boolean
  pictureId?: string | number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:open': [open: boolean]
}>()

const picture = ref<API.PictureVO>({})
const loginUserStore = useLoginUserStore()
const pictureStore = usePictureStore()
const router = useRouter()

// 信息面板状态
const showInfoPanel = ref(false)

// 图片缩放和拖拽状态
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const imageContainer = ref<HTMLElement>()
const dialogContainer = ref<HTMLElement>()

// 是否具有编辑权限
const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  // 未登录不可编辑
  if (!loginUser.id) {
    return false
  }
  // 仅本人或管理员可编辑
  const user = picture.value.userVO || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})

// 监听 pictureId 变化，获取图片详情
watch(
  () => props.pictureId,
  async (newId) => {
    if (newId && props.open) {
      await fetchPictureDetail()
    }
  },
  { immediate: true },
)

// 监听 open 状态变化
watch(
  () => props.open,
  async (newOpen) => {
    if (newOpen && props.pictureId) {
      await fetchPictureDetail()
    } else if (!newOpen) {
      // 关闭时重置状态
      resetZoom()
      showInfoPanel.value = false
    }
  },
)

// 获取图片详情
const fetchPictureDetail = async () => {
  if (!props.pictureId) return

  try {
    const res = await getPictureVoByIdUsingGet({
      id: String(props.pictureId),
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

// 切换信息面板
const toggleInfoPanel = () => {
  showInfoPanel.value = !showInfoPanel.value
}

// 滚轮缩放
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()
  const delta = event.deltaY > 0 ? -0.1 : 0.1

  scale.value = Math.max(1, Math.min(5, scale.value + delta))
}

// 鼠标拖拽
const handleMouseDown = (event: MouseEvent) => {
  if (scale.value <= 1) return
  isDragging.value = true
  dragStart.value = {
    x: event.clientX - translateX.value,
    y: event.clientY - translateY.value,
  }
}

const handleMouseMove = (event: MouseEvent) => {
  if (!isDragging.value || scale.value <= 1) return
  translateX.value = event.clientX - dragStart.value.x
  translateY.value = event.clientY - dragStart.value.y
}

const handleMouseUp = () => {
  isDragging.value = false
}

// 图片加载完成
const handleImageLoad = () => {
  resetZoom()
}

// 缩放控制
const zoomIn = () => {
  scale.value = Math.min(5, scale.value + 0.2)
}

const zoomOut = () => {
  scale.value = Math.max(1, scale.value - 0.2)
}

const resetZoom = () => {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

// 编辑
const doEdit = () => {
  router.push('/add-picture?id=' + picture.value.id)
  emit('update:open', false)
}

// 删除数据
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 触发页面数据刷新
    pictureStore.triggerRefresh()
    emit('update:open', false)
  } else {
    message.error('删除失败')
  }
}

// 下载图片
const doDownload = () => {
  downloadImage(picture.value.url)
}

// 处理背景双击事件
const handleBackgroundDoubleClick = (event: MouseEvent) => {
  // 检查点击的目标是否是图片元素
  const target = event.target as HTMLElement
  const isImage = target.tagName === 'IMG'

  // 如果点击的是图片，不关闭弹窗
  if (isImage) {
    return
  }

  // 如果点击的是黑色背景区域，关闭弹窗
  emit('update:open', false)
}
</script>

<style scoped>
/* 重复样式类 - 减少代码重复 */

/* 白色文本按钮样式 - 用于工具栏按钮 */
.white-text-button {
  color: #ffffff !important;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: color 0.15s ease-in-out;
}

.white-text-button:hover {
  color: #f3f4f6 !important;
  background-color: rgba(255, 255, 255, 0.1);
}

/* 缩放控制按钮样式 */
.zoom-control-button {
  color: #ffffff !important;
  padding: 0.25rem;
  transition: color 0.15s ease-in-out;
}

.zoom-control-button:hover {
  color: #f3f4f6 !important;
}

/* 信息面板标签样式 */
.info-label {
  color: #9ca3af;
  display: block;
  margin-bottom: 0.25rem;
}

/* 信息面板内容样式 */
.info-content {
  color: #ffffff;
}

/* 技术信息网格布局 */
.tech-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  font-size: 0.75rem;
  line-height: 1rem;
}

/* 信息间距 */
.info-spacing > * + * {
  margin-top: 0.75rem;
}

/* 边框分隔线 */
.border-divider {
  border-top: 1px solid #374151;
  padding-top: 1rem;
}

/* 标签样式 */
.tag-style {
  padding: 0.25rem 0.5rem;
  background-color: #374151;
  color: #ffffff;
  font-size: 0.75rem;
  line-height: 1rem;
  border-radius: 0.25rem;
}

/* 图片容器样式 */
.cursor-move {
  cursor: move;
}

/* 禁止图片选择 */
.select-none {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

/* 滚动条样式 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* 按钮禁用状态 */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 图片加载状态 */
img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 覆盖默认 dialog 样式以实现真正的全屏 */
:deep(.fullscreen-dialog) {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  max-width: none !important;
  max-height: none !important;
  margin: 0 !important;
  padding: 0 !important;
  border-radius: 0 !important;
  border: none !important;
  transform: none !important;
}

/* 确保遮罩层也是全屏的 */
:deep([data-slot='dialog']) {
  position: fixed !important;
  inset: 0 !important;
  z-index: 50 !important;
}

/* 强制移除所有可能的边距和内边距 */
:deep([data-slot='dialog'] > *) {
  margin: 0 !important;
  padding: 0 !important;
}

/* 确保dialog overlay覆盖整个屏幕 */
:deep([data-radix-dialog-overlay]) {
  position: fixed !important;
  inset: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* 覆盖 DialogOverlay 的默认样式 */

/* 覆盖 DialogContent 的默认边距和内边距 */

/* 更具体的选择器来覆盖 DialogContent */

/* 强制重置所有可能影响布局的默认样式 */
.fullscreen-dialog,
.fullscreen-dialog * {
  box-sizing: border-box;
}

/* 确保dialog完全覆盖屏幕，没有任何偏移 */
:deep([data-radix-portal]) {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
}

/* 强制覆盖所有可能的边距类 */

:deep(.p-6) {
  padding: 0 !important;
}

/* 使用更高优先级的选择器 */
.fullscreen-dialog :deep(*) {
  margin-top: 0 !important;
  margin-bottom: 0 !important;
}
</style>
