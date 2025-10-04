<template>
  <div class="space-y-6 p-6">
    <!-- 页面头部 -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div class="flex items-center gap-4">
        <Button @click="goBack" variant="ghost" size="sm" class="inline-flex items-center gap-2">
          <ChevronLeft class="h-4 w-4" />
        </Button>
        <div class="space-y-1">
          <h1 class="text-2xl font-semibold tracking-tight">空间管理</h1>
          <p class="text-sm text-muted-foreground">管理系统中的所有空间资源</p>
        </div>
      </div>
      <Button
        @click="() => openWindow('/add-space')"
        class="inline-flex items-center gap-2"
        variant="outline"
      >
        <Plus class="h-4 w-4" />
        创建空间
      </Button>
    </div>

    <!-- 搜索表单 -->
    <Card class="p-6">
      <div class="space-y-4">
        <h3 class="text-lg font-medium">搜索筛选</h3>
        <form
          @submit.prevent="doSearch"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4"
        >
          <div class="space-y-2">
            <Label for="space-id">空间ID</Label>
            <Input
              id="space-id"
              v-model="searchParams.id"
              placeholder="请输入空间ID"
              class="w-full"
            />
          </div>
          <div class="space-y-2">
            <Label for="space-name">空间名称</Label>
            <Input
              id="space-name"
              v-model="searchParams.spaceName"
              placeholder="请输入空间名称"
              class="w-full"
            />
          </div>
          <div class="space-y-2">
            <Label for="user-id">用户ID</Label>
            <Input
              id="user-id"
              v-model="searchParams.userId"
              placeholder="请输入用户ID"
              class="w-full"
            />
          </div>
          <div class="flex items-end">
            <Button type="submit" class="w-full" variant="outline">
              <Search class="h-4 w-4 mr-2" />
              搜索
            </Button>
          </div>
        </form>
      </div>
    </Card>

    <!-- 数据表格 -->
    <Card>
      <div class="p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium">空间列表</h3>
          <div class="text-sm text-muted-foreground">共 {{ total }} 条记录</div>
        </div>

        <div class="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow class="bg-muted/50">
                <!-- <TableHead class="w-20">ID</TableHead> -->
                <TableHead>空间名称</TableHead>
                <TableHead>使用情况</TableHead>
                <TableHead>空间等级</TableHead>
                <TableHead class="w-20">用户ID</TableHead>
                <TableHead>创建时间</TableHead>
                <!-- <TableHead>编辑时间</TableHead> -->
                <TableHead class="w-32">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-if="dataList.length === 0">
                <TableCell :colspan="8" class="text-center py-8 text-muted-foreground">
                  <div class="flex flex-col items-center gap-2">
                    <FileX class="h-8 w-8" />
                    <span>暂无数据</span>
                  </div>
                </TableCell>
              </TableRow>
              <TableRow v-for="record in dataList" :key="record.id" class="hover:bg-muted/50">
                <!-- <TableCell class="font-mono text-sm">{{ record.id }}</TableCell> -->
                <TableCell class="font-medium">{{ record.spaceName }}</TableCell>
                <TableCell>
                  <div class="space-y-1">
                    <div class="flex items-center gap-2 text-sm">
                      <HardDrive class="h-3 w-3" />
                      <span
                        >{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</span
                      >
                    </div>
                    <div class="flex items-center gap-2 text-sm text-muted-foreground">
                      <Files class="h-3 w-3" />
                      <span>{{ record.totalCount }} / {{ record.maxCount }}</span>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <span
                    class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300"
                  >
                    {{ SPACE_LEVEL_MAP[record.spaceLevel || 0] }}
                  </span>
                </TableCell>
                <TableCell class="font-mono text-sm">{{ record.userId }}</TableCell>
                <TableCell class="text-sm text-muted-foreground">
                  {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </TableCell>
                <!-- <TableCell class="text-sm text-muted-foreground">
                  {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
                </TableCell> -->
                <TableCell>
                  <div class="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      @click="() => openWindow(`/add-space?id=${record.id}`)"
                    >
                      <Edit class="h-3 w-3" />
                    </Button>
                    <Button variant="destructive" size="sm" @click="doDelete(record.id || '')">
                      <Trash2 class="h-3 w-3" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <!-- 分页 -->
        <div class="flex flex-col sm:flex-row items-center justify-between gap-4 mt-4">
          <div class="text-sm text-muted-foreground">
            第 {{ ((searchParams.current || 1) - 1) * (searchParams.pageSize || 10) + 1 }} -
            {{ Math.min((searchParams.current || 1) * (searchParams.pageSize || 10), total) }} 条，
            共 {{ total }} 条记录
          </div>
          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              :disabled="(searchParams.current || 1) <= 1"
              @click="changePage((searchParams.current || 1) - 1)"
            >
              <ChevronLeft class="h-4 w-4" />
            </Button>
            <div class="flex items-center gap-1">
              <Button
                v-for="page in getPageNumbers()"
                :key="page"
                :variant="page === (searchParams.current || 1) ? 'default' : 'outline'"
                size="sm"
                @click="changePage(page)"
                class="w-8 h-8"
              >
                {{ page }}
              </Button>
            </div>
            <Button
              variant="outline"
              size="sm"
              :disabled="
                (searchParams.current || 1) >= Math.ceil(total / (searchParams.pageSize || 10))
              "
              @click="changePage((searchParams.current || 1) + 1)"
            >
              <ChevronRight class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { deleteSpaceUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { SPACE_LEVEL_MAP } from '@/constants/space.ts'
import { formatSize } from '@/utils'
import { listSpaceByPageUsingPost } from '@/api/spaceControllerAdmin.ts'

// shadcn/ui components
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

// Icons
import {
  ChevronLeft,
  ChevronRight,
  Edit,
  Files,
  FileX,
  HardDrive,
  Plus,
  Search,
  Trash2,
} from 'lucide-vue-next'
import router from '@/router'

const goBack = () => {
  router.push('/admin')
}

// 定义数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页逻辑
const changePage = (page: number) => {
  searchParams.current = page
  fetchData()
}

// 生成分页页码
const getPageNumbers = () => {
  const totalPages = Math.ceil(total.value / (searchParams.pageSize || 10))
  const currentPage = searchParams.current || 1
  const pages = []

  // 简单的分页逻辑：显示当前页前后各2页
  const start = Math.max(1, currentPage - 2)
  const end = Math.min(totalPages, currentPage + 2)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
}

// 打开新窗口的辅助函数
const openWindow = (url: string) => {
  window.open(url, '_blank')
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>
