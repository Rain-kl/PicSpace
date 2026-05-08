<template>
  <div class="text-center py-16 space-y-4 primary-empty-state">
    <div
      class="flex items-center justify-center w-20 h-20 mx-auto rounded-full transition-colors"
      :class="iconBgClass"
    >
      <component :is="icon" class="h-10 w-10" :class="themeClasses.text.tertiary" />
    </div>
    <div class="space-y-2">
      <h3 class="text-lg font-semibold transition-colors" :class="themeClasses.text.primary">
        {{ title }}
      </h3>
      <p class="text-sm max-w-sm mx-auto transition-colors" :class="themeClasses.text.secondary">
        {{ description }}
      </p>
    </div>

    <!-- 可选的操作按钮 -->
    <div v-if="$slots.action" class="pt-2">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LucideIcon } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore'

export interface EmptyStateProps {
  title: string
  description: string
  icon: LucideIcon
}

defineProps<EmptyStateProps>()

const themeStore = useThemeStore()

// 响应式颜色类
const themeClasses = computed(() => ({
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
}))

// 图标背景样式
const iconBgClass = computed(() => (themeStore.isDark ? 'bg-gray-800/50' : 'bg-gray-100'))
</script>

<style scoped>
/* 空状态动画 */
@keyframes iosEmptyState {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.primary-empty-state {
  animation: iosEmptyState 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.2s forwards;
  opacity: 0;
}
</style>
