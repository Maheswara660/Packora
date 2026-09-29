---
layout: home

hero:
  name: Packora
  text: Next-Gen Android WebAPK Studio
  tagline: Transform websites, modern SPAs, and full-stack server runtimes into native, hardened Android applications directly on your phone. Features deterministic on-device RSA signing, 50+ vector anti-fingerprint shield, encrypted DoH, instant footer hiding, and HTML5 web notifications — zero PC required.
  image:
    src: /logo.png
    alt: Packora Logo
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: Explore 12 App Types
      link: /guide/app-types/
    - theme: alt
      text: View on GitHub
      link: https://github.com/Maheswara660/Packora

features:
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 4v16"/><path d="M4 9h5"/></svg>
    title: 12 Real On-Device Runtimes
    details: Node.js, PHP, Python, Go, WordPress, HTML Offline Packs, and SPAs fork+exec native binaries directly from app storage — compiled into self-contained WebAPKs.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/></svg>
    title: Hardened Network & Encrypted DNS
    details: Built-in DNS-over-HTTPS with 9 privacy resolvers (Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, System, Custom DoH), Strict DoH, and Encrypted Client Hello (ECH).
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 8l-9-5-9 5 9 5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
    title: Deterministic RSA-3072 Signing
    details: Isolated, deterministic on-device keystores per package name with APK Signature Scheme V1, V2, and V3 support. Shipped apps update seamlessly with zero signature or parse conflicts.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.4"/></svg>
    title: 50+ Vector Privacy Shield
    details: Real-time anti-fingerprinting spoofing Canvas 2D hashes, WebGL GPU strings, AudioContext oscillators, DOM ClientRects jitter, and WebRTC local IP leak blockers.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
    title: Native HTML5 Web Notifications
    details: Complete bridge for window.Notification and Service Worker push notifications routed directly into Android system notifications with app icons and action intents.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18a14 14 0 0 1 0-18"/></svg>
    title: PWA Analyzer & Scraper
    details: Instant manifest extraction for 512x512 icons, theme colors, and metadata, plus a recursive web crawler that packages websites into offline portable apps.
---

<div class="packora-home">

<!-- Live Interactive Studio Simulator Component -->
<div class="packora-section-header">
  <span class="packora-section-badge">Live Interactive Studio</span>
  <h2>Simulate Your WebAPK Build Experience</h2>
  <p class="packora-section-subtitle">Test how Packora applies real-time privacy shields, instant 0ms CSS footer hiding, encrypted DNS, and native notifications right inside the browser.</p>
</div>

<StudioSimulator />

<!-- Bento Grid Architecture Showcase -->
<div class="packora-section-header">
  <span class="packora-section-badge">Engineering Highlights</span>
  <h2>Engineered for Power, Privacy & Independence</h2>
  <p class="packora-section-subtitle">Every feature in Packora is built from first principles for performance and privacy, running 100% on your device.</p>
</div>

<div class="packora-bento-grid">
  <!-- Bento Item 1: Large Featured -->
  <div class="bento-card bento-hero">
    <div class="bento-content">
      <div class="bento-tag">Runtime Freedom</div>
      <h3>Twelve On-Device Execution Engines</h3>
      <p>Unlike basic web shortcuts, Packora bundles real runtime binaries directly inside app storage. Compile complete Node.js microservices, PHP backends with SQLite, Python automation scripts, Go microservices, and portable WordPress instances — all running locally without cloud servers.</p>
      <div class="bento-chips-row">
        <span class="bento-chip">Node.js</span>
        <span class="bento-chip">PHP & SQLite</span>
        <span class="bento-chip">Python 3</span>
        <span class="bento-chip">Go microservice</span>
        <span class="bento-chip">React / Vite SPA</span>
        <span class="bento-chip">Offline HTML</span>
      </div>
    </div>
  </div>

  <!-- Bento Item 2: Deterministic Signing -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">Zero Parse Errors</div>
      <h3>Deterministic RSA-3072 Isolated Keystores</h3>
      <p>Each app is assigned a dedicated, cryptographically isolated signing identity generated from package coordinates. Upgrades and re-builds match previous certificates automatically, eliminating Android "problem parsing package" update errors.</p>
    </div>
  </div>

  <!-- Bento Item 3: 50+ Privacy Shield -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">Anti-Tracking</div>
      <h3>50+ Vector Privacy Shield</h3>
      <p>Neutralizes advanced commercial trackers and device fingerprinting scripts. Injects cryptographic micro-noise into Canvas 2D, WebGL GPU strings, AudioContext buffers, and blocks WebRTC local IP leaks.</p>
    </div>
  </div>

  <!-- Bento Item 4: Instant 0ms Footer Suppressor -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">Instant Clean View</div>
      <h3>0ms Early CSS Footer Suppressor</h3>
      <p>Suppresses intrusive mobile website sticky footers, download banners, and install popups before initial paint via high-priority CSS stylesheet injection and MutationObserver listeners.</p>
    </div>
  </div>

  <!-- Bento Item 5: HTML5 Web Notifications -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">System Integration</div>
      <h3>HTML5 Web Notifications Bridge</h3>
      <p>Websites calling <code>window.Notification</code> or PWA Service Worker push endpoints seamlessly trigger native Android system bar notifications with custom action channels and badges.</p>
    </div>
  </div>
