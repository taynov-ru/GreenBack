package ru.taynov.esp.service

import org.springframework.stereotype.Service
import ru.taynov.esp.entity.toEntity
import ru.taynov.esp.enums.Constants.Companion.ALARM
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.model.Sensor
import ru.taynov.esp.repository.EspDataRepository

@Service
class MessageProcessor(
    private val espInteractionService: EspInteractionService,
    private val dataRepository: EspDataRepository,
) {

    private val params = listOf(
        ParamName.HEATING_ENABLED,
        ParamName.AUTO_CONTROL_HEAT,
        ParamName.CHECKING_VOLTAGE_ENABLED,
        ParamName.SILENT_MODE_ENABLED
    )

    fun process(message: EspMessage) {
        runCatching { checkMessageContainsAlarm(message) }
        runCatching { checkChangedParams(message) }
        saveMessage(message)
    }

    private fun checkChangedParams(message: EspMessage) {
        val lastStateOfParams = espInteractionService.getLast(message.id)?.params ?: emptyList()
        if (lastStateOfParams.isNotEmpty()) {
            message.params?.filter { it.name in params && lastStateOfParams.first { last -> last.name == it.name }.value != it.value }
                ?.takeIf { it.isNotEmpty() }?.let {
                    espInteractionService.invokeParamsCallback(message.id, it)
                }
        }
    }

    private fun checkMessageContainsAlarm(message: EspMessage) {
        val alarmWasBefore = espInteractionService.getLast(message.id)?.sensors.containsAlarm()

        if (alarmWasBefore.not() && message.sensors.containsAlarm()) {
            espInteractionService.invokeAlarmCallback(message.id)
        }
    }

    private fun saveMessage(message: EspMessage) {
        dataRepository.save(message.toEntity())
    }

    private fun List<Sensor>?.containsAlarm(): Boolean {
        return this?.firstOrNull { it.name == ALARM } != null
    }
}