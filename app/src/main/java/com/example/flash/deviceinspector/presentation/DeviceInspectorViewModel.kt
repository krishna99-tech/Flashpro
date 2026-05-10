package com.example.flash.deviceinspector.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flash.deviceinspector.domain.model.*
import com.example.flash.deviceinspector.domain.repository.SystemInfoRepository
import com.example.flash.deviceinspector.domain.repository.SystemInfoRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceInspectorUi(
    val loading: Boolean = true,
    val device: List<InfoSection> = emptyList(),
    val display: List<InfoSection> = emptyList(),
    val cpu: List<InfoSection> = emptyList(),
    val network: List<InfoSection> = emptyList(),
    val wifiScan: List<NetworkScanResult> = emptyList(),
    val bluetooth: List<String> = emptyList(),
    val sensors: List<SensorInfo> = emptyList(),
    val cameras: List<CameraInfo> = emptyList(),
    val audio: List<InfoSection> = emptyList(),
    val codecs: List<InfoSection> = emptyList(),
    val apps: List<InstalledApp> = emptyList(),
    val battery: List<InfoSection> = emptyList(),
    val features: List<InfoItem> = emptyList(),
    val telephony: List<InfoSection> = emptyList(),
    val error: String? = null
)

class DeviceInspectorViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: SystemInfoRepository = SystemInfoRepositoryImpl(application.applicationContext)
    private val _ui = MutableStateFlow(DeviceInspectorUi())
    val ui: StateFlow<DeviceInspectorUi> = _ui

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                DeviceInspectorUi(
                    loading = false,
                    device = repo.getDeviceInfo(),
                    display = repo.getDisplayInfo(),
                    cpu = repo.getCpuInfo(),
                    network = repo.getNetworkInfo(),
                    wifiScan = repo.scanWifi(),
                    bluetooth = repo.getBluetoothDevices(),
                    sensors = repo.getSensors(),
                    cameras = repo.getCameras(),
                    audio = repo.getAudioInfo(),
                    codecs = repo.getVideoCodecs(),
                    apps = repo.getInstalledApps(),
                    battery = repo.getBatteryInfo(),
                    features = repo.getHardwareFeatures(),
                    telephony = repo.getTelephonyInfo(),
                    error = null
                )
            }.onSuccess { _ui.value = it }
             .onFailure { _ui.update { old -> old.copy(loading = false, error = it.message ?: "Unknown error") } }
        }
    }
}
