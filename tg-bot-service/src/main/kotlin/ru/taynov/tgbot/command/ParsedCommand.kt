package ru.taynov.tgbot.command

data class ParsedCommand(
    val command: Command,
    val payload: String,
)