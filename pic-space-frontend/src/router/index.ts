import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AddPicturePage from '@/pages/AddPicturePage.vue'
import PictureManagePage from '@/pages/admin/PictureManagePage.vue'
import PictureDetailPage from '@/pages/PictureDetailPage.vue'
import PictureCrawler from '@/components/upload/PictureCrawler.vue'
import SpaceManagePage from '@/pages/admin/SpaceManagePage.vue'
import AddSpacePage from '@/pages/AddSpacePage.vue'
import RedirectSpacePage from '@/pages/RedirectSpacePage.vue'
import SpacePage from '@/pages/SpacePage.vue'
import UpdatePicture from '@/components/UpdatePicture.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/search/:keyword',
      name: 'search',
      component: HomePage,
    },
    {
      path: '/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: PictureManagePage,
    },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManagePage,
    },
    {
      path: '/add-picture',
      name: '创建图片',
      component: AddPicturePage,
    },
    {
      path: '/add-space',
      name: '创建空间',
      component: AddSpacePage,
    },
    {
      path: '/add-picture/batch',
      name: '批量创建图片',
      component: PictureCrawler,
    },
    {
      path: '/space',
      name: '空间列表',
      component: RedirectSpacePage,
    },
    {
      path: '/space/:spaceId',
      name: '空间详情',
      component: SpacePage,
      props: true,
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: PictureDetailPage,
      props: true,
    },
    {
      path: '/test',
      name: '测试组件',
      component: UpdatePicture,
    },
  ],
})

export default router
