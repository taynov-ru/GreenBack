package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.model.Param
import ru.taynov.esp.service.EspInteractionService
import ru.taynov.tgbot.dto.InfoCardDto
import ru.taynov.tgbot.dto.InfoCardDto.Companion.toInfoCardDto

@Service
class InteractionService(
    private val service: EspInteractionService,
) {

    fun sendMessage(device: String, message: SetParamsResponse) {
        service.sendMessage(device, message)
    }

    fun getParameterValue(deviceId: String, param: ParamName): Int? {
        return service.getLast(deviceId)?.params?.first { it.name == param }?.value
    }

    fun getLast(device: String): InfoCardDto {
        return service.getLast(device).toInfoCardDto()
    }

    fun setAlarmCallback(callback: (deviceId: String) -> Unit) {
        service.setAlarmCallback(callback)
    }

    fun setParamsCallback(callback: (deviceId: String, params: List<Param>) -> Unit) {
        service.setParamsCallback(callback)
    }

    fun getDevices(): List<String> {
        return service.getDevices()
    }
}