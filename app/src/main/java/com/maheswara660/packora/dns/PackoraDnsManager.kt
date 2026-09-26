package com.maheswara660.packora.dns

import android.content.Context
import android.os.Build
import com.maheswara660.packora.builder.AppLogger
import com.maheswara660.packora.model.PackoraDnsProvider
import com.maheswara660.packora.model.PackoraNetworkConfig
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance DNS-over-HTTPS (DoH) resolver manager.
 * Bypasses local ISP censorship, DNS poisoning, and interception.
 */
class PackoraDnsManager(private val context: Context) {

    companion object {
        private const val TAG = "PackoraDnsManager"

        fun isDohSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    private val client = OkHttpClient.Builder().build()
    private val dnsCache = ConcurrentHashMap<String, List<InetAddress>>()

    @Volatile
    private var currentConfig: PackoraNetworkConfig? = null

    fun applyConfig(config: PackoraNetworkConfig) {
        currentConfig = config
        dnsCache.clear()

        if (config.dohProvider == PackoraDnsProvider.SYSTEM || config.effectiveDohUrl.isBlank()) {
            AppLogger.d(TAG, "DNS using System Default")
            return
        }

        AppLogger.d(TAG, "Applied DoH provider: ${config.dohProvider.displayName} (${config.effectiveDohUrl}), strict=${config.strictDoh}")
    }

    fun clear() {
        currentConfig = null
        dnsCache.clear()
        AppLogger.d(TAG, "DNS configuration cleared, restored to system default")
    }

    fun resolve(hostname: String): List<InetAddress> {
        val config = currentConfig ?: return Dns.SYSTEM.lookup(hostname)
        val dohUrl = config.effectiveDohUrl

        if (config.dohProvider == PackoraDnsProvider.SYSTEM || dohUrl.isBlank()) {
            return Dns.SYSTEM.lookup(hostname)
        }

        dnsCache[hostname]?.let { return it }

        AppLogger.d(TAG, "Resolving host via DoH: $hostname -> $dohUrl")

        val httpUrl = dohUrl.toHttpUrlOrNull() ?: throw UnknownHostException("Invalid DoH URL: $dohUrl")

        val dohDns = DnsOverHttps.Builder()
            .client(client)
            .url(httpUrl)
            .includeIPv6(true)
            .build()

        return try {
            val resolved = dohDns.lookup(hostname)
            if (resolved.isNotEmpty()) {
                dnsCache[hostname] = resolved
            }
            resolved
        } catch (e: Exception) {
            AppLogger.w(TAG, "DoH lookup failed for $hostname: ${e.message}")
            if (config.strictDoh) {
                throw UnknownHostException("Strict DoH resolution failed for $hostname: ${e.message}")
            }
            // Fallback to system DNS in non-strict mode
            Dns.SYSTEM.lookup(hostname)
        }
    }
}
