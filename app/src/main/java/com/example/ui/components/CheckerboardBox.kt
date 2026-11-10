package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun CheckerboardBox(
    modifier: Modifier = Modifier,
    squareSize: Float = 24f,
    color1: Color = Color(0xFFE5E7EB),
    color2: Color = Color(0xFF9CA3AF),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            var isDark = false

            var y = 0f
            while (y < height) {
                var x = 0f
                while (x < width) {
                    drawRect(
                        color = if (isDark) color2 else color1,
                        topLeft = Offset(x, y),
                        size = Size(squareSize, squareSize)
                    )
                    x += squareSize
                    isDark = !isDark
                }
                y += squareSize
                if ((width / squareSize).toInt() % 2 == 0) {
                    isDark = !isDark
                }
            }
        }
        content()
    }
}
