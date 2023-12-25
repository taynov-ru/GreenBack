package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.UserDeviceEntity

@Repository
interface UserRepository : JpaRepository<UserDeviceEntity, Long> {

}
