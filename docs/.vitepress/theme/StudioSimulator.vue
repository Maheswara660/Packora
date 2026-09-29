<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface ArchitectureTarget {
  id: string
  title: string
  badge: string
  description: string
}

interface DnsResolver {
  id: string
  name: string
  endpoint: string
  features: string
}

interface PrivacyVector {
  category: string
  vectorName: string
  attackSurface: string
  mitigation: string
}

interface PipelineStep {
  stepNumber: number
  title: string
  detail: string
  durationMs: number
}

interface Preset {
  name: string
  url: string
  pkg: string
  icon: string
  theme: string
  arch: string
}

const presets: Preset[] = [
  { name: 'Linear', url: 'https://linear.app', pkg: 'app.linear.packora', icon: '⚡', theme: '#000000', arch: 'web' },
  { name: 'GitHub', url: 'https://github.com', pkg: 'com.github.packora', icon: '🐙', theme: '#000000', arch: 'web' },
  { name: 'Notion', url: 'https://notion.so', pkg: 'so.notion.packora', icon: '📝', theme: '#000000', arch: 'frontend' },
  { name: 'Excalidraw', url: 'https://excalidraw.com', pkg: 'com.excalidraw.packora', icon: '🎨', theme: '#000000', arch: 'html' }
]

// Fallback targets matching KMP definitions
const defaultTargets: ArchitectureTarget[] = [
  { id: 'web', title: 'Default Web App', badge: 'Universal PWA', description: 'Full standalone WebAPK with independent task affinity and custom keystores.' },
  { id: 'html', title: 'Offline HTML Pack', badge: '100% Offline', description: 'Self-contained local web assets bundled into assets/www/ with zero network dependency.' },
  { id: 'frontend', title: 'Frontend SPA', badge: 'SPA Router', description: 'Single Page App with client-side history routing fallback for React, Vue, Vite, Nuxt.' },
  { id: 'multi-web', title: 'Multi-Web Hub', badge: 'Tabbed Workspace', description: 'Aggregates multiple destinations with an interactive native dark pill navigation bar.' },
  { id: 'media', title: 'Media Streamer', badge: 'Background Audio', description: 'Video and audio player with auto-keep-screen-on and uninterrupted background playback.' }
]

const defaultResolvers: DnsResolver[] = [
  { id: 'cloudflare', name: 'Cloudflare DNS', endpoint: 'https://cloudflare-dns.com/dns-query', features: 'Ultra-fast global Anycast (1.1.1.1)' },
  { id: 'google', name: 'Google Public DNS', endpoint: 'https://dns.google/dns-query', features: 'High-capacity worldwide infrastructure (8.8.8.8)' },
  { id: 'adguard', name: 'AdGuard DNS', endpoint: 'https://dns.adguard-dns.com/dns-query', features: 'Built-in ad & tracker blocking resolver' },
  { id: 'quad9', name: 'Quad9 DNS', endpoint: 'https://dns.quad9.net/dns-query', features: 'Swiss privacy-first security & threat protection' },
  { id: 'mullvad', name: 'Mullvad DoH', endpoint: 'https://doh.mullvad.net/dns-query', features: 'Strict zero-logging audited privacy resolver' },
  { id: 'controld', name: 'Control D', endpoint: 'https://freedns.controld.com/p0', features: 'High-performance privacy resolver with zero telemetry' },
  { id: 'dnssb', name: 'DNS.SB', endpoint: 'https://doh.dns.sb/dns-query', features: 'European privacy-first non-censored DNS resolver' },
  { id: 'cleanbrowsing', name: 'CleanBrowsing Security', endpoint: 'https://doh.cleanbrowsing.org/doh/security-filter/', features: 'Malware, phishing & malicious domain blocker' },
  { id: 'opendns', name: 'OpenDNS', endpoint: 'https://doh.opendns.com/dns-query', features: 'Cisco Anycast recursive DNS network' }
]

