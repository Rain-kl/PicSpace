<template>
  <div id="basicLayout" class="flex">
    <SidebarProvider>
      <GlobalSidebar />

      <main class="flex-1 flex justify-center px-2 sm:px-4 lg:px-6 py-4">
        <div class="w-full max-w-none sm:max-w-8xl">
          <router-view v-slot="{ Component }">
            <keep-alive include="HomePage,SpacePage">
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </div>
      </main>
    </SidebarProvider>
  </div>
</template>

<script setup lang="ts">
import GlobalSidebar from '@/components/GlobalSidebar.vue'
import { SidebarProvider } from '@/components/ui/sidebar'
</script>

<style scoped></style>
