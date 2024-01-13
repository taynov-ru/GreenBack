package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.model.Param

@Service
class UpdateParameterService(
    private val interactionService: InteractionService
) {

    fun inverseBooleanParameter(deviceId: String, parameter: ParamName) {
        if (parameter.type != Boolean::class) return
        val parameterValue = (interactionService.getParameterValue(deviceId, parameter) ?: 0) == 1
        val reversedValue = if (parameterValue) 0 else 1
        val newParameters = mutableSetOf(Param(parameter, reversedValue))
        disableAutoHeatControllingIfHeatingChanged(newParameters)
        interactionService.sendMessage(deviceId, SetParamsResponse(newParameters))
    }

    private fun disableAutoHeatControllingIfHeatingChanged(newParameters: MutableSet<Param>) {
        if (newParameters.firstOrNull { it.name == ParamName.HEATING_ENABLED } != null) {
            newParameters.add(Param(ParamName.AUTO_CONTROL_HEAT, 0))
        }
    }
}