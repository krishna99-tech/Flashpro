package com.example.flash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.flash.ui.*
import com.example.flash.ui.theme.*
import com.example.flash.ui.terminal.TerminalScreen
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
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
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (permissionsGranted) {
                        MainAppContent(themeViewModel)
                    } else {
                        PermissionDeniedScreen {
                            val permissions = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE,
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
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val wifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        val changeWifi = ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val nearby = ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
            fineLocation && wifiState && changeWifi && nearby
        } else {
            fineLocation && wifiState && changeWifi
        }
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

    LaunchedEffect(Unit) {
        while(true) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo
            val rssi = info.rssi
            val health = when {
                rssi > -55 -> 98
                rssi > -65 -> 85
                rssi > -75 -> 65
                rssi > -85 -> 40
                else -> 10
            }
            DeviceDiscoveryManager.updateNetworkHealth(health)
            delay(5000) 
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FlashSidebar(navController, currentRoute, scope, drawerState, themeViewModel)
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = { FlashTopBar(scope, drawerState) },
            bottomBar = { FlashBottomNav(navController, currentRoute) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            ) {
                NavHost(
                    navController = navController, 
                    startDestination = Screen.Dashboard.route,
                    enterTransition = { fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 40 }) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(400)) },
                    popExitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    composable(Screen.Dashboard.route) { DashboardScreen(navController) }
                    composable(Screen.NetworkMap.route) { NodesScreen() }
                    composable(Screen.Diagnostics.route) { DiagnosticsScreen() }
                    composable(Screen.WiFiAnalyzer.route) { WifiAnalyzerScreen() }
                    composable(Screen.Security.route) { SecurityScreen() }
                    composable(Screen.InternetTools.route) { InternetToolsScreen() }
                    composable(Screen.Analytics.route) { AnalyticsScreen() }
                    composable(Screen.Info.route) { InfoScreen() }
                    composable(Screen.Terminal.route) { TerminalScreen() }
                    composable("packet_inspector") { PacketInspectorScreen() }
                    composable("settings_theme") { ThemeSettingsScreen(themeViewModel) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashTopBar(scope: CoroutineScope, drawerState: DrawerState) {
    CenterAlignedTopAppBar(
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlashOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("FLASH", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("PRO", fontWeight = FontWeight.Light, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        actions = {
            IconButton(onClick = { /* Refresh logic */ }) {
                Icon(Icons.Default.Tune, "Settings", tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun FlashBottomNav(navController: NavController, currentRoute: String?) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp)),
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(70.dp)
        ) {
            val items = listOf(
                Screen.Dashboard,
                Screen.NetworkMap,
                Screen.Security,
                Screen.InternetTools
            )

            items.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    icon = { 
                        Icon(
                            screen.icon, 
                            contentDescription = screen.label,
                            modifier = Modifier.size(if(selected) 26.dp else 22.dp)
                        ) 
                    },
                    label = { Text(screen.label, fontSize = 10.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal) },
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
fun FlashSidebar(
    navController: NavController,
    currentRoute: String?,
    scope: CoroutineScope,
    drawerState: DrawerState,
    themeViewModel: ThemeViewModel
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(0.dp, 32.dp, 32.dp, 0.dp),
        drawerTonalElevation = 12.dp
    ) {
        Column(Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Surface(
                        Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("FLASH PRO", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("INFRASTRUCTURE SUITE", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(Modifier.height(16.dp))

            val menuItems = listOf(
                SidebarItem(Screen.Dashboard.label, Screen.Dashboard.icon, Screen.Dashboard.route),
                SidebarItem("Topology", Icons.Default.Hub, Screen.NetworkMap.route),
                SidebarItem("Packet Lab", Icons.Default.Search, "packet_inspector"),
                SidebarItem("SSH Access", Icons.Default.Terminal, Screen.Terminal.route),
                SidebarItem("WiFi Radar", Icons.Default.Wifi, Screen.WiFiAnalyzer.route),
                SidebarItem("Protocol Tools", Icons.Default.Language, Screen.InternetTools.route),
                SidebarItem("Event Logs", Icons.AutoMirrored.Filled.Assignment, Screen.Analytics.route),
                SidebarItem("Settings", Icons.Default.Settings, "settings_theme"),
            )

            LazyColumn {
                items(menuItems) { item ->
                    val selected = currentRoute == item.route
                    NavigationDrawerItem(
                        label = { Text(item.label, fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium) },
                        selected = selected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(item.icon, null, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            Text(
                "v1.0.4-GOLD Edition", 
                modifier = Modifier.padding(16.dp), 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class SidebarItem(val label: String, val icon: ImageVector, val route: String)
