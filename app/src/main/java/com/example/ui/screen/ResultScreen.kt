package com.example.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Campaign
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
import com.example.ui.viewmodel.IconLayer
import com.example.util.ApkInfo
import com.example.util.ExportFormat
import com.example.util.ImageExporter
import com.example.util.MaskShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    apkInfo: ApkInfo,
    exportFormat: ExportFormat,
    exportSize: Int,
    maskShape: MaskShape,
    includeBackground: Boolean,
    selectedLayer: IconLayer,
    onFormatChange: (ExportFormat) -> Unit,
    onSizeChange: (Int) -> Unit,
    onMaskShapeChange: (MaskShape) -> Unit,
    onIncludeBackgroundChange: (Boolean) -> Unit,
    onLayerChange: (IconLayer) -> Unit,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onOpenAiEnhance: () -> Unit,
    onOpenMarketing: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    val sourceBitmap = when (selectedLayer) {
        IconLayer.FULL -> apkInfo.fullIconBitmap
        IconLayer.FOREGROUND -> apkInfo.foregroundBitmap ?: apkInfo.fullIconBitmap
        IconLayer.BACKGROUND -> apkInfo.backgroundBitmap ?: apkInfo.fullIconBitmap
    }

    val processedBitmap = remember(sourceBitmap, exportSize, maskShape, includeBackground) {
        ImageExporter.processBitmap(sourceBitmap, exportSize, maskShape, includeBackground)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل وتحرير الأيقونة", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenMarketing) {
                        Icon(Icons.Default.Campaign, contentDescription = "Marketing", tint = Color(0xFF22C55E))
                    }
                    IconButton(onClick = onOpenAiEnhance) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "AI Enhance", tint = Color(0xFF22C55E))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D2417))
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D2417)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", color = Color.White)
                    }

                    Button(
                        onClick = onSaveToGallery,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFF07150E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Preview Box
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF143823))
                    .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = "Extracted Icon Preview",
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marketing Quick Launch Button
            Button(
                onClick = onOpenMarketing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF143823))
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF22C55E))
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح أداة النشر والتسويق لواتساب وتليجرام", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = apkInfo.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = apkInfo.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF22C55E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF1E3A2F))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoColumn(label = "الإصدار", value = "${apkInfo.versionName} (${apkInfo.versionCode})")
                        InfoColumn(label = "SDK (الأنظار/المستهدف)", value = "${apkInfo.minSdk} / ${apkInfo.targetSdk}")
                        InfoColumn(label = "الحجم", value = "${apkInfo.fileSize / (1024 * 1024)} MB")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بصمة SHA-256: ${apkInfo.sha256.take(24)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Format
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تنسيق التصدير", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                SingleChoiceSegmentedButtonRow {
                    ExportFormat.values().forEachIndexed { index, format ->
                        SegmentedButton(
                            selected = exportFormat == format,
                            onClick = { onFormatChange(format) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ExportFormat.values().size)
                        ) {
                            Text(format.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resolution
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الدقة (Resolution)", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                SingleChoiceSegmentedButtonRow {
                    val sizes = listOf(192, 512, 1024, 2048)
                    sizes.forEachIndexed { index, size ->
                        SegmentedButton(
                            selected = exportSize == size,
                            onClick = { onSizeChange(size) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size)
                        ) {
                            Text("${size}px")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mask Shape
            Text("شكل القناع (Mask Shape)", style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            ScrollableTabRow(
                selectedTabIndex = MaskShape.values().indexOf(maskShape),
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                MaskShape.values().forEach { shape ->
                    Tab(
                        selected = maskShape == shape,
                        onClick = { onMaskShapeChange(shape) },
                        text = { Text(shape.name, color = if (maskShape == shape) Color(0xFF22C55E) else Color(0xFF9CA3AF)) }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
