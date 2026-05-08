# PicSpace

PicSpace 是一个面向个人与团队的图片空间管理系统，支持图片上传、URL 导入、分类标签、空间隔离、团队成员权限、图片审核和后台管理。

## 功能特性

- 图片管理：本地上传、URL 上传、批量抓取、瀑布流浏览、分类、标签、搜索。
- 空间管理：私有空间、团队空间、空间等级、容量上限、图片数量上限。
- 团队协作：空间成员管理，支持成员、编辑、管理员三类空间角色。
- 权限控制：系统角色和空间权限结合，基于 Sa-Token 与 Redis Session。
- 后台管理：用户管理、图片审核、空间管理、数据概览。
- 工程化：前后端分离、Knife4j 接口文档、OpenAPI 生成前端 API、VitePress 项目文档。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、TypeScript、Pinia、Vue Router、Ant Design Vue、Tailwind CSS |
| 后端 | Java 17、Spring Boot 2.7、MyBatis-Plus、Sa-Token、Spring Session、Knife4j |
| 存储 | MySQL、Redis、MinIO |
| 文档 | VitePress |

## 项目结构

```text
.
├── pic-space-backend   # Spring Boot 后端
├── pic-space-frontend  # Vue 3 前端
├── docs                # VitePress 文档
├── Dockerfile
└── README.md
```

## 快速开始

### 1. 准备环境

- JDK 17
- Maven 3.8+
- Node.js 20.19+ 或 22.12+
- pnpm
- MySQL 8.x
- Redis
- MinIO

### 2. 初始化数据库

```bash
mysql -uroot -p < pic-space-backend/sql/create_table.sql
```

默认数据库名为 `pic_space`。

### 3. 配置后端

编辑：

```text
pic-space-backend/src/main/resources/application.yml
```

重点配置 MySQL、Redis 和 MinIO：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pic_space
    username: root
    password: root
  redis:
    host: 127.0.0.1
    port: 6379
    password: root

space:
  minio:
    bucket-name: pic-space
    endpoint: http://localhost:9000
```

### 4. 启动后端

```bash
cd pic-space-backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8123/api
```

Knife4j 文档：

```text
http://localhost:8123/api/doc.html
```

### 5. 启动前端

```bash
cd pic-space-frontend
pnpm install
pnpm dev
```

### 6. 启动文档

```bash
cd docs
pnpm install
pnpm docs:dev
```

构建文档：

```bash
pnpm docs:build
```

## API 代码生成

后端启动后，在前端目录执行：

```bash
cd pic-space-frontend
pnpm openapi
```

配置文件位于 `pic-space-frontend/openapi.config.js`，默认读取：

```text
http://localhost:8123/api/v2/api-docs
```

## 文档入口

文档位于 `docs`，已按以下板块组织：

- 首页：项目定位与核心能力。
- 指南：快速开始、配置、上传、空间协作、权限安全。
- 架构：总体架构、前端、后端、权限模型、数据模型。
- API：接口约定与主要 REST API。

## License

详见 [LICENSE](./LICENSE)。
