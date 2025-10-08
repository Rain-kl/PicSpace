import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark' | 'system'

/**
 * 主题状态管理，支持深色模式切换
 */
export const useThemeStore = defineStore('theme', () => {
  // 当前主题模式（用户选择）
  const themeMode = ref<ThemeMode>('system')

  // 系统是否为深色模式
  const systemPrefersDark = ref(false)

  // 当前实际应用的主题（计算属性）
  const isDark = computed(() => {
    if (themeMode.value === 'dark') return true
    if (themeMode.value === 'light') return false
    return systemPrefersDark.value
  })

  // 初始化主题检测
  function initTheme() {
    // 从localStorage恢复用户偏好
    const savedTheme = localStorage.getItem('theme-mode') as ThemeMode
    if (savedTheme && ['light', 'dark', 'system'].includes(savedTheme)) {
      themeMode.value = savedTheme
    }

    // 检测系统主题偏好
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      systemPrefersDark.value = mediaQuery.matches

      // 监听系统主题变化
      mediaQuery.addEventListener('change', (e) => {
        systemPrefersDark.value = e.matches
      })
    }

    // 应用主题到DOM
    applyTheme()
  }

  // 设置主题模式
  function setThemeMode(mode: ThemeMode) {
    themeMode.value = mode
    localStorage.setItem('theme-mode', mode)
    applyTheme()
  }

  // 切换主题（在light和dark之间切换）
  function toggleTheme() {
    if (themeMode.value === 'light') {
      setThemeMode('dark')
    } else if (themeMode.value === 'dark') {
      setThemeMode('light')
    } else {
      // 如果当前是system，根据系统主题切换到相反的模式
      setThemeMode(systemPrefersDark.value ? 'light' : 'dark')
    }
  }

  // 应用主题到DOM
  function applyTheme() {
    const root = document.documentElement
    if (isDark.value) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
  }

  // 监听主题变化并应用到DOM
  watch(isDark, applyTheme, { immediate: true })

  // 主题颜色
  const themeColors = computed(() => ({
    primary: isDark.value ? '#007AFF' : '#007AFF',
    secondary: isDark.value ? '#5856D6' : '#5856D6',
    success: isDark.value ? '#34C759' : '#34C759',
    warning: isDark.value ? '#FF9500' : '#FF9500',
    danger: isDark.value ? '#FF3B30' : '#FF3B30',
    background: isDark.value ? '#000000' : '#FFFFFF',
    surface: isDark.value ? '#1C1C1E' : '#F2F2F7',
    card: isDark.value ? '#2C2C2E' : '#FFFFFF',
    border: isDark.value ? '#38383A' : '#E5E5EA',
    text: {
      primary: isDark.value ? '#FFFFFF' : '#000000',
      secondary: isDark.value ? '#EBEBF5' : '#3C3C43',
      tertiary: isDark.value ? '#EBEBF5' : '#3C3C43',
      disabled: isDark.value ? '#636366' : '#C7C7CC'
    }
  }))

  return {
    themeMode,
    isDark,
    systemPrefersDark,
    themeColors,
    initTheme,
    setThemeMode,
    toggleTheme
  }
})
