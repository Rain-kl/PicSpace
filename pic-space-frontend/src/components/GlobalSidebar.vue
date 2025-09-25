<template>
  <Sidebar collapsible="icon">
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="item in items" :key="item.title">
              <SidebarMenuButton asChild>
                <a :href="item.url" class="flex items-center gap-2">
                  <!-- 图标会根据折叠状态自动调整大小 -->
                  <component :is="item.icon" />
                  <span>{{ item.title }}</span>
                </a>
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
                  <SidebarMenuButton>
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
                      <SidebarMenuSubButton asChild>
                        <a :href="item.url" class="flex items-center gap-2">
                          <component :is="item.icon" />
                          <span>{{ item.title }}</span>
                        </a>
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
                  <SidebarMenuButton>
                    <Settings />
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent side="right" class="w-48">
                  <DropdownMenuItem v-for="item in adminItems" :key="item.title">
                    <a :href="item.url" class="flex items-center gap-2 w-full">
                      <component :is="item.icon" class="h-4 w-4" />
                      <span>{{ item.title }}</span>
                    </a>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
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
import { useRouter } from 'vue-router'

const router = useRouter()
const doLogin = () => {
  router.push('/user/login')
}

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
    url: '/my-space',
    icon: Inbox,
  },
  {
    url: '/add-picture',
    icon: GitPullRequestCreate,
    title: '创建图片',
  }
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
