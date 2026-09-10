package com.maheswara660.packora.builder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ApkTemplate(private val context: Context) {

    companion object {
        const val CONFIG_PATH = "assets/app_config.json"

        // Both path formats: AAPT2 may emit either -v4 or no qualifier suffix
        val ICON_PATHS = listOf(
            "res/mipmap-mdpi/ic_launcher.png" to 48,
            "res/mipmap-hdpi/ic_launcher.png" to 72,
            "res/mipmap-xhdpi/ic_launcher.png" to 96,
            "res/mipmap-xxhdpi/ic_launcher.png" to 144,
            "res/mipmap-xxxhdpi/ic_launcher.png" to 192,
            // Legacy -v4 variants
            "res/mipmap-mdpi-v4/ic_launcher.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher.png" to 192
        )

        val ROUND_ICON_PATHS = listOf(
            "res/mipmap-mdpi/ic_launcher_round.png" to 48,
            "res/mipmap-hdpi/ic_launcher_round.png" to 72,
            "res/mipmap-xhdpi/ic_launcher_round.png" to 96,
            "res/mipmap-xxhdpi/ic_launcher_round.png" to 144,
            "res/mipmap-xxxhdpi/ic_launcher_round.png" to 192,
            // Legacy -v4 variants
            "res/mipmap-mdpi-v4/ic_launcher_round.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher_round.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher_round.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher_round.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher_round.png" to 192
        )
    }

    private val templateDir = File(context.cacheDir, "apk_templates")

    init {
        templateDir.mkdirs()
    }

    fun getTemplateApk(): File? {
        val templateFile = File(templateDir, "webview_shell.apk")

        // Always re-extract from assets to ensure we never use a stale cached version
        return try {
            context.assets.open("template/webview_shell.apk").use { input ->
                FileOutputStream(templateFile).use { output ->
                    input.copyTo(output)
                }
            }
            templateFile
        } catch (e: Exception) {
            AppLogger.e("ApkTemplate", "Failed to extract webview_shell.apk from assets", e)
            if (templateFile.exists() && templateFile.length() > 0) templateFile else null
        }
    }

    fun hasTemplate(): Boolean {
        return try {
            context.assets.open("template/webview_shell.apk").close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun scaleBitmapToPng(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw clean white rounded squircle background
        val radius = size * 0.18f
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        canvas.drawRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), radius, radius, bgPaint)

        // 88% safe zone for crisp, clear high-resolution visual visibility
        val safeZoneSize = (size * 0.88f).toInt()
        val padding = (size - safeZoneSize) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        val matrix = Matrix()
        val scale = safeZoneSize.toFloat() / bitmap.width.toFloat()
        matrix.postScale(scale, scale)
        matrix.postTranslate(padding, padding)

        canvas.drawBitmap(bitmap, matrix, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()

        return baos.toByteArray()
    }

    fun loadBitmap(iconPath: String): Bitmap? {
        return try {
            if (iconPath.startsWith("/")) {
                BitmapFactory.decodeFile(iconPath)
            } else if (iconPath.startsWith("content://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(iconPath))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                BitmapFactory.decodeFile(iconPath)
            }
        } catch (e: Exception) {
            AppLogger.e("ApkTemplate", "Failed to load bitmap from: $iconPath", e)
            null
        }
    }

    fun createAdaptiveForegroundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 76% safe zone for adaptive foreground icons according to Android spec
        val safeZoneSize = (size * 0.76f).toInt()
        val padding = (size - safeZoneSize) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        val matrix = Matrix()
        val scale = safeZoneSize.toFloat() / bitmap.width.toFloat()
        matrix.postScale(scale, scale)
        matrix.postTranslate(padding, padding)

        canvas.drawBitmap(bitmap, matrix, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()

        return baos.toByteArray()
    }

    fun createAdaptiveBackgroundIcon(size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()

        return baos.toByteArray()
    }

    fun createRoundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw white circle background
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, bgPaint)

        // 88% safe zone for round launcher icons
        val safeZoneSize = (size * 0.88f).toInt()
        val padding = (size - safeZoneSize) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        val matrix = Matrix()
        val scale = safeZoneSize.toFloat() / bitmap.width.toFloat()
        matrix.postScale(scale, scale)
        matrix.postTranslate(padding, padding)

        canvas.drawBitmap(bitmap, matrix, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()

        return baos.toByteArray()
    }

    fun clearCache() {
        templateDir.listFiles()?.forEach { it.delete() }
    }
}
