<template>
  <div class="min-h-screen pt-4 px-2 sm:px-0">
    <div class="max-w-7xl mx-auto">
      <h2 class="text-2xl font-bold text-gray-900 mb-8">
        {{ route.query?.id ? '修改图片' : '创建图片' }}
      </h2>
      <a-divider />

      <!-- 主要内容区域：左右布局 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- 左侧：上传区域 -->
        <div class="flex flex-col space-y-4">
          <PictureUpload :picture="picture" :onSuccess="onSuccess" />

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
            :picture-form-data="pictureForm"
            :disabled="!picture"
            :success-callback="handleFormSuccess"
          />
        </div>
      </div>

      <!-- URL 上传弹窗 -->
      <div
        v-if="showUrlUpload"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      >
        <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4">
          <h3 class="text-lg font-semibold mb-4">从网址添加图片</h3>
          <PictureUploadUrl :picture="picture" :onSuccess="onUrlSuccess" />
          <button
            @click="showUrlUpload = false"
            class="mt-4 w-full bg-gray-100 hover:bg-gray-200 text-gray-700 py-2 px-4 rounded-lg"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/upload/PictureUpload.vue'
import PictureUploadUrl from '@/components/upload/PictureUploadUrl.vue'
import PictureInfoForm from '@/components/PictureInfoForm.vue'
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureUpdateRequest>({})
const showUrlUpload = ref(false)

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

/**
 * URL 上传成功回调
 */
const onUrlSuccess = (newPicture: API.PictureVO) => {
  onSuccess(newPicture)
  showUrlUpload.value = false
}

/**
 * 表单提交成功回调
 */
const handleFormSuccess = () => {
  router.push({
    path: `/picture/${pictureForm.id}`,
  })
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
  if (id && typeof id === 'string') {
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
