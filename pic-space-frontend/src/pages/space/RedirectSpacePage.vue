<template>
  <div>
    <p v-if="spaceList.length === 0 && teamSpaceList.length === 0">正在跳转</p>
    <div v-if="spaceList.length !== 0 || teamSpaceList.length !== 0">
      <ListSpacePage :space-list="spaceList" :team-space-list="teamSpaceList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceUsingPost } from '@/api/spaceController.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'
import ListSpacePage from '@/pages/space/ListSpacePage.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const spaceList = ref<API.SpaceVO[]>([])
const teamSpaceList = ref<API.SpaceVO[]>([])

// check login and load spaces
const checkSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser || !loginUser.id) {
    router.replace('/login')
    return
  }

  try {
    // 获取我的空间
    const response = await listSpaceUsingPost()
    console.log('我的空间:', response.data.data)
    if (response.data.code === 0 && response.data.data) {
      spaceList.value = response.data.data
    }

    // 获取我加入的团队空间
    const teamResponse = await listMyTeamSpaceUsingPost()
    console.log('团队空间:', teamResponse.data.data)
    if (teamResponse.data.code === 0 && teamResponse.data.data) {
      // 过滤数据：admin角色的空间归入"我的空间"，其他角色的归入"团队空间"
      const adminSpaces: API.SpaceVO[] = []
      const memberSpaces: API.SpaceVO[] = []

      teamResponse.data.data.forEach((spaceUser: API.SpaceUserVO) => {
        if (spaceUser.space) {
          if (spaceUser.spaceRole === 'admin') {
            // 检查是否已经存在于我的空间中，避免重复
            const existsInMySpaces = spaceList.value.some(
              (space) => space.id === spaceUser.space?.id,
            )
            if (!existsInMySpaces) {
              adminSpaces.push(spaceUser.space)
            }
          } else {
            memberSpaces.push(spaceUser.space)
          }
        }
      })

      // 将admin角色的空间添加到"我的空间"
      spaceList.value = [...spaceList.value, ...adminSpaces]
      // 只有非admin角色的空间才算作"团队空间"
      teamSpaceList.value = memberSpaces
    }

    // 如果没有任何空间，跳转到创建空间页面
    if (spaceList.value.length === 0 && teamSpaceList.value.length === 0) {
      router.replace('/add-space')
    }
  } catch (error) {
    message.error('获取空间失败')
    console.error('获取空间失败:', error)
  }
}

onMounted(() => {
  checkSpace()
})
</script>

<style scoped></style>
