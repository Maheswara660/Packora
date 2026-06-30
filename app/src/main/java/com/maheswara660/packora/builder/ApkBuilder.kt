package com.maheswara660.packora.builder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ApkBuilder(private val context: Context) {

    private val template = ApkTemplate(context)
    private val signer = JarSigner(context)
    private val axmlRebuilder = AxmlRebuilder()
    private val arscRebuilder = ArscRebuilder()

    private val tempDir = File(context.cacheDir, "apk_build_temp").apply { mkdirs() }

    fun cleanTempFiles() {
        try {
            tempDir.deleteRecursively()
            tempDir.mkdirs()
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to clean temp files", e)
        }
    }

    fun buildApk(
        appName: String,
        packageName: String,
        targetUrl: String,
        versionCode: Int,
        versionName: String,
        iconBitmap: Bitmap?,
        disableHeader: Boolean,
        outputPath: String,
        customExportUri: String? = null,
        keystorePassword: String?,
        keyAlias: String?,
        commonName: String?,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): String? {
        cleanTempFiles()
        onProgress(5, "Preparing builder context...")

        val templateApk = template.getTemplateApk()
        if (templateApk == null) {
            AppLogger.e("ApkBuilder", "Base template APK not found")
            return null
        }

        val sanitizedAppName = appName.replace(Regex("[^\\w\\s\\-]"), "").replace(" ", "_")
        val relativeFolder = "Packora/$sanitizedAppName"

        // Handle Custom Keystore Generation if password and alias are provided
        var isCustomSigningActive = false
        if (!keystorePassword.isNullOrBlank() && !keyAlias.isNullOrBlank()) {
            try {
                onProgress(10, "Generating custom signing keystore...")
                val tempKeystore = File(tempDir, "${sanitizedAppName}_keystore.p12")
                
                val cn = if (commonName.isNullOrBlank()) "Packora Publisher" else commonName
                val genSuccess = signer.generateAndLoadCustomKeystore(
                    outputFile = tempKeystore,
                    password = keystorePassword.toCharArray(),
                    alias = keyAlias,
                    commonName = cn
                )
                
                if (genSuccess) {
                    isCustomSigningActive = true
                    AppLogger.d("ApkBuilder", "Custom keystore generated successfully")
                    
                    // Copy generated keystore file to public Downloads/Packora/<AppName>/
                    val keystoreFileName = "${sanitizedAppName}_keystore.p12"
                    copyFileToPublicDownloads(context, tempKeystore, relativeFolder, keystoreFileName, "application/x-pkcs12")
                } else {
                    AppLogger.e("ApkBuilder", "Failed to generate custom keystore")
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Error generating custom signing keys", e)
            }
        }

        val finalIconBitmap = iconBitmap ?: getDefaultMascotIcon(context)
        val targetAbis = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        val unsignedApk = File(tempDir, "${packageName}_unsigned.apk")
        val alignedApk = File(tempDir, "${packageName}_aligned.apk")
        val signedApk = File(tempDir, "${packageName}_signed.apk")

        unsignedApk.delete()
        alignedApk.delete()
        signedApk.delete()

        var discoveredOldIconPaths = emptySet<String>()

        try {
            onProgress(20, "Compiling APK...")
            ZipFile(templateApk).use { zipIn ->
                ZipOutputStream(FileOutputStream(unsignedApk)).use { zipOut ->
                    val entries = zipIn.entries().toList()
                        .sortedWith(compareBy<ZipEntry> { it.name != "resources.arsc" })

                    entries.forEach { entry ->
                        // Skip adaptive icon XML definitions to fix the package installer flyout mismatch
                        if (entry.name == "res/mipmap-anydpi-v26/ic_launcher.xml" || entry.name == "res/mipmap-anydpi-v26/ic_launcher_round.xml") {
                            return@forEach
                        }
                        when {
                            entry.name.startsWith("META-INF/") &&
                            (entry.name.endsWith(".SF") || entry.name.endsWith(".RSA") ||
                             entry.name.endsWith(".DSA") || entry.name == "META-INF/MANIFEST.MF") -> {
                                // Skip signatures
                            }

                            entry.name == "AndroidManifest.xml" -> {
                                val originalData = zipIn.getInputStream(entry).readBytes()
                                val modifiedData = axmlRebuilder.expandAndModifyFull(
                                    axmlData = originalData,
                                    originalPackage = "com.webtoapp",
                                    newPackage = packageName,
                                    versionCode = versionCode,
                                    versionName = versionName,
                                    permissions = listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE")
                                )
                                ZipUtils.writeEntryDeflated(zipOut, entry.name, modifiedData)
                            }

                            entry.name == "resources.arsc" -> {
                                val originalData = zipIn.getInputStream(entry).readBytes()
                                val modifiedData = arscRebuilder.rebuildWithNewAppNameAndIcons(
                                    arscData = originalData,
                                    targetAppName = appName,
                                    replaceIcons = true
                                )
                                discoveredOldIconPaths = arscRebuilder.getLastDiscoveredIconPaths()
                                ZipUtils.writeEntryStored(zipOut, entry.name, modifiedData)
                            }

                            entry.name == ApkTemplate.CONFIG_PATH -> {
                                val configJson = JSONObject().apply {
                                    put("appName", appName)
                                    put("packageName", packageName)
                                    put("targetUrl", targetUrl)
                                    put("versionCode", versionCode)
                                    put("versionName", versionName)
                                    put("webViewConfig", JSONObject().apply {
                                        put("openExternalLinks", true)
                                    })
                                }
                                val configBytes = configJson.toString().toByteArray(Charsets.UTF_8)
                                ZipUtils.writeEntryDeflated(zipOut, entry.name, configBytes)
                            }

                            isIconEntry(entry.name) || discoveredOldIconPaths.contains(entry.name) -> {
                                val size = getIconSize(entry.name)
                                val iconBytes = template.scaleBitmapToPng(finalIconBitmap, size)
                                ZipUtils.writeEntryDeflated(zipOut, entry.name, iconBytes)
                            }

                            entry.name.startsWith("lib/") -> {
                                val parts = entry.name.split("/")
                                if (parts.size >= 3) {
                                    val abi = parts[1]
                                    if (targetAbis.contains(abi)) {
                                        val originalData = zipIn.getInputStream(entry).readBytes()
                                        
                                        // Perform 16KB page alignment on native executables
                                        val tempSoFile = File(tempDir, parts.last())
                                        tempSoFile.writeBytes(originalData)
                                        val alignedSoFile = try {
                                            val res = ElfAligner16k.ensureAligned(tempSoFile, File(tempDir, "elf16k"))
                                            res.outputFile
                                        } catch (e: Exception) {
                                            AppLogger.w("ApkBuilder", "16KB ELF alignment failed for ${entry.name}, using original", e)
                                            tempSoFile
                                        }
                                        val alignedBytes = alignedSoFile.readBytes()
                                        // Store uncompressed to enable page mapping
                                        ZipUtils.writeEntryStored(zipOut, entry.name, alignedBytes)
                                    }
                                }
                            }

                            else -> {
                                ZipUtils.copyEntryPreserveMethod(zipIn, zipOut, entry)
                            }
                        }
                    }

                    val hasConfig = entries.any { it.name == ApkTemplate.CONFIG_PATH }
                    if (!hasConfig) {
                        val configJson = JSONObject().apply {
                            put("appName", appName)
                            put("packageName", packageName)
                            put("targetUrl", targetUrl)
                            put("versionCode", versionCode)
                            put("versionName", versionName)
                            put("webViewConfig", JSONObject().apply {
                                put("openExternalLinks", true)
                            })
                        }
                        val configBytes = configJson.toString().toByteArray(Charsets.UTF_8)
                        ZipUtils.writeEntryDeflated(zipOut, ApkTemplate.CONFIG_PATH, configBytes)
                    }
                }
            }

            onProgress(60, "Running 16KB Zip alignment...")
            if (!ZipAligner.align(unsignedApk, alignedApk)) {
                AppLogger.e("ApkBuilder", "Zip alignment failed")
                return null
            }

            onProgress(80, "Signing APK...")
            if (!signer.sign(alignedApk, signedApk)) {
                AppLogger.e("ApkBuilder", "APK signing failed")
                return null
            }

            onProgress(90, "Exporting generated APK to Downloads folder...")
            // Copy final signed APK to public Downloads/Packora/<AppName>/
            val finalPath = copyFileToPublicDownloads(context, signedApk, relativeFolder, outputPath, "application/vnd.android.package-archive", customExportUri)
            if (finalPath != null) {
                onProgress(100, "APK generated successfully!")
                return finalPath
            } else {
                AppLogger.e("ApkBuilder", "Exporting APK to public downloads folder failed")
                return null
            }

        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Build sequence failed", e)
            return null
        } finally {
            unsignedApk.delete()
            alignedApk.delete()
            signedApk.delete()
            if (isCustomSigningActive) {
                // Clear app-local keystore credentials securely
                File(context.filesDir, "custom_keystore.p12").delete()
                File(context.filesDir, "custom_keystore_password.txt").delete()
                File(context.filesDir, "custom_keystore_alias.txt").delete()
                File(context.filesDir, "custom_keystore_keypass.txt").delete()
                signer.reloadKeys()
            }
        }
    }

    private fun copyFileToPublicDownloads(
        context: Context,
        srcFile: File,
        relativeFolder: String,
        displayName: String,
        mimeType: String,
        customExportUri: String? = null
    ): String? {
        if (customExportUri != null) {
            try {
                val treeUri = android.net.Uri.parse(customExportUri)
                val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                if (documentFile != null && documentFile.canWrite()) {
                    val existingFile = documentFile.findFile(displayName)
                    existingFile?.delete()
                    
                    val newFile = documentFile.createFile(mimeType, displayName)
                    if (newFile != null) {
                        context.contentResolver.openOutputStream(newFile.uri)?.use { outStream ->
                            srcFile.inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                        return newFile.uri.toString() // Return URI string for SAF
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to write via SAF: ${e.message}", e)
            }
        }
        val resolver = context.contentResolver
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                val externalUri = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                
                // Clear any existing matching entry first to prevent duplicate entries/appended indexes
                val selection = "${android.provider.MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${android.provider.MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(displayName, "Download/$relativeFolder/")
                resolver.delete(externalUri, selection, selectionArgs)

                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/$relativeFolder")
                }
                
                val uri = resolver.insert(externalUri, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri).use { outStream ->
                        if (outStream != null) {
                            srcFile.inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                            return File(File(downloadsDir, relativeFolder), displayName).absolutePath
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to write via MediaStore: ${e.message}", e)
            }
        }
        
        // Fallback for pre-Q or if MediaStore fails
        try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val packoraDir = File(downloadsDir, relativeFolder)
            if (!packoraDir.exists()) {
                packoraDir.mkdirs()
            }
            val destFile = File(packoraDir, displayName)
            if (destFile.exists()) {
                destFile.delete()
            }
            srcFile.copyTo(destFile, overwrite = true)
            return destFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to write via legacy File API: ${e.message}", e)
        }
        
        return null
    }

    private fun isIconEntry(name: String): Boolean {
        return ApkTemplate.ICON_PATHS.any { it.first == name } ||
               ApkTemplate.ROUND_ICON_PATHS.any { it.first == name }
    }

    private fun getIconSize(name: String): Int {
        val pathMatch = ApkTemplate.ICON_PATHS.find { it.first == name }
            ?: ApkTemplate.ROUND_ICON_PATHS.find { it.first == name }
        return pathMatch?.second ?: 96
    }

    private fun getDefaultMascotIcon(context: Context): Bitmap {
        val width = 512
        val height = 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgId = context.resources.getIdentifier("ic_launcher_background", "drawable", context.packageName)
        val bgDrawable = if (bgId != 0) ResourcesCompat.getDrawable(context.resources, bgId, context.theme) else null
        if (bgDrawable != null) {
            bgDrawable.setBounds(0, 0, width, height)
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.parseColor("#3DDC84"))
        }

        val fgId = context.resources.getIdentifier("ic_launcher_foreground", "drawable", context.packageName)
        val fgDrawable = if (fgId != 0) ResourcesCompat.getDrawable(context.resources, fgId, context.theme) else null
        if (fgDrawable != null) {
            val paddingX = (width * 0.14).toInt()
            val paddingY = (height * 0.14).toInt()
            fgDrawable.setBounds(paddingX, paddingY, width - paddingX, height - paddingY)
            fgDrawable.draw(canvas)
        }

        return bitmap
    }
}
