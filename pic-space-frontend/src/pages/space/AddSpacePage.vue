<template>
  <div class="flex h-full w-full items-center justify-center px-4">
    <div class="mx-auto max-w-lg w-full">
      <!-- 空间表单卡片 -->
      <Card>
        <CardContent class="pt-0">
          <CardTitle class="text-xl">
            {{ route.query?.id ? '修改空间' : '创建空间' }}
          </CardTitle>
          <CardDescription class="text-sm">
            {{ route.query?.id ? '编辑空间信息' : '创建您的新空间' }}
          </CardDescription>
          <a-divider />
          <form @submit.prevent="handleSubmit" class="grid gap-3">
            <div class="grid gap-1.5">
              <Label for="spaceName" class="text-sm">空间名称</Label>
              <Input
                id="spaceName"
                v-model="spaceForm.spaceName"
                type="text"
                placeholder="请输入空间名称"
                class="h-9"
                required
              />
            </div>
            <div class="grid gap-1.5">
              <div class="flex mt-3">
                <Label for="spaceLevel" class="text-sm">空间级别</Label>
              </div>
              <Select v-model="spaceForm.spaceLevel" required>
                <SelectTrigger class="h-9">
                  <SelectValue placeholder="请选择空间级别" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="option in SPACE_LEVEL_OPTIONS"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
              <div class="text-sm">
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="ghost" class="h-9 text-sm">
                      <svg
                        class="w-4 h-4 mr-1.5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                        ></path>
                      </svg>
                      查看空间级别介绍
                    </Button>
                  </DialogTrigger>
                  <DialogContent class="max-w-md">
                    <DialogHeader>
                      <DialogTitle class="text-lg">空间级别介绍</DialogTitle>
                      <DialogDescription class="text-sm">
                        不同级别空间的存储限制和功能说明
                      </DialogDescription>
                    </DialogHeader>
                    <div class="space-y-3 mt-4">
                      <div
                        class="p-3 bg-blue-50 dark:bg-blue-950 rounded-md border border-blue-200 dark:border-blue-800"
                      >
                        <p class="text-sm text-blue-800 dark:text-blue-200">
                          <strong>免费空间:</strong> 1G 存储空间，100
                          张图片，适合个人用户使用。如需升级，请联系管理员。
                        </p>
                      </div>
                      <div
                        v-for="spaceLevel in spaceLevelList"
                        :key="spaceLevel.text"
                        class="p-3 bg-gray-50 dark:bg-gray-900 rounded-md border"
                      >
                        <p class="text-sm text-gray-700 dark:text-gray-300">
                          <strong>{{ spaceLevel.text }}:</strong>
                          存储大小 {{ formatSize(spaceLevel.maxSize) }}， 最多
                          {{ spaceLevel.maxCount }} 张图片
                        </p>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
            <a-divider />

            <Button :disabled="loading" type="submit" class="w-full h-9 !text-white mt-2">
              <span v-if="loading" class="mr-1.5">
                <svg class="animate-spin h-3.5 w-3.5" viewBox="0 0 24 24">
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
              </span>
              {{ route.query?.id ? '保存修改' : '创建空间' }}
            </Button>
            <!-- 空间级别介绍按钮 -->
            <Button variant="ghost" class="h-9 text-sm" @click="router.back()"> 返回 </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { addSpaceUsingPost, listSpaceLevelUsingGet } from '@/api/spaceController.ts'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { getSpaceByIdUsingGet, updateSpaceUsingPost } from '@/api/spaceControllerAdmin.ts'

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
  if (rsp.data.code === 0 && rsp.data.data) {
    spaceLevelList.value = rsp.data.data
  } else {
    message.error('获取空间级别失败，' + rsp.data.message)
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 验证必填字段
  if (!spaceForm.spaceName || spaceForm.spaceLevel === undefined) {
    message.error('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const id = route.query?.id
    if (id) {
      const res = await updateSpaceUsingPost({
        id: Number(id),
        spaceName: spaceForm.spaceName,
        spaceLevel: spaceForm.spaceLevel,
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
        spaceName: spaceForm.spaceName,
        spaceLevel: spaceForm.spaceLevel,
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
    const res = await getSpaceByIdUsingGet({
      id: Number(id),
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

<style scoped></style>
