# Media Streamer Architecture

The **Media Streamer** architecture (`PackoraAppType.MEDIA`) is engineered specifically for audio and video streaming platforms, podcasts, internet radio stations, and media viewers.

---

## Media-Centric Optimizations

- **Auto-Keep-Screen-On**: Injects `FLAG_KEEP_SCREEN_ON` during active video playback so users can watch long-form video content without the display dimming or locking.
- **Unconstrained Autoplay**: Configures `mediaPlaybackRequiresUserGesture = false`, allowing media streams and playlists to advance automatically without manual touch gestures.
- **Continuous Background Audio**: In `template/MainActivity.kt`, `onPause()` is specialized for media apps, ensuring that background audio streams continue playing uninterrupted when the device screen is locked or when switching between apps.
- **Hardware Acceleration**: Enforces GPU-accelerated video decoding for smooth 60fps and 4K video playback.

---

## Best For

- Internet radio stations and podcast players.
- Music streaming web apps and personal cloud audio libraries (Subsonic, Jellyfin, Plex).
- Video-on-demand platforms and tutorial video libraries.
- Ambient sound, meditation, and white-noise web players.
