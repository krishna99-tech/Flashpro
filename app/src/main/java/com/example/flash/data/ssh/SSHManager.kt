package com.example.flash.data.ssh

import com.jcraft.jsch.*
import java.io.InputStream
import java.io.PrintWriter
import java.util.Properties

class SSHManager {

    private var session: Session? = null
    private var shellChannel: ChannelShell? = null
    private var shellPrintWriter: PrintWriter? = null

    fun connect(host: String, user: String, password: String, port: Int) {
        val jsch = JSch()
        val session = jsch.getSession(user, host, port)
        session.setPassword(password)

        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        config["PreferredAuthentications"] = "password,keyboard-interactive,publickey"
        // Windows SSH sometimes needs these
        config["kex"] = "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256"
        
        session.setConfig(config)
        session.connect(15000)
        this.session = session
    }

    fun openShell(): InputStream? {
        val currentSession = session ?: return null
        if (!currentSession.isConnected) return null

        val channel = currentSession.openChannel("shell") as? ChannelShell ?: return null
        shellChannel = channel
        
        val out = channel.outputStream
        shellPrintWriter = PrintWriter(out, true)
        
        channel.setPty(true)
        // vt100 is often more compatible with Windows than xterm
        channel.setPtyType("vt100")
        channel.connect(5000)
        
        return channel.inputStream
    }

    fun sendCommand(command: String) {
        try {
            val writer = shellPrintWriter
            if (writer != null && isShellActive()) {
                // Windows often expects \r\n, but PrintWriter.println uses system default.
                // We'll explicitly send \r\n for better compatibility across OS types.
                writer.print(command + "\r\n")
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun executeCommand(command: String): String {
        val currentSession = session ?: return "Error: No Session"
        if (!currentSession.isConnected) return "Error: Session Disconnected"

        return try {
            val channel = currentSession.openChannel("exec") as? ChannelExec ?: return "Error: Channel Failed"
            channel.setCommand(command)
            val input = channel.inputStream
            channel.connect(5000)
            
            val result = input.bufferedReader().use { it.readText() }
            channel.disconnect()
            stripAnsi(result)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun getSftpChannel(): ChannelSftp? {
        val currentSession = session ?: return null
        if (!currentSession.isConnected) return null

        return try {
            val channel = currentSession.openChannel("sftp") as? ChannelSftp
            channel?.connect(5000)
            channel
        } catch (e: Exception) {
            null
        }
    }

    fun isConnected(): Boolean {
        return session?.isConnected == true
    }

    fun isShellActive(): Boolean {
        return shellChannel?.isConnected == true
    }

    fun disconnect() {
        try {
            shellPrintWriter?.close()
            shellChannel?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            shellChannel = null
            shellPrintWriter = null
            session = null
        }
    }

    companion object {
        fun stripAnsi(text: String): String {
            // Regex to remove ANSI escape sequences (garbage letters)
            return text.replace("\u001B\\[[;\\d]*[A-Za-z]".toRegex(), "")
        }
    }
}

object SSHRepository {
    private val managers = mutableMapOf<String, SSHManager>()
    var currentServerId: String? = null
    
    fun getManager(serverId: String = "default"): SSHManager {
        return managers.getOrPut(serverId) { SSHManager() }
    }
    
    fun clearManager(serverId: String) {
        managers[serverId]?.disconnect()
        managers.remove(serverId)
    }
}
