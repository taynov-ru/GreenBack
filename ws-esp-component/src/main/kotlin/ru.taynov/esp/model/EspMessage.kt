package ru.taynov.esp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.taynov.esp.entity.EspDataEntity


@Serializable
data class EspMessage(
    val type: String,
    val id: String,
    val sensors: List<Sensor>?,
    val params: List<Param>?,
)

@Serializable
data class Sensor(
    val name: String,
    val valueType: String,
    val value: String,
)


@Serializable
data class Param(
    val name: ParamName,
    val value: Int
)

enum class ParamName {
    AUTO_CONTROL_HEAT,
    CHECKING_VOLTAGE_ENABLED,
    HEATING_ENABLED,
    HIGH_BOUND_ALARM_TEMPERATURE,
    HIGH_BOUND_HEAT_TEMPERATURE,
    LOW_BOUND_ALARM_TEMPERATURE,
    LOW_BOUND_HEAT_TEMPERATURE,
    SILENT_MODE_ENABLED
}

fun EspDataEntity.toModel(): EspMessage =
    Json.decodeFromString(data)