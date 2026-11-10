package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screen.AiEnhanceScreen
import com.example.ui.screen.HistoryScreen
import com.example.ui.screen.HomeScreen
import com.example.ui.screen.InstalledAppsScreen
import com.example.ui.screen.ResultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

enum class AppScreen {
    HOME, RESULT, HISTORY, INSTALLED_APPS, AI_ENHANCE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val viewModel: MainViewModel = viewModel { MainViewModel(context) }

                    val history by viewModel.history.collectAsState()
                    val selectedApkInfo by viewModel.selectedApkInfo.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    val successMessage by viewModel.successMessage.collectAsState()

                    val exportFormat by viewModel.exportFormat.collectAsState()
                    val exportSize by viewModel.exportSize.collectAsState()
                    val maskShape by viewModel.maskShape.collectAsState()
                    val includeBackground by viewModel.includeBackground.collectAsState()
                    val selectedLayer by viewModel.selectedLayer.collectAsState()

                    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(intent) {
                        intent?.data?.let { uri ->
                            viewModel.loadApk(context, uri)
                            currentScreen = AppScreen.RESULT
                        }
                    }

                    val mimeApk = "application/vnd.android.package-archive"
                    val mimeOctet = "application/octet-stream"
                    val mimeAny = "*/*"

                    val apkPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let {
                            viewModel.loadApk(context, it)
                            currentScreen = AppScreen.RESULT
                        }
                    }

                    LaunchedEffect(errorMessage) {
                        errorMessage?.let {
                            snackbarHostState.showSnackbar(it)
                            viewModel.clearMessages()
                        }
                    }

                    LaunchedEffect(successMessage) {
                        successMessage?.let {
                            snackbarHostState.showSnackbar(it)
                            viewModel.clearMessages()
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { _ ->
                        when (currentScreen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    history = history,
                                    isLoading = isLoading,
                                    onSelectApkClick = {
                                        apkPickerLauncher.launch(arrayOf(mimeApk, mimeOctet, mimeAny))
                                    },
                                    onSelectInstalledClick = {
                                        currentScreen = AppScreen.INSTALLED_APPS
                                    },
                                    onHistoryItemClick = { item ->
                                        currentScreen = AppScreen.RESULT
                                    },
                                    onDeleteItem = { id ->
                                        viewModel.deleteHistoryItem(id)
                                    },
                                    onToggleFavorite = { id, fav ->
                                        viewModel.toggleFavorite(id, fav)
                                    },
                                    onOpenHistoryScreen = {
                                        currentScreen = AppScreen.HISTORY
                                    }
                                )
                            }
                            AppScreen.INSTALLED_APPS -> {
                                BackHandler { currentScreen = AppScreen.HOME }
                                InstalledAppsScreen(
                                    onAppSelected = { packageName ->
                                        viewModel.loadInstalledApp(context, packageName)
                                        currentScreen = AppScreen.RESULT
                                    },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                            AppScreen.RESULT -> {
                                BackHandler {
                                    currentScreen = AppScreen.HOME
                                    viewModel.clearSelectedApk()
                                }
                                selectedApkInfo?.let { apkInfo ->
                                    ResultScreen(
                                        apkInfo = apkInfo,
                                        exportFormat = exportFormat,
                                        exportSize = exportSize,
                                        maskShape = maskShape,
                                        includeBackground = includeBackground,
                                        selectedLayer = selectedLayer,
                                        onFormatChange = { viewModel.setExportFormat(it) },
                                        onSizeChange = { viewModel.setExportSize(it) },
                                        onMaskShapeChange = { viewModel.setMaskShape(it) },
                                        onIncludeBackgroundChange = { viewModel.setIncludeBackground(it) },
                                        onLayerChange = { viewModel.setSelectedLayer(it) },
                                        onSaveToGallery = { viewModel.saveToGallery(context) },
                                        onShare = { viewModel.shareIcon(context) },
                                        onCopy = { viewModel.copyToClipboard(context) },
                                        onOpenAiEnhance = { currentScreen = AppScreen.AI_ENHANCE },
                                        onBack = {
                                            currentScreen = AppScreen.HOME
                                            viewModel.clearSelectedApk()
                                        }
                                    )
                                } ?: run {
                                    LaunchedEffect(Unit) { currentScreen = AppScreen.HOME }
                                }
                            }
                            AppScreen.AI_ENHANCE -> {
                                BackHandler { currentScreen = AppScreen.RESULT }
                                selectedApkInfo?.let { apkInfo ->
                                    AiEnhanceScreen(
                                        originalBitmap = apkInfo.fullIconBitmap,
                                        onSaveEnhanced = { enhanced ->
                                            viewModel.saveToGallery(context)
                                            currentScreen = AppScreen.RESULT
                                        },
                                        onBack = { currentScreen = AppScreen.RESULT }
                                    )
                                } ?: run {
                                    LaunchedEffect(Unit) { currentScreen = AppScreen.HOME }
                                }
                            }
                            AppScreen.HISTORY -> {
                                BackHandler { currentScreen = AppScreen.HOME }
                                HistoryScreen(
                                    history = history,
                                    onHistoryItemClick = { item ->
                                        currentScreen = AppScreen.HOME
                                    },
                                    onDeleteItem = { id ->
                                        viewModel.deleteHistoryItem(id)
                                    },
                                    onToggleFavorite = { id, fav ->
                                        viewModel.toggleFavorite(id, fav)
                                    },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
