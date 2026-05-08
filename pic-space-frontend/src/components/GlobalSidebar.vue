<template>
  <!-- 侧边栏 -->
  <aside
    class="fixed left-4 top-4 bottom-4 w-20 z-50 bg-background/80 dark:bg-sidebar/80 backdrop-blur-xl rounded-2xl shadow-lg dark:shadow-2xl border border-border/20 dark:border-sidebar-border/20 flex flex-col transition-all duration-300 ease-out"
    :class="{ 'shadow-xl': !themeStore.isDark }"
  >
    <!-- 顶部Logo区域 -->
    <div class="flex items-center justify-center pt-6 pb-4">
      <div
        class="w-10 h-10 bg-foreground dark:bg-sidebar-foreground rounded-xl shadow-md flex items-center justify-center transition-transform duration-200 hover:scale-105 active:scale-95"
      >
        <span class="text-background dark:text-sidebar text-lg font-semibold">P</span>
      </div>
    </div>

    <!-- 主导航区域 -->
    <nav class="flex-1 px-3">
      <div class="space-y-3">
        <!-- 主页 -->
        <NavItem to="/" :icon="Home" :is-active="isActiveRoute('/')" aria-label="主页" />

        <!-- 私人空间 -->
        <NavItem
          to="/space"
          :icon="Inbox"
          :is-active="isActiveRoute('/space')"
          aria-label="私人空间"
        />

        <!-- 创建图片 -->
        <NavItem
          to="/add-picture"
          :icon="Plus"
          :is-active="isActiveRoute('/add-picture')"
          aria-label="创建图片"
        />
      </div>
    </nav>

    <!-- 分割线 -->
    <div class="mx-3 my-4">
      <div class="h-px bg-border/30 dark:bg-sidebar-border/30"></div>
    </div>

    <!-- 底部功能区域 -->
    <div class="px-3 pb-6 space-y-3">
      <!-- 主题切换 -->
      <div class="flex justify-center">
        <ThemeToggle />
      </div>

      <!-- 用户头像和菜单 -->
      <div class="flex justify-center">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button
              class="w-12 h-12 rounded-xl overflow-hidden transition-all duration-200 ease-out hover:scale-105 active:scale-95 focus:outline-none focus:ring-2 focus:ring-foreground/50 dark:focus:ring-sidebar-foreground/50 shadow-md hover:shadow-lg border border-border/20 dark:border-sidebar-border/20"
              :aria-label="loginUserStore.loginUser?.userName ?? '用户菜单'"
            >
              <img
                v-if="loginUserStore.loginUser?.userAvatar"
                :alt="loginUserStore.loginUser.userName ?? '用户头像'"
                :src="loginUserStore.loginUser.userAvatar"
                class="w-full rounded-xl h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 flex items-center justify-center"
              >
                <User :size="24" class="text-foreground/60 dark:text-sidebar-foreground/60" />
              </div>
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            class="w-56 ml-6 rounded-xl border-border/20 shadow-xl"
            side="right"
            :sideOffset="8"
          >
            <!-- 用户信息区域 -->
            <div class="px-4 py-3 border-b border-border/20">
              <p class="font-medium text-sm">
                {{ loginUserStore.loginUser?.userName ?? '游客' }}
              </p>
              <p
                v-if="loginUserStore.loginUser?.userProfile"
                class="text-xs text-muted-foreground mt-1"
              >
                {{ loginUserStore.loginUser.userProfile }}
              </p>
            </div>

            <!-- 菜单项 -->
            <div class="py-2">
              <div v-if="loginUserStore.loginUser?.id" class="space-y-1">
                <DropdownMenuItem>
                  <button class="flex items-center gap-3 w-full px-2 py-1.5 text-left">
                    <User :size="16" />
                    <span>个人主页</span>
                  </button>
                </DropdownMenuItem>

                <!-- 管理员控制台 -->
                <DropdownMenuItem v-if="loginUserStore.loginUser?.userRole === 'admin'">
                  <router-link
                    :class="{
                      'bg-accent': isActiveRoute('/admin'),
                    }"
                    class="flex items-center gap-3 w-full px-2 py-1.5 rounded-md hover:bg-accent transition-colors"
                    to="/admin"
                  >
                    <Shield :size="16" />
                    <span>管理控制台</span>
                  </router-link>
                </DropdownMenuItem>

                <div class="border-t border-border/20 my-2"></div>

                <DropdownMenuItem>
                  <button
                    class="flex items-center gap-3 w-full px-2 py-1.5 text-left text-destructive hover:bg-destructive/10"
                    @click="loginUserStore.logout()"
                  >
                    <LogOut :size="16" />
                    <span>退出登录</span>
                  </button>
                </DropdownMenuItem>
              </div>

              <DropdownMenuItem v-else>
                <button
                  class="flex items-center gap-3 w-full px-2 py-1.5 text-left text-foreground dark:text-sidebar-foreground"
                  @click="doLogin"
                >
                  <LogIn :size="16" />
                  <span>登录</span>
                </button>
              </DropdownMenuItem>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  </aside>
</template>
<script lang="ts" setup>
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useThemeStore } from '@/stores/useThemeStore'
import { Home, Inbox, LogIn, LogOut, Plus, Shield, User } from 'lucide-vue-next'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import NavItem from '@/components/primary-ui/NavItem.vue'
import ThemeToggle from '@/components/primary-ui/ThemeToggle.vue'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const themeStore = useThemeStore()

// 登录跳转
const doLogin = () => {
  router.push('/login')
}

// 判断是否为活跃路由
const isActiveRoute = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
/* 侧边栏样式 */
aside {
  /* 毛玻璃效果背景 */
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);

  /* 边框 */
  border: 1px solid rgb(255 255 255 / 0.2);

  /* 平滑的阴影过渡 */
  box-shadow:
    0 10px 25px rgb(0 0 0 / 0.1),
    0 4px 6px rgb(0 0 0 / 0.05),
    inset 0 1px 0 rgb(255 255 255 / 0.1);
}

/* 深色模式的特殊处理 */
.dark aside {
  border-color: rgb(255 255 255 / 0.1);
  box-shadow:
    0 10px 25px rgb(0 0 0 / 0.3),
    0 4px 6px rgb(0 0 0 / 0.1),
    inset 0 1px 0 rgb(255 255 255 / 0.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  aside {
    left: 0.5rem;
    top: 0.5rem;
    bottom: 0.5rem;
    width: 4.5rem;
  }
}

/* 提升交互体验的动画 */
aside * {
  -webkit-tap-highlight-color: transparent;
}

/* 改进焦点指示器 */
:focus-visible {
  outline: 2px solid rgb(0 0 0 / 0.8);
  outline-offset: 2px;
}

.dark :focus-visible {
  outline-color: rgb(255 255 255 / 0.8);
}

/* 平滑的过渡动画 */
* {
  transition-property:
    color, background-color, border-color, text-decoration-color, fill, stroke, opacity, box-shadow,
    transform, filter, backdrop-filter;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

/* 增强用户头像的视觉反馈 */
button img {
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

button:hover img {
  transform: scale(1.05);
}

button:active img {
  transform: scale(0.98);
}
</style>
