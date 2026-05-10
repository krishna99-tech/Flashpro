package com.example.flash.deviceinspector.presentation.screens.modules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.flash.deviceinspector.domain.model.InfoItem
import com.example.flash.deviceinspector.domain.model.InfoSection
import com.example.flash.deviceinspector.presentation.DeviceInspectorUi
import com.example.flash.deviceinspector.presentation.components.InfoRowComposable
import com.example.flash.deviceinspector.presentation.components.InfoSectionComposable
import com.example.flash.deviceinspector.presentation.components.SignalBarsIcon
import com.example.flash.deviceinspector.presentation.nav.InspectorRoute

private data class CategoryCard(val title: String, val icon: ImageVector, val subtitle: String, val route: String)

@Composable
fun HomeGrid(ui: DeviceInspectorUi, onNavigate: (String) -> Unit) {
    val cards = listOf(
        CategoryCard("Device", Icons.Default.DeveloperBoard, "${ui.device.sumOf { it.items.size }} items", InspectorRoute.Device.route),
        CategoryCard("OS", Icons.Default.Android, "${ui.display.size} sections", InspectorRoute.Display.route),
        CategoryCard("CPU", Icons.Default.Memory, "CPU details", InspectorRoute.Cpu.route),
        CategoryCard("Memory", Icons.Default.Memory, "RAM/Storage", InspectorRoute.Battery.route),
        CategoryCard("Network", Icons.Default.Wifi, "${ui.wifiScan.size} APs", InspectorRoute.Network.route),
        CategoryCard("Telephony", Icons.Default.SettingsEthernet, "${ui.telephony.size} sections", InspectorRoute.Telephony.route),
        CategoryCard("Sensors", Icons.Default.Sensors, "${ui.sensors.size} sensors", InspectorRoute.Sensors.route),
        CategoryCard("Camera", Icons.Default.CameraAlt, "${ui.cameras.size} cameras", InspectorRoute.Camera.route),
        CategoryCard("Audio", Icons.Default.VolumeUp, "${ui.audio.sumOf { it.items.size }} items", InspectorRoute.Audio.route),
        CategoryCard("Apps", Icons.Default.Apps, "${ui.apps.size} apps", InspectorRoute.Apps.route),
        CategoryCard("Battery", Icons.Default.BatteryChargingFull, "${ui.battery.sumOf { it.items.size }} values", InspectorRoute.Battery.route),
        CategoryCard("Hardware", Icons.Default.Hardware, "${ui.features.size} features", InspectorRoute.Hardware.route)
    )
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cards) { c ->
            Card(Modifier.fillMaxWidth().height(110.dp).clickable { onNavigate(c.route) }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.material3.Icon(c.icon, null, modifier = Modifier.size(28.dp))
                    Text(c.title, fontWeight = FontWeight.Bold)
                    Text(c.subtitle, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable fun SectionList(sections: List<InfoSection>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) { items(sections) { InfoSectionComposable(it) } } }

@Composable
fun DeviceModuleScreen(sections: List<InfoSection>, cpuSections: List<InfoSection>) {
    val totalItems = sections.sumOf { it.items.size }
    val cpuUsage = cpuSections.firstOrNull()?.items?.firstOrNull { it.label.contains("Usage", true) }?.value ?: "N/A"
    val cores = cpuSections.firstOrNull()?.items?.firstOrNull { it.label.contains("Core", true) }?.value ?: "N/A"
    val manufacturer = sections.firstOrNull()?.items?.firstOrNull { it.label.contains("Manufacturer", true) }?.value ?: "N/A"
    val model = sections.firstOrNull()?.items?.firstOrNull { it.label.contains("Model", true) }?.value ?: "N/A"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.DeveloperBoard, "Items", totalItems.toString(), Modifier.weight(1f))
            SpecCard(Icons.Default.Android, "Model", model, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.Memory, "CPU Usage", cpuUsage, Modifier.weight(1f))
            SpecCard(Icons.Default.Memory, "CPU Cores", cores, Modifier.weight(1f))
        }
        SpecCard(Icons.Default.SettingsEthernet, "Manufacturer", manufacturer, Modifier.fillMaxWidth())
        SectionList(sections + cpuSections)
    }
}

@Composable
fun CpuModuleScreen(sections: List<InfoSection>) {
    val usage = sections.findValue("Usage")
    val cores = sections.findValue("Cores")
    val architecture = sections.findValue("Architecture")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.Memory, "Usage", usage, Modifier.weight(1f))
            SpecCard(Icons.Default.DeveloperBoard, "Cores", cores, Modifier.weight(1f))
        }
        SpecCard(Icons.Default.Android, "Architecture", architecture, Modifier.fillMaxWidth())
        SectionList(sections)
    }
}

@Composable
fun DisplayModuleScreen(sections: List<InfoSection>) {
    val resolution = sections.findValue("Resolution")
    val density = sections.findValue("DPI")
    val physicalSize = sections.findValue("Physical")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.DeveloperBoard, "Resolution", resolution, Modifier.weight(1f))
            SpecCard(Icons.Default.Android, "Density", density, Modifier.weight(1f))
        }
        SpecCard(Icons.Default.Hardware, "Physical Size", physicalSize, Modifier.fillMaxWidth())
        SectionList(sections)
    }
}

