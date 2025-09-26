<template>
  <div class="flex container flex-col justify-center items-center w-full">
    <!-- 搜索框 -->
    <!--    <SearchBar/>-->

    <div
      class="sticky z-10 top-0 w-full h-[70px] bg-white shadow-none border-gray-200  flex items-center mb-2"
    >
      <!--    <div class="w-full h-15">-->

      <Input
        id="search"
        type="text"
        placeholder="搜索..."
        class="w-full h-[50px] border-0 rounded-md bg-[#EFEFEF]"
        v-model="searchInputData"
        @keydown.enter="doSearch()"
      />
    </div>

    <div class="w-full">
      <!-- 分类和标签筛选 -->
      <a-tabs v-model:active-key="selectedCategory" @change="doFilter" class="self-start">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane v-for="category in categoryList" :tab="category" :key="category" />
      </a-tabs>
<!--      <div class="mb-4">-->
<!--        <span style="margin-right: 8px">标签：</span>-->
<!--        <a-space :size="[0, 8]" wrap>-->
<!--          <a-checkable-tag-->
<!--            v-for="(tag, index) in tagList"-->
<!--            :key="tag"-->
<!--            v-model:checked="selectedTagList[index]"-->
<!--            @change="doFilter"-->
<!--          >-->
<!--            {{ tag }}-->
<!--          </a-checkable-tag>-->
<!--        </a-space>-->
<!--      </div>-->
      <!--          <PictureWaterfall :dataList="dataList" :loading="loading" />-->
      <PictureList :dataList="dataList" :loading="loading" />

      <!-- 滚动加载状态指示 -->
      <div class="load-more-status" v-if="dataList.length > 0">
        <div v-if="loadingMore" class="loading-indicator">
          <a-spin size="small" />
          <span style="margin-left: 8px">加载中...</span>
        </div>
        <div v-else-if="!hasMore" class="no-more-data">已加载全部内容</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="HomePage">
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { Input } from '@/components/ui/input'
import router from '@/router'
import PictureList from '@/components/PictureList.vue'
import { useRoute } from 'vue-router'
const route = useRoute()
watch(
  () => route.params.keyword,
  () => {
    searchInputData.value = route.params.keyword as string
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

// 搜索
const doFilter = () => {
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

// 跳转至图片详情页
onMounted(() => {
  getTagCategoryOptions()
})
</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 16px;
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
</style>