</div>

<!-- Workflow Steps -->
<div class="packora-section-header">
  <span class="packora-section-badge">Streamlined Workflow</span>
  <h2>From URL to Signed APK in Three Simple Steps</h2>
  <p class="packora-section-subtitle">No Android Studio, no complex toolchains, and no PC required. Everything compiles natively on your phone in under a second.</p>
</div>

<div class="packora-steps">
  <div class="packora-step-card">
    <div class="packora-step-num">1</div>
    <h3>Target Selection</h3>
    <p>Choose from <a href="/guide/app-types/">12 application types</a>: standard Web wrappers, offline HTML bundles, frontend SPAs, or on-device Node.js, PHP, Python, and Go servers.</p>
  </div>

  <div class="packora-step-card">
    <div class="packora-step-num">2</div>
    <h3>Engine Configuration</h3>
    <p>Type your URL or select local files. Packora auto-extracts high-res icons and theme colors. Toggle AdBlock, DoH encrypted DNS, and Privacy Shield with smooth iOS switches.</p>
  </div>

  <div class="packora-step-card">
    <div class="packora-step-num">3</div>
    <h3>Native On-Device Build</h3>
    <p>Packora patches AXML bytecode, injects runtime assets, and signs the package with isolated RSA-3072 keystores (V1/V2/V3). Tap Install and your native WebAPK is ready!</p>
  </div>
</div>

<!-- UI Gallery Showcase -->
<div class="packora-section-header">
  <span class="packora-section-badge">Studio Gallery</span>
  <h2>Inside Packora Android Studio</h2>
  <p class="packora-section-subtitle">Designed for speed, clarity, and precision control over every WebAPK detail.</p>
</div>

<div class="packora-gallery-grid">
  <div class="packora-screenshot-card">
    <img src="/screenshots/build_screen.png" alt="Packora Build Screen" />
    <h4>WebAPK Build Studio</h4>
    <p>Target types, URL inspection, offline crawler & signing controls</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/my_apps_screen.png" alt="Packora My Apps" />
    <h4>My Apps Dashboard</h4>
    <p>Sort by name, date, size with instant card-level reordering</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/url_icon_selection_menu.png" alt="Packora Icon Picker" />
    <h4>High-Res Icon Picker</h4>
    <p>Live PWA extraction, Apple touch icons, and palette customization</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/history_screen.png" alt="Packora History" />
    <h4>Build History & Logs</h4>
    <p>Detailed compile diagnostics, APK sizes, and one-tap reinstall</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/updates_screen.png" alt="Packora Updates" />
    <h4>WebAPK Update Center</h4>
    <p>Automated version checking and batch app update compilation</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/settings_screen.png" alt="Packora Settings" />
    <h4>Settings & Appearance</h4>
    <p>Color accents, M3 bottomsheets, and Auto-Prompt update modes</p>
  </div>
</div>

<!-- Feature Comparison Table -->
<div class="packora-section-header">
  <span class="packora-section-badge">Competitive Matrix</span>
  <h2>Why Packora Outperforms the Rest</h2>
  <p class="packora-section-subtitle">See how Packora compares to browser home shortcuts, wrapper apps, and cloud builders.</p>
</div>

