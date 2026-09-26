package com.maheswara660.packora.crypto

import android.content.Context
import android.os.Debug
import com.maheswara660.packora.builder.AppLogger
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM resource encryption engine and runtime tamper defense shield.
 */
object PackoraCryptoShield {

    private const val TAG = "PackoraCryptoShield"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    /**
     * Derives a 256-bit AES key from password and salt using PBKDF2WithHmacSHA256.
     */
    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM with a random salt and IV.
     * Output format: [16-byte salt][12-byte IV][ciphertext + 16-byte GCM tag]
     */
    fun encrypt(data: ByteArray, password: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { random.nextBytes(it) }

        val key = deriveKey(password.toCharArray(), salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val encrypted = cipher.doFinal(data)
        return salt + iv + encrypted
    }

    /**
     * Decrypts AES-256-GCM encrypted bytes.
     */
    fun decrypt(encryptedPayload: ByteArray, password: String): ByteArray {
        require(encryptedPayload.size > SALT_LENGTH + IV_LENGTH) { "Payload too small to contain valid encrypted data" }

        val salt = encryptedPayload.copyOfRange(0, SALT_LENGTH)
        val iv = encryptedPayload.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
        val ciphertext = encryptedPayload.copyOfRange(SALT_LENGTH + IV_LENGTH, encryptedPayload.size)

        val key = deriveKey(password.toCharArray(), salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return cipher.doFinal(ciphertext)
    }

    /**
     * Encrypts a stream using AES-256-GCM.
     */
    fun encryptStream(input: InputStream, output: OutputStream, password: String) {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { random.nextBytes(it) }

        output.write(salt)
        output.write(iv)

        val key = deriveKey(password.toCharArray(), salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        CipherOutputStream(output, cipher).use { cipherOut ->
            input.copyTo(cipherOut)
        }
    }

    /**
     * Runtime Tamper and Debugger Detector.
     * Returns true if debugging tools, Frida, or ptrace hooks are active.
     */
    fun isDebuggerOrTamperDetected(context: Context): Boolean {
        // 1. Android Debug Flag check
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            AppLogger.w(TAG, "Debugger connection detected")
            return true
        }

        // 2. Linux TracerPid Inspection (/proc/self/status)
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                val tracerLine = statusFile.readLines().firstOrNull { it.startsWith("TracerPid:") }
                val pid = tracerLine?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
                if (pid > 0) {
                    AppLogger.w(TAG, "TracerPid active: $pid")
                    return true
                }
            }
        } catch (ignored: Exception) {}

        // 3. Frida default port probe
        try {
            val fridaPorts = listOf(27042, 27043)
            for (port in fridaPorts) {
                java.net.Socket().use { s ->
                    s.connect(java.net.InetSocketAddress("127.0.0.1", port), 40)
                    AppLogger.w(TAG, "Frida server port open: $port")
                    return true
                }
            }
        } catch (ignored: Exception) {}

        return false
    }
}
