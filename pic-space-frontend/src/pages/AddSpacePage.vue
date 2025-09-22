<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改空间' : '创建空间' }}
    </h2>
    <!-- 空间信息表单 -->
    <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">
      <a-form-item name="spaceName" label="名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          placeholder="请选择空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          allow-clear
          style="min-width: 180px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%"
          >创建</a-button
        >
      </a-form-item>
    </a-form>

    <a-card title="空间级别介绍">
      <a-typography-paragraph>
         免费空间: 1G 存储空间，100 张图片，适合个人用户使用。如需升级，请联系管理员。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList" :key="spaceLevel.text">
        {{ spaceLevel.text}}: 大小 {{formatSize(spaceLevel.maxSize)}}, 数量 {{ spaceLevel.maxCount}} 张图片
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet, listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'

const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest>({
  spaceName: '',
  spaceLevel: undefined,
})
const router = useRouter()
const route = useRoute()
const loading = ref(false)

const spaceLevelList = ref<API.SpaceLevel[]>([])
const fetchSpaceLevelList = async () => {
  const rsp = await listSpaceLevelUsingGet()
  if(rsp.data.code === 0 && rsp.data.data) {
    spaceLevelList.value = rsp.data.data
  }else {
    message.error('获取空间级别失败，' + rsp.data.message)
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  loading.value = true
  try {
    const id = route.query?.id
    if (id) {
      const res = await updateSpaceUsingPost({
        id,
        ...values,
      })
      if (res.data.code === 0 && res.data.data) {
        message.success('修改成功')
        router.push({
          path: `/space/${id}`,
        })
      } else {
        message.error('修改失败，' + res.data.message)
      }
    } else {
      const res = await addSpaceUsingPost({
        ...values,
      })
      // 操作成功
      if (res.data.code === 0 && res.data.data) {
        message.success('创建成功')
        // 跳转到空间详情页
        router.push({
          path: `/space/${res.data.data}`,
        })
      } else {
        message.error('创建失败，' + res.data.message)
      }
    }
  } catch (error) {
    const id = Number(route.query?.id)
    message.error(`${id ? '修改' : '创建'}失败，请稍后重试`)
  } finally {
    loading.value = false
  }
}

// 获取老数据
const getOldSpace = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}

onMounted(() => {
  fetchSpaceLevelList()
  getOldSpace()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
