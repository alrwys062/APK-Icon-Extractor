package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ExtractedIconEntity
import com.example.data.repository.IconRepository
import com.example.util.ApkExtractor
import com.example.util.ApkInfo
import com.example.util.ExportFormat
import com.example.util.ImageExporter
import com.example.util.MaskShape
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IconLayer {
    FULL, FOREGROUND, BACKGROUND
}

class MainViewModel(context: Context) : ViewModel() {

    private val repository: IconRepository

    init {
        val dao = AppDatabase.getDatabase(context).extractedIconDao()
        repository = IconRepository(dao)
    }

    val history: StateFlow<List<ExtractedIconEntity>> = repository.allIcons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedApkInfo = MutableStateFlow<ApkInfo?>(null)
    val selectedApkInfo: StateFlow<ApkInfo?> = _selectedApkInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _exportFormat = MutableStateFlow(ExportFormat.PNG)
    val exportFormat: StateFlow<ExportFormat> = _exportFormat.asStateFlow()

    private val _exportSize = MutableStateFlow(512)
    val exportSize: StateFlow<Int> = _exportSize.asStateFlow()

    private val _maskShape = MutableStateFlow(MaskShape.ROUNDED_SQUARE)
    val maskShape: StateFlow<MaskShape> = _maskShape.asStateFlow()

    private val _includeBackground = MutableStateFlow(true)
    val includeBackground: StateFlow<Boolean> = _includeBackground.asStateFlow()

    private val _selectedLayer = MutableStateFlow(IconLayer.FULL)
    val selectedLayer: StateFlow<IconLayer> = _selectedLayer.asStateFlow()

    fun setExportFormat(format: ExportFormat) { _exportFormat.value = format }
    fun setExportSize(size: Int) { _exportSize.value = size }
    fun setMaskShape(shape: MaskShape) { _maskShape.value = shape }
    fun setIncludeBackground(include: Boolean) { _includeBackground.value = include }
    fun setSelectedLayer(layer: IconLayer) { _selectedLayer.value = layer }

    fun loadApk(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val apkInfo = ApkExtractor.extractApk(context, uri)
                _selectedApkInfo.value = apkInfo
                saveToHistory(context, apkInfo)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to extract APK: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadInstalledApp(context: Context, packageName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val apkInfo = ApkExtractor.extractInstalledApp(context, packageName)
                _selectedApkInfo.value = apkInfo
                saveToHistory(context, apkInfo)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to extract installed app: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveToHistory(context: Context, apkInfo: ApkInfo) {
        val currentBitmap = when (_selectedLayer.value) {
            IconLayer.FULL -> apkInfo.fullIconBitmap
            IconLayer.FOREGROUND -> apkInfo.foregroundBitmap ?: apkInfo.fullIconBitmap
            IconLayer.BACKGROUND -> apkInfo.backgroundBitmap ?: apkInfo.fullIconBitmap
        }
        val processed = ImageExporter.processBitmap(currentBitmap, _exportSize.value, _maskShape.value, _includeBackground.value)
        val savedFile = ImageExporter.saveToCache(context, processed, apkInfo.packageName, _exportFormat.value)

        val entity = ExtractedIconEntity(
            appName = apkInfo.appName,
            packageName = apkInfo.packageName,
            versionName = apkInfo.versionName,
            versionCode = apkInfo.versionCode,
            minSdk = apkInfo.minSdk,
            targetSdk = apkInfo.targetSdk,
            fileSize = apkInfo.fileSize,
            imagePath = savedFile.absolutePath,
            isRelease = apkInfo.isRelease,
            sha256 = apkInfo.sha256,
            sourceSource = apkInfo.sourceSource
        )
        repository.insert(entity)
    }

    fun saveToGallery(context: Context) {
        val apkInfo = _selectedApkInfo.value ?: return
        val currentBitmap = getCurrentProcessedBitmap() ?: return
        try {
            val uri = ImageExporter.saveToGallery(context, currentBitmap, apkInfo.packageName, _exportFormat.value)
            if (uri != null) {
                _successMessage.value = "Saved successfully to Gallery!"
            } else {
                _errorMessage.value = "Failed to save image."
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error saving to gallery: ${e.localizedMessage}"
        }
    }

    fun shareIcon(context: Context) {
        val apkInfo = _selectedApkInfo.value ?: return
        val currentBitmap = getCurrentProcessedBitmap() ?: return
        try {
            val file = ImageExporter.saveToCache(context, currentBitmap, apkInfo.packageName, _exportFormat.value)
            val uri = ImageExporter.getShareUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when (_exportFormat.value) {
                    ExportFormat.PNG -> "image/png"
                    ExportFormat.WEBP -> "image/webp"
                    ExportFormat.JPEG -> "image/jpeg"
                    ExportFormat.SVG -> "image/svg+xml"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Icon")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            _errorMessage.value = "Error sharing icon: ${e.localizedMessage}"
        }
    }

    fun copyToClipboard(context: Context) {
        val currentBitmap = getCurrentProcessedBitmap() ?: return
        try {
            val file = ImageExporter.saveToCache(context, currentBitmap, "clipboard_icon", ExportFormat.PNG)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newUri(context.contentResolver, "Extracted App Icon", FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
            clipboard.setPrimaryClip(clip)
            _successMessage.value = "Copied to clipboard!"
        } catch (e: Exception) {
            _errorMessage.value = "Error copying to clipboard: ${e.localizedMessage}"
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch { repository.updateFavorite(id, isFavorite) }
    }

    fun clearSelectedApk() { _selectedApkInfo.value = null }
    fun clearMessages() { _errorMessage.value = null; _successMessage.value = null }

    private fun getCurrentProcessedBitmap(): Bitmap? {
        val apkInfo = _selectedApkInfo.value ?: return null
        val source = when (_selectedLayer.value) {
            IconLayer.FULL -> apkInfo.fullIconBitmap
            IconLayer.FOREGROUND -> apkInfo.foregroundBitmap ?: apkInfo.fullIconBitmap
            IconLayer.BACKGROUND -> apkInfo.backgroundBitmap ?: apkInfo.fullIconBitmap
        }
        return ImageExporter.processBitmap(source, _exportSize.value, _maskShape.value, _includeBackground.value)
    }
}
