# Dynamic Launcher Icons & Themes

Packora features **12 dynamic launcher app icons** and **12 matching color themes** tuned specifically for AMOLED, dark, and light display environments.

---

## 12 Curated Icon & Accent Styles

1. **Original Classic Blue**: The signature Packora royal blue icon and accents.
2. **Cyber Lime**: High-visibility electric lime for cyberpunk aesthetics.
3. **Ruby Blaze**: Deep crimson red with rich ruby highlights.
4. **Ocean Teal**: Serene aquamarine teal inspired by tropical oceans.
5. **Frost White**: Minimalist icy silver-white with high contrast.
6. **Neon Indigo**: Deep purple-indigo with luminescent accents.
7. **Deep Sapphire**: Intense sapphire blue for sleek modern displays.
8. **Electric Azure**: Vibrant sky azure tuned for maximum daylight visibility.
9. **Emerald Green**: Rich emerald jewel tones.
10. **Royal Violet**: Majestic royal purple with violet accents.
11. **Amber Sunset**: Warm sunset amber with golden radiance.
12. **Stealth Onyx**: Pure black and stealth charcoal for battery efficiency on OLED displays.

---

## How Dynamic Icons Work

Packora implements dynamic launcher icon switching via Android `activity-alias` components declared in `AndroidManifest.xml`. When a user selects a new icon in Settings:
- Packora toggles the corresponding `activity-alias` via Android's `PackageManager.setComponentEnabledSetting()`.
- The Android launcher immediately updates the app drawer icon and name without requiring a device reboot or application reinstallation.
