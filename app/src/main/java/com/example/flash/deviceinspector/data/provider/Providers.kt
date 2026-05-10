package com.example.flash.deviceinspector.data.provider

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaCodecList
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.flash.deviceinspector.domain.model.*
import java.io.File
import java.net.NetworkInterface
import java.text.DateFormat
import java.io.RandomAccessFile

class DeviceInfoProvider {
    fun get(context: Context): List<InfoSection> {
        val buildTime = DateFormat.getDateTimeInstance().format(Build.TIME)
        return listOf(
            InfoSection(
                "Device Identity",
                Icons.Default.PhoneAndroid,
                listOf(
                    info("Device Name", Settings.Global.getString(context.contentResolver, "device_name") ?: "Unavailable", Icons.Default.PhoneAndroid),
                    info("Manufacturer", Build.MANUFACTURER, Icons.Default.Business),
                    info("Brand", Build.BRAND, Icons.Default.Label),
                    info("Model", Build.MODEL, Icons.Default.Devices),
                    info("Product", Build.PRODUCT, Icons.Default.Inventory2),
                    info("Hardware", Build.HARDWARE, Icons.Default.Memory),
                    info("Board", Build.BOARD, Icons.Default.DeveloperBoard),
                    info("Device", Build.DEVICE, Icons.Default.Code),
                    info("Bootloader", Build.BOOTLOADER, Icons.Default.SystemUpdate),
                    info("Fingerprint", Build.FINGERPRINT, Icons.Default.Fingerprint),
                    info("Host", Build.HOST, Icons.Default.Dns),
                    info("Build ID", Build.ID, Icons.Default.Badge),
                    info("Build Type", Build.TYPE, Icons.Default.Build),
                    info("Build Tags", Build.TAGS, Icons.Default.LocalOffer),
                    info("Build Time", buildTime, Icons.Default.Schedule)
                )
            ),
            InfoSection(
                "Operating System",
                Icons.Default.Android,
                listOf(
                    info("Android Version", Build.VERSION.RELEASE, Icons.Default.Android),
                    info("API Level", Build.VERSION.SDK_INT.toString(), Icons.Default.Api),
                    info("Security Patch", Build.VERSION.SECURITY_PATCH ?: "Unavailable", Icons.Default.Security),
                    info("Codename", Build.VERSION.CODENAME, Icons.Default.MilitaryTech),
                    info("Kernel", readFile("/proc/version"), Icons.Default.Terminal),
                    info("ABI List", Build.SUPPORTED_ABIS.joinToString(), Icons.Default.List),
                    info("VM Version", System.getProperty("java.vm.version") ?: "Unavailable", Icons.Default.Coffee),
                    info("VM Heap", Runtime.getRuntime().maxMemory().toString(), Icons.Default.Storage)
                )
            )
        )
    }
}

class DisplayInfoProvider {
    fun get(context: Context): List<InfoSection> {
        val dm = context.resources.displayMetrics
        val inches = kotlin.math.sqrt((dm.widthPixels / dm.xdpi) * (dm.widthPixels / dm.xdpi) + (dm.heightPixels / dm.ydpi) * (dm.heightPixels / dm.ydpi))
        return listOf(
            InfoSection("Display", Icons.Default.Monitor, listOf(
                info("Resolution", "${dm.widthPixels} x ${dm.heightPixels}", Icons.Default.AspectRatio),
                info("DPI", dm.densityDpi.toString(), Icons.Default.DensityMedium),
                info("Density", dm.density.toString(), Icons.Default.Straighten),
                info("Physical Size", String.format("%.2f in", inches), Icons.Default.Monitor),
                info("Font Scale", context.resources.configuration.fontScale.toString(), Icons.Default.TextFields)
            ))
        )
    }
}

class CpuInfoProvider {
    fun get(): List<InfoSection> {
        val usage = currentCpuUsagePercent()
        val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch").orEmpty().ifBlank { "Unavailable" }
        val cores = Runtime.getRuntime().availableProcessors()
        val model = readCpuInfoField("model name")
            ?: readCpuInfoField("Processor")
            ?: readCpuInfoField("Hardware")
            ?: Build.HARDWARE
        return listOf(
            InfoSection(
                "CPU",
                Icons.Default.Memory,
                listOf(
                    info("CPU Usage", String.format("%.1f%%", usage), Icons.Default.Speed),
                    info("Architecture", architecture, Icons.Default.Memory),
                    info("Cores", cores.toString(), Icons.Default.Grain),
                    info("Model", model, Icons.Default.DeveloperBoard)
                )
            )
        )
    }

