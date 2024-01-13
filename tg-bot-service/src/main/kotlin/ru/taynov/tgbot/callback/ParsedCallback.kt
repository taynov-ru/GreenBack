package ru.taynov.tgbot.callback

data class ParsedCallback(
    val callback: Callback,
    val payload: String = "",
) {
    override fun toString(): String {
        return "${callback.name}|$payload"
    }
}