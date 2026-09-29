<script setup lang="ts">
import { ref, computed } from 'vue'

interface Preset {
  name: string
  url: string
  pkg: string
  icon: string
  theme: string
}

const presets: Preset[] = [
  { name: 'Linear', url: 'https://linear.app', pkg: 'app.linear.packora', icon: '⚡', theme: '#5e6ad2' },
  { name: 'GitHub', url: 'https://github.com', pkg: 'com.github.packora', icon: '🐙', theme: '#238636' },
  { name: 'Notion', url: 'https://notion.so', pkg: 'so.notion.packora', icon: '📝', theme: '#000000' },
  { name: 'Claude', url: 'https://claude.ai', pkg: 'ai.claude.packora', icon: '✨', theme: '#d97706' }
]

const activePreset = ref<Preset>(presets[0])
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

function selectPreset(p: Preset) {
  activePreset.value = p
  customUrl.value = p.url
  customName.value = p.name
  customPkg.value = p.pkg
  buildDone.value = false
  buildProgress.value = 0
}

function startBuild() {
  if (isBuilding.value) return
  isBuilding.value = true
  buildDone.value = false
  buildProgress.value = 5
  buildStage.value = 'Parsing PWA manifest & fetching 512x512 icons...'

  const stages = [
    { p: 25, msg: 'Injecting 0ms early CSS stylesheets & footer hider...' },
    { p: 50, msg: 'Bridging HTML5 Web Notifications to Android NotificationManager...' },
    { p: 75, msg: 'Applying 50+ vector anti-fingerprinting & DoH resolver...' },
    { p: 90, msg: 'Signing APK with deterministic RSA-3072 keystore (V1/V2/V3)...' },
    { p: 100, msg: 'Package compiled successfully! 3.4 MB standalone WebAPK ready.' }
  ]

  let step = 0
  const interval = setInterval(() => {
    if (step < stages.length) {
      buildProgress.value = stages[step].p
      buildStage.value = stages[step].msg
      step++
    } else {
      clearInterval(interval)
      isBuilding.value = false
      buildDone.value = true
    }
  }, 400)
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
        <span>Runtime & Specs</span>
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
          <span class="studio-title-badge">Packora WebAPK Compiler v5.2.0</span>
        </div>
        <div class="studio-header-right">
          <span class="studio-status-pill">
            <span class="status-dot"></span> On-Device Engine Ready
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

      <!-- URL & Metadata Row -->
      <div class="studio-meta-grid">
        <div class="input-group">
          <label>Target URL</label>
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
              <b>50+ Vector Privacy Shield</b>
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
              <b>Smart AdBlocker & Cosmetic DOM</b>
            </div>
            <p>Block tracking networks & collapse empty banner spaces</p>
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
            <p>Early CSS injection hides annoying web footers before paint</p>
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
            <p>Cloudflare, Google, AdGuard, Quad9 with Strict DoH mode</p>
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
              <b>Native HTML5 Web Notifications</b>
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
              <b>Deterministic RSA-3072 Signing</b>
            </div>
            <p>Prevents package parse errors during in-place app updates</p>
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
        <h3>50+ Real-Time Anti-Fingerprinting Defenses</h3>
        <p>Packora neutralizes modern invasive tracking scripts (FingerprintJS, CreepJS, DataDome, Cloudflare Bot Management) directly at the DOM API layer.</p>
      </div>
      <div class="vectors-grid">
        <div class="vector-item">
          <span class="vector-badge">Canvas 2D</span>
          <h4>Sub-Perceptual Noise Injection</h4>
          <p>Injects deterministic cryptographic micro-noise into <code>toDataURL()</code> and <code>getImageData()</code>, producing unique canvas hashes per session.</p>
        </div>
        <div class="vector-item">
          <span class="vector-badge">WebGL GPU</span>
          <h4>Renderer & Vendor Normalization</h4>
          <p>Spoofs <code>UNMASKED_RENDERER_WEBGL</code> to a generic Adreno/Mali GPU profile while preserving 60 FPS hardware acceleration.</p>
        </div>
        <div class="vector-item">
          <span class="vector-badge">AudioContext</span>
          <h4>Oscillator Frequency Jitter</h4>
          <p>Adds fractional phase shifting to DynamicsCompressor audio buffer rendering, defeating audio frequency fingerprinting.</p>
        </div>
        <div class="vector-item">
          <span class="vector-badge">DOM ClientRects</span>
          <h4>Sub-Pixel Dimension Jitter</h4>
          <p>Perturbs <code>getBoundingClientRect()</code> return values by ±0.0001px to scramble font and display scaling telemetry.</p>
        </div>
        <div class="vector-item">
          <span class="vector-badge">WebRTC Leak</span>
          <h4>Host Candidate Suppression</h4>
          <p>Neutralizes <code>RTCPeerConnection.createOffer</code> local IP probing, strictly preventing internal LAN address discovery.</p>
        </div>
        <div class="vector-item">
          <span class="vector-badge">Timezone & Locale</span>
          <h4>Deterministic Offset Spoofing</h4>
          <p>Synchronizes <code>Intl.DateTimeFormat</code> and <code>Date.prototype.getTimezoneOffset</code> with user-configured location preferences.</p>
        </div>
      </div>
    </div>

    <!-- TAB 3: RUNTIME & SPECS -->
    <div v-if="activeTab === 'specs'" class="studio-detail-card">
      <div class="detail-card-head">
        <h3>Architecture & Performance Metrics</h3>
        <p>Packora is engineered in Kotlin using Jetpack Compose, native JNI bytecode modification, and Android Signature Scheme V1/V2/V3.</p>
      </div>
      <div class="specs-grid">
        <div class="spec-card">
          <span class="spec-metric">&lt; 800ms</span>
          <span class="spec-title">Compilation Latency</span>
          <p>Fast on-device AXML patching, resource rewriting, and signing with zero network overhead.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">51.0 KB</span>
          <span class="spec-title">Template Overhead</span>
          <p>Ultra-lean native container with zero bloat, no analytics libraries, and zero telemetry.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">12</span>
          <span class="spec-title">Native On-Device Runtimes</span>
          <p>Node.js, PHP, Python, Go, WordPress, Frontend SPAs, Offline Crawlers compiled to APKs.</p>
        </div>
        <div class="spec-card">
          <span class="spec-metric">RSA-3072</span>
          <span class="spec-title">Isolated Keystores</span>
          <p>Deterministic per-package signing identities preventing update parse failures.</p>
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
  background: rgba(14, 21, 38, 0.6);
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
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  border: 1px solid var(--packora-glass-border);
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
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
}

