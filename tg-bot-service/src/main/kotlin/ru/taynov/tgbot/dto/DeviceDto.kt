package ru.taynov.tgbot.dto

import ru.taynov.tgbot.entity.UserEspDataEntity

data class DeviceDto(
    val name: String,
    val id: String,
)

fun DeviceDto.toEspDataEntity(userId: Long): UserEspDataEntity {
    return UserEspDataEntity(
        name = name, deviceId = id, userId = userId
    )
}

fun UserEspDataEntity.toDeviceDto(): DeviceDto {
    val id = this.deviceId ?: ""
    return DeviceDto(this.name ?: id, id)
}
