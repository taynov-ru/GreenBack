package ru.taynov.tgbot.entity

import MapAttributeConverter
import ru.taynov.tgbot.state.State
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Transient

@Entity
open class UserStateEntity(
    @Id
    var userId: Long? = null,

    var username: String? = "",

    var selectedDevice: String? = null,

    @Convert(converter = MapAttributeConverter::class)
    private var payload: MutableMap<String, String?> = mutableMapOf(),
) {

    @get:Transient
    @set:Transient
    var state: State
        get() = payload["state"]?.let { State.valueOf(it) } ?: State.NONE
        set(value) {
            payload["state"] = value.name
        }

    @get:Transient
    @set:Transient
    var data: String?
        get() = payload["data"]
        set(value) {
            payload["data"] = value
        }
}