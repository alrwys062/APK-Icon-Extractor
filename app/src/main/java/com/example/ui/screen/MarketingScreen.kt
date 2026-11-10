package com.example.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.ApkInfo
import com.example.util.ImageExporter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingScreen(
    apkInfo: ApkInfo,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Auto-generated marketing text in Arabic
    val generatedDescription = remember(apkInfo) {
        """
        🚀 ارفع أداءك الرقمي مع تطبيق "${apkInfo.appName}" الرائع!
        
        تطبيق احترافي جديد كلياً تم تصميمه بعناية ليقدم لك تجربة مستخدم سلسة، سريعة، وموثوقة.
        • اسم الحزمة: ${apkInfo.packageName}
        • الإصدار: ${apkInfo.versionName} (${apkInfo.versionCode})
        • متوافق مع أنظمة أندرويد الحديثة (الحد الأدنى SDK ${apkInfo.minSdk}، المستهدف SDK ${apkInfo.targetSdk}).
        
        حمّله الآن واستمتع بالميزات الحصرية! 📥✨
        """.trimIndent()
    }

    val generatedFeatures = remember(apkInfo) {
        """
        ✨ الميزات البارزة في تطبيق ${apkInfo.appName}:
        1. تصميم عصري وواجهة مستخدم احترافية متوافقة مع Material 3.
        2. سرعة فائقة واستقرار تام في الأداء.
        3. خفيف الحجم (${apkInfo.fileSize / (1024 * 1024)} ميجابايت) ولا يستهلك موارد الجهاز.
        4. دعم كامل للوضع الليلي واللغة العربية.
        5. تحديثات مستمرة لضمان الأمان والاعتمادية.
        """.trimIndent()
    }

    var descriptionText by remember { mutableStateOf(generatedDescription) }
    var featuresText by remember { mutableStateOf(generatedFeatures) }
    var selectedScreenshots by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val screenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedScreenshots = uris.take(5) // Max 5 screenshots
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أداة النشر والتسويق (واتساب وتليجرام)", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: AI Generated Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("وصف التطبيق التسويقي (توليد تلقائي)", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Description", descriptionText))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22C55E),
                            unfocusedBorderColor = Color(0xFF1E3A2F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Section 2: AI Generated Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ميزات التطبيق البارزة", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Features", featuresText))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = featuresText,
                        onValueChange = { featuresText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22C55E),
                            unfocusedBorderColor = Color(0xFF1E3A2F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Section 3: Phone Mockup Screenshots Picker
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143823))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("لقطات شاشة التطبيق داخل إطار هاتف (${selectedScreenshots.size}/5)", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        Button(
                            onClick = { screenshotLauncher.launch(arrayOf("image/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اختر لقطات", color = Color.Black)
                        }
                    }

                    if (selectedScreenshots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedScreenshots.forEach { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp, 120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: WhatsApp & Telegram Direct Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val fullMessage = "$descriptionText\n\n$featuresText"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            setPackage("com.whatsapp")
                            putExtra(Intent.EXTRA_TEXT, fullMessage)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to general share
                            val generalIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, fullMessage)
                            }
                            context.startActivity(Intent.createChooser(generalIntent, "مشاركة عبر"))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("إرسال إلى واتساب", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        val fullMessage = "$descriptionText\n\n$featuresText"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            setPackage("org.telegram.messenger")
                            putExtra(Intent.EXTRA_TEXT, fullMessage)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val generalIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, fullMessage)
                            }
                            context.startActivity(Intent.createChooser(generalIntent, "مشاركة عبر"))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9))
                ) {
                    Text("إرسال إلى تليجرام", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
