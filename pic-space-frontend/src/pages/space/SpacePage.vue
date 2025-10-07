<template>
  <div class="w-full max-w-8xl mx-auto p-4">
    <!-- 搜索框 -->
    <!--    <SearchBar/>-->

    <div
      class="sticky z-5 top-0 w-full h-[70px] bg-white shadow-none border-gray-200 flex justify-between items-center mb-2"
    >
      <!--    <div class="w-full h-15">-->

      <Input
        id="search"
        type="text"
        placeholder="搜索..."
        class="h-[50px] border-0 rounded-md bg-[#EFEFEF]"
        v-model="searchInputData"
        @keydown.enter="doSearch()"
      />

      <div class="ml-5 h-[50px] justify-self-end flex items-center mb-2 gap-4 justify-center">
        <!--添加图片-->
        <Dialog>
          <DialogTrigger>
            <Button variant="ghost" size="icon">
              <Plus class="button" />
            </Button>
          </DialogTrigger>
          <DialogContent
            class="sm:max-w-6xl !min-h-[80vh] overflow-y-auto"
            style="max-height: 80vh"
          >
            <Card class="border-none shadow-none">
              <CardHeader>
                <CardTitle class="text-3xl">添加图片</CardTitle>
              </CardHeader>
              <CardContent>
                <a-tabs v-model:activeKey="activeKey">
                  <a-tab-pane key="1" tab="上传图片">
                    <!-- 新的左右布局 -->
                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 py-4">
                      <!-- 左侧：上传区域 -->
                      <div class="flex flex-col space-y-4">
                        <PictureUpload
                          :space-id="spaceInfo.id"
                          :picture="currentPicture"
                          :onSuccess="onUploadSuccess"
                        />

                        <!-- 从网址收藏按钮 -->
                        <button
                          class="w-full bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium py-3 px-4 rounded-lg border border-gray-200 transition-colors"
                          @click="showUrlUpload = true"
                        >
                          从网址收藏
                        </button>
                      </div>

                      <!-- 右侧：信息输入表单 -->
                      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                        <PictureInfoForm
                          :picture-form-data="currentPictureForm"
                          :disabled="!currentPicture"
                          :success-callback="handleFormSuccess"
                        />
                      </div>
                    </div>
                  </a-tab-pane>
                  <a-tab-pane key="2" force-render tab="批量上传">
                    <PictureUploadBatch :space-id="spaceInfo.id" />
                  </a-tab-pane>
                </a-tabs>
              </CardContent>
              <!--        <CardFooter> Card Footer </CardFooter>-->
            </Card>

            <!-- URL 上传弹窗 -->
            <div
              v-if="showUrlUpload"
              class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
              @click="showUrlUpload = false"
            >
              <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4" @click.stop>
                <h3 class="text-lg font-semibold mb-4">从网址添加图片</h3>
                <PictureUploadUrl
                  :space-id="spaceInfo.id"
                  :picture="currentPicture"
                  :onSuccess="onUrlUploadSuccess"
                />
                <button
                  @click="showUrlUpload = false"
                  class="mt-4 w-full bg-gray-100 hover:bg-gray-200 text-gray-700 py-2 px-4 rounded-lg"
                >
                  取消
                </button>
              </div>
            </div>
          </DialogContent>
        </Dialog>

        <!--成员管理-->
        <Dialog>
          <DialogTrigger>
            <Button size="icon" variant="ghost"> <ShieldUser class="button" /> </Button
          ></DialogTrigger>
          <DialogContent
            class="sm:max-w-4/5 !min-h-[80vh] overflow-y-auto"
            style="max-height: 80vh"
          >
            <SpaceUserManagement :space-id="spaceInfo.id as string" />
          </DialogContent>
        </Dialog>

        <!--空间设置-->
        <Dialog>
          <DialogTrigger>
            <Button variant="ghost" size="icon"> <Settings class="button" /> </Button
          ></DialogTrigger>
          <DialogContent
            class="sm:max-w-4/5 !min-h-[80vh] overflow-y-auto"
            style="max-height: 80vh"
          >
            <Card class="border-none shadow-none">
              <CardHeader>
                <CardTitle class="text-3xl">Settings</CardTitle>
              </CardHeader>
              <CardContent>
                <p>
                  存储用量
                  {{ (((spaceInfo.totalSize || 0) / (spaceInfo.maxSize || 1)) * 100).toFixed(1) }}%
                </p>
                <p class="text-sm text-gray-400">
                  {{ formatSize(spaceInfo.totalSize || 0) }}/{{
                    formatSize(spaceInfo.maxSize || 0)
                  }}
                </p>
                <div class="flex">
                  <Progress
                    :model-value="((spaceInfo.totalSize || 0) / (spaceInfo.maxSize || 1)) * 100"
                  />
                </div>
              </CardContent>
              <!--        <CardFooter> Card Footer </CardFooter>-->
            </Card>
          </DialogContent>
        </Dialog>
      </div>
    </div>

    <div v-if="dataList.length == 0" class="self-center justify-self-center my-50">
      <h1 class="font-bold font-mono text-2xl text-gray-400">没有图片</h1>
    </div>

    <div v-if="dataList.length != 0" class="w-full">
      <PictureWaterfall :dataList="dataList" :loading="loading" @clickPicture="onClickPicture" />
      <!-- 滚动加载状态指示 -->
      <div class="load-more-status" v-if="dataList.length > 0">
        <div v-if="loadingMore" class="loading-indicator">
          <a-spin size="small" />
          <span style="margin-left: 8px">加载中...</span>
        </div>
        <div v-else-if="!hasMore" class="no-more-data">已加载全部内容</div>
      </div>
    </div>

    <!-- 图片详情弹窗 -->
    <PictureDetailDialog v-model:open="dialogOpen" :picture-id="selectedPictureId" />
  </div>
