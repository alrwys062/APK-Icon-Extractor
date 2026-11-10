package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newPos = sliderPosition + (dragAmount.x / size.width)
                    sliderPosition = newPos.coerceIn(0f, 1f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // After Image (Base)
        androidx.compose.foundation.Image(
            bitmap = afterBitmap.asImageBitmap(),
            contentDescription = "AI Enhanced",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Before Image (Clipped)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize()
        ) {
            androidx.compose.foundation.Image(
                bitmap = beforeBitmap.asImageBitmap(),
                contentDescription = "Original",
                modifier = Modifier
                    .fillMaxSize()
                    .matchParentSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Divider Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.CenterStart)
                .offset(x = (sliderPosition * 300).dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
