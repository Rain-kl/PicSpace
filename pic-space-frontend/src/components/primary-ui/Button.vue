<template>
  <component
    :is="as"
    class="primary-button relative inline-flex items-center justify-center gap-2 font-medium text-sm transition-all duration-200 ease-out focus:outline-none focus:ring-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 [&_svg]:shrink-0 overflow-hidden cursor-pointer"
    :class="[buttonClasses, props.class]"
    :disabled="disabled"
    @mousedown="handlePress"
    @touchstart="handlePress"
    @click="handleClick"
  >
    <!-- 光泽效果背景 -->
    <div
      class="absolute inset-0 bg-gradient-to-b from-white/20 to-transparent opacity-0 transition-opacity duration-200 pointer-events-none"
      :class="{ 'opacity-100': isPressed }"
    />

    <!-- 内容插槽 -->
    <slot />

    <!-- 点击波纹效果 -->
    <div ref="rippleContainer" class="absolute inset-0 pointer-events-none overflow-hidden" />
  </component>
</template>

<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { computed, ref } from 'vue'
import { createRipple, hapticFeedback } from '@/utils/effects.ts'

export interface IOSButtonProps {
  as?: string
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive'
  size?: 'sm' | 'md' | 'lg' | 'icon'
  shape?: 'rounded' | 'pill' | 'square'
  disabled?: boolean
  class?: HTMLAttributes['class']
}

const props = withDefaults(defineProps<IOSButtonProps>(), {
  as: 'button',
  variant: 'primary',
  size: 'md',
  shape: 'rounded',
  disabled: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const isPressed = ref(false)
const rippleContainer = ref<HTMLElement>()

// 计算按钮样式类
const buttonClasses = computed(() => {
  const classes = []

  // 变体样式
  switch (props.variant) {
    case 'primary':
      classes.push(
        'bg-blue-600 text-white shadow-lg',
        'hover:bg-blue-700 hover:shadow-xl hover:-translate-y-0.5',
        'active:bg-blue-800 active:translate-y-0 active:shadow-md',
        'focus:ring-blue-500/50',
      )
      break
    case 'secondary':
      classes.push(
        'bg-gray-100 text-gray-900 shadow-sm',
        'hover:bg-gray-200 hover:shadow-md hover:-translate-y-0.5',
        'active:bg-gray-300 active:translate-y-0 active:shadow-sm',
        'focus:ring-gray-500/50',
        'dark:bg-gray-800 dark:text-gray-100',
        'dark:hover:bg-gray-700 dark:active:bg-gray-900',
      )
      break
    case 'outline':
      classes.push(
        'border border-gray-300 bg-white/80 text-gray-700 shadow-sm backdrop-blur-sm',
        'hover:bg-gray-50 hover:border-gray-400 hover:shadow-md hover:-translate-y-0.5',
        'active:bg-gray-100 active:translate-y-0 active:shadow-sm',
        'focus:ring-gray-500/50',
        'dark:border-gray-600 dark:bg-gray-900/80 dark:text-gray-200',
        'dark:hover:bg-gray-800/80 dark:hover:border-gray-500',
        'dark:active:bg-gray-700/80',
      )
      break
    case 'ghost':
      classes.push(
        'text-gray-700 hover:bg-gray-100 hover:-translate-y-0.5',
        'active:bg-gray-200 active:translate-y-0',
        'focus:ring-gray-500/50',
        'dark:text-gray-200 dark:hover:bg-gray-800',
        'dark:active:bg-gray-700',
      )
      break
    case 'destructive':
      classes.push(
        'bg-red-600 text-white shadow-lg',
        'hover:bg-red-700 hover:shadow-xl hover:-translate-y-0.5',
        'active:bg-red-800 active:translate-y-0 active:shadow-md',
        'focus:ring-red-500/50',
      )
      break
  }

  // 尺寸样式
  switch (props.size) {
    case 'sm':
      classes.push('h-8 px-3 text-xs gap-1.5')
      break
    case 'md':
      classes.push('h-10 px-4 text-sm gap-2')
      break
    case 'lg':
      classes.push('h-12 px-6 text-base gap-2.5')
      break
    case 'icon':
      classes.push('h-10 w-10 p-0')
      break
  }

  // 形状样式
  switch (props.shape) {
    case 'rounded':
      classes.push('rounded-lg')
      break
    case 'pill':
      classes.push('rounded-full')
      break
    case 'square':
      classes.push('rounded-none')
      break
  }

  return classes.join(' ')
})

// 处理按下效果
const handlePress = () => {
  if (props.disabled) return

  isPressed.value = true
  hapticFeedback('light')

  setTimeout(() => {
    isPressed.value = false
  }, 150)
}

// 处理点击事件
const handleClick = (event: MouseEvent) => {
  if (props.disabled) return

  // 创建波纹效果
  if (rippleContainer.value) {
    createRipple(event, rippleContainer.value)
  }

  hapticFeedback('medium')
  emit('click', event)
}
</script>

<style scoped>
/* 按钮基础样式 */
.primary-button {
  position: relative;
  isolation: isolate;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transform-origin: center;
  will-change: transform;
}

/* 防止在按下时出现文本选择 */
.primary-button {
  -webkit-user-select: none;
  -moz-user-select: none;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

/* 深色模式下的调整 */
@media (prefers-color-scheme: dark) {
  .primary-button {
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
  }
}

/* 响应式触摸目标大小 */
@media (pointer: coarse) {
  .primary-button {
    min-height: 44px;
    min-width: 44px;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .primary-button {
    border-width: 2px;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .primary-button {
    transition: none;
  }

  .primary-button:hover {
    transform: none;
  }
}
</style>
