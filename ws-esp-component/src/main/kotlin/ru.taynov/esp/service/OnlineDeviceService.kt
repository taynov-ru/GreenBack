package ru.taynov.esp.service

import mu.KotlinLogging
import org.slf4j.Logger
import org.springframework.context.annotation.Lazy
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
        val messageWasRecently = secondsFromLastInteraction(deviceId).let { it != 0L && it < THRESHOLD }
        if (messageWasRecently.not() && onlineDevices.add(deviceId)) {
            log.info("Connected deviceId: $deviceId")
            espInteractionService.invokeDeviceStatusCallback(deviceId, DeviceStatus.ONLINE)
        }
    }

    @Scheduled(fixedRate = 60000) // каждую минуту
    fun cleanupSessions() {
        val forCleanup = mutableSetOf<String>()
        onlineDevices.forEach { deviceId ->

            val seconds = secondsFromLastInteraction(deviceId)
            if (seconds >= THRESHOLD) {
                forCleanup.add(deviceId)
                log.info("Disconnected deviceId: $deviceId")
                espInteractionService.invokeDeviceStatusCallback(deviceId, DeviceStatus.OFFLINE)
            }
        }
        onlineDevices.removeAll(forCleanup)
    }

    private fun secondsFromLastInteraction(deviceId: String): Long {
        val creationTime = espInteractionService.getLast(deviceId)?.timestamp?.let {
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
        }
        return creationTime?.let { ChronoUnit.SECONDS.between(it, LocalDateTime.now()) } ?: 0
    }

    companion object {
        private val log: Logger = KotlinLogging.logger { }
        private const val THRESHOLD = 300 // 5 min
    }
}