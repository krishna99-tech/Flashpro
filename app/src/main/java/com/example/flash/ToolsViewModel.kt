package com.example.flash

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress

class ToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val _ipInfo = MutableStateFlow<IpInfo?>(null)
    val ipInfo: StateFlow<IpInfo?> = _ipInfo

    private val _pingResult = MutableStateFlow<PingLog?>(null)
    val pingResult: StateFlow<PingLog?> = _pingResult

    private val _portScanResults = MutableStateFlow<List<PortInfo>>(emptyList())
    val portScanResults: StateFlow<List<PortInfo>> = _portScanResults

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _dnsResults = MutableStateFlow<List<DnsResult>>(emptyList())
    val dnsResults: StateFlow<List<DnsResult>> = _dnsResults

    private val _activeDnsServers = MutableStateFlow<List<String>>(emptyList())
    val activeDnsServers: StateFlow<List<String>> = _activeDnsServers

    private val _tracerouteResults = MutableStateFlow<List<TracerouteStep>>(emptyList())
    val tracerouteResults: StateFlow<List<TracerouteStep>> = _tracerouteResults

    private val _isTracerouteRunning = MutableStateFlow(false)
    val isTracerouteRunning: StateFlow<Boolean> = _isTracerouteRunning

    private val _speedTestMetrics = MutableStateFlow(SpeedTestMetrics())
    val speedTestMetrics: StateFlow<SpeedTestMetrics> = _speedTestMetrics

    private val _isSpeedTestRunning = MutableStateFlow(false)
    val isSpeedTestRunning: StateFlow<Boolean> = _isSpeedTestRunning

    // System Info States
    private val _cpuUsage = MutableStateFlow(0f)
    val cpuUsage: StateFlow<Float> = _cpuUsage
    private val _cpuHistory = MutableStateFlow<List<Float>>(emptyList())
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo

    private val _ramInfo = MutableStateFlow<RamInfo?>(null)
    val ramInfo: StateFlow<RamInfo?> = _ramInfo

    private val _storageInfo = MutableStateFlow<StorageInfo?>(null)
    val storageInfo: StateFlow<StorageInfo?> = _storageInfo

    private val _wifiStatus = MutableStateFlow<WifiConnectionStatus?>(null)
    val wifiStatus: StateFlow<WifiConnectionStatus?> = _wifiStatus

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    private val _sensors = MutableStateFlow<List<String>>(emptyList())
    val sensors: StateFlow<List<String>> = _sensors

    private val _cameras = MutableStateFlow<List<String>>(emptyList())
    val cameras: StateFlow<List<String>> = _cameras
    private val _deviceCapabilities = MutableStateFlow<DeviceCapabilities?>(null)
    val deviceCapabilities: StateFlow<DeviceCapabilities?> = _deviceCapabilities

    private var systemMonitorJob: Job? = null

    fun startSystemMonitoring(context: Context) {
        if (systemMonitorJob?.isActive == true) return
        systemMonitorJob = viewModelScope.launch {
            while (isActive) {
                // Offload blocking operations to IO dispatcher to prevent UI freezing
                val cpu = withContext(Dispatchers.IO) { SystemInfoManager.getCpuUsage() }
                val battery = withContext(Dispatchers.IO) { SystemInfoManager.getBatteryInfo(context) }
                val ram = withContext(Dispatchers.IO) { SystemInfoManager.getRamInfo(context) }
                val storage = withContext(Dispatchers.IO) { SystemInfoManager.getStorageInfo() }
                val wifi = withContext(Dispatchers.IO) { SystemInfoManager.getWifiConnectionStatus(context) }
                
                _cpuUsage.value = cpu
                _cpuHistory.value = (_cpuHistory.value + cpu).takeLast(30)
                _batteryInfo.value = battery
                _ramInfo.value = ram
                _storageInfo.value = storage
                _wifiStatus.value = wifi
                
                delay(3000)
            }
        }
    }

    fun fetchStaticSystemInfo(context: Context) {
        viewModelScope.launch {
            _installedApps.value = withContext(Dispatchers.IO) { SystemInfoManager.getInstalledApps(context) }
            _sensors.value = withContext(Dispatchers.IO) { SystemInfoManager.getSensors(context) }
            _cameras.value = withContext(Dispatchers.IO) { SystemInfoManager.getCameraSpecs(context) }
            _deviceCapabilities.value = withContext(Dispatchers.IO) { SystemInfoManager.getDeviceCapabilities(context) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        systemMonitorJob?.cancel()
    }

    fun fetchIpInfo(context: Context) {
        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) { RetrofitClient.instance.getIpInfo() }
                _ipInfo.value = info
            } catch (e: Exception) {
                _ipInfo.value = IpInfo(status = "fail")
            }
            _activeDnsServers.value = withContext(Dispatchers.IO) { NetworkToolsManager.getActiveDnsServers(context) }
        }
    }

    fun runPing(host: String) {
        viewModelScope.launch {
            _pingResult.value = withContext(Dispatchers.IO) { NetworkToolsManager.ping(host) }
        }
    }

    fun runPortScan(host: String, startPort: Int, endPort: Int) {
        viewModelScope.launch {
            _isScanning.value = true
            _portScanResults.value = emptyList()
            val results = mutableListOf<PortInfo>()
            withContext(Dispatchers.IO) {
                for (port in startPort..endPort) {
                    if (!isActive) break
                    val info = NetworkToolsManager.checkPort(host, port)
                    if (info.isOpen) {
                        results.add(info)
                        _portScanResults.value = results.toList()
                    }
                }
            }
            _isScanning.value = false
        }
    }

    fun runDnsLookup(domain: String) {
        viewModelScope.launch {
            _dnsResults.value = withContext(Dispatchers.IO) { NetworkToolsManager.dnsLookup(domain) }
        }
    }

    fun runTraceroute(host: String) {
        viewModelScope.launch {
            _isTracerouteRunning.value = true
            val steps = mutableListOf<TracerouteStep>()
            _tracerouteResults.value = emptyList()
            withContext(Dispatchers.IO) {
                NetworkToolsManager.traceroute(host) { step ->
                    steps.add(step)
                    _tracerouteResults.value = steps.toList()
                }
            }
            _isTracerouteRunning.value = false
        }
    }

    fun runSpeedTest() {
        viewModelScope.launch {
            _isSpeedTestRunning.value = true
            _speedTestMetrics.value = SpeedTestMetrics()
            withContext(Dispatchers.IO) {
                NetworkToolsManager.runSpeedTest { metrics ->
                    _speedTestMetrics.value = metrics
                }
            }
            _isSpeedTestRunning.value = false
        }
    }

    fun sendWakeOnLan(mac: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { NetworkToolsManager.wakeOnLan(mac) }
        }
    }
}
