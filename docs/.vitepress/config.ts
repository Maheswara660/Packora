import { defineConfig } from 'vitepress'

// GitHub Pages serves this under /Packora/. Override with DOCS_BASE for a
// custom domain (e.g. DOCS_BASE=/ npm run build).
const base = process.env.DOCS_BASE || '/Packora/'

const nav = [
  { text: 'Guide', link: '/guide/introduction' },
  { text: 'Developer', link: '/developer/' },
  { text: 'Plugins', link: '/extensions/' }
]

const sidebar = {
  '/guide/': [
    {
      text: 'Start',
      items: [
        { text: 'Introduction', link: '/guide/introduction' },
        { text: 'Getting Started', link: '/guide/getting-started' }
      ]
    },
    {
      text: 'Main Screen',
      items: [
        { text: 'My Apps', link: '/guide/main-screen/my-apps' },
        { text: 'Dark / Light', link: '/guide/main-screen/dark-light' },
        { text: 'Language', link: '/guide/main-screen/language' },
        { text: 'Search', link: '/guide/main-screen/search' },
        { text: 'More Menu', link: '/guide/main-screen/more' },
        { text: 'Categories', link: '/guide/main-screen/categories' },
        { text: 'App List', link: '/guide/main-screen/app-list' },
        { text: 'Create Button', link: '/guide/main-screen/create-app' }
      ]
    },
    {
      text: 'Create App',
      items: [
        { text: 'Overview', link: '/guide/app-types/' },
        { text: 'Web', link: '/guide/app-types/web' },
        { text: 'Multi-Web', link: '/guide/app-types/multi-web' },
        { text: 'HTML', link: '/guide/app-types/html' },
        { text: 'Offline Pack', link: '/guide/app-types/offline-pack' },
        { text: 'Frontend', link: '/guide/app-types/frontend' },
        { text: 'PHP', link: '/guide/app-types/php' },
        { text: 'WordPress', link: '/guide/app-types/wordpress' },
        { text: 'Node.js', link: '/guide/app-types/nodejs' },
        { text: 'Python', link: '/guide/app-types/python' },
        { text: 'Go', link: '/guide/app-types/go' },
        { text: 'Media', link: '/guide/app-types/media' },
        { text: 'Gallery', link: '/guide/app-types/gallery' }
      ]
    },
    {
      text: 'App Actions',
      items: [
        { text: 'Edit Core Config', link: '/guide/app-actions/edit-core-config' },
        { text: 'Edit Common Config', link: '/guide/app-actions/edit-common-config' },
        { text: 'Create Shortcut', link: '/guide/app-actions/create-shortcut' },
        { text: 'Build APK', link: '/guide/app-actions/build-apk' },
        { text: 'Share APK', link: '/guide/app-actions/share-apk' },
        { text: 'Export', link: '/guide/app-actions/export-apk' },
        { text: 'Move to Category', link: '/guide/app-actions/move-to-category' },
        { text: 'Delete', link: '/guide/app-actions/delete' }
      ]
    },
    {
      text: 'Common Config',
      items: [
        {
          text: 'Basic Info',
          collapsed: false,
          items: [
            { text: 'App Icon', link: '/guide/app-actions/edit-common-config/app-icon' },
            { text: 'Config Templates', link: '/guide/app-actions/edit-common-config/config-templates' },
            { text: 'App Name', link: '/guide/app-actions/edit-common-config/app-name' },
            { text: 'URL / Webpage', link: '/guide/app-actions/edit-common-config/url-webpage' }
          ]
        },
        {
          text: 'Browser & Interface',
          collapsed: false,
          items: [
            { text: 'Browser Toolbar', link: '/guide/app-actions/edit-common-config/browser-toolbar' },
            { text: 'Fullscreen Mode', link: '/guide/app-actions/edit-common-config/fullscreen' },
            { text: 'Screen Orientation', link: '/guide/app-actions/edit-common-config/orientation' },
            { text: 'Keep Screen On', link: '/guide/app-actions/edit-common-config/keep-screen-on' },
            { text: 'Floating Window', link: '/guide/app-actions/edit-common-config/floating-window' },
            { text: 'Long-press Menu', link: '/guide/app-actions/edit-common-config/long-press-menu' }
          ]
        },
        {
          text: 'Media & Interaction',
          collapsed: false,
          items: [
            { text: 'Splash Animation', link: '/guide/app-actions/edit-common-config/splash' },
            { text: 'Background Music', link: '/guide/app-actions/edit-common-config/bgm' },
            { text: 'Popup Announcement', link: '/guide/app-actions/edit-common-config/announcement' },
            { text: 'Auto Translation', link: '/guide/app-actions/edit-common-config/translate' }
          ]
        },
        {
          text: 'Extensions & Network',
          collapsed: false,
          items: [
            { text: 'Plugins', link: '/guide/app-actions/edit-common-config/extension-modules' },
            { text: 'Ad Blocking', link: '/guide/app-actions/edit-common-config/ad-blocking' },
            { text: 'Custom DNS', link: '/guide/app-actions/edit-common-config/custom-dns' }
          ]
        },
        {
          text: 'Disguise',
          collapsed: false,
          items: [
            { text: 'Device Disguise', link: '/guide/app-actions/edit-common-config/device-disguise' }
          ]
        },
        {
          text: 'Launch & Runtime',
          collapsed: false,
          items: [
            { text: 'Auto-start', link: '/guide/app-actions/edit-common-config/auto-start' }
          ]
        },
        {
          text: 'Advanced & Export',
          collapsed: false,
          items: [
            { text: 'Advanced Settings', link: '/guide/app-actions/edit-common-config/advanced-settings' },
            { text: 'Special Settings', link: '/guide/app-actions/edit-common-config/special-settings' },
            { text: 'APK Export Config', link: '/guide/app-actions/edit-common-config/apk-export' }
          ]
        }
      ]
    },
    {
      text: 'More Features',
      items: [
        {
          text: 'AI Tools',
          collapsed: false,
          items: [
            { text: 'Agent', link: '/guide/more-features/agent' },
            { text: 'AI Settings', link: '/guide/more-features/ai-settings' }
          ]
        },
        {
          text: 'Developer Tools',
          collapsed: false,
          items: [
            { text: 'Plugins', link: '/guide/more-features/extension-modules' },
            { text: 'App Modifier', link: '/guide/more-features/app-modifier' },
            { text: 'Linux Environment', link: '/guide/more-features/linux-environment' },
            { text: 'Runtime Management', link: '/guide/more-features/runtime-management' },
            { text: 'Port Manager', link: '/guide/more-features/port-manager' }
          ]
        },
        {
          text: 'Browser',
          collapsed: false,
          items: [
            { text: 'Browser Kernel', link: '/guide/more-features/browser-kernel' },
            { text: 'Hosts Ad Blocking', link: '/guide/more-features/hosts-adblock' },
            { text: 'Receive Shared Content', link: '/guide/more-features/share-receive' }
          ]
        },
        {
          text: 'System',
          collapsed: false,
          items: [
            { text: 'Usage Stats', link: '/guide/more-features/usage-stats' },
            { text: 'Google Play', link: '/guide/more-features/google-play' },
            { text: 'File Manager', link: '/guide/more-features/file-manager' },
            { text: 'Batch Import', link: '/guide/more-features/batch-import' },
            { text: 'About', link: '/guide/more-features/about' }
          ]
        }
      ]
    },
    {
      text: 'App Configuration',
      items: [
        { text: 'Overview', link: '/guide/config/' },
        { text: 'Network', link: '/guide/config/network' },
        { text: 'Privacy', link: '/guide/config/privacy' },
        { text: 'Appearance', link: '/guide/config/appearance' },
        { text: 'Runtimes', link: '/guide/config/runtimes' }
      ]
    },
    {
      text: 'FAQ',
      items: [{ text: 'FAQ', link: '/guide/faq' }]
    }
  ],
  '/developer/': [
    {
      text: 'Developer Docs',
      items: [
        { text: 'Overview', link: '/developer/' },
        { text: 'Architecture', link: '/developer/architecture' },
        { text: 'Export Pipeline', link: '/developer/export-pipeline' },
        { text: 'Template & Shell Architecture', link: '/developer/shell-sync' },
        { text: 'Config Field Drift', link: '/developer/config-drift' },
        { text: 'Internationalization', link: '/developer/i18n' },
        { text: 'Change Recipes', link: '/developer/recipes' },
        { text: 'Contributing', link: '/developer/contributing' }
      ]
    }
  ],
  '/extensions/': [
    {
      text: 'Extension Authoring',
      items: [
        { text: 'Overview', link: '/extensions/' },
        { text: 'HCJ Plugins', link: '/extensions/js-module' },
        { text: 'CSS Plugins', link: '/extensions/css-module' },
        { text: 'Userscripts', link: '/extensions/userscript' },
        { text: 'Chrome MV3 Extensions', link: '/extensions/chrome-mv3' },
        { text: 'API Reference', link: '/extensions/api-reference' },
        { text: 'Publish to the Market', link: '/extensions/publish' }
      ]
    }
  ]
}

