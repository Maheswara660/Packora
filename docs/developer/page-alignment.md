# Android 15+ 16KB Page Alignment

Starting with Android 15 (API 35), Android devices can be configured with **16KB memory page sizes** instead of the traditional 4KB standard to boost memory throughput and application launch performance.

---

## The 16KB Alignment Requirement

Android's dynamic linker requires all Executable and Linkable Format (ELF) shared libraries (`.so` files) within an APK to be aligned to 16,384-byte boundaries. If an APK contains unaligned native binaries, Android 15+ kernels reject loading them, causing immediate application crashes (`dlopen failed: unaligned ELF`).

---

## In-House `ElfAligner16k` Engine

Packora includes `ElfAligner16k.kt`, a native byte-level ZIP archive re-aligner:
1. **Central Directory Traversal**: Scans the central directory records of the compiled APK archive.
2. **ELF Header Verification**: Detects all `.so` shared libraries in `lib/arm64-v8a/`, `lib/x86_64/`, etc.
3. **Offset Calculation**: Calculates the padding required so that the raw file offset of each ELF binary is an exact multiple of 16,384 (`offset % 16384 == 0`).
4. **ZIP Extra Field Padding**: Injects null byte padding into the local file header's extra field (`0x0000`), perfectly shifting the binary payload to the 16KB boundary.
5. **Zero Re-compression Overhead**: Aligns binaries without uncompressing and recompressing stored data, preserving CRC32 checksums.
