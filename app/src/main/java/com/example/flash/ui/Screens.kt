package com.example.flash.ui

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flash.DeviceDiscoveryManager
import com.example.flash.NavigationManager
import com.example.flash.Screen
import com.example.flash.SystemInfoManager
import com.example.flash.ToolsViewModel
import kotlinx.coroutines.launch

@Composable
fun MatrixBackground(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val fontSize = 10.sp
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val characters = remember { "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray() }
    var ticker by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            ticker += 1f
            kotlinx.coroutines.delay(30)
        }
    }

    Canvas(modifier = modifier.fillMaxSize().alpha(0.10f)) {
        val cols = (size.width / fontSizePx).toInt()
        val rows = (size.height / fontSizePx).toInt()
        if (cols <= 0 || rows <= 0) return@Canvas

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = primaryColor.toArgb()
                textSize = fontSizePx
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
            }

            for (c in 0 until cols) {
                val speed = ((c * 313) % 7 + 3) * 0.04f
                val startOffset = (c * 197) % rows
                val currentY = (ticker * speed + startOffset) % rows
                val x = c * fontSizePx
                val tailLength = 15
                for (r in 0 until tailLength) {
                    val rowIdx = (currentY.toInt() - r + rows) % rows
                    val y = rowIdx * fontSizePx
                    paint.alpha = if (r == 0) 255 else ((tailLength - r) / tailLength.toFloat() * 180).toInt().coerceIn(0, 255)
                    val charIdx = (c + rowIdx + (ticker / 20).toInt()) % characters.size
                    canvas.nativeCanvas.drawText(characters[charIdx].toString(), x, y, paint)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(navigationManager: NavigationManager, viewModel: ToolsViewModel) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val ipInfo by viewModel.ipInfo.collectAsState()
    val wifiStatus by viewModel.wifiStatus.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        SectionHeader("Flash Dashboard", "Live status and quick tools")
        GlassCard(Modifier.fillMaxWidth().height(180.dp)) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.align(Alignment.BottomStart)) {
                    Text("Live System Core", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Animated telemetry surface", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardMetricTile("CPU", "${cpuUsage.toInt()}%", Icons.Default.Memory, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) {
                navigationManager.navigate(Screen.Hardware)
            }
            DashboardMetricTile("Battery", "${batteryInfo?.percentage ?: 0}%", Icons.Default.BatteryChargingFull, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                navigationManager.navigate(Screen.Battery)
            }
        }

        Spacer(Modifier.height(14.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Network Identity", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                InfoLine(Icons.Default.Language, "ISP", ipInfo?.isp ?: "N/A")
                InfoLine(Icons.Default.Public, "IP", ipInfo?.ip ?: "N/A")
                InfoLine(Icons.Default.Public, "Location", "${ipInfo?.city ?: "N/A"}, ${ipInfo?.country ?: "N/A"}")
                InfoLine(Icons.Default.Wifi, "WiFi", "${wifiStatus?.ssid ?: "N/A"} (${wifiStatus?.networkType ?: "Unknown"})")
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Tools", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernActionCard("Diagnostics", Icons.Default.Dns, MaterialTheme.colorScheme.primary, Modifier.weight(1f).height(120.dp)) {
                navigationManager.navigate(Screen.Diagnostics)
            }
            ModernActionCard("WiFi", Icons.Default.Wifi, MaterialTheme.colorScheme.secondary, Modifier.weight(1f).height(120.dp)) {
                navigationManager.navigate(Screen.WiFiAnalyzer)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernActionCard("Sensors", Icons.Default.Sensors, MaterialTheme.colorScheme.secondary, Modifier.weight(1f).height(120.dp)) {
                navigationManager.navigate(Screen.Sensors)
            }
            ModernActionCard("Installed Apps", Icons.Default.Apps, MaterialTheme.colorScheme.primary, Modifier.weight(1f).height(120.dp)) {
                navigationManager.navigate(Screen.Apps)
            }
        }
    }
}

