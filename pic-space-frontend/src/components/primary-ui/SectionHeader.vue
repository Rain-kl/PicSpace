<template>
  <div class="flex items-center justify-between mb-6">
    <div class="flex items-center space-x-3">
      <div
        class="flex items-center justify-center w-8 h-8 rounded-full transition-colors"
        :class="iconBgClass"
      >
        <component :is="icon" class="h-4 w-4" :class="themeClasses.text.secondary" />
      </div>
      <div>
        <h2 class="text-xl font-semibold transition-colors" :class="themeClasses.text.primary">
          {{ title }}
        </h2>
        <p v-if="subtitle" class="text-sm transition-colors" :class="themeClasses.text.tertiary">
          {{ subtitle }}
        </p>
      </div>
    </div>

    <!-- 可选的右侧内容槽 -->
    <div v-if="$slots.actions" class="flex items-center space-x-3">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LucideIcon } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore'

export interface SectionHeaderProps {
  title: string
  subtitle?: string
  icon: LucideIcon
}

defineProps<SectionHeaderProps>()

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
const iconBgClass = computed(() => (themeStore.isDark ? 'bg-gray-800' : 'bg-gray-100'))
</script>
