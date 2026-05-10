package com.example.flash.deviceinspector.domain.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.vector.ImageVector

data class InfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val available: Boolean = true,
    val highlight: Boolean = false
)

data class InfoSection(
    val title: String,
    val icon: ImageVector,
    val items: List<InfoItem>
)

data class SensorInfo(
    val name: String,
    val type: String,
    val vendor: String,
    val version: Int,
    val resolution: Float,
    val maxRange: Float,
    val power: Float,
    val minDelay: Int,
    val maxDelay: Int,
    val isWakeUp: Boolean,
    val liveValue: String? = null,
    val icon: ImageVector
)

data class InstalledApp(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val installDate: Long,
    val lastUpdated: Long,
    val apkSizeBytes: Long,
    val isSystemApp: Boolean,
    val isDebuggable: Boolean,
    val targetSdk: Int,
    val minSdk: Int,
    val icon: Bitmap? = null
)

data class CameraInfo(
    val id: String,
    val facing: String,
    val maxMp: String,
    val focalLengths: String,
    val apertures: String,
    val hasOis: Boolean,
    val hasFlash: Boolean,
    val hardwareLevel: String,
    val maxVideoResolution: String,
    val supportsRaw: Boolean,
    val supportsHdr: Boolean
)

data class NetworkScanResult(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val security: String,
    val standard: String,
    val channelWidth: String
)

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val msg: String) : UiState<Nothing>()
}
