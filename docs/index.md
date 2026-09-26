---
layout: home

hero:
  name: Packora
  text: Next-Gen Android WebAPK Studio
  tagline: Transform websites, SPAs, and server runtimes into native, high-performance, hardened Android applications directly on your phone. Native on-device signing, 50+ vector privacy shield, encrypted DoH, and offline bundling — zero PC required.
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
    details: Isolated, deterministic on-device keystores per package name with APK Signature Scheme V1, V2, and V3 support. Shipped apps update seamlessly.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.4"/></svg>
    title: 50+ Vector Privacy Shield
    details: Real-time anti-fingerprinting spoofing Canvas 2D hashes, WebGL GPU strings, AudioContext oscillators, DOM ClientRects jitter, and WebRTC local IP leak blockers.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M10 2v4M14 2v4M10 18v4M14 18v4M2 10h4M2 14h4M18 10h4M18 14h4"/><rect x="8" y="8" width="8" height="8" rx="1.5"/></svg>
    title: Smart Ad-Block & Cosmetic Filter
    details: High-speed domain blocking paired with dynamic MutationObserver cosmetic element suppression — banishes banners, tracking scripts, and white gaps.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18a14 14 0 0 1 0-18"/></svg>
    title: PWA Analyzer & Scraper
    details: Instant manifest extraction for 512x512 icons, theme colors, and metadata, plus a recursive web crawler that packages websites into offline portable apps.
---

<div class="packora-home">

<div class="packora-section-header">
  <span class="packora-section-badge">Fast Workflow</span>
  <h2>From URL to Signed APK in 3 Steps</h2>
  <p class="packora-section-subtitle">No Android Studio, no build queues, and no PC required. Everything compiles natively on your phone.</p>
</div>

<div class="packora-steps">

<div class="packora-step-card">
  <div class="packora-step-num">1</div>
  <h3>Choose Target Type</h3>
  <p>Select from <a href="/guide/app-types/">12 application types</a>: standard Web wrappers, offline HTML bundles, frontend SPAs, or on-device Node.js, PHP, Python, and Go servers.</p>
</div>

<div class="packora-step-card">
  <div class="packora-step-num">2</div>
  <h3>Configure Engine</h3>
  <p>Type your URL or select a project. Packora auto-extracts high-res icons and theme colors. Toggle AdBlock, DoH encrypted DNS, and Privacy Shield with smooth iOS switches.</p>
</div>

<div class="packora-step-card">
  <div class="packora-step-num">3</div>
  <h3>Build & Install</h3>
  <p>Packora modifies bytecode, patches AXML/ARSC, and signs the package with isolated RSA-3072 keystores (V1/V2/V3). Tap Install and your native WebAPK is ready!</p>
</div>

</div>

<!-- Interactive Packora Engine Controls Simulation -->
<div class="packora-section-header">
  <span class="packora-section-badge">Modern UI Architecture</span>
  <h2>Native Look & Feel, Powered by Jetpack Compose</h2>
  <p class="packora-section-subtitle">Packora is built with Material 3 design, custom spring iOS switches, dot loaders, and non-destructive bottomsheet menus.</p>
</div>

<div class="packora-demo-box">
  <div class="packora-demo-grid">
    <div class="packora-demo-item">
      <div class="packora-demo-info">
        <h4>50+ Vector Privacy Shield</h4>
        <p>Mask Canvas 2D, WebGL GPU, AudioContext & WebRTC IP</p>
      </div>
      <label class="packora-ios-switch">
        <input type="checkbox" checked />
        <span class="packora-ios-slider"></span>
      </label>
    </div>

    <div class="packora-demo-item">
      <div class="packora-demo-info">
        <h4>Next-Gen AdBlocker & Cosmetic DOM</h4>
        <p>Block ad hosts & hide ad containers in real-time</p>
      </div>
      <label class="packora-ios-switch">
        <input type="checkbox" checked />
        <span class="packora-ios-slider"></span>
      </label>
    </div>

    <div class="packora-demo-item">
      <div class="packora-demo-info">
        <h4>Strict DNS-over-HTTPS (DoH)</h4>
        <p>Encrypted queries via Cloudflare, Google, AdGuard, Quad9, Mullvad, Control D, NextDNS</p>
      </div>
      <label class="packora-ios-switch">
        <input type="checkbox" checked />
        <span class="packora-ios-slider"></span>
      </label>
    </div>

    <div class="packora-demo-item">
      <div class="packora-demo-info">
        <h4>Per-App RSA-3072 Keystore</h4>
        <p>Deterministic isolated signing identity per package</p>
      </div>
      <label class="packora-ios-switch">
        <input type="checkbox" checked />
        <span class="packora-ios-slider"></span>
      </label>
    </div>
  </div>
</div>

<!-- Screenshots Gallery Showcase -->
<div class="packora-section-header">
  <span class="packora-section-badge">UI Gallery</span>
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
</div>

<!-- Bottom Call to Action -->
<div class="packora-cta">
  <h3>Ready to Build Your First WebAPK?</h3>
  <p>Download the latest release of Packora and start compiling your web projects into native Android apps.</p>
  <a href="/guide/getting-started">Get Started with Packora</a>
</div>

</div>