.traffic-light.red { background: #ef4444; }
.traffic-light.yellow { background: #f59e0b; }
.traffic-light.green { background: #10b981; }

.studio-title-badge {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--vp-c-text-1);
  letter-spacing: 0.2px;
}

.studio-status-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  padding: 4px 10px;
  border-radius: 9999px;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}

/* Presets */
.preset-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.preset-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--vp-c-text-3);
}

.preset-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 9999px;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--vp-c-text-2);
  cursor: pointer;
  transition: all 0.2s;
}

.preset-chip:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--vp-c-text-1);
}

.preset-chip.active {
  background: var(--vp-c-brand-1);
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-bg);
}

/* Metadata inputs */
.studio-meta-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr;
  gap: 14px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
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
  background: rgba(10, 15, 28, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 8px 12px;
  gap: 8px;
}

.input-with-icon input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--vp-c-text-1);
  font-size: 0.88rem;
  width: 100%;
  font-family: inherit;
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
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: all 0.2s;
}

.toggle-card:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.1);
}

.toggle-info {
  flex: 1;
  padding-right: 12px;
}

.toggle-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--vp-c-text-1);
  margin-bottom: 2px;
}

.toggle-info p {
  font-size: 0.74rem;
  color: var(--vp-c-text-3);
  margin: 0;
  line-height: 1.3;
}