const defaultVectors: PrivacyVector[] = [
  { category: 'Canvas 2D', vectorName: 'toDataURL & getImageData', attackSurface: 'Pixel hashing to identify browser graphics stack', mitigation: 'Injects microscopic, deterministic pixel noise to randomize tracking hashes without visual degradation.' },
  { category: 'WebGL', vectorName: 'UNMASKED_RENDERER_WEBGL', attackSurface: 'GPU vendor, renderer strings & extensions profiling', mitigation: 'Spoofs GPU strings to generic high-end Adreno/Mali profiles and masks shader precision.' },
  { category: 'AudioContext', vectorName: 'OscillatorNode & AnalyserNode', attackSurface: 'Acoustic frequency response & FFT fingerprinting', mitigation: 'Applies sub-audible jitter (+/- 0.0001) to buffer frequencies, breaking audio fingerprint curves.' },
  { category: 'DOM Geometry', vectorName: 'getClientRects & getBoundingClientRect', attackSurface: 'Subpixel font rendering & display scaling telemetry', mitigation: 'Injects microscopic fractional floating-point jitter to subpixel coordinate readbacks.' },
  { category: 'WebRTC', vectorName: 'RTCPeerConnection ICE Candidates', attackSurface: 'Local intranet IP address & network topology leakage', mitigation: 'Blocks host-type ICE candidate generation, preventing local private IP leakages.' },
  { category: 'Battery API', vectorName: 'navigator.getBattery()', attackSurface: 'Battery charging level & discharge time fingerprinting', mitigation: 'Spoofs charging status to 100% constant, preventing timing-based session correlation.' },
  { category: 'Device Memory', vectorName: 'navigator.deviceMemory', attackSurface: 'RAM capacity device categorization', mitigation: 'Normalizes memory reporting to standard 8GB profiles across all devices.' },
  { category: 'Hardware Concurrency', vectorName: 'navigator.hardwareConcurrency', attackSurface: 'CPU core count correlation', mitigation: 'Clamps CPU core reports to standard 8-core mobile baseline.' },
  { category: 'Data Hygiene', vectorName: 'Storage & Cache Eviction', attackSurface: 'Persistent cross-session tracking via IndexedDB/Cookies', mitigation: 'Performs comprehensive cache, DOM storage, and cookie wiping upon application exit when enabled.' }
]

const kmpVersion = ref('5.4.0')
const targets = ref<ArchitectureTarget[]>(defaultTargets)
const resolvers = ref<DnsResolver[]>(defaultResolvers)
const privacyVectors = ref<PrivacyVector[]>(defaultVectors)

const activePreset = ref<Preset>(presets[0])
const selectedArch = ref<string>('web')
const selectedDns = ref<string>('cloudflare')
const customUrl = ref(presets[0].url)
const customName = ref(presets[0].name)
const customPkg = ref(presets[0].pkg)

// Toggles
const privacyShield = ref(true)
const adBlock = ref(true)
const instantFooter = ref(true)
const encryptedDns = ref(true)
const webNotifications = ref(true)
const perAppSigning = ref(true)

// Building state
const isBuilding = ref(false)
const buildProgress = ref(0)
const buildStage = ref('')
const buildDone = ref(false)
const notificationTriggered = ref(false)
const activeTab = ref<'studio' | 'vectors' | 'specs'>('studio')

onMounted(() => {
  if (typeof window !== 'undefined' && (window as any).PackoraKMP) {
    const kmp = (window as any).PackoraKMP
    if (kmp.getVersion) kmpVersion.value = kmp.getVersion()
    if (kmp.getTargets) targets.value = kmp.getTargets()
    if (kmp.getResolvers) resolvers.value = kmp.getResolvers()
    if (kmp.getPrivacyVectors) privacyVectors.value = kmp.getPrivacyVectors()
  }
})

function selectPreset(p: Preset) {
  activePreset.value = p
  customUrl.value = p.url
  customName.value = p.name
  customPkg.value = p.pkg
  selectedArch.value = p.arch
  buildDone.value = false
  buildProgress.value = 0
}

