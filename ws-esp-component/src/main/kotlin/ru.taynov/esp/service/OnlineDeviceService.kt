package ru.taynov.esp.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OnlineDeviceService(
    private val devicesBySessionId: ConcurrentHashMap<String, String> = ConcurrentHashMap(),
    private val sessionIdByDevice: ConcurrentHashMap<String, String> = ConcurrentHashMap()
) {

    fun isOnline(deviceId: String): Boolean {
        return sessionIdByDevice.containsKey(deviceId)
    }

    fun get(sessionId: String): String? {
        return devicesBySessionId[sessionId]
    }

    fun add(sessionId: String, deviceId: String) {
        devicesBySessionId[sessionId] = deviceId
        sessionIdByDevice[deviceId] = sessionId
    }

    fun delete(sessionId: String) {
        devicesBySessionId[sessionId]?.also { deviceId ->
            sessionIdByDevice.remove(deviceId)
            devicesBySessionId.remove(sessionId)
        }
    }


}