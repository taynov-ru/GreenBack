package ru.taynov.tgbot.validator

import org.springframework.stereotype.Component
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.service.InteractionService

@Component
class ValidationService(
    private val interactionService: InteractionService
) {

    fun validateDeviceId(text: String) {
        val devices = interactionService.getDevices()
        if (!devices.contains(text)) throw ModuleError.UNKNOWN_DEVICE.getException()
    }

    fun validateIntParameter(deviceId: String, parameter: ParamName, value: Int) {
        val currentParameters = interactionService.getLast(deviceId).params
        when (parameter) {
            ParamName.LOW_BOUND_ALARM_TEMPERATURE ->
                shouldLessThan(value, ParamName.HIGH_BOUND_ALARM_TEMPERATURE, currentParameters)

            ParamName.HIGH_BOUND_ALARM_TEMPERATURE ->
                shouldGreaterThan(value, ParamName.LOW_BOUND_ALARM_TEMPERATURE, currentParameters)

            ParamName.LOW_BOUND_HEAT_TEMPERATURE ->
                shouldLessThan(value, ParamName.HIGH_BOUND_HEAT_TEMPERATURE, currentParameters)

            ParamName.HIGH_BOUND_HEAT_TEMPERATURE ->
                shouldGreaterThan(value, ParamName.LOW_BOUND_HEAT_TEMPERATURE, currentParameters)

            ParamName.OPEN_WINDOW_TEMPERATURE ->
                shouldGreaterThan(value, ParamName.CLOSE_WINDOW_TEMPERATURE, currentParameters)

            ParamName.CLOSE_WINDOW_TEMPERATURE ->
                shouldLessThan(value, ParamName.OPEN_WINDOW_TEMPERATURE, currentParameters)

            else -> {}
        }
    }

    private fun shouldLessThan(newValue: Int, comparableParameter: ParamName, currentParameters: List<Param>) {
        if (currentParameters.getParameterValue(comparableParameter)?.let { newValue >= it } != false)
            throw ModuleError.VALUE_INCORRECT.getException("Значение должно быть меньше значения \"${comparableParameter.value}\"")
    }

    private fun shouldGreaterThan(newValue: Int, comparableParameter: ParamName, currentParameters: List<Param>) {
        if (currentParameters.getParameterValue(comparableParameter)?.let { newValue <= it } != false)
            throw ModuleError.VALUE_INCORRECT.getException("Значение должно быть больше значения \"${comparableParameter.value}\"")
    }

    private fun List<Param>.getParameterValue(param: ParamName): Int? {
        return this.firstOrNull { it.name == param }?.value
    }
}