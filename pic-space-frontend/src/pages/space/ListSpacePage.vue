<script setup lang="ts">
import { Progress } from '@/components/ui/progress'
import { SPACE_LEVEL_MAP } from '@/constants/space.ts'
import router from '@/router'
import { formatSize } from '@/utils'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Crown, Folder, HardDrive, Plus, Shield, Users, Zap } from 'lucide-vue-next'

const props = defineProps<{
  spaceList: API.SpaceVO[]
  teamSpaceList?: API.SpaceVO[]
}>()

// 空间级别图标映射
const spaceLevelIcons: Record<number, any> = {
  0: Shield, // 普通版
  1: Zap, // 专业版
  2: Crown, // 旗舰版
}

// 空间级别颜色映射
const spaceLevelColors: Record<number, string> = {
  0: 'text-green-600 bg-green-50',
  1: 'text-blue-600 bg-blue-50',
  2: 'text-yellow-600 bg-yellow-50',
}

// 获取空间级别图标
const getSpaceLevelIcon = (level: number) => spaceLevelIcons[level] || Shield

// 获取空间级别颜色
const getSpaceLevelColor = (level: number) => spaceLevelColors[level] || spaceLevelColors[0]
</script>

<template>
  <div class="min-h-screen bg-gray-50/50">
    <div class="container mx-auto p-6 max-w-7xl">
      <!-- 页面头部 -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
        <div>
          <h1 class="text-3xl font-bold tracking-tight text-gray-900">空间列表</h1>
          <p class="text-gray-600 mt-1">管理您的个人空间和团队协作空间</p>
        </div>
        <Button variant="outline" @click="router.push('/add-space')" class="gap-2 shadow-md">
          <Plus class="h-4 w-4" />
          创建空间
        </Button>
      </div>

      <Separator class="mb-8" />

      <!-- 我的空间 -->
      <div class="mb-12">
        <div class="flex items-center gap-3 mb-6">
          <Folder class="h-5 w-5 text-gray-700" />
          <h2 class="text-2xl font-semibold text-gray-900">我的空间</h2>
          <span class="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
            {{ props.spaceList.length }} 个空间
          </span>
        </div>

        <div v-if="props.spaceList.length === 0" class="text-center py-12 empty-state">
          <div
            class="bg-gray-50 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4"
          >
            <Folder class="h-10 w-10 text-gray-400" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">还没有空间</h3>
          <p class="text-gray-500 mb-6">创建您的第一个空间，开始管理和分享您的图片</p>
          <Button @click="router.push('/add-space')" class="gap-2 shadow-md">
            <Plus class="h-4 w-4" />
            创建第一个空间
          </Button>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <Card
            v-for="space in props.spaceList"
            :key="space.id"
            class="group cursor-pointer card-hover-effect border-0 shadow-md bg-white/80 backdrop-blur-sm fade-in-up"
            @click="router.push('/space/' + space.id)"
          >
            <CardHeader class="pb-3">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <CardTitle
                    class="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors"
                  >
                    {{ space.spaceName }}
                  </CardTitle>
                  <CardDescription class="mt-1"> 点击进入空间 </CardDescription>
                </div>
                <component
                  :is="getSpaceLevelIcon(space.spaceLevel ?? 0)"
                  class="h-5 w-5 flex-shrink-0"
                  :class="getSpaceLevelColor(space.spaceLevel ?? 0).split(' ')[0]"
                />
              </div>
            </CardHeader>

            <CardContent class="pt-0">
              <div class="space-y-4">
                <!-- 空间级别标签 -->
                <div
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
                  :class="getSpaceLevelColor(space.spaceLevel ?? 0)"
                >
                  <component :is="getSpaceLevelIcon(space.spaceLevel ?? 0)" class="h-3 w-3" />
                  {{ SPACE_LEVEL_MAP[space.spaceLevel ?? 0] || '普通版' }}
                </div>

                <!-- 存储使用情况 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between text-sm">
                    <div class="flex items-center gap-1.5 text-gray-600">
                      <HardDrive class="h-3.5 w-3.5" />
                      存储使用
                    </div>
                    <span class="text-gray-500 font-medium">
                      {{ Math.round(((space.totalSize || 0) / (space.maxSize || 1)) * 100) }}%
                    </span>
                  </div>
                  <Progress
                    :model-value="((space.totalSize || 0) / (space.maxSize || 1)) * 100"
                    class="h-2"
                  />
                  <p class="text-xs text-gray-500">
                    {{ formatSize(space.totalSize || 0) }} / {{ formatSize(space.maxSize || 0) }}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <!-- 团队空间 -->
      <div v-if="props.teamSpaceList && props.teamSpaceList.length > 0">
        <div class="flex items-center gap-3 mb-6">
          <Users class="h-5 w-5 text-gray-700" />
          <h2 class="text-2xl font-semibold text-gray-900">团队空间</h2>
          <span class="text-sm text-gray-500 bg-gray-100 px-2 py-1 rounded-full">
            {{ props.teamSpaceList.length }} 个空间
          </span>
        </div>

        <div
          class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6"
        >
          <Card
            v-for="space in props.teamSpaceList"
            :key="space.id"
            class="group cursor-pointer card-hover-effect border-0 shadow-md bg-white/80 backdrop-blur-sm fade-in-up"
            @click="router.push('/space/' + space.id)"
          >
            <CardHeader class="pb-3">
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <CardTitle
                    class="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors"
                  >
                    {{ space.spaceName }}
                  </CardTitle>
                  <CardDescription class="mt-1 flex items-center gap-1.5">
                    <Users class="h-3.5 w-3.5" />
                    团队协作空间
                  </CardDescription>
                </div>
                <component
                  :is="getSpaceLevelIcon(space.spaceLevel ?? 0)"
                  class="h-5 w-5 flex-shrink-0"
                  :class="getSpaceLevelColor(space.spaceLevel ?? 0).split(' ')[0]"
                />
              </div>
            </CardHeader>

            <CardContent class="pt-0">
              <div class="space-y-4">
                <!-- 空间级别标签 -->
                <div
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
                  :class="getSpaceLevelColor(space.spaceLevel ?? 0)"
                >
                  <component :is="getSpaceLevelIcon(space.spaceLevel ?? 0)" class="h-3 w-3" />
                  {{ SPACE_LEVEL_MAP[space.spaceLevel ?? 0] || '普通版' }}
                </div>

                <!-- 存储使用情况 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between text-sm">
                    <div class="flex items-center gap-1.5 text-gray-600">
                      <HardDrive class="h-3.5 w-3.5" />
                      存储使用
                    </div>
                    <span class="text-gray-500 font-medium">
                      {{ Math.round(((space.totalSize || 0) / (space.maxSize || 1)) * 100) }}%
                    </span>
                  </div>
                  <Progress
                    :model-value="((space.totalSize || 0) / (space.maxSize || 1)) * 100"
                    class="h-2"
                  />
                  <p class="text-xs text-gray-500">
                    {{ formatSize(space.totalSize || 0) }} / {{ formatSize(space.maxSize || 0) }}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 卡片悬停动画 */
.card-hover-effect {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-hover-effect:hover {
  transform: translateY(-8px);
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* 渐入动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 0.6s ease-out;
}

/* 错误处理布局 */
.empty-state {
  opacity: 0;
  animation: fadeInUp 0.8s ease-out 0.2s forwards;
}
</style>
