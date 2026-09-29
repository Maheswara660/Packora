/**
 * Packora v5.5.0 — Interactive Web Studio Engine
 * Pure Vanilla JavaScript — Zero External Dependencies
 */

(function () {
  'use strict';

  // State
  const state = {
    appName: 'Linear',
    targetUrl: 'https://linear.app',
    packageName: 'app.linear.packora',
    architecture: 'web',
    privacyShield: true,
    adBlock: true,
    instantFooter: true,
    encryptedDns: true,
    webNotifications: true,
    deterministicKeys: true,
    isCompiling: false,
    compileProgress: 0,
    compileStep: ''
  };

  // Presets Data
  const presets = {
    linear: {
      name: 'Linear',
      url: 'https://linear.app',
      pkg: 'app.linear.packora',
      arch: 'web',
      icon: '⚡'
    },
    github: {
      name: 'GitHub',
      url: 'https://github.com',
      pkg: 'com.github.packora',
      arch: 'web',
      icon: '🐙'
    },
    notion: {
      name: 'Notion',
      url: 'https://notion.so',
      pkg: 'so.notion.packora',
      arch: 'frontend',
      icon: '📝'
    },
    excalidraw: {
      name: 'Excalidraw',
      url: 'https://excalidraw.com',
      pkg: 'com.excalidraw.packora',
      arch: 'html',
      icon: '🎨'
    }
  };

  // Compilation Pipeline Steps
  const compilationSteps = [
    { p: 15, msg: 'Inspecting target endpoint & harvesting manifest icons...' },
    { p: 30, msg: 'Cloning base template shell (Android 15 API 35 ready)...' },
    { p: 45, msg: 'In-house binary AXML patching: rewriting package identity & permissions...' },
    { p: 60, msg: 'In-house binary ARSC rebuilding: updating string pools & app name...' },
    { p: 75, msg: 'Aligning native binaries (*.so) to 16KB ELF page boundaries...' },
    { p: 85, msg: 'Computing isolated deterministic RSA-3072 keystore...' },
    { p: 95, msg: 'Generating APK Signature Scheme v2 & v3 blocks via apksig...' },
    { p: 100, msg: 'WebAPK compilation complete! 3.4 MB standalone APK ready for install.' }
  ];

  // DOM Elements
  const urlInput = document.getElementById('target-url');
  const nameInput = document.getElementById('app-name');
  const pkgInput = document.getElementById('package-name');
  const compileBtn = document.getElementById('btn-compile');
  const notifBtn = document.getElementById('btn-test-notif');
  const progressBar = document.getElementById('progress-fill');
  const progressText = document.getElementById('progress-text');
  const progressPct = document.getElementById('progress-pct');
  const notifBanner = document.getElementById('sim-notification');
  const themeToggleBtn = document.getElementById('btn-theme-toggle');

  // Architecture Cards
  const archCards = document.querySelectorAll('.sim-arch-card');
  archCards.forEach(card => {
    card.addEventListener('click', () => {
      archCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      state.architecture = card.getAttribute('data-arch');
    });
  });

  // Preset Buttons
  const presetBtns = document.querySelectorAll('.sim-preset-btn');
  presetBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const presetKey = btn.getAttribute('data-preset');
      const p = presets[presetKey];
      if (!p) return;

      presetBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      state.appName = p.name;
      state.targetUrl = p.url;
      state.packageName = p.pkg;
      state.architecture = p.arch;

      if (urlInput) urlInput.value = p.url;
      if (nameInput) nameInput.value = p.name;
      if (pkgInput) pkgInput.value = p.pkg;

      // Update active arch card
      archCards.forEach(c => {
        if (c.getAttribute('data-arch') === p.arch) c.classList.add('active');
        else c.classList.remove('active');
      });
    });
  });

  // Input Listeners
  if (urlInput) {
    urlInput.addEventListener('input', (e) => {
      state.targetUrl = e.target.value;
      // Auto-suggest package name
      try {
        const urlObj = new URL(e.target.value.startsWith('http') ? e.target.value : 'https://' + e.target.value);
        const hostParts = urlObj.hostname.replace('www.', '').split('.');
        if (hostParts.length >= 2) {
          const suggestedPkg = `app.${hostParts[0]}.packora`;
          state.packageName = suggestedPkg;
          if (pkgInput) pkgInput.value = suggestedPkg;
        }
      } catch (err) {}
    });
  }

  if (nameInput) {
    nameInput.addEventListener('input', (e) => {
      state.appName = e.target.value;
    });
  }

  if (pkgInput) {
    pkgInput.addEventListener('input', (e) => {
      state.packageName = e.target.value;
    });
  }

  // Compile Trigger
  if (compileBtn) {
    compileBtn.addEventListener('click', () => {
      if (state.isCompiling) return;
      state.isCompiling = true;
      compileBtn.disabled = true;
      compileBtn.innerHTML = `<span>⏳</span> Compiling WebAPK...`;

      let currentStep = 0;
      const interval = setInterval(() => {
        if (currentStep < compilationSteps.length) {
          const step = compilationSteps[currentStep];
          if (progressBar) progressBar.style.width = step.p + '%';
          if (progressText) progressText.innerText = step.msg;
          if (progressPct) progressPct.innerText = step.p + '%';
          currentStep++;
        } else {
          clearInterval(interval);
          state.isCompiling = false;
          compileBtn.disabled = false;
          compileBtn.innerHTML = `<span>✅</span> Recompile WebAPK`;
        }
      }, 350);
    });
  }

  // Notification Test Banner
  if (notifBtn && notifBanner) {
    notifBtn.addEventListener('click', () => {
      notifBanner.style.display = 'block';
      const appNameSlot = document.getElementById('notif-app-name');
      if (appNameSlot) appNameSlot.innerText = state.appName + ' • Android System';
      setTimeout(() => {
        notifBanner.style.display = 'none';
      }, 4500);
    });
  }

  // Theme Toggle (Pure Monochrome Light/Dark)
  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
      const isLight = document.body.classList.contains('light-theme');
      themeToggleBtn.innerText = isLight ? '🌙' : '☀️';
      localStorage.setItem('packora-theme', isLight ? 'light' : 'dark');
    });

    // Restore preference
    const savedTheme = localStorage.getItem('packora-theme');
    if (savedTheme === 'light') {
      document.body.classList.add('light-theme');
      themeToggleBtn.innerText = '🌙';
    }
  }

  console.log('Packora v5.5.0 Standalone Web Studio initialized.');
})();
