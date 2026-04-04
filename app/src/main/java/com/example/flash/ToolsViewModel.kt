package com.example.flash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class ToolsViewModel : ViewModel() {
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

    fun fetchIpInfo(context: Context) {
        viewModelScope.launch {
            try {
                val info = RetrofitClient.instance.getIpInfo()
                _ipInfo.value = info
            } catch (e: Exception) {
                _ipInfo.value = IpInfo(status = "fail")
            }
            _activeDnsServers.value = NetworkToolsManager.getActiveDnsServers(context)
        }
    }

    fun runPing(host: String) {
        viewModelScope.launch {
            _pingResult.value = NetworkToolsManager.ping(host)
        }
    }

    fun runPortScan(host: String, startPort: Int, endPort: Int) {
        viewModelScope.launch {
            _isScanning.value = true
            _portScanResults.value = emptyList()
            val results = mutableListOf<PortInfo>()
            for (port in startPort..endPort) {
                val info = NetworkToolsManager.checkPort(host, port)
                if (info.isOpen) {
                    results.add(info)
                    _portScanResults.value = results.toList()
                }
            }
            _isScanning.value = false
        }
    }

    fun runDnsLookup(domain: String) {
        viewModelScope.launch {
            _dnsResults.value = NetworkToolsManager.dnsLookup(domain)
        }
    }

    fun runTraceroute(host: String) {
        viewModelScope.launch {
            _isTracerouteRunning.value = true
            val steps = mutableListOf<TracerouteStep>()
            _tracerouteResults.value = emptyList()
            NetworkToolsManager.traceroute(host) { step ->
                steps.add(step)
                _tracerouteResults.value = steps.toList()
            }
            _isTracerouteRunning.value = false
        }
    }

    fun runSpeedTest() {
        viewModelScope.launch {
            _isSpeedTestRunning.value = true
            _speedTestMetrics.value = SpeedTestMetrics()
            NetworkToolsManager.runSpeedTest { metrics ->
                _speedTestMetrics.value = metrics
            }
            _isSpeedTestRunning.value = false
        }
    }

    fun sendWakeOnLan(mac: String) {
        viewModelScope.launch {
            NetworkToolsManager.wakeOnLan(mac)
        }
    }
}
