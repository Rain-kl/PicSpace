<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 120px)"
        placeholder="请输入图片地址"
      />
      <a-button type="primary" style="width: 120px" :loading="loading" @click="handleUpload">
        提交
      </a-button>
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController.ts'
import { usePictureStore } from '@/stores/usePictureStore.ts'

interface Props {
  picture?: API.PictureVO
  pictureFormData?: Partial<API.PictureUploadRequest> | null
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()
const fileUrl = ref<string>('')
const loading = ref<boolean>(false)

// keep input in sync with incoming data so edit forms show existing values
watch(
  () => [props.pictureFormData?.fileUrl, props.picture?.url],
  ([formFileUrl, pictureUrl]) => {
    const nextValue = formFileUrl ?? pictureUrl ?? ''
    if (nextValue !== fileUrl.value) {
      fileUrl.value = nextValue
    }
  },
  { immediate: true },
)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { fileUrl: fileUrl.value }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    const errMsg = error instanceof Error ? error.message : String(error)
    message.error('图片上传失败，' + errMsg)
  }
  loading.value = false
  usePictureStore().triggerRefresh()
}
</script>
<style scoped>
.url-picture-upload {
  margin-bottom: 16px;
}

.url-picture-upload img {
  max-width: 100%;
  max-height: 480px;
}

.url-picture-upload .img-wrapper {
  text-align: center;
  margin-top: 16px;
}
</style>
