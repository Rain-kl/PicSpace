<template>
  <div class="bg-gray-50">
    <!-- 页面标题 -->
    <div class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <h1 class="text-3xl font-bold text-gray-900">管理控制台</h1>
          <p class="mt-2 text-gray-600">管理用户、图片和空间设置</p>
        </div>
      </div>
    </div>

    <!-- 管理菜单网格 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- 用户管理 -->
        <div class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200">
          <router-link class="block p-6" to="/admin/userManage">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Users class="h-8 w-8 text-blue-600" />
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-gray-900">用户管理</h3>
                <p class="text-sm text-gray-600">管理系统用户和权限</p>
              </div>
            </div>
            <div class="mt-4">
              <div class="flex items-center text-sm text-blue-600">
                <span>进入管理</span>
                <ChevronRight class="ml-1 h-4 w-4" />
              </div>
            </div>
          </router-link>
        </div>

        <!-- 图片管理 -->
        <div class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200">
          <router-link class="block p-6" to="/admin/pictureManage">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Image class="h-8 w-8 text-green-600" />
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-gray-900">图片管理</h3>
                <p class="text-sm text-gray-600">管理系统中的所有图片</p>
              </div>
            </div>
            <div class="mt-4">
              <div class="flex items-center text-sm text-green-600">
                <span>进入管理</span>
                <ChevronRight class="ml-1 h-4 w-4" />
              </div>
            </div>
          </router-link>
        </div>

        <!-- 空间管理 -->
        <div class="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200">
          <router-link class="block p-6" to="/admin/spaceManage">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <FolderOpen class="h-8 w-8 text-purple-600" />
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-gray-900">空间管理</h3>
                <p class="text-sm text-gray-600">管理图片空间和分类</p>
              </div>
            </div>
            <div class="mt-4">
              <div class="flex items-center text-sm text-purple-600">
                <span>进入管理</span>
                <ChevronRight class="ml-1 h-4 w-4" />
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="mt-12">
        <h2 class="text-xl font-semibold text-gray-900 mb-6">系统统计</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Users class="h-6 w-6 text-blue-600" />
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-gray-600">总用户数</p>
                <p class="text-2xl font-semibold text-gray-900">{{ dataOverview?.userCount }}</p>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <Image class="h-6 w-6 text-green-600" />
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-gray-600">总图片数</p>
                <p class="text-2xl font-semibold text-gray-900">{{ dataOverview?.pictureCount }}</p>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <FolderOpen class="h-6 w-6 text-purple-600" />
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-gray-600">总空间数</p>
                <p class="text-2xl font-semibold text-gray-900">{{ dataOverview?.spaceCount }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ChevronRight, FolderOpen, Image, Users } from 'lucide-vue-next'
import { getDataOverviewUsingGet } from '@/api/dataAnalysisController.ts'
import { onMounted, ref } from 'vue'

onMounted(() => {
  fetchDataOverview()
})

const dataOverview = ref<API.DataOverview>()

const fetchDataOverview = async () => {
  // 模拟API调用
  // const response = await api.getAdminOverview()
  // 使用响应数据更新统计信息
  const rsp = await getDataOverviewUsingGet()
  if (rsp.data.code === 0) {
    console.log('数据概览:', rsp.data.data)
    dataOverview.value = rsp.data.data
  }
}
</script>

<style scoped>
/* 卡片悬停效果 */
.bg-white:hover {
  transform: translateY(-2px);
  transition: transform 0.2s ease-in-out;
}
</style>
