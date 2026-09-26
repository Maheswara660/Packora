package com.maheswara660.packora.builder

import android.content.Context
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date

/**
 * Per-package deterministic signing identities.
 *
 * Each package name gets its own isolated PKCS12 keystore under `filesDir/app_signing/`,
 * generated once and reused on every subsequent build of that package.
 * Keys are RSA-3072 with a self-signed certificate (`CN=<packageName>`), stored under a
 * randomly generated host-local credential file.
 */
object PerAppSigningIdentity {

    private const val TAG = "PerAppSigning"

    private const val STORE_DIR = "app_signing"
    private const val CREDENTIAL_FILE = ".app_signing_credential"
    private const val KEY_ALIAS = "packora_app"
    private const val KEY_SIZE = 3072
    private const val VALIDITY_YEARS = 30L
    private const val CN_MAX_LEN = 64

    data class Identity(
        val packageName: String,
        val privateKey: PrivateKey,
        val certificate: X509Certificate,
        val storeFile: File,
        val createdNow: Boolean
    ) {
        fun certSha256(): ByteArray =
            MessageDigest.getInstance("SHA-256").digest(certificate.encoded)

        fun certSha256Hex(): String = certSha256().joinToString("") { "%02x".format(it) }

        fun toSigningIdentity() = JarSigner.SigningIdentity(privateKey, certificate)
    }

    @Synchronized
    fun identityFor(context: Context, packageName: String): Identity {
        val storeFile = storeFileFor(context, packageName)
        val password = getOrCreateStorePassword(context)

        if (storeFile.exists()) {
            load(storeFile, password, packageName)?.let { return it }
            if (storeFile.exists()) {
                throw IllegalStateException("Per-app keystore exists but failed to load: ${storeFile.name}")
            }
        }

        return create(storeFile, password, packageName)
    }

    fun existingFingerprint(context: Context, packageName: String): String? {
        val storeFile = storeFileFor(context, packageName)
        if (!storeFile.exists()) return null
        val passwordFile = File(context.filesDir, CREDENTIAL_FILE)
        if (!passwordFile.exists()) return null
        return runCatching {
            load(storeFile, passwordFile.readText().trim().toCharArray(), packageName)
                ?.certificate?.encoded
                ?.let { MessageDigest.getInstance("SHA-256").digest(it) }
                ?.joinToString(":") { "%02X".format(it) }
        }.getOrNull()
    }

    fun delete(context: Context, packageName: String): Boolean =
        storeFileFor(context, packageName).delete()

    private fun storeFileFor(context: Context, packageName: String): File {
        val dir = File(context.filesDir, STORE_DIR).apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(packageName.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(dir, "sig_${digest.take(20)}.p12")
    }

    private fun getOrCreateStorePassword(context: Context): CharArray {
        val file = File(context.filesDir, CREDENTIAL_FILE)
        if (file.exists()) {
            val saved = file.readText().trim()
            if (saved.isNotEmpty()) return saved.toCharArray()
        }
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val password = CharArray(40) { chars[SecureRandom().nextInt(chars.length)] }
        file.writeText(String(password))
        return password
    }

    private fun load(storeFile: File, password: CharArray, packageName: String): Identity? {
        return try {
            val keyStore = KeyStore.getInstance("PKCS12")
            FileInputStream(storeFile).use { keyStore.load(it, password.copyOf()) }
            val key = keyStore.getKey(KEY_ALIAS, password.copyOf()) as? PrivateKey ?: return null
            val cert = keyStore.getCertificate(KEY_ALIAS) as? X509Certificate ?: return null
            if (cert.notAfter.before(Date())) {
                AppLogger.w(TAG, "Per-app certificate expired for $packageName — regenerating")
                storeFile.delete()
                return null
            }
            Identity(packageName, key, cert, storeFile, createdNow = false)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Per-app keystore load failed: ${e.message}")
            null
        }
    }

    private fun create(storeFile: File, password: CharArray, packageName: String): Identity {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(KEY_SIZE, SecureRandom())
        val keyPair = keyPairGenerator.generateKeyPair()

        val now = System.currentTimeMillis()
        val subject = X500Name("CN=${packageName.take(CN_MAX_LEN)}")
        val certHolder = JcaX509v3CertificateBuilder(
            subject,
            BigInteger.valueOf(now),
            Date(now),
            Date(now + VALIDITY_YEARS * 365L * 24L * 60L * 60L * 1000L),
            subject,
            keyPair.public
        ).apply {
            addExtension(Extension.basicConstraints, true, BasicConstraints(false))
            addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature))
        }.build(JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private))
        val cert = JcaX509CertificateConverter().getCertificate(certHolder)

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, password.copyOf())
        keyStore.setKeyEntry(KEY_ALIAS, keyPair.private, password.copyOf(), arrayOf(cert))
        FileOutputStream(storeFile).use { keyStore.store(it, password.copyOf()) }

        AppLogger.i(TAG, "Created per-app signing identity for $packageName (${storeFile.name})")
        return Identity(packageName, keyPair.private, cert, storeFile, createdNow = true)
    }
}
