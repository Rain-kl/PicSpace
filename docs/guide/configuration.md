# 系统配置

后端主要配置位于 `pic-space-backend/src/main/resources/application.yml`。

## 服务配置

```yaml
server:
  port: 8123
  servlet:
    context-path: /api
```

这意味着所有后端接口都以 `/api` 为前缀。例如登录接口完整路径为：

```text
POST http://localhost:8123/api/user/login
```

## 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/pic_space
    username: root
    password: root
```

生产环境建议通过环境变量或外部配置覆盖账号密码，避免把敏感信息提交到仓库。

## Redis 与 Session

```yaml
spring:
  redis:
    database: 1
    host: 127.0.0.1
    port: 6379
    timeout: 5000
    password: root
  session:
    store-type: redis
    timeout: 2592000
```

登录态依赖 Redis Session。前端请求实例开启了 `withCredentials: true`，跨域部署时需要同步配置 CORS 与 Cookie 策略。

## 文件上传限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
```

该限制影响单张图片上传大小。空间层面还会根据空间等级控制总容量和图片数量。

## MinIO 配置

```yaml
space:
  minio:
    access-key-id: your-access-key
    access-key-secret: your-secret-key
    bucket-name: pic-space
    endpoint: http://localhost:9000
```

需要确保：

- MinIO 服务可访问。
- `bucket-name` 对应的 bucket 已创建。
- Access Key 具备上传、读取、删除对象所需权限。

## Knife4j 配置

```yaml
knife4j:
  enable: true
  openapi:
    title: 接口文档
    version: 1.0
```

开发环境可访问 `http://localhost:8123/api/doc.html` 查看接口文档。
