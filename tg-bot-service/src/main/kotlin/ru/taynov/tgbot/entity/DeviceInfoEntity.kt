package ru.taynov.tgbot.entity


import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "device_info")
open class DeviceInfoEntity(
    @Id @GeneratedValue
    val id: Long? = null,
    var name: String? = null,
    val userId: Long = -1,
    val deviceId: String? = null,
)