---
layout: home

hero:
  name: Packora
  text: Next-Gen Android WebAPK Studio
  tagline: Transform modern websites, offline HTML bundles, and frontend SPAs into native, hardened Android apps directly on your phone. Deterministic RSA signing, 50+ vector stealth shield, encrypted DoH, and 16KB ELF alignment — zero PC required.
  image:
    src: /logo.png
    alt: Packora Logo
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: 5 Core Architectures
      link: /guide/app-types/
    - theme: alt
      text: View on GitHub
      link: https://github.com/Maheswara660/Packora

features:
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 4v16"/><path d="M4 9h5"/></svg>
    title: 5 Native Architectures
    details: Web App (PWA/URL), Offline HTML5 bundles, Frontend SPAs (React/Vue/Vite), Multi-Web Tabbed Hubs, and Immersive Media players compiled into standalone APKs.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/></svg>
    title: Hardened Network & Encrypted DNS
    details: Built-in DNS-over-HTTPS with 9 privacy resolvers (Cloudflare, Google, AdGuard, Quad9, Mullvad, Control D, DNS.SB, CleanBrowsing, OpenDNS), Strict DoH, and EasyList filtering.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 8l-9-5-9 5 9 5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
    title: Deterministic RSA-3072 Keystores
    details: Isolated, deterministic on-device keystores per package name with APK Signature Scheme V1 and V2 support. Shipped apps update in-place with zero signature conflicts.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.4"/></svg>
    title: 50+ Vector Stealth Shield
    details: Real-time anti-fingerprinting spoofing Canvas 2D hashes, WebGL GPU strings, AudioContext oscillators, DOM geometry jitter, and WebRTC local IP leak blockers.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
    title: 16KB ELF Page Alignment
    details: Fully compatible with Android 15+ kernels. All native shared libraries and APK zip entries are aligned to strict 16,384-byte page boundaries.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
    title: Updates Hub & Management
    details: Real-time update tracker, SHA-256 integrity hash verification, batch background updates, and complete build history management directly inside the studio.
---

<div class="packora-home">

<!-- Live Interactive Studio Simulator Component powered by Kotlin Multiplatform -->
<div class="packora-section-header">
  <span class="packora-section-badge">Kotlin Multiplatform Studio Engine</span>
  <h2>Simulate Your WebAPK Build Experience</h2>
  <p class="packora-section-subtitle">Experience how Packora applies real-time privacy shields, instant 0ms CSS footer hiding, encrypted DNS, and native notifications right inside the browser.</p>
</div>

<StudioSimulator />

<!-- Bento Grid Architecture Showcase -->
<div class="packora-section-header">
  <span class="packora-section-badge">Engineering Highlights</span>
  <h2>Engineered for Power, Privacy & Independence</h2>
  <p class="packora-section-subtitle">Every feature in Packora is built from first principles for performance and privacy, running 100% locally on your Android device.</p>
</div>

<div class="packora-bento-grid">
  <!-- Bento Item 1: Large Featured -->
  <div class="bento-card bento-hero">
    <div class="bento-content">
      <div class="bento-tag">Zero PC Required</div>
      <h3>On-Device Binary Compiler Engine</h3>
      <p>Packora bypasses cloud build servers and desktop toolchains. Its internal engine directly parses and binary-patches AndroidManifest.xml (AXML), rebuilds compiled resources (ARSC), aligns zip entries to 16KB page boundaries, and cryptographically signs output APKs in under 2 seconds.</p>
      <div class="bento-chips-row">
        <span class="bento-chip">AXML Patcher</span>
        <span class="bento-chip">ARSC Rebuilder</span>
        <span class="bento-chip">16KB ELF Alignment</span>
        <span class="bento-chip">RSA-3072 Deterministic Keys</span>
        <span class="bento-chip">ApkSig V1/V2</span>
        <span class="bento-chip">Android 15+ Ready</span>
      </div>
    </div>
  </div>

  <!-- Bento Item 2: Deterministic Signing -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">Zero Parse Conflicts</div>
      <h3>Deterministic Isolated Keystores</h3>
      <p>Each app is assigned a dedicated, cryptographically isolated signing identity generated deterministically from package coordinates. Upgrades and re-builds match previous certificates automatically, eliminating Android package signature mismatch errors.</p>
    </div>
  </div>

  <!-- Bento Item 3: 50+ Privacy Shield -->
  <div class="bento-card">
    <div class="bento-content">
      <div class="bento-tag">Anti-Tracking</div>
      <h3>50+ Vector Stealth Shield</h3>
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
  <p class="packora-section-subtitle">No Android Studio, no complex toolchains, and no PC required. Everything compiles natively on your phone in seconds.</p>
