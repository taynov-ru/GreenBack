package ru.taynov.esp.dto

import ru.taynov.esp.model.Command
import ru.taynov.esp.model.Param


data class SetParamsResponse(
    val params: Set<Param> = emptySet(),
    val commands: Set<Command> = emptySet(),
)