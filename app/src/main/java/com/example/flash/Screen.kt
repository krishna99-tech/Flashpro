package com.example.flash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dash", "Home", Icons.Default.Dashboard)
    object NetworkMap : Screen("nodes", "Scanner", Icons.Default.Hub)
    object Diagnostics : Screen("diag", "Diag", Icons.Default.Speed)
    object WiFiAnalyzer : Screen("wifi", "WiFi", Icons.Default.SignalCellularAlt)
    object Security : Screen("security", "Safety", Icons.Default.Shield)
    object InternetTools : Screen("internet", "Global", Icons.Default.Language)
    object Analytics : Screen("analytics", "Graphs", Icons.Default.BarChart)
    object Info : Screen("info", "Details", Icons.Default.Info)
    object Terminal : Screen("terminal", "SSH", Icons.Default.Terminal)
}
