package com.maheswara660.packora.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.receiver.PackageInstallStatusReceiver
import java.io.File

object PackageInstallerHelper {

    /**
     * Installs an APK package.
     * When [silent] is true on Android 12+ (API 31+), requests USER_ACTION_NOT_REQUIRED
     * to perform an unattended background installation without opening the package installer prompt.
     * If system policy requires user intervention (e.g. initial install, changed permissions),
     * it automatically delivers STATUS_PENDING_USER_ACTION which seamlessly launches the confirmation dialog.
     */
    fun installPackage(
        context: Context,
        apkPath: String,
        packageName: String? = null,
        appName: String? = null,
        silent: Boolean = false
    ) {
        val file = File(apkPath)
        if (!file.exists()) {
            Toast.makeText(context, "APK file not found: $apkPath", Toast.LENGTH_SHORT).show()
            return
        }

        // On Android versions below 12 (API 31) or when silent is false, fallback to standard installApkFile
        if (!silent || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            installApkFile(context, apkPath)
            return
        }

        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                if (!packageName.isNullOrBlank()) {
                    setAppPackageName(packageName)
                }
            }

            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            session.openWrite("package", 0, file.length()).use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                session.fsync(outputStream)
            }

            val callbackIntent = Intent(context, PackageInstallStatusReceiver::class.java).apply {
                putExtra("apk_path", apkPath)
                if (!packageName.isNullOrBlank()) {
                    putExtra("package_name", packageName)
                }
                if (!appName.isNullOrBlank()) {
                    putExtra("app_name", appName)
                }
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                callbackIntent,
                flags
            )

            val displayName = appName ?: packageName ?: "App"
            Toast.makeText(context, "Installing update for $displayName in the background...", Toast.LENGTH_SHORT).show()

            session.commit(pendingIntent.intentSender)
            session.close()
        } catch (e: Exception) {
            // Fallback to standard package installer if session creation fails
            installApkFile(context, apkPath)
        }
    }
}
