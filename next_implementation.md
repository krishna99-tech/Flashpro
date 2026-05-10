# 🔍 DeviceInspector — Complete Android Device Info App
### Kotlin Blueprint 

---

## 📋 Table of Contents

1. [App Overview](#app-overview)
2. [All Detection Modules](#all-detection-modules)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [Data Models](#data-models)
6. [Icon Mapping Reference](#icon-mapping-reference)
7. [Permissions Required](#permissions-required)
8. [AI Build Prompt](#ai-build-prompt)
9. [Screen Flow](#screen-flow)
10. [API Reference per Module](#api-reference-per-module)

---

## App Overview

**DeviceInspector** is a full Android application in Kotlin that reads and displays every piece of detectable device information — hardware specs, installed apps, network details, sensors, media capabilities, OS info, and more — in a clean, categorized, icon-rich UI built with Jetpack Compose.

| Property       | Value                         |
|---------------|-------------------------------|
| Language       | Kotlin 1.9+                   |
| Min SDK        | API 26 (Android 8.0)          |
| Target SDK     | API 34 (Android 14)           |
| UI             | Jetpack Compose + Material 3  |
| Architecture   | MVVM + Repository             |
| No cloud sync  | Fully offline / on-device     |

---

## All Detection Modules

### 📱 Module 1 — Device Identity
| Info Item          | Android API / Method                              | Icon              |
|--------------------|--------------------------------------------------|-------------------|
| Device Name        | `Settings.Global.DEVICE_NAME`                    | `phone_android`   |
| Manufacturer       | `Build.MANUFACTURER`                             | `business`        |
| Brand              | `Build.BRAND`                                    | `label`           |
| Model              | `Build.MODEL`                                    | `devices`         |
| Product Name       | `Build.PRODUCT`                                  | `inventory_2`     |
| Hardware Name      | `Build.HARDWARE`                                 | `memory`          |
| Board              | `Build.BOARD`                                    | `developer_board` |
| Device Codename    | `Build.DEVICE`                                   | `code`            |
| Serial Number      | `Build.getSerial()` (needs READ_PRIVILEGED_PHONE_STATE or shows "unknown") | `tag` |
| Bootloader Version | `Build.BOOTLOADER`                              | `system_update`   |
| Fingerprint        | `Build.FINGERPRINT`                              | `fingerprint`     |
| Host               | `Build.HOST`                                     | `dns`             |
| Build ID           | `Build.ID`                                       | `badge`           |
| Build Type         | `Build.TYPE`                                     | `build`           |
| Build Tags         | `Build.TAGS`                                     | `local_offer`     |
| Build Time         | `Build.TIME` (formatted date)                    | `schedule`        |

---

### 🤖 Module 2 — Operating System
| Info Item              | Android API / Method                                    | Icon                |
|------------------------|--------------------------------------------------------|---------------------|
| Android Version        | `Build.VERSION.RELEASE`                                | `android`           |
| API Level              | `Build.VERSION.SDK_INT`                                | `api`               |
| Security Patch Level   | `Build.VERSION.SECURITY_PATCH`                         | `security`          |
| Codename               | `Build.VERSION.CODENAME`                               | `military_tech`     |
| Incremental Build      | `Build.VERSION.INCREMENTAL`                            | `update`            |
| Base OS                | `Build.VERSION.BASE_OS`                                | `layers`            |
| Preview SDK Int        | `Build.VERSION.PREVIEW_SDK_INT`                        | `preview`           |
| Kernel Version         | Read `/proc/version` file                              | `terminal`          |
| Root Status            | Check for `su` binary in known paths                   | `admin_panel_settings` |
| Treble Enabled         | `SystemProperties.get("ro.treble.enabled")`            | `schema`            |
| JIT Compilation        | `dalvik.vm.usejit` system property                     | `speed`             |
| ABI List               | `Build.SUPPORTED_ABIS`                                 | `list`              |
| 32-bit ABI List        | `Build.SUPPORTED_32_BIT_ABIS`                          | `list_alt`          |
| 64-bit ABI List        | `Build.SUPPORTED_64_BIT_ABIS`                          | `view_list`         |
| VM Version             | `System.getProperty("java.vm.version")`                | `coffee`            |
| VM Heap Size           | `Runtime.getRuntime().maxMemory()`                     | `storage`           |

---

### ⚙️ Module 3 — Processor / CPU
| Info Item          | Android API / Method                                     | Icon              |
|--------------------|----------------------------------------------------------|-------------------|
| CPU Architecture   | `Build.SUPPORTED_ABIS[0]`                               | `memory`          |
| CPU Governor       | Read `/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor` | `tune`        |
| CPU Min Freq       | `/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq` | `arrow_downward`  |
| CPU Max Freq       | `/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq` | `arrow_upward`    |
| CPU Cur Freq       | `/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq` | `speed`           |
| Number of Cores    | `Runtime.getRuntime().availableProcessors()`            | `grain`           |
| CPU Hardware Info  | Read `/proc/cpuinfo` → `Hardware` field                 | `developer_board` |
| CPU Model Name     | `/proc/cpuinfo` → `model name` or `Processor`          | `chip`            |
| SOC Manufacturer   | `Build.SOC_MANUFACTURER` (API 31+)                      | `factory`         |
| SOC Model          | `Build.SOC_MODEL` (API 31+)                             | `memory`          |

---

### 💾 Module 4 — Memory & Storage
| Info Item           | Android API / Method                                         | Icon              |
|---------------------|-------------------------------------------------------------|-------------------|
| Total RAM           | `ActivityManager.MemoryInfo.totalMem`                       | `memory`          |
| Available RAM       | `ActivityManager.MemoryInfo.availMem`                       | `storage`         |
| Low Memory Flag     | `ActivityManager.MemoryInfo.lowMemory`                      | `warning`         |
| Low Mem Threshold   | `ActivityManager.MemoryInfo.threshold`                      | `priority_high`   |
| Internal Total      | `Environment.getDataDirectory()` + `StatFs`                 | `storage`         |
| Internal Free       | `StatFs.getAvailableBlocksLong() * blockSize`               | `inventory`       |
| External Total      | `Environment.getExternalStorageDirectory()` + `StatFs`      | `sd_card`         |
| External Free       | External `StatFs` available                                 | `sd_card`         |
| External Removable  | `Environment.isExternalStorageRemovable()`                  | `eject`           |
| External State      | `Environment.getExternalStorageState()`                     | `info`            |

---

### 📺 Module 5 — Display / Screen
| Info Item              | Android API / Method                                          | Icon                |
|------------------------|--------------------------------------------------------------|---------------------|
| Screen Resolution      | `WindowMetrics.bounds` (API 30+) or `DisplayMetrics`        | `aspect_ratio`      |
| Screen Density (DPI)   | `DisplayMetrics.densityDpi`                                  | `density_medium`    |
| Density Bucket         | `DisplayMetrics.density` → mdpi/hdpi/xhdpi etc.             | `straighten`        |
| Physical Screen Size   | Calculated from resolution + DPI                             | `monitor`           |
| Refresh Rate           | `Display.getRefreshRate()`                                   | `refresh`           |
| Supported Refresh Rates| `Display.getSupportedRefreshRates()`                         | `speed`             |
| HDR Capabilities       | `Display.getHdrCapabilities().supportedHdrTypes`             | `hdr_on`            |
| Wide Color Gamut       | `Display.isWideColorGamut()`                                 | `palette`           |
| Rotation               | `Display.getRotation()`                                      | `screen_rotation`   |
| Pixel Format           | `PixelFormat` info from `WindowManager`                      | `grid_on`           |
| Display Cutout Type    | `DisplayCutout.boundingRects` (API 28+)                      | `crop_free`         |
| Cutout Insets          | `DisplayCutout.safeInsetTop/Bottom/Left/Right`              | `margin`            |
| Always-On Display      | Check `Settings.Secure.DOZE_ALWAYS_ON`                       | `brightness_low`    |
| Adaptive Brightness    | `Settings.System.SCREEN_BRIGHTNESS_MODE`                     | `brightness_auto`   |
| Font Scale             | `Configuration.fontScale`                                    | `text_fields`       |

#### 📐 Screen Isolation / Cutout Types (with descriptions)
| Cutout Type           | Description                          | Icon              |
|-----------------------|--------------------------------------|-------------------|
| `NONE`                | No notch or cutout                   | `crop_square`     |
| `SHORT_EDGES`         | Cutout on short edges (top/bottom)   | `crop_portrait`   |
| `LONG_EDGES`          | Cutout on long edges (left/right)    | `crop_landscape`  |
| `ROUND_CORNERS`       | Rounded corners, no cutout           | `rounded_corner`  |
| `TALL` (Notch)        | Traditional notch at top center      | `indeterminate_check_box` |
| `PUNCH_HOLE`          | Small hole-punch camera              | `radio_button_unchecked` |
| `WATERFALL`           | Curved waterfall edges               | `water`           |
| `FOLDABLE`            | Hinge area cutout (fold devices)     | `smartphone`      |

---

### 🌐 Module 6 — Network & Connectivity
| Info Item             | Android API / Method                                         | Icon              |
|-----------------------|-------------------------------------------------------------|-------------------|
| Wi-Fi Enabled         | `WifiManager.isWifiEnabled()`                               | `wifi`            |
| Wi-Fi SSID            | `WifiInfo.getSSID()` (needs ACCESS_FINE_LOCATION)           | `wifi_find`       |
| Wi-Fi BSSID           | `WifiInfo.getBSSID()`                                       | `router`          |
| Wi-Fi SSID (hidden)   | `WifiInfo.getHiddenSSID()`                                  | `wifi_password`   |
| Wi-Fi Frequency       | `WifiInfo.getFrequency()` (2.4GHz / 5GHz / 6GHz)          | `signal_cellular_alt` |
| Wi-Fi Channel Width   | `WifiInfo.getTxLinkSpeedMbps()`                             | `bandwidth`       |
| Wi-Fi IP Address      | `WifiInfo.getIpAddress()` (formatted)                       | `lan`             |
| Wi-Fi MAC Address     | `WifiInfo.getMacAddress()` (may show randomized)            | `devices`         |
| Wi-Fi Signal Strength | `WifiInfo.getRssi()` in dBm + bars                          | `signal_wifi_4_bar` |
| Wi-Fi Link Speed      | `WifiInfo.getLinkSpeed()` Mbps                              | `speed`           |
| Network Capabilities  | `NetworkCapabilities` from `ConnectivityManager`            | `hub`             |
| Mobile Data Active    | `ConnectivityManager.getActiveNetworkInfo()`                | `signal_cellular_4_bar` |
| Network Type          | `TelephonyManager.getDataNetworkType()` (2G/3G/4G/5G)      | `cell_tower`      |
| VPN Active            | `NetworkCapabilities.hasTransport(VPN)`                     | `vpn_lock`        |
| Ethernet Connected    | `NetworkCapabilities.hasTransport(ETHERNET)`                | `cable`           |
| IPv4 Address          | `NetworkInterface.getNetworkInterfaces()`                   | `dns`             |
| IPv6 Address          | `NetworkInterface` iteration                                | `dns`             |
| Network Metered       | `ConnectivityManager.isActiveNetworkMetered()`              | `data_usage`      |
| Proxy Host            | `ProxyInfo` from `ConnectivityManager`                      | `mediation`       |
| DNS Servers           | Read `/etc/resolv.conf` or `LinkProperties.dnsServers`      | `dns`             |

#### 🗺️ Network Mapper
| Scan Type             | Method                                               | Icon              |
|-----------------------|-----------------------------------------------------|-------------------|
| Nearby Wi-Fi APs      | `WifiManager.startScan()` → `getScanResults()`      | `wifi_tethering`  |
| AP SSID               | `ScanResult.SSID`                                   | `wifi`            |
| AP BSSID              | `ScanResult.BSSID`                                  | `router`          |
| AP Signal Level       | `ScanResult.level` dBm                              | `signal_wifi_4_bar` |
| AP Frequency          | `ScanResult.frequency`                              | `frequency`       |
| AP Security           | `ScanResult.capabilities`                           | `lock`            |
| AP Channel Width      | `ScanResult.channelWidth`                           | `settings_ethernet` |
| AP Wi-Fi Standard     | `ScanResult.wifiStandard` (WiFi 4/5/6/6E)          | `wifi`            |
| Connected AP Passpoint| `ScanResult.isPasspointNetwork()`                   | `verified`        |
| Bluetooth Devices     | `BluetoothAdapter.bondedDevices`                    | `bluetooth`       |
| BT Device Name        | `BluetoothDevice.name`                              | `label`           |
| BT Device Address     | `BluetoothDevice.address`                           | `devices`         |
| BT Device Class       | `BluetoothClass.getDeviceClass()`                   | `category`        |
| BT LE Scan Results    | `BluetoothLeScanner.startScan()` callback           | `bluetooth_searching` |

---

### 📲 Module 7 — Telephony & SIM
| Info Item              | Android API / Method                                        | Icon              |
|------------------------|-----------------------------------------------------------|-------------------|
| Phone Number           | `TelephonyManager.getLine1Number()` (READ_PHONE_STATE)    | `phone`           |
| IMEI                   | `TelephonyManager.getImei()` (READ_PRIVILEGED)            | `pin`             |
| MEID                   | `TelephonyManager.getMeid()`                              | `pin`             |
| Network Operator       | `TelephonyManager.getNetworkOperatorName()`               | `cell_tower`      |
| Network Country        | `TelephonyManager.getNetworkCountryIso()`                 | `flag`            |
| SIM Operator Name      | `TelephonyManager.getSimOperatorName()`                   | `sim_card`        |
| SIM Country            | `TelephonyManager.getSimCountryIso()`                     | `sim_card`        |
| SIM State              | `TelephonyManager.getSimState()`                          | `sim_card_alert`  |
| SIM Serial Number      | `TelephonyManager.getSimSerialNumber()`                   | `numbers`         |
| ICCID                  | Same as SIM serial                                        | `credit_card`     |
| Subscriber ID (IMSI)   | `TelephonyManager.getSubscriberId()`                      | `person`          |
| Phone Type             | `TelephonyManager.getPhoneType()` (GSM/CDMA/SIP)         | `phone_iphone`    |
| Call State             | `TelephonyManager.getCallState()`                         | `call`            |
| Data State             | `TelephonyManager.getDataState()`                         | `data_usage`      |
| Voice Mail Number      | `TelephonyManager.getVoiceMailNumber()`                   | `voicemail`       |
| Dual SIM               | `TelephonyManager.getPhoneCount() > 1`                    | `sim_card`        |
| Active Data SIM        | `SubscriptionManager.getDefaultDataSubscriptionId()`      | `swap_sim`        |
| Signal Strength        | `TelephonyManager.getSignalStrength()`                    | `signal_cellular_alt` |

---

### 🔬 Module 8 — Sensors
| Sensor Type                  | `Sensor.TYPE_*` Constant         | Icon                 |
|------------------------------|----------------------------------|----------------------|
| Accelerometer                | `TYPE_ACCELEROMETER`             | `moving`             |
| Gyroscope                    | `TYPE_GYROSCOPE`                 | `360`                |
| Magnetometer (Compass)       | `TYPE_MAGNETIC_FIELD`            | `explore`            |
| Proximity                    | `TYPE_PROXIMITY`                 | `sensors`            |
| Light (Ambient)              | `TYPE_LIGHT`                     | `light_mode`         |
| Barometer (Pressure)         | `TYPE_PRESSURE`                  | `compress`           |
| Temperature                  | `TYPE_AMBIENT_TEMPERATURE`       | `thermostat`         |
| Relative Humidity            | `TYPE_RELATIVE_HUMIDITY`         | `water_drop`         |
| Gravity                      | `TYPE_GRAVITY`                   | `arrow_downward`     |
| Linear Acceleration          | `TYPE_LINEAR_ACCELERATION`       | `speed`              |
| Rotation Vector              | `TYPE_ROTATION_VECTOR`           | `rotate_right`       |
| Game Rotation Vector         | `TYPE_GAME_ROTATION_VECTOR`      | `sports_esports`     |
| Geomagnetic Rotation Vector  | `TYPE_GEOMAGNETIC_ROTATION_VECTOR` | `north`            |
| Step Counter                 | `TYPE_STEP_COUNTER`              | `directions_walk`    |
| Step Detector                | `TYPE_STEP_DETECTOR`             | `footprint`          |
| Significant Motion           | `TYPE_SIGNIFICANT_MOTION`        | `directions_run`     |
| Heart Rate                   | `TYPE_HEART_RATE`                | `favorite`           |
| Heart Beat                   | `TYPE_HEART_BEAT`                | `monitor_heart`      |
| Pose 6DOF                    | `TYPE_POSE_6DOF`                 | `3d_rotation`        |
| Stationary Detect            | `TYPE_STATIONARY_DETECT`         | `do_not_disturb`     |
| Motion Detect                | `TYPE_MOTION_DETECT`             | `motion_photos_on`   |
| Low Latency Off-body Detect  | `TYPE_LOW_LATENCY_OFFBODY_DETECT`| `body_system`        |
| Hinge Angle (Fold)           | `TYPE_HINGE_ANGLE`               | `pivot_table_chart`  |
| Accelerometer Uncalibrated   | `TYPE_ACCELEROMETER_UNCALIBRATED`| `moving`             |
| Gyroscope Uncalibrated       | `TYPE_GYROSCOPE_UNCALIBRATED`    | `360`                |
| Magnetic Field Uncalibrated  | `TYPE_MAGNETIC_FIELD_UNCALIBRATED`| `explore`           |

**Per-sensor metadata to display:**
- `Sensor.getName()` — sensor name string
- `Sensor.getVendor()` — hardware vendor
- `Sensor.getVersion()` — version number
- `Sensor.getResolution()` — smallest value detectable
- `Sensor.getMaximumRange()` — max measurable value
- `Sensor.getPower()` — mA draw
- `Sensor.getMinDelay()` — fastest update interval (µs)
- `Sensor.getMaxDelay()` — slowest update interval (µs)
- `Sensor.isWakeUpSensor()` — wakes device from sleep

---

### 📷 Module 9 — Camera
| Info Item                  | Android API / Method                                          | Icon              |
|----------------------------|--------------------------------------------------------------|-------------------|
| Number of Cameras          | `CameraManager.getCameraIdList().size`                       | `camera_alt`      |
| Camera IDs                 | `CameraManager.getCameraIdList()`                            | `tag`             |
| Lens Facing                | `CameraCharacteristics.LENS_FACING` (Front/Back/External)    | `flip_camera_android` |
| Supported Resolutions      | `StreamConfigurationMap.getOutputSizes()`                    | `aspect_ratio`    |
| Max Resolution (MP)        | Computed from max output size                                | `hd`              |
| Focal Lengths              | `CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS`    | `center_focus_weak` |
| Apertures                  | `CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES`        | `lens_blur`       |
| Optical Stabilization      | `CameraCharacteristics.LENS_INFO_AVAILABLE_OIS_MODES`        | `stabilization`   |
| Digital Stabilization      | Check `CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE`      | `stabilization`   |
| Flash Available            | `CameraCharacteristics.FLASH_INFO_AVAILABLE`                 | `flash_on`        |
| Auto Focus Modes           | `CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES`           | `center_focus_strong` |
| AE Modes                   | `CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES`           | `exposure`        |
| AWB Modes                  | `CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES`          | `wb_auto`         |
| Sensor Physical Size       | `CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE`            | `crop_original`   |
| Sensor Pixel Array Size    | `CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE`         | `grid_4x4`        |
| Max Zoom Ratio             | `CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM`    | `zoom_in`         |
| Supported Video Sizes      | `StreamConfigurationMap.getOutputSizes(MediaRecorder::class.java)` | `videocam` |
| HDR Video Profiles         | `CamcorderProfile.hasProfile(QUALITY_HIGH)`                  | `hdr_video`       |
| Hardware Level             | `CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL`        | `hardware`        |
| Depth Sensor               | Check `CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES` | `blur_on`         |
| RAW Capture Support        | `CAPABILITIES_RAW` capability flag                           | `raw_on`          |
| Logical Multi-Camera       | `CAPABILITIES_LOGICAL_MULTI_CAMERA`                          | `linked_camera`   |
| Timestamp Source           | `CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE`         | `timer`           |

---

### 🔊 Module 10 — Audio
| Info Item                  | Android API / Method                                        | Icon              |
|----------------------------|-----------------------------------------------------------|-------------------|
| Output Devices             | `AudioManager.getDevices(GET_DEVICES_OUTPUTS)`            | `speaker`         |
| Input Devices              | `AudioManager.getDevices(GET_DEVICES_INPUTS)`             | `mic`             |
| Earpiece Available         | Check output devices for `TYPE_BUILTIN_EARPIECE`          | `hearing`         |
| Speaker Available          | Check for `TYPE_BUILTIN_SPEAKER`                          | `volume_up`       |
| Microphone Available       | Check input for `TYPE_BUILTIN_MIC`                        | `mic`             |
| Wired Headset Connected    | Check for `TYPE_WIRED_HEADSET`                            | `headphones`      |
| Wired Headphones Connected | Check for `TYPE_WIRED_HEADPHONES`                         | `headset`         |
| Bluetooth A2DP Connected   | Check for `TYPE_BLUETOOTH_A2DP`                           | `bluetooth_audio` |
| Bluetooth SCO Connected    | Check for `TYPE_BLUETOOTH_SCO`                            | `bluetooth`       |
| USB Audio Connected        | Check for `TYPE_USB_HEADSET` / `TYPE_USB_DEVICE`          | `usb`             |
| HDMI Audio                 | Check for `TYPE_HDMI`                                     | `tv`              |
| Dolby Atmos Support        | Check `AudioManager` extra `dolby.audio.sink.info`        | `surround_sound`  |
| Spatial Audio              | `Spatializer.isAvailable()` (API 32+)                     | `surround_sound`  |
| Audio Effects Supported    | `AudioEffect.queryEffects()` — Equalizer, Bass, Reverb    | `equalizer`       |
| Native Sample Rate         | `AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE`                | `tune`            |
| Native Frame Count         | `AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER`          | `audio_file`      |
| Low Latency Support        | `PackageManager.FEATURE_AUDIO_LOW_LATENCY`                | `fast_forward`    |
| Pro Audio Support          | `PackageManager.FEATURE_AUDIO_PRO`                        | `music_note`      |
| Music Active               | `AudioManager.isMusicActive()`                            | `library_music`   |
| Ringer Mode                | `AudioManager.getRingerMode()` (Normal/Silent/Vibrate)    | `notifications`   |
| Volume Levels              | All `STREAM_*` current/max volumes                        | `volume_up`       |

---

### 🎥 Module 11 — Video Codecs
| Info Item                  | Android API / Method                                        | Icon              |
|----------------------------|-----------------------------------------------------------|-------------------|
| All Media Codecs           | `MediaCodecList.getCodecInfos()`                          | `video_settings`  |
| Encoder List               | Filter `CodecInfo.isEncoder == true`                      | `upload`          |
| Decoder List               | Filter `CodecInfo.isEncoder == false`                     | `download`        |
| H.264 Support              | Check codec names for `avc`                               | `videocam`        |
| H.265 / HEVC Support       | Check for `hevc`                                          | `high_quality`    |
| VP8 Support                | Check for `vp8`                                           | `video_library`   |
| VP9 Support                | Check for `vp9`                                           | `video_library`   |
| AV1 Support                | Check for `av01`                                          | `hd`              |
| Dolby Vision Support       | Check for `dolby-vision`                                  | `hdr_on`          |
| HDR10 Support              | `CodecProfileLevel` entries for HDR10                     | `hdr_plus`        |
| HDR10+ Support             | HDR10Plus profile level check                             | `hdr_plus`        |
| Max Video Resolution       | `CodecCapabilities.videoCapabilities.getSupportedWidths()`| `4k`              |
| Hardware Codec             | `CodecInfo.isHardwareAccelerated()`                       | `hardware`        |
| Software Codec             | `CodecInfo.isSoftwareOnly()`                              | `code`            |

---

### 📦 Module 12 — Installed Apps
| Info Item              | Android API / Method                                            | Icon              |
|------------------------|----------------------------------------------------------------|-------------------|
| All Installed Packages | `PackageManager.getInstalledPackages(0)`                       | `apps`            |
| App Name               | `ApplicationInfo.loadLabel(pm)`                                | `label`           |
| Package Name           | `PackageInfo.packageName`                                      | `package_2`       |
| Version Name           | `PackageInfo.versionName`                                      | `new_releases`    |
| Version Code           | `PackageInfo.longVersionCode`                                  | `tag`             |
| Install Date           | `PackageInfo.firstInstallTime`                                 | `event`           |
| Last Updated Date      | `PackageInfo.lastUpdateTime`                                   | `update`          |
| APK Size               | `File(ApplicationInfo.sourceDir).length()`                     | `storage`         |
| Is System App          | `ApplicationInfo.FLAG_SYSTEM` flag check                       | `android`         |
| Is Debuggable          | `ApplicationInfo.FLAG_DEBUGGABLE` flag check                   | `bug_report`      |
| Target SDK             | `ApplicationInfo.targetSdkVersion`                             | `api`             |
| Min SDK                | `ApplicationInfo.minSdkVersion`                                | `api`             |
| Installer Package      | `PackageManager.getInstallerPackageName(pkg)`                  | `install_mobile`  |
| Declared Permissions   | `PackageInfo.requestedPermissions`                             | `security`        |
| Granted Permissions    | `PackageInfo.requestedPermissionsFlags`                        | `verified`        |
| App Icon               | `ApplicationInfo.loadIcon(pm)` → converted to `ImageBitmap`   | `apps`            |
| Is Enabled             | `ApplicationInfo.enabled`                                      | `toggle_on`       |
| Data Directory         | `ApplicationInfo.dataDir`                                      | `folder`          |
| Native Library Dir     | `ApplicationInfo.nativeLibraryDir`                             | `folder_open`     |

**Filters / Sort Options:**
- Filter: System | User-installed | Disabled
- Sort: Name A–Z | Install date | Size | Last updated
- Search bar: package name or app name

---

### ⚡ Module 13 — Battery & Power
| Info Item                | Android API / Method                                           | Icon              |
|--------------------------|---------------------------------------------------------------|-------------------|
| Battery Level            | `BatteryManager.EXTRA_LEVEL / EXTRA_SCALE`                   | `battery_full`    |
| Charging State           | `BatteryManager.EXTRA_STATUS`                                 | `battery_charging_full` |
| Charge Type              | `BatteryManager.EXTRA_PLUGGED` (AC/USB/Wireless)             | `power`           |
| Battery Health           | `BatteryManager.EXTRA_HEALTH`                                 | `health_and_safety` |
| Battery Voltage          | `BatteryManager.EXTRA_VOLTAGE` (mV)                          | `bolt`            |
| Battery Temperature      | `BatteryManager.EXTRA_TEMPERATURE` (°C)                      | `thermostat`      |
| Battery Technology       | `BatteryManager.EXTRA_TECHNOLOGY` (Li-ion etc.)              | `science`         |
| Charge Counter (µAh)     | `BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER`             | `electrical_services` |
| Current Now (µA)         | `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW`                | `electric_bolt`   |
| Current Average (µA)     | `BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE`            | `show_chart`      |
| Energy Counter (nWh)     | `BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER`             | `energy_savings_leaf` |
| Battery Capacity (mAh)   | Read from `/sys/class/power_supply/battery/charge_full_design`| `battery_5_bar`  |
| Low Power Mode           | `PowerManager.isPowerSaveMode()`                              | `battery_saver`   |
| Interactive (Screen on)  | `PowerManager.isInteractive()`                                | `phonelink`       |
| Thermal Status           | `PowerManager.getCurrentThermalStatus()` (API 29+)           | `device_thermostat` |
| Device Idle Mode         | `PowerManager.isDeviceIdleMode()`                            | `do_not_disturb`  |

---

### 🔧 Module 14 — Hardware Features
| Feature                  | PackageManager Feature String                                   | Icon              |
|--------------------------|----------------------------------------------------------------|-------------------|
| NFC                      | `FEATURE_NFC`                                                  | `nfc`             |
| NFC Beam                 | `FEATURE_NFC_BEAM`                                             | `nfc`             |
| NFC Host Card Emulation  | `FEATURE_NFC_HOST_CARD_EMULATION`                             | `credit_card`     |
| Fingerprint              | `FEATURE_FINGERPRINT`                                          | `fingerprint`     |
| Face Authentication      | `FEATURE_FACE`                                                 | `face`            |
| Iris Authentication      | `FEATURE_IRIS`                                                 | `visibility`      |
| Bluetooth                | `FEATURE_BLUETOOTH`                                            | `bluetooth`       |
| Bluetooth LE             | `FEATURE_BLUETOOTH_LE`                                         | `bluetooth`       |
| Wi-Fi                    | `FEATURE_WIFI`                                                 | `wifi`            |
| Wi-Fi Direct             | `FEATURE_WIFI_DIRECT`                                          | `wifi_tethering`  |
| Wi-Fi Aware              | `FEATURE_WIFI_AWARE`                                           | `sensors`         |
| 5G NR                    | `FEATURE_TELEPHONY_RADIO_ACCESS_5G_NR`                        | `5g`              |
| GPS / Location           | `FEATURE_LOCATION_GPS`                                         | `gps_fixed`       |
| Network Location         | `FEATURE_LOCATION_NETWORK`                                     | `location_on`     |
| USB Host                 | `FEATURE_USB_HOST`                                             | `usb`             |
| USB Accessory            | `FEATURE_USB_ACCESSORY`                                        | `usb`             |
| Microphone               | `FEATURE_MICROPHONE`                                           | `mic`             |
| Camera                   | `FEATURE_CAMERA`                                               | `camera_alt`      |
| Camera Front             | `FEATURE_CAMERA_FRONT`                                         | `camera_front`    |
| Camera Autofocus         | `FEATURE_CAMERA_AUTOFOCUS`                                     | `center_focus_strong` |
| Camera Flash             | `FEATURE_CAMERA_FLASH`                                         | `flash_on`        |
| Camera RAW               | `FEATURE_CAMERA_RAW`                                           | `raw_on`          |
| Touchscreen              | `FEATURE_TOUCHSCREEN`                                          | `touch_app`       |
| Multi-touch              | `FEATURE_TOUCHSCREEN_MULTITOUCH`                               | `pan_tool`        |
| IR Blaster               | `FEATURE_CONSUMER_IR`                                          | `settings_remote` |
| Barometer                | `FEATURE_SENSOR_BAROMETER`                                     | `compress`        |
| Compass                  | `FEATURE_SENSOR_COMPASS`                                       | `explore`         |
| Gyroscope                | `FEATURE_SENSOR_GYROSCOPE`                                     | `360`             |
| Step Counter             | `FEATURE_SENSOR_STEP_COUNTER`                                  | `directions_walk` |
| Heart Rate               | `FEATURE_SENSOR_HEART_RATE`                                    | `favorite`        |
| Hinge Angle Sensor       | `FEATURE_SENSOR_HINGE_ANGLE`                                   | `pivot_table_chart` |
| Telephony                | `FEATURE_TELEPHONY`                                            | `phone`           |
| CDMA Telephony           | `FEATURE_TELEPHONY_CDMA`                                       | `phone`           |
| VoLTE                    | `FEATURE_TELEPHONY_IMS`                                        | `hd`              |
| SIP                      | `FEATURE_SIP`                                                  | `voicemail`       |
| Ethernet                 | `FEATURE_ETHERNET`                                             | `cable`           |
| Game Controller          | `FEATURE_GAMEPAD`                                              | `sports_esports`  |
| Printing                 | `FEATURE_PRINTING`                                             | `print`           |

---

## Tech Stack

```
├── UI
│   ├── Jetpack Compose + Material 3
│   ├── Material Icons Extended (all icons used above)
│   └── Coil (app icon loading)
│
├── Data
│   ├── No database needed — all data is live from system APIs
│   ├── DataStore (cache last scan timestamp)
│   └── Kotlin Serialization (export to JSON)
│
├── DI
│   └── Hilt
│
├── Async
│   └── Kotlin Coroutines + StateFlow
│
└── Utilities
    ├── Apache Commons CSV (CSV export)
    └── Gson (JSON export)
```

---

## Architecture

```
app/
├── data/
│   └── provider/
│       ├── DeviceInfoProvider.kt
│       ├── NetworkInfoProvider.kt
│       ├── SensorInfoProvider.kt
│       ├── CameraInfoProvider.kt
│       ├── AudioInfoProvider.kt
│       ├── DisplayInfoProvider.kt
│       ├── AppListProvider.kt
│       ├── BatteryInfoProvider.kt
│       └── HardwareFeaturesProvider.kt
│
├── domain/
│   ├── model/
│   │   ├── DeviceInfo.kt
│   │   ├── NetworkInfo.kt
│   │   ├── SensorInfo.kt
│   │   ├── CameraInfo.kt
│   │   ├── AudioInfo.kt
│   │   ├── DisplayInfo.kt
│   │   ├── InstalledApp.kt
│   │   └── BatteryInfo.kt
│   └── repository/
│       └── SystemInfoRepository.kt
│
├── presentation/
│   ├── home/         (category grid)
│   ├── device/       (device + OS info)
│   ├── network/      (Wi-Fi + telephony + mapper)
│   ├── sensors/      (sensor list + live readings)
│   ├── camera/       (camera specs per lens)
│   ├── audio/        (audio devices + codecs)
│   ├── display/      (screen + cutout info)
│   ├── apps/         (installed app list)
│   ├── battery/      (power info)
│   ├── hardware/     (feature flags)
│   └── components/   (shared InfoRow, SectionCard, etc.)
│
├── di/
└── util/ (formatters, extensions)
```

---

## Data Models

```kotlin
data class InfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector,        // Material Icons
    val available: Boolean = true,
    val highlight: Boolean = false // Highlights unusual values
)

data class InfoSection(
    val title: String,
    val icon: ImageVector,
    val items: List<InfoItem>
)

data class SensorInfo(
    val name: String,
    val type: Int,
    val vendor: String,
    val version: Int,
    val resolution: Float,
    val maxRange: Float,
    val power: Float,             // mA
    val minDelay: Int,            // µs
    val maxDelay: Int,            // µs
    val isWakeUp: Boolean,
    val icon: ImageVector
)

data class InstalledApp(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val installDate: Long,
    val lastUpdated: Long,
    val apkSizeBytes: Long,
    val isSystemApp: Boolean,
    val isDebuggable: Boolean,
    val targetSdk: Int,
    val icon: Bitmap?
)

data class CameraInfo(
    val id: String,
    val facing: String,           // Front / Back / External
    val maxMp: Double,
    val focalLengths: List<Float>,
    val apertures: List<Float>,
    val hasOis: Boolean,
    val hasFlash: Boolean,
    val hardwareLevel: String,
    val maxVideoResolution: String,
    val supportsRaw: Boolean,
    val supportsHdr: Boolean
)

data class NetworkScanResult(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val security: String,
    val standard: String,         // Wi-Fi 4/5/6/6E
    val channelWidth: String
)
```

---

## Icon Mapping Reference

All icons are from `androidx.compose.material.icons.Icons` (Extended set).

```kotlin
object ModuleIcons {
    // Home screen categories
    val Device        = Icons.Default.PhoneAndroid
    val OS            = Icons.Default.Android
    val CPU           = Icons.Default.Memory
    val Memory        = Icons.Default.Storage
    val Display       = Icons.Default.Monitor
    val Network       = Icons.Default.Wifi
    val Telephony     = Icons.Default.SimCard
    val Sensors       = Icons.Default.Sensors
    val Camera        = Icons.Default.CameraAlt
    val Audio         = Icons.Default.VolumeUp
    val Video         = Icons.Default.Videocam
    val Apps          = Icons.Default.Apps
    val Battery       = Icons.Default.BatteryFull
    val Hardware      = Icons.Default.Hardware
    val NetworkMapper = Icons.Default.Map
}
```

---

## Permissions Required

```xml
<!-- AndroidManifest.xml -->

<!-- READ ONLY — no dangerous permissions needed for most info -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>       <!-- For Wi-Fi scan trigger -->
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"
    android:minSdkVersion="31"/>
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:minSdkVersion="31"/>

<!-- Location needed for SSID + Wi-Fi scan results -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

<!-- Telephony -->
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<!-- Query installed packages (Android 11+) -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
    tools:ignore="QueryAllPackagesPermission"/>

<!-- Internet for export share (no network calls needed otherwise) -->
<uses-permission android:name="android.permission.INTERNET"/>
```

**Runtime permission flow:**
1. Launch → show rationale card explaining each permission's purpose
2. Request one group at a time
3. Gracefully degrade — show "Permission required" placeholder for blocked items

---

## Screen Flow

```
Home (Category Grid — 14 cards with icons)
        │
  ┌─────┴──────────────────────────────────┐
  ▼       ▼       ▼       ▼       ▼        ▼
Device  Network  Sensors Camera  Audio  Installed
+ OS    + Mapper + Live  Specs   + AV   Apps
                  Readings       Codecs  List
  │       │
  ▼       ▼
Storage  Wi-Fi
+ CPU    Scan
         Results
```

**Home Screen Card Grid (4 × 4):**

| 📱 Device | 🤖 OS | ⚙️ CPU | 💾 Memory |
|-----------|--------|--------|-----------|
| 📺 Display | 🌐 Network | 📲 Telephony | 🔬 Sensors |
| 📷 Camera | 🔊 Audio | 🎥 Video | 📦 Apps |
| ⚡ Battery | 🔧 Hardware | 🗺️ Net Mapper | 📤 Export |

---

## AI Build Prompt

> **Copy and paste into Claude, Cursor, or any AI coding assistant to scaffold the entire project.**

---

```
You are an expert Android Kotlin developer. Build a complete Android application 
called "DeviceInspector" in Kotlin that detects and displays all system information 
about the device. Follow every instruction precisely.

=== PROJECT SETUP ===

Language: Kotlin 1.9+
Min SDK: 26 | Target SDK: 34
UI: Jetpack Compose with Material 3
Architecture: MVVM + Repository Pattern
DI: Hilt
Async: Kotlin Coroutines + StateFlow
Build: Gradle (Kotlin DSL + libs.versions.toml)

=== DEPENDENCIES ===

implementation("androidx.core:core-ktx")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")  // ALL icons
implementation("com.google.dagger:hilt-android")
kapt("com.google.dagger:hilt-compiler")
implementation("io.coil-kt:coil-compose")
implementation("androidx.datastore:datastore-preferences")
implementation("org.apache.commons:commons-csv:1.10.0")
implementation("com.google.code.gson:gson")

=== DATA LAYER ===

Create provider classes (no database, all data read live from Android APIs):

1. DeviceInfoProvider.kt
   - Read all Build.* fields
   - Read /proc/version for kernel
   - Read /proc/cpuinfo for CPU details
   - Read /sys/devices/system/cpu/* for frequency info
   - Return List<InfoSection> with DeviceIdentity and OS sections

2. DisplayInfoProvider.kt
   - Use WindowManager + DisplayMetrics
   - Detect screen resolution, DPI, density bucket
   - Get Display.getRefreshRate() and getSupportedRefreshRates()
   - Get Display.getHdrCapabilities()
   - Detect DisplayCutout type and classify it:
     NO_CUTOUT | NOTCH | PUNCH_HOLE | WATERFALL | ROUNDED_CORNERS | FOLDABLE
   - Compute physical screen size in inches from resolution + DPI
   - Return List<InfoSection>

3. NetworkInfoProvider.kt
   - WifiManager: SSID, BSSID, frequency, signal, link speed, IP, MAC
   - ConnectivityManager: active network capabilities, VPN, metered
   - TelephonyManager: network type (2G/3G/4G/5G), operator name
   - NetworkInterface: enumerate all interfaces for IPv4/IPv6
   - Return List<InfoSection>

4. WifiScanProvider.kt
   - WifiManager.startScan() with BroadcastReceiver for SCAN_RESULTS_AVAILABLE
   - Return List<NetworkScanResult> with SSID, BSSID, RSSI, frequency, 
     security (parse ScanResult.capabilities), wifiStandard (WiFi 4/5/6/6E),
     channelWidth
   - Include BluetoothAdapter.bondedDevices list
   - BluetoothLeScanner scan for nearby BLE devices (with BLUETOOTH_SCAN permission)

5. SensorInfoProvider.kt
   - SensorManager.getSensorList(Sensor.TYPE_ALL)
   - For each sensor: name, vendor, version, type, resolution, maxRange, power, 
     minDelay, maxDelay, isWakeUpSensor
   - Map each Sensor.TYPE_* to an appropriate Material icon ImageVector
   - Return List<SensorInfo>

6. CameraInfoProvider.kt
   - CameraManager.getCameraIdList() — iterate all camera IDs
   - For each camera ID, get CameraCharacteristics
   - Extract: facing (Front/Back/External), supported output sizes,
     max megapixels, focal lengths, apertures, OIS modes, flash available,
     AF modes, AE modes, AWB modes, sensor physical size, pixel array size,
     max zoom, supported video sizes, hardware level, capabilities flags
     (RAW, LOGICAL_MULTI_CAMERA, DEPTH_OUTPUT)
   - Return List<CameraInfo>

7. AudioInfoProvider.kt
   - AudioManager.getDevices(GET_DEVICES_OUTPUTS) + GET_DEVICES_INPUTS
   - List all connected audio devices with type name and icon
   - Read PROPERTY_OUTPUT_SAMPLE_RATE and PROPERTY_OUTPUT_FRAMES_PER_BUFFER
   - Check PackageManager for FEATURE_AUDIO_LOW_LATENCY, FEATURE_AUDIO_PRO
   - AudioEffect.queryEffects() — list available effects
   - Spatializer.isAvailable() (API 32+, guard with version check)
   - Get all stream volume levels (STREAM_MUSIC, ALARM, RING, etc.)
   - Return List<InfoSection>

8. VideoCodecProvider.kt
   - MediaCodecList(ALL_CODECS).getCodecInfos()
   - Group into encoders and decoders
   - For each codec: name, supported MIME types, isHardwareAccelerated, isSoftwareOnly
   - Detect H.264, H.265/HEVC, VP8, VP9, AV1, Dolby Vision, HDR10, HDR10+
   - Get max supported video resolution per codec
   - Return List<InfoSection>

9. AppListProvider.kt
   - PackageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
   - For each: name, packageName, versionName, versionCode, firstInstallTime,
     lastUpdateTime, APK size, isSystemApp, isDebuggable, targetSdkVersion,
     minSdkVersion, installerPackageName, requestedPermissions,
     requestedPermissionsFlags (which are granted), ApplicationInfo.enabled
   - Load app icon as Bitmap using ApplicationInfo.loadIcon(pm)
   - Return List<InstalledApp>

10. BatteryInfoProvider.kt
    - Register BroadcastReceiver for ACTION_BATTERY_CHANGED
    - Read all EXTRA_* fields: level, scale, status, plugged, health, voltage, 
      temperature, technology
    - BatteryManager properties: CHARGE_COUNTER, CURRENT_NOW, CURRENT_AVERAGE, 
      ENERGY_COUNTER
    - PowerManager: isPowerSaveMode, isInteractive, getCurrentThermalStatus (API 29+),
      isDeviceIdleMode
    - Attempt to read capacity from /sys/class/power_supply/battery/charge_full_design
    - Return List<InfoSection>

11. HardwareFeaturesProvider.kt
    - PackageManager.getSystemAvailableFeatures() — list ALL available features
    - Also check specific FEATURE_* constants (NFC, Fingerprint, Bluetooth, etc.)
    - Map each feature to a Material Icon
    - Show: feature name, whether it is available (boolean), icon
    - Return List<InfoItem> sorted: available first, then unavailable

12. TelephonyProvider.kt
    - TelephonyManager: line1Number, IMEI (guard with permission), network operator,
      network country, SIM operator, SIM state, phone type, data state, 
      voice mail number, signal strength, isMultiSimSupported
    - SubscriptionManager: all active subscriptions, default data/call/SMS sub IDs
    - Return List<InfoSection>

=== DOMAIN LAYER ===

Create SystemInfoRepository interface with one method per provider:
- suspend fun getDeviceInfo(): List<InfoSection>
- suspend fun getDisplayInfo(): List<InfoSection>
- suspend fun getNetworkInfo(): List<InfoSection>
- suspend fun scanWifi(): List<NetworkScanResult>
- suspend fun getSensors(): List<SensorInfo>
- suspend fun getCameras(): List<CameraInfo>
- suspend fun getAudioInfo(): List<InfoSection>
- suspend fun getVideoCodecs(): List<InfoSection>
- suspend fun getInstalledApps(): List<InstalledApp>
- suspend fun getBatteryInfo(): List<InfoSection>
- suspend fun getHardwareFeatures(): List<InfoItem>
- suspend fun getTelephonyInfo(): List<InfoSection>

Create implementation SystemInfoRepositoryImpl injecting all providers.

=== DATA MODELS ===

data class InfoItem(val label: String, val value: String, val icon: ImageVector, 
    val available: Boolean = true, val highlight: Boolean = false)
data class InfoSection(val title: String, val icon: ImageVector, val items: List<InfoItem>)
data class SensorInfo(name, type, vendor, version, resolution, maxRange, power, 
    minDelay, maxDelay, isWakeUp, liveValue: String? = null, icon: ImageVector)
data class InstalledApp(name, packageName, versionName, versionCode, installDate, 
    lastUpdated, apkSizeBytes, isSystemApp, isDebuggable, targetSdk, icon: Bitmap?)
data class CameraInfo(id, facing, maxMp, focalLengths, apertures, hasOis, hasFlash,
    hardwareLevel, maxVideoResolution, supportsRaw, supportsHdr)
data class NetworkScanResult(ssid, bssid, rssi, frequency, security, standard, channelWidth)

=== PRESENTATION LAYER ===

Create a ViewModel per screen, each exposing UiState<T>:
sealed class UiState<T> { Loading; Success(data: T); Error(msg: String) }

Screens to create:

1. HomeScreen.kt
   - 4×4 LazyVerticalGrid of category cards
   - Each card: large icon (64dp), category name, subtitle with count or key value
     (e.g. "Network" shows SSID, "Sensors" shows count)
   - Material3 ElevatedCard with ripple
   - Animated enter: staggered fade + slide from bottom

2. DeviceScreen.kt
   - LazyColumn of InfoSection composables
   - Each InfoSection: header with icon + title + count badge
   - Each InfoRow: leading icon (tinted), label, value (right-aligned)
   - Show a "HIGHLIGHT" chip on unusual values (e.g. rooted device)

3. DisplayScreen.kt
   - Same InfoSection pattern
   - Add a visual cutout type diagram: draw an SVG-style Canvas composable 
     showing the phone outline with the cutout position highlighted

4. NetworkScreen.kt
   - Tabs: "Current" | "Wi-Fi Scan" | "Bluetooth"
   - Current tab: InfoSection list
   - Wi-Fi Scan tab: 
     * Scan button triggers WifiScanProvider
     * LazyColumn of scanned APs, each card shows:
       SSID, signal bars icon (colored by RSSI), security lock icon, 
       frequency badge (2.4GHz / 5GHz / 6GHz), Wi-Fi standard badge
     * Sort by signal strength descending
   - Bluetooth tab: bonded + scanned BLE devices list

5. SensorsScreen.kt
   - LazyColumn of sensors
   - Each SensorCard: icon, name, vendor, resolution, power (mA), wakeup chip
   - Tap a sensor to open SensorDetailScreen with live readings:
     * Register SensorEventListener for that sensor
     * Show real-time values with animated number changes
     * Show a simple line graph of last 50 readings (Canvas composable)

6. CameraScreen.kt
   - Top: camera count + camera switcher tabs (Back / Front / External)
   - Per camera: specs grid (max MP, sensor size, max zoom, hardware level)
   - Supported resolutions: sortable list
   - Focal lengths and apertures shown as chips
   - Capabilities chips: OIS, Flash, RAW, HDR, Multi-Camera

7. AudioScreen.kt
   - Tabs: "Devices" | "Codecs" | "Volume"
   - Devices: list of all audio input/output devices with type icon
   - Codecs: video codec list (encoder/decoder) with HW/SW badge
   - Volume: sliders (read-only) for all stream types showing current/max

8. AppsScreen.kt
   - SearchBar at top
   - Filter chips: All | User | System | Disabled
   - Sort menu: Name | Install Date | Size | Updated
   - LazyColumn of AppCard:
     * App icon (Coil), app name, package name
     * Version, install date, APK size formatted
     * System badge (if system app), Debug badge (if debuggable)
   - Tap to open AppDetailScreen:
     * Full permission list split into Granted / Denied
     * Full metadata table

9. BatteryScreen.kt
   - Large animated battery icon at top (fill level animated)
   - Circular progress showing percentage
   - InfoSection list below
   - Thermal status chip (None / Light / Moderate / Severe / Critical / Emergency)
   - Power save mode warning card

10. HardwareScreen.kt
    - Two sections: "Available" | "Not Available"
    - Each feature: icon, feature name, available/unavailable chip
    - Available features shown with green icon, unavailable with grey

11. ExportScreen.kt
    - Choose format: JSON | CSV | Text
    - Choose sections (multi-select checkboxes)
    - Export button → create file in Downloads → share via ShareSheet

=== SHARED COMPOSABLES ===

Create in presentation/components/:

InfoRowComposable.kt:
@Composable fun InfoRow(icon: ImageVector, label: String, value: String, 
    highlight: Boolean = false, tint: Color = MaterialTheme.colorScheme.primary)
- Row with: leading Icon(24dp) → Column(label text 12sp secondary, value text 14sp) → trailing value text
- If highlight = true, show yellow background tint on the row

InfoSectionComposable.kt:
@Composable fun InfoSection(section: InfoSection)
- ElevatedCard with: header row (icon + title + count chip) + Divider + 
  Column of InfoRow composables

StatusChip.kt:
@Composable fun StatusChip(text: String, available: Boolean)
- SuggestionChip with green check icon if available, grey X icon if not

SignalBarsIcon.kt:
@Composable fun SignalBarsIcon(rssi: Int)  // -30 excellent → -90 poor
- Draw 4 bars on Canvas with color: green/yellow/orange/red based on RSSI

PermissionRationaleDialog.kt:
- Show dialog explaining why a permission is needed before requesting it

=== NAVIGATION ===

NavHost with routes:
home, device, display, network, sensors, sensor_detail/{sensorId},
camera, audio, apps, app_detail/{packageName}, battery, hardware, export

TopAppBar on all screens with back arrow (except home).
Home uses CenterAlignedTopAppBar with search icon (searches across all info).

=== GLOBAL SEARCH ===

On HomeScreen, search icon opens a SearchBar (Material3).
Search queries all InfoItems from all loaded sections.
Show results grouped by section with a section header chip.

=== EXPORT FEATURE ===

ExportManager.kt:
- JSON: Gson → serialize all sections to JSON → write to Downloads/DeviceInspector_<date>.json
- CSV: Apache Commons CSV → flatten all InfoItems → write to CSV
- Text: plain formatted text report
- Share via FileProvider + Intent.ACTION_SEND

=== THEMING ===

MaterialTheme with:
- Dynamic color (wallpaper-based) on API 31+
- Static seed color #1B6CA8 (blue) on older devices
- Dark mode: follow system
- Custom typography: use Roboto Mono for values (technical data) + 
  default Roboto for labels

=== PERMISSION HANDLING ===

PermissionManager.kt:
- Track which permissions are granted
- For each screen, check required permissions on enter
- If missing, show PermissionBanner composable at top of screen
- PermissionBanner: icon, explanation text, "Grant" button → launches request

=== ERROR HANDLING ===

All providers must wrap reads in try-catch.
If a value cannot be read: return InfoItem with value = "Unavailable" and available = false.
Never crash on SecurityException or UnsupportedOperationException.

=== PERFORMANCE ===

- All provider calls run on Dispatchers.IO
- ViewModels cache results in StateFlow — don't re-read on recomposition
- App icons loaded lazily with Coil (paginated in groups of 20)
- Sensors screen uses LaunchedEffect to start/stop SensorEventListener 
  based on lifecycle

=== FILE STRUCTURE ===

com.deviceinspector/
├── data/provider/          (12 provider classes)
├── domain/model/           (data classes)
├── domain/repository/      (interface + impl)
├── presentation/
│   ├── home/
│   ├── device/
│   ├── display/
│   ├── network/
│   ├── sensors/
│   ├── camera/
│   ├── audio/
│   ├── apps/
│   ├── battery/
│   ├── hardware/
│   ├── export/
│   └── components/
├── di/                     (Hilt modules)
├── util/                   (formatters, extensions, PermissionManager)
└── MainActivity.kt

Generate ALL files completely. Every screen must be fully implemented 
with real Android API calls. No placeholder TODO comments. 
Every icon must be from Icons.Default.* or Icons.Outlined.* (material-icons-extended).
```

---

## Development Order

1. Project setup + Hilt wiring
2. Data models + InfoItem/InfoSection structures
3. All provider classes (start with DeviceInfoProvider — no permissions needed)
4. Repository + ViewModels
5. Shared composables (InfoRow, InfoSection, StatusChip)
6. HomeScreen grid
7. Device + OS screen
8. Display screen + cutout diagram
9. Battery screen
10. Hardware features screen
11. Sensors screen + live readings
12. Camera screen
13. Network screen + Wi-Fi scanner
14. Audio + Video codecs screen
15. Installed Apps screen (most complex — lazy loading)
16. Export screen
17. Global search
18. Permission handling polish
19. Dark mode + dynamic color
20. Final QA pass — verify graceful degradation for all missing permissions

---