<div class="packora-comparison-table-wrapper">
  <table class="packora-comparison-table">
    <thead>
      <tr>
        <th>Feature</th>
        <th class="highlight-col">Packora v5.1.0</th>
        <th>Chrome PWA Shortcut</th>
        <th>Hermit Lite Apps</th>
        <th>TWA / Bubblewrap</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Standalone Native APK</strong></td>
        <td class="highlight-col check">✅ Real APK file</td>
        <td class="cross">❌ Browser tab only</td>
        <td class="cross">❌ Sandbox container</td>
        <td class="check">✅ Real APK file</td>
      </tr>
      <tr>
        <td><strong>Zero PC / Cloud Dependency</strong></td>
        <td class="highlight-col check">✅ 100% On-Device</td>
        <td class="check">✅ On-Device</td>
        <td class="check">✅ On-Device</td>
        <td class="cross">❌ Requires Node + JDK PC</td>
      </tr>
      <tr>
        <td><strong>Deterministic RSA-3072 Signing</strong></td>
        <td class="highlight-col check">✅ Isolated per package</td>
        <td class="cross">❌ N/A</td>
        <td class="cross">❌ Shared certificate</td>
        <td class="warning">⚠️ Manual keystore setup</td>
      </tr>
      <tr>
        <td><strong>50+ Vector Anti-Fingerprinting</strong></td>
        <td class="highlight-col check">✅ Canvas, WebGL, Audio, WebRTC</td>
        <td class="cross">❌ None</td>
        <td class="warning">⚠️ Basic User-Agent only</td>
        <td class="cross">❌ None</td>
      </tr>
      <tr>
        <td><strong>12 On-Device Server Runtimes</strong></td>
        <td class="highlight-col check">✅ Node, PHP, Python, Go, WP</td>
        <td class="cross">❌ None</td>
        <td class="cross">❌ None</td>
        <td class="cross">❌ None</td>
      </tr>
      <tr>
        <td><strong>HTML5 Web Notifications Bridge</strong></td>
        <td class="highlight-col check">✅ Native Android alerts</td>
        <td class="warning">⚠️ Web Push only</td>
        <td class="cross">❌ Web feeds only</td>
        <td class="check">✅ Web Push via Firebase</td>
      </tr>
      <tr>
        <td><strong>Encrypted DNS-over-HTTPS (DoH)</strong></td>
        <td class="highlight-col check">✅ 9 Resolvers + Strict DoH</td>
        <td class="cross">❌ OS Default</td>
        <td class="cross">❌ OS Default</td>
        <td class="cross">❌ OS Default</td>
      </tr>
      <tr>
        <td><strong>0ms Early CSS Footer Suppressor</strong></td>
        <td class="highlight-col check">✅ Instant pre-paint hide</td>
        <td class="cross">❌ None</td>
        <td class="warning">⚠️ Post-load script</td>
        <td class="cross">❌ None</td>
      </tr>
    </tbody>
  </table>
</div>

<!-- 12 App Types Section -->
<div class="packora-section-header">
  <span class="packora-section-badge">Versatile Studio</span>
  <h2>Twelve App Target Types</h2>
  <p class="packora-section-subtitle">Every project is unique. Packora provides dedicated runtimes tailored for each architecture.</p>
</div>

<div class="packora-types-grid">
  <div class="packora-type-tile">
    <a href="/guide/app-types/web">Standard Web</a>
    <p>Lightweight, high-speed single URL wrapper with full PWA service worker support.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/multi-web">Multi-Web Hub</a>
    <p>Tabbed portals, multi-domain browsers, and link feeds in a unified interface.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/html">HTML & Offline Pack</a>
    <p>Package local HTML/CSS/JS or scrape any website into a standalone offline app.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/frontend">Frontend SPA</a>
    <p>Ship React, Vue, Svelte, or Vite builds served cleanly over local internal assets.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/nodejs">Node.js Server</a>
    <p>Fork+exec real Node.js binaries from app storage to run Express, Fastify, and APIs.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/php">PHP Server</a>
    <p>Native PHP CLI runtime serving dynamic scripts and internal web servers on localhost.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/python">Python Environment</a>
    <p>Embedded Python interpreter running Flask, FastAPI, or offline automation scripts.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/go">Go Microservice</a>
    <p>Ultra-efficient compiled Go web services running natively in app storage.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/wordpress">WordPress Portable</a>
    <p>Complete WordPress site powered by PHP and SQLite running fully on-device.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/media">Media Streamer</a>
    <p>Hardware-accelerated HTML5 audio/video player with fullscreen reparenting.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/gallery">Gallery Showcase</a>
    <p>Standalone image and portfolio galleries with smooth touch interactions.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/more-features/app-modifier">App Cloner</a>
    <p>Clone, rebrand, and customize existing APKs with custom assets and package names.</p>
  </div>
</div>

<!-- Stats Ribbon -->
<div class="packora-stats">
  <div class="packora-stat"><b>12</b><span>App Types</span></div>
  <div class="packora-stat"><b>50+</b><span>Privacy Vectors</span></div>
  <div class="packora-stat"><b>9</b><span>DoH Resolvers</span></div>
  <div class="packora-stat"><b>RSA-3072</b><span>Per-App Keys</span></div>
  <div class="packora-stat"><b>&lt; 800ms</b><span>On-Device Build</span></div>
</div>

<!-- Bottom Call to Action -->
<div class="packora-cta">
  <div class="cta-glow-effect"></div>
  <div class="cta-inner">
    <h3>Ready to Build Your First WebAPK?</h3>
    <p>Download the latest release of Packora and start compiling your web projects into native Android apps directly on your phone.</p>
    <div class="cta-actions">
      <a class="cta-primary-btn" href="/guide/getting-started">Get Started with Packora</a>
      <a class="cta-secondary-btn" href="https://github.com/Maheswara660/Packora" target="_blank" rel="noreferrer">Star on GitHub</a>
    </div>
  </div>
</div>

</div>