function startBuild() {
  if (isBuilding.value) return
  isBuilding.value = true
  buildDone.value = false
  buildProgress.value = 5
  buildStage.value = 'Initializing Packora KMP Compiler v' + kmpVersion.value + '...'

  let pipeline: PipelineStep[] = []
  if (typeof window !== 'undefined' && (window as any).PackoraKMP?.getPipeline) {
    pipeline = (window as any).PackoraKMP.getPipeline(
      selectedArch.value,
      customUrl.value,
      customName.value,
      customPkg.value
    )
  }

  if (!pipeline || pipeline.length === 0) {
    pipeline = [
      { stepNumber: 1, title: 'Parsing Web Target', detail: `Harvesting manifest from ${customUrl.value}...`, durationMs: 300 },
      { stepNumber: 2, title: 'Extracting Template APK', detail: 'Staging webview_shell.apk base container (Android 15 ready)...', durationMs: 250 },
      { stepNumber: 3, title: 'In-House AXML Patching', detail: `Patching AndroidManifest.xml: package=${customPkg.value}...`, durationMs: 350 },
      { stepNumber: 4, title: 'In-House ARSC Rebuilding', detail: `Updating string pools: appName="${customName.value}"...`, durationMs: 300 },
      { stepNumber: 5, title: '16KB ELF Boundary Alignment', detail: 'Aligning native shared libraries to 16,384-byte boundaries...', durationMs: 350 },
      { stepNumber: 6, title: 'Deterministic Keystore Gen', detail: `Computing isolated RSA-3072 key for ${customPkg.value}...`, durationMs: 400 },
      { stepNumber: 7, title: 'APK Signature Scheme v2/v3', detail: 'Applying cryptographic signature blocks...', durationMs: 300 },
      { stepNumber: 8, title: 'WebAPK Compilation Complete', detail: 'Production-ready standalone WebAPK ready for in-place updates.', durationMs: 200 }
    ]
  }

  let currentStep = 0
  const total = pipeline.length

  function runNext() {
    if (currentStep < total) {
      const step = pipeline[currentStep]
      buildStage.value = `[${step.stepNumber}/${total}] ${step.title}: ${step.detail}`
      buildProgress.value = Math.round(((currentStep + 1) / total) * 100)
      currentStep++
      setTimeout(runNext, step.durationMs || 300)
    } else {
      isBuilding.value = false
      buildDone.value = true
    }
  }

  setTimeout(runNext, 200)
}

function testNotification() {
  notificationTriggered.value = true
  setTimeout(() => {
    notificationTriggered.value = false
  }, 4000)
}
</script>