    private fun currentCpuUsagePercent(): Float {
        return runCatching {
            val first = readCpuTimes()
            Thread.sleep(240)
            val second = readCpuTimes()
            val idleDelta = second.idle - first.idle
            val totalDelta = second.total - first.total
            if (totalDelta <= 0L) 0f else (((totalDelta - idleDelta).toFloat() / totalDelta.toFloat()) * 100f).coerceIn(0f, 100f)
        }.getOrDefault(0f)
    }

    private data class CpuTimes(val idle: Long, val total: Long)

    private fun readCpuTimes(): CpuTimes {
        RandomAccessFile("/proc/stat", "r").use { reader ->
            val tokens = reader.readLine().trim().split(Regex("\\s+"))
            val values = tokens.drop(1).mapNotNull { it.toLongOrNull() }
            val idle = values.getOrElse(3) { 0L } + values.getOrElse(4) { 0L }
            return CpuTimes(idle = idle, total = values.sum())
        }
    }

    private fun readCpuInfoField(key: String): String? {
        return runCatching {
            File("/proc/cpuinfo")
                .readLines()
                .firstOrNull { it.startsWith("$key", ignoreCase = true) }
                ?.substringAfter(":")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

class NetworkInfoProvider {
    fun get(context: Context): List<InfoSection> {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val wifiInfo = wifi.connectionInfo
        val ipV = NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }.map { it.hostAddress ?: "" }
        return listOf(
            InfoSection("Network", Icons.Default.Wifi, listOf(
                info("WiFi Enabled", wifi.isWifiEnabled.toString(), Icons.Default.Wifi),
                info("SSID", wifiInfo?.ssid ?: "Unavailable", Icons.Default.WifiFind),
                info("BSSID", wifiInfo?.bssid ?: "Unavailable", Icons.Default.Router),
                info("Frequency", (wifiInfo?.frequency ?: 0).toString(), Icons.Default.SignalCellularAlt),
                info("Link Speed", "${wifiInfo?.linkSpeed ?: 0} Mbps", Icons.Default.Speed),
                info("VPN Active", (caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true).toString(), Icons.Default.VpnLock),
                info("Metered", cm.isActiveNetworkMetered.toString(), Icons.Default.DataUsage),
                info("IP Addresses", ipV.joinToString(), Icons.Default.Dns)
            ))
        )
    }
}

class WifiScanProvider {
    fun scan(context: Context): List<NetworkScanResult> {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return runCatching { wifi.scanResults }.getOrNull().orEmpty().map {
            NetworkScanResult(
                ssid = it.SSID ?: "Hidden",
                bssid = it.BSSID ?: "Unavailable",
                rssi = it.level,
                frequency = it.frequency,
                security = it.capabilities ?: "Open",
                standard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.wifiStandard.toString() else "Legacy",
                channelWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) it.channelWidth.toString() else "N/A"
            )
        }.sortedByDescending { it.rssi }
    }

    fun bondedBluetooth(): List<String> = BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.map { "${it.name} (${it.address})" } ?: emptyList()
}

class SensorInfoProvider {
    fun get(context: Context): List<SensorInfo> {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sm.getSensorList(Sensor.TYPE_ALL).map { s ->
            SensorInfo(s.name, s.type.toString(), s.vendor, s.version, s.resolution, s.maximumRange, s.power, s.minDelay, s.maxDelay, s.isWakeUpSensor, null, sensorIcon(s.type))
        }
    }

    private fun sensorIcon(type: Int): ImageVector = when (type) {
        Sensor.TYPE_ACCELEROMETER -> Icons.Default.Moving
        Sensor.TYPE_GYROSCOPE -> Icons.Default.RotateRight
        Sensor.TYPE_MAGNETIC_FIELD -> Icons.Default.Explore
        Sensor.TYPE_PROXIMITY -> Icons.Default.Sensors
        Sensor.TYPE_LIGHT -> Icons.Default.LightMode
        Sensor.TYPE_PRESSURE -> Icons.Default.Compress
        else -> Icons.Default.Sensors
    }
}

class CameraInfoProvider {
    fun get(context: Context): List<CameraInfo> {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return cm.cameraIdList.map { id ->
            val c = cm.getCameraCharacteristics(id)
            val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(android.graphics.ImageFormat.JPEG)?.toList().orEmpty()
            val max = sizes.maxByOrNull { it.width * it.height }
            val mp = if (max == null) "N/A" else String.format("%.1f MP", (max.width * max.height) / 1_000_000f)
            CameraInfo(
                id = id,
                facing = when (c.get(CameraCharacteristics.LENS_FACING)) { CameraCharacteristics.LENS_FACING_FRONT -> "Front"; CameraCharacteristics.LENS_FACING_BACK -> "Back"; else -> "External" },
                maxMp = mp,
                focalLengths = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.joinToString() ?: "N/A",
                apertures = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.joinToString() ?: "N/A",
                hasOis = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)?.isNotEmpty() == true,
                hasFlash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true,
                hardwareLevel = (c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ?: -1).toString(),
                maxVideoResolution = max?.let { "${it.width}x${it.height}" } ?: "N/A",
                supportsRaw = c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true,
                supportsHdr = c.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)?.contains(CameraCharacteristics.CONTROL_SCENE_MODE_HDR) == true
            )
        }
    }
}

