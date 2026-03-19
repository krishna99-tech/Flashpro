package com.example.flash

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.annotations.SerializedName

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
    val latency: Long = -1,
    val isOnline: Boolean = true,
    val isNew: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
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

data class SpeedTestMetrics(
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val latency: Long = 0,
    val jitter: Long = 0,
    val packetLoss: Double = 0.0
)

// --- SSH MODULAR MODELS ---

data class RemoteServer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val user: String,
    val password: String,
    val port: Int = 443
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
    var networkHealthScore = mutableIntStateOf(100)
    
    fun updateNetworkHealth(score: Int) {
        networkHealthScore.intValue = score
    }

    // Thread-safe additions for SnapshotStateLists
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
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            discoveryLog.add(0, "[$timestamp] Detected ${device.ip} (${device.vendor})")
            if (discoveryLog.size > 50) discoveryLog.removeAt(50)
        }
    }
}