</template>

<script setup lang="ts" name="SpacePage">
import { Plus, Settings, ShieldUser } from 'lucide-vue-next'
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { listPictureTagCategoryUsingGet, listPictureVoByPageUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { Input } from '@/components/ui/input'
import { useRoute } from 'vue-router'
import { usePictureStore } from '@/stores/usePictureStore.ts'
import PictureWaterfall from '@/components/PictureWaterfall.vue'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { formatSize } from '@/utils'
import PictureUploadBatch from '@/components/upload/PictureUploadBatch.vue'
import PictureUpload from '@/components/upload/PictureUpload.vue'
import PictureUploadUrl from '@/components/upload/PictureUploadUrl.vue'
import PictureInfoForm from '@/components/PictureInfoForm.vue'
import SpaceUserManagement from '@/pages/space/SpaceUserManagement.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import PictureDetailDialog from '@/components/PictureDetailDialog.vue'

const activeKey = ref('1')
const route = useRoute()
const pictureStore = usePictureStore()

// 弹窗状态
const dialogOpen = ref(false)
const selectedPictureId = ref<string | number>()

// 上传相关状态
const currentPicture = ref<API.PictureVO>()
const currentPictureForm = reactive<API.PictureUpdateRequest>({})
const showUrlUpload = ref(false)

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 记录当前已加载的 spaceId，用于判断是否需要重新加载数据
const currentSpaceId = ref<string>('')

// 搜索条件
const searchParams = reactive({
  current: 1,
  pageSize: 30,
  sortField: 'createTime',
  sortOrder: 'descend',
  publicFlag: false,
  spaceId: route.params.spaceId as string,
} as Required<API.PictureQueryRequest>)

watch(
  () => route.params.keyword,
  () => {
    searchInputData.value = route.params.keyword as string
  },
)

// 监听路由 spaceId 变化，只在切换到不同 space 时重新加载数据
watch(
  () => route.params.spaceId,
  (newSpaceId) => {
    const spaceId = newSpaceId as string
    // 只有当 spaceId 真正发生变化且数据尚未加载时才重新加载
    if (spaceId && spaceId !== currentSpaceId.value) {
      currentSpaceId.value = spaceId
      // 更新搜索参数中的 spaceId
      searchParams.spaceId = spaceId
      searchParams.current = 1
      // 清空数据列表
      dataList.value = []
      // 重新获取空间信息和图片数据
      fetchSpaceInfo()
      fetchData()
    }
  },
)

// 监听图片状态变化，自动刷新数据
watch(
  () => pictureStore.refreshTrigger,
  () => {
    // 重置搜索参数并重新加载数据
    searchParams.current = 1
    fetchData()
  },
)

// 新增状态管理
const hasMore = ref(true) // 是否还有更多数据
const loadingMore = ref(false) // 是否正在加载更多

const onClickPicture = (picture: API.PictureVO) => {
  selectedPictureId.value = picture.id
  dialogOpen.value = true
}

// 上传相关处理函数
const onUploadSuccess = (newPicture: API.PictureVO) => {
  currentPicture.value = newPicture
  // 设置完整的表单数据
  currentPictureForm.id = newPicture.id
  currentPictureForm.name = newPicture.name
  currentPictureForm.introduction = newPicture.introduction
  currentPictureForm.category = newPicture.category
  currentPictureForm.tags = newPicture.tags
}

const onUrlUploadSuccess = (newPicture: API.PictureVO) => {
  onUploadSuccess(newPicture)
  showUrlUpload.value = false
}

const handleFormSuccess = () => {
  // 刷新图片列表
  searchParams.current = 1
  fetchData()
  // 重置表单状态
  currentPicture.value = undefined
  Object.keys(currentPictureForm).forEach((key) => {
    delete currentPictureForm[key as keyof API.PictureUpdateRequest]
  })
  message.success('图片保存成功')
}

// 获取数据
const fetchData = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  // 转换搜索参数
  const params = {
    ...searchParams,
    ...(searchInputData.value.trim() && { name: searchInputData.value.trim() }),
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // [true, false, false] => ['java']
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })

  try {
    const res = await listPictureVoByPageUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      const newData = res.data.data.records ?? []

      if (isLoadMore) {
        // 加载更多时追加数据
        dataList.value = [...dataList.value, ...newData]
      } else {
        // 首次加载或搜索时替换数据
        dataList.value = newData
      }

      total.value = res.data.data.total ?? 0

      // 判断是否还有更多数据
      hasMore.value = dataList.value.length < total.value
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    message.error('获取数据失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 滚动监听处理函数
const handleScroll = () => {
  // 获取文档的总高度、当前滚动位置和窗口高度
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  // 当滚动到距离底部200px以内时触发加载
  const threshold = 200
  const isNearBottom = scrollTop + windowHeight >= documentHeight - threshold

  if (isNearBottom && hasMore.value && !loadingMore.value && !loading.value) {
    loadMore()
  }
}

// 页面加载时处理初始数据加载和滚动监听
onMounted(() => {
  // 处理首次进入或路由参数变化的数据加载
  const spaceId = route.params.spaceId as string
  if (spaceId && spaceId !== currentSpaceId.value) {
    currentSpaceId.value = spaceId
    searchParams.spaceId = spaceId
    fetchSpaceInfo()
    fetchData()
  }

  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

// 页面卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 加载更多数据
const loadMore = async () => {
  if (!hasMore.value || loadingMore.value) {
    return
  }

  // 增加页码
  searchParams.current += 1
  await fetchData(true)
}

const searchInputData = ref('')

// 搜索
const doSearch = () => {
  console.log('search:', searchParams)
  dataList.value = []
  searchParams.current = 1
  fetchData()
  return
}

// 标签和分类列表
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

/**
 * 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

// 页面加载时获取标签分类选项，空间信息由路由监听器处理
onMounted(() => {
  getTagCategoryOptions()
})

const spaceInfo = ref<API.SpaceVO>({})
const fetchSpaceInfo = async () => {
  const spaceId = route.params.spaceId as string
  const rsp = await getSpaceVoByIdUsingGet({ id: spaceId })
  if (rsp.data.code === 0 && rsp.data.data) {
    spaceInfo.value = rsp.data.data
  }
}
</script>

<style scoped>

/* shadcn 风格的 tabs 样式 */
:deep(.ant-tabs) {
  border-bottom: 1px solid hsl(220 13% 91%);
  background: transparent;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 0;
  background: transparent;
}

:deep(.ant-tabs-nav::before) {
  border-bottom: none;
}

:deep(.ant-tabs-nav-wrap) {
  padding: 0;
}

:deep(.ant-tabs-nav-list) {
  position: relative;
}

:deep(.ant-tabs-tab) {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  border: none;
  background: transparent;
  color: #000000;
  margin: 0 4px;
}

:deep(.ant-tabs-tab:hover) {
  color: #000000;
  background-color: hsl(210 40% 98%);
}


:deep(.ant-tabs-tab-active) {
  background-color: hsl(0 0% 100%);
  color: #000000;
  border: 1px solid hsl(220 13% 91%);
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #000000 !important;
}

:deep(.ant-tabs-tab:focus-visible) {
  outline: none;
  box-shadow: 0 0 0 2px hsl(222.2 84% 4.9%);
}

:deep(.ant-tabs-tab-btn) {
  color: #A6A6A6 !important;
  transition: inherit;
}

:deep(.ant-tabs-ink-bar) {
  display: none;
}

:deep(.ant-tabs-content-holder) {
  margin-top: 8px;
}

:deep(.ant-tabs-tabpane) {
  outline: none;
}

.load-more-status {
  text-align: center;
  margin: 24px 0;
  padding: 16px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.no-more-data {
  color: #999;
  font-size: 14px;
}

.button {
  width: calc(var(--spacing) * 6) /* 1.5rem = 24px */;
  height: calc(var(--spacing) * 6) /* 1.5rem = 24px */;
  cursor: pointer;
  color: #4a5565;
}
</style>