</div>

<div class="packora-steps">
  <div class="packora-step-card">
    <div class="packora-step-num">1</div>
    <h3>Target Selection</h3>
    <p>Choose from <a href="/guide/app-types/">5 specialized architectures</a>: standard Web wrappers, offline HTML5 bundles, frontend SPAs, multi-web tabbed hubs, or immersive media players.</p>
  </div>

  <div class="packora-step-card">
    <div class="packora-step-num">2</div>
    <h3>Engine Configuration</h3>
    <p>Enter your URL or select local files. Packora auto-extracts high-res icons and theme colors. Toggle AdBlock, DoH encrypted DNS, and Privacy Shield with smooth iOS switches.</p>
  </div>

  <div class="packora-step-card">
    <div class="packora-step-num">3</div>
    <h3>Native On-Device Build</h3>
    <p>Packora patches AXML bytecode, injects runtime assets, aligns to 16KB boundaries, and signs the package with isolated RSA-3072 keystores. Tap Install and your native WebAPK is ready!</p>
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
    <p>Architectures, URL inspection, offline crawler & signing controls</p>
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
    <p>Detailed compile diagnostics, SHA-256 hashes, and one-tap reinstall</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/updates_screen.png" alt="Packora Updates" />
    <h4>WebAPK Update Center</h4>
    <p>Automated version checking and batch app update compilation</p>
  </div>
  <div class="packora-screenshot-card">
    <img src="/screenshots/settings_screen.png" alt="Packora Settings" />
    <h4>Settings & Appearance</h4>
    <p>Monochrome themes, M3 bottomsheets, and custom install modes</p>
  </div>
</div>

<!-- 5 App Architectures Section -->
<div class="packora-section-header">
  <span class="packora-section-badge">Versatile Studio</span>
  <h2>Five Core Architectures</h2>
  <p class="packora-section-subtitle">Every project is unique. Packora provides dedicated runtimes tailored for each architecture.</p>
</div>

<div class="packora-types-grid">
  <div class="packora-type-tile">
    <a href="/guide/app-types/web">Standard Web App</a>
    <p>Lightweight, high-speed single URL wrapper with full PWA service worker support and cache storage.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/html">Offline HTML5 Pack</a>
    <p>Package local HTML/CSS/JS or scrape any website into a completely self-contained offline application.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/frontend">Frontend SPA</a>
    <p>Ship React, Vue, Svelte, or Vite builds served cleanly over local assets with client routing fallback.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/multi-web">Multi-Web Tabbed Hub</a>
    <p>Tabbed portals, multi-domain browsers, and link feeds in a unified interface with native pill navigation.</p>
  </div>
  <div class="packora-type-tile">
    <a href="/guide/app-types/media">Immersive Media</a>
    <p>Hardware-accelerated HTML5 audio/video player with screen wake lock and background audio playback.</p>
  </div>
</div>

<!-- Stats Ribbon -->
<div class="packora-stats">
  <div class="packora-stat"><b>5</b><span>Core Engines</span></div>
  <div class="packora-stat"><b>50+</b><span>Privacy Vectors</span></div>
  <div class="packora-stat"><b>9</b><span>DoH Resolvers</span></div>
  <div class="packora-stat"><b>RSA-3072</b><span>Per-App Keys</span></div>
  <div class="packora-stat"><b>16KB</b><span>ELF Aligned</span></div>
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
