package ru.taynov.tgbot.entity

import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id

@Entity(name = "device_user_assignment")
class DeviceUserAssignmentEntity {

    @Id
    var deviceId: String = ""

    @ElementCollection(fetch = FetchType.EAGER)
    var users: MutableSet<String> = mutableSetOf()
}