<template>
  <button
    @click="handleToggle"
    class="theme-toggle-btn relative w-12 h-12 rounded-xl bg-muted/60 dark:bg-sidebar-accent/40 hover:bg-muted dark:hover:bg-sidebar-accent/60 transition-all duration-200 ease-out hover:scale-105 active:scale-95 focus:outline-none focus:ring-2 focus:ring-foreground/50 dark:focus:ring-sidebar-foreground/50 flex items-center justify-center shadow-sm hover:shadow-md"
    :aria-label="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'"
  >
    <!-- 太阳图标 (浅色模式) -->
    <Transition name="theme-icon" mode="out-in">
      <Sun v-if="!themeStore.isDark" key="sun" :size="20" class="text-foreground/80" />
      <!-- 月亮图标 (深色模式) -->
      <Moon v-else key="moon" :size="20" class="text-sidebar-foreground/80" />
    </Transition>

    <!-- 活跃指示器 -->
    <div
      class="absolute inset-0 rounded-xl bg-gradient-to-br from-foreground/10 to-foreground/5 dark:from-sidebar-foreground/10 dark:to-sidebar-foreground/5 opacity-0 transition-opacity duration-200 pointer-events-none"
      :class="{ 'opacity-100': isToggling }"
    />
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore'
import { Moon, Sun } from 'lucide-vue-next'

const themeStore = useThemeStore()
const isToggling = ref(false)

// 添加切换动画反馈
const handleToggle = () => {
  isToggling.value = true
  themeStore.toggleTheme()

  setTimeout(() => {
    isToggling.value = false
  }, 300)
}
</script>

<style scoped>
.theme-toggle-btn {
  /* 触觉反馈效果 */
  -webkit-tap-highlight-color: transparent;
}

/* 主题图标切换动画 */
.theme-icon-enter-active,
.theme-icon-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-icon-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.8);
}

.theme-icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.8);
}

.theme-icon-enter-to,
.theme-icon-leave-from {
  opacity: 1;
  transform: rotate(0deg) scale(1);
}

/* 增强的悬停效果 */
.theme-toggle-btn:hover {
  transform: scale(1.05);
  box-shadow:
    0 4px 12px rgb(0 0 0 / 0.1),
    0 2px 4px rgb(0 0 0 / 0.05);
}

.theme-toggle-btn:active {
  transform: scale(0.95);
  transition-duration: 100ms;
}

/* 风格的光泽效果 */
.theme-toggle-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  border-radius: 0.75rem 0.75rem 0 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.theme-toggle-btn:hover::before {
  opacity: 1;
}

/* 深色模式下的特殊处理 */
.dark .theme-toggle-btn::before {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
}
</style>
