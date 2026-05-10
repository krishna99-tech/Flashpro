package com.example.flash.deviceinspector.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flash.deviceinspector.presentation.components.PermissionBanner
import com.example.flash.deviceinspector.presentation.nav.InspectorRoute
import com.example.flash.deviceinspector.presentation.screens.detail.AppDetailScreen
import com.example.flash.deviceinspector.presentation.screens.detail.SensorDetailScreen
import com.example.flash.deviceinspector.presentation.screens.modules.*
import com.example.flash.deviceinspector.util.ExportManager
import com.example.flash.deviceinspector.util.permission.PermissionManager
import com.example.flash.deviceinspector.domain.model.InfoSection

@Composable
fun DeviceInspectorScreen(vm: DeviceInspectorViewModel = viewModel()) {
    val nav = rememberNavController()
    val ui by vm.ui.collectAsState()

    if (ui.loading) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator() }
        return
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val query = InspectorHeader(vm, ui)
        if (query.isBlank()) {
            InspectorTopTabs(nav)
        NavHost(navController = nav, startDestination = InspectorRoute.Home.route) {
            composable(InspectorRoute.Home.route) { HomeGrid(ui, onNavigate = { nav.navigate(it) }) }
            composable(InspectorRoute.Device.route) { DeviceModuleScreen(ui.device, ui.cpu) }
            composable(InspectorRoute.Cpu.route) { CpuModuleScreen(ui.cpu) }
            composable(InspectorRoute.Display.route) { DisplayModuleScreen(ui.display) }
            composable(InspectorRoute.Network.route) { NetworkModuleScreen(ui) }
            composable(InspectorRoute.Sensors.route) { SensorsModuleScreen(ui) { nav.navigate(InspectorRoute.SensorDetail.create(it)) } }
            composable(InspectorRoute.Camera.route) { CamerasModuleScreen(ui) }
            composable(InspectorRoute.Audio.route) { AudioModuleScreen(ui.audio, ui.codecs) }
            composable(InspectorRoute.Apps.route) { AppsModuleScreen(ui) { nav.navigate(InspectorRoute.AppDetail.create(it)) } }
            composable(InspectorRoute.Battery.route) { BatteryModuleScreen(ui.battery) }
            composable(InspectorRoute.Hardware.route) { HardwareModuleScreen(ui.features) }
            composable(InspectorRoute.Telephony.route) { TelephonyModuleScreen(ui.telephony) }
            composable(InspectorRoute.Export.route) { ExportModuleScreen(ui) }
                composable(InspectorRoute.SensorDetail.route, arguments = listOf(navArgument("sensorId") { type = NavType.IntType })) {
                    SensorDetailScreen(ui.sensors.getOrNull(it.arguments?.getInt("sensorId") ?: -1))
                }
                composable(InspectorRoute.AppDetail.route, arguments = listOf(navArgument("packageName") { type = NavType.StringType })) {
                    val pkg = it.arguments?.getString("packageName") ?: ""
                    AppDetailScreen(ui.apps.firstOrNull { a -> a.packageName == pkg })
                }
            }
        } else {
            SearchResultScreen(query, ui.device + ui.display + ui.cpu + ui.network + ui.audio + ui.codecs + ui.battery + ui.telephony)
        }
    }
}

@Composable
private fun InspectorHeader(vm: DeviceInspectorViewModel, ui: DeviceInspectorUi): String {
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    val missing = PermissionManager.missing(context, PermissionManager.requiredForNetwork())

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("DeviceInspector", style = MaterialTheme.typography.headlineSmall)
        Row {
            IconButton(onClick = vm::refreshAll) { Icon(Icons.Default.Refresh, null) }
        }
    }

    PermissionBanner(missing = missing, onGrant = { launcher.launch(missing.toTypedArray()) })
    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Global Search") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth())
    return query
}

@Composable
private fun InspectorTopTabs(nav: androidx.navigation.NavHostController) {
    val tabs = listOf(
        "Home" to InspectorRoute.Home.route,
        "Device" to InspectorRoute.Device.route,
        "CPU" to InspectorRoute.Cpu.route,
        "Display" to InspectorRoute.Display.route,
        "Network" to InspectorRoute.Network.route,
        "Sensors" to InspectorRoute.Sensors.route,
        "Camera" to InspectorRoute.Camera.route,
        "Audio" to InspectorRoute.Audio.route,
        "Apps" to InspectorRoute.Apps.route,
        "Battery" to InspectorRoute.Battery.route,
        "Hardware" to InspectorRoute.Hardware.route,
        "Telephony" to InspectorRoute.Telephony.route,
        "Export" to InspectorRoute.Export.route
    )
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    ScrollableTabRow(selectedTabIndex = tabs.indexOfFirst { it.second == current }.coerceAtLeast(0)) {
        tabs.forEach { (name, route) ->
            Tab(
                selected = current == route,
                onClick = { nav.navigate(route) },
                text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

@Composable
private fun ExportModuleScreen(ui: DeviceInspectorUi) {
    val context = LocalContext.current
    val sections = ui.device + ui.display + ui.cpu + ui.network + ui.audio + ui.codecs + ui.battery + ui.telephony
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Card(Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
            ExportManager.shareFile(context, ExportManager.exportJson(context, sections))
        }) {
            Text("Export JSON", Modifier.fillMaxWidth().padding(12.dp))
        }
        androidx.compose.material3.Card(Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
            ExportManager.shareFile(context, ExportManager.exportCsv(context, sections))
        }) {
            Text("Export CSV", Modifier.fillMaxWidth().padding(12.dp))
        }
        androidx.compose.material3.Card(Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
            ExportManager.shareFile(context, ExportManager.exportText(context, sections))
        }) {
            Text("Export Text", Modifier.fillMaxWidth().padding(12.dp))
        }
    }
}

@Composable
private fun SearchResultScreen(query: String, sections: List<InfoSection>) {
    val filtered = sections.mapNotNull { section ->
        val items = section.items.filter { it.label.contains(query, true) || it.value.contains(query, true) }
        if (items.isEmpty()) null else section.copy(items = items)
    }
    if (filtered.isEmpty()) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No matching system info")
        }
    } else {
        SectionList(filtered)
    }
}
