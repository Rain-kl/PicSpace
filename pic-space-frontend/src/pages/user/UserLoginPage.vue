<template>
  <div class="grid min-h-svh lg:grid-cols-2">
    <div class="flex flex-col gap-4 p-6 md:p-10">
      <div class="flex justify-center gap-2 md:justify-start">
        <a href="#" class="flex items-center gap-2 font-medium">
          <div
            class="flex h-6 w-6 items-center justify-center rounded-md bg-primary text-primary-foreground"
          >
            <GalleryVerticalEnd class-name="size-4" />
          </div>
          PicSpace
        </a>
      </div>
      <div class="flex flex-1 items-center justify-center">
        <div class="w-full max-w-xs">
          <form @submit.prevent="handleLogin" :class="cn('flex flex-col gap-6')">
            <div class="flex flex-col items-center gap-2 text-center">
              <h1 class="text-2xl font-bold">登录你的账户</h1>
              <p class="text-balance text-sm text-muted-foreground">
                在下面输入您的电子邮件以登录您的帐户
              </p>
            </div>
            <div class="grid gap-6">
              <div class="grid gap-2">
                <Label for="email">Username</Label>
                <Input id="email" type="text" v-model="formState.userAccount" required />
              </div>
              <div class="grid gap-2">
                <div class="flex items-center">
                  <Label for="password">Password</Label>

                  <!--                  <a href="#" class="ml-auto text-sm underline-offset-4 hover:underline">-->
                  <!--                    Forgot your password?-->
                  <!--                  </a>-->
                </div>
                <Input id="password" type="password" v-model="formState.userPassword" required />
              </div>
              <Button type="button" @click="handleLogin" class="w-full !text-white"> Login </Button>
              <div
                class="relative text-center text-sm after:absolute after:inset-0 after:top-1/2 after:z-0 after:flex after:items-center after:border-t after:border-border"
              >
                <span class="relative z-10 bg-background px-2 text-muted-foreground">
                  Or continue with
                </span>
              </div>
              <Button variant="outline" class="w-full">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <path
                    d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"
                    fill="currentColor"
                  />
                </svg>
                Login with GitHub
              </Button>
            </div>
            <div class="text-center text-sm">
              Don't have an account?
              <a href="/register" class="underline underline-offset-4"> Sign up </a>
            </div>
          </form>
        </div>
      </div>
    </div>
    <!-- <div class="relative hidden bg-muted lg:block"> -->
    <div class="relative hidden lg:block bg-zinc-900">
      <!-- <img
        src="/placeholder.svg"
        alt="Image"
        class="absolute inset-0 h-full w-full object-cover dark:brightness-[0.2] dark:grayscale"
      > -->
    </div>
  </div>
</template>
<script lang="ts" setup>
import { userLoginUsingPost } from '@/api/userController'
import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useRoute } from 'vue-router'

import { GalleryVerticalEnd } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils.ts'

const route = useRoute()
const loginUserStore = useLoginUserStore()
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const handleLogin = async () => {
  try {
    console.log('开始登录，表单数据:', formState)
    const res = await userLoginUsingPost(formState)
    console.log('登录API响应:', res.data)

    if (res.data.code == 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')

      if (route.query.redirect) {
        console.log('登录成功，开始重定向到:', route.query.redirect)
        window.location.href = route.query.redirect as string
        return
      }

      console.log('登录成功，跳转首页')
      await router.push('/')
    } else {
      console.log('登录失败，错误信息:', res.data.message)
      message.error(res.data.message ?? '登录失败')
    }
  } catch (error) {
    console.error('登录过程发生错误:', error)
    message.error('登录过程中发生错误，请重试')
  }
}
</script>

<style scoped></style>
