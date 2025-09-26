<template>
  <div id="basicLayout">
    <SidebarProvider>
      <GlobalSidebar />
      <SidebarInset>
        <div class="mt-3 sticky top-0 z-10">
          <SidebarTrigger />
        </div>
        <div class="w-full flex justify-center px-7">
          <router-view v-slot="{ Component }">
            <keep-alive include="HomePage">
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </div>
      </SidebarInset>
    </SidebarProvider>
  </div>
</template>

<script setup lang="ts">
import GlobalSidebar from '@/components/GlobalSidebar.vue'
import { SidebarProvider, SidebarTrigger, SidebarInset } from '@/components/ui/sidebar'
</script>

<style scoped></style>
