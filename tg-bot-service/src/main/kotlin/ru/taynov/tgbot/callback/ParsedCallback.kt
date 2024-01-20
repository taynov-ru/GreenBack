package ru.taynov.tgbot.callback

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.taynov.tgbot.callback.ParsedCallback.ParameterEnum.PAYLOAD

data class ParsedCallback(
    val callback: Callback,
) {
    constructor(callback: Callback, payload: String?) : this(callback) {
        this.payload = payload ?: ""
    }

    private var data: MutableMap<String, String?> = mutableMapOf()


    override fun toString(): String {
        return "${callback.name}|${Json.encodeToString(data)}"
    }

    var payload: String
        get() = data[PAYLOAD.name] ?: ""
        set(value) {
            data[PAYLOAD.name] = value
        }

    fun setParameter(key: ParameterEnum, value: String?) {
        data[key.name] = value
    }

    fun getParameter(key: ParameterEnum): String? {
        return data[key.name]
    }

    enum class ParameterEnum {
        PARENT_MESSAGE,
        PAYLOAD
    }
}