# Built-in Ad & Tracker Blocker

Packora integrates a high-speed, zero-dependency content blocker that operates locally inside the WebAPK process. It eliminates banner ads, tracking telemetry, analytics beacons, and cosmetic whitespace without draining device battery or requiring third-party VPN apps.

---

## Dual-Layer Filtering Engine

### Layer 1: Request Interception
- **Mechanism**: Intercepts outgoing HTTP and HTTPS requests within `shouldInterceptRequest` before they hit the network stack.
- **Rule Engine**: Checks requested domains against bundled, high-efficiency rule engines (derived from EasyList, AdGuard, and tracking telemetry databases).
- **Blocked Resource Types**: Third-party ad scripts, tracking pixels, telemetry beacons, and behavioral profiling SDKs.
- **Response**: Instantly returns a lightweight empty `WebResourceResponse`, preventing unwanted network bandwidth consumption.

### Layer 2: Cosmetic Container Collapsing
- **Mechanism**: Dynamic, debounced DOM `MutationObserver` detects orphaned ad slot containers (e.g. empty `<div>` elements, Google AdSense wrappers, sponsored banners).
- **Style Injection**: Automatically applies `display: none !important` and collapses element height to `0px`.
- **Zero Reflow Penalty**: Observers are throttled (300ms–400ms) and automatically disconnect 10–12 seconds after initial page load to prevent main-thread JavaScript starvation.

---

## Zero Telemetry & Privacy
All filter list parsing and request decisions happen entirely on-device in application RAM. Packora never transmits your browsing queries, visited URLs, or blocked statistics to external cloud servers.
