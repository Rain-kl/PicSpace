<template>
  <div
    class="primary-card group cursor-pointer primary-card-animation"
    :class="[themeClasses.card, cardClass]"
    :style="{ animationDelay: `${animationDelay}ms` }"
    @click="$emit('click', space)"
  >
    <!-- 卡片头部 -->
    <div class="p-4 pb-3">
      <div class="flex items-start justify-between mb-3">
        <div class="flex-1 min-w-0">
          <h3
            class="font-semibold text-base truncate transition-colors group-hover:text-blue-600"
            :class="themeClasses.text.primary"
          >
            {{ space.spaceName }}
          </h3>
          <div
            v-if="isTeamSpace"
            class="flex items-center gap-1.5 text-sm mt-1 transition-colors"
            :class="themeClasses.text.tertiary"
          >
            <Users class="h-3.5 w-3.5" />
            <span>团队协作空间</span>
          </div>
          <p v-else class="text-sm mt-1 transition-colors" :class="themeClasses.text.tertiary">
            点击进入空间
          </p>
        </div>
        <component
          :is="levelConfig.icon"
          class="h-5 w-5 flex-shrink-0 ml-2"
          :class="levelConfig.colors[isDark ? 'dark' : 'light'].split(' ')[0]"
        />
      </div>

      <!-- 级别标签 -->
      <div
        class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
        :class="levelConfig.colors[isDark ? 'dark' : 'light']"
      >
        <component :is="levelConfig.icon" class="h-3 w-3" />
        {{ levelConfig.name }}
      </div>
    </div>

    <!-- 存储信息 -->
    <div class="px-4 pb-4">
      <div class="space-y-3">
        <div class="flex items-center justify-between text-sm">
          <div
            class="flex items-center gap-1.5 transition-colors"
            :class="themeClasses.text.secondary"
          >
            <HardDrive class="h-3.5 w-3.5" />
            <span>存储使用</span>
          </div>
          <span
            class="font-medium tabular-nums transition-colors"
            :class="themeClasses.text.secondary"
          >
            {{ storagePercentage }}%
          </span>
        </div>

        <ProgressBar :value="storagePercentage" :class="themeClasses" />

        <p class="text-xs tabular-nums transition-colors" :class="themeClasses.text.tertiary">
          {{ formatSize(space.totalSize || 0) }} / {{ formatSize(space.maxSize || 0) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Crown, HardDrive, Shield, Users, Zap } from 'lucide-vue-next'
import { formatSize } from '@/utils'
import { useThemeStore } from '@/stores/useThemeStore'
import ProgressBar from './ProgressBar.vue'

export interface SpaceCardProps {
  space: API.SpaceVO
  isTeamSpace?: boolean
  animationDelay?: number
  cardClass?: string
}

const props = withDefaults(defineProps<SpaceCardProps>(), {
  isTeamSpace: false,
  animationDelay: 0,
})

const emit = defineEmits<{
  click: [space: API.SpaceVO]
}>()

const themeStore = useThemeStore()

// 空间级别配置
const spaceLevelConfig = {
  0: {
    icon: Shield,
    name: '基础版',
    colors: {
      light: 'text-emerald-600 bg-emerald-50',
      dark: 'text-emerald-400 bg-emerald-950/50',
    },
  },
  1: {
    icon: Zap,
    name: '专业版',
    colors: {
      light: 'text-blue-600 bg-blue-50',
      dark: 'text-blue-400 bg-blue-950/50',
    },
  },
  2: {
    icon: Crown,
    name: '旗舰版',
    colors: {
      light: 'text-amber-600 bg-amber-50',
      dark: 'text-amber-400 bg-amber-950/50',
    },
  },
}

// 获取空间级别配置
const levelConfig = computed(
  () =>
    spaceLevelConfig[props.space.spaceLevel as keyof typeof spaceLevelConfig] ||
    spaceLevelConfig[0],
)

// 计算存储使用百分比
const storagePercentage = computed(() =>
  Math.round(((props.space.totalSize || 0) / (props.space.maxSize || 1)) * 100),
)

// 响应式颜色类
const themeClasses = computed(() => ({
  card: themeStore.isDark ? 'bg-gray-900/90' : 'bg-white/95',
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
}))

// 是否为深色模式
const isDark = computed(() => themeStore.isDark)
</script>

<style scoped>
/* 卡片设计 */
.primary-card {
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  border: none;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.08),
    0 1px 2px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 深色模式下的卡片样式 */
:global(.dark) .primary-card {
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.3),
    0 1px 2px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.08);
}

.primary-card:hover {
  transform: translateY(-2px) scale(1.005);
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.08);
}

/* 深色模式下的卡片悬停效果 */
:global(.dark) .primary-card:hover {
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.4),
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.12);
}

.primary-card:active {
  transform: translateY(-1px) scale(1.002);
  transition: all 0.15s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 卡片入场动画 */
@keyframes iosSlideUp {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.primary-card-animation {
  animation: iosSlideUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  opacity: 0;
}

/* 触觉反馈（视觉模拟） */
.primary-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.primary-card:hover::before {
  opacity: 1;
}

:global(.dark) .primary-card::before {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
}

/* 深度层次感 */
.primary-card {
  position: relative;
  isolation: isolate;
}

.primary-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  pointer-events: none;
  background: linear-gradient(145deg, transparent 0%, rgba(0, 0, 0, 0.02) 100%);
}

:global(.dark) .primary-card::after {
  background: linear-gradient(145deg, transparent 0%, rgba(255, 255, 255, 0.02) 100%);
}

/* 响应式布局优化 */
@media (max-width: 640px) {
  .primary-card {
    border-radius: 12px;
  }

  .primary-card:hover {
    transform: translateY(-1px) scale(1.002);
  }
}
</style>
