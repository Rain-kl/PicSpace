<template>
  <Sidebar collapsible="icon">
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="item in items" :key="item.title">
              <SidebarMenuButton asChild :data-active="isActiveRoute(item.url)">
                <router-link
                  :to="item.url"
                  class="flex items-center gap-2"
                  :class="{
                    'bg-accent text-accent-foreground': isActiveRoute(item.url),
                  }"
                >
                  <!-- 图标会根据折叠状态自动调整大小 -->
                  <component :is="item.icon" />
                  <span>{{ item.title }}</span>
                </router-link>
              </SidebarMenuButton>
            </SidebarMenuItem>

            <!-- 展开状态下的可折叠菜单 -->
            <Collapsible
              v-if="loginUserStore.loginUser?.userRole === 'admin' && state !== 'collapsed'"
              defaultOpen
              class="group/collapsible"
            >
              <SidebarMenuItem>
                <CollapsibleTrigger asChild>
                  <SidebarMenuButton
                    :class="{
                      'bg-accent text-accent-foreground': isAdminMenuActive,
                    }"
                  >
                    <Settings />
                    <span>管理员功能</span>
                    <ChevronRight
                      class="ml-auto transition-transform group-data-[state=open]/collapsible:rotate-90"
                    />
                  </SidebarMenuButton>
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <SidebarMenuSub>
                    <SidebarMenuSubItem v-for="item in adminItems" :key="item.title">
                      <SidebarMenuSubButton asChild :data-active="isActiveRoute(item.url)">
                        <router-link
                          :to="item.url"
                          class="flex items-center gap-2"
                          :class="{
                            'bg-accent text-accent-foreground': isActiveRoute(item.url),
                          }"
                        >
                          <component :is="item.icon" />
                          <span>{{ item.title }}</span>
                        </router-link>
                      </SidebarMenuSubButton>
                    </SidebarMenuSubItem>
                  </SidebarMenuSub>
                </CollapsibleContent>
              </SidebarMenuItem>
            </Collapsible>

            <!-- 折叠状态下的下拉菜单 -->
            <SidebarMenuItem
              v-if="loginUserStore.loginUser?.userRole === 'admin' && state === 'collapsed'"
            >
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <SidebarMenuButton
                    :class="{
                      'bg-accent text-accent-foreground': isAdminMenuActive,
                    }"
                  >
                    <Settings />
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent side="right" class="w-48">
                  <DropdownMenuItem v-for="item in adminItems" :key="item.title">
                    <router-link
                      :to="item.url"
                      class="flex items-center gap-2 w-full"
                      :class="{
                        'bg-accent text-accent-foreground': isActiveRoute(item.url),
                      }"
                    >
                      <component :is="item.icon" class="h-4 w-4" />
                      <span>{{ item.title }}</span>
                    </router-link>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
      <SidebarTrigger />
      <SidebarMenu>
        <SidebarMenuItem>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <SidebarMenuButton>
                <a-space>
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                </a-space>
                <span v-if="state !== 'collapsed'">{{
                  loginUserStore.loginUser.userName ?? '无名'
                }}</span>
              </SidebarMenuButton>
            </DropdownMenuTrigger>
            <DropdownMenuContent side="top" class="w-[--reka-popper-anchor-width]">
              <DropdownMenuItem>
                <span @click="loginUserStore.logout()" v-if="loginUserStore.loginUser.id"
                  >Sign out</span
                >
                <span @click="doLogin" v-if="!loginUserStore.loginUser.id">login</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarFooter>
  </Sidebar>
</template>
<script lang="ts" setup>
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  SidebarTrigger,
  useSidebar,
} from '@/components/ui/sidebar'
import {
  GitPullRequestCreate,
  Home,
  Inbox,
  Settings,
  Users,
  Image,
  FolderOpen,
  ChevronRight,
} from 'lucide-vue-next'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const doLogin = () => {
  router.push('/login')
}

// 判断是否为活跃路由
const isActiveRoute = (url: string) => {
  return route.path === url
}

// 判断管理员菜单是否有活跃项
const isAdminMenuActive = computed(() => {
  return adminItems.some((item) => isActiveRoute(item.url))
})

interface MenuProps {
  title: string
  url: string
  icon: any
}

// Menu items.
const originItems: MenuProps[] = [
  {
    title: 'Home',
    url: '/',
    icon: Home,
  },
  {
    title: '私人空间',
    url: '/space',
    icon: Inbox,
  },
  {
    url: '/add-picture',
    icon: GitPullRequestCreate,
    title: '创建图片',
  },
]

const adminItems: MenuProps[] = [
  {
    url: '/admin/userManage',
    icon: Users,
    title: '用户管理',
  },
  {
    url: '/admin/pictureManage',
    icon: Image,
    title: '图片管理',
  },
  {
    url: '/admin/spaceManage',
    icon: FolderOpen,
    title: '空间管理',
  },
]

const loginUserStore = useLoginUserStore()
const { state } = useSidebar()

const items = computed(() => originItems)

// 路由跳转事件
</script>

<style scoped></style>
