package ru.taynov.tgbot.dto

import ru.taynov.esp.enums.Constants
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.enums.WindowMode
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.model.Param
import ru.taynov.esp.model.Sensor
import kotlin.reflect.KClass

data class InfoCardDto(
    val sensorsAndParams: List<SensorDto>,
    val params: List<Param> = emptyList(),
    val isOnline: Boolean = false,
) {

    companion object {

        val allowedParams = listOf(
            ParamName.AUTO_CONTROL_HEAT,
            ParamName.HEATING_ENABLED,
            ParamName.ALARM_LOUD_MODE_ENABLED,
            ParamName.WINDOW_MODE,
        )

        fun EspMessage?.toInfoCardDto(): InfoCardDto {
            if (this == null) return InfoCardDto(emptyList())
            val sensorList = sensors?.map { mapToSensorDto(it) }?.toMutableList() ?: mutableListOf()
            val paramsMap = params?.associateBy { it.name } ?: emptyMap()

            val params = allowedParams.mapNotNull { paramToAdd ->
                paramsMap[paramToAdd]?.let {
                    SensorDto(name = paramToAdd.value, value = it.value.convertValue(paramToAdd.type))
                }
            }
            sensorList.addAll(params)
            return InfoCardDto(sensorList, ParamName.entries.mapNotNull { paramsMap[it] }, isOnline)
        }

        private fun mapToSensorDto(sensor: Sensor): SensorDto {
            val name = sensor.name + when (sensor.name) {
                Constants.TEMPERATURE -> " 🌡"
                Constants.VOLTAGE -> " ⚡"
                Constants.ALARM -> " ‼️"
                else -> ""
            }

            val value = sensor.value + when (sensor.name) {
                Constants.TEMPERATURE -> " °C"
                Constants.VOLTAGE -> if (sensor.value == "Отсутствует") " 🔴" else " 🟢"
                Constants.ALARM -> " ‼️"
                else -> ""
            }
            return SensorDto(name, value)
        }

        fun Int.convertValue(type: KClass<*>): String {
            return when (type) {
                Boolean::class -> if (this == 0) "Выключено 🔴" else "Включено 🟢"
                WindowMode::class -> WindowMode.valueFromOrdinal(this).value

                else -> this.toString()
            }
        }
    }
}

data class SensorDto(
    val name: String,
    val value: String
)




