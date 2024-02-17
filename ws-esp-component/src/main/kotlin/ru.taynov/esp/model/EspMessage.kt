package ru.taynov.esp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.taynov.esp.entity.EspDataEntity
import ru.taynov.esp.enums.ParamName
import java.time.LocalDateTime
import java.time.ZoneOffset

@Serializable
data class EspMessage(
    val type: String,
    val id: String,
    val sensors: List<Sensor>?,
    val params: List<Param>?,
) {
    var isOnline: Boolean = true
    var timestamp: Long = 0
}

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

@Serializable
data class Command(
    val name: ParamName,
    var value: Int
)

fun EspDataEntity.toModel(isOnline: Boolean): EspMessage =
    Json.decodeFromString<EspMessage>(data)
        .apply { this.isOnline = isOnline; this.timestamp = createdAt.toEpochSecond(ZoneOffset.UTC) }