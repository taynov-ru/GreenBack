package ru.taynov.tgbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.taynov.tgbot.entity.DeviceUserAssignmentEntity

@Repository
interface DeviceAssignmentRepository : JpaRepository<DeviceUserAssignmentEntity, String> {

}
