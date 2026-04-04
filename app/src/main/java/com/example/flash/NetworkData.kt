package com.example.flash

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

data class IpInfo(
    @SerializedName("query") val ip: String = "N/A",
    @SerializedName("isp") val isp: String = "N/A",
    @SerializedName("org") val org: String = "N/A",
    @SerializedName("as") val asName: String = "N/A",
    @SerializedName("city") val city: String = "N/A",
    @SerializedName("country") val country: String = "N/A",
    @SerializedName("status") val status: String = "fail"
)

data class NetworkDevice(
    val ip: String,
    val mac: String = "02:00:00:00:00:00",
    val vendor: String = "Unknown Vendor",
    val hostname: String = "Unknown Host",
    val deviceType: String = "Generic",
    val os: String = "Unknown",
    val latency: Long = -1,
    val isOnline: Boolean = true,
    val isNew: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val name: String = "Unknown Device"
)

data class PingLog(
    val host: String, 
    val sent: Int = 0,
    val received: Int = 0,
    val lost: Int = 0,
    val minTime: Long = 0,
    val maxTime: Long = 0,
    val avgTime: Long = 0,
    val lastTime: Long = 0,
    val ttl: Int = -1,
    val success: Boolean = false
)

data class PortInfo(
    val port: Int,
    val service: String,
    val isOpen: Boolean,
    val banner: String = ""
)

data class TracerouteStep(
    val hop: Int,
    val ip: String,
    val time: Long,
    val success: Boolean
)

data class DnsResult(
    val domain: String,
    val ip: String,
    val type: String = "A"
)

data class WifiNetworkInfo(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String
)

data class SpeedTestMetrics(
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val latency: Long = 0,
    val jitter: Long = 0,
    val packetLoss: Double = 0.0
)

data class CapturedPacket(
    val timestamp: Long = System.currentTimeMillis(),
    val source: String,
    val destination: String,
    val protocol: String,
    val length: Int,
    val info: String
)

// --- SSH MODULAR MODELS ---

data class RemoteServer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val user: String,
    val password: String,
    val port: Int = 22
)

data class CommandPreset(
    val name: String,
    val command: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class RemoteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val permissions: String
)

data class ServerStats(
    val cpuUsage: Float = 0f,
    val ramUsed: Float = 0f,
    val ramTotal: Float = 0f,
    val diskUsed: Float = 0f,
    val diskTotal: Float = 0f,
    val uptime: String = "N/A"
)

object DeviceDiscoveryManager {
    val knownDevices = mutableStateListOf<NetworkDevice>()
    val discoveryLog = mutableStateListOf<String>()
    val capturedPackets = mutableStateListOf<CapturedPacket>()
    var networkHealthScore = mutableIntStateOf(100)
    var isScanning = mutableStateOf(false)
    
    fun updateNetworkHealth(score: Int) {
        networkHealthScore.intValue = score
    }

    fun startScan(context: Context) {
        val subnet = NetworkToolsManager.getLocalSubnet(context) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            isScanning.value = true
            synchronized(knownDevices) {
                knownDevices.clear()
            }
            NetworkToolsManager.scanSubnet(subnet) { device ->
                addIfNew(device)
            }
            isScanning.value = false
        }
    }

    fun addIfNew(device: NetworkDevice): Boolean {
        synchronized(knownDevices) {
            val exists = knownDevices.any { it.ip == device.ip }
            if (!exists) {
                knownDevices.add(device)
                addToHistory(device)
                return true
            }
            return false
        }
    }
    
    fun addToHistory(device: NetworkDevice) {
        synchronized(discoveryLog) {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            discoveryLog.add(0, "[$timestamp] Detected ${device.ip} (${device.os})")
            if (discoveryLog.size > 50) discoveryLog.removeAt(50)
        }
    }

    fun addPacket(packet: CapturedPacket) {
        synchronized(capturedPackets) {
            capturedPackets.add(0, packet)
            if (capturedPackets.size > 100) capturedPackets.removeAt(100)
        }
    }
}