export default defineConfig({
  title: 'Packora',
  description:
    'Next-Gen Android WebAPK Studio — Transform websites, SPAs, and server runtimes into native, high-performance, hardened Android applications directly on your device.',
  lang: 'en-US',
  base,
  cleanUrls: true,
  lastUpdated: true,
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}logo.png` }],
    ['meta', { name: 'theme-color', content: '#00F2FE' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Packora - Next-Gen Android WebAPK Studio' }],
    [
      'meta',
      {
        property: 'og:description',
        content: 'Transform any website, SPA, or server runtime into a hardened, high-performance Android WebAPK directly on your phone.'
      }
    ],
    ['meta', { property: 'og:image', content: `${base}social-preview.png` }]
  ],
  themeConfig: {
    logo: '/logo.png',
    siteTitle: 'Packora',
    nav,
    sidebar,
    outline: { label: 'On this page' },
    docFooter: {
      prev: 'Prev',
      next: 'Next'
    },
    darkModeSwitchLabel: 'Appearance',
    lightModeSwitchText: 'Light',
    darkModeSwitchText: 'Dark',
    sidebarMenuLabel: 'Menu',
    returnToTopLabel: 'Return to top',
    lastUpdated: {
      text: 'Last updated',
      formatOptions: { dateStyle: 'medium' }
    },
    editLink: {
      pattern: 'https://github.com/Maheswara660/Packora/edit/master/docs/:path',
      text: 'Edit this page on GitHub'
    },
    search: {
      provider: 'local'
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Maheswara660/Packora' }
    ],
    footer: {
      message: 'Released under the GNU General Public License v3.0 (GPL-3.0).',
      copyright: 'Copyright © 2026 Maheswara660 · Packora'
    }
  }
})
