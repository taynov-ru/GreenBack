package ru.taynov.tgbot.dto

import ru.taynov.tgbot.entity.DeviceInfoEntity

data class DeviceDto(
    val name: String,
    val id: String,
)

fun DeviceInfoEntity.toDeviceDto(): DeviceDto {
    val id = this.deviceId ?: ""
    return DeviceDto(this.name ?: id, id)
}
