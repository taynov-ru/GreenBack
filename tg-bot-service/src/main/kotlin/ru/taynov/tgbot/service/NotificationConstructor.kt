package ru.taynov.tgbot.service

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.taynov.esp.enums.DeviceStatus
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.TelegramBot
import ru.taynov.tgbot.dto.InfoCardDto.Companion.convertValue
import ru.taynov.tgbot.dto.toDeviceDto
import ru.taynov.tgbot.repository.DeviceAssignmentRepository
import ru.taynov.tgbot.repository.DeviceRepository
import ru.taynov.tgbot.repository.UserStateRepository
import javax.annotation.PostConstruct
import kotlin.jvm.optionals.getOrNull

@Component
class NotificationConstructor(
    private val bot: TelegramBot,
    private val deviceUserAssignmentRepository: DeviceAssignmentRepository,
    private val deviceRepository: DeviceRepository,
    private val userStateRepository: UserStateRepository,
    private val interactionService: InteractionService,
) {

    @PostConstruct
    fun setCallbacks() {
        interactionService.setAlarmCallback(alarmCallback)
        interactionService.setParamsCallback(changedParamsCallback)
        interactionService.setDeviceStatusCallback(deviceStatusCallback)
    }

    private val deviceStatusCallback: ((deviceId: String, status: DeviceStatus) -> Unit) = { deviceId, status ->
        userStateRepository.findAllBySelectedDevice(deviceId).forEach {
            bot.sendQueue.add(SendMessage(it.userId.toString(), buildChangedDeviceStatusText(status)))
        }
    }

    private fun buildChangedDeviceStatusText(status: DeviceStatus): String {
        return if (status == DeviceStatus.ONLINE) "Связь с устройством восстановлена" else "Связь с устройством потеряна"
    }

    private val alarmCallback: ((deviceId: String) -> Unit) = { deviceId ->
        deviceUserAssignmentRepository.findById(deviceId).getOrNull()?.users?.forEach { chatId ->
            deviceRepository.findByUserIdAndDeviceId(chatId.toLong(), deviceId)?.toDeviceDto()?.name?.let {
                bot.sendQueue.add(SendMessage(chatId, buildAlarmText(it)))
            }
        }
    }

    private val changedParamsCallback: ((deviceId: String, params: List<Param>) -> Unit) = { deviceId, params ->
        userStateRepository.findAllBySelectedDevice(deviceId).forEach {
            bot.sendQueue.add(SendMessage(it.userId.toString(), buildChangedParamsText(params)))
        }
    }

    private fun buildChangedParamsText(params: List<Param>): String {
        return params.joinToString(separator = "\n") { "${it.name.value} ${it.value.convertValue(it.name.type)}" }
    }

    private fun buildAlarmText(deviceName: String): String {
        return """
            🚨🚨🚨
            
            Сигнал на устройстве: $deviceName
             
            🚨🚨🚨
        """.trimIndent()
    }
}