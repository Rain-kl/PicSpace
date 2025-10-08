<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-section">
      <SearchBar
        v-model="searchInputData"
        placeholder="搜索图片..."
        variant="glass"
        size="lg"
        @search="doSearch"
      />
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 分类筛选 -->
      <div class="filter-section">
        <CategoryFilter
          :categories="categoryOptions"
          v-model:selected-category="selectedCategory"
          variant="glass"
          size="md"
          @change="doFilter"
        />
      </div>

      <!-- 图片列表或空状态 -->
      <div class="picture-section">
        <EmptyState
          v-if="!loading && dataList.length === 0"
          title="没有找到图片"
          description="试试调整搜索条件或浏览其他分类"
          :icon="ImageOff"
        >
          <template #action>
            <Button variant="primary" size="sm" @click="resetFilters"> 重置筛选 </Button>
          </template>
        </EmptyState>

        <PictureWaterfall
          v-else
          :dataList="dataList"
          :loading="loading"
          @clickPicture="onClickPicture"
        />
      </div>

      <!-- 加载状态指示器 -->
      <LoadingIndicator
        v-if="dataList.length > 0"
        type="loadMore"
        :loading="loadingMore"
        :has-more="hasMore"
        variant="glass"
        size="md"
      />
    </div>

    <!-- 图片详情弹窗 -->
    <PictureDetailDialog v-model:open="dialogOpen" :picture-id="selectedPictureId" />
  </div>
</template>

<script setup lang="ts" name="HomePage">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ImageOff } from 'lucide-vue-next'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import { usePictureStore } from '@/stores/usePictureStore.ts'
import PictureWaterfall from '@/components/PictureWaterfall.vue'
import PictureDetailDialog from '@/components/PictureDetailDialog.vue'
import {
  Button,
  CategoryFilter,
  EmptyState,
  LoadingIndicator,
  SearchBar,
} from '@/components/primary-ui'

const route = useRoute()
const pictureStore = usePictureStore()

// 弹窗状态
const dialogOpen = ref(false)
const selectedPictureId = ref<string | number>()

const onClickPicture = (picture: API.PictureVO) => {
  selectedPictureId.value = picture.id
  dialogOpen.value = true
}

watch(
  () => route.params.keyword,
  () => {
    searchInputData.value = route.params.keyword as string
  },
)

// 监听图片状态变化，自动刷新数据
watch(
  () => pictureStore.refreshTrigger,
  () => {
    console.log('refreshTrigger changed:', pictureStore.refreshTrigger)
    // 重置搜索参数并重新加载数据
    searchParams.current = 1
    fetchData()
  },
)

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive({
  current: 1,
  pageSize: 30,
  sortField: 'createTime',
  sortOrder: 'descend',
} as Required<API.PictureQueryRequest>)

// 新增状态管理
const hasMore = ref(true) // 是否还有更多数据
const loadingMore = ref(false) // 是否正在加载更多

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

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
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
  if (
    searchInputData.value.trim() === '' &&
    selectedCategory.value === 'all' &&
    !selectedTagList.value.includes(true)
  ) {
    searchParams.current = 1
    router.push('/')
    fetchData()
    return
  }

  router.push('/search/' + searchInputData.value)
  searchParams.current = 1
  fetchData()
  return
}

// 筛选
const doFilter = (categoryKey?: string) => {
  console.log('doFilter called with:', categoryKey)
  console.log('Current selectedCategory:', selectedCategory.value)
  console.log('searchParams:', searchParams)

  dataList.value = []
  searchParams.current = 1
  fetchData()
}

// 标签和分类列表
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 分类选项（为CategoryFilter组件准备）
const categoryOptions = computed(() => {
  const options = [{ key: 'all', label: '全部' }]
  categoryList.value.forEach((category) => {
    options.push({ key: category, label: category })
  })
  return options
})

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

// 重置筛选条件
const resetFilters = () => {
  searchInputData.value = ''
  selectedCategory.value = 'all'
  selectedTagList.value = []
  searchParams.current = 1
  router.push('/')
  fetchData()
}

// 跳转至图片详情页
onMounted(() => {
  getTagCategoryOptions()
})
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
}

.search-section {
  position: sticky;
  top: 0;
  z-index: 20;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.5);
  transition: all 0.2s ease-out;
}

.dark .search-section {
  background: rgba(17, 24, 39, 0.8);
  border-bottom-color: rgba(75, 85, 99, 0.5);
}

.content-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1.5rem 1rem;
}

.filter-section {
  width: 100%;
}

.picture-section {
  width: 100%;
  min-height: 400px;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .content-section {
    padding: 1rem 0.75rem;
    gap: 1rem;
  }
}

/* 大屏幕适配 */
@media (min-width: 1024px) {
  .content-section {
    padding: 2rem;
    gap: 2rem;
    margin: 0 auto;
  }
}

/* 平滑滚动 */
@media (prefers-reduced-motion: no-preference) {
  html {
    scroll-behavior: smooth;
  }
}

/* 圆角优化 */
.search-section {
  border-radius: 0 0 1.5rem 1.5rem;
}
</style>
