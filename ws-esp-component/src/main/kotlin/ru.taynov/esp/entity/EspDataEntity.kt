package ru.taynov.esp.entity


import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import ru.taynov.esp.model.EspMessage
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ESP_DATA")
@EntityListeners(AuditingEntityListener::class)
open class EspDataEntity(
    @Id
    val id: String = "no_id",
    @Column(length = 2048)
    val data: String = "no_data",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

fun EspMessage.toEntity(): EspDataEntity = EspDataEntity(
    id = id,
    data = Json.encodeToString(this),
    createdAt = LocalDateTime.now(),
)