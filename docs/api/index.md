# API 参考

本文档整理当前后端 Controller 中暴露的主要 REST API。运行后端后，也可以访问 Knife4j 查看自动生成的接口文档：

```text
http://localhost:8123/api/doc.html
```

## 接口约定

### 基础地址

```text
http://localhost:8123/api
```

### 响应结构

```json
{
  "code": 0,
  "data": {},
  "message": "ok"
}
```

### 分页参数

分页请求通常继承以下字段：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `current` | number | `1` | 当前页 |
| `pageSize` | number | `10` | 每页数量 |
| `sortField` | string | - | 排序字段 |
| `sortOrder` | string | `descend` | 排序方向 |

### 认证

系统使用 Cookie Session 保存登录态。前端请求需要携带 Cookie：

```ts
withCredentials: true
```

需要登录或管理员权限的接口会在未登录、无权限时返回业务错误码。

## 用户接口

### 用户注册

```text
POST /user/register
```

请求体：

```json
{
  "userAccount": "ryan",
  "userPassword": "12345678",
  "checkPassword": "12345678"
}
```

返回用户 ID。

### 用户登录

```text
POST /user/login
```

请求体：

```json
{
  "userAccount": "ryan",
  "userPassword": "12345678"
}
```

返回当前登录用户视图，并写入登录态 Cookie。

### 获取当前登录用户

```text
GET /user/get/login
```

需要登录。

### 用户注销

```text
POST /user/logout
```

需要登录。

### 根据 ID 获取用户视图

```text
GET /user/get/vo?id=1
```

需要登录。

### 分页查询用户

```text
POST /user/get/list/page
```

普通查询接口仅允许按 `id`、`userAccount` 查询。

## 图片接口

### 上传图片

```text
POST /picture/upload
```

Content-Type：`multipart/form-data`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `file` | file | 图片文件 |
| `id` | number | 可选，重新上传时传入图片 ID |
| `picName` | string | 可选，图片名称 |
| `spaceId` | number | 可选，目标空间 |

需要登录和 `picture:upload` 权限。

### 通过 URL 上传图片

```text
POST /picture/upload/url
```

请求体：

```json
{
  "fileUrl": "https://example.com/image.png",
  "picName": "示例图片",
  "spaceId": 1
}
```

需要登录和 `picture:upload` 权限。

### 删除图片

```text
POST /picture/delete
```

请求体：

```json
{
  "id": 1
}
```

需要登录和 `picture:delete` 权限。

### 获取图片详情

```text
GET /picture/get/vo?id=1
```

公共图片需审核通过；空间图片会校验空间访问权限。

### 分页查询图片

```text
POST /picture/list/page/vo
```

请求体常用字段：

```json
{
  "current": 1,
  "pageSize": 10,
  "searchText": "壁纸",
  "category": "现实",
  "tags": ["高清"],
  "spaceId": 1
}
```

`pageSize` 最大为 `50`。不传 `spaceId` 时只查询公共图库；传入 `spaceId` 时需要是空间成员。

### 编辑图片

```text
POST /picture/edit
```

请求体：

```json
{
  "id": 1,
  "name": "新名称",
  "introduction": "图片说明",
  "category": "现实",
  "tags": ["高清", "背景"]
}
```

需要登录和 `picture:edit` 权限。

### 获取分类与标签

```text
GET /picture/tag_category
```

返回内置分类和标签列表。

## 空间接口

### 创建空间

```text
POST /space/add
```

请求体：

```json
{
  "spaceName": "设计素材",
  "spaceLevel": 1,
  "spaceType": 1
}
```

需要登录。

### 获取空间信息

```text
GET /space/get/vo?id=1
```

需要登录且属于该空间。

### 删除空间

```text
POST /space/delete
```

请求体：

```json
{
  "id": 1
}
```

空间创建者或系统管理员可删除。

### 查询我的空间

```text
POST /space/list
```

返回当前用户创建的空间列表。

### 编辑空间

```text
POST /space/edit
```

请求体：

```json
{
  "id": 1,
  "spaceName": "新空间名称"
}
```

### 查询空间等级

```text
GET /space/list/level
```

返回普通版、专业版、旗舰版的数量和容量限制。

## 空间成员接口

### 添加成员

```text
POST /spaceUser/add
```

请求体：

```json
{
  "spaceId": 1,
  "userId": 2,
  "spaceRole": "editor"
}
```

需要 `spaceUser:manage` 权限。

### 移除成员

```text
POST /spaceUser/delete
```

请求体：

```json
{
  "id": 1
}
```

需要 `spaceUser:manage` 权限。

### 查询成员关系

```text
POST /spaceUser/get
```

请求体：

```json
{
  "spaceId": 1,
  "userId": 2
}
```

需要 `picture:view` 权限。

### 查询成员列表

```text
POST /spaceUser/list
```

请求体：

```json
{
  "spaceId": 1
}
```

需要 `picture:view` 权限。

### 编辑成员角色

```text
POST /spaceUser/edit
```

请求体：

```json
{
  "id": 1,
  "spaceRole": "admin"
}
```

需要 `spaceUser:manage` 权限。

### 查询我加入的团队空间

```text
POST /spaceUser/list/my
```

返回当前用户加入的空间关系列表。

## 管理接口

以下接口需要系统管理员权限。

### 用户管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/user/add` | 创建用户，默认密码为 `12345678` |
| `GET` | `/user/get?id=1` | 获取用户实体 |
| `POST` | `/user/delete` | 删除用户 |
| `POST` | `/user/update` | 更新用户 |
| `POST` | `/user/list/page/vo` | 分页查询用户视图 |

### 图片管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/picture/upload/batch` | 批量抓取并创建图片 |
| `POST` | `/picture/update` | 管理员更新图片 |
| `GET` | `/picture/get?id=1` | 获取图片实体 |
| `POST` | `/picture/list/page` | 后台分页查询图片 |
| `POST` | `/picture/review` | 审核图片 |

图片审核请求体：

```json
{
  "id": 1,
  "reviewStatus": 1,
  "reviewMessage": "审核通过"
}
```

### 空间管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/space/update` | 管理员更新空间 |
| `GET` | `/space/get?id=1` | 获取空间实体 |
| `POST` | `/space/list/page` | 后台分页查询空间 |
| `POST` | `/space/list/page/vo` | 分页查询空间视图，已废弃 |

### 数据分析

```text
GET /dataAnalysis/overview
```

返回后台数据概览。

## 错误响应

| 业务码 | 说明 |
| --- | --- |
| `0` | 成功 |
| `40000` | 请求参数错误 |
| `40100` | 未登录 |
| `40101` | 无权限 |
| `40300` | 禁止访问 |
| `40400` | 请求数据不存在 |
| `50000` | 系统内部异常 |
| `50001` | 操作失败 |

错误示例：

```json
{
  "code": 40100,
  "data": null,
  "message": "未登录"
}
```
