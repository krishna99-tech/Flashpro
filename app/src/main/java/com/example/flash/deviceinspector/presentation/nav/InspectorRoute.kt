package com.example.flash.deviceinspector.presentation.nav

sealed class InspectorRoute(val route: String) {
    data object Home : InspectorRoute("inspector_home")
    data object Device : InspectorRoute("inspector_device")
    data object Cpu : InspectorRoute("inspector_cpu")
    data object Display : InspectorRoute("inspector_display")
    data object Network : InspectorRoute("inspector_network")
    data object Sensors : InspectorRoute("inspector_sensors")
    data object Camera : InspectorRoute("inspector_camera")
    data object Audio : InspectorRoute("inspector_audio")
    data object Apps : InspectorRoute("inspector_apps")
    data object Battery : InspectorRoute("inspector_battery")
    data object Hardware : InspectorRoute("inspector_hardware")
    data object Telephony : InspectorRoute("inspector_telephony")
    data object Export : InspectorRoute("inspector_export")
    data object SensorDetail : InspectorRoute("sensor_detail/{sensorId}") {
        fun create(sensorId: Int) = "sensor_detail/$sensorId"
    }
    data object AppDetail : InspectorRoute("app_detail/{packageName}") {
        fun create(packageName: String) = "app_detail/$packageName"
    }
}
