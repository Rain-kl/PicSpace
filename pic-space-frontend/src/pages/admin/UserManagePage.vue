<template>
  <div class="min-h-screen bg-background p-6">
    <div class="container mx-auto max-w-7xl space-y-6">
      <!-- 页面标题 -->
      <div class="flex items-center gap-4">

        <Button @click="router.push('/admin')" variant="ghost" size="sm" class="inline-flex items-center gap-2">
          <ChevronLeft class="h-4 w-4" />
        </Button>
        <div>
          <h1 class="text-3xl font-bold tracking-tight">用户管理</h1>
          <p class="text-muted-foreground">管理系统中的所有用户账户</p>
        </div>
      </div>

      <!-- 搜索表单 -->
      <Card>
        <CardHeader>
          <CardTitle>搜索用户</CardTitle>
          <CardDescription>根据账号或用户名筛选用户</CardDescription>
        </CardHeader>
        <CardContent>
          <form
            @submit.prevent="doSearch"
            class="flex flex-col gap-4 sm:flex-row sm:items-end sm:gap-4"
          >
            <div class="grid gap-2 flex-1">
              <Label for="userAccount" class="text-sm font-medium">账号</Label>
              <Input
                id="userAccount"
                v-model="searchParams.userAccount"
                placeholder="输入账号"
                class="w-full"
              />
            </div>
            <div class="grid gap-2 flex-1">
              <Label for="userName" class="text-sm font-medium">用户名</Label>
              <Input
                id="userName"
                v-model="searchParams.userName"
                placeholder="输入用户名"
                class="w-full"
              />
            </div>
            <Button type="submit" variant="outline" class="w-full sm:w-auto">
              <Search class="h-4 w-4 mr-2" />
              搜索
            </Button>
          </form>
        </CardContent>
      </Card>

      <!-- 数据表格 -->
      <Card>
        <CardHeader>
          <CardTitle>用户列表</CardTitle>
          <CardDescription>共 {{ total }} 个用户</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead class="w-[80px]">ID</TableHead>
                  <TableHead class="w-[120px]">头像</TableHead>
                  <TableHead class="min-w-[120px]">账号</TableHead>
                  <TableHead class="min-w-[120px]">用户名</TableHead>
                  <TableHead class="min-w-[200px]">简介</TableHead>
                  <TableHead class="w-[120px]">角色</TableHead>
                  <TableHead class="w-[180px]">创建时间</TableHead>
                  <TableHead class="w-[100px] text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="user in dataList" :key="user.id" class="hover:bg-muted/50">
                  <TableCell class="font-mono text-sm">{{ user.id }}</TableCell>
                  <TableCell>
                    <div class="flex items-center justify-center">
                      <img
                        v-if="user.userAvatar"
                        :src="user.userAvatar"
                        :alt="user.userName"
                        class="h-12 w-12 rounded-full object-cover border border-border"
                      />
                      <div
                        v-else
                        class="h-12 w-12 rounded-full bg-muted flex items-center justify-center border border-border"
                      >
                        <User class="h-6 w-6 text-muted-foreground" />
                      </div>
                    </div>
                  </TableCell>
                  <TableCell class="font-medium">{{ user.userAccount }}</TableCell>
                  <TableCell>{{ user.userName || '未设置' }}</TableCell>
                  <TableCell>
                    <div class="max-w-[300px] truncate" :title="user.userProfile">
                      {{ user.userProfile || '暂无简介' }}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div
                      v-if="user.userRole === 'admin'"
                      class="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors bg-green-50 text-green-700 border-green-200 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800"
                    >
                      <Shield class="h-3 w-3 mr-1" />
                      管理员
                    </div>
                    <div
                      v-else
                      class="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-800"
                    >
                      <User class="h-3 w-3 mr-1" />
                      普通用户
                    </div>
                  </TableCell>
                  <TableCell class="text-sm text-muted-foreground">
                    {{ dayjs(user.createTime).format('YYYY-MM-DD HH:mm') }}
                  </TableCell>
                  <TableCell class="text-right">
                    <Button
                      variant="destructive"
                      size="sm"
                      @click="doDelete(user.id)"
                      class="h-8 !text-white"
                    >
                      <Trash2 class="h-4 w-4 mr-1" />
                      删除
                    </Button>
                  </TableCell>
                </TableRow>
                <TableRow v-if="dataList.length === 0">
                  <TableCell :colspan="8" class="h-24 text-center">
                    <div class="flex flex-col items-center justify-center text-muted-foreground">
                      <Search class="h-8 w-8 mb-2" />
                      <p>没有找到用户数据</p>
                    </div>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>

          <!-- 分页控件 -->
          <div v-if="total > 0" class="flex items-center justify-between pt-4">
            <div class="text-sm text-muted-foreground">
              显示第 {{ ((searchParams.current || 1) - 1) * (searchParams.pageSize || 10) + 1 }} -
              {{
                Math.min((searchParams.current || 1) * (searchParams.pageSize || 10), total)
              }}
              条， 共 {{ total }} 条记录
            </div>
            <div class="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                :disabled="(searchParams.current || 1) <= 1"
                @click="goToPage((searchParams.current || 1) - 1)"
              >
                <ChevronLeft class="h-4 w-4" />
                上一页
              </Button>
              <div class="flex items-center space-x-1">
                <Button
                  v-for="page in visiblePages"
                  :key="page"
                  :variant="page === (searchParams.current || 1) ? 'default' : 'outline'"
                  size="sm"
                  class="w-8 h-8 p-0"
                  @click="goToPage(page)"
                  :disabled="page < 0"
                >
                  {{ page > 0 ? page : '...' }}
                </Button>
              </div>
              <Button
                variant="outline"
                size="sm"
                :disabled="(searchParams.current || 1) >= totalPages"
                @click="goToPage((searchParams.current || 1) + 1)"
              >
                下一页
                <ChevronRight class="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { ChevronLeft, ChevronRight, Search, Shield, Trash2, User } from 'lucide-vue-next'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'

