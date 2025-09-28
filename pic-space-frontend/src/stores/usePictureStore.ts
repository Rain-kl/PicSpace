import { ref } from 'vue'
import { defineStore } from 'pinia'

/**
 * 图片相关状态管理
 */
export const usePictureStore = defineStore('picture', () => {
  // 用于触发页面数据刷新的事件标识
  const refreshTrigger = ref(0)

  /**
   * 触发页面数据刷新
   * 当图片被删除、添加或修改时调用
   */
  function triggerRefresh() {
    refreshTrigger.value++
  }

  /**
   * 获取当前刷新触发器的值
   */
  function getRefreshTrigger() {
    return refreshTrigger.value
  }

  return {
    refreshTrigger,
    triggerRefresh,
    getRefreshTrigger,
  }
})
