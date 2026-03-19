package com.example.flash.ui.terminal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flash.CommandPreset
import com.example.flash.RemoteFile
import com.example.flash.RemoteServer
import com.example.flash.ServerStats
import com.example.flash.data.db.ServerDao
import com.example.flash.data.db.toDomain
import com.example.flash.data.db.toEntity
import com.example.flash.data.ssh.SSHManager
import com.example.flash.data.ssh.SSHRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class TerminalViewModel(private val serverDao: ServerDao) : ViewModel() {

    private var activeServer: RemoteServer? = null
    private fun getSshManager() = SSHRepository.getManager(activeServer?.id ?: "default")

    var terminalText by mutableStateOf("")
        private set

    var isConnected by mutableStateOf(false)
        private set

    var isConnecting by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    val remoteFiles = mutableStateListOf<RemoteFile>()
    var currentPath by mutableStateOf("/")
    var serverStats by mutableStateOf(ServerStats())
    
    val presets = listOf(
        CommandPreset("System Update", "sudo apt update && sudo apt upgrade -y", Icons.Default.SystemUpdate),
        CommandPreset("Docker Ps", "docker ps", Icons.Default.DataUsage),
        CommandPreset("Net Stats", "netstat -tuln", Icons.Default.NetworkCheck),
        CommandPreset("Top Stats", "top -n 1 -b", Icons.Default.Speed)
    )

    val savedServers = mutableStateListOf<RemoteServer>()

    init {
        viewModelScope.launch {
            serverDao.getAllServers().collectLatest { entities ->
                savedServers.clear()
                savedServers.addAll(entities.map { it.toDomain() })
            }
        }
    }

    fun connect(server: RemoteServer) {
        if (isConnecting) return
        activeServer = server
        val manager = getSshManager()
        
        viewModelScope.launch(Dispatchers.IO) {
            isConnecting = true
            statusMessage = "Connecting to ${server.host}:${server.port}..."
            try {
                if (manager.isConnected()) manager.disconnect()
                
                manager.connect(server.host, server.user, server.password, server.port)
                val inputStream = manager.openShell() ?: throw Exception("Pty Shell Allocation Failed")
                
                withContext(Dispatchers.Main) {
                    isConnected = true
                    terminalText = "" 
                    terminalText += "--- SECURE SESSION ESTABLISHED: ${server.name.uppercase()} ---\n"
                    statusMessage = "Logged In"
                }

                val reader = InputStreamReader(inputStream)
                val buffer = CharArray(4096)
                isConnecting = false
                
                startMonitoring()
                refreshFileList("/")
                
                while (manager.isConnected()) {
                    if (inputStream.available() > 0) {
                        val count = reader.read(buffer)
                        if (count > 0) {
                            val rawData = String(buffer, 0, count)
                            val cleanData = SSHManager.stripAnsi(rawData)
                            withContext(Dispatchers.Main) {
                                terminalText = (terminalText + cleanData).takeLast(20000)
                            }
                        }
                    } else {
                        delay(20)
                    }
                    if (!manager.isShellActive()) break
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isConnected = false
                    statusMessage = "Connection Failed: ${e.localizedMessage}"
                    terminalText += "\n[SSH ERROR] ${e.message}\n"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isConnecting = false
                    isConnected = manager.isConnected()
                }
            }
        }
    }

    fun disconnect() {
        val manager = getSshManager()
        viewModelScope.launch(Dispatchers.IO) {
            manager.disconnect()
            withContext(Dispatchers.Main) {
                isConnected = false
                isConnecting = false
                terminalText += "\n--- SESSION CLOSED ---\n"
                statusMessage = "Link Terminated"
            }
        }
    }

    fun sendCommand(command: String) {
        val manager = getSshManager()
        viewModelScope.launch(Dispatchers.IO) {
            if (manager.isConnected() && manager.isShellActive()) {
                manager.sendCommand(command)
            } else {
                withContext(Dispatchers.Main) {
                    statusMessage = "Disconnected: Cannot send"
                }
            }
        }
    }

    fun addServer(server: RemoteServer) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.insertServer(server.toEntity())
        }
    }

    fun removeServer(server: RemoteServer) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.deleteServer(server.toEntity())
            withContext(Dispatchers.Main) {
                statusMessage = "Node Removed"
            }
        }
    }

    fun refreshFileList(path: String) {
        val manager = getSshManager()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sftp = manager.getSftpChannel() ?: return@launch
                val vector = sftp.ls(path)
                val newList = mutableListOf<RemoteFile>()
                
                vector.forEach {
                    val entry = it as? com.jcraft.jsch.ChannelSftp.LsEntry ?: return@forEach
                    if (entry.filename == "." || entry.filename == "..") return@forEach
                    
                    newList.add(RemoteFile(
                        name = entry.filename,
                        path = "$path/${entry.filename}".replace("//", "/"),
                        isDirectory = entry.attrs.isDir,
                        size = entry.attrs.size,
                        permissions = entry.attrs.permissionsString
                    ))
                }
                
                withContext(Dispatchers.Main) {
                    remoteFiles.clear()
                    remoteFiles.addAll(newList.sortedByDescending { it.isDirectory })
                    currentPath = path
                }
                sftp.disconnect()
            } catch (e: Exception) {}
        }
    }

    private fun startMonitoring() {
        val manager = getSshManager()
        viewModelScope.launch(Dispatchers.IO) {
            while (manager.isConnected()) {
                try {
                    // Optimized for multi-OS support
                    val cpuStr = manager.executeCommand("top -bn1 | grep 'Cpu(s)' | awk '{print $2}'")
                    val cpu = cpuStr.replace(",", ".").trim().toFloatOrNull() ?: 0f
                    val memInfo = manager.executeCommand("free -m | grep Mem")
                    val memParts = memInfo.split(Regex("\\s+"))
                    
                    if (memParts.size >= 3) {
                        val total = memParts[1].toFloatOrNull() ?: 0f
                        val used = memParts[2].toFloatOrNull() ?: 0f
                        withContext(Dispatchers.Main) {
                            serverStats = serverStats.copy(cpuUsage = cpu, ramUsed = used, ramTotal = total)
                        }
                    }
                } catch (e: Exception) {}
                delay(5000)
            }
        }
    }

    fun clearStatus() {
        statusMessage = null
    }
}
