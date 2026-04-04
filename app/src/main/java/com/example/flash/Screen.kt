package com.example.flash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    object NetworkMap : Screen("network_map", "Scanner", Icons.Default.Hub)
    object Diagnostics : Screen("diagnostics", "Diag", Icons.AutoMirrored.Filled.ShowChart)
    object WiFiAnalyzer : Screen("wifi_analyzer", "WiFi", Icons.Default.Wifi)
    object Security : Screen("security", "Safety", Icons.Default.Security)
    object InternetTools : Screen("internet_tools", "Global", Icons.Default.Language)
    object Analytics : Screen("analytics", "Graphs", Icons.AutoMirrored.Filled.Assignment)
    object Info : Screen("info", "Details", Icons.Default.Info)
    object Terminal : Screen("terminal", "SSH", Icons.Default.Terminal)
}
