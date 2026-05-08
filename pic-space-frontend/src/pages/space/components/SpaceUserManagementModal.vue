<template>
  <div class="space-user-management-modal">
    <Dialog :open="open" @update:open="$emit('update:open', $event)">
      <DialogContent class="user-management-content" :class="modalClasses">
        <div class="user-management-container" :class="containerClasses">
          <!-- 头部 -->
          <div class="user-management-header">
            <h2 class="user-management-title" :class="titleClasses">
              <Users :class="titleIconClasses" />
              成员管理
            </h2>
          </div>

          <!-- 内容区域 -->
          <div class="user-management-content-area">
            <Card class="border-none shadow-none">
              <CardContent>
                <!-- 添加成员按钮 - 仅管理员可见 -->
                <div v-if="isAdmin" class="mb-4">
                  <Button variant="outline" @click="doOpenAddDialog">添加成员</Button>
                </div>
                <a-divider />
                <a-table :columns="columns" :dataSource="dataSource">
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.dataIndex === 'spaceRole'">
                      <a-space wrap>
                        <a-tag :color="SPACE_ROLE_STYLE_MAP[record.spaceRole].aTagColor">
                          {{ SPACE_ROLE_STYLE_MAP[record.spaceRole].name }}
                        </a-tag>
                      </a-space>
                    </template>

                    <template v-if="column.dataIndex === 'createTime'">
                      {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                    </template>

                    <template v-else-if="column.key === 'action'">
                      <a-space wrap>
                        <Button variant="outline" @click="doEdit(record)"> 编辑 </Button>
                        <a-button danger @click="doDelete(record.id)">移除</a-button>
                      </a-space>
                    </template>
                  </template>
                </a-table>
              </CardContent>
              <!--        <CardFooter> Card Footer </CardFooter>-->
            </Card>

            <!-- 编辑对话框 -->
            <Dialog v-model:open="isEditDialogOpen">
              <DialogContent class="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>编辑成员</DialogTitle>
                  <DialogDescription> 修改成员的空间角色 </DialogDescription>
                </DialogHeader>
                <div v-if="editingUser" class="grid gap-4 py-4">
                  <div class="grid grid-cols-4 items-center gap-4">
                    <label class="text-right">用户名</label>
                    <span class="col-span-3">{{ editingUser.user?.userName }}</span>
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <label class="text-right">账号</label>
                    <span class="col-span-3">{{ editingUser.user?.userAccount }}</span>
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <label class="text-right">空间角色</label>
                    <Select v-model="editingUser.spaceRole" class="col-span-3">
                      <SelectTrigger>
                        <SelectValue placeholder="选择角色" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="admin">管理员</SelectItem>
                        <SelectItem value="editor">编辑</SelectItem>
                        <SelectItem value="user">用户</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter>
                  <Button variant="outline" @click="doCancelEdit">取消</Button>
                  <Button class="!text-white" @click="doSaveEdit">保存</Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>

            <!-- 添加成员对话框 -->
            <Dialog v-model:open="isAddDialogOpen">
              <DialogContent class="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>添加成员</DialogTitle>
                  <DialogDescription> 通过用户账号添加新成员到空间 </DialogDescription>
                </DialogHeader>
                <div class="grid gap-4 py-4">
                  <div class="grid grid-cols-4 items-center gap-4">
                    <label class="text-right">用户账号</label>
                    <Input
                      v-model="newUser.userAccount"
                      class="col-span-3"
                      placeholder="请输入用户账号"
                    />
                  </div>
                  <div class="grid grid-cols-4 items-center gap-4">
                    <label class="text-right">空间角色</label>
                    <Select v-model="newUser.spaceRole" class="col-span-3">
                      <SelectTrigger>
                        <SelectValue placeholder="选择角色" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="admin">管理员</SelectItem>
                        <SelectItem value="editor">编辑</SelectItem>
                        <SelectItem value="user">用户</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter>
                  <Button variant="outline" @click="doCancelAdd">取消</Button>
                  <Button @click="doAddUser">添加</Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Users } from 'lucide-vue-next'
