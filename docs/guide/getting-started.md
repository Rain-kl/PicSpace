# 快速开始

本页说明如何在本地启动 PicSpace。默认后端端口为 `8123`，后端接口统一挂载在 `/api` 下。

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 20.19+ 或 22.12+
- pnpm
- MySQL 8.x
- Redis
- MinIO

## 初始化数据库

创建 MySQL 数据库并执行建表脚本：

```bash
mysql -uroot -p < pic-space-backend/sql/create_table.sql
```

脚本会创建 `pic_space` 数据库，并初始化一个管理员账号：

| 字段 | 值 |
| --- | --- |
| 账号 | `admin` |
| 密码 | 由后端加密逻辑校验，数据库内保存的是加密值 |
| 角色 | `admin` |

## 启动依赖服务

后端默认连接：

| 服务 | 默认地址 |
| --- | --- |
| MySQL | `localhost:3306/pic_space` |
| Redis | `127.0.0.1:6379` |
| MinIO | `http://localhost:9000` |

请先在 MinIO 中创建 `pic-space` bucket，或按你的部署方式修改后端配置。

## 启动后端

```bash
cd pic-space-backend
mvn spring-boot:run
```

启动成功后：

- API 基础地址：`http://localhost:8123/api`
- Knife4j 文档：`http://localhost:8123/api/doc.html`
- OpenAPI JSON：`http://localhost:8123/api/v2/api-docs`

## 启动前端

```bash
cd pic-space-frontend
pnpm install
pnpm dev
```

前端默认由 Vite 启动，访问终端输出的本地地址即可。开发时如需代理 `/api`，请在 `pic-space-frontend/vite.config.ts` 中配置代理，或让前端直接请求后端地址。

## 生成前端 API 代码

后端启动后，可以使用 OpenAPI 配置重新生成前端请求代码：

```bash
cd pic-space-frontend
pnpm openapi
```

生成配置位于 `pic-space-frontend/openapi.config.js`，默认读取 `http://localhost:8123/api/v2/api-docs`。

## 启动文档

```bash
cd docs
pnpm install
pnpm docs:dev
```

文档构建命令：

```bash
pnpm docs:build
```
