<template>
  <div class="primary-loading-indicator">
    <!-- 加载更多状态 -->
    <div v-if="type === 'loadMore'" class="load-more-container" :class="containerClasses">
      <div v-if="loading" class="loading-content">
        <div class="spinner" :class="spinnerClasses">
          <Loader2 class="animate-spin" :class="iconSizeClasses" />
        </div>
        <span class="loading-text" :class="textClasses">{{ loadingText }}</span>
      </div>
      <div v-else-if="!hasMore" class="no-more-content">
        <span class="no-more-text" :class="noMoreTextClasses">{{ noMoreText }}</span>
      </div>
    </div>

    <!-- 页面加载状态 -->
    <div v-else-if="type === 'page'" class="page-loading-container" :class="pageContainerClasses">
      <div class="page-loading-content">
        <div class="spinner" :class="[spinnerClasses, 'mb-4']">
          <Loader2 class="animate-spin" :class="iconSizeClasses" />
        </div>
        <span class="loading-text" :class="textClasses">{{ loadingText }}</span>
      </div>
    </div>

    <!-- 内联加载状态 -->
    <div
      v-else-if="type === 'inline'"
      class="inline-loading-container"
      :class="inlineContainerClasses"
    >
      <div class="spinner" :class="spinnerClasses">
        <Loader2 class="animate-spin" :class="iconSizeClasses" />
      </div>
      <span v-if="showText" class="loading-text" :class="textClasses">{{ loadingText }}</span>
    </div>

    <!-- 覆盖层加载状态 -->
    <div
      v-else-if="type === 'overlay'"
      class="overlay-loading-container"
      :class="overlayContainerClasses"
    >
      <div class="overlay-backdrop" :class="backdropClasses">
        <div class="overlay-content" :class="overlayContentClasses">
          <div class="spinner" :class="[spinnerClasses, 'mb-4']">
            <Loader2 class="animate-spin" :class="iconSizeClasses" />
          </div>
          <span class="loading-text" :class="textClasses">{{ loadingText }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Loader2 } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore'

export interface LoadingIndicatorProps {
  loading?: boolean
  hasMore?: boolean
  type?: 'loadMore' | 'page' | 'inline' | 'overlay'
  size?: 'sm' | 'md' | 'lg'
  loadingText?: string
  noMoreText?: string
  showText?: boolean
  variant?: 'default' | 'glass'
}

const props = withDefaults(defineProps<LoadingIndicatorProps>(), {
  loading: false,
  hasMore: true,
  type: 'loadMore',
  size: 'md',
  loadingText: '加载中...',
  noMoreText: '已加载全部内容',
  showText: true,
  variant: 'glass',
})

const themeStore = useThemeStore()

// 响应式主题类
const themeClasses = computed(() => ({
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
  bg: {
    glass: themeStore.isDark
      ? 'bg-gray-900/30 backdrop-blur-sm border-gray-700/50'
      : 'bg-white/30 backdrop-blur-sm border-gray-200/50',
    default: themeStore.isDark ? 'bg-gray-800/80 border-gray-700' : 'bg-white/80 border-gray-200',
  },
}))

// 容器样式
const containerClasses = computed(() => ['flex items-center justify-center py-6 px-4'])

const pageContainerClasses = computed(() => [
  'flex items-center justify-center min-h-[200px] py-12',
])

const inlineContainerClasses = computed(() => ['flex items-center justify-center gap-2 py-2'])

const overlayContainerClasses = computed(() => [
  'fixed inset-0 z-50 flex items-center justify-center',
])

// 背景遮罩样式
const backdropClasses = computed(() => [
  'absolute inset-0 transition-all duration-200',
  themeStore.isDark ? 'bg-gray-900/80' : 'bg-white/80',
  'backdrop-blur-sm',
])

// 覆盖层内容样式
const overlayContentClasses = computed(() => [
  'relative z-10 flex flex-col items-center justify-center p-8 rounded-2xl border transition-all duration-200',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
  'shadow-lg',
])

// 旋转器样式
const spinnerClasses = computed(() => [
  'flex items-center justify-center transition-colors duration-200',
  themeClasses.value.text.secondary,
])

// 图标尺寸
const iconSizeClasses = computed(() => ({
  'w-4 h-4': props.size === 'sm',
  'w-5 h-5': props.size === 'md',
  'w-6 h-6': props.size === 'lg',
}))

// 文本样式
const textClasses = computed(() => [
  'transition-colors duration-200 font-medium',
  {
    'text-sm': props.size === 'sm',
    'text-base': props.size === 'md',
    'text-lg': props.size === 'lg',
  },
  themeClasses.value.text.secondary,
])

// 无更多内容文本样式
const noMoreTextClasses = computed(() => [
  'transition-colors duration-200',
  {
    'text-xs': props.size === 'sm',
    'text-sm': props.size === 'md',
    'text-base': props.size === 'lg',
  },
  themeClasses.value.text.tertiary,
])
</script>

<style scoped>
.primary-loading-indicator {
  width: 100%;
}

.loading-content,
.no-more-content,
.page-loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.page-loading-content {
  flex-direction: column;
  gap: 1rem;
}

.overlay-content {
  min-width: 200px;
}

/* 加载动画优化 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .load-more-container,
  .page-loading-container {
    padding: 1rem;
  }

  .overlay-content {
    margin: 1rem;
    min-width: auto;
    width: calc(100% - 2rem);
    max-width: 300px;
  }
}

/* 减少动画在低性能设备上的影响 */
@media (prefers-reduced-motion: reduce) {
  .animate-spin {
    animation: none;
  }

  .spinner {
    opacity: 0.7;
  }
}
</style>