class AudioInfoProvider {
    fun get(context: Context): List<InfoSection> {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).joinToString { deviceType(it.type) }
        val inputs = am.getDevices(AudioManager.GET_DEVICES_INPUTS).joinToString { deviceType(it.type) }
        return listOf(InfoSection("Audio", Icons.Default.VolumeUp, listOf(
            info("Outputs", outputs.ifBlank { "None" }, Icons.Default.Speaker),
            info("Inputs", inputs.ifBlank { "None" }, Icons.Default.Mic),
            info("Sample Rate", am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) ?: "N/A", Icons.Default.GraphicEq),
            info("Frames/Buffer", am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) ?: "N/A", Icons.Default.Equalizer)
        )))
    }

    private fun deviceType(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Speaker"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Headphones"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
        else -> "Type $type"
    }
}

class VideoCodecProvider {
    fun get(): List<InfoSection> {
        val infos = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
        val enc = infos.filter { it.isEncoder }.take(20).joinToString { it.name }
        val dec = infos.filterNot { it.isEncoder }.take(20).joinToString { it.name }
        return listOf(InfoSection("Video Codecs", Icons.Default.Videocam, listOf(
            info("Encoders", enc, Icons.Default.Upload),
            info("Decoders", dec, Icons.Default.Download)
        )))
    }
}

class AppListProvider {
    fun get(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).map { app ->
            val p = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
            val file = File(app.sourceDir ?: "")
            InstalledApp(
                name = app.loadLabel(pm).toString(),
                packageName = app.packageName,
                versionName = p.versionName ?: "N/A",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) p.longVersionCode else p.versionCode.toLong(),
                installDate = p.firstInstallTime,
                lastUpdated = p.lastUpdateTime,
                apkSizeBytes = if (file.exists()) file.length() else 0L,
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                isDebuggable = (app.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
                targetSdk = app.targetSdkVersion,
                minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) app.minSdkVersion else 0,
                icon = null
            )
        }
    }
}

class BatteryInfoProvider {
    fun get(context: Context): List<InfoSection> {
        val i = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return listOf(InfoSection("Battery", Icons.Default.BatteryChargingFull, listOf(
            info("Level", "${i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0}%", Icons.Default.BatteryFull),
            info("Voltage", "${i?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0} mV", Icons.Default.ElectricBolt),
            info("Temperature", "${(i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f} C", Icons.Default.Thermostat),
            info("Technology", i?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "N/A", Icons.Default.Biotech),
            info("Charge Counter", bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toString(), Icons.Default.Numbers)
        )))
    }
}

class HardwareFeaturesProvider {
    fun get(context: Context): List<InfoItem> {
        val pm = context.packageManager
        return pm.systemAvailableFeatures.map {
            InfoItem(it.name ?: "Unnamed Feature", "Available", Icons.Default.Extension, true)
        }.sortedBy { it.label }
    }
}

class TelephonyProvider {
    fun get(context: Context): List<InfoSection> {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        return listOf(InfoSection("Telephony", Icons.Default.SimCard, listOf(
            info("Network Operator", tm.networkOperatorName ?: "N/A", Icons.Default.CellTower),
            info("SIM Operator", tm.simOperatorName ?: "N/A", Icons.Default.SimCard),
            info("Network Country", tm.networkCountryIso ?: "N/A", Icons.Default.Flag),
            info("SIM State", tm.simState.toString(), Icons.Default.SimCardAlert),
            info("Phone Count", tm.phoneCount.toString(), Icons.Default.SimCard)
        )))
    }
}

private fun info(label: String, value: String, icon: ImageVector): InfoItem = InfoItem(label, value.ifBlank { "Unavailable" }, icon, value.isNotBlank())
private fun readFile(path: String): String = runCatching { File(path).readText().trim() }.getOrDefault("Unavailable")
