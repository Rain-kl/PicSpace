<template>
  <div id="pictureManagePage" class="space-y-6 p-6">
    <Card>
      <CardHeader>
        <div class="flex items-center justify-between gap-4">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <Button
              @click="router.push('/admin')"
              variant="ghost"
              size="sm"
              class="inline-flex items-center gap-2"
            >
              <ChevronLeft class="h-4 w-4" />
            </Button>
            <div>
              <CardTitle class="text-2xl font-bold">图片管理</CardTitle>
              <CardDescription>管理和审核平台上的所有图片内容</CardDescription>
            </div>
          </div>
          <div class="flex gap-2">
            <Button variant="outline" as-child>
              <a href="/add-picture" target="_blank" class="inline-flex items-center gap-2">
                <span class="text-sm">+</span>
                创建图片
              </a>
            </Button>
            <Button as-child>
              <a href="/add-picture/batch" target="_blank" class="inline-flex items-center gap-2">
                <span class="text-sm">+</span>
                批量创建图片
              </a>
            </Button>
          </div>
        </div>
      </CardHeader>
    </Card>
    <!-- 搜索表单 -->
    <Card>
      <CardHeader>
        <CardTitle class="text-lg">筛选条件</CardTitle>
      </CardHeader>
      <CardContent>
        <form
          @submit.prevent="doSearch"
          class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 items-end"
        >
          <div class="space-y-2">
            <Label for="searchText">关键词</Label>
            <Input
              id="searchText"
              v-model="searchParams.searchText"
              placeholder="从名称和简介搜索"
              class="w-full"
            />
          </div>

          <div class="space-y-2">
            <Label for="category">类型</Label>
            <Input
              id="category"
              v-model="searchParams.category"
              placeholder="请输入类型"
              class="w-full"
            />
          </div>

          <div class="space-y-2">
            <Label for="tags">标签</Label>
            <Input
              id="tags"
              v-model="tagsInputValue"
              placeholder="输入标签，用逗号分隔"
              class="w-full"
              @input="handleTagsInput"
              @blur="handleTagsInput"
            />
            <!-- <p class="text-xs text-muted-foreground">多个标签请用逗号分隔</p> -->
          </div>

          <div class="space-y-2">
            <Label for="reviewStatus">审核状态</Label>
            <Select
              v-model:model-value="reviewStatusValue"
              @update:model-value="handleReviewStatusChange"
            >
              <SelectTrigger class="w-full">
                <SelectValue placeholder="请选择审核状态" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部</SelectItem>
                <SelectItem value="0">待审核</SelectItem>
                <SelectItem value="1">通过</SelectItem>
                <SelectItem value="2">拒绝</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="flex items-end sm:col-span-2 lg:col-span-1">
            <Button type="submit" variant="outline" class="w-full hover:bg-primary/90">
              <span class="mr-2">🔍</span>
              搜索
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
    <!-- 表格 -->
    <Card>
      <CardContent class="p-0">
        <!-- 移动端卡片布局 -->
        <div class="md:hidden space-y-4">
          <div
            v-for="record in dataList"
            :key="record.id"
            class="border rounded-lg p-4 space-y-3 hover:shadow-md transition-shadow"
          >
            <div class="flex items-start gap-4">
              <a-image
                :src="record.url"
                :alt="record.name"
                :width="64"
                :height="64"
                class="object-cover rounded-lg shadow-sm border flex-shrink-0"
                :preview="true"
              />
              <div class="flex-1 min-w-0">
                <h4 class="font-medium truncate">{{ record.name }}</h4>
                <p class="text-sm text-muted-foreground line-clamp-2 mt-1">
                  {{ record.introduction }}
                </p>
                <div class="flex items-center gap-2 mt-2">
                  <span class="text-xs text-muted-foreground">ID: {{ record.id }}</span>
                  <span
                    :class="[
                      'inline-flex items-center rounded-full px-2 py-1 text-xs font-medium',
                      record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS
                        ? 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
                        : record.reviewStatus === PIC_REVIEW_STATUS_ENUM.REJECT
                          ? 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400'
                          : 'bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
                    ]"
                  >
                    {{
                      PIC_REVIEW_STATUS_MAP[
                        Number(record.reviewStatus) as keyof typeof PIC_REVIEW_STATUS_MAP
                      ]
                    }}
                  </span>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
              <div>类型: {{ record.category }}</div>
              <div>用户: {{ record.userId }}</div>
              <div>尺寸: {{ record.picWidth }}×{{ record.picHeight }}</div>
              <div>大小: {{ record.picSize ? (record.picSize / 1024).toFixed(2) : 0 }}KB</div>
            </div>

            <div v-if="JSON.parse(record.tags || '[]').length" class="flex flex-wrap gap-1">
              <span
                v-for="tag in JSON.parse(record.tags || '[]')"
                :key="tag"
                class="inline-flex items-center rounded-full bg-gray-50 px-2 py-1 text-xs font-medium text-gray-700 dark:bg-gray-800 dark:text-gray-300"
              >
                {{ tag }}
              </span>
            </div>

            <div class="flex gap-2 pt-2 border-t">
              <Button
                v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
                variant="outline"
                size="sm"
                @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
                class="text-green-600 hover:text-green-700 hover:bg-green-50 flex-1"
              >
                通过
              </Button>
              <Button
                v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
                variant="outline"
                size="sm"
                @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
                class="text-red-600 hover:text-red-700 hover:bg-red-50 flex-1"
              >
                拒绝
              </Button>
              <Button variant="outline" size="sm" as-child class="flex-1">
                <a :href="`/add-picture?id=${record.id}`" target="_blank">编辑</a>
              </Button>
              <Button variant="destructive" size="sm" @click="doDelete(record.id!)" class="flex-1">
                删除
              </Button>
            </div>
          </div>

          <div v-if="dataList.length === 0" class="text-center py-8 text-muted-foreground">
            暂无数据
          </div>
        </div>

        <!-- 桌面端表格布局 -->
        <div class="hidden md:block rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead class="w-16">ID</TableHead>
                <TableHead class="w-32">图片</TableHead>
                <TableHead>名称</TableHead>
                <TableHead class="hidden lg:table-cell">简介</TableHead>
                <TableHead class="hidden lg:table-cell">类型</TableHead>
                <TableHead class="hidden lg:table-cell">标签</TableHead>
                <TableHead class="hidden xl:table-cell">图片信息</TableHead>
                <TableHead class="hidden lg:table-cell">用户ID</TableHead>
                <TableHead class="hidden xl:table-cell">审核信息</TableHead>
                <TableHead class="hidden lg:table-cell">创建时间</TableHead>
                <TableHead class="w-40">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="record in dataList" :key="record.id" class="hover:bg-muted/50">
                <TableCell class="font-mono text-xs">{{ record.id }}</TableCell>

                <TableCell>
                  <div class="flex justify-center">
                    <a-image
                      :src="record.url"
                      :alt="record.name"
                      :width="80"
                      :height="80"
                      class="object-cover rounded-lg shadow-sm border"
                      :preview="true"
                    />
                  </div>
                </TableCell>

                <TableCell class="font-medium max-w-32">
                  <div class="truncate" :title="record.name">{{ record.name }}</div>
                </TableCell>

                <TableCell class="hidden lg:table-cell max-w-48">
                  <div class="truncate text-sm text-muted-foreground" :title="record.introduction">
                    {{ record.introduction }}
                  </div>
                </TableCell>

                <TableCell class="hidden lg:table-cell">
                  <span
                    class="inline-flex items-center rounded-full bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 dark:bg-blue-900/20 dark:text-blue-400"
                  >
                    {{ record.category }}
                  </span>
                </TableCell>

                <TableCell class="hidden lg:table-cell">
                  <div class="flex flex-wrap gap-1">
                    <span
                      v-for="tag in JSON.parse(record.tags || '[]')"
                      :key="tag"
                      class="inline-flex items-center rounded-full bg-gray-50 px-2 py-1 text-xs font-medium text-gray-700 dark:bg-gray-800 dark:text-gray-300"
                    >
                      {{ tag }}
                    </span>
                  </div>
                </TableCell>

                <TableCell class="hidden xl:table-cell">
                  <div class="text-xs space-y-1 text-muted-foreground">
                    <div>格式: {{ record.picFormat }}</div>
                    <div>尺寸: {{ record.picWidth }}×{{ record.picHeight }}</div>
                    <div>比例: {{ record.picScale }}</div>
                    <div>大小: {{ record.picSize ? (record.picSize / 1024).toFixed(2) : 0 }}KB</div>
                  </div>
                </TableCell>

                <TableCell class="hidden lg:table-cell font-mono text-xs">
                  {{ record.userId }}
                </TableCell>

                <TableCell class="hidden xl:table-cell">
                  <div class="space-y-1 text-xs">
                    <div class="flex items-center gap-2">
                      <span class="text-muted-foreground">状态:</span>
                      <span
                        :class="[
                          'inline-flex items-center rounded-full px-2 py-1 text-xs font-medium',
                          record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS
                            ? 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
                            : record.reviewStatus === PIC_REVIEW_STATUS_ENUM.REJECT
                              ? 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400'
                              : 'bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
                        ]"
                      >
                        {{
                          PIC_REVIEW_STATUS_MAP[
                            Number(record.reviewStatus) as keyof typeof PIC_REVIEW_STATUS_MAP
                          ]
                        }}
                      </span>
                    </div>
                    <div v-if="record.reviewMessage" class="text-muted-foreground">
                      信息: {{ record.reviewMessage }}
                    </div>
                    <div v-if="record.reviewerId" class="text-muted-foreground">
                      审核人: {{ record.reviewerId }}
                    </div>
                    <div v-if="record.reviewTime" class="text-muted-foreground">
                      时间: {{ dayjs(record.reviewTime).format('MM-DD HH:mm') }}
                    </div>
                  </div>
                </TableCell>

                <TableCell class="hidden lg:table-cell text-xs text-muted-foreground">
                  {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}
                </TableCell>

                <TableCell>
                  <div class="flex flex-col gap-1">
                    <div class="flex gap-1">
                      <Button
                        v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
                        variant="outline"
                        size="sm"
                        @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
                        class="text-green-600 hover:text-green-700 hover:bg-green-50"
                      >
                        通过
                      </Button>
                      <Button
                        v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
                        variant="outline"
                        size="sm"
                        @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
                        class="text-red-600 hover:text-red-700 hover:bg-red-50"
                      >
                        拒绝
                      </Button>
                    </div>
                    <div class="flex gap-1">
                      <Button variant="outline" size="sm" as-child>
                        <a :href="`/add-picture?id=${record.id}`" target="_blank">编辑</a>
                      </Button>
                      <Button variant="destructive" size="sm" @click="doDelete(record.id!)">
                        删除
                      </Button>
                    </div>
                  </div>
                </TableCell>
              </TableRow>

              <TableRow v-if="dataList.length === 0">
                <TableCell :colspan="11" class="text-center py-8 text-muted-foreground">
                  暂无数据
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <!-- 分页组件 -->
        <div
          class="flex flex-col sm:flex-row items-center justify-between gap-4 px-4 sm:px-6 py-4 border-t bg-muted/30"
        >
          <div class="text-sm text-muted-foreground order-2 sm:order-1">
            共 {{ total }} 条记录，第 {{ searchParams.current }} /
            {{ Math.ceil(total / (searchParams.pageSize || 10)) }} 页
          </div>
          <div class="flex items-center gap-2 order-1 sm:order-2">
            <Button
              variant="outline"
              size="sm"
              :disabled="(searchParams.current || 1) <= 1"
              @click="handlePageChange((searchParams.current || 1) - 1)"
              class="h-8 px-3"
            >
              上一页
            </Button>
            <div
              class="flex items-center justify-center min-w-[60px] h-8 px-2 text-sm font-medium bg-primary text-primary-foreground rounded-md"
            >
              {{ searchParams.current }}
            </div>
            <span class="text-sm text-muted-foreground">/</span>
            <div
              class="flex items-center justify-center min-w-[60px] h-8 px-2 text-sm text-muted-foreground"
            >
              {{ Math.ceil(total / (searchParams.pageSize || 10)) }}
            </div>
            <Button
              variant="outline"
              size="sm"
              :disabled="
                (searchParams.current || 1) >= Math.ceil(total / (searchParams.pageSize || 10))
              "
              @click="handlePageChange((searchParams.current || 1) + 1)"
              class="h-8 px-3"
            >
              下一页
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP } from '@/constants/picture.ts'
import dayjs from 'dayjs'
import {
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureControllerAdmin.ts'

// Shadcn UI 组件导入
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { ChevronLeft } from 'lucide-vue-next'
import router from '@/router'

// 移除不再使用的 columns 定义

// 定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 标签输入处理
const tagsInputValue = ref('')

// 审核状态选择处理
const reviewStatusValue = ref('all')

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
  publicFlag: true,
})

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 初始化表单输入值
const initFormInputs = () => {
  // 初始化标签输入
  if (searchParams.tags && Array.isArray(searchParams.tags)) {
    tagsInputValue.value = searchParams.tags.join(', ')
  }
  // 初始化审核状态选择
  if (searchParams.reviewStatus !== undefined) {
    reviewStatusValue.value = searchParams.reviewStatus.toString()
  } else {
    reviewStatusValue.value = 'all'
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  initFormInputs()
  fetchData()
})

// 标签输入处理函数
const handleTagsInput = () => {
  // 将逗号分隔的字符串转换为数组
  if (tagsInputValue.value) {
    searchParams.tags = tagsInputValue.value
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag)
  } else {
    searchParams.tags = undefined
  }
}

// 审核状态选择处理函数
const handleReviewStatusChange = (value: any) => {
  reviewStatusValue.value = value || 'all'
  if (!value || value === 'all') {
    searchParams.reviewStatus = undefined
  } else {
    searchParams.reviewStatus = Number(value)
  }
}

// 分页处理函数
const handlePageChange = (page: number) => {
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
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 审核图片
const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表数据
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}
</script>
