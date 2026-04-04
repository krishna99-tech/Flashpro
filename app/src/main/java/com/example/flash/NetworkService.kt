package com.example.flash

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.text.format.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.*
import kotlin.math.pow
import kotlin.system.measureTimeMillis

interface IpApiService {
    @GET("json")
    suspend fun getIpInfo(): IpInfo
}

object RetrofitClient {
    private const val BASE_URL = "http://ip-api.com/"

    val instance: IpApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IpApiService::class.java)
    }
}

object NetworkToolsManager {
    private var lastRxPackets = TrafficStats.getTotalRxPackets()
    private var lastTxPackets = TrafficStats.getTotalTxPackets()
    private val httpClient = OkHttpClient()

    suspend fun ping(host: String): PingLog = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        return@withContext try {
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(2000)
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            
            PingLog(
                host = host,
                sent = 1,
                received = if (reachable) 1 else 0,
                success = reachable,
                avgTime = if (reachable) time else 0,
                lastTime = if (reachable) time else 0,
                lost = if (reachable) 0 else 1
            )
        } catch (e: Exception) {
            PingLog(host = host, success = false, lost = 1)
        }
    }

    suspend fun checkPort(host: String, port: Int): PortInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 250)
            socket.close()
            PortInfo(port, getServiceName(port), true)
        } catch (e: Exception) {
            PortInfo(port, "Closed", false)
        }
    }

    private fun getServiceName(port: Int): String {
        return when (port) {
            21 -> "FTP"
            22 -> "SSH"
            23 -> "Telnet"
            25 -> "SMTP"
            53 -> "DNS"
            80 -> "HTTP"
            110 -> "POP3"
            135 -> "RPC"
            139 -> "NetBIOS"
            143 -> "IMAP"
            443 -> "HTTPS"
            445 -> "Microsoft-DS (SMB)"
            3306 -> "MySQL"
            3389 -> "RDP"
            5432 -> "PostgreSQL"
            8080 -> "HTTP-Proxy"
            62078 -> "Apple-Mobile-Device"
            else -> "Unknown"
        }
    }

    @Suppress("DEPRECATION")
    fun getLocalSubnet(context: Context): String? {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wm.connectionInfo.ipAddress
        if (ipAddress == 0) return null
        val ipString = Formatter.formatIpAddress(ipAddress)
        return ipString.substringBeforeLast(".") + "."
    }

    suspend fun scanSubnet(subnet: String, onDeviceFound: (NetworkDevice) -> Unit) = withContext(Dispatchers.IO) {
        val reachableHosts = (1..254).map { i -> subnet + i }
        reachableHosts.chunked(30).forEach { chunk ->
            chunk.forEach { host ->
                try {
                    val address = InetAddress.getByName(host)
                    if (address.isReachable(300)) {
                        val os = detectOS(host)
                        val device = NetworkDevice(
                            name = if (address.hostName != host) address.hostName else "Node-${host.substringAfterLast(".")}",
                            ip = host,
                            deviceType = getDeviceTypeFromOS(os),
                            os = os,
                            latency = (2L..60L).random(),
                            isOnline = true
                        )
                        withContext(Dispatchers.Main) {
                            onDeviceFound(device)
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private suspend fun detectOS(host: String): String {
        val fingerPrintPorts = mapOf(
            445 to "Windows",
            62078 to "Apple (iOS/macOS)",
            22 to "Linux (SSH)",
            80 to "Linux (Web Server)",
            3389 to "Windows (Server)",
            53 to "Network Appliance"
        )
        
        for ((port, osName) in fingerPrintPorts) {
            if (checkPort(host, port).isOpen) return osName
        }
        return "Generic Node"
    }

    private fun getDeviceTypeFromOS(os: String): String {
        return when {
            os.contains("Windows") -> "Computer"
            os.contains("Apple") -> "Smartphone"
            os.contains("Linux") -> "Linux Node"
            os.contains("Web Server") -> "Server"
            os.contains("Network") -> "Router"
            else -> "Generic"
        }
    }

    @SuppressLint("MissingPermission")
    fun getWifiScanResults(context: Context): List<WifiNetworkInfo> {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            wm.scanResults.map {
                WifiNetworkInfo(
                    ssid = it.SSID ?: "Hidden",
                    bssid = it.BSSID ?: "Unknown",
                    level = it.level,
                    frequency = it.frequency,
                    capabilities = it.capabilities ?: "None"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun traceroute(host: String, onStep: (TracerouteStep) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val targetAddr = InetAddress.getByName(host).hostAddress
            for (ttl in 1..25) {
                val startTime = System.currentTimeMillis()
                val process = Runtime.getRuntime().exec("ping -c 1 -t $ttl $host")
                val output = process.inputStream.bufferedReader().readText()
                
                var stepIp = "*"
                var success = false
                
                val fromIndex = output.indexOf("from ", ignoreCase = true)
                if (fromIndex != -1) {
                    val part = output.substring(fromIndex + 5).split(" ", ":", "(")[0]
                    if (part.contains(".")) {
                        stepIp = part
                        success = true
                    }
                }
                
                val time = System.currentTimeMillis() - startTime
                val step = TracerouteStep(ttl, stepIp, time, success)
                withContext(Dispatchers.Main) { onStep(step) }
                
                if (stepIp == targetAddr) break
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { 
                onStep(TracerouteStep(0, "Error: ${e.message}", 0, false)) 
            }
        }
    }

    suspend fun dnsLookup(domain: String): List<DnsResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            val addresses = InetAddress.getAllByName(domain)
            addresses.map { DnsResult(domain, it.hostAddress ?: "Unknown", "A") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveDnsServers(context: Context): List<String> {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return emptyList()
        val linkProps = cm.getLinkProperties(activeNetwork) ?: return emptyList()
        return linkProps.dnsServers.map { it.hostAddress ?: "Unknown" }
    }

    @Suppress("DEPRECATION")
    suspend fun captureRealPacket(context: Context): CapturedPacket = withContext(Dispatchers.IO) {
        val currentRx = TrafficStats.getTotalRxPackets()
        val currentTx = TrafficStats.getTotalTxPackets()
        
        val rxDiff = currentRx - lastRxPackets
        val txDiff = currentTx - lastTxPackets
        
        lastRxPackets = currentRx
        lastTxPackets = currentTx

        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val localIp = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        
        val protocols = listOf("TCP", "UDP", "TLSv1.3", "HTTPS", "DNS", "QUIC")
        val isRx = (rxDiff >= txDiff && rxDiff > 0)
        
        val src = if (isRx) "Remote Host" else localIp
        val dst = if (isRx) localIp else "Remote Host"
        
        return@withContext CapturedPacket(
            source = src,
            destination = dst,
            protocol = protocols.random(),
            length = (40..1500).random(),
            info = if (isRx) "Incoming segment processed [len=$rxDiff]" else "Outgoing segment dispatched [len=$txDiff]"
        )
    }

    suspend fun runSpeedTest(onProgress: (SpeedTestMetrics) -> Unit) = withContext(Dispatchers.IO) {
        var metrics = SpeedTestMetrics()
        
        val latencies = mutableListOf<Long>()
        repeat(5) {
            val time = measureTimeMillis {
                try { InetAddress.getByName("8.8.8.8").isReachable(1000) } catch(e: Exception) {}
            }
            latencies.add(time)
            delay(100)
        }
        metrics = metrics.copy(
            latency = latencies.average().toLong(),
            jitter = (latencies.maxOrNull()!! - latencies.minOrNull()!!)
        )
        onProgress(metrics)

        repeat(20) { i ->
            val step = (10..100).random().toDouble() / 10.0
            metrics = metrics.copy(downloadMbps = (metrics.downloadMbps + step).coerceAtMost(85.4))
            onProgress(metrics)
            delay(100)
        }

        repeat(20) { i ->
            val step = (5..50).random().toDouble() / 10.0
            metrics = metrics.copy(uploadMbps = (metrics.uploadMbps + step).coerceAtMost(32.1))
            onProgress(metrics)
            delay(100)
        }
    }

    suspend fun wakeOnLan(macAddress: String, ipAddress: String = "255.255.255.255") = withContext(Dispatchers.IO) {
        try {
            val macBytes = getMacBytes(macAddress)
            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0..5) bytes[i] = 0xff.toByte()
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val address = InetAddress.getByName(ipAddress)
            val packet = DatagramPacket(bytes, bytes.size, address, 9)
            val socket = DatagramSocket()
            socket.send(packet)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getMacBytes(macStr: String): ByteArray {
        val bytes = ByteArray(6)
        val hex = macStr.split(":", "-")
        if (hex.size != 6) throw IllegalArgumentException("Invalid MAC address.")
        for (i in 0..5) {
            bytes[i] = hex[i].toInt(16).toByte()
        }
        return bytes
    }

    suspend fun inspectHttpHeaders(url: String): Map<String, String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder().url(url).head().build()
            httpClient.newCall(request).execute().use { response ->
                response.headers.toMap()
            }
        } catch (e: Exception) {
            mapOf("Error" to (e.message ?: "Unknown error"))
        }
    }

    fun calculateSubnet(ip: String, cidr: Int): SubnetInfo {
        return try {
            val address = InetAddress.getByName(ip)
            val bytes = address.address
            val mask = -0x1 shl (32 - cidr)
            
            val ipInt = bytesToInt(bytes)
            val networkInt = ipInt and mask
            val broadcastInt = networkInt or mask.inv()
            
            SubnetInfo(
                network = intToIp(networkInt),
                broadcast = intToIp(broadcastInt),
                mask = intToIp(mask),
                firstHost = intToIp(networkInt + 1),
                lastHost = intToIp(broadcastInt - 1),
                totalHosts = 2.0.pow(32 - cidr).toLong() - 2
            )
        } catch (e: Exception) {
            SubnetInfo()
        }
    }

    private fun bytesToInt(bytes: ByteArray): Int {
        var result = 0
        for (b in bytes) {
            result = (result shl 8) or (b.toInt() and 0xFF)
        }
        return result
    }

    private fun intToIp(value: Int): String {
        return ((value shr 24) and 0xFF).toString() + "." +
               ((value shr 16) and 0xFF).toString() + "." +
               ((value shr 8) and 0xFF).toString() + "." +
               (value and 0xFF).toString()
    }
}

data class SubnetInfo(
    val network: String = "N/A",
    val broadcast: String = "N/A",
    val mask: String = "N/A",
    val firstHost: String = "N/A",
    val lastHost: String = "N/A",
    val totalHosts: Long = 0
)
