package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.enums.WindowMode
import ru.taynov.esp.model.Command
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.validator.ValidationService

@Service
class UpdateParameterService(
    private val interactionService: InteractionService,
    private val validationService: ValidationService
) {

    fun inverseBooleanParameter(deviceId: String, parameter: ParamName) {
        if (parameter.type != Boolean::class) return
        val parameterValue = interactionService.getParameterValue(deviceId, parameter) == 1
        val reversedValue = if (parameterValue) 0 else 1
        val newParameters = mutableSetOf(Param(parameter, reversedValue))
        disableAutoHeatControllingIfHeatingChanged(newParameters)
        interactionService.sendMessage(deviceId, SetParamsResponse(params = newParameters))
    }

    fun setIntParameter(deviceId: String, parameter: ParamName, value: Int) {
        if (parameter.type != Int::class) return
        validationService.validateIntParameter(deviceId, parameter, value)
        interactionService.sendMessage(deviceId, SetParamsResponse(params = setOf(Param(parameter, value))))
    }

    fun setWindowModeParameter(deviceId: String, mode: WindowMode) {
        val commands = setOf(
            Command(ParamName.WINDOW_MODE, mode.ordinal),
        )
        interactionService.sendMessage(deviceId, SetParamsResponse(commands = commands))
    }

    private fun disableAutoHeatControllingIfHeatingChanged(newParameters: MutableSet<Param>) {
        if (newParameters.firstOrNull { it.name == ParamName.HEATING_ENABLED } != null) {
            newParameters.add(Param(ParamName.AUTO_CONTROL_HEAT, 0))
        }
    }
}