<template>
  <div class="studio-sim-wrapper">
    <!-- Tab Navigation -->
    <div class="studio-tabs">
      <button
        class="studio-tab-btn"
        :class="{ active: activeTab === 'studio' }"
        @click="activeTab = 'studio'"
      >
        <span class="tab-icon">⚡</span>
        <span>Interactive Studio</span>
      </button>
      <button
        class="studio-tab-btn"
        :class="{ active: activeTab === 'vectors' }"
        @click="activeTab = 'vectors'"
      >
        <span class="tab-icon">🛡️</span>
        <span>50+ Privacy Vectors</span>
      </button>
      <button
        class="studio-tab-btn"
        :class="{ active: activeTab === 'specs' }"
        @click="activeTab = 'specs'"
      >
        <span class="tab-icon">⚙️</span>
        <span>Engine Specs</span>
      </button>
    </div>

    <!-- TAB 1: STUDIO BUILDER -->
    <div v-if="activeTab === 'studio'" class="studio-main-card">
      <div class="studio-card-header">
        <div class="studio-header-left">
          <div class="studio-traffic-lights">
            <span class="traffic-light red"></span>
            <span class="traffic-light yellow"></span>
            <span class="traffic-light green"></span>
          </div>
          <span class="studio-title-badge">Packora Studio Compiler v{{ kmpVersion }}</span>
        </div>
        <div class="studio-header-right">
          <span class="studio-status-pill">
            <span class="status-dot"></span> Kotlin Multiplatform Core Active
          </span>
        </div>
      </div>

      <!-- Presets bar -->
      <div class="preset-selector">
        <span class="preset-label">Quick Presets:</span>
        <button
          v-for="p in presets"
          :key="p.name"
          class="preset-chip"
          :class="{ active: activePreset.name === p.name }"
          @click="selectPreset(p)"
        >
          <span>{{ p.icon }}</span>
          <span>{{ p.name }}</span>
        </button>
      </div>

      <!-- Architecture Picker (5 True Architectures) -->
      <div class="arch-selector-section">
        <label class="section-label">Target Architecture (5 Core Engines):</label>
        <div class="arch-chips-grid">
          <button
            v-for="t in targets"
            :key="t.id"
            class="arch-chip"
            :class="{ active: selectedArch === t.id }"
            @click="selectedArch = t.id"
          >
            <div class="arch-chip-top">
              <span class="arch-chip-title">{{ t.title }}</span>
              <span class="arch-chip-badge">{{ t.badge }}</span>
            </div>
            <p class="arch-chip-desc">{{ t.description }}</p>
          </button>
        </div>
      </div>

      <!-- URL & Metadata Row -->
      <div class="studio-meta-grid">
        <div class="input-group">
          <label>Target URL / Bundle Endpoint</label>
          <div class="input-with-icon">
            <span class="input-icon">🔗</span>
            <input v-model="customUrl" type="url" placeholder="https://example.com" />
          </div>
        </div>
        <div class="input-group">
          <label>App Name</label>
          <div class="input-with-icon">
            <span class="input-icon">📱</span>
            <input v-model="customName" type="text" placeholder="App Name" />
          </div>
        </div>
        <div class="input-group">
          <label>Package Identity</label>
          <div class="input-with-icon">
            <span class="input-icon">📦</span>
            <input v-model="customPkg" type="text" placeholder="com.example.packora" />
          </div>
        </div>
      </div>

      <!-- Hardening & Features Toggles Grid -->
      <div class="toggles-grid">
        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">🛡️</span>
              <b>50+ Vector Stealth Shield</b>
            </div>
            <p>Spoof Canvas 2D, WebGL GPU, AudioContext & WebRTC local IP</p>
          </div>
          <label class="switch-ui">
            <input v-model="privacyShield" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>

        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">🚫</span>
              <b>EasyList Ad & Tracker Blocker</b>
            </div>
            <p>70,000+ rules matched in WebView pipeline with 0ms overhead</p>
          </div>
          <label class="switch-ui">
            <input v-model="adBlock" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>

        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">⚡</span>
              <b>Instant 0ms Footer Hider</b>
            </div>
            <p>Early CSS injection hides annoying web footers before initial paint</p>
          </div>
          <label class="switch-ui">
            <input v-model="instantFooter" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>

        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">🔒</span>
              <b>Encrypted DNS-over-HTTPS (DoH)</b>
            </div>
            <p>9 built-in privacy resolvers with zero ISP tracking</p>
          </div>
          <label class="switch-ui">
            <input v-model="encryptedDns" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>

        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">🔔</span>
              <b>Native HTML5 Notifications</b>
            </div>
            <p>Real Android system bar push notifications with badge</p>
          </div>
          <label class="switch-ui">
            <input v-model="webNotifications" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>

        <div class="toggle-card">
          <div class="toggle-info">
            <div class="toggle-title">
              <span class="toggle-icon">🔑</span>
              <b>Deterministic RSA-3072 Keys</b>
            </div>
            <p>Isolated signing identities for conflict-free in-place updates</p>
          </div>
          <label class="switch-ui">
            <input v-model="perAppSigning" type="checkbox" />
            <span class="switch-slider"></span>
          </label>
        </div>
      </div>

      <!-- Action & Progress Bar -->
      <div class="studio-action-area">
        <div v-if="isBuilding || buildDone" class="build-progress-box">
          <div class="progress-bar-track">
            <div class="progress-bar-fill" :style="{ width: buildProgress + '%' }"></div>
          </div>
          <div class="progress-info-row">
            <span class="progress-stage-text">
              <span v-if="isBuilding" class="spin-dot">⏳</span>
              <span v-else>✅</span>
              {{ buildStage }}
            </span>
            <span class="progress-pct">{{ buildProgress }}%</span>
          </div>
        </div>

        <div class="action-buttons-row">
          <button
            class="build-trigger-btn"
            :disabled="isBuilding"
            @click="startBuild"
          >
            <span class="btn-glow"></span>
            <span class="btn-content">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
              <span>{{ isBuilding ? 'Compiling WebAPK...' : buildDone ? 'Rebuild WebAPK' : 'Compile WebAPK Directly On Device' }}</span>
            </span>
          </button>

          <button
            v-if="webNotifications"
            class="test-notif-btn"
            @click="testNotification"
          >
            <span>🔔</span>
            <span>Test Web Notification</span>
          </button>
        </div>

        <!-- Simulated Native Android Notification Banner -->
        <transition name="pop-in">
          <div v-if="notificationTriggered" class="simulated-notification">
            <div class="notif-header">
              <span class="notif-app-name">{{ customName }} • Android System</span>
              <span class="notif-time">now</span>
            </div>
            <div class="notif-body">
              <div class="notif-icon-box">{{ activePreset.icon }}</div>
              <div class="notif-text">
                <b>New Update Available</b>
                <p>Packora HTML5 Web Notification Bridge active and delivering system-level alerts!</p>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- TAB 2: 50+ PRIVACY VECTORS -->
    <div v-if="activeTab === 'vectors'" class="studio-detail-card">
      <div class="detail-card-head">
        <h3>50+ Real-Time Stealth Anti-Fingerprinting Defenses</h3>
        <p>Packora intercepts invasive tracking scripts directly at the DOM API layer before execution.</p>
      </div>
      <div class="vectors-grid">
        <div v-for="vec in privacyVectors" :key="vec.category + vec.vectorName" class="vector-item">
          <span class="vector-badge">{{ vec.category }}</span>
          <h4>{{ vec.vectorName }}</h4>
          <p class="vector-attack"><strong>Attack:</strong> {{ vec.attackSurface }}</p>
          <p class="vector-mitigation"><strong>Defense:</strong> {{ vec.mitigation }}</p>
        </div>
      </div>
    </div>

    <!-- TAB 3: RUNTIME & SPECS -->
    <div v-if="activeTab === 'specs'" class="studio-detail-card">
      <div class="detail-card-head">
        <h3>Architecture & Engine Performance Specifications</h3>
        <p>Built with Jetpack Compose, native binary bytecode manipulation, and Android Signature Scheme V1/V2/V3.</p>
      </div>
      <div class="specs-grid">
        <div class="spec-card">
          <span class="spec-metric">&lt; 1.2s</span>
          <span class="spec-title">On-Device Compile Time</span>
          <p>Direct in-memory AXML patching, resource re-indexing, and V1/V2 signing with zero PC needed.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">16KB</span>
          <span class="spec-title">ELF Page Alignment</span>
          <p>Full support for modern Android 15+ kernels with strict 16,384-byte ELF boundaries.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">5</span>
          <span class="spec-title">True Architectures</span>
          <p>PWA/URL Web App, Offline HTML5, Frontend SPA, Multi-Web Tabbed Hub, and Immersive Media.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">RSA-3072</span>
          <span class="spec-title">Deterministic Keys</span>
          <p>Isolated per-package cryptographic keystores preventing update signature mismatch errors.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.studio-sim-wrapper {
  margin: 36px 0;
  position: relative;
}