@Composable
fun NodesScreen() {
    val devices = DeviceDiscoveryManager.knownDevices
    val isScanning by DeviceDiscoveryManager.isScanning
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            SectionHeader("Network Map", "Device discovery")
            Button(
                onClick = { DeviceDiscoveryManager.startScan(context, scope) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isScanning) "Scanning..." else "Start Network Scan", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                val onlineCount = devices.count { it.isOnline }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    StatPill(Icons.Default.Devices, "Discovered", devices.size.toString(), Modifier.weight(1f))
                    StatPill(Icons.Default.Router, "Online", onlineCount.toString(), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(devices) { device ->
                    GlassCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(deviceTypeIcon(device.os, device.deviceType), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(device.ip, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(device.hostname, color = Color.Gray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                            Text(device.os, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticsScreen(viewModel: ToolsViewModel) {
    val pingResult by viewModel.pingResult.collectAsState()
    val dnsResults by viewModel.dnsResults.collectAsState()
    var pingHost by remember { mutableStateOf("8.8.8.8") }
    var dnsDomain by remember { mutableStateOf("google.com") }

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Diagnostics", "ICMP ping and DNS lookup")
        OutlinedTextField(value = pingHost, onValueChange = { pingHost = it }, label = { Text("ICMP Host") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.runPing(pingHost.trim()) }, modifier = Modifier.fillMaxWidth()) { Text("Run Ping") }
        GlassCard(Modifier.fillMaxWidth()) {
            Column {
                InfoLine(Icons.Default.Radar, "Ping Status", if (pingResult?.success == true) "Success" else "Failed")
                InfoLine(Icons.Default.Memory, "Average Time", "${pingResult?.avgTime ?: 0} ms")
                InfoLine(Icons.Default.Dns, "Packets Lost", "${pingResult?.lost ?: 0}")
            }
        }

        OutlinedTextField(value = dnsDomain, onValueChange = { dnsDomain = it }, label = { Text("DNS Domain") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.runDnsLookup(dnsDomain.trim()) }, modifier = Modifier.fillMaxWidth()) { Text("Run DNS Lookup") }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DNS Results", fontWeight = FontWeight.Bold)
                if (dnsResults.isEmpty()) {
                    Text("No results yet")
                } else {
                    dnsResults.take(8).forEach { Text("${it.type}: ${it.ip}") }
                }
            }
        }
    }
}

@Composable
fun InfoScreen(viewModel: ToolsViewModel, navigationManager: NavigationManager) {
    val ipInfo by viewModel.ipInfo.collectAsState()
    val wifiStatus by viewModel.wifiStatus.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("System Info", "OS, model, network and modules")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoLine(Icons.Default.PhoneAndroid, "Model", Build.MODEL)
                InfoLine(Icons.Default.DeveloperBoard, "Manufacturer", Build.MANUFACTURER)
                InfoLine(Icons.Default.Android, "OS", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                InfoLine(Icons.Default.Wifi, "WiFi Connection", if (wifiStatus?.connected == true) "Connected" else "Disconnected")
                InfoLine(Icons.Default.Wifi, "SSID", wifiStatus?.ssid ?: "N/A")
                InfoLine(Icons.Default.Public, "IP", ipInfo?.ip ?: wifiStatus?.ipAddress ?: "N/A")
                InfoLine(Icons.Default.Language, "ISP", ipInfo?.isp ?: "N/A")
                InfoLine(Icons.Default.Public, "Location", "${ipInfo?.city ?: "N/A"}, ${ipInfo?.country ?: "N/A"}")
            }
        }

        ModernActionCard("Open Inspector", Icons.Default.DeveloperBoard, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth().height(80.dp)) {
            navigationManager.navigate(Screen.DeviceInspector)
        }
    }
}

@Composable
fun HardwareScreen(viewModel: ToolsViewModel) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val cpuHistory by viewModel.cpuHistory.collectAsState()
    val ramInfo by viewModel.ramInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val sensors by viewModel.sensors.collectAsState()
    val deviceCapabilities by viewModel.deviceCapabilities.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val hw = remember { SystemInfoManager.getHardwareInfo() }
    val batteryIcon = if ((batteryInfo?.percentage ?: 0) > 20) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Hardware Mapping", "CPU, battery, memory and device modules")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("CPU Usage: ${cpuUsage.toInt()}%")
                }
                CpuUsageGraph(cpuHistory)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCard(
                icon = Icons.Default.Memory,
                title = "CPU",
                value = "${cpuUsage.toInt()}%",
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                icon = Icons.Default.GraphicEq,
                title = "GPU",
                value = deviceCapabilities?.gpuName ?: "Unavailable",
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCard(
                icon = Icons.Default.Storage,
                title = "RAM Total",
                value = formatBytes(ramInfo?.total ?: 0L),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                icon = Icons.Default.Storage,
                title = "RAM Free",
                value = formatBytes(ramInfo?.available ?: 0L),
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCard(
                icon = Icons.Default.SdStorage,
                title = "Storage Total",
                value = formatBytes(storageInfo?.total ?: 0L),
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                icon = Icons.Default.SdStorage,
                title = "Storage Free",
                value = formatBytes(storageInfo?.available ?: 0L),
                modifier = Modifier.weight(1f)
            )
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HardwareRow(Icons.Default.PhoneAndroid, "System Model", hw.model)
                HardwareRow(Icons.Default.DeveloperBoard, "Architecture", hw.architecture)
                HardwareRow(Icons.Default.Android, "Android Version", "${hw.androidVersion} (SDK ${hw.sdkVersion})")
                HardwareRow(Icons.Default.Memory, "Processor", deviceCapabilities?.processorName ?: hw.chipName)
                HardwareRow(Icons.Default.GraphicEq, "GPU", deviceCapabilities?.gpuName ?: "Unavailable")
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HardwareRow(Icons.Default.BatteryChargingFull, "Battery", "${batteryInfo?.percentage ?: 0}%")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(batteryIcon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Battery Health: ${batteryInfo?.health ?: "Unknown"}")
                }
                HardwareRow(Icons.Default.Storage, "RAM Total", formatBytes(ramInfo?.total ?: 0L))
                HardwareRow(Icons.Default.SdStorage, "Storage Total", formatBytes(storageInfo?.total ?: 0L))
                HardwareRow(Icons.Default.SdStorage, "Storage Free", formatBytes(storageInfo?.available ?: 0L))
                HardwareRow(Icons.Default.Apps, "Installed Apps", installedApps.size.toString())
                HardwareRow(Icons.Default.Sensors, "Sensors", sensors.size.toString())
                HardwareRow(Icons.Default.CameraAlt, "Camera Modules", (deviceCapabilities?.cameraCount ?: 0).toString())
                HardwareRow(Icons.Default.Bluetooth, "Bluetooth", if (deviceCapabilities?.bluetoothAvailable == true) "Available" else "Not available")
                HardwareRow(Icons.Default.GraphicEq, "Audio", if (deviceCapabilities?.audioAvailable == true) "Available" else "Unavailable")
                HardwareRow(Icons.Default.Memory, "Memory", formatBytes(deviceCapabilities?.memoryTotal ?: 0L))
            }
        }
    }
}

