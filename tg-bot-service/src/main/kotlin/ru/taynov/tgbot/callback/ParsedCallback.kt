package ru.taynov.tgbot.callback

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        get() = data["payload"] ?: ""
        set(value) {
            data["payload"] = value
        }

    fun setParameter(key: String, value: String?) {
        data[key] = value
    }

    fun getParameter(key: String): String? {
        return data[key]
    }
}