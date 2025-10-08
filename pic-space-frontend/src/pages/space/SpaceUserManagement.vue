<template>
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
          <Input v-model="newUser.userAccount" class="col-span-3" placeholder="请输入用户账号" />
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
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { Card, CardContent } from '@/components/ui/card/index.ts'
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  getSpaceUserUsingPost,
  listSpaceUserUsingPost,
} from '@/api/spaceUserController.ts'
import dayjs from 'dayjs'
import { SPACE_ROLE_STYLE_MAP } from '@/constants/space.ts'
import { Button } from '@/components/ui/button'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { message } from 'ant-design-vue'
import { getUserListPageUsingPost } from '@/api/userController.ts'

const props = defineProps<{
  spaceId: string
}>()

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
