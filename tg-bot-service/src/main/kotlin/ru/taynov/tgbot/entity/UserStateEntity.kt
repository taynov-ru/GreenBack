package ru.taynov.tgbot.entity

import ru.taynov.tgbot.enums.State
import javax.persistence.Entity
import javax.persistence.Id

@Entity
open class UserStateEntity(
    @Id
    var userId: Long? = null,

    var username: String? = "",

    var selectedDevice: String? = null,

    var state: State = State.NONE
)