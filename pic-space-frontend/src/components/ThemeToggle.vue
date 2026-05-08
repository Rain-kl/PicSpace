<template>
  <Button
    variant="ghost"
    size="icon"
    @click="toggleTheme"
    class="relative h-9 w-9 rounded-full transition-all duration-300 hover:bg-gray-100 dark:hover:bg-gray-800"
    :class="themeClasses"
  >
    <!-- 太阳图标 (浅色模式时显示) -->
    <Sun v-if="!themeStore.isDark" class="h-4 w-4 transition-all duration-300 text-amber-500" />
    <!-- 月亮图标 (深色模式时显示) -->
    <Moon v-else class="h-4 w-4 transition-all duration-300 text-blue-400" />

    <span class="sr-only">切换主题</span>
  </Button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore'
import { Button } from '@/components/ui/button'
import { Moon, Sun } from 'lucide-vue-next'

const themeStore = useThemeStore()

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const themeClasses = computed(() => ({
  'bg-amber-50 text-amber-600 hover:bg-amber-100': !themeStore.isDark,
  'bg-blue-950/50 text-blue-400 hover:bg-blue-900/50': themeStore.isDark,
}))
</script>

<style scoped>
/* 主题切换按钮 */
.theme-toggle {
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
</style>
