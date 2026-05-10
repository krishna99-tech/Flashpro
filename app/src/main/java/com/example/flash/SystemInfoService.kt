package com.example.flash

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import java.io.RandomAccessFile

data class DeviceHardwareInfo(
    val model: String = Build.MODEL,
    val manufacturer: String = Build.MANUFACTURER,
    val chipName: String = Build.HARDWARE,
    val architecture: String = System.getProperty("os.arch") ?: "Unknown",
    val androidVersion: String = Build.VERSION.RELEASE,
    val sdkVersion: Int = Build.VERSION.SDK_INT,
    val securityPatch: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Unknown",
    val buildNumber: String = Build.DISPLAY,
    val hardware: String = Build.HARDWARE,
    val board: String = Build.BOARD
)

data class CpuInfo(
    val usage: Float,
    val cores: Int = Runtime.getRuntime().availableProcessors(),
    val chipName: String = Build.HARDWARE
)

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val health: String,
    val temperature: Float,
    val technology: String
)

data class RamInfo(
    val total: Long,
    val available: Long,
    val threshold: Long
)

data class StorageInfo(
    val total: Long,
    val available: Long
)

data class WifiConnectionStatus(
    val connected: Boolean,
    val ssid: String,
    val ipAddress: String,
    val networkType: String
)

data class AppInfo(
    val name: String,
    val packageName: String,
    val version: String,
    val isSystemApp: Boolean
)

data class DeviceCapabilities(
    val processorName: String,
    val gpuName: String,
    val memoryTotal: Long,
    val bluetoothAvailable: Boolean,
    val audioAvailable: Boolean,
    val cameraCount: Int
)

object SystemInfoManager {

    fun getHardwareInfo() = DeviceHardwareInfo()

    fun getCpuUsage(): Float {
        return try {
            val first = readCpuTimes()
            Thread.sleep(360)
            val second = readCpuTimes()

            val idleDelta = second.idle - first.idle
            val totalDelta = second.total - first.total
            if (totalDelta <= 0L) 0f
            else (((totalDelta - idleDelta).toFloat() / totalDelta.toFloat()) * 100f).coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }

    private data class CpuTimes(val idle: Long, val total: Long)

    private fun readCpuTimes(): CpuTimes {
        RandomAccessFile("/proc/stat", "r").use { reader ->
            val tokens = reader.readLine()
                .trim()
                .split(Regex("\\s+"))
            val values = tokens.drop(1).mapNotNull { it.toLongOrNull() }
            val idle = values.getOrElse(3) { 0L } + values.getOrElse(4) { 0L }
            val total = values.sum()
            return CpuTimes(idle = idle, total = total)
        }
    }

    fun getBatteryInfo(context: Context): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = (level / scale.toFloat() * 100).toInt()
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        
        val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val health = when(healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            else -> "Unknown"
        }

        val temp = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val tech = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        return BatteryInfo(percentage, isCharging, health, temp, tech)
    }

    fun getRamInfo(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return RamInfo(memoryInfo.totalMem, memoryInfo.availMem, memoryInfo.threshold)
    }

    fun getStorageInfo(): StorageInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        return StorageInfo(totalBlocks * blockSize, availableBlocks * blockSize)
    }

    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.map {
            AppInfo(
                name = it.loadLabel(pm).toString(),
                packageName = it.packageName,
                version = try { pm.getPackageInfo(it.packageName, 0).versionName ?: "N/A" } catch (e: Exception) { "N/A" },
                isSystemApp = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }.sortedBy { it.name }
    }

    fun getSensors(context: Context): List<String> {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sm.getSensorList(Sensor.TYPE_ALL).map { it.name }
    }

    fun getCameraSpecs(context: Context): List<String> {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            cm.cameraIdList.map { id ->
                val chars = cm.getCameraCharacteristics(id)
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                "Camera ID: $id (${if(facing == 0) "Front" else "Back"})"
            }
        } catch (e: Exception) {
            listOf("No cameras detected")
        }
    }

    @Suppress("DEPRECATION")
    fun getWifiConnectionStatus(context: Context): WifiConnectionStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val networkType = when {
            capabilities == null -> "Disconnected"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo?.ssid?.replace("\"", "").orEmpty().ifBlank { "Unknown" }
        val ip = if (wifiInfo != null && wifiInfo.ipAddress != 0) Formatter.formatIpAddress(wifiInfo.ipAddress) else "N/A"

        return WifiConnectionStatus(
            connected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            ssid = ssid,
            ipAddress = ip,
            networkType = networkType
        )
    }

    fun getDeviceCapabilities(context: Context): DeviceCapabilities {
        val pm = context.packageManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ram = getRamInfo(context)
        val processor = listOf(
            Build.SOC_MODEL,
            Build.HARDWARE,
            Build.BOARD
        ).firstOrNull { !it.isNullOrBlank() && it != Build.UNKNOWN } ?: "Unknown Processor"
        val gpu = System.getProperty("ro.hardware.egl")
            ?: System.getProperty("ro.opengles.version")
            ?: "GPU name not exposed on this device"
        val cameraCount = try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cm.cameraIdList.size
        } catch (e: Exception) {
            0
        }
        return DeviceCapabilities(
            processorName = processor,
            gpuName = gpu,
            memoryTotal = ram.total,
            bluetoothAvailable = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            audioAvailable = audioManager.mode >= 0,
            cameraCount = cameraCount
        )
    }
}
