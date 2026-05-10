package com.example.flash.deviceinspector.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.flash.deviceinspector.domain.model.InfoItem
import com.example.flash.deviceinspector.domain.model.InfoSection

@Composable
fun InfoRowComposable(item: InfoItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.highlight) Color(0x33FFD54F) else Color.Transparent)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text("  ${item.label}", style = MaterialTheme.typography.labelMedium)
        }
        Text(item.value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun InfoSectionComposable(section: InfoSection) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(section.icon, contentDescription = null)
                Text(section.title, style = MaterialTheme.typography.titleMedium)
                StatusChip(text = section.items.size.toString(), available = true)
            }
            section.items.forEach { InfoRowComposable(it) }
        }
    }
}

@Composable
fun StatusChip(text: String, available: Boolean) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        leadingIcon = { Icon(if (available) Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null) }
    )
}

@Composable
fun SignalBarsIcon(rssi: Int) {
    val color = when {
        rssi >= -55 -> Color(0xFF2E7D32)
        rssi >= -70 -> Color(0xFFF9A825)
        rssi >= -80 -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }
    Canvas(Modifier.size(20.dp)) {
        val w = size.width / 5f
        for (i in 1..4) {
            drawRect(
                color = if (i * -15 - 30 >= rssi) color else color.copy(alpha = 0.25f),
                topLeft = androidx.compose.ui.geometry.Offset(i * w, size.height - i * (size.height / 4f)),
                size = androidx.compose.ui.geometry.Size(w, i * (size.height / 4f))
            )
        }
    }
}
