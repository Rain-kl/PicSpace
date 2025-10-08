<template>
  <router-link
    :to="to"
    class="nav-item-link relative flex items-center justify-center w-14 h-14 rounded-xl transition-all duration-200 ease-out hover:scale-105 active:scale-95 focus:outline-none focus:ring-2 focus:ring-foreground/50 dark:focus:ring-sidebar-foreground/50 overflow-hidden"
    :class="navItemClasses"
    :aria-label="ariaLabel"
    @mousedown="handlePress"
    @touchstart="handlePress"
  >
    <!-- 背景效果 -->
    <div
      class="absolute inset-0 rounded-xl transition-all duration-200"
      :class="backgroundClasses"
    />

    <!-- 光泽效果 -->
    <div
      class="absolute inset-0 rounded-xl bg-gradient-to-b from-white/20 to-transparent opacity-0 transition-opacity duration-200 pointer-events-none"
      :class="{ 'opacity-100': isActive }"
    />

    <!-- 图标 -->
    <component
      :is="icon"
      :size="22"
      class="relative z-10 transition-all duration-200"
      :class="iconClasses"
    />

    <!-- 活跃指示器 -->
    <Transition name="indicator" appear>
      <div
        v-if="isActive"
        class="absolute -right-1 top-1/2 -translate-y-1/2 w-1 h-6 bg-foreground dark:bg-sidebar-foreground rounded-full shadow-sm"
      />
    </Transition>
  </router-link>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LucideIcon } from 'lucide-vue-next'
import { createRipple, hapticFeedback } from '@/utils/effects.ts'

interface Props {
  to: string
  icon: LucideIcon
  isActive: boolean
  ariaLabel?: string
}

const props = defineProps<Props>()

// 触摸反馈
const handlePress = (event: MouseEvent | TouchEvent) => {
  // 轻触觉反馈
  hapticFeedback('light')

  // 创建波纹效果 - 使用黑白配色
  const target = event.currentTarget as HTMLElement
  createRipple(event, target, {
    color: props.isActive ? 'rgb(0 0 0)' : 'rgb(156 163 175)',
    opacity: 0.15,
    duration: 400,
  })
}

// 背景样式
const backgroundClasses = computed(() => ({
  // 活跃状态 - 使用黑白配色
  'bg-foreground/10 dark:bg-sidebar-foreground/15': props.isActive,
  // 悬停状态 - 使用hover:前缀确保只在悬停时生效
  'hover:bg-muted/60 dark:hover:bg-sidebar-accent/40': !props.isActive,
  // 毛玻璃效果
  'backdrop-blur-sm': props.isActive,
}))

// 图标样式
const iconClasses = computed(() => ({
  'text-foreground dark:text-sidebar-foreground': props.isActive,
  'text-foreground/70 dark:text-sidebar-foreground/70': !props.isActive,
  'group-hover:text-foreground dark:group-hover:text-sidebar-foreground': !props.isActive,
}))

// 链接样式
const navItemClasses = computed(() => ({
  group: true,
}))
</script>

<style scoped>
.nav-item-link {
  /* 触觉反馈效果 */
  -webkit-tap-highlight-color: transparent;
}

/* 增强的悬停和活跃状态动画 */
.nav-item-link:hover {
  transform: scale(1.05);
}

.nav-item-link:active {
  transform: scale(0.95);
  transition-duration: 100ms;
}

/* 阴影效果 */
.nav-item-link::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 0.75rem;
  padding: 1px;
  background: linear-gradient(
    145deg,
    rgb(229 231 235 / 0.2) 0%,
    transparent 50%,
    rgb(229 231 235 / 0.1) 100%
  );
  -webkit-mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.2s ease;
}

/* 深色模式下的边框效果 */
.dark .nav-item-link::before {
  background: linear-gradient(
    145deg,
    rgb(255 255 255 / 0.1) 0%,
    transparent 50%,
    rgb(255 255 255 / 0.05) 100%
  );
}

.nav-item-link:hover::before {
  opacity: 1;
}

/* 活跃指示器动画 */
.indicator-enter-active,
.indicator-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.indicator-enter-from {
  opacity: 0;
  transform: translateY(-50%) scale(0.8);
}

.indicator-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.8);
}

.indicator-enter-to,
.indicator-leave-from {
  opacity: 1;
  transform: translateY(-50%) scale(1);
}
</style>
