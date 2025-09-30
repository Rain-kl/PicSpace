<template>
  <div class="flex h-screen w-full items-center justify-center px-4">
    <Card class="mx-auto max-w-md w-full">
      <CardHeader>
        <CardTitle class="text-2xl"> 用户注册 </CardTitle>
        <CardDescription> 创建您的新账户 </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid gap-4">
          <div class="grid gap-2">
            <Label for="userAccount">用户账号</Label>
            <Input
              id="userAccount"
              v-model="formState.userAccount"
              type="text"
              placeholder="请输入用户账号"
              required
            />
          </div>
          <div class="grid gap-2">
            <Label for="password">密码</Label>
            <Input
              id="password"
              v-model="formState.userPassword"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>
          <div class="grid gap-2">
            <Label for="checkPassword">确认密码</Label>
            <Input
              id="checkPassword"
              v-model="formState.checkPassword"
              type="password"
              placeholder="请确认密码"
              required
            />
          </div>
          <Button @click="handleRegister" type="submit" class="w-full !text-white"> 注册 </Button>
        </div>
        <div class="mt-4 text-center text-sm">
          已有账户？
          <router-link to="/user/login" class="underline"> 立即登录 </router-link>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
<script lang="ts" setup>
import { userRegisterUsingPost } from '@/api/userController'
import router from '@/router'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const handleRegister = async () => {
  // 验证密码一致性
  if (formState.userPassword !== formState.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }

  // 验证必填字段
  if (!formState.userAccount || !formState.userPassword) {
    message.error('请填写完整信息')
    return
  }

  try {
    const res = await userRegisterUsingPost(formState)
    if (res.data.code == 0 && res.data.data) {
      message.success('注册成功')
      router.push({ path: '/login', replace: true })
    } else {
      message.error(res.data.message ?? '注册失败')
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
  }
}
</script>

<style scoped></style>
