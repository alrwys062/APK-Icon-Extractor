package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

enum class ExportFormat(val extension: String, val compressFormat: Bitmap.CompressFormat) {
    PNG("png", Bitmap.CompressFormat.PNG),
    WEBP("webp", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP),
    JPEG("jpg", Bitmap.CompressFormat.JPEG),
    SVG("svg", Bitmap.CompressFormat.PNG)
}

enum class MaskShape {
    NONE, CIRCLE, SQUIRCLE, ROUNDED_SQUARE, HEXAGON, TEARDROP, BADGE, SQUARE
}

object ImageExporter {

    fun processBitmap(
        source: Bitmap,
        size: Int,
        maskShape: MaskShape,
        includeBackground: Boolean,
        backgroundColor: Int = android.graphics.Color.WHITE
    ): Bitmap {
        val scaled = Bitmap.createScaledBitmap(source, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        if (includeBackground && maskShape != MaskShape.CIRCLE) {
            paint.color = backgroundColor
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            paint.color = android.graphics.Color.WHITE
        }

        if (maskShape == MaskShape.NONE) {
            canvas.drawBitmap(scaled, 0f, 0f, paint)
            return output
        }

        val path = Path()
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        when (maskShape) {
            MaskShape.CIRCLE -> {
                val radius = size / 2f
                if (includeBackground) {
                    paint.color = backgroundColor
                    canvas.drawCircle(radius, radius, radius, paint)
                }
                canvas.drawCircle(radius, radius, radius, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
                paint.xfermode = null
            }
            MaskShape.ROUNDED_SQUARE -> {
                val cornerRadius = size * 0.22f
                path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
                canvas.clipPath(path)
                if (includeBackground) canvas.drawColor(backgroundColor)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
            MaskShape.SQUIRCLE -> {
                val cornerRadius = size * 0.35f
                path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
                canvas.clipPath(path)
                if (includeBackground) canvas.drawColor(backgroundColor)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
            MaskShape.HEXAGON -> {
                val radius = size / 2f
                for (i in 0..5) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val x = (radius + radius * Math.cos(angle)).toFloat()
                    val y = (radius + radius * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.clipPath(path)
                if (includeBackground) canvas.drawColor(backgroundColor)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
            MaskShape.TEARDROP -> {
                path.addRoundRect(rect, size * 0.5f, size * 0.5f, Path.Direction.CW)
                canvas.clipPath(path)
                if (includeBackground) canvas.drawColor(backgroundColor)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
            MaskShape.BADGE, MaskShape.SQUARE -> {
                if (includeBackground) canvas.drawColor(backgroundColor)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
            else -> {
                canvas.drawBitmap(scaled, 0f, 0f, paint)
            }
        }

        return output
    }

    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String, format: ExportFormat): Uri? {
        val filename = "${fileName}_${System.currentTimeMillis()}.${format.extension}"
        val mimeType = when (format) {
            ExportFormat.PNG -> "image/png"
            ExportFormat.WEBP -> "image/webp"
            ExportFormat.JPEG -> "image/jpeg"
            ExportFormat.SVG -> "image/svg+xml"
        }

        if (format == ExportFormat.SVG) {
            val file = SvgExporter.exportToSvgFile(context, bitmap, fileName)
            return Uri.fromFile(file)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/APKIconExtractor")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(format.compressFormat, 100, stream)
                }
            }
            return uri
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "APKIconExtractor")
            if (!appDir.exists()) appDir.mkdirs()
            val imageFile = File(appDir, filename)
            FileOutputStream(imageFile).use { stream ->
                bitmap.compress(format.compressFormat, 100, stream)
            }
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(imageFile)
            }
            context.sendBroadcast(intent)
            return Uri.fromFile(imageFile)
        }
    }

    fun saveToCache(context: Context, bitmap: Bitmap, fileName: String, format: ExportFormat): File {
        if (format == ExportFormat.SVG) {
            return SvgExporter.exportToSvgFile(context, bitmap, fileName)
        }
        val cacheDir = File(context.cacheDir, "exported_icons")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "${fileName}_${System.currentTimeMillis()}.${format.extension}")
        FileOutputStream(file).use { stream ->
            bitmap.compress(format.compressFormat, 100, stream)
        }
        return file
    }

    fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
