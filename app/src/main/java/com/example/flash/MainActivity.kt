package com.example.flash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flash.data.preferences.ThemePreferences
import com.example.flash.deviceinspector.presentation.DeviceInspectorScreen
import com.example.flash.ui.DashboardScreen
import com.example.flash.ui.DiagnosticsScreen
import com.example.flash.ui.InfoScreen
import com.example.flash.ui.NodesScreen
import com.example.flash.ui.PermissionDeniedScreen
import com.example.flash.ui.ThreeJsView
import com.example.flash.ui.theme.FlashTheme
import com.example.flash.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val toolsViewModel: ToolsViewModel = viewModel()
            val themeViewModel: ThemeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ThemeViewModel(ThemePreferences(applicationContext)) as T
                    }
                }
            )
            val themeMode by themeViewModel.themeMode.collectAsState()

            FlashTheme(themeMode = themeMode) {
                MainAppContent(toolsViewModel, themeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(toolsViewModel: ToolsViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val navigationManager = remember { NavigationManager(navController) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    val primaryItems = remember {
        listOf(
            Screen.Dashboard,
            Screen.NetworkMap,
            Screen.Diagnostics,
            Screen.Info,
            Screen.DeviceInspector
        )
    }
    val drawerItems = remember {
        primaryItems + Screen.SettingsTheme
    }
    val allItems = remember {
        drawerItems + listOf(
            Screen.Hardware,
            Screen.Software,
            Screen.Battery,
            Screen.Apps,
            Screen.Sensors,
            Screen.WiFiAnalyzer
        )
    }

    var hasPermissions by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    if (!hasPermissions) {
        PermissionDeniedScreen {
            val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            launcher.launch(perms)
        }
        return
    }

    LaunchedEffect(Unit) {
        toolsViewModel.startSystemMonitoring(context)
        toolsViewModel.fetchStaticSystemInfo(context)
        toolsViewModel.fetchIpInfo(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text("Flash Control Center", style = MaterialTheme.typography.titleLarge)
                    Text("Production Network Toolkit", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navigateTopLevel(navController, screen.route)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        Text(allItems.firstOrNull { it.route == currentRoute }?.title ?: "Flash")
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Screen.SettingsTheme.icon, contentDescription = "Open sidebar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                if (screenWidthDp < 700) {
                    BottomNavigationBar(navController, primaryItems)
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                ThreeJsView(Modifier.fillMaxSize())
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    enterTransition = { fadeIn() + slideInVertically(initialOffsetY = { 24 }) },
                    exitTransition = { fadeOut() },
                    popEnterTransition = { fadeIn() },
                    popExitTransition = { fadeOut() }
                ) {
                    composable(
                        route = Screen.Dashboard.route,
                        enterTransition = { scaleIn(initialScale = 0.96f) + fadeIn() },
                        exitTransition = { scaleOut(targetScale = 1.02f) + fadeOut() }
                    ) { DashboardScreen(navigationManager, toolsViewModel) }
                    composable(
                        route = Screen.NetworkMap.route,
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
                    ) { NodesScreen() }
                    composable(Screen.Diagnostics.route) { DiagnosticsScreen(toolsViewModel) }
                    composable(Screen.Info.route) { InfoScreen(toolsViewModel, navigationManager) }
                    composable(Screen.Hardware.route) { com.example.flash.ui.HardwareScreen(toolsViewModel) }
                    composable(Screen.Software.route) { com.example.flash.ui.SoftwareScreen() }
                    composable(Screen.Battery.route) { com.example.flash.ui.BatteryScreen(toolsViewModel) }
                    composable(Screen.Apps.route) { com.example.flash.ui.AppsScreen(toolsViewModel) }
                    composable(Screen.Sensors.route) { com.example.flash.ui.SensorsScreen(toolsViewModel) }
                    composable(Screen.WiFiAnalyzer.route) { com.example.flash.ui.WifiAnalyzerScreen(toolsViewModel) }
                    composable(Screen.DeviceInspector.route) { DeviceInspectorScreen() }
                    composable(Screen.SettingsTheme.route) { com.example.flash.ui.ThemeSettingsScreen(themeViewModel) }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, items: List<Screen>) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(if (selected) screen.activeIcon else screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                selected = selected,
                onClick = {
                    navigateTopLevel(navController, screen.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

private fun navigateTopLevel(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

class NavigationManager(private val navController: NavController) {
    fun navigate(screen: Screen) {
        navController.navigate(screen.route)
    }

    fun goBack() {
        navController.popBackStack()
    }
}
