package ru.taynov.esp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.taynov.esp.entity.EspDataEntity
import ru.taynov.esp.enums.ParamName

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
    val value: String,
)

@Serializable
data class Param(
    val name: ParamName,
    var value: Int
)

fun EspDataEntity.toModel(): EspMessage = Json.decodeFromString(data)