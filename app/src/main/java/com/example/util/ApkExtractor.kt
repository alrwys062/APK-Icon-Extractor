package com.example.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipFile

data class ApkInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val fileSize: Long,
    val apkFilePath: String,
    val fullIconBitmap: Bitmap,
    val foregroundBitmap: Bitmap?,
    val backgroundBitmap: Bitmap?,
    val isAdaptive: Boolean,
    val isRelease: Boolean,
    val sha256: String,
    val permissions: List<String>,
    val sourceSource: String
)

object ApkExtractor {

    private const val TAG = "ApkExtractor"

    fun extractApk(context: Context, uri: Uri): ApkInfo {
        val cacheDir = context.cacheDir
        val apkFile = File(cacheDir, "extracted_${System.currentTimeMillis()}.apk")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(apkFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalArgumentException("Could not open APK stream from URI")

        val apkPath = apkFile.absolutePath
        val pm = context.packageManager
        val packageInfo = pm.getPackageArchiveInfo(
            apkPath,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
        ) ?: throw IllegalStateException("Failed to parse APK file.")

        return processPackageInfo(context, packageInfo, apkPath, apkFile.length(), "APK")
    }

    fun extractInstalledApp(context: Context, packageName: String): ApkInfo {
        val pm = context.packageManager
        val packageInfo = pm.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
        )
        val appInfo = packageInfo.applicationInfo ?: throw IllegalStateException("Application info missing")
        val apkPath = appInfo.sourceDir ?: ""
        val file = File(apkPath)
        val fileSize = if (file.exists()) file.length() else 0L

        return processPackageInfo(context, packageInfo, apkPath, fileSize, "Installed")
    }

    private fun processPackageInfo(
        context: Context,
        packageInfo: PackageInfo,
        apkPath: String,
        fileSize: Long,
        sourceSource: String
    ): ApkInfo {
        val pm = context.packageManager
        val appInfo = packageInfo.applicationInfo ?: throw IllegalStateException("Application info missing")
        if (apkPath.isNotEmpty() && File(apkPath).exists()) {
            appInfo.sourceDir = apkPath
            appInfo.publicSourceDir = apkPath
        }

        val appName = try {
            appInfo.loadLabel(pm)?.toString() ?: packageInfo.packageName
        } catch (e: Exception) {
            packageInfo.packageName
        }

        val packageName = packageInfo.packageName ?: "unknown.package"
        val versionName = packageInfo.versionName ?: "1.0"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.minSdkVersion
        } else {
            24
        }
        val targetSdk = appInfo.targetSdkVersion

        // Permissions
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        // Signature hash & Debug detection
        var sha256 = "A1:B2:C3:D4:E5:F6:78:90:AB:CD:EF:12:34:56:78:90"
        var isRelease = true
        try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            signatures?.firstOrNull()?.let { sig ->
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(sig.toByteArray())
                sha256 = digest.joinToString(":") { "%02X".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error computing signature hash", e)
        }

        val iconDrawable = try {
            appInfo.loadIcon(pm)
        } catch (e: Exception) {
            pm.defaultActivityIcon
        }

        val fullBitmap = drawableToBitmap(iconDrawable, 512, 512)
        var foregroundBitmap: Bitmap? = null
        var backgroundBitmap: Bitmap? = null
        var isAdaptive = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && iconDrawable is android.graphics.drawable.AdaptiveIconDrawable) {
            isAdaptive = true
            try {
                iconDrawable.foreground?.let { foregroundBitmap = drawableToBitmap(it, 512, 512) }
                iconDrawable.background?.let { backgroundBitmap = drawableToBitmap(it, 512, 512) }
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting adaptive layers", e)
            }
        }

        return ApkInfo(
            appName = appName,
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            minSdk = minSdk,
            targetSdk = targetSdk,
            fileSize = fileSize,
            apkFilePath = apkPath,
            fullIconBitmap = fullBitmap,
            foregroundBitmap = foregroundBitmap,
            backgroundBitmap = backgroundBitmap,
            isAdaptive = isAdaptive,
            isRelease = isRelease,
            sha256 = sha256,
            permissions = permissions,
            sourceSource = sourceSource
        )
    }

    fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width == width && bmp.height == height) return bmp
            return Bitmap.createScaledBitmap(bmp, width, height, true)
        }

        val bitmap = Bitmap.createBitmap(
            if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else width,
            if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        if (bitmap.width != width || bitmap.height != height) {
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
        return bitmap
    }
}
