package com.maheswara660.packora.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun testIsNewerVersion() {
        // Newer versions
        assertTrue(AppUpdateManager.isNewerVersion("3.2.2", "3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("3.2.1", "3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("3.2.0", "3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("3.1.1", "3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("3.0.0", "3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("3.0.0", "v3.2.3"))
        assertTrue(AppUpdateManager.isNewerVersion("v3.0.0", "v4.0.0"))
        assertTrue(AppUpdateManager.isNewerVersion("3.0", "3.0.1"))
        assertTrue(AppUpdateManager.isNewerVersion("2.9.9", "3.0.0"))

        // Same version
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "3.2.3"))
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "v3.2.3"))
        assertFalse(AppUpdateManager.isNewerVersion("v3.2.3", "3.2.3"))

        // Older versions
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "3.2.2"))
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "3.2.1"))
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "3.2.0"))
        assertFalse(AppUpdateManager.isNewerVersion("3.2.3", "3.1.1"))
        assertFalse(AppUpdateManager.isNewerVersion("3.0.1", "3.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("4.0.0", "v3.5.0"))
    }

    @Test
    fun testFindBestAssetAbiMatching() {
        val assets = listOf(
            ReleaseAsset(name = "Packora-v3.2.2-universal-release.apk", downloadUrl = "http://example.com/univ.apk", sizeBytes = 25000000),
            ReleaseAsset(name = "Packora-v3.2.2-arm64-v8a-release.apk", downloadUrl = "http://example.com/arm64.apk", sizeBytes = 15000000),
            ReleaseAsset(name = "Packora-v3.2.2-armeabi-v7a-release.apk", downloadUrl = "http://example.com/v7a.apk", sizeBytes = 13000000),
            ReleaseAsset(name = "Packora-v3.2.2-x86_64-release.apk", downloadUrl = "http://example.com/x64.apk", sizeBytes = 16000000),
            ReleaseAsset(name = "Packora-v3.2.2-x86-release.apk", downloadUrl = "http://example.com/x86.apk", sizeBytes = 14000000)
        )

        // On 64-bit ARM device
        val bestArm64 = AppUpdateManager.findBestAsset(assets, preferredAbis = listOf("arm64-v8a", "armeabi-v7a"))
        assertEquals("Packora-v3.2.2-arm64-v8a-release.apk", bestArm64?.name)

        // On 32-bit ARM device
        val bestArmV7 = AppUpdateManager.findBestAsset(assets, preferredAbis = listOf("armeabi-v7a"))
        assertEquals("Packora-v3.2.2-armeabi-v7a-release.apk", bestArmV7?.name)

        // On x86_64 device
        val bestX86_64 = AppUpdateManager.findBestAsset(assets, preferredAbis = listOf("x86_64", "x86"))
        assertEquals("Packora-v3.2.2-x86_64-release.apk", bestX86_64?.name)

        // On x86 device
        val bestX86 = AppUpdateManager.findBestAsset(assets, preferredAbis = listOf("x86"))
        assertEquals("Packora-v3.2.2-x86-release.apk", bestX86?.name)
    }

    @Test
    fun testFindBestAssetFallback() {
        val assets = listOf(
            ReleaseAsset(name = "Packora-v3.2.2-universal-release.apk", downloadUrl = "http://example.com/univ.apk", sizeBytes = 20000000),
            ReleaseAsset(name = "release-notes.txt", downloadUrl = "http://example.com/notes.txt", sizeBytes = 1000)
        )
        val best = AppUpdateManager.findBestAsset(assets, preferredAbis = emptyList())
        assertNotNull(best)
        assertEquals("Packora-v3.2.2-universal-release.apk", best?.name)
    }
}
