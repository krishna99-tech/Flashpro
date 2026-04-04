package com.example.flash.ui.terminal

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flash.*
import com.example.flash.ui.SectionHeader
import com.example.flash.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FlashApplication
    val dao = app.database.serverDao()
    
    val viewModel: TerminalViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TerminalViewModel(dao) as T
            }
        }
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabInfo("Shell", Icons.Default.Terminal),
        TabInfo("Files", Icons.Default.Folder),
        TabInfo("Metrics", Icons.Default.Speed),
        TabInfo("Tasks", Icons.Default.AutoFixHigh),
        TabInfo("Nodes", Icons.Default.Dns)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.statusMessage) {
        viewModel.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Persistent Top Bar
            if (viewModel.isConnected) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 0.dp,
                            modifier = Modifier.weight(1f),
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { 
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(tab.icon, null, modifier = Modifier.size(18.dp))
                                            Text(tab.name, fontSize = 10.sp)
                                        }
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.disconnect() }) {
                            Icon(Icons.Default.PowerSettingsNew, "Disconnect", tint = Color.Red)
                        }
                    }
                }
            }

            // Main Content Area
            Box(Modifier.weight(1f)) {
                if (!viewModel.isConnected && !viewModel.isConnecting && selectedTab != 4) {
                    ServerSelector(viewModel)
                } else {
                    when (selectedTab) {
                        0 -> TerminalView(viewModel)
                        1 -> FileExplorerView(viewModel)
                        2 -> MonitorDashboardView(viewModel)
                        3 -> PresetsView(viewModel)
                        4 -> ServerSelector(viewModel)
                    }
                }
            }
        }

        // Overlays
        if (viewModel.isConnecting) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
                    Spacer(Modifier.height(24.dp))
                    Text("NEGOTIATING ENCRYPTED LINK...", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.disconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ABORT CONNECTION")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class TabInfo(val name: String, val icon: ImageVector)

@Composable
fun ServerSelector(viewModel: TerminalViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Node")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            SectionHeader("Infrastructure", "Remote Node Management")
            
            LazyColumn(Modifier.weight(1f)) {
                items(viewModel.savedServers) { server ->
                    FeatureCard(
                        title = server.name,
                        icon = Icons.Default.Dns,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.connect(server) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${server.user}@${server.host}:${server.port}", color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.removeServer(server) }) {
                                Icon(Icons.Default.DeleteSweep, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.Login, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddServerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { viewModel.addServer(it) }
        )
    }
}

@Composable
fun TerminalView(viewModel: TerminalViewModel) {
    var input by remember { mutableStateOf("") }
    var showInput by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(viewModel.terminalText) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(showInput) {
        if (showInput) {
            delay(150)
            focusRequester.requestFocus()
        }
    }

    Box(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // Terminal Window
            Box(
                Modifier.weight(1f).fillMaxWidth().background(Color.Black, RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text(
                    text = viewModel.terminalText,
                    modifier = Modifier.verticalScroll(scrollState),
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            // Integrated Input Bar (Separated from FAB to avoid overlap)
            AnimatedVisibility(
                visible = showInput,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Close Action inside the bar
                        IconButton(
                            onClick = { showInput = false },
                            modifier = Modifier.size(48.dp).background(Color.DarkGray, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }

                        OutlinedTextField(
                            value = input, onValueChange = { input = it },
                            modifier = Modifier.weight(1f).focusRequester(focusRequester),
                            placeholder = { Text("Command...", fontSize = 14.sp, color = Color.Gray) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.DarkGray
                            )
                        )

                        // "Tick" (Send) Action inside the bar
                        IconButton(
                            onClick = {
                                if (input.isNotBlank()) {
                                    viewModel.sendCommand(input)
                                    input = ""
                                    showInput = false
                                }
                            },
                            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        // Only show Keyboard FAB when input is hidden
        if (!showInput) {
            FloatingActionButton(
                onClick = { showInput = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = "Open Input")
            }
        }
    }
}

@Composable
fun FileExplorerView(viewModel: TerminalViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(viewModel.currentPath, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshFileList(viewModel.currentPath) }) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        LazyColumn(Modifier.weight(1f)) {
            items(viewModel.remoteFiles) { file ->
                ListItem(
                    headlineContent = { Text(file.name, color = MaterialTheme.colorScheme.onSurface) },
                    supportingContent = { Text("${file.permissions} | ${file.size}B", fontSize = 10.sp, color = Color.Gray) },
                    leadingContent = { 
                        Icon(
                            if(file.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                            null, tint = if(file.isDirectory) MaterialTheme.colorScheme.secondary else Color.Gray
                        ) 
                    },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.DarkGray, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.clickable { if(file.isDirectory) viewModel.refreshFileList(file.path) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun MonitorDashboardView(viewModel: TerminalViewModel) {
    val stats = viewModel.serverStats
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Health Monitor", "Telemetry from remote node")
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("CPU Utilization", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${stats.cpuUsage}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { stats.cpuUsage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Text("Physical Memory", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                val progress = if(stats.ramTotal > 0) stats.ramUsed / stats.ramTotal else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Text("${stats.ramUsed.toInt()}MB / ${stats.ramTotal.toInt()}MB", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun PresetsView(viewModel: TerminalViewModel) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Command Presets", "Rapid execution sequence")
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.presets) { preset ->
                FeatureCard(
                    title = preset.name,
                    icon = preset.icon,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { viewModel.sendCommand(preset.command) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(preset.command, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Bolt, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AddServerDialog(onDismiss: () -> Unit, onAdd: (RemoteServer) -> Unit) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deploy Target Node") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Friendly Name") }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(host, { host = it }, label = { Text("IP / Endpoint") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(port, { port = it }, label = { Text("Port") }, modifier = Modifier.width(80.dp))
                }
                OutlinedTextField(user, { user = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button({ 
                val p = port.toIntOrNull() ?: 22
                onAdd(RemoteServer(name = name, host = host, user = user, password = pass, port = p)); 
                onDismiss() 
            }) {
                Text("DEPLOY")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
