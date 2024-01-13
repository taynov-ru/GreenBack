package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.UserEspDataEntity

@Repository
interface DeviceRepository : JpaRepository<UserEspDataEntity, Long> {
    fun findAllByUserId(userId: Long): List<UserEspDataEntity>

    fun findByUserIdAndDeviceId(userId: Long, deviceId: String): UserEspDataEntity?
}
