package ru.taynov.tgbot.entity


import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "user_device")
@EntityListeners(AuditingEntityListener::class)
open class UserDeviceEntity(
    @Id
    var userId: Long = -1,

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "userId")
    var deviceData: MutableList<UserEspDataEntity>? = null,
)