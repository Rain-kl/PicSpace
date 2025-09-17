<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
// import { onMounted } from 'vue'
// import { useLoginUserStore } from '@/stores/useLoginUserStore'

// const loginUserStore = useLoginUserStore()

// // 应用启动时自动获取用户登录信息
// onMounted(async () => {
//   try {
//     await loginUserStore.fetchLoginUser()
//   } catch (error) {
//     // 获取用户信息失败时不做处理，保持默认的未登录状态
//     console.log('获取用户登录信息失败:', error)
//   }
// })
</script>

<style scoped></style>
