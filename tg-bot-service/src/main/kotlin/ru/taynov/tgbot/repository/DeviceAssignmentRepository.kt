package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.DeviceUserAssignmentEntity
import ru.taynov.tgbot.entity.UserEspDataEntity

@Repository
interface DeviceAssignmentRepository : JpaRepository<DeviceUserAssignmentEntity, String> {

}
