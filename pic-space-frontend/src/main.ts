import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/access/access.ts'
import '@/index.css'
// 初始化主题
import { useThemeStore } from '@/stores/useThemeStore'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

const initTheme = () => {
  const themeStore = useThemeStore()
  themeStore.initTheme()
}

app.mount('#app')

// 在app挂载后初始化主题
initTheme()
