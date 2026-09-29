# Deterministic RSA-3072 Keystores & Signing

Android enforces strict package signing security: an installed application cannot be updated in-place unless the update is signed with the **exact same cryptographic key** as the currently installed package (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

---

## Isolated Per-App Signing Identity

Packora implements `PerAppSigningIdentity`, a deterministic key derivation engine that guarantees lifetime update compatibility:

1. **Seed Derivation**: Derives a cryptographic seed using a PBKDF2 / SHA-256 HMAC of the package identifier (`packageName`) combined with a secure local device salt.
2. **RSA-3072 Keypair**: Uses Bouncy Castle to generate a 3,072-bit RSA keypair and X.509 certificate deterministically from the derived seed.
3. **Collision Resistance**: Every package name receives a unique, isolated cryptographic certificate. Two different WebAPKs will never share the same signing certificate.
4. **Lifelong Reproducibility**: Whenever an update for that package is compiled on the device, the exact same RSA-3072 key is derived, allowing seamless in-place updates.

---

## APK Signature Scheme v2 & v3 Support

Using the Android `apksig` signing library directly on-device:
- Signs packages with both **APK Signature Scheme v2** (whole-file hash integrity) and **v3** (key rotation support).
- Verified against Android 7.0 through Android 15+ package managers and Google Play Protect without security warnings.
