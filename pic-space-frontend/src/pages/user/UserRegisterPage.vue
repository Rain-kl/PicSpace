<template>
  <div id="userRegisterPage">
    <h1 class="title">用户注册</h1>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="onFinish">
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: 'Please input your username!' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: 'Please input your password!' }]"
      >

        <a-input-password v-model:value="formState.userPassword" placeholder="输入密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[{ required: true, message: 'Please input your password!' }]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="确认密码" />
      </a-form-item>

      <div class="tips">
        <span>已有账号？</span>
        <RouterLink to="/user/register">去登录</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { userRegisterUsingPost } from '@/api/userController'
import router from '@/router'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { RouterLink } from 'vue-router'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const onFinish = async (values: API.UserRegisterRequest) => {
  const res = await userRegisterUsingPost(values)
  if (res.data.code == 0 && res.data.data) {
    message.success('注册成功')
    router.push({ path: '/user/login', replace: true })
  } else {
    message.error(res.data.message ?? '注册失败')
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin: 16px 0;
}
.tips {
  text-align: right;
  color: #bbb;
  margin-bottom: 16px;
}
</style>
