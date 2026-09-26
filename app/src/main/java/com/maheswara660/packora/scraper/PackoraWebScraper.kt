package com.maheswara660.packora.scraper

import android.content.Context
import com.maheswara660.packora.builder.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * High-speed offline pack generator. Crawls any website and bundles HTML, CSS, JS,
 * and media assets into an on-device offline WebAPK.
 */
class PackoraWebScraper(private val context: Context) {

    companion object {
        private const val TAG = "PackoraWebScraper"

        private val RESOURCE_SRC_REGEX = Pattern.compile(
            """(?:src|href|poster|data-src)\s*=\s*["']([^"'#]+?)["']""",
            Pattern.CASE_INSENSITIVE
        )
        private val CSS_URL_REGEX = Pattern.compile(
            """url\(\s*["']?([^"')]+?)["']?\s*\)""",
            Pattern.CASE_INSENSITIVE
        )
    }

    data class ScrapeOptions(
        val targetUrl: String,
        val maxDepth: Int = 2,
        val maxFiles: Int = 300,
        val outputDirectory: File,
        val userAgent: String = "Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36"
    )

    data class Progress(
        val downloadedCount: Int,
        val totalDiscovered: Int,
        val currentUrl: String,
        val bytesDownloaded: Long
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val downloadedUrls = ConcurrentHashMap<String, File>()

    suspend fun scrape(
        options: ScrapeOptions,
        onProgress: (Progress) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            options.outputDirectory.mkdirs()
            downloadedUrls.clear()

            var discovered = 1
            var downloaded = 0
            var totalBytes = 0L

            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(options.targetUrl to 0)

            while (queue.isNotEmpty() && downloaded < options.maxFiles) {
                val (currentUrl, depth) = queue.removeFirst()
                if (downloadedUrls.containsKey(currentUrl)) continue

                onProgress(Progress(downloaded, discovered, currentUrl, totalBytes))

                val request = Request.Builder()
                    .url(currentUrl)
                    .header("User-Agent", options.userAgent)
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@use

                        val body = response.body ?: return@use
                        val bytes = body.bytes()
                        totalBytes += bytes.size

                        val relativePath = sanitizePath(currentUrl, options.targetUrl)
                        val targetFile = File(options.outputDirectory, relativePath)
                        targetFile.parentFile?.mkdirs()

                        val isHtml = response.header("Content-Type")?.contains("text/html") == true ||
                                currentUrl.endsWith(".html") || currentUrl.endsWith(".htm") || depth == 0

                        if (isHtml) {
                            var htmlContent = String(bytes, Charsets.UTF_8)

                            // Extract sub-resources
                            val matcher = RESOURCE_SRC_REGEX.matcher(htmlContent)
                            while (matcher.find()) {
                                val foundPath = matcher.group(1) ?: continue
                                val absoluteUrl = resolveUrl(currentUrl, foundPath)
                                if (absoluteUrl != null && !downloadedUrls.containsKey(absoluteUrl)) {
                                    discovered++
                                    if (depth + 1 <= options.maxDepth) {
                                        queue.add(absoluteUrl to (depth + 1))
                                    }
                                }
                            }

                            targetFile.writeText(htmlContent)
                        } else {
                            targetFile.writeBytes(bytes)
                        }

                        downloadedUrls[currentUrl] = targetFile
                        downloaded++
                    }
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to scrape $currentUrl: ${e.message}")
                }
            }

            AppLogger.i(TAG, "Scraping complete: $downloaded files downloaded to ${options.outputDirectory.absolutePath}")
            options.outputDirectory
        }
    }

    private fun resolveUrl(base: String, relative: String): String? {
        return try {
            val baseUri = URI(base)
            val resolved = baseUri.resolve(relative)
            resolved.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizePath(url: String, baseUrl: String): String {
        return try {
            val uri = URI(url)
            var path = uri.path
            if (path.isNullOrBlank() || path == "/") {
                "index.html"
            } else {
                path.removePrefix("/").replace("/", File.separator)
            }
        } catch (e: Exception) {
            "file_${url.hashCode()}.dat"
        }
    }
}
