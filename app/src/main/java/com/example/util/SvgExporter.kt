package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object SvgExporter {

    fun exportToSvgFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val width = bitmap.width
        val height = bitmap.height
        
        // Convert bitmap to base64 embedded SVG
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

        val svgContent = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$width" height="$height" viewBox="0 0 $width $height">
                <image width="$width" height="$height" xlink:href="data:image/png;base64,$base64Image"/>
            </svg>
        """.trimIndent()

        val cacheDir = File(context.cacheDir, "exported_svgs")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "${fileName}_${System.currentTimeMillis()}.svg")
        FileOutputStream(file).use {
            it.write(svgContent.toByteArray())
        }
        return file
    }
}
