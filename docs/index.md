---
layout: home

hero:
  name: "PicSpace"
  text: "图片空间管理系统"
  tagline: 为个人图库、团队素材库与后台审核场景设计的图片管理平台
  image:
    src: /logo_small.png
    alt: PicSpace
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 查看架构
      link: /architecture/
    - theme: alt
      text: API 参考
      link: /api/

features:
  - icon: 📷
    title: 图片资产管理
    details: 支持本地文件上传、URL 上传、批量抓取、分类、标签、检索与瀑布流浏览
  - icon: 🗂️
    title: 个人与团队空间
    details: 支持私有空间和团队空间，按空间等级限制图片数量和容量
  - icon: 🔐
    title: 多层权限控制
    details: 系统角色与空间角色结合，覆盖成员管理、图片查看、上传、编辑与删除
  - icon: 🧰
    title: 前后端分离
    details: Vue 3 前端、Spring Boot 后端、MyBatis-Plus 数据访问，结构清晰便于二次开发
  - icon: 🪣
    title: 对象存储集成
    details: 通过 MinIO 存储图片文件，数据库保存图片元信息与空间统计
  - icon: 📘
    title: 接口文档友好
    details: 后端集成 Knife4j，前端可通过 OpenAPI 自动生成 TypeScript 请求代码
---