import { useThemeStore } from '@/stores/useThemeStore.ts'

import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  getSpaceUserUsingPost,
  listSpaceUserUsingPost
} from '@/api/spaceUserController.ts'

import { message } from 'ant-design-vue'
import { getUserListPageUsingPost } from '@/api/userController.ts'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { SPACE_ROLE_STYLE_MAP } from '@/constants/space.ts'
import dayjs from 'dayjs'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

export interface SpaceUserManagementModalProps {
  open: boolean
  spaceId: string
  variant?: 'default' | 'glass'
  size?: string
}

const props = withDefaults(defineProps<SpaceUserManagementModalProps>(), {
  variant: 'glass',
  size: 'min-w-2/3 ',
})

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const themeStore = useThemeStore()

// 响应式主题类
const themeClasses = computed(() => ({
  text: {
    primary: themeStore.isDark ? 'text-white' : 'text-gray-900',
    secondary: themeStore.isDark ? 'text-gray-300' : 'text-gray-600',
  },
  bg: {
    glass: themeStore.isDark
      ? 'bg-gray-900/80 backdrop-blur-xl border-gray-700/50'
      : 'bg-white/80 backdrop-blur-xl border-gray-200/50',
    default: themeStore.isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200',
  },
}))

// 模态框样式
const modalClasses = computed(() => [
  'overflow-hidden',
  'border rounded-3xl shadow-2xl',
  props.variant === 'glass' ? 'backdrop-blur-xl' : '',
  props.size,
])

// 容器样式
const containerClasses = computed(() => [
  'p-8 space-y-6',
  props.variant === 'glass' ? themeClasses.value.bg.glass : themeClasses.value.bg.default,
])

// 标题样式
const titleClasses = computed(() => [
  'text-3xl font-bold flex items-center gap-3 transition-colors duration-200',
  themeClasses.value.text.primary,
])

const titleIconClasses = computed(() => [
  'w-8 h-8 transition-colors duration-200',
  themeClasses.value.text.secondary,
])



const isAdmin = ref(false)
const isEditDialogOpen = ref(false)
const editingUser = ref<API.SpaceUserVO | null>(null)
const isAddDialogOpen = ref(false)
const newUser = ref({
  userAccount: '',
  spaceRole: 'user',
})

onMounted(() => {
  fetchSpaceMemberList()
  checkAdmin()
})

const checkAdmin = async () => {
  try {
    const userStore = useLoginUserStore()
    const userId = userStore.loginUser.id
    const res = await getSpaceUserUsingPost({
      spaceId: props.spaceId,
      userId: userId,
    })
    if (res.data.code === 0 && res.data.data) {
      isAdmin.value = res.data.data.spaceRole === 'admin'
    } else {
      message.error('检查管理员权限失败')
    }
    console.log(userId, isAdmin.value)
  } catch (error: any) {
    message.error(`获取用户权限出错：${error.message || '网络请求失败'}`)
    console.error('检查管理员权限出错:', error)
  }
}

const fetchSpaceMemberList = async () => {
  try {
    console.log(props.spaceId)
    const params = {
      spaceId: props.spaceId,
    }
    const res = await listSpaceUserUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      dataSource.value = res.data.data
    } else {
      message.error(`获取成员列表失败：${res.data.message || '未知错误'}`)
    }
  } catch (error: any) {
    message.error(`获取成员列表出错：${error.message || '网络请求失败'}`)
    console.error('获取成员列表出错:', error)
  }
}

const dataSource = ref<API.SpaceUserVO[]>([])

