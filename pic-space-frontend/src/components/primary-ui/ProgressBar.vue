<template>
  <div class="primary-progress-container">
    <div class="primary-progress-track" :class="trackClass">
      <div
        class="primary-progress-fill transition-all duration-500 ease-out"
        :class="fillClass"
        :style="{ width: `${Math.min(value, 100)}%` }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore'

export interface ProgressBarProps {
  value: number
  color?: 'blue' | 'green' | 'red' | 'amber'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<ProgressBarProps>(), {
  color: 'blue',
  size: 'md',
})

const themeStore = useThemeStore()

// 进度条轨道样式
const trackClass = computed(() => (themeStore.isDark ? 'bg-gray-800' : 'bg-gray-200'))

// 进度条填充样式
const fillClass = computed(() => {
  const colorClasses = {
    blue: 'bg-blue-500',
    green: 'bg-green-500',
    red: 'bg-red-500',
    amber: 'bg-amber-500',
  }

  const sizeClasses = {
    sm: 'h-1',
    md: 'h-1.5',
    lg: 'h-2',
  }

  return `${colorClasses[props.color]} ${sizeClasses[props.size]}`
})
</script>

<style scoped>
/* 进度条 */
.primary-progress-container {
  position: relative;
}

.primary-progress-track {
  width: 100%;
  height: 6px;
  border-radius: 9999px;
  overflow: hidden;
}

.primary-progress-fill {
  height: 100%;
  border-radius: 9999px;
  background: linear-gradient(90deg, currentColor 0%, currentColor 100%);
  transform-origin: left;
}

/* 蓝色渐变 */
.primary-progress-fill.bg-blue-500 {
  background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 100%);
}

/* 绿色渐变 */
.primary-progress-fill.bg-green-500 {
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
}

/* 红色渐变 */
.primary-progress-fill.bg-red-500 {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
}

/* 琥珀色渐变 */
.primary-progress-fill.bg-amber-500 {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
}

/* 尺寸变体 */
.primary-progress-track.h-1 {
  height: 4px;
}

.primary-progress-track.h-1\.5 {
  height: 6px;
}

.primary-progress-track.h-2 {
  height: 8px;
}
</style>
