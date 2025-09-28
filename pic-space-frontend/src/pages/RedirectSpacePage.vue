<template>
  <div id="addSpacePage">
    <p>正在跳转</p>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// check login
const checkSpace = async ()=>{
  const loginUser = loginUserStore.loginUser
  if (!loginUser || !loginUser.id) {
    router.replace('/login')
    return
  }
  const response = await listSpaceByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize:1
  })
  if(response.data.code === 0){
    if(response.data.data?.records?.length>0){
      const space = response.data.data?.records[0]
      router.replace(`/space/${space?.id}`)
    }else {
      router.replace('/add-space')
    }
  }else {
    message.error("获取空间失败"+response.data.message)
  }

}

onMounted(()=>{
  checkSpace()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
