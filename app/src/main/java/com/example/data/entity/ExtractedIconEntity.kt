package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extracted_icons")
data class ExtractedIconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val fileSize: Long,
    val imagePath: String,
    val isFavorite: Boolean = false,
    val sourceSource: String = "APK", // APK or INSTALLED
    val isRelease: Boolean = true,
    val sha256: String = "",
    val permissions: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
