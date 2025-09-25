<template>
  <div id="basicLayout">
    <SidebarProvider :default-open="false">
      <GlobalSidebar />
      <div class="mt-3">
        <SidebarTrigger />
      </div>
      <div class="w-full m-6 mr-15">
        <router-view />
      </div>
    </SidebarProvider>
  </div>
</template>

<script setup lang="ts">
import GlobalSidebar from '@/components/GlobalSidebar.vue'
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
</script>

<style scoped></style>
