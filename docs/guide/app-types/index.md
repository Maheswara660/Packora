# App Architecture Targets

Packora provides **5 specialized application architectures** tailored for different web workloads. Each target configures the underlying Android WebView container, asset packaging, and background lifecycle policies for optimal performance.

---

## The 5 Packora Architectures

| Architecture | Type Key | Input | Runtime Characteristics | Best For |
| :--- | :--- | :--- | :--- | :--- |
| **[Web App](/guide/app-types/web)** | `WEB` | Public HTTPS / HTTP URL | Standard standalone WebAPK with independent task affinity, anti-fingerprinting, and custom keystores. | Dashboards, SaaS, responsive websites, blogs, and documentation portals. |
| **[Offline HTML](/guide/app-types/html)** | `HTML` | Local HTML, CSS & JS directory | Self-contained offline package bundled into `assets/www/`. Loads via secure file protocol with zero network dependencies. | Local documentation, offline calculators, interactive reference guides, games. |
| **[Frontend SPA](/guide/app-types/frontend)** | `FRONTEND` | SPA URL or static bundle | Optimized Single Page Application with client-side history routing fallback (`spaRoutingFallback`), IndexedDB, and persistent DOM storage. | React, Vue, Vite, Svelte, Nuxt, and Next.js static exports. |
| **[Multi-Web Hub](/guide/app-types/multi-web)** | `MULTI_WEB` | Primary URL + Secondary URLs | Multi-tab destination workspace rendering an interactive, native dark pill tab bar for instant in-app navigation. | Developer hubs, service portals, documentation + forum suites. |
| **[Media Streamer](/guide/app-types/media)** | `MEDIA` | Streaming web player URL | Configures `FLAG_KEEP_SCREEN_ON`, unconstrained video autoplay, and continuous background audio streaming during minimization. | Music players, podcasts, audiobooks, video streaming platforms. |

---

## Architecture Selection in Build Studio

You can select your target architecture directly above the URL input bar in **Build Studio**:

1. **Tap the Target Pill**: Choose between `Web`, `HTML`, `Frontend`, `Multi-Web`, or `Media`.
2. **Context-Aware Fields**:
   - Selecting `HTML` activates offline asset directory selection.
   - Selecting `Frontend` activates the **Client History Routing Fallback** toggle.
   - Selecting `Multi-Web` activates the **Secondary URLs** tab input card.
   - Selecting `Media` activates **Keep Screen On** and **Background Audio Playback** toggles.
3. **Automatic Inheritance**: When an app is built, its architecture target is saved into **Build History** and displayed in the **Packora App Info** sheet.
4. **Update Parity**: Subsequent single and batch updates compiled via the **Updates Hub** automatically retain and apply the configured architecture.