/* Tab Navigation */
.studio-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  background: rgba(0, 0, 0, 0.4);
  padding: 6px;
  border-radius: 14px;
  border: 1px solid var(--packora-glass-border);
  backdrop-filter: blur(12px);
  width: fit-content;
}

.studio-tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--vp-c-text-2);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.studio-tab-btn:hover {
  color: var(--vp-c-text-1);
}

.studio-tab-btn.active {
  background: #ffffff;
  color: #000000;
  border: 1px solid #ffffff;
}

/* Main Card */
.studio-main-card, .studio-detail-card {
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--packora-glass-border);
  border-radius: 20px;
  padding: 24px;
}

.studio-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--packora-glass-border);
  margin-bottom: 20px;
}

.studio-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.studio-traffic-lights {
  display: flex;
  gap: 6px;
}

.traffic-light {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.traffic-light.red { background: #555555; }
.traffic-light.yellow { background: #888888; }
.traffic-light.green { background: #cccccc; }

.studio-title-badge {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
  letter-spacing: 0.3px;
}

.studio-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--vp-c-text-1);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--packora-glass-border);
  padding: 4px 10px;
  border-radius: 20px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 8px #ffffff;
}

/* Presets */
.preset-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.preset-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--vp-c-text-2);
}

.preset-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  padding: 6px 12px;
  border-radius: 10px;
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: all 0.2s;
}

.preset-chip:hover {
  color: var(--vp-c-text-1);
  border-color: rgba(255, 255, 255, 0.4);
}

.preset-chip.active {
  background: #ffffff;
  color: #000000;
  border-color: #ffffff;
  font-weight: 700;
}

/* Architecture Selector Section */
.arch-selector-section {
  margin-bottom: 20px;
}

.section-label {
  display: block;
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--vp-c-text-2);
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.arch-chips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.arch-chip {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--packora-glass-border);
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
}

.arch-chip:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.3);
}

.arch-chip.active {
  background: rgba(255, 255, 255, 0.12);
  border-color: #ffffff;
}

.arch-chip-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.arch-chip-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.arch-chip-badge {
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.15);
  color: var(--vp-c-text-1);
}

.arch-chip-desc {
  font-size: 0.74rem;
  color: var(--vp-c-text-2);
  margin: 0;
  line-height: 1.35;
}

/* Inputs */
.studio-meta-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr;
  gap: 14px;
  margin-bottom: 20px;
}

@media (max-width: 800px) {
  .studio-meta-grid {
    grid-template-columns: 1fr;
  }
}

.input-group label {
  display: block;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--vp-c-text-2);
  margin-bottom: 6px;
}

.input-with-icon {
  display: flex;
  align-items: center;
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  border-radius: 10px;
  padding: 0 12px;
  transition: border-color 0.2s;
}

