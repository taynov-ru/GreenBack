package ru.taynov.esp.service

import mu.KLogger
import mu.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.entity.toEntity
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.model.Param
import ru.taynov.esp.model.toModel
import ru.taynov.esp.repository.EspDataRepository
import kotlin.jvm.optionals.getOrNull

@Service
class EspInteractionService(
    val repository: EspDataRepository,
    val messagingTemplate: SimpMessagingTemplate,
) {

    private val log: KLogger = KotlinLogging.logger {}
    private var alarmCallback: ((deviceId: String) -> Unit)? = null
    private var paramsCallback: ((deviceId: String, params: List<Param>) -> Unit)? = null

    fun sendMessage(device: String, message: SetParamsResponse) {
        log.info { "Send to $device. Message: $message" }
        messagingTemplate.convertAndSend("/topic/messages/$device", message)
        replaceParametersInSavedMessage(device, message)
    }

    fun getLast(device: String): EspMessage? {
        return repository.findDistinctById(device)?.toModel()
    }

    fun getDevices(): List<String> =
        repository.findAll().map { it.id }.distinct()

    fun setAlarmCallback(callback: (deviceId: String) -> Unit) {
        alarmCallback = callback
    }

    fun setParamsCallback(callback: (deviceId: String, params: List<Param>) -> Unit) {
        paramsCallback = callback
    }

    fun invokeAlarmCallback(deviceId: String) {
        alarmCallback?.invoke(deviceId)
    }

    fun invokeParamsCallback(deviceId: String, params: List<Param>) {
        paramsCallback?.invoke(deviceId, params)
    }

    private fun replaceParametersInSavedMessage(device: String, message: SetParamsResponse) {
        val savedMessage = repository.findById(device).getOrNull()?.toModel()
        message.params.forEach { toSave ->
            savedMessage?.params?.first { it.name == toSave.name }?.value = toSave.value
        }
        savedMessage?.let { repository.save(it.toEntity()) }
    }
}