import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userControllerAdmin.ts'
import router from '@/router'

// 定义数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / (searchParams.pageSize || 10))
})

// 计算可见的页码
const visiblePages = computed(() => {
  const current = searchParams.current || 1
  const total = totalPages.value
  const pages: number[] = []

  if (total <= 7) {
    // 如果总页数不超过7页，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 总是显示第一页
    pages.push(1)

    if (current <= 4) {
      // 当前页在前面时，显示 1, 2, 3, 4, 5, ..., total
      for (let i = 2; i <= 5; i++) {
        pages.push(i)
      }
      if (total > 6) {
        pages.push(-1) // 省略号
      }
      pages.push(total)
    } else if (current >= total - 3) {
      // 当前页在后面时，显示 1, ..., total-4, total-3, total-2, total-1, total
      if (total > 6) {
        pages.push(-1) // 省略号
      }
      for (let i = total - 4; i <= total; i++) {
        if (i > 1) pages.push(i)
      }
    } else {
      // 当前页在中间时，显示 1, ..., current-1, current, current+1, ..., total
      pages.push(-1) // 省略号
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push(-2) // 省略号
      pages.push(total)
    }
  }

  return pages
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listUserVoByPageUsingPost({
      ...searchParams,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = Number(res.data.data.total) || 0
    } else {
      message.error('获取数据失败: ' + res.data.message)
    }
  } catch (error) {
    message.error('获取数据失败，请稍后重试')
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 跳转到指定页面
const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  searchParams.current = page
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string | undefined) => {
  if (!id) {
    message.error('用户ID不能为空')
    return
  }

  try {
    const res = await deleteUserUsingPost({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败: ' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败，请稍后重试')
  }
}
</script>
