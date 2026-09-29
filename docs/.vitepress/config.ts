import { defineConfig } from 'vitepress'

// GitHub Pages serves this under /Packora/. Override with DOCS_BASE for a
// custom domain (e.g. DOCS_BASE=/ npm run build).
const base = process.env.DOCS_BASE || '/Packora/'

const nav = [
  { text: 'Guide', link: '/guide/introduction' },
  { text: 'Architectures', link: '/guide/app-types/' },
  { text: 'Stealth Privacy', link: '/guide/security-privacy/stealth-privacy' },
  { text: 'Developer', link: '/developer/' },
  { text: 'Releases', link: 'https://github.com/Maheswara660/Packora/releases' }
]

const sidebar = {
  '/guide/': [
    {
      text: 'Start',
      items: [
        { text: 'Introduction', link: '/guide/introduction' },
        { text: 'Getting Started', link: '/guide/getting-started' },
        { text: 'FAQ', link: '/guide/faq' }
      ]
    },
    {
      text: 'Architectures',
      items: [
        { text: 'Overview', link: '/guide/app-types/' },
        { text: 'Web App (PWA)', link: '/guide/app-types/web' },
        { text: 'Offline HTML5', link: '/guide/app-types/html' },
        { text: 'Frontend SPA', link: '/guide/app-types/frontend' },
        { text: 'Multi-Web Hub', link: '/guide/app-types/multi-web' },
        { text: 'Immersive Media', link: '/guide/app-types/media' }
      ]
    },
    {
      text: 'Security & Privacy',
      items: [
        { text: 'Stealth Privacy Shield', link: '/guide/security-privacy/stealth-privacy' },
        { text: 'Ad & Tracker Blocking', link: '/guide/security-privacy/ad-blocking' },
        { text: 'Encrypted DNS (DoH)', link: '/guide/security-privacy/encrypted-dns' },
        { text: 'Smart Footer & Quick Nav', link: '/guide/security-privacy/smart-footer' }
      ]
    },
    {
      text: 'Studio Management',
      items: [
        { text: 'Updates Hub', link: '/guide/management/updates-hub' },
        { text: 'Build History & Artifacts', link: '/guide/management/build-history' },
        { text: 'App Info & Analysis', link: '/guide/management/app-info' },
        { text: 'Dynamic Icon Picker', link: '/guide/management/dynamic-icons' }
      ]
    }
  ],
  '/developer/': [
    {
      text: 'Developer Docs',
      items: [
        { text: 'Overview', link: '/developer/' },
        { text: 'Engine Architecture', link: '/developer/architecture' },
        { text: 'Binary Engine & Patching', link: '/developer/binary-engine' },
        { text: 'Deterministic RSA-3072 Keys', link: '/developer/deterministic-keys' },
        { text: '16KB ELF Page Alignment', link: '/developer/page-alignment' },
        { text: 'Contributing', link: '/developer/contributing' }
      ]
    }
  ]
}

export default defineConfig({
  title: 'Packora',
  description:
    'Next-Gen Android WebAPK Studio — Transform websites, SPAs, and offline HTML bundles into hardened, high-performance native Android applications directly on your device.',
  lang: 'en-US',
  base,
  cleanUrls: true,
  lastUpdated: true,
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}logo.png` }],
    ['meta', { name: 'theme-color', content: '#000000' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Packora - Next-Gen Android WebAPK Studio' }],
    [
      'meta',
      {
        property: 'og:description',
        content: 'Compile websites, SPAs, and offline HTML bundles into hardened, high-performance Android WebAPKs with on-device binary patching, 50+ vector stealth shield, and 16KB ELF alignment.'
      }
    ],
    ['meta', { property: 'og:image', content: `${base}social-preview.png` }],
    ['script', { src: `${base}kmp/packora-kmp.js`, defer: 'true' }]
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
      pattern: 'https://github.com/Maheswara660/Packora/edit/main/docs/:path',
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
