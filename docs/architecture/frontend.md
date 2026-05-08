# 前端架构

前端位于 `pic-space-frontend`，基于 Vue 3、Vite 和 TypeScript。

## 目录结构

| 目录 | 说明 |
| --- | --- |
| `src/pages` | 页面级组件，包含首页、登录注册、图片详情、空间、后台管理 |
| `src/components` | 业务组件和通用 UI 组件 |
| `src/api` | OpenAPI 生成的接口请求代码 |
| `src/stores` | Pinia 状态管理 |
| `src/router` | Vue Router 路由配置 |
| `src/access` | 前端访问控制辅助逻辑 |
| `src/request.ts` | Axios 实例与响应拦截 |

## 页面组成

| 页面区域 | 说明 |
| --- | --- |
| 首页 | 公共图片浏览、搜索、分类过滤 |
| 图片详情 | 图片信息查看、操作入口 |
| 图片上传 | 本地上传、URL 上传、批量上传 |
| 空间 | 空间列表、空间详情、成员管理 |
| 后台 | 用户管理、图片管理、空间管理 |

## 请求封装

前端统一使用 `src/request.ts` 中的 Axios 实例：

```ts
const myAxios = axios.create({
  timeout: 60000,
  withCredentials: true,
})
```

当接口返回 `40100` 时，拦截器会提示用户登录，并跳转到登录页。

## API 代码生成

`openapi.config.js` 读取后端 Knife4j 输出的 OpenAPI JSON：

```js
schemaPath: 'http://localhost:8123/api/v2/api-docs'
```

执行：

```bash
pnpm openapi
```

即可刷新 `src/api` 下的 TypeScript 请求代码。
