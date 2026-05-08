import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

const docsRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const dayjsEsmRoot = resolve(docsRoot, 'node_modules/dayjs/esm')

// https://vitepress.dev/reference/site-config
export default withMermaid(defineConfig({
  title: 'PicSpace',
  description: '面向个人与团队的图片空间管理系统',
  lang: 'zh-CN',
  lastUpdated: true,
  mermaid: {
    theme: 'default',
    securityLevel: 'strict',
  },
  mermaidPlugin: {
    class: 'mermaid',
  },
  vite: {
    resolve: {
      alias: [
        { find: /^dayjs$/, replacement: resolve(dayjsEsmRoot, 'index.js') },
        { find: /^dayjs\/plugin\/(.+?)(?:\.js)?$/, replacement: resolve(dayjsEsmRoot, 'plugin/$1/index.js') },
        { find: /^dayjs\/locale\/(.+?)(?:\.js)?$/, replacement: resolve(dayjsEsmRoot, 'locale/$1.js') },
      ],
    },
    optimizeDeps: {
      include: ['dayjs'],
    },
  },
  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-CN',
      title: 'PicSpace',
      description: '面向个人与团队的图片空间管理系统',
      themeConfig: {
        logo: '/logo_small.png',
        nav: [
          { text: '首页', link: '/' },
          { text: '指南', link: '/guide/getting-started' },
          { text: '架构', link: '/architecture/' },
          { text: 'API', link: '/api/' },
        ],

        sidebar: {
          '/guide/': [
            {
              text: '入门',
              items: [
                {
                  text: '项目介绍',
                  link: '/guide/introduction',
                },
                { text: '快速开始', link: '/guide/getting-started' },
                { text: '系统配置', link: '/guide/configuration' },
              ],
            },
            {
              text: '功能指南',
              items: [
                { text: '图片上传', link: '/guide/upload' },
                { text: '空间与协作', link: '/guide/management' },
                { text: '权限与安全', link: '/guide/security' },
              ],
            },
          ],
          '/architecture/': [
            {
              text: '架构',
              items: [
                { text: '总体架构', link: '/architecture/' },
                { text: '后端架构', link: '/architecture/backend' },
                { text: '前端架构', link: '/architecture/frontend' },
                { text: '权限模型', link: '/architecture/auth' },
                { text: '数据模型', link: '/architecture/data-model' },
              ],
            },
          ],
          '/api/': [
            {
              text: 'API 参考',
              items: [
                { text: '接口约定', link: '/api/#接口约定' },
                { text: '用户接口', link: '/api/#用户接口' },
                { text: '图片接口', link: '/api/#图片接口' },
                { text: '空间接口', link: '/api/#空间接口' },
                { text: '空间成员接口', link: '/api/#空间成员接口' },
                { text: '管理接口', link: '/api/#管理接口' },
                { text: '错误响应', link: '/api/#错误响应' },
              ],
            },
          ],
        },
        footer: {
          message: 'PicSpace 文档',
          copyright: 'Copyright © 2026 PicSpace',
        },
        search: {
          provider: 'local',
        },
        outline: {
          level: [2, 3],
          label: '目录',
        },
      },
    },
  },

  themeConfig: {
    // 语言切换器
    langMenuLabel: '切换语言',

    // 页脚
    footer: {
      message: 'PicSpace 文档',
      copyright: 'Copyright © 2026 PicSpace',
    },

    // 搜索
    search: {
      provider: 'local',
      options: {
        locales: {
          zh: {
            translations: {
              button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档',
              },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: {
                  selectText: '选择',
                  navigateText: '切换',
                },
              },
            },
          },
          en: {
            translations: {
              button: {
                buttonText: 'Search',
                buttonAriaLabel: 'Search docs',
              },
              modal: {
                noResultsText: 'No results found',
                resetButtonTitle: 'Clear query',
                footer: {
                  selectText: 'to select',
                  navigateText: 'to navigate',
                },
              },
            },
          },
        },
      },
    },

    outline: {
      level: [2, 3],
      label: '目录',
    },
  },
}))
