package com.example.flash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val activeIcon: ImageVector = icon) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard, Icons.Default.Dashboard)
    object NetworkMap : Screen("network_map", "Topology", Icons.Default.Hub, Icons.Default.Hub)
    object Diagnostics : Screen("diagnostics", "Diag", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Filled.ShowChart)
    object WiFiAnalyzer : Screen("wifi_analyzer", "WiFi Radar", Icons.Default.Wifi, Icons.Default.Wifi)
    object Info : Screen("info", "Details", Icons.Default.Info, Icons.Default.Info)
    
    // System Info Screens
    object Hardware : Screen("hardware", "Hardware", Icons.Default.Memory, Icons.Default.Memory)
    object Software : Screen("software", "System", Icons.Default.Android, Icons.Default.Android)
    object Battery : Screen("battery", "Battery", Icons.Default.BatteryChargingFull, Icons.Default.BatteryChargingFull)
    object Apps : Screen("apps", "Apps", Icons.Default.Apps, Icons.Default.Apps)
    object Sensors : Screen("sensors", "Sensors", Icons.Default.Sensors, Icons.Default.Sensors)

    object SettingsTheme : Screen("settings_theme", "Theme", Icons.Default.Settings, Icons.Default.Settings)
    object DeviceInspector : Screen("device_inspector", "Inspector", Icons.Default.DeveloperBoard, Icons.Default.DeveloperBoard)
}
