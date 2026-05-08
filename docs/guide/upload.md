# 图片上传

PicSpace 支持三种图片进入系统的方式：本地文件上传、URL 上传和管理员批量抓取。

## 本地文件上传

前端上传组件会以 `multipart/form-data` 调用后端：

```text
POST /api/picture/upload
```

表单中需要包含：

| 字段 | 说明 |
| --- | --- |
| `file` | 图片文件 |
| `picName` | 图片名称，可选 |
| `spaceId` | 目标空间 ID，可选；为空时进入公共图库 |
| `id` | 已有图片 ID，可选；传入时表示重新上传 |

后端会解析图片宽高、大小、比例、格式和哈希值，并将文件保存到 MinIO。

## URL 上传

URL 上传接口：

```text
POST /api/picture/upload/url
```

请求体示例：

```json
{
  "fileUrl": "https://example.com/image.png",
  "picName": "示例图片",
  "spaceId": 1
}
```

该方式适合从外部图片地址导入素材。

## 批量抓取

管理员可通过关键词批量抓取图片：

```text
POST /api/picture/upload/batch
```

请求体示例：

```json
{
  "searchText": "壁纸",
  "count": 10,
  "namePrefix": "wallpaper"
}
```

该接口仅管理员可用，适合初始化素材库或快速补充演示数据。

## 图片审核

图片表内包含审核状态：

| 值 | 状态 |
| --- | --- |
| `0` | 审核中 |
| `1` | 通过 |
| `2` | 拒绝 |

普通列表接口只返回审核通过的公共图片；空间图片还会额外校验成员权限。
