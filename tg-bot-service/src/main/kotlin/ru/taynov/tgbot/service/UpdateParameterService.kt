package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.enums.WindowMode
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.enums.ModuleError

@Service
class UpdateParameterService(
    private val interactionService: InteractionService
) {

    fun inverseBooleanParameter(deviceId: String, parameter: ParamName) {
        if (parameter.type != Boolean::class) return
        val parameterValue = interactionService.getParameterValue(deviceId, parameter) == 1
        val reversedValue = if (parameterValue) 0 else 1
        val newParameters = mutableSetOf(Param(parameter, reversedValue))
        disableAutoHeatControllingIfHeatingChanged(newParameters)
        interactionService.sendMessage(deviceId, SetParamsResponse(newParameters))
    }

    fun setIntParameter(deviceId: String, parameter: ParamName, value: Int) {
        if (parameter.type != Int::class) return
        val currentParameters = interactionService.getLast(deviceId).params
        when (parameter) {
            ParamName.LOW_BOUND_ALARM_TEMPERATURE ->
                shouldLowerThan(value, ParamName.HIGH_BOUND_ALARM_TEMPERATURE, currentParameters)

            ParamName.HIGH_BOUND_ALARM_TEMPERATURE ->
                shouldGreaterThan(value, ParamName.LOW_BOUND_ALARM_TEMPERATURE, currentParameters)

            ParamName.LOW_BOUND_HEAT_TEMPERATURE ->
                shouldLowerThan(value, ParamName.HIGH_BOUND_HEAT_TEMPERATURE, currentParameters)

            ParamName.HIGH_BOUND_HEAT_TEMPERATURE ->
                shouldGreaterThan(value, ParamName.LOW_BOUND_HEAT_TEMPERATURE, currentParameters)

            else -> {}
        }
        interactionService.sendMessage(deviceId, SetParamsResponse(setOf(Param(parameter, value))))
    }

    fun setWindowModeParameter(deviceId: String, mode: WindowMode) {
        interactionService.sendMessage(deviceId, SetParamsResponse(setOf(Param(ParamName.WINDOW_MODE, mode.ordinal))))
    }

    private fun shouldLowerThan(newValue: Int, comparableParameter: ParamName, currentParameters: List<Param>) {
        if (currentParameters.getParameterValue(comparableParameter)?.let { newValue >= it } != false)
            throw ModuleError.VALUE_INCORRECT.getException("Значение должно быть меньше значения \"${comparableParameter.value}\"")
    }

    private fun shouldGreaterThan(newValue: Int, comparableParameter: ParamName, currentParameters: List<Param>) {
        if (currentParameters.getParameterValue(comparableParameter)?.let { newValue <= it } != false)
            throw ModuleError.VALUE_INCORRECT.getException("Значение должно быть больше значения \"${comparableParameter.value}\"")

    }

    private fun disableAutoHeatControllingIfHeatingChanged(newParameters: MutableSet<Param>) {
        if (newParameters.firstOrNull { it.name == ParamName.HEATING_ENABLED } != null) {
            newParameters.add(Param(ParamName.AUTO_CONTROL_HEAT, 0))
        }
    }

    private fun List<Param>.getParameterValue(param: ParamName): Int? {
        return this.firstOrNull { it.name == param }?.value
    }
}