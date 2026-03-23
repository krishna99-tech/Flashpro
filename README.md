# Flash Toolkit ⚡

Flash is a powerful, "Hacker-style" Android network utility suite designed for network administrators, security enthusiasts, and power users. Built with modern Jetpack Compose and Material 3, it provides a comprehensive set of tools for network discovery, diagnostics, and remote server management.

## 📥 Download
You can download the latest version of the Flash app here:
[**Download Flash APK**](https://github.com/krishna99-tech/flashpro/releases/latest/download/app-debug.apk)

## 🚀 Features

### 1. Real-time Dashboard
* **Network Health Score:** Get an instant rating of your current connection quality.
* **Live Traffic Monitor:** Visualized network throughput and latency graphs.
* **Public Identity:** Quick view of your public IP, ISP, and geographic location.

### 2. Network Mapper (Scanner)
* **Subnet Discovery:** Automatically scan and list all devices in your local network.
* **Deep Fingerprinting:** Identifies device types (Router, Smartphone, Laptop, etc.) using MAC OUI and service banners.
* **OS Detection:** Guesses operating systems via SSH, HTTP, and VNC banner grabbing.
* **Vendor Identification:** Real-time MAC address vendor lookups.

### 3. SSH Terminal & Remote Management
* **Multi-Node Support:** Save and manage multiple remote server profiles.
* **Interactive Shell:** A custom terminal interface with specialized keypads for mobile-friendly command execution (Ctrl, Alt, Tab, Esc).
* **Remote File Explorer:** Navigate, view, and manage files on your remote servers.
* **Telemetry Monitor:** Real-time CPU and RAM utilization tracking for connected nodes.
* **Command Presets:** Execute frequently used commands with a single tap.

### 4. Connectivity Diagnostics
* **Advanced Ping:** ICMP statistics with minimum, maximum, and average round-trip times.
* **Visual Traceroute:** Map the path packets take across the internet.
* **DNS Lookup:** Resolve hostnames and inspect DNS records.

### 5. WiFi Analyzer
* **Signal Strength (RSSI):** Monitor signal quality with real-time dBm tracking.
* **Spectrum Analysis:** Identify WiFi channels, frequencies, and bands (2.4GHz/5GHz).
* **Interference Detection:** Estimate network congestion and overlapping Access Points.

### 6. Security Audit
* **Port Scanner:** Probe common ports (SSH, HTTP, FTP, etc.) for vulnerabilities.
* **Encryption Audit:** Verify WiFi protocols (WPA2/WPA3-SAE).
* **Rogue Device Alerts:** Notifies you when unknown devices appear on your network.

## 🎨 Professional Themes
Flash features a unique **Hacker Default** theme (Neon Blue/Purple on Tech-Dark) along with standard Light, Dark, and System-adaptive modes.

## 🛠️ Tech Stack
* **UI:** Jetpack Compose & Material 3
* **Navigation:** Compose Navigation
* **Networking:** Retrofit & OkHttp
* **SSH:** JSch (Java Secure Channel)
* **Database:** Room Persistence Library
* **Concurrency:** Kotlin Coroutines & Flow
* **Architecture:** MVVM (Model-View-ViewModel)



---
**Disclaimer:** Flash is intended for network administration and security auditing purposes on networks you own or have explicit permission to test. Please use responsibly.
