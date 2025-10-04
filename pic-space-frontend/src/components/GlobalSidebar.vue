<template>
  <div class="fixed left-0 top-0 h-full w-18 bg-white shadow-sm z-50 flex flex-col">
    <!-- Logo区域 -->
    <!--    <div class="flex items-center justify-center py-4">-->
    <!--      <div class="w-12 h-12 bg-gray-600 rounded-full flex items-center justify-center">-->
    <!--        <span class="text-white text-xl font-bold">PicSpace</span>-->
    <!--      </div> -->
    <!--    </div>-->

    <!-- 主导航区域 -->
    <div class="flex-1 flex flex-col items-center space-y-6 py-6">
      <!-- 主页 -->
      <div class="nav-item">
        <router-link
          :class="{
            'bg-black text-white': isActiveRoute('/'),
            'text-gray-600': !isActiveRoute('/'),
          }"
          class="w-12 h-12 rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors"
          to="/"
        >
          <Home :size="24" />
        </router-link>
      </div>

      <!-- 私人空间 -->
      <div class="nav-item">
        <router-link
          :class="{
            'bg-black text-white': isActiveRoute('/space'),
            'text-gray-600': !isActiveRoute('/space'),
          }"
          class="w-12 h-12 rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors"
          to="/space"
        >
          <Inbox :size="24" />
        </router-link>
      </div>

      <!-- 创建图片 -->
      <div class="nav-item">
        <router-link
          :class="{
            'bg-black text-white': isActiveRoute('/add-picture'),
            'text-gray-600': !isActiveRoute('/add-picture'),
          }"
          class="w-12 h-12 rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors"
          to="/add-picture"
        >
          <Plus :size="24" />
        </router-link>
      </div>

      <!-- TODO: 通知 (带红色徽章) -->
      <!-- <div class="nav-item">
        <div class="relative">
          <button
            class="w-12 h-12 rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors text-gray-600"
          >
            <Bell :size="24" />
          </button>
          <div
            class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full px-2 py-1 min-w-[24px] text-center"
          >
            99+
          </div>
        </div>
      </div> -->

      <!-- TODO: 消息/聊天 -->
      <!-- <div class="nav-item">
        <button
          class="w-12 h-12 rounded-full flex items-center justify-center hover:bg-gray-100 transition-colors text-gray-600"
        >
          <MessageCircle :size="24" />
        </button>
      </div> -->
    </div>

    <!-- 底部用户和设置区域 -->
    <div class="flex flex-col items-center space-y-4 pb-6">
      <!-- 用户头像和菜单 -->
      <div class="nav-item">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              class="w-10 h-10 rounded-full overflow-hidden hover:ring-2 hover:ring-gray-300 transition-all"
            >
              <img
                v-if="loginUserStore.loginUser?.userAvatar"
                :alt="loginUserStore.loginUser.userName ?? '用户头像'"
                :src="loginUserStore.loginUser.userAvatar"
                class="w-full h-full rounded-full object-cover"
              />
              <div
                v-else
                class="w-full rounded-full h-full bg-gray-300 flex items-center justify-center"
              >
                <User :size="20" class="text-gray-600" />
              </div>
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent class="w-48 ml-4" side="right">
            <div class="px-3 py-2 border-b">
              <p class="font-medium">{{ loginUserStore.loginUser?.userName ?? '游客' }}</p>
            </div>
            <div v-if="loginUserStore.loginUser?.id">
              <DropdownMenuItem>
                <button class="w-full text-left" @click="loginUserStore.logout()">退出登录</button>
              </DropdownMenuItem>
              <DropdownMenuItem>
                <button class="w-full text-left">个人主页</button>
              </DropdownMenuItem>
            </div>
            <DropdownMenuItem v-else>
              <button class="w-full text-left" @click="doLogin">登录</button>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <!-- 设置 -->
      <div class="nav-item">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              :class="{
                'bg-black text-white': isSettingsMenuActive,
                'text-gray-600 hover:bg-gray-100': !isSettingsMenuActive,
              }"
              class="w-12 h-12 rounded-full flex items-center justify-center transition-colors"
            >
              <Settings
                :class="{
                  'text-white': isSettingsMenuActive,
                  'text-gray-600': !isSettingsMenuActive,
                }"
                :size="24"
              />
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent class="w-48 ml-4" side="right">
            <!-- 管理员选项 (如果是管理员) -->
            <template v-if="loginUserStore.loginUser?.userRole === 'admin'">
              <DropdownMenuItem>
                <router-link
                  :class="{
                    'bg-gray-100': isActiveRoute('/admin'),
                  }"
                  class="flex items-center gap-3 w-full px-2 py-1 rounded hover:bg-gray-100"
                  to="/admin"
                >
                  <Shield :size="16" />
                  <span>管理控制台</span>
                </router-link>
              </DropdownMenuItem>
              <div class="border-t my-1"></div>
            </template>

            <!-- 通用设置选项 -->
            <DropdownMenuItem>
              <button
                class="flex items-center gap-3 w-full px-2 py-1 rounded hover:bg-gray-100 text-left"
              >
                <User :size="16" />
                <span>个人设置</span>
              </button>
            </DropdownMenuItem>
            <!--            <DropdownMenuItem>-->
            <!--              <button-->
            <!--                class="flex items-center gap-3 w-full px-2 py-1 rounded hover:bg-gray-100 text-left"-->
            <!--              >-->
            <!--                <Bell :size="16" />-->
            <!--                <span>通知设置</span>-->
            <!--              </button>-->
            <!--            </DropdownMenuItem>-->
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { Home, Inbox, Plus, Settings, Shield, User } from 'lucide-vue-next'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const doLogin = () => {
  router.push('/login')
}

// 判断是否为活跃路由
const isActiveRoute = (url: string) => {
  return route.path === url
}

// 判断设置菜单是否有活跃项
const isSettingsMenuActive = computed(() => {
  return isActiveRoute('/admin') || route.path.startsWith('/admin/')
})

const loginUserStore = useLoginUserStore()
</script>

<style scoped>
.nav-item {
  transition: all 0.2s ease-in-out;
}

.nav-item:hover {
  transform: scale(1.05);
}

/* 红色徽章动画 */
@keyframes pulse-red {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.nav-item .absolute {
  animation: pulse-red 2s infinite;
}

/* 用户头像悬停效果 */
.nav-item button:hover img {
  transform: scale(1.1);
  transition: transform 0.2s ease-in-out;
}

/* 阴影效果优化 */
.fixed.shadow-lg {
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}
</style>
