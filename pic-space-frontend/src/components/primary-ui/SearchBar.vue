<template>
  <div class="primary-search-bar">
    <div class="search-container" :class="containerClasses">
      <div class="search-icon">
        <Search class="w-5 h-5" :class="themeClasses.text.tertiary" />
      </div>
      <input
        ref="inputRef"
        type="text"
        :placeholder="placeholder"
        :value="modelValue"
        @input="handleInput"
        @keydown.enter="handleEnter"
        @focus="handleFocus"
        @blur="handleBlur"
        class="search-input"
        :class="inputClasses"
      />
      <div v-if="modelValue && clearable" class="clear-button" @click="handleClear">
        <X class="w-4 h-4" :class="themeClasses.text.tertiary" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search, X } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore'

export interface SearchBarProps {
  modelValue: string
  placeholder?: string
  clearable?: boolean
  disabled?: boolean
  variant?: 'default' | 'glass'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<SearchBarProps>(), {
  placeholder: '搜索...',
  clearable: true,
  disabled: false,
  variant: 'glass',
  size: 'md',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
  clear: []
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
}>()

const themeStore = useThemeStore()
const inputRef = ref<HTMLInputElement>()
const isFocused = ref(false)

// 响应式主题类
const themeClasses = computed(() => ({
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
  bg: {
    glass: themeStore.isDark
      ? 'bg-gray-900/30 backdrop-blur-md border-gray-700/50'
      : 'bg-white/30 backdrop-blur-md border-gray-200/50',
    default: themeStore.isDark ? 'bg-gray-800 border-gray-700' : 'bg-gray-100 border-gray-200',
  },
}))

// 容器样式类
const containerClasses = computed(() => [
  'relative flex items-center transition-all duration-200 ease-out',
  'border rounded-2xl shadow-sm',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
  {
    'h-10 px-3': props.size === 'sm',
    'h-12 px-4': props.size === 'md',
    'h-14 px-5': props.size === 'lg',
  },
  isFocused.value && 'ring-2 ring-blue-500/20 border-blue-500/50',
  props.disabled && 'opacity-50 cursor-not-allowed',
])

// 输入框样式类
const inputClasses = computed(() => [
  'flex-1 bg-transparent border-none outline-none transition-colors duration-200',
  'placeholder:transition-colors placeholder:duration-200',
  {
    'text-sm': props.size === 'sm',
    'text-base': props.size === 'md',
    'text-lg': props.size === 'lg',
  },
  themeClasses.value.text.primary,
  themeStore.isDark ? 'placeholder:text-gray-400' : 'placeholder:text-gray-500',
])

// 事件处理
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleEnter = () => {
  emit('search', props.modelValue)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
  inputRef.value?.focus()
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
}

// 暴露方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
})
</script>

<style scoped>
.primary-search-bar {
  width: 100%;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.search-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input {
  min-width: 0;
}

.clear-button {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  border-radius: 0.375rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.dark .clear-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 移动端适配 */
@media (max-width: 640px) {
  .search-container {
    gap: 0.5rem;
  }
}
</style>
