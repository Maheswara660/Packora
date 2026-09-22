package com.maheswara660.packora.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast
import com.maheswara660.packora.manager.PackoraPreferencesManager
import java.io.File

class PackageInstallStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val apkPath = intent.getStringExtra("apk_path")
        val appName = intent.getStringExtra("app_name")
        val pkgName = intent.getStringExtra("package_name")
            ?: intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        val displayName = appName ?: pkgName ?: "App"

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // System policy or permissions require user confirmation
                val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }

                if (confirmationIntent != null) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(confirmationIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error launching installer confirmation: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                // Background update succeeded
                val prefs = PackoraPreferencesManager(context)
                if (prefs.autoDeleteApkAfterInstall && !apkPath.isNullOrBlank()) {
                    try {
                        val file = File(apkPath)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        // Ignored
                    }
                }
                Toast.makeText(context, "$displayName update installed successfully!", Toast.LENGTH_SHORT).show()
            }

            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                // User cancelled the prompt, ignore without error
            }

            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Toast.makeText(
                    context,
                    "$displayName update failed: ${message ?: "Installation error ($status)"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