.input-with-icon:focus-within {
  border-color: #ffffff;
}

.input-icon {
  margin-right: 8px;
  font-size: 0.9rem;
}

.input-with-icon input {
  width: 100%;
  padding: 10px 0;
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.85rem;
  color: var(--vp-c-text-1);
}

/* Toggles Grid */
.toggles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

@media (max-width: 640px) {
  .toggles-grid {
    grid-template-columns: 1fr;
  }
}

.toggle-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  border-radius: 12px;
  padding: 14px 16px;
  transition: all 0.2s;
}

.toggle-card:hover {
  border-color: rgba(255, 255, 255, 0.25);
}

.toggle-info {
  flex: 1;
  padding-right: 14px;
}

.toggle-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
  color: var(--vp-c-text-1);
  margin-bottom: 4px;
}

.toggle-info p {
  font-size: 0.75rem;
  color: var(--vp-c-text-2);
  margin: 0;
  line-height: 1.35;
}

/* iOS Switch UI */
.switch-ui {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 26px;
  flex-shrink: 0;
}

.switch-ui input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #333333;
  transition: .25s;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 2px;
  bottom: 2px;
  background-color: #888888;
  transition: .25s;
  border-radius: 50%;
}

.switch-ui input:checked + .switch-slider {
  background-color: #ffffff;
}

.switch-ui input:checked + .switch-slider:before {
  transform: translateX(18px);
  background-color: #000000;
}

/* Action Area */
.studio-action-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.build-progress-box {
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  border-radius: 12px;
  padding: 14px;
}

.progress-bar-track {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-bar-fill {
  height: 100%;
  background: #ffffff;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--vp-c-text-1);
}

.progress-stage-text {
  font-family: var(--vp-font-family-mono);
  font-size: 0.78rem;
}

.progress-pct {
  font-weight: 700;
}

.action-buttons-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.build-trigger-btn {
  flex: 2;
  position: relative;
  overflow: hidden;
  background: #ffffff;
  color: #000000;
  border: 1px solid #ffffff;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 0.92rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.build-trigger-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 255, 255, 0.2);
}

.build-trigger-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.test-notif-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--vp-c-bg-mute);
  color: var(--vp-c-text-1);
  border: 1px solid var(--packora-glass-border);
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.test-notif-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
}

/* Simulated Android Notification */
.simulated-notification {
  background: #111111;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 14px 18px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.notif-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #aaaaaa;
  margin-bottom: 8px;
}

.notif-body {
  display: flex;
  gap: 12px;
  align-items: center;
}

.notif-icon-box {
  width: 36px;
  height: 36px;
  background: #222222;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.notif-text b {
  font-size: 0.85rem;
  color: #ffffff;
  display: block;
}

.notif-text p {
  font-size: 0.78rem;
  color: #bbbbbb;
  margin: 2px 0 0 0;
}

/* Vectors Grid */
.detail-card-head {
  margin-bottom: 20px;
}

.detail-card-head h3 {
  font-size: 1.2rem;
  font-weight: 800;
  color: var(--vp-c-text-1);
  margin-bottom: 6px;
}

.detail-card-head p {
  color: var(--vp-c-text-2);
  font-size: 0.88rem;
  margin: 0;
}

.vectors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.vector-item {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--packora-glass-border);
  border-radius: 14px;
  padding: 16px;
  transition: all 0.2s;
}

.vector-item:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.vector-badge {
  display: inline-block;
  font-size: 0.72rem;
  font-weight: 700;
  color: #000000;
  background: #ffffff;
  padding: 3px 8px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.vector-item h4 {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
  margin-bottom: 8px;
}

.vector-attack {
  font-size: 0.78rem;
  color: #bbbbbb;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.vector-mitigation {
  font-size: 0.78rem;
  color: #ffffff;
  margin: 0;
  line-height: 1.4;
}

/* Specs Grid */
.specs-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 800px) {
  .specs-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.spec-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--packora-glass-border);
  border-radius: 14px;
  padding: 20px 16px;
  text-align: center;
}

.spec-metric {
  display: block;
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--vp-c-text-1);
  margin-bottom: 6px;
}

.spec-title {
  display: block;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
  margin-bottom: 6px;
}

.spec-card p {
  font-size: 0.78rem;
  color: var(--vp-c-text-2);
  margin: 0;
  line-height: 1.4;
}

.pop-in-enter-active, .pop-in-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.pop-in-enter-from, .pop-in-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}
</style>
