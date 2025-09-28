<template>
  <Waterfall
    :list="imageList"
    :gutter="20"
    :breakpoints="{
      1200: { rowPerView: 6 },
      2000: { rowPerView: 6 },
      800: { rowPerView: 2 },
      500: { rowPerView: 2 },
    }"
    :animationDuration="0"
    :animation="false"
  >
    <!-- v2.6.0之前版本插槽数据获取 -->
    <!-- <template #item="{ item, url, index }"> -->
    <!-- 新版插槽数据获取 -->
    <template #default="{ item, url, index }">
      <div class="card cursor-pointer" @click="doClickPicture(item)">
        <LazyImg id="{{index}}" :url="url" class="rounded-2xl" />
      </div>
    </template>
  </Waterfall>
</template>

<script setup lang="ts">
import { LazyImg, Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  dataList: API.PictureVO[]
  loading: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
})

// 将 PictureVO 数据转换为瀑布流组件需要的格式
const imageList = computed(() => {
  return props.dataList.map((picture) => ({
    ...picture,
    src: picture.url, // 将 url 字段映射为 src 字段
  }))
})

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}
</script>

<style scoped>
.card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
</style>
