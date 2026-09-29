# Privacy & Hardening

A thematic index of the privacy, disguise, and hardening capabilities. Each is configured by a card in [Edit Common Config](/guide/app-actions/edit-common-config/) — follow the links for full detail.

## Access gating

- [Activation Code Verification](/guide/app-actions/edit-common-config/activation) — local or remote activation codes.

## Ad blocking

- [Ad Blocking](/guide/app-actions/edit-common-config/ad-blocking) — hosts rules and subscriptions (manage lists in [Hosts Ad Blocking](/guide/more-features/hosts-adblock)).

## Disguise & Fingerprint Shield

- [Device Disguise](/guide/app-actions/edit-common-config/device-disguise) — device-level property spoofing.
- **Browser Fingerprint Disguise (50+ vectors)**:
  - **Canvas 2D**: Sub-perceptual cryptographic noise injection on `toDataURL` and `getImageData`.
  - **WebGL GPU**: Normalizes `UNMASKED_RENDERER_WEBGL` and vendor strings to generic mobile GPU profiles.
  - **AudioContext**: Adds fractional phase jitter to audio frequency analysis.
  - **DOM ClientRects**: Sub-pixel dimension perturbation (±0.0001px) scrambling font telemetry.
  - **WebRTC Protection**: Suppresses local IP leakage via candidate blocking.

## System & Network Integration

- **HTML5 Web Notifications Bridge**: Bridges `window.Notification` and PWA Service Worker push alerts directly to native Android system bar notifications with permission negotiation.
- **Instant 0ms Footer Suppressor**: High-priority early CSS stylesheet injection prevents mobile download/install footers from flickering before initial render.
- **Deterministic RSA-3072 Isolated Keystores**: Unique cryptographic keypair per package name preventing update parse failures and signature conflicts.

## Encryption & hardening

- **Resource encryption** and **runtime hardening** (anti-debug, anti-Frida, DEX-tamper) — chosen at build time; see [APK Export Config](/guide/app-actions/edit-common-config/apk-export) and [Build APK](/guide/app-actions/build-apk).
- **Anti-capture** — under [Special Settings](/guide/app-actions/edit-common-config/special-settings).
