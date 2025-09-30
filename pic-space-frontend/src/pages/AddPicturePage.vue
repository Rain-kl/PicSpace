<template>
  <div class="">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!-- 根据是否有图片决定布局 -->
        <div v-if="!picture">
          <!-- 无图片时：正常显示上传组件 -->
          <PictureUpload :picture="picture" :onSuccess="onSuccess" />
        </div>
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
          <!-- 有图片时：左侧显示上传组件（缩小） -->
          <div>
            <PictureUpload :picture="picture" :onSuccess="onSuccess" />
          </div>
          <!-- 右侧显示编辑表单 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
            <UpdatePicture :picture-form-data="pictureForm" />
          </div>
        </div>
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <!-- 根据是否有图片决定布局 -->
        <div v-if="!picture">
          <!-- 无图片时：正常显示上传组件 -->
          <PictureUploadUrl :picture="picture" :onSuccess="onSuccess" />
        </div>
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
          <!-- 有图片时：左侧显示上传组件（缩小） -->
          <div>
            <PictureUploadUrl :picture="picture" :onSuccess="onSuccess" />
          </div>
          <!-- 右侧显示编辑表单 -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
            <UpdatePicture :picture-form-data="pictureForm" />
          </div>
        </div>
      </a-tab-pane>
      <a-tab-pane
        key="batch"
        tab="批量上传"
        force-render
        v-if="loginUserStore.loginUser.userRole === 'admin'"
      >
        <PictureUploadBatch />
      </a-tab-pane>
      <a-tab-pane
        key="crawl"
        tab="图片爬取"
        force-render
        v-if="loginUserStore.loginUser.userRole === 'admin'"
      >
        <PictureCrawler />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/upload/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute } from 'vue-router'
import PictureUploadUrl from '@/components/upload/PictureUploadUrl.vue'
import PictureCrawler from '@/components/upload/PictureCrawler.vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import PictureUploadBatch from '@/components/upload/PictureUploadBatch.vue'
import UpdatePicture from '@/components/UpdatePicture.vue'

const loginUserStore = useLoginUserStore()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureUpdateRequest>({})
const uploadType = ref<'file' | 'url'>('file')

const route = useRoute()

/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  // 设置完整的表单数据
  pictureForm.id = newPicture.id
  pictureForm.name = newPicture.name
  pictureForm.introduction = newPicture.introduction
  pictureForm.category = newPicture.category
  pictureForm.tags = newPicture.tags
}

const categoryOptions = ref<{ value: string; label: string }[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])
/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

// 获取老数据
const getOldPicture = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.id = data.id
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
  getTagCategoryOptions()
})
</script>

<style scoped></style>
