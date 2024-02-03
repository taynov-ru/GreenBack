package ru.taynov.esp.service

import mu.KotlinLogging
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.taynov.esp.enums.DeviceStatus
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class OnlineDeviceService(
    private val onlineDevices: MutableSet<String> = mutableSetOf(),
    @Lazy private val espInteractionService: EspInteractionService
) {

    fun isOnline(deviceId: String): Boolean {
        return onlineDevices.contains(deviceId)
    }

    fun setOnline(deviceId: String) {
        if (onlineDevices.add(deviceId)) {
            log.info("Connected deviceId: $deviceId")
            espInteractionService.invokeDeviceStatusCallback(deviceId, DeviceStatus.ONLINE)
        }
    }

    @Scheduled(fixedRate = 60000) // каждую минуту
    fun cleanupSessions() {
        val currentTime = LocalDateTime.now()
        val forCleanup = mutableSetOf<String>()
        onlineDevices.forEach { deviceId ->
            val creationTime = espInteractionService.getLast(deviceId)?.timestamp?.let {
                LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
            }
            if (creationTime != null) {
                val minutesSinceCreation = ChronoUnit.MINUTES.between(creationTime, currentTime)
                if (minutesSinceCreation >= THRESHOLDS_MINUTES) {
                    forCleanup.add(deviceId)
                    log.info("Disconnected deviceId: $deviceId")
                    espInteractionService.invokeDeviceStatusCallback(deviceId, DeviceStatus.OFFLINE)
                }
            }
        }
        onlineDevices.removeAll(forCleanup)
    }

    companion object {
        private val log: Logger = KotlinLogging.logger { }
        private const val THRESHOLDS_MINUTES = 5
    }
}