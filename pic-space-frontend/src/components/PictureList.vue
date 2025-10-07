<template>
  <div id="pictureList">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="props.dataList"
      :loading="props.loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>

    <!-- 图片详情弹窗 -->
    <PictureDetailDialog v-model:open="dialogOpen" :picture-id="selectedPictureId" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PictureDetailDialog from './PictureDetailDialog.vue'

interface Props {
  dataList: API.PictureVO[]
  loading: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
})

// 弹窗状态
const dialogOpen = ref(false)
const selectedPictureId = ref<string | number>()

// 点击图片打开详情弹窗
const doClickPicture = (picture: API.PictureVO) => {
  selectedPictureId.value = picture.id
  dialogOpen.value = true
}
</script>

<style scoped></style>
