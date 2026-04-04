package com.example.flash.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flash.*
import com.example.flash.ui.theme.TechBlue
import com.example.flash.ui.theme.TechPurple
import com.example.flash.ui.theme.TechSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val health by remember { DeviceDiscoveryManager.networkHealthScore }
    val context = LocalContext.current
    val viewModel: ToolsViewModel = viewModel()
    val ipInfo by viewModel.ipInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchIpInfo(context)
    }

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        SectionHeader("Command Center", "Real-time infrastructure intelligence")

        // Modern Glassmorphism Health Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))))
                .padding(1.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        ) {
            Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                        label = "scale"
                    )
                    
                    CircularProgressIndicator(
                        progress = { health / 100f },
                        modifier = Modifier.size(90.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                        color = if (health > 80) Color(0xFF00FF88) else if (health > 50) MaterialTheme.colorScheme.primary else Color(0xFFFF4B4B),
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                    Text("$health%", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(24.dp))
                Column {
                    Text("SYSTEM INTEGRITY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Text(if(health > 80) "SECURE" else "VULNERABLE", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Last audit: Just now", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        ModernSectionTitle("Edge Node Data", Icons.Default.Public)
        
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(Modifier.padding(20.dp)) {
                ipInfo?.let { info ->
                    ModernInfoRow("Gateway", info.ip, Icons.Default.Dns, MaterialTheme.colorScheme.primary)
                    ModernInfoRow("Provider", info.isp, Icons.Default.Router, MaterialTheme.colorScheme.secondary)
                    ModernInfoRow("Location", "${info.city}, ${info.country}", Icons.Default.Explore, Color.Yellow)
                } ?: LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        ModernSectionTitle("System Operations", Icons.Default.GridView)
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModernActionCard("Radar Scan", Icons.Default.Radar, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { navController.navigate(Screen.NetworkMap.route) }
            ModernActionCard("Performance", Icons.AutoMirrored.Filled.ShowChart, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { navController.navigate(Screen.Diagnostics.route) }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModernActionCard("Sniffer Lab", Icons.Default.Search, Color(0xFF00FF88), Modifier.weight(1f)) { navController.navigate("packet_inspector") }
            ModernActionCard("Secure SSH", Icons.Default.Terminal, Color(0xFFFFB74D), Modifier.weight(1f)) { navController.navigate(Screen.Terminal.route) }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun NodesScreen() {
    val devices = DeviceDiscoveryManager.knownDevices
    val isScanning by remember { DeviceDiscoveryManager.isScanning }
    val context = LocalContext.current
    val viewModel: ToolsViewModel = viewModel()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionHeader("Network Topology", "Mapping ${devices.size} active identities")
            }
            Surface(
                color = if(isScanning) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp).clickable(enabled = !isScanning) { DeviceDiscoveryManager.startScan(context) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isScanning) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Radar, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (devices.isEmpty() && !isScanning) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No data in buffer. Initialize scan.", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(devices) { device ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        when(device.deviceType) {
                                            "Router" -> Icons.Default.Router
                                            "Smartphone" -> Icons.Default.Smartphone
                                            "Computer" -> Icons.Default.Computer
                                            else -> Icons.Default.Memory
                                        },
                                        null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(device.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(device.ip, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                Text("MAC: ${device.mac}", color = Color.Gray, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(onClick = { viewModel.sendWakeOnLan(device.mac) }) {
                                    Icon(Icons.Default.PowerSettingsNew, "WOL", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Text("${device.latency}ms", fontWeight = FontWeight.Black, color = Color(0xFF00FF88), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InternetToolsScreen() {
    val viewModel: ToolsViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    var hostInput by remember { mutableStateOf("google.com") }
    
    val pingResult by viewModel.pingResult.collectAsState()
    val portResults by viewModel.portScanResults.collectAsState()
    val dnsResults by viewModel.dnsResults.collectAsState()
    val tracerouteResults by viewModel.tracerouteResults.collectAsState()
    
    val isScanning by viewModel.isScanning.collectAsState()
    val isTracerouteRunning by viewModel.isTracerouteRunning.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Protocol Lab", "Advanced traffic synthesis")
        
        TextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("Enter target identity...", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.Podcasts, null, tint = MaterialTheme.colorScheme.primary) }
        )
        
        Spacer(Modifier.height(24.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(4.dp)) {
                ModernTabItem("PING", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                ModernTabItem("NMAP", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                ModernTabItem("DNS", selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
                ModernTabItem("TRACE", selectedTab == 3, Modifier.weight(1f)) { selectedTab = 3 }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Box(Modifier.weight(1f)) {
            when(selectedTab) {
                0 -> PingView(hostInput, pingResult) { viewModel.runPing(hostInput) }
                1 -> PortScanView(hostInput, portResults, isScanning) { viewModel.runPortScan(hostInput, 1, 1024) }
                2 -> DnsView(hostInput, dnsResults) { viewModel.runDnsLookup(hostInput) }
                3 -> TracerouteView(hostInput, tracerouteResults, isTracerouteRunning) { viewModel.runTraceroute(hostInput) }
            }
        }
    }
}

@Composable
fun PacketInspectorScreen() {
    var isCapturing by remember { mutableStateOf(false) }
    val packets = DeviceDiscoveryManager.capturedPackets
    val context = LocalContext.current
    
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            while(isCapturing) {
                val packet = NetworkToolsManager.captureRealPacket(context)
                DeviceDiscoveryManager.addPacket(packet)
                delay((100..500).random().toLong())
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionHeader("Frame Buffer", "Capturing live data stream")
            }
            IconButton(
                onClick = { isCapturing = !isCapturing },
                modifier = Modifier.size(56.dp).background(if(isCapturing) Color(0xFFFF4B4B) else MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(if(isCapturing) Icons.Default.Stop else Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = Color.Black,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
                items(packets) { packet ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(packet.protocol, Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        Column {
                            Text("${packet.source} → ${packet.destination}", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text(packet.info, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 0.5.dp)
                }
            }
        }
    }
}

// --- MODERN UI UTILS ---

@Composable
fun ModernSectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ModernTabItem(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.onPrimary else Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
    }
}

@Composable
fun ModernInfoRow(label: String, value: String, icon: ImageVector, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ModernActionCard(label: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Text(label, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
        }
    }
}

// --- SUB-VIEWS ---

@Composable
fun DnsView(host: String, results: List<DnsResult>, onRun: () -> Unit) {
    Column {
        Button(onClick = onRun, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("EXECUTE RESOLVER", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(results) { record ->
                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(record.ip, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(record.domain, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TracerouteView(host: String, results: List<TracerouteStep>, isRunning: Boolean, onRun: () -> Unit) {
    Column {
        Button(onClick = onRun, enabled = !isRunning, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            if (isRunning) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
            else Text("INITIATE TRACE", fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { step ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${step.hop}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
                    Text(step.ip, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace)
                    Text("${step.time}ms", color = Color.Gray, fontSize = 12.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun PingView(host: String, result: PingLog?, onRun: () -> Unit) {
    Column {
        Button(onClick = onRun, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("SEND ICMP PROBE", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.height(24.dp))
        result?.let {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(24.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(if(it.success) Color.Green else Color.Red, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Text(if(it.success) "HOST REACHABLE" else "REQUEST TIMEOUT", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Latency: ${it.avgTime}ms", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun PortScanView(host: String, results: List<PortInfo>, isScanning: Boolean, onRun: () -> Unit) {
    Column {
        Button(onClick = onRun, enabled = !isScanning, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            if (isScanning) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
            else Text("START NMAP SCAN", fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(results) { port ->
                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("PORT ${port.port}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Text(port.service, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun WifiAnalyzerScreen() {
    val context = LocalContext.current
    var networks by remember { mutableStateOf(emptyList<WifiNetworkInfo>()) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    fun refresh() {
        isRefreshing = true
        networks = NetworkToolsManager.getWifiScanResults(context)
        isRefreshing = false
    }

    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionHeader("WiFi Radar", "${networks.size} spectrum signatures")
            }
            IconButton(onClick = { refresh() }, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(networks.sortedByDescending { it.level }) { network ->
                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = if(network.level > -60) Color.Green else Color.Yellow)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(network.ssid, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(network.capabilities, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                        }
                        Text("${network.level} dBm", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Shield Protocol", "Vulnerability assessment active")
        
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, Color.Black)), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF00FF88), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("CORE INTEGRITY: OPTIMAL", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(24.dp))
                SecurityToggle("Real-time Protection", true)
                SecurityToggle("Encrypted Tunnel", false)
                SecurityToggle("Deep Frame Analysis", true)
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("INITIALIZE PEN-TEST", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun SecurityToggle(label: String, initial: Boolean) {
    var checked by remember { mutableStateOf(initial) }
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { checked = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun DiagnosticsScreen() {
    val viewModel: ToolsViewModel = viewModel()
    val speedMetrics by viewModel.speedTestMetrics.collectAsState()
    val isRunning by viewModel.isSpeedTestRunning.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Telemetry", "System performance synthesis")
        
        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp)).padding(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NETWORK SPEED TEST", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(32.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SpeedMetricGauge("DOWNLOAD", speedMetrics.downloadMbps, "Mb/s", MaterialTheme.colorScheme.primary)
                    SpeedMetricGauge("UPLOAD", speedMetrics.uploadMbps, "Mb/s", MaterialTheme.colorScheme.secondary)
                }
                
                Spacer(Modifier.height(32.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DiagnosticMiniMetric("LATENCY", "${speedMetrics.latency}", "ms")
                    DiagnosticMiniMetric("JITTER", "${speedMetrics.jitter}", "ms")
                }
                
                Spacer(Modifier.height(40.dp))
                
                Button(
                    onClick = { viewModel.runSpeedTest() },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isRunning) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    else Text("START DIAGNOSTIC", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SpeedMetricGauge(label: String, value: Double, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { (value / 100f).toFloat() },
                modifier = Modifier.size(100.dp),
                color = color,
                strokeWidth = 8.dp,
                trackColor = color.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(String.format("%.1f", value), fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(unit, fontSize = 10.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun DiagnosticMiniMetric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value + unit, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun AnalyticsScreen() {
    val logs = DeviceDiscoveryManager.discoveryLog
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Blackbox Log", "Persistent event stream")
        Surface(Modifier.fillMaxSize(), color = Color.Black, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)) {
            LazyColumn(Modifier.padding(16.dp)) {
                items(logs) { log ->
                    Text(
                        text = "> $log",
                        color = Color(0xFF00FF41),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoScreen() {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(100.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FlashOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(60.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("FLASH PRO", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text("STABLE GOLD EDITION", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(48.dp))
        Text("Designed for infrastructure auditing and security research. v1.0.4", textAlign = TextAlign.Center, color = Color.Gray)
    }
}