@Composable
fun SoftwareScreen() {
    val hw = remember { SystemInfoManager.getHardwareInfo() }
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Software", "OS details and build data")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Android Version: ${hw.androidVersion}")
                Text("SDK: ${hw.sdkVersion}")
                Text("Security Patch: ${hw.securityPatch}")
                Text("Build Number: ${hw.buildNumber}")
            }
        }
    }
}

@Composable
fun BatteryScreen(viewModel: ToolsViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Battery", "Power and health status")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "Level",
                value = "${batteryInfo?.percentage ?: 0}%",
                modifier = Modifier.weight(1f)
            )
            SpecCard(
                icon = Icons.Default.GraphicEq,
                title = "Temperature",
                value = "${batteryInfo?.temperature ?: 0f}°C",
                modifier = Modifier.weight(1f)
            )
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Level: ${batteryInfo?.percentage ?: 0}%")
                Text("Charging: ${if (batteryInfo?.isCharging == true) "Yes" else "No"}")
                Text("Health: ${batteryInfo?.health ?: "Unknown"}")
                Text("Temperature: ${batteryInfo?.temperature ?: 0f}°C")
                Text("Technology: ${batteryInfo?.technology ?: "Unknown"}")
            }
        }
    }
}

@Composable
fun AppsScreen(viewModel: ToolsViewModel) {
    val apps by viewModel.installedApps.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Installed Apps", "Package inventory")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(apps.take(120)) { app ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Column {
                        Text(app.name, fontWeight = FontWeight.Bold)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                        Text("Version: ${app.version} | System: ${if (app.isSystemApp) "Yes" else "No"}")
                    }
                }
            }
        }
    }
}

@Composable
fun SensorsScreen(viewModel: ToolsViewModel) {
    val sensors by viewModel.sensors.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Sensors", "Available hardware sensors")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sensors) { sensor ->
                GlassCard(Modifier.fillMaxWidth()) { Text(sensor) }
            }
        }
    }
}

@Composable
fun WifiAnalyzerScreen(viewModel: ToolsViewModel) {
    val wifiStatus by viewModel.wifiStatus.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("WiFi Connection", "Connection and status")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoLine(Icons.Default.Wifi, "Status", if (wifiStatus?.connected == true) "Connected" else "Disconnected")
                InfoLine(Icons.Default.Router, "Network Type", wifiStatus?.networkType ?: "Unknown")
                InfoLine(Icons.Default.Wifi, "SSID", wifiStatus?.ssid ?: "N/A")
                InfoLine(Icons.Default.Public, "Local IP", wifiStatus?.ipAddress ?: "N/A")
            }
        }
    }
}

private fun formatBytes(value: Long): String {
    if (value <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var index = 0
    while (size >= 1024 && index < units.lastIndex) {
        size /= 1024
        index++
    }
    return "%.2f %s".format(size, units[index])
}

@Composable
private fun HardwareRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: $value")
    }
}

@Composable
private fun CpuUsageGraph(history: List<Float>) {
    val safe = if (history.isEmpty()) listOf(0f) else history
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val path = Path()
        safe.forEachIndexed { index, value ->
            val x = (index.toFloat() / (safe.lastIndex.coerceAtLeast(1))) * size.width
            val y = size.height - ((value.coerceIn(0f, 100f) / 100f) * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor)
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: $value")
    }
}

@Composable
private fun StatPill(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

private fun deviceTypeIcon(os: String, type: String): ImageVector {
    val tag = "$os $type".lowercase()
    return when {
        tag.contains("router") || tag.contains("network") -> Icons.Default.Router
        tag.contains("apple") || tag.contains("android") || tag.contains("phone") -> Icons.Default.PhoneAndroid
        tag.contains("windows") || tag.contains("linux") || tag.contains("computer") -> Icons.Default.Devices
        else -> Icons.Default.Dns
    }
}

@Composable
private fun SpecCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier.heightIn(min = 92.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}


