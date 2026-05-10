package com.example.flash.deviceinspector.presentation.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flash.deviceinspector.domain.model.InstalledApp
import com.example.flash.deviceinspector.domain.model.SensorInfo

@Composable
fun SensorDetailScreen(sensor: SensorInfo?) {
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.fillMaxSize()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sensor Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (sensor == null) {
                    Text("Sensor not found")
                } else {
                    Text("Name: ${sensor.name}")
                    Text("Vendor: ${sensor.vendor}")
                    Text("Type: ${sensor.type}")
                    Text("Resolution: ${sensor.resolution}")
                    Text("Max Range: ${sensor.maxRange}")
                    Text("Power: ${sensor.power} mA")
                    Text("WakeUp: ${sensor.isWakeUp}")
                }
            }
        }
    }
}

@Composable
fun AppDetailScreen(app: InstalledApp?) {
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.fillMaxSize()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("App Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (app == null) {
                    Text("App not found")
                } else {
                    Text("Name: ${app.name}")
                    Text("Package: ${app.packageName}")
                    Text("Version: ${app.versionName}")
                    Text("Version Code: ${app.versionCode}")
                    Text("Target SDK: ${app.targetSdk}")
                    Text("Min SDK: ${app.minSdk}")
                    Text("System App: ${app.isSystemApp}")
                    Text("Debuggable: ${app.isDebuggable}")
                    Text("APK Size: ${app.apkSizeBytes}")
                }
            }
        }
    }
}
