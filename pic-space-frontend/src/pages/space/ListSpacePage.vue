<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores/useThemeStore'
import router from '@/router'
import { Button, EmptyState, SectionHeader, SpaceCard } from '@/components/primary-ui'
import { Folder, Plus, Users } from 'lucide-vue-next'

const props = defineProps<{
  spaceList: API.SpaceVO[]
  teamSpaceList?: API.SpaceVO[]
}>()

const themeStore = useThemeStore()

// 初始化主题
onMounted(() => {
  themeStore.initTheme()
})

// 响应式颜色类
const themeClasses = computed(() => ({
  background: themeStore.isDark ? 'bg-black' : 'bg-gray-50/30',
  surface: themeStore.isDark ? 'bg-gray-900/95' : 'bg-white/80',
  card: themeStore.isDark ? 'bg-gray-900/90' : 'bg-white/95',
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
    tertiary: themeStore.isDark ? 'text-gray-400' : 'text-gray-500',
  },
  border: themeStore.isDark ? 'border-gray-800' : 'border-gray-200',
  hover: themeStore.isDark ? 'hover:bg-gray-800/50' : 'hover:bg-gray-50',
}))
</script>

<template>
  <div class="min-h-screen transition-colors duration-300" :class="themeClasses.background">
    <!-- 导航栏 -->
    <header
      class="sticky top-0 z-50 backdrop-blur-xl border-b transition-colors duration-300"
      :class="[themeClasses.surface, themeClasses.border]"
    >
      <div class="container mx-auto px-4 py-3 max-w-6xl">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <h1
              class="text-2xl font-semibold tracking-tight transition-colors"
              :class="themeClasses.text.primary"
            >
              空间
            </h1>
          </div>
          <div class="flex items-center space-x-3">
            <Button @click="router.push('/add-space')" variant="outline" shape="pill" size="sm">
              <Plus class="h-4 w-4 mr-1.5" />
              新建
            </Button>
          </div>
        </div>
      </div>
    </header>

    <main class="container mx-auto px-4 py-6 max-w-6xl">
      <!-- 我的空间部分 -->
      <section class="mb-10">
        <SectionHeader
          title="我的空间"
          :subtitle="`${props.spaceList.length} 个空间`"
          :icon="Folder"
        />

        <!-- 空状态 -->
        <EmptyState
          v-if="props.spaceList.length === 0"
          title="还没有空间"
          description="创建您的第一个空间，开始管理和分享您的图片"
          :icon="Folder"
        >
          <template #action>
            <Button @click="router.push('/add-space')" variant="primary" shape="pill" size="lg">
              <Plus class="h-4 w-4 mr-2" />
              创建第一个空间
            </Button>
          </template>
        </EmptyState>

        <!-- 空间卡片网格 -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <SpaceCard
            v-for="(space, index) in props.spaceList"
            :key="space.id"
            :space="space"
            :animation-delay="index * 50"
            @click="router.push('/space/' + space.id)"
          />
        </div>
      </section>

      <!-- 团队空间部分 -->
      <section v-if="props.teamSpaceList && props.teamSpaceList.length > 0" class="mb-10">
        <SectionHeader
          title="团队空间"
          :subtitle="`${props.teamSpaceList.length} 个协作空间`"
          :icon="Users"
        />

        <!-- 团队空间卡片网格 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          <SpaceCard
            v-for="(space, index) in props.teamSpaceList"
            :key="space.id"
            :space="space"
            :is-team-space="true"
            :animation-delay="index * 50"
            @click="router.push('/space/' + space.id)"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
/* 全局样式 */
* {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 滚动条 */
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
  transition: background 0.2s;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

:global(.dark) ::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
}

:global(.dark) ::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