@Composable
fun BatteryModuleScreen(sections: List<InfoSection>) {
    val level = sections.findValue("Level")
    val temperature = sections.findValue("Temperature")
    val technology = sections.findValue("Technology")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.BatteryChargingFull, "Level", level, Modifier.weight(1f))
            SpecCard(Icons.Default.Memory, "Temperature", temperature, Modifier.weight(1f))
        }
        SpecCard(Icons.Default.Hardware, "Technology", technology, Modifier.fillMaxWidth())
        SectionList(sections)
    }
}

@Composable
fun NetworkModuleScreen(ui: DeviceInspectorUi) {
    var netTab by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = netTab) {
            listOf("Current", "Wi-Fi Scan", "Bluetooth").forEachIndexed { i, t -> Tab(selected = netTab == i, onClick = { netTab = i }, text = { Text(t) }) }
        }
        when (netTab) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        SpecCard(Icons.Default.Wifi, "Wi-Fi APs", ui.wifiScan.size.toString(), Modifier.weight(1f))
                        SpecCard(Icons.Default.SettingsEthernet, "Bluetooth", ui.bluetooth.size.toString(), Modifier.weight(1f))
                    }
                    SectionList(ui.network)
                }
            }
            1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ui.wifiScan) { ap ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(ap.ssid.ifBlank { "Hidden" }, fontWeight = FontWeight.Bold)
                                Text("${ap.bssid} | ${ap.frequency}MHz | ${ap.security}", style = MaterialTheme.typography.labelSmall)
                                Text("${ap.standard} | ${ap.channelWidth}", style = MaterialTheme.typography.labelSmall)
                            }
                            SignalBarsIcon(ap.rssi)
                        }
                    }
                }
            }
            else -> LazyColumn { items(ui.bluetooth) { Text(it, Modifier.padding(8.dp)) } }
        }
    }
}

@Composable
fun AudioModuleScreen(audio: List<InfoSection>, codecs: List<InfoSection>) {
    val totalAudioItems = audio.sumOf { it.items.size }
    val totalCodecItems = codecs.sumOf { it.items.size }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.VolumeUp, "Audio Info", totalAudioItems.toString(), Modifier.weight(1f))
            SpecCard(Icons.Default.Memory, "Codecs", totalCodecItems.toString(), Modifier.weight(1f))
        }
        SectionList(audio + codecs)
    }
}

@Composable
fun TelephonyModuleScreen(sections: List<InfoSection>) {
    val totalItems = sections.sumOf { it.items.size }
    val operator = sections.firstOrNull()?.items?.firstOrNull { it.label.contains("Operator", true) }?.value ?: "N/A"
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.SettingsEthernet, "Entries", totalItems.toString(), Modifier.weight(1f))
            SpecCard(Icons.Default.Wifi, "Operator", operator, Modifier.weight(1f))
        }
        SectionList(sections)
    }
}

@Composable
fun SensorsModuleScreen(ui: DeviceInspectorUi, onSensor: (Int) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(ui.sensors.size) { idx ->
            val s = ui.sensors[idx]
            Card(Modifier.fillMaxWidth().clickable { onSensor(idx) }) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Icon(s.icon, null); Text("  ${s.name}", fontWeight = FontWeight.Bold) }
                    Text("Vendor: ${s.vendor} | Type: ${s.type}")
                    Text("Range: ${s.maxRange}, Resolution: ${s.resolution}, Power: ${s.power}mA")
                }
            }
        }
    }
}

@Composable
fun CamerasModuleScreen(ui: DeviceInspectorUi) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(ui.cameras) { c ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Camera ${c.id} (${c.facing})", fontWeight = FontWeight.Bold)
                    Text("MP: ${c.maxMp} | Video: ${c.maxVideoResolution}")
                    Text("Focal: ${c.focalLengths}")
                    Text("Aperture: ${c.apertures}")
                    Text("OIS:${c.hasOis} Flash:${c.hasFlash} RAW:${c.supportsRaw} HDR:${c.supportsHdr}")
                }
            }
        }
    }
}

@Composable
fun AppsModuleScreen(ui: DeviceInspectorUi, onApp: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Search apps") }, modifier = Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ui.apps.filter { it.name.contains(q, true) || it.packageName.contains(q, true) }.take(200)) { app ->
                Card(Modifier.fillMaxWidth().clickable { onApp(app.packageName) }) {
                    Column(Modifier.padding(10.dp)) {
                        Text(app.name, fontWeight = FontWeight.Bold)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                        Text("v${app.versionName} | targetSdk ${app.targetSdk} | ${if (app.isSystemApp) "System" else "User"}")
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareModuleScreen(features: List<InfoItem>) {
    val availableCount = features.count { it.value.equals("Yes", true) || it.value.equals("Available", true) }
    val unavailableCount = features.count { it.value.equals("No", true) || it.value.equals("Unavailable", true) }
    val totalCount = features.size
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpecCard(Icons.Default.Hardware, "Features", totalCount.toString(), Modifier.weight(1f))
            SpecCard(Icons.Default.Android, "Available", availableCount.toString(), Modifier.weight(1f))
        }
        SpecCard(Icons.Default.Memory, "Unavailable", unavailableCount.toString(), Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(features) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) { InfoRowComposable(it) } } }
        }
    }
}

private fun List<InfoSection>.findValue(label: String): String {
    return firstNotNullOfOrNull { section ->
        section.items.firstOrNull { it.label.contains(label, ignoreCase = true) }?.value
    } ?: "N/A"
}

@Composable
private fun SpecCard(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier.heightIn(min = 84.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.padding(start = 8.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
