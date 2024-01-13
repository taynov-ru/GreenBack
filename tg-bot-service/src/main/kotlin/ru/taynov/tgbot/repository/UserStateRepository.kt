package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.UserEspDataEntity
import ru.taynov.tgbot.entity.UserStateEntity

@Repository
interface UserStateRepository : JpaRepository<UserStateEntity, Long> {
    fun findAllBySelectedDevice(selectedDevice: String): List<UserStateEntity>
}