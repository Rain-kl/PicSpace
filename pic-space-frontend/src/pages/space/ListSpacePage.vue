<script setup lang="ts">
import { Progress } from '@/components/ui/progress'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_STYLE_MAP } from '@/constants/space.ts'
import router from '@/router'
import { formatSize } from '@/utils'
import { Button } from '@/components/ui/button'

const props = defineProps<{
  spaceList: API.SpaceVO[]
  teamSpaceList?: API.SpaceVO[]
}>()
</script>

<template>
  <div class="p-4">
    <h1 class="text-2xl">空间列表</h1>
    <a-divider class="border-1" />
    <Button :variant="'outline'" @click="router.push('/add-space')">创建空间</Button>

    <!-- 我的空间 -->
    <div class="mt-6">
      <h2 class="text-xl mb-4">我的空间</h2>
      <div class="flex flex-wrap gap-6">
        <a-card
          v-for="space in props.spaceList"
          :key="space.id"
          :hoverable="true"
          class="w-64 shadow-md"
          @click="router.push('/space/' + space.id)"
        >
          <a-space class="w-full" direction="vertical">
            <div class="text-lg font-semibold">{{ space.spaceName }}</div>
            <a-tag
              :bordered="false"
              :color="SPACE_LEVEL_STYLE_MAP[space.spaceLevel ?? 0]?.aTagColor || 'green'"
              >{{ SPACE_LEVEL_MAP[space.spaceLevel ?? 0] || '普通版' }}</a-tag
            >

            <Progress :model-value="((space.totalSize || 0) / (space.maxSize || 1)) * 100" />
            <p class="text-sm text-gray-400">
              {{ formatSize(space.totalSize || 0) }}/{{ formatSize(space.maxSize || 0) }}
            </p>
            <!--          <div class="text-sm text-gray-500">{{ space.spaceType }}</div>-->
          </a-space>
        </a-card>
      </div>
    </div>

    <!-- 我加入的团队空间 -->
    <div v-if="props.teamSpaceList && props.teamSpaceList.length > 0" class="mt-8">
      <h2 class="text-xl mb-4">团队空间</h2>
      <div class="flex flex-wrap gap-6">
        <a-card
          v-for="space in props.teamSpaceList"
          :key="space.id"
          :hoverable="true"
          class="w-64 shadow-md"
          @click="router.push('/space/' + space.id)"
        >
          <a-space class="w-full" direction="vertical">
            <div class="text-lg font-semibold">{{ space.spaceName }}</div>
            <a-tag
              :bordered="false"
              :color="SPACE_LEVEL_STYLE_MAP[space.spaceLevel ?? 0]?.aTagColor || 'green'"
              >{{ SPACE_LEVEL_MAP[space.spaceLevel ?? 0] || '普通版' }}</a-tag
            >

            <Progress :model-value="((space.totalSize || 0) / (space.maxSize || 1)) * 100" />
            <p class="text-sm text-gray-400">
              {{ formatSize(space.totalSize || 0) }}/{{ formatSize(space.maxSize || 0) }}
            </p>
            <!--          <div class="text-sm text-gray-500">{{ space.spaceType }}</div>-->
          </a-space>
        </a-card>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
