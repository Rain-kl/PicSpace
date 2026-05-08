# 贡献指南

欢迎参与 PicSpace 的功能完善、文档维护和问题修复。

## 开发建议

- 后端改动优先补充或更新对应 Controller、Service、DTO、VO 文档。
- 前端接口变更后，启动后端并执行 `pnpm openapi` 生成最新请求代码。
- 数据库结构变更需要同步更新 `pic-space-backend/sql/create_table.sql` 和架构文档。
- 权限变更需要同步更新 `spaceUserAuthConfig.json` 与权限模型文档。

## 提交前检查

```bash
cd pic-space-backend
mvn test

cd ../pic-space-frontend
pnpm build

cd ../docs
pnpm docs:build
```
