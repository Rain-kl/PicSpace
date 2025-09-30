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

<style scoped></style>
