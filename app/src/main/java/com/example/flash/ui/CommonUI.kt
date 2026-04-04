package com.example.flash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flash.ui.theme.TechBlue
import com.example.flash.ui.theme.TechPurple
import com.example.flash.ui.theme.TechSurface
import com.example.flash.ui.theme.ThemeMode
import com.example.flash.ui.theme.ThemeViewModel

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title.uppercase(),
            color = TechBlue,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = 2.sp
        )
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GppBad,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "ACCESS DENIED",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Flash requires location and network permissions to map your infrastructure security.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = TechPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("INITIALIZE PERMISSION REQUEST", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel) {
    val currentTheme by viewModel.themeMode.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Interface Matrix", "Customize your environment HUD")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(ThemeMode.entries) { mode ->
                val selected = mode == currentTheme
                Surface(
                    onClick = { viewModel.setTheme(mode) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) TechBlue.copy(alpha = 0.1f) else TechSurface,
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, TechBlue) else null
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            null,
                            tint = if (selected) TechBlue else Color.Gray
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            mode.name,
                            modifier = Modifier.weight(1f),
                            color = if (selected) Color.White else Color.Gray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (selected) {
                            Icon(Icons.Default.Check, null, tint = TechBlue)
                        }
                    }
                }
            }
        }
    }
}
