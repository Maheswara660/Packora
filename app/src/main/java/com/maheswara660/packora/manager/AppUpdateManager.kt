package com.maheswara660.packora.manager

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.maheswara660.packora.installApkFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class AppReleaseInfo(
    val tagName: String,
    val versionName: String,
    val releaseTitle: String,
    val changelog: String,
    val htmlUrl: String,
    val publishedAt: String,
    val assets: List<ReleaseAsset>,
    val bestAsset: ReleaseAsset?
)

sealed class UpdateCheckResult {
    data class NoUpdateAvailable(val currentVersion: String) : UpdateCheckResult()
    data class UpdateAvailable(val releaseInfo: AppReleaseInfo) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

object AppUpdateManager {

    private const val GITHUB_OWNER = "maheswara660"
    private const val GITHUB_REPO = "Packora"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    const val GITHUB_RELEASES_PAGE = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"

    /**
     * Retrieve the currently installed version name of Packora.
     */
    fun getCurrentVersion(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "3.1.1"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "3.1.1"
            }
        } catch (e: Exception) {
            "3.1.1"
        }
    }

    /**
     * Check if [latestVer] is strictly newer than [currentVer] using semantic versioning.
     */
    fun isNewerVersion(currentVer: String, latestVer: String): Boolean {
        val curClean = currentVer.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        val latClean = latestVer.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        if (curClean.equals(latClean, ignoreCase = true)) return false

        val curParts = curClean.split(".").mapNotNull { it.toIntOrNull() }
        val latParts = latClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(curParts.size, latParts.size)
        for (i in 0 until maxLen) {
            val curVal = curParts.getOrElse(i) { 0 }
            val latVal = latParts.getOrElse(i) { 0 }
            if (latVal > curVal) return true
            if (latVal < curVal) return false
        }
        return false
    }

    /**
     * Finds the best compatible APK asset for this device's architecture.
     * Searches device ABIs in preference order (e.g. arm64-v8a, armeabi-v7a, x86_64, x86),
     * falling back to universal or first available APK.
     */
    fun findBestAsset(
        assets: List<ReleaseAsset>,
        preferredAbis: List<String>? = null
    ): ReleaseAsset? {
        val apkAssets = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apkAssets.isEmpty()) return null

        val supportedAbis = preferredAbis ?: run {
            try {
                Build.SUPPORTED_ABIS?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        // 1. Try matching device ABIs in order of preference
        for (abi in supportedAbis) {
            val matching = when {
                abi.equals("arm64-v8a", ignoreCase = true) -> {
                    apkAssets.firstOrNull {
                        it.name.contains("arm64-v8a", ignoreCase = true) ||
                                it.name.contains("arm64", ignoreCase = true) ||
                                it.name.contains("v8a", ignoreCase = true)
                    }
                }
                abi.equals("armeabi-v7a", ignoreCase = true) -> {
                    apkAssets.firstOrNull {
                        (it.name.contains("armeabi-v7a", ignoreCase = true) ||
                                it.name.contains("armv7", ignoreCase = true) ||
                                it.name.contains("v7a", ignoreCase = true)) &&
                                !it.name.contains("arm64", ignoreCase = true) &&
                                !it.name.contains("v8a", ignoreCase = true)
                    }
                }
                abi.equals("x86_64", ignoreCase = true) -> {
                    apkAssets.firstOrNull {
                        it.name.contains("x86_64", ignoreCase = true) ||
                                it.name.contains("x64", ignoreCase = true)
                    }
                }
                abi.equals("x86", ignoreCase = true) -> {
                    apkAssets.firstOrNull {
                        it.name.contains("x86", ignoreCase = true) &&
                                !it.name.contains("x86_64", ignoreCase = true) &&
                                !it.name.contains("x64", ignoreCase = true)
                    }
                }
                else -> {
                    apkAssets.firstOrNull { it.name.contains(abi, ignoreCase = true) }
                }
            }
            if (matching != null) return matching
        }

        // 2. Fallback to universal APK
        val universal = apkAssets.firstOrNull { it.name.contains("universal", ignoreCase = true) }
        if (universal != null) return universal

        // 3. Fallback to any APK asset
        return apkAssets.firstOrNull()
    }

    /**
     * Checks GitHub releases for Packora updates asynchronously.
     * Note: Strictly manual invocation - no background auto-checks.
     */
    suspend fun checkForUpdates(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val currentVer = getCurrentVersion(context)
            val url = URL(GITHUB_API_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Packora-Android/$currentVer")
                connectTimeout = 12000
                readTimeout = 15000
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext UpdateCheckResult.Error("GitHub API returned HTTP $responseCode")
            }

            val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val rootJson = JSONObject(jsonStr)
            val tagName = rootJson.optString("tag_name", "")
            val releaseTitle = rootJson.optString("name", tagName)
            val changelog = rootJson.optString("body", "No changelog provided.")
            val htmlUrl = rootJson.optString("html_url", GITHUB_RELEASES_PAGE)
            val publishedAt = rootJson.optString("published_at", "")

            val assetsArray = rootJson.optJSONArray("assets")
            val assetsList = mutableListOf<ReleaseAsset>()
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val assetObj = assetsArray.getJSONObject(i)
                    val assetName = assetObj.optString("name", "")
                    val downloadUrl = assetObj.optString("browser_download_url", "")
                    val size = assetObj.optLong("size", 0L)
                    if (assetName.isNotBlank() && downloadUrl.isNotBlank()) {
                        assetsList.add(ReleaseAsset(name = assetName, downloadUrl = downloadUrl, sizeBytes = size))
                    }
                }
            }

            val bestAsset = findBestAsset(assetsList)
            val cleanLatestVer = tagName.removePrefix("v").removePrefix("V")

            val releaseInfo = AppReleaseInfo(
                tagName = tagName,
                versionName = cleanLatestVer,
                releaseTitle = releaseTitle,
                changelog = changelog,
                htmlUrl = htmlUrl,
                publishedAt = publishedAt,
                assets = assetsList,
                bestAsset = bestAsset
            )

            if (isNewerVersion(currentVer, tagName)) {
                UpdateCheckResult.UpdateAvailable(releaseInfo)
            } else {
                UpdateCheckResult.NoUpdateAvailable(currentVer)
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: e.message ?: "Failed to connect to GitHub")
        }
    }

    /**
     * Resolve target directory for storing the downloaded update APK.
     * Prioritizes custom storage folder from preferences, then system Downloads/Packora,
     * ensuring APK persists even if user cancels the installation prompt.
     */
    fun resolveDownloadDestination(context: Context, filename: String): File {
        val prefsManager = PackoraPreferencesManager(context)
        val customFolder = if (prefsManager.useCustomStorageFolder && !prefsManager.customStorageFolder.isNullOrBlank()) {
            File(prefsManager.customStorageFolder!!)
        } else null

        val targetDir: File = when {
            customFolder != null && (customFolder.exists() || customFolder.mkdirs()) && customFolder.canWrite() -> {
                customFolder
            }
            else -> {
                val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val packoraDownloads = File(publicDownloads, "Packora")
                if (!packoraDownloads.exists()) {
                    packoraDownloads.mkdirs()
                }
                if (packoraDownloads.exists() && packoraDownloads.canWrite()) {
                    packoraDownloads
                } else {
                    val extDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                    val fallbackDir = File(extDir, "Updates")
                    if (!fallbackDir.exists()) fallbackDir.mkdirs()
                    fallbackDir
                }
            }
        }

        return File(targetDir, filename)
    }

    /**
     * Downloads the APK asset with progress reporting, following redirects.
     * Automatically triggers system APK installer upon download completion.
     */
    suspend fun downloadAndInstallUpdate(
        context: Context,
        asset: ReleaseAsset,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destinationFile = resolveDownloadDestination(context, asset.name)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            var currentUrl = asset.downloadUrl
            var connection: HttpURLConnection
            var redirectCount = 0

            // Handle HTTP redirects (GitHub Releases redirect to AWS S3 objects.githubusercontent.com)
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Packora-Android/${getCurrentVersion(context)}")
                    connectTimeout = 15000
                    readTimeout = 30000
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308
                ) {
                    val newUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (newUrl != null && redirectCount < 8) {
                        currentUrl = newUrl
                        redirectCount++
                        continue
                    }
                }
                break
            }

            val totalBytes = if (connection.contentLengthLong > 0) connection.contentLengthLong else asset.sizeBytes
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        onProgress(progress, downloadedBytes, totalBytes)
                    }
                    output.flush()
                }
            }
            connection.disconnect()

            // Auto-trigger Android package installer
            withContext(Dispatchers.Main) {
                installApkFile(context, destinationFile.absolutePath)
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
