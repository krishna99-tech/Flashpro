package com.example.flash.deviceinspector.domain.repository

import android.content.Context
import com.example.flash.deviceinspector.data.provider.*
import com.example.flash.deviceinspector.domain.model.*

interface SystemInfoRepository {
    suspend fun getDeviceInfo(): List<InfoSection>
    suspend fun getDisplayInfo(): List<InfoSection>
    suspend fun getCpuInfo(): List<InfoSection>
    suspend fun getNetworkInfo(): List<InfoSection>
    suspend fun scanWifi(): List<NetworkScanResult>
    suspend fun getSensors(): List<SensorInfo>
    suspend fun getCameras(): List<CameraInfo>
    suspend fun getAudioInfo(): List<InfoSection>
    suspend fun getVideoCodecs(): List<InfoSection>
    suspend fun getInstalledApps(): List<InstalledApp>
    suspend fun getBatteryInfo(): List<InfoSection>
    suspend fun getHardwareFeatures(): List<InfoItem>
    suspend fun getTelephonyInfo(): List<InfoSection>
    suspend fun getBluetoothDevices(): List<String>
}

class SystemInfoRepositoryImpl(private val context: Context) : SystemInfoRepository {
    private val device = DeviceInfoProvider()
    private val display = DisplayInfoProvider()
    private val cpu = CpuInfoProvider()
    private val network = NetworkInfoProvider()
    private val wifiScan = WifiScanProvider()
    private val sensors = SensorInfoProvider()
    private val cameras = CameraInfoProvider()
    private val audio = AudioInfoProvider()
    private val video = VideoCodecProvider()
    private val apps = AppListProvider()
    private val battery = BatteryInfoProvider()
    private val features = HardwareFeaturesProvider()
    private val telephony = TelephonyProvider()

    override suspend fun getDeviceInfo() = device.get(context)
    override suspend fun getDisplayInfo() = display.get(context)
    override suspend fun getCpuInfo() = cpu.get()
    override suspend fun getNetworkInfo() = network.get(context)
    override suspend fun scanWifi() = wifiScan.scan(context)
    override suspend fun getSensors() = sensors.get(context)
    override suspend fun getCameras() = cameras.get(context)
    override suspend fun getAudioInfo() = audio.get(context)
    override suspend fun getVideoCodecs() = video.get()
    override suspend fun getInstalledApps() = apps.get(context)
    override suspend fun getBatteryInfo() = battery.get(context)
    override suspend fun getHardwareFeatures() = features.get(context)
    override suspend fun getTelephonyInfo() = telephony.get(context)
    override suspend fun getBluetoothDevices(): List<String> = wifiScan.bondedBluetooth()
}
