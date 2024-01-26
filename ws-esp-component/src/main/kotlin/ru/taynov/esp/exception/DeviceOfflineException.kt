package ru.taynov.esp.exception

class DeviceOfflineException(deviceId: String) : RuntimeException("Device $deviceId is offline") {
}