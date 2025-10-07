<template>
  <!-- 主容器 - 全屏图片查看器 -->
  <div class="fixed inset-0 bg-black z-50 flex flex-col">
    <!-- 顶部工具栏 -->
    <div
      class="relative z-10 flex items-center justify-between px-6 py-2 bg-black/80 backdrop-blur-sm"
    >
      <!-- 左侧 - 返回按钮 -->
      <div class="flex items-center space-x-4">
        <button @click="router.back()" class="white-text-button">
          <ArrowLeftOutlined class="text-xl" style="color: #ffffff !important" />
        </button>
        <!-- 缩放控制器 -->
        <div class="flex items-center space-x-2 bg-white/10 rounded-lg px-3 py-1">
          <button @click="zoomOut" class="zoom-control-button" :disabled="scale <= 1" title="缩小">
            <MinusOutlined style="color: #ffffff !important" />
          </button>
          <span class="text-white text-sm min-w-12 text-center" style="color: #ffffff !important">
            {{ Math.round(scale * 100) }}%
          </span>
          <button @click="zoomIn" class="zoom-control-button" :disabled="scale >= 5" title="放大">
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

    <!-- 图片展示区域 - 在工具栏下方，但水平居中于整个屏幕 -->
    <div class="flex-1 relative overflow-hidden">
      <!-- 图片容器 - 相对于屏幕宽度居中 -->
      <div
        ref="imageContainer"
        class="absolute inset-0 flex items-center justify-center cursor-move"
        style="left: 50vw; transform: translateX(-50%); width: 100vw"
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
          draggable="false"
        />
      </div>
    </div>

    <!-- 右侧信息面板 -->
    <div
      v-if="showInfoPanel"
      class="fixed top-0 right-0 h-full w-80 z-20 bg-black/90 backdrop-blur-sm border-l border-gray-700 transform transition-transform duration-300 ease-in-out"
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
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
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

interface Props {
  id: string | number
}

const props = defineProps<Props>()
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

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: String(props.id),
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
  const newScale = Math.max(1, Math.min(5, scale.value + delta))
  scale.value = newScale
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
    router.back()
  } else {
    message.error('删除失败')
  }
}

// 下载图片
const doDownload = () => {
  downloadImage(picture.value.url)
}

onMounted(() => {
  fetchPictureDetail()
})
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

/* 确保全屏覆盖 */
.fixed.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
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
  max-width: 100vw;
  max-height: 100vh;
  object-fit: contain;
}
</style>