/* Switch UI */
.switch-ui {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 24px;
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
  background-color: rgba(255, 255, 255, 0.15);
  transition: .3s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: 24px;
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .3s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.switch-ui input:checked + .switch-slider {
  background: var(--vp-c-brand-1);
}

.switch-ui input:checked + .switch-slider:before {
  transform: translateX(18px);
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
  padding: 12px 16px;
}

.progress-bar-track {
  width: 100%;
  height: 6px;
  background: var(--vp-c-bg-soft);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-bar-fill {
  height: 100%;
  background: var(--vp-c-brand-1);
  transition: width 0.3s ease;
  border-radius: 6px;
}

.progress-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
}

.progress-stage-text {
  color: #93c5fd;
  display: flex;
  align-items: center;
  gap: 6px;
}

.progress-pct {
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.action-buttons-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.build-trigger-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 14px 24px;
  background: var(--vp-c-brand-1);
  border: 1px solid var(--vp-c-brand-1);
  border-radius: 14px;
  color: var(--vp-c-bg);
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.build-trigger-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.build-trigger-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-notif-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  color: var(--vp-c-text-1);
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.test-notif-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--vp-c-brand-1);
}

/* Simulated Android Notification */
.simulated-notification {
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  border-radius: 16px;
  padding: 14px 16px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.15);
}

.notif-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.74rem;
  color: #94a3b8;
  margin-bottom: 8px;
}

.notif-body {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.notif-icon-box {
  font-size: 1.4rem;
  background: rgba(255, 255, 255, 0.06);
  padding: 8px;
  border-radius: 10px;
  line-height: 1;
}

.notif-text b {
  display: block;
  font-size: 0.88rem;
  color: #f8fafc;
  margin-bottom: 2px;
}

.notif-text p {
  font-size: 0.8rem;
  color: #cbd5e1;
  margin: 0;
  line-height: 1.4;
}

/* Tab 2 & 3 Details */
.detail-card-head {
  margin-bottom: 24px;
}

.detail-card-head h3 {
  font-size: 1.3rem;
  font-weight: 800;
  color: var(--vp-c-text-1);
  margin-bottom: 8px;
}

.detail-card-head p {
  color: var(--vp-c-text-2);
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.5;
}

.vectors-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .vectors-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .vectors-grid {
    grid-template-columns: 1fr;
  }
}

.vector-item {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 14px;
  padding: 16px;
  transition: all 0.2s;
}

.vector-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
}

.vector-badge {
  display: inline-block;
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
  background: var(--vp-c-bg-mute);
  border: 1px solid var(--packora-glass-border);
  padding: 3px 8px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.vector-item h4 {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--vp-c-text-1);
  margin-bottom: 6px;
}

.vector-item p {
  font-size: 0.8rem;
  color: var(--vp-c-text-3);
  margin: 0;
  line-height: 1.45;
}

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
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
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
  color: var(--vp-c-text-3);
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

/* Multi-screen Scalability: Tablet & Mobile phones */
@media (max-width: 768px) {
  .studio-main-card, .studio-detail-card {
    padding: 18px 14px;
    border-radius: 16px;
  }

  .studio-tabs {
    width: 100%;
    display: flex;
    overflow-x: auto;
    scrollbar-width: none;
    -webkit-overflow-scrolling: touch;
    padding: 4px;
    gap: 4px;
  }

  .studio-tabs::-webkit-scrollbar {
    display: none;
  }

  .studio-tab-btn {
    flex: 1;
    min-width: fit-content;
    white-space: nowrap;
    justify-content: center;
    font-size: 0.8rem;
    padding: 8px 12px;
  }

  .studio-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .studio-header-right {
    width: 100%;
  }

  .studio-status-pill {
    width: 100%;
    justify-content: center;
  }

  .action-buttons-row {
    flex-direction: column;
    gap: 10px;
  }

  .build-trigger-btn, .test-notif-btn {
    width: 100%;
    flex: 1 1 100%;
    padding: 12px 18px;
    font-size: 0.9rem;
  }

  .simulated-notification {
    max-width: 100%;
    box-sizing: border-box;
  }
}

@media (max-width: 480px) {
  .preset-chip {
    padding: 5px 10px;
    font-size: 0.78rem;
  }

  .studio-title-badge {
    font-size: 0.78rem;
  }

  .toggle-card {
    padding: 12px 10px;
  }

  .toggle-title {
    font-size: 0.82rem;
  }
}
</style>
