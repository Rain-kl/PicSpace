<template>
  <div id="basicLayout" class="flex min-h-screen">
    <GlobalSidebar />

    <!-- 主内容区域，左边留出80px的空间给侧边栏 -->
    <main class="flex-1 ml-20 flex justify-center px-2 sm:px-4 lg:px-6 py-4">
      <div class="w-full max-w-none sm:max-w-8xl">
        <router-view v-slot="{ Component }">
          <keep-alive include="HomePage,SpacePage">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import GlobalSidebar from '@/components/GlobalSidebar.vue'
</script>

<style scoped></style>