const baseColumns = [
  {
    title: '用户名',
    dataIndex: ['user', 'userName'], // 嵌套字段写法
    key: 'userName',
  },
  {
    title: '账号',
    dataIndex: ['user', 'userAccount'],
    key: 'userAccount',
  },
  {
    title: '空间角色',
    dataIndex: 'spaceRole',
    key: 'spaceRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
]

const columns = computed(() => {
  const cols = [...baseColumns] as any[]
  if (isAdmin.value) {
    cols.push({
      title: '操作',
      key: 'action',
      width: 240,
      fixed: 'right' as const,
    })
  }
  return cols
})

const doOpenAddDialog = () => {
  newUser.value = {
    userAccount: '',
    spaceRole: 'user',
  }
  isAddDialogOpen.value = true
}

const doAddUser = async () => {
  if (!newUser.value.userAccount.trim()) {
    message.error('请输入用户账号')
    return
  }

  try {
    // 先通过用户账号查找用户
    const userQueryRes = await getUserListPageUsingPost({
      userAccount: newUser.value.userAccount,
      current: 1,
      pageSize: 1,
    })

    if (userQueryRes.data.code !== 0 || !userQueryRes.data.data?.records?.length) {
      message.error('用户不存在，请检查用户账号')
      return
    }

    const userId = userQueryRes.data.data.records[0].id

    // 添加用户到空间
    const res = await addSpaceUserUsingPost({
      spaceId: props.spaceId,
      userId: userId,
      spaceRole: newUser.value.spaceRole,
    })

    if (res.data.code === 0) {
      message.success('添加成员成功')
      isAddDialogOpen.value = false
      newUser.value = {
        userAccount: '',
        spaceRole: 'user',
      }
      // 添加成功后重新获取列表
      await fetchSpaceMemberList()
    } else {
      message.error(`添加成员失败：${res.data.message || '未知错误'}`)
    }
  } catch (error: any) {
    message.error(`添加成员出错：${error.message || '网络请求失败'}`)
    console.error('添加成员出错:', error)
  }
}

const doCancelAdd = () => {
  isAddDialogOpen.value = false
  newUser.value = {
    userAccount: '',
    spaceRole: 'user',
  }
}

const doEdit = (record: API.SpaceUserVO) => {
  editingUser.value = { ...record }
  isEditDialogOpen.value = true
}

const doSaveEdit = async () => {
  if (!editingUser.value) return

  try {
    const res = await editSpaceUserUsingPost({
      id: editingUser.value.id,
      spaceRole: editingUser.value.spaceRole,
    })
    if (res.data.code === 0) {
      message.success('编辑成员成功')
      isEditDialogOpen.value = false
      editingUser.value = null
      // 编辑成功后重新获取列表
      await fetchSpaceMemberList()
    } else {
      message.error(`编辑成员失败：${res.data.message || '未知错误'}`)
    }
  } catch (error: any) {
    message.error(`编辑成员出错：${error.message || '网络请求失败'}`)
    console.error('编辑成员出错:', error)
  }
}

const doCancelEdit = () => {
  isEditDialogOpen.value = false
  editingUser.value = null
}

const doDelete = async (spaceUserId: string) => {
  // 添加确认对话框
  const confirmed = confirm('确定要移除该成员吗？此操作不可撤销。')
  if (!confirmed) {
    return
  }

  try {
    const res = await deleteSpaceUserUsingPost({ id: spaceUserId })
    if (res.data.code === 0) {
      message.success('移除成员成功')
      // 删除成功后重新获取列表
      await fetchSpaceMemberList()
    } else {
      message.error(`移除成员失败：${res.data.message || '未知错误'}`)
    }
  } catch (error: any) {
    message.error(`移除成员出错：${error.message || '网络请求失败'}`)
    console.error('删除成员出错:', error)
  }
}
</script>

<style scoped>
.user-management-content {
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}

.user-management-content-area {
  max-height: 70vh;
  overflow-y: auto;
  border-radius: 1rem;
  padding: 1rem 0;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .user-management-content {
    margin: 1rem;
    max-width: calc(100vw - 2rem);
  }

  .user-management-container {
    padding: 1.5rem;
  }
}

/* 滚动条美化 */
.user-management-content-area::-webkit-scrollbar {
  width: 6px;
}

.user-management-content-area::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.user-management-content-area::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
}

.user-management-content-area::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.5);
}

.dark .user-management-content-area::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
}

.dark .user-management-content-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
}

.dark .user-management-content-area::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
