/**
 * 触觉反馈工具类
 */

export interface RippleOptions {
  duration?: number
  color?: string
  opacity?: number
}

/**
 * 点击波纹效果
 */
export function createRipple(
  event: MouseEvent | TouchEvent,
  element: HTMLElement,
  options: RippleOptions = {}
) {
  const {
    duration = 600,
    color = 'currentColor',
    opacity = 0.15
  } = options

  // 获取元素相对位置
  const rect = element.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  const radius = size / 2

  // 计算点击位置
  let x: number, y: number
  if ('touches' in event) {
    x = event.touches[0].clientX - rect.left - radius
    y = event.touches[0].clientY - rect.top - radius
  } else {
    x = event.clientX - rect.left - radius
    y = event.clientY - rect.top - radius
  }

  // 创建波纹元素
  const ripple = document.createElement('div')
  ripple.style.position = 'absolute'
  ripple.style.left = `${x}px`
  ripple.style.top = `${y}px`
  ripple.style.width = `${size}px`
  ripple.style.height = `${size}px`
  ripple.style.borderRadius = '50%'
  ripple.style.backgroundColor = color
  ripple.style.opacity = `${opacity}`
  ripple.style.transform = 'scale(0)'
  ripple.style.pointerEvents = 'none'
  ripple.style.zIndex = '1000'
  ripple.style.transition = `all ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)`

  // 确保父元素有相对定位
  const originalPosition = element.style.position
  if (!originalPosition || originalPosition === 'static') {
    element.style.position = 'relative'
  }

  // 添加波纹元素
  element.appendChild(ripple)

  // 触发动画
  requestAnimationFrame(() => {
    ripple.style.transform = 'scale(2)'
    ripple.style.opacity = '0'
  })

  // 清理
  setTimeout(() => {
    if (ripple.parentNode) {
      ripple.parentNode.removeChild(ripple)
    }
    // 恢复原始定位
    if (!originalPosition) {
      element.style.position = ''
    }
  }, duration)
}

/**
 * 触觉反馈振动
 */
export function hapticFeedback(intensity: 'light' | 'medium' | 'heavy' = 'light') {
  if ('vibrate' in navigator) {
    const patterns = {
      light: [10],
      medium: [20],
      heavy: [30]
    }
    navigator.vibrate(patterns[intensity])
  }
}

/**
 * 按钮按压效果
 */
export function createButtonPressEffect(element: HTMLElement) {
  const originalTransform = element.style.transform
  const originalTransition = element.style.transition

  element.style.transition = 'transform 100ms cubic-bezier(0.4, 0, 0.2, 1)'
  element.style.transform = 'scale(0.95)'

  setTimeout(() => {
    element.style.transform = originalTransform
    element.style.transition = originalTransition
  }, 100)
}

/**
 * Vue指令：触摸反馈
 */
export const vIosTouch = {
  mounted(el: HTMLElement, binding: any) {
    const options = binding.value || {}

    const handleStart = (e: MouseEvent | TouchEvent) => {
      // 触觉反馈
      hapticFeedback(options.haptic || 'light')

      // 按压效果
      createButtonPressEffect(el)

      // 波纹效果
      if (options.ripple !== false) {
        createRipple(e, el, options.rippleOptions)
      }
    }

    el.addEventListener('mousedown', handleStart)
    el.addEventListener('touchstart', handleStart, { passive: true })
  }
}
