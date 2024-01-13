package ru.taynov.tgbot.state

data class ExtendedState(
    val state: State,
    val payload: String? = null,
)