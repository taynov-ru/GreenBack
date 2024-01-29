package ru.taynov.esp.service

import mu.KLogger
import mu.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.entity.toEntity
import ru.taynov.esp.enums.DeviceStatus
import ru.taynov.esp.exception.DeviceOfflineException
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.model.Param
import ru.taynov.esp.model.toModel
import ru.taynov.esp.repository.EspDataRepository
import kotlin.jvm.optionals.getOrNull

@Service
class EspInteractionService(
    private val repository: EspDataRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val onlineDeviceService: OnlineDeviceService,
) {

    private val log: KLogger = KotlinLogging.logger {}
    private var alarmCallback: ((deviceId: String) -> Unit)? = null
    private var paramsCallback: ((deviceId: String, params: List<Param>) -> Unit)? = null
    private var deviceStatusCallback: ((deviceId: String, status: DeviceStatus) -> Unit)? = null

    fun sendMessage(device: String, message: SetParamsResponse) {
        if (onlineDeviceService.isOnline(device).not()) {
            log.warn { "Send to device: $device failed. Reason: Device is offline" }
            throw DeviceOfflineException(device)
        }
        log.info { "Send to $device. Message: $message" }
        messagingTemplate.convertAndSend("/topic/messages/$device", message)
        replaceParametersInSavedMessage(device, message)
    }

    fun getLast(device: String): EspMessage? {
        return repository.findDistinctById(device)?.toModel(onlineDeviceService.isOnline(device))
    }

    fun getDevices(): List<String> =
        repository.findAll().map { it.id }.distinct()

    fun setAlarmCallback(callback: (deviceId: String) -> Unit) {
        alarmCallback = callback
    }

    fun setParamsCallback(callback: (deviceId: String, params: List<Param>) -> Unit) {
        paramsCallback = callback
    }

    fun setDeviceStatusCallback(callback: (deviceId: String, status: DeviceStatus) -> Unit) {
        deviceStatusCallback = callback
    }

    fun invokeAlarmCallback(deviceId: String) {
        alarmCallback?.invoke(deviceId)
    }

    fun invokeParamsCallback(deviceId: String, params: List<Param>) {
        paramsCallback?.invoke(deviceId, params)
    }

    fun invokeDeviceStatusCallback(deviceId: String, status: DeviceStatus) {
        deviceStatusCallback?.invoke(deviceId, status)
    }

    private fun replaceParametersInSavedMessage(device: String, message: SetParamsResponse) {
        val savedMessage = repository.findById(device).getOrNull()?.toModel(onlineDeviceService.isOnline(device))
        message.params.forEach { toSave ->
            savedMessage?.params?.firstOrNull { it.name == toSave.name }?.value = toSave.value
        }
        savedMessage?.let { repository.save(it.toEntity()) }
    }
}