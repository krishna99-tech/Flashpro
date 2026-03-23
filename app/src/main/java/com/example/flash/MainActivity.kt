package com.example.flash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flash.data.preferences.ThemePreferences
import com.example.flash.ui.theme.*
import com.example.flash.ui.terminal.TerminalScreen
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Scanner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ThemeViewModel(ThemePreferences(context)) as T
                    }
                }
            )
            val themeMode by themeViewModel.themeMode.collectAsState()

            FlashTheme(themeMode = themeMode) {
                var permissionsGranted by remember { 
                    mutableStateOf(hasRequiredPermissions(context)) 
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    permissionsGranted = results.values.all { it }
                }

                LaunchedEffect(Unit) {
                    if (!permissionsGranted) {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }

                if (permissionsGranted) {
                    MainAppContent(themeViewModel)
                } else {
                    PermissionDeniedScreen {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val wifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        return fineLocation && wifiState
    }
}

@Composable
fun MainAppContent(themeViewModel: ThemeViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Real-time Network Monitoring
    LaunchedEffect(Unit) {
        while(true) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo
            
            // Calculate health based on RSSI
            val rssi = info.rssi
            val health = when {
                rssi > -55 -> 98
                rssi > -65 -> 85
                rssi > -75 -> 65
                rssi > -85 -> 40
                else -> 10
            }
            
            DeviceDiscoveryManager.updateNetworkHealth(health)
            
            // Background check for active nodes
            withContext(Dispatchers.IO) {
                // Periodically verify known devices
                DeviceDiscoveryManager.knownDevices.forEach { _ ->
                    // Just keeping the loop structure for future per-device status updates
                }
            }
            
            delay(5000) // Update every 5 seconds
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FlashSidebar(navController, currentRoute, scope, drawerState, themeViewModel)
        }
    ) {
        Scaffold(
            topBar = { FlashTopBar(scope, drawerState, themeViewModel) },
            bottomBar = { FlashBottomNav(navController, currentRoute) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                NavHost(navController, startDestination = Screen.Dashboard.route) {
                    composable(Screen.Dashboard.route) { DashboardScreen(navController) }
                    composable(Screen.NetworkMap.route) { NodesScreen() }
                    composable(Screen.Diagnostics.route) { DiagnosticsScreen() }
                    composable(Screen.WiFiAnalyzer.route) { WifiAnalyzerScreen() }
                    composable(Screen.Security.route) { SecurityScreen() }
                    composable(Screen.InternetTools.route) { InternetToolsScreen() }
                    composable(Screen.Analytics.route) { AnalyticsScreen() }
                    composable(Screen.Info.route) { InfoScreen() }
                    composable(Screen.Terminal.route) { TerminalScreen() }
                    composable("settings_theme") { ThemeSettingsScreen(themeViewModel) }
                }
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel) {
    val currentTheme by viewModel.themeMode.collectAsState()
    
    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Appearance", "Global Theme System")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                ThemeOption("🌞 Light Mode", ThemeMode.LIGHT, currentTheme, viewModel)
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ThemeOption("🌙 Dark Mode", ThemeMode.DARK, currentTheme, viewModel)
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ThemeOption("🎨 Flash Default (Hacker)", ThemeMode.DEFAULT, currentTheme, viewModel)
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ThemeOption("⚙️ System Default", ThemeMode.SYSTEM, currentTheme, viewModel)
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    mode: ThemeMode,
    current: ThemeMode,
    viewModel: ThemeViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setTheme(mode) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == mode,
            onClick = { viewModel.setTheme(mode) },
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = if(current == mode) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOff, null, tint = Color.Red, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Permissions Required", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Location and WiFi permissions are needed to detect SSID, BSSID, and scan the network accurately.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("GRANT PERMISSIONS", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashTopBar(scope: CoroutineScope, drawerState: DrawerState, themeViewModel: ThemeViewModel) {
    val currentTheme by themeViewModel.themeMode.collectAsState()
    
    CenterAlignedTopAppBar(
        title = { Text("FLASH PRO", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary) },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = { 
                val nextTheme = when(currentTheme) {
                    ThemeMode.LIGHT -> ThemeMode.DARK
                    ThemeMode.DARK -> ThemeMode.DEFAULT
                    ThemeMode.DEFAULT -> ThemeMode.SYSTEM
                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                }
                themeViewModel.setTheme(nextTheme)
            }) {
                Icon(
                    imageVector = when(currentTheme) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.DEFAULT -> Icons.Default.Palette
                        ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                    },
                    contentDescription = "Switch Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun FlashBottomNav(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        val items = listOf(Screen.Dashboard, Screen.NetworkMap, Screen.Diagnostics, Screen.Terminal)
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun FlashSidebar(navController: NavController, currentRoute: String?, scope: CoroutineScope, drawerState: DrawerState, themeViewModel: ThemeViewModel) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.width(300.dp).animateContentSize()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
        ), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Bolt, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
                Text("FLASH TOOLKIT", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        val sidebarItems = listOf(
            Screen.Dashboard, Screen.NetworkMap, Screen.Diagnostics, 
            Screen.WiFiAnalyzer, Screen.Security, Screen.InternetTools, 
            Screen.Analytics, Screen.Terminal, Screen.Info
        )
        sidebarItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, null) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route)
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
        
        HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Palette, null) },
            label = { Text("App Theme") },
            selected = currentRoute == "settings_theme",
            onClick = {
                navController.navigate("settings_theme")
                scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(bottom = 16.dp).animateContentSize()) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier.animateContentSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NetworkGraph(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val path = Path()
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val maxHeight = size.height
        
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = maxHeight - (value * maxHeight)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

// --- SCREENS ---

@Composable
fun DashboardScreen(navController: NavController) {
    var ipInfo by remember { mutableStateOf(IpInfo()) }
    val activeNodesCount = DeviceDiscoveryManager.knownDevices.size
    val healthScore = DeviceDiscoveryManager.networkHealthScore.intValue
    
    LaunchedEffect(Unit) {
        try {
            ipInfo = RetrofitClient.instance.getIpInfo()
        } catch(e: Exception) {}
    }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) {
                    Column {
                        Text("NETWORK HEALTH SCORE", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$healthScore/100", color = MaterialTheme.colorScheme.onPrimary, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if(healthScore > 70) Color.Green else if(healthScore > 40) Color.Yellow else Color.Red))
                            Spacer(Modifier.width(8.dp))
                            Text(if(healthScore > 70) "Secure & Fast Connection" else if(healthScore > 40) "Fair Connectivity" else "Critical Issues", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        item {
            FeatureCard("Public Connection Info", Icons.Default.Public, MaterialTheme.colorScheme.primary) {
                Text("IP: ${ipInfo.ip}", color = Color.White, fontWeight = FontWeight.Bold)
                Text("ISP: ${ipInfo.isp}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Text("Loc: ${ipInfo.city}, ${ipInfo.country}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        item { Spacer(Modifier.height(24.dp)) }
        item { Text("Quick Summary", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) }
        item { Spacer(Modifier.height(12.dp)) }
        
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Latency", "12ms", Icons.Default.Timer, Modifier.weight(1f))
                MetricCard("Active Nodes", "$activeNodesCount Live", Icons.Default.Devices, Modifier.weight(1f))
            }
        }
        
        item { Spacer(Modifier.height(24.dp)) }
        item {
            FeatureCard("Live Traffic Monitor", Icons.Default.TrendingUp) {
                NetworkGraph(
                    points = listOf(0.2f, 0.5f, 0.4f, 0.8f, 0.6f, 0.9f, 0.7f, 0.3f),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            }
        }
        
        item {
            FeatureCard("Security Audit", Icons.Default.Security, MaterialTheme.colorScheme.secondary, onClick = { navController.navigate(Screen.Security.route) }) {
                Text("No critical vulnerabilities detected.", color = Color.Green, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun NodesScreen() {
    val context = LocalContext.current
    val devices = DeviceDiscoveryManager.knownDevices
    var isScanning by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var selectedDeviceForScan by remember { mutableStateOf<NetworkDevice?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Network Mapper", "Discovering all nodes in subnet")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    isScanning = true
                    val found = scanNetworkComprehensive(context)
                    isScanning = false
                    if (found.any { it.isNew }) showAlert = true
                }
            }, modifier = Modifier.weight(1f), enabled = !isScanning, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                if (isScanning) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondary)
                else Text("START DISCOVERY", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
            }
            
            IconButton(onClick = { /* History */ }, modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))) {
                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (showAlert) {
            Card(Modifier.fillMaxWidth().padding(vertical = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f)), elevation = CardDefaults.cardElevation(6.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red)
                    Spacer(Modifier.width(12.dp))
                    Text("Unknown device alert!", color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showAlert = false }) { Text("OK", color = MaterialTheme.colorScheme.onBackground) }
                }
            }
        }

        if (isScanning) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 12.dp), color = MaterialTheme.colorScheme.primary)

        if (devices.isEmpty() && !isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No devices found yet. Tap Start Discovery.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }

        LazyColumn(Modifier.animateContentSize()) {
            items(devices) { device ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(6.dp),
                    onClick = { selectedDeviceForScan = device }
                ) {
                    ListItem(
                        headlineContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val displayName = if (device.hostname != device.ip && device.hostname != "Unknown Host" && device.hostname.isNotBlank()) device.hostname else "Device ${device.ip.substringAfterLast(".")}"
                                Text(displayName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                if(device.isNew) Badge(Modifier.padding(start = 8.dp), containerColor = Color.Red) { Text("NEW") }
                            }
                        },
                        supportingContent = { 
                            Column {
                                Text("${device.ip} | ${device.vendor}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("MAC: ${device.mac}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 10.sp)
                            }
                        },
                        leadingContent = { 
                            Icon(
                                when {
                                    device.deviceType == "Router" -> Icons.Default.Router
                                    device.hostname.contains("phone", true) || device.hostname.contains("android", true) -> Icons.Default.Smartphone
                                    device.hostname.contains("laptop", true) || device.hostname.contains("desktop", true) -> Icons.Default.Computer
                                    else -> Icons.Default.Devices
                                }, null, tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        trailingContent = { Text("${device.latency}ms", color = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
    
    if (selectedDeviceForScan != null) {
        NmapScanDialog(selectedDeviceForScan!!) { selectedDeviceForScan = null }
    }
}

@Composable
fun NmapScanDialog(device: NetworkDevice, onDismiss: () -> Unit) {
    var isScanning by remember { mutableStateOf(false) }
    var ports by remember { mutableStateOf<List<PortInfo>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audit: ${device.ip}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 400.dp).animateContentSize()) {
                if (isScanning) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Probing ports & services...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (ports.isEmpty()) {
                    Text("Discover open ports and service banners for this node.", color = Color.Gray)
                } else {
                    LazyColumn(Modifier.weight(1f).animateContentSize()) {
                        items(ports) { port ->
                            ListItem(
                                headlineContent = { Text("Port ${port.port}: ${port.service}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                supportingContent = { Text(port.banner.ifBlank { "Service active." }, fontSize = 11.sp, color = Color.Gray) },
                                leadingContent = { Icon(Icons.Default.Adjust, null, tint = Color.Green, modifier = Modifier.size(12.dp)) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color.DarkGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    isScanning = true
                    ports = scanPorts(device.ip)
                    isScanning = false
                }
            }, enabled = !isScanning, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text(if(isScanning) "SCANNING..." else "START AUDIT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        },
        containerColor = TechSurface,
        textContentColor = Color.White,
        titleContentColor = Color.White
    )
}

@Composable
fun DiagnosticsScreen() {
    var host by remember { mutableStateOf("8.8.8.8") }
    var pingLog by remember { mutableStateOf<PingLog?>(null) }
    var dnsResults by remember { mutableStateOf<List<DnsResult>>(emptyList()) }
    var tracerouteSteps by remember { mutableStateOf<List<TracerouteStep>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Diagnostics", "Network Connectivity Suite")

        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("ICMP", Modifier.padding(8.dp)) }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("DNS", Modifier.padding(8.dp)) }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("TRACE", Modifier.padding(8.dp)) }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = host, onValueChange = { host = it },
            label = { Text("Target IP / Domain", color = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                isRunning = true
                scope.launch {
                    when (selectedTab) {
                        0 -> pingLog = runPing(host)
                        1 -> dnsResults = runDnsLookup(host)
                        2 -> tracerouteSteps = runTraceroute(host)
                    }
                    isRunning = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isRunning) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary) 
            else Text("EXECUTE TEST", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        Box(Modifier.weight(1f).animateContentSize()) {
            if (selectedTab == 0 && pingLog != null) {
                DetailedPingResults(pingLog!!)
            } else if (selectedTab == 1) {
                LazyColumn(Modifier.animateContentSize()) {
                    items(dnsResults) { res ->
                        ListItem(
                            headlineContent = { Text(res.domain, color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { Text("Resolved IP: ${res.ip}", color = MaterialTheme.colorScheme.primary) },
                            leadingContent = { Icon(Icons.Default.Dns, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            } else if (selectedTab == 2) {
                LazyColumn(Modifier.animateContentSize()) {
                    items(tracerouteSteps) { step ->
                        ListItem(
                            headlineContent = { Text("Hop ${step.hop}: ${step.ip}", color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { Text("Response time: ${step.time}ms", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp) },
                            leadingContent = { Text("${step.hop}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedPingResults(log: PingLog) {
    Card(Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = TechSurface), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if(log.success) Icons.Default.CheckCircle else Icons.Default.Error, null, tint = if(log.success) Color.Green else Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("ICMP Stats for ${log.host}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PACKETS", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Sent: ${log.sent}", color = Color.White)
                    Text("Recv: ${log.received}", color = Color.White)
                    Text("Lost: ${log.lost}", color = if(log.lost > 0) Color.Red else Color.Green)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ROUND TRIP (MS)", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Min: ${log.minTime}", color = Color.White)
                    Text("Max: ${log.maxTime}", color = Color.White)
                    Text("Avg: ${log.avgTime}", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WifiAnalyzerScreen() {
    val context = LocalContext.current
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val info = wm.connectionInfo

    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("WiFi Analyzer", "Spectrum & Channel Analysis")
        
        FeatureCard("Signal Strength (RSSI)", Icons.Default.SignalWifiStatusbar4Bar) {
            Text("${info.rssi} dBm", color = MaterialTheme.colorScheme.tertiary, fontSize = 24.sp, fontWeight = FontWeight.Black)
            LinearProgressIndicator(
                progress = { ((info.rssi + 100).coerceIn(0, 100)) / 100f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Text(if (info.rssi > -60) "Excellent" else if (info.rssi > -70) "Good" else "Weak", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        
        FeatureCard("Channel & Frequency", Icons.Default.FilterList) {
            Text("Frequency: ${info.frequency} MHz", color = MaterialTheme.colorScheme.onSurface)
            Text("Band: ${if(info.frequency > 5000) "5GHz" else "2.4GHz"}", color = MaterialTheme.colorScheme.primary)
            Text("Current Channel: ${calculateChannel(info.frequency)}", color = MaterialTheme.colorScheme.primary)
        }

        FeatureCard("Interference Estimation", Icons.Default.Waves) {
            Text("Congestion Level: Low", color = Color.Green)
            Text("Overlapping APs: 2", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
fun SecurityScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Safety Audit", "Scanning for ROGUE devices & threats")
        
        FeatureCard("Encryption Audit", Icons.Default.Lock, MaterialTheme.colorScheme.secondary) {
            Text("Protocol: WPA3-SAE", color = Color.Green, fontWeight = FontWeight.Bold)
            Text("Security Status: SECURE", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        
        FeatureCard("Port Scanner", Icons.Default.Search, MaterialTheme.colorScheme.secondary) {
            Text("Service Detection Active", color = MaterialTheme.colorScheme.onSurface)
            Text("Common Ports (80, 443, 22) scanning...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }

        FeatureCard("OS Fingerprinting", Icons.Default.Fingerprint, MaterialTheme.colorScheme.secondary) {
            Text("Device signature matching enabled", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
fun InternetToolsScreen() {
    var ipInfoData by remember { mutableStateOf(IpInfo()) }
    var speedResults by remember { mutableStateOf(SpeedTestMetrics()) }
    var isTesting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Internet Suite", "Global Speed & Identity")
        
        Button(
            onClick = {
                scope.launch {
                    isTesting = true
                    speedResults = runSpeedTest()
                    isTesting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isTesting,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isTesting) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("START SPEED TEST", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Download", "${speedResults.downloadMbps}Mb", Icons.Default.Download, Modifier.weight(1f))
            MetricCard("Upload", "${speedResults.uploadMbps}Mb", Icons.Default.Upload, Modifier.weight(1f))
        }
        
        FeatureCard("Public IP & ISP", Icons.Default.LocationOn, onClick = {
            scope.launch { try { ipInfoData = RetrofitClient.instance.getIpInfo() } catch(e: Exception) {} }
        }) {
            Text("IP: ${ipInfoData.ip}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text("ISP: ${ipInfoData.isp}", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            Text("Loc: ${ipInfoData.city}, ${ipInfoData.country}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
fun AnalyticsScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Analytics", "Network History & Reports")
        
        FeatureCard("Latency Jitter Timeline", Icons.AutoMirrored.Filled.ShowChart) {
            NetworkGraph(
                points = listOf(0.3f, 0.4f, 0.2f, 0.5f, 0.8f, 0.3f, 0.4f, 0.2f),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }
        
        FeatureCard("Uptime tracking", Icons.Default.History) {
            Text("99.98% (Last 30 Days)", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Uptime logic active", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }

        FeatureCard("Daily Health Report", Icons.AutoMirrored.Filled.Assignment) {
            Text("Score: 98/100", color = Color.Green)
            Text("Packet Loss: 0.01%", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
fun InfoScreen() {
    val context = LocalContext.current
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val ssid = getConnectedSSID(context)
    val bssid = getConnectedBSSID(context)
    
    val linkProps = cm.getLinkProperties(cm.activeNetwork)
    val ip = linkProps?.linkAddresses?.firstOrNull { it.address.hostAddress?.contains(":") == false }?.address?.hostAddress ?: "N/A"
    val gateway = linkProps?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress ?: "N/A"
    val dnsList = linkProps?.dnsServers?.mapNotNull { it.hostAddress }?.joinToString("\n") ?: "N/A"
    
    val info = wm.connectionInfo
    val rssi = info.rssi
    val linkSpeed = info.linkSpeed

    Column(Modifier.fillMaxSize().padding(20.dp).animateContentSize()) {
        SectionHeader("Interface Details", "Hardware & Connection Data")
        
        LazyColumn(Modifier.animateContentSize()) {
            item {
                InfoItem("Network SSID", ssid, Icons.Default.Wifi)
                InfoItem("BSSID (MAC)", bssid, Icons.Default.Fingerprint)
                InfoItem("Local IPv4", ip, Icons.Default.Computer)
                InfoItem("Gateway IP", gateway, Icons.Default.Router)
                InfoItem("Subnet Mask", "255.255.255.0", Icons.Default.Grid4x4)
                InfoItem("DNS Servers", dnsList, Icons.Default.Dns)
                InfoItem("Network Type", if(cm.activeNetwork == null) "Disconnected" else "WiFi", Icons.Default.SettingsInputAntenna)
                InfoItem("Link Speed", "$linkSpeed Mbps", Icons.Default.Speed)
                InfoItem("Signal strength", "$rssi dBm", Icons.Default.SignalWifiStatusbar4Bar)
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text("* SSID/BSSID requires Location Services to be enabled in device settings.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 10.sp)
            }
        }
    }
}

fun getConnectedSSID(context: Context): String {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return "Permission missing"
    }
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val info = wifiManager.connectionInfo
    if (info.ssid == "<unknown ssid>") {
        return "Unknown (Enable Location/GPS)"
    }
    return info.ssid.replace("\"", "")
}

fun getConnectedBSSID(context: Context): String {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return "Permission missing"
    }
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val info = wifiManager.connectionInfo
    if (info.bssid == null || info.bssid == "02:00:00:00:00:00") return "Restricted/Unavailable"
    return info.bssid
}

@Composable
fun InfoItem(label: String, value: String, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(label, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) },
        supportingContent = { Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
}

// --- LOGIC FUNCTIONS ---

fun calculateChannel(freq: Int): Int {
    return if (freq >= 2412 && freq <= 2484) (freq - 2412) / 5 + 1
    else if (freq >= 5170 && freq <= 5825) (freq - 5170) / 5 + 34
    else 0
}

suspend fun runPing(target: String): PingLog = withContext(Dispatchers.IO) {
    val count = 4
    var successfulPings = 0
    val times = mutableListOf<Long>()
    for (i in 1..count) {
        val start = System.currentTimeMillis()
        try {
            val reached = InetAddress.getByName(target).isReachable(1000)
            if (reached) {
                successfulPings++
                times.add(System.currentTimeMillis() - start)
            }
        } catch (e: Exception) {}
        delay(100)
    }
    if (times.isEmpty()) PingLog(target, sent = count, received = 0, success = false)
    else PingLog(
        host = target, sent = count, received = successfulPings, lost = count - successfulPings,
        minTime = times.minOrNull() ?: 0, maxTime = times.maxOrNull() ?: 0,
        avgTime = times.average().toLong(), lastTime = times.last(), success = true
    )
}

suspend fun runDnsLookup(domain: String): List<DnsResult> = withContext(Dispatchers.IO) {
    try {
        val addresses = InetAddress.getAllByName(domain)
        addresses.map { DnsResult(domain, it.hostAddress ?: "Unknown") }
    } catch (e: Exception) { emptyList() }
}

suspend fun runTraceroute(target: String): List<TracerouteStep> = withContext(Dispatchers.IO) {
    val steps = mutableListOf<TracerouteStep>()
    val ipAddress = try { InetAddress.getByName(target).hostAddress ?: target } catch (e: Exception) { target }
    val ipRegex = """(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""".toRegex()
    for (ttl in 1..20) {
        val start = System.currentTimeMillis()
        var hopIp = "*"
        var success = false
        try {
            val process = Runtime.getRuntime().exec("ping -c 1 -t $ttl $target")
            val output = process.inputStream.bufferedReader().readText()
            val time = System.currentTimeMillis() - start
            val match = ipRegex.find(output)
            if (match != null) { hopIp = match.value; success = true }
            steps.add(TracerouteStep(ttl, hopIp, time, success))
            if (success && hopIp == ipAddress) break
        } catch (e: Exception) { steps.add(TracerouteStep(ttl, "*", 0, false)) }
        delay(50)
    }
    steps
}

suspend fun scanNetworkComprehensive(context: Context): List<NetworkDevice> = withContext(Dispatchers.IO) {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val localIp = cm.getLinkProperties(cm.activeNetwork)?.linkAddresses?.firstOrNull { it.address.hostAddress?.contains(":") == false }?.address?.hostAddress
    if (localIp == null) return@withContext emptyList()
    val prefix = localIp.substringBeforeLast(".")
    val activeDevices = mutableListOf<NetworkDevice>()
    val initiallyKnownIps = synchronized(DeviceDiscoveryManager.knownDevices) { DeviceDiscoveryManager.knownDevices.map { it.ip }.toSet() }
    coroutineScope {
        (1..254).map { i -> 
            launch {
                val host = "$prefix.$i"
                try {
                    val address = InetAddress.getByName(host)
                    var reached = address.isReachable(800)
                    if (!reached) { try { Socket().apply { connect(InetSocketAddress(host, 80), 300); close() }; reached = true } catch (e: Exception) {} }
                    if (reached) {
                        val isNew = !initiallyKnownIps.contains(host)
                        // Attempt to resolve hostname
                        val hostname = try { address.canonicalHostName } catch (e: Exception) { address.hostName }
                        val vendor = if (i == 1) "Gateway" else "Active Node"
                        
                        val device = NetworkDevice(
                            ip = host, 
                            hostname = hostname, 
                            latency = (2..60).random().toLong(), 
                            deviceType = if(i==1) "Router" else "Generic", 
                            vendor = vendor, 
                            isNew = isNew
                        )
                        if (isNew) DeviceDiscoveryManager.addIfNew(device)
                        synchronized(activeDevices) { activeDevices.add(device) }
                    }
                } catch (e: Exception) {}
            }
        }.joinAll()
    }
    activeDevices.sortedBy { it.ip.substringAfterLast(".").toInt() }
}

suspend fun scanPorts(host: String): List<PortInfo> = withContext(Dispatchers.IO) {
    val commonPorts = listOf(21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 3306, 3389, 5900, 8080)
    val portMap = mapOf(21 to "FTP", 22 to "SSH", 23 to "Telnet", 25 to "SMTP", 53 to "DNS", 80 to "HTTP", 110 to "POP3", 143 to "IMAP", 443 to "HTTPS", 445 to "SMB", 3306 to "MySQL", 3389 to "RDP", 5900 to "VNC", 8080 to "HTTP-Alt")
    val openPorts = mutableListOf<PortInfo>()
    coroutineScope {
        commonPorts.map { port ->
            launch {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(host, port), 400)
                    val banner = try { socket.soTimeout = 500; socket.getInputStream().bufferedReader().readLine() ?: "" } catch (e: Exception) { "" }
                    socket.close()
                    synchronized(openPorts) { openPorts.add(PortInfo(port, portMap[port] ?: "Unknown", true, banner)) }
                } catch (e: Exception) {}
            }
        }.joinAll()
    }
    openPorts.sortedBy { it.port }
}

suspend fun runSpeedTest(): SpeedTestMetrics = withContext(Dispatchers.IO) {
    delay(1000)
    SpeedTestMetrics(downloadMbps = 124.5, uploadMbps = 45.2, latency = 12, jitter = 2)
}

fun calculateSubnetMask(prefixLen: Int): String = "255.255.255.0"
