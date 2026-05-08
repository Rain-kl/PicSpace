<template>
  <div class="primary-category-filter">
    <div class="category-tabs" :class="containerClasses">
      <button
        v-for="category in categories"
        :key="category.key"
        @click="handleCategorySelect(category.key)"
        class="category-tab"
        :class="[
          tabClasses,
          category.key === selectedCategory ? activeTabClasses : inactiveTabClasses,
        ]"
      >
        <span class="category-text">{{ category.label }}</span>
        <span
          v-if="category.count !== undefined"
          class="category-count"
          :class="getCountClasses(category.key)"
        >
          {{ category.count }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore'

export interface CategoryItem {
  key: string
  label: string
  count?: number
}

export interface CategoryFilterProps {
  categories: CategoryItem[]
  selectedCategory: string
  variant?: 'default' | 'glass'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<CategoryFilterProps>(), {
  variant: 'glass',
  size: 'md',
})

const emit = defineEmits<{
  'update:selectedCategory': [key: string]
  change: [key: string]
}>()

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
      ? 'bg-gray-900/20 backdrop-blur-sm border-gray-700/30'
      : 'bg-white/20 backdrop-blur-sm border-gray-200/30',
    default: themeStore.isDark ? 'bg-gray-800/50 border-gray-700' : 'bg-white/80 border-gray-200',
  },
}))

// 容器样式
const containerClasses = computed(() => [
  'flex items-center gap-1 p-1 border rounded-2xl transition-all duration-200',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
  'overflow-x-auto scrollbar-hide',
])

// 基础tab样式
const tabClasses = computed(() => [
  'flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all duration-200 ease-out',
  'whitespace-nowrap cursor-pointer select-none',
  'hover:scale-[1.02] active:scale-[0.98]',
  {
    'text-sm min-h-[32px]': props.size === 'sm',
    'text-sm min-h-[36px]': props.size === 'md',
    'text-base min-h-[40px]': props.size === 'lg',
  },
])

// 激活状态tab样式
const activeTabClasses = computed(() => [
  themeStore.isDark
    ? 'bg-gray-700/80 text-white shadow-lg shadow-gray-900/20'
    : 'bg-white/90 text-gray-900 shadow-lg shadow-gray-200/50',
  'border border-opacity-20',
  themeStore.isDark ? 'border-gray-600' : 'border-gray-300',
])

// 非激活状态tab样式
const inactiveTabClasses = computed(() => [
  'hover:bg-opacity-60',
  themeStore.isDark
    ? 'text-gray-300 hover:bg-gray-700/40 hover:text-white'
    : 'text-gray-600 hover:bg-gray-100/60 hover:text-gray-900',
])

// 计数器样式（动态计算）
const getCountClasses = (categoryKey: string) => [
  'text-xs px-2 py-0.5 rounded-full transition-colors duration-200',
  props.selectedCategory === categoryKey
    ? themeStore.isDark
      ? 'bg-gray-600/60 text-gray-200'
      : 'bg-gray-100/80 text-gray-600'
    : themeStore.isDark
      ? 'bg-gray-800/60 text-gray-400'
      : 'bg-gray-200/60 text-gray-500',
]

// 事件处理
const handleCategorySelect = (key: string) => {
  if (key !== props.selectedCategory) {
    emit('update:selectedCategory', key)
    emit('change', key)
  }
}
</script>

<style scoped>
.primary-category-filter {
  width: 100%;
}

.category-tabs {
  position: relative;
}

.category-tab {
  border: none;
  background: transparent;
  outline: none;
}

.category-text {
  font-weight: 500;
}

.category-count {
  font-weight: 600;
  min-width: 1.5rem;
  text-align: center;
}

/* 隐藏滚动条但保持滚动功能 */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .category-tabs {
    gap: 0.25rem;
    padding: 0.25rem;
  }

  .category-tab {
    padding: 0.5rem 0.75rem;
    min-width: fit-content;
  }
}

/* 平滑的焦点环 */
.category-tab:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5);
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .category-tab:hover {
    transform: none;
  }

  .category-tab:active {
    transform: scale(0.96);
  }
}
</style>
