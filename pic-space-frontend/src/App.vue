<template>
  <div id="app">
    <!-- 判断是否需要使用基础布局 -->
    <BasicLayout v-if="useBasicLayout" />
    <!-- 独立页面直接渲染 -->
    <router-view v-if="!useBasicLayout" />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 不需要基础布局的页面路径
const independentPages = ['/login', '/register']

// 计算是否使用基础布局
const useBasicLayout = computed(() => {
  return !independentPages.includes(route.path)
})
</script>

<style scoped>
/* shadcn 风格的 tabs 样式 */
:deep(.ant-tabs) {
  border-bottom: 1px solid hsl(220 13% 91%);
  background: transparent;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 0;
  background: transparent;
}

:deep(.ant-tabs-nav::before) {
  border-bottom: none;
}

:deep(.ant-tabs-nav-wrap) {
  padding: 0;
}

:deep(.ant-tabs-nav-list) {
  position: relative;
}

:deep(.ant-tabs-tab) {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  border: none;
  background: transparent;
  color: #000000;
  margin: 0 4px;
}

:deep(.ant-tabs-tab:hover) {
  color: #000000;
  background-color: hsl(210 40% 98%);
}

:deep(.ant-tabs-tab-active) {
  background-color: hsl(0 0% 100%);
  color: #000000;
  border: 1px solid hsl(220 13% 91%);
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #000000 !important;
}

:deep(.ant-tabs-tab:focus-visible) {
  outline: none;
  box-shadow: 0 0 0 2px hsl(222.2 84% 4.9%);
}

:deep(.ant-tabs-tab-btn) {
  color: #a6a6a6 !important;
  transition: inherit;
}

:deep(.ant-tabs-ink-bar) {
  display: none;
}

:deep(.ant-tabs-content-holder) {
  margin-top: 8px;
}

:deep(.ant-tabs-tabpane) {
  outline: none;
}
</style>
