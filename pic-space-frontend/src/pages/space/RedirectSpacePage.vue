<template>
  <div>
    <p v-if="spaceList.length === 0">正在跳转</p>
    <div v-if="spaceList.length !== 0">
      <ListSpacePage :space-list="spaceList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'
import ListSpacePage from '@/pages/space/ListSpacePage.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const spaceList = ref<API.SpaceVO[]>([])

// check login
const checkSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser || !loginUser.id) {
    router.replace('/login')
    return
  }
  const response = await listSpaceUsingPost()
  console.log(response.data.data)
  if (response.data.code === 0) {
    if (response.data.data?.length > 0) {
      spaceList.value = response.data.data
    } else {
      router.replace('/add-space')
    }
  } else {
    message.error('获取空间失败' + response.data.message)
  }
}

onMounted(() => {
  checkSpace()
})
</script>

<style scoped></style>
