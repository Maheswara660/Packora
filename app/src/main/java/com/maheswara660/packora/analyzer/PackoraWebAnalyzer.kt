package com.maheswara660.packora.analyzer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.maheswara660.packora.builder.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * High-speed PWA & Website Metadata Analyzer for Packora.
 * Inspects any website URL to extract:
 * - Application title from PWA manifest, <title>, and OpenGraph tags
 * - Theme colors (<meta name="theme-color">, manifest theme_color)
 * - High-resolution application icons (manifest icons, Apple touch icons, favicons)
 * - PWA capabilities and manifest metadata
 */
data class WebSiteIcon(
    val url: String,
    val size: Int = 0,
    val type: String = "image/png",
    val source: String = "HTML"
)

data class WebSiteAnalysis(
    val title: String? = null,
    val shortName: String? = null,
    val description: String? = null,
    val themeColorHex: String? = null,
    val backgroundColorHex: String? = null,
    val icons: List<WebSiteIcon> = emptyList(),
    val isPwa: Boolean = false,
    val startUrl: String? = null,
    val finalUrl: String? = null
)

object PackoraWebAnalyzer {

    private const val TAG = "PackoraWebAnalyzer"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val USER_AGENT = "Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36"

    suspend fun analyze(rawUrl: String): WebSiteAnalysis = withContext(Dispatchers.IO) {
        val targetUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else rawUrl

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val finalUrl = response.request.url.toString()
            val html = response.body?.string() ?: ""

            val baseUrl = finalUrl
            val uri = runCatching { Uri.parse(baseUrl) }.getOrNull()
            val scheme = uri?.scheme ?: "https"
            val host = uri?.host ?: ""

            // 1. Check for Manifest
            val manifestUrl = extractManifestUrl(html, baseUrl)
            var pwaManifest: JSONObject? = null
            if (manifestUrl != null) {
                pwaManifest = fetchJson(manifestUrl)
            } else {
                // Try standard fallbacks
                val fallback1 = "$scheme://$host/manifest.json"
                pwaManifest = fetchJson(fallback1)
                if (pwaManifest == null) {
                    val fallback2 = "$scheme://$host/manifest.webmanifest"
                    pwaManifest = fetchJson(fallback2)
                }
            }

            val icons = mutableListOf<WebSiteIcon>()

            // 2. Parse Manifest attributes if available
            var pwaName: String? = null
            var pwaShortName: String? = null
            var pwaThemeColor: String? = null
            var pwaBgColor: String? = null
            var pwaDesc: String? = null
            var pwaStartUrl: String? = null
            var isPwa = false

            if (pwaManifest != null) {
                isPwa = true
                pwaName = pwaManifest.optString("name").takeIf { it.isNotBlank() }
                pwaShortName = pwaManifest.optString("short_name").takeIf { it.isNotBlank() }
                pwaThemeColor = pwaManifest.optString("theme_color").takeIf { it.isNotBlank() }
                pwaBgColor = pwaManifest.optString("background_color").takeIf { it.isNotBlank() }
                pwaDesc = pwaManifest.optString("description").takeIf { it.isNotBlank() }
                pwaStartUrl = pwaManifest.optString("start_url").takeIf { it.isNotBlank() }

                val manifestIcons = pwaManifest.optJSONArray("icons")
                if (manifestIcons != null) {
                    for (i in 0 until manifestIcons.length()) {
                        val iconObj = manifestIcons.optJSONObject(i) ?: continue
                        val src = iconObj.optString("src").trim()
                        if (src.isBlank()) continue
                        val fullIconUrl = resolveUrl(src, baseUrl)
                        val sizes = iconObj.optString("sizes", "0x0")
                        val sizeInt = parseLargestSize(sizes)
                        val type = iconObj.optString("type", "image/png")
                        icons.add(WebSiteIcon(url = fullIconUrl, size = sizeInt, type = type, source = "PWA Manifest"))
                    }
                }
            }

            // 3. Parse HTML Meta Tags & Favicons
            val htmlTitle = extractTagContent(html, "<title>(.*?)</title>")
            val ogTitle = extractMetaProperty(html, "og:title")
            val ogImage = extractMetaProperty(html, "og:image")
            val ogDesc = extractMetaProperty(html, "og:description")
            val metaDesc = extractMetaName(html, "description")
            val metaThemeColor = extractMetaName(html, "theme-color")

            // Parse HTML icon links
            val iconLinkPattern = Pattern.compile(
                """<link[^>]+rel=["'](?:shortcut\s+)?(?:icon|apple-touch-icon)["'][^>]*>""",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = iconLinkPattern.matcher(html)
            while (matcher.find()) {
                val tag = matcher.group()
                val href = extractAttribute(tag, "href")
                if (!href.isNullOrBlank()) {
                    val fullUrl = resolveUrl(href, baseUrl)
                    val sizes = extractAttribute(tag, "sizes") ?: "0x0"
                    val sizeInt = parseLargestSize(sizes)
                    val isApple = tag.contains("apple-touch-icon", ignoreCase = true)
                    icons.add(
                        WebSiteIcon(
                            url = fullUrl,
                            size = if (isApple && sizeInt == 0) 180 else sizeInt,
                            type = "image/png",
                            source = if (isApple) "Apple Touch Icon" else "HTML Link Tag"
                        )
                    )
                }
            }

            // Add standard root favicons if no high-res icons found
            if (icons.isEmpty() && host.isNotBlank()) {
                icons.add(WebSiteIcon(url = "$scheme://$host/favicon.ico", size = 32, source = "Root Favicon"))
                icons.add(WebSiteIcon(url = "$scheme://$host/apple-touch-icon.png", size = 180, source = "Root Apple Touch Icon"))
            }

            // Also add og:image if square or applicable
            if (!ogImage.isNullOrBlank()) {
                icons.add(WebSiteIcon(url = resolveUrl(ogImage, baseUrl), size = 256, source = "OpenGraph Image"))
            }

            // Sort icons descending by size
            val sortedIcons = icons.distinctBy { it.url }.sortedByDescending { it.size }

            val resolvedTitle = pwaName ?: pwaShortName ?: ogTitle ?: htmlTitle?.let { cleanHtmlTitle(it) }

            WebSiteAnalysis(
                title = resolvedTitle?.trim(),
                shortName = pwaShortName ?: pwaName ?: resolvedTitle,
                description = pwaDesc ?: ogDesc ?: metaDesc,
                themeColorHex = pwaThemeColor ?: metaThemeColor,
                backgroundColorHex = pwaBgColor,
                icons = sortedIcons,
                isPwa = isPwa,
                startUrl = pwaStartUrl?.let { resolveUrl(it, baseUrl) },
                finalUrl = finalUrl
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to analyze website $rawUrl", e)
            WebSiteAnalysis(finalUrl = targetUrl)
        }
    }

    suspend fun downloadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val bytes = response.body?.bytes() ?: return@withContext null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractManifestUrl(html: String, baseUrl: String): String? {
        val pattern = Pattern.compile("""<link[^>]+rel=["']manifest["'][^>]*>""", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            val tag = matcher.group()
            val href = extractAttribute(tag, "href")
            if (!href.isNullOrBlank()) {
                return resolveUrl(href, baseUrl)
            }
        }
        return null
    }

    private fun fetchJson(url: String): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/manifest+json,application/json,*/*")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string()?.trim() ?: return null
            if (body.startsWith("{")) JSONObject(body) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveUrl(relativeOrAbsolute: String, baseUrl: String): String {
        return try {
            val base = URI_create(baseUrl)
            base.resolve(relativeOrAbsolute).toString()
        } catch (e: Exception) {
            if (relativeOrAbsolute.startsWith("http://") || relativeOrAbsolute.startsWith("https://")) {
                relativeOrAbsolute
            } else if (relativeOrAbsolute.startsWith("//")) {
                "https:$relativeOrAbsolute"
            } else {
                val cleanBase = baseUrl.trimEnd('/')
                val cleanRel = relativeOrAbsolute.trimStart('/')
                "$cleanBase/$cleanRel"
            }
        }
    }

    private fun URI_create(str: String): java.net.URI {
        return java.net.URI.create(str)
    }

    private fun parseLargestSize(sizesStr: String): Int {
        var largest = 0
        val parts = sizesStr.split(" ")
        for (part in parts) {
            val dims = part.lowercase().split("x")
            if (dims.size == 2) {
                val w = dims[0].toIntOrNull() ?: 0
                val h = dims[1].toIntOrNull() ?: 0
                val minDim = minOf(w, h)
                if (minDim > largest) largest = minDim
            }
        }
        return largest
    }

    private fun extractAttribute(tag: String, attr: String): String? {
        val pattern = Pattern.compile("""$attr=["']([^"']+)["']""", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(tag)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractMetaProperty(html: String, property: String): String? {
        val pattern = Pattern.compile("""<meta[^>]+property=["']$property["'][^>]+content=["']([^"']+)["'][^>]*>""", Pattern.CASE_INSENSITIVE)
        var matcher = pattern.matcher(html)
        if (matcher.find()) return matcher.group(1)
        val altPattern = Pattern.compile("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']$property["'][^>]*>""", Pattern.CASE_INSENSITIVE)
        matcher = altPattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractMetaName(html: String, name: String): String? {
        val pattern = Pattern.compile("""<meta[^>]+name=["']$name["'][^>]+content=["']([^"']+)["'][^>]*>""", Pattern.CASE_INSENSITIVE)
        var matcher = pattern.matcher(html)
        if (matcher.find()) return matcher.group(1)
        val altPattern = Pattern.compile("""<meta[^>]+content=["']([^"']+)["'][^>]+name=["']$name["'][^>]*>""", Pattern.CASE_INSENSITIVE)
        matcher = altPattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractTagContent(html: String, regex: String): String? {
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun cleanHtmlTitle(rawTitle: String): String {
        return rawTitle.replace("\n", " ").replace("\r", " ")
            .split("|", "-", "—", "•")
            .firstOrNull()?.trim() ?: rawTitle
    }
}
