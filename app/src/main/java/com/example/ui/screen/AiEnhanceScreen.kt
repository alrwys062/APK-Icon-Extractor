package com.example.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AiIconEnhancerEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiEnhanceScreen(
    originalBitmap: Bitmap,
    onSaveEnhanced: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    var scaleFactor by remember { mutableStateOf(2) }
    var enhanceSharpen by remember { mutableStateOf(true) }
    var boostVibrance by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    val enhancedBitmap = remember(originalBitmap, scaleFactor, enhanceSharpen, boostVibrance) {
        AiIconEnhancerEngine.enhanceIcon(originalBitmap, scaleFactor, enhanceSharpen, boostVibrance)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معسن الذكاء الاصطناعي الفائق", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D2417))
            )
        },
        containerColor = Color(0xFF07150E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Preview Card
            Card(
                modifier = Modifier
                    .size(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = enhancedBitmap.asImageBitmap(),
                        contentDescription = "AI Enhanced Icon",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خيارات التحسين بالذكاء الاصطناعي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مضاعفة الدقة (Upscale)", color = Color(0xFF9CA3AF))
                        SingleChoiceSegmentedButtonRow {
                            listOf(2, 4).forEachIndexed { index, scale ->
                                SegmentedButton(
                                    selected = scaleFactor == scale,
                                    onClick = { scaleFactor = scale },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                                ) {
                                    Text("${scale}x")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إزالة التشويش والحدّة (Sharpen)", color = Color(0xFF9CA3AF))
                        Switch(
                            checked = enhanceSharpen,
                            onCheckedChange = { enhanceSharpen = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تعزيز الحيوية والألوان", color = Color(0xFF9CA3AF))
                        Switch(
                            checked = boostVibrance,
                            onCheckedChange = { boostVibrance = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveEnhanced(enhancedBitmap) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تطبيق وحفظ النتيجة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
