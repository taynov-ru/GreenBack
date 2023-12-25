package ru.taynov.tgbot.entity


import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "user_esp_data")
@EntityListeners(AuditingEntityListener::class)
class UserEspDataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,
    var name: String = "",
    var userId: Long = -1,
    var deviceId: String? = null,
)