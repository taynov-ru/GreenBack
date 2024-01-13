package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.DeviceInfoEntity

@Repository
interface DeviceRepository : JpaRepository<DeviceInfoEntity, Long> {
    fun findAllByUserId(userId: Long): List<DeviceInfoEntity>

    fun findByUserIdAndDeviceId(userId: Long, deviceId: String): DeviceInfoEntity?
}
