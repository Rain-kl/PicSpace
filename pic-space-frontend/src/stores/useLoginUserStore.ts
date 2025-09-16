import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet, userLogoutUsingPost } from '@/api/userController'
import { message } from 'ant-design-vue'

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  /**
   * 设置登录用户
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }

  async function logout() {
    const rsp = await userLogoutUsingPost()
    if (rsp.data.code !== 0) {
      message.error(rsp.data.message || '退出登录失败')
    } else {
      message.success('退出登录成功')
      setLoginUser({ userName: '未登录' })
      window.location.reload()
    }
  }

  // 返回
  return { loginUser, fetchLoginUser, setLoginUser, logout }
